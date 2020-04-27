#Socket与系统调用深度分析

- 系统调用是什么
从用户应用程序的角度来看，内核是一个透明的系统层，它一直存在，但是从未真正的被注意到。进程是不知道内核的工作内容的。比如，进程不知道数据的真实物理地址，哪些数据已经被换入或换出。但是不可否认的是，进程在执行的过程中，或多或少的在与内核交互，请求内存资源、访问外设、与其它进程通信等等。为了达到这些目的，进程使用标准C库，C库里的函数调用内核函数，最终由内核负责在各个请求进程之间不失公平的进行处理。
因此，应用程序看到的内核是负责执行各种系统功能的函数集合。标准C库只是一个中间层，用于在不同的体系结构和系统之间，标准化并简化内核调用方式。
最后，大致应该明白了：系统调用是操作系统提供给应用程序访问系统资源的接口，应用程序是通过这个接口来获得操作系统的服务的，比如打开文件，读文件等。

- 内核态与用户态
在上面我们知道，应用程序要访问系统资源，必须通过系统调用。但是这里有一个问题，为什么要这么做？如果考虑到程序的执行效率，为什么不能由应用程序直接访问系统资源呢，非要间接的通过系统调用来实现？这里就涉及到linux系统的用户态和内核态概念了。
之所以要有内核态和用户态的区分，最直接的理由就是系统安全。你想想，如果用户程序能够直接访问硬件，万一操作不当，就可能面临系统崩溃的局面。刚买的一台电脑，你刚写个程序，一运行，电脑就崩，这样的代价太大了。所以，必须要区分内核态和用户态。其他的理由请自行百度。
这里说明一点：应用程序运行在用户态，当发生系统调用时，系统会自动陷入内核态。当系统调用处理完成后，再回到用户态。在x86体系中，应用程序是通过int 0x80实现从用户态到内核态的转换的。

- socket数据结构
```
linux-4.15.9/include/linux/net.h
/**
 *  struct socket - general BSD socket
 *  @state: socket state (%SS_CONNECTED, etc)
 *  @type: socket type (%SOCK_STREAM, etc)
 *  @flags: socket flags (%SOCK_NOSPACE, etc)
 *  @ops: protocol specific socket operations
 *  @file: File back pointer for gc
 *  @sk: internal networking protocol agnostic socket representation
 *  @wq: wait queue for several uses
 */
struct socket {
	socket_state		state;

	short			type;

	unsigned long		flags;

	struct socket_wq __rcu	*wq;

	struct file		*file;
	struct sock		*sk;
	const struct proto_ops	*ops;
};
```
socket的定义并未绑定具体的协议内容，这也说明了为什么需要proto_ops指针指向一个数据结构。其中包含了用于处理套接字的特定协议的函数

```
linux-4.15.9/include/linux/net.h
struct proto_ops {
	int		family;
	struct module	*owner;
	int		(*release)   (struct socket *sock);
	int		(*bind)	     (struct socket *sock,
				      struct sockaddr *myaddr,
				      int sockaddr_len);
	int		(*connect)   (struct socket *sock,
				      struct sockaddr *vaddr,
				      int sockaddr_len, int flags);
	int		(*socketpair)(struct socket *sock1,
				      struct socket *sock2);
	int		(*accept)    (struct socket *sock,
				      struct socket *newsock, int flags, bool kern);
	int		(*getname)   (struct socket *sock,
				      struct sockaddr *addr,
				      int *sockaddr_len, int peer);
	unsigned int	(*poll)	     (struct file *file, struct socket *sock,
				      struct poll_table_struct *wait);
	int		(*ioctl)     (struct socket *sock, unsigned int cmd,
				      unsigned long arg);
#ifdef CONFIG_COMPAT
	int	 	(*compat_ioctl) (struct socket *sock, unsigned int cmd,
				      unsigned long arg);
#endif
	int		(*listen)    (struct socket *sock, int len);
	int		(*shutdown)  (struct socket *sock, int flags);
	int		(*setsockopt)(struct socket *sock, int level,
				      int optname, char __user *optval, unsigned int optlen);
	int		(*getsockopt)(struct socket *sock, int level,
				      int optname, char __user *optval, int __user *optlen);
#ifdef CONFIG_COMPAT
	int		(*compat_setsockopt)(struct socket *sock, int level,
				      int optname, char __user *optval, unsigned int optlen);
	int		(*compat_getsockopt)(struct socket *sock, int level,
				      int optname, char __user *optval, int __user *optlen);
#endif
	int		(*sendmsg)   (struct socket *sock, struct msghdr *m,
				      size_t total_len);
	/* Notes for implementing recvmsg:
	 * ===============================
	 * msg->msg_namelen should get updated by the recvmsg handlers
	 * iff msg_name != NULL. It is by default 0 to prevent
	 * returning uninitialized memory to user space.  The recvfrom
	 * handlers can assume that msg.msg_name is either NULL or has
	 * a minimum size of sizeof(struct sockaddr_storage).
	 */
	int		(*recvmsg)   (struct socket *sock, struct msghdr *m,
				      size_t total_len, int flags);
	int		(*mmap)	     (struct file *file, struct socket *sock,
				      struct vm_area_struct * vma);
	ssize_t		(*sendpage)  (struct socket *sock, struct page *page,
				      int offset, size_t size, int flags);
	ssize_t 	(*splice_read)(struct socket *sock,  loff_t *ppos,
				       struct pipe_inode_info *pipe, size_t len, unsigned int flags);
	int		(*set_peek_off)(struct sock *sk, int val);
	int		(*peek_len)(struct socket *sock);

	/* The following functions are called internally by kernel with
	 * sock lock already held.
	 */
	int		(*read_sock)(struct sock *sk, read_descriptor_t *desc,
				     sk_read_actor_t recv_actor);
	int		(*sendpage_locked)(struct sock *sk, struct page *page,
					   int offset, size_t size, int flags);
	int		(*sendmsg_locked)(struct sock *sk, struct msghdr *msg,
					  size_t size);
};
```
许多函数指针都与C标准库函数同名。这不是巧合。因为C库函数会通过socketcall系统调用导向上述的函数指针。

-  socket与文件

在建立连接之后，用户空间进程使用普通的文件操作来访问套接字。这在内核中是如何实现的呢？这就多亏了VFS层的开放结构，只需要做很少的工作。因为对套接字文件描述符的文件操作，可以透明的重定向到网络子系统的代码中。套接字使用的file_operations 结构如下

```
linux-4.15.9/net/socket.c
/*
 *	Socket files have a set of 'special' operations as well as the generic file ones. These don't appear
 *	in the operation structures but are done directly via the socketcall() multiplexor.
 */

static const struct file_operations socket_file_ops = {
	.owner =	THIS_MODULE,
	.llseek =	no_llseek,
	.read_iter =	sock_read_iter,
	.write_iter =	sock_write_iter,
	.poll =		sock_poll,
	.unlocked_ioctl = sock_ioctl,
#ifdef CONFIG_COMPAT
	.compat_ioctl = compat_sock_ioctl,
#endif
	.mmap =		sock_mmap,
	.release =	sock_close,
	.fasync =	sock_fasync,
	.sendpage =	sock_sendpage,
	.splice_write = generic_splice_sendpage,
	.splice_read =	sock_splice_read,
};
```

- socketcall系统调用

linux提供了socketcall系统调用，它实现在sys_socketcall中。
sys_socketcall的任务其实并不困难，它充当“socket多路分配器”，将系统调用转到其他具体的函数执行，并传递参数，后者中的每个函数都实现了一个“更小”的系统调用

```
linux-4.15.9/net/socket.c

/* Argument list sizes for sys_socketcall */
#define AL(x) ((x) * sizeof(unsigned long))
static const unsigned char nargs[21] = {
	AL(0), AL(3), AL(3), AL(3), AL(2), AL(3),
	AL(3), AL(3), AL(4), AL(4), AL(4), AL(6),
	AL(6), AL(2), AL(5), AL(5), AL(3), AL(3),
	AL(4), AL(5), AL(4)
};


/*
 *	System call vectors.
 *
 *	Argument checking cleaned up. Saved 20% in size.
 *  This function doesn't need to set the kernel lock because
 *  it is set by the callees.
 */

SYSCALL_DEFINE2(socketcall, int, call, unsigned long __user *, args)
{
	unsigned long a[AUDITSC_ARGS];
	unsigned long a0, a1;
	int err;
	unsigned int len;

	if (call < 1 || call > SYS_SENDMMSG)
		return -EINVAL;

	len = nargs[call];
	if (len > sizeof(a))
		return -EINVAL;

	/* copy_from_user should be SMP safe. */
	if (copy_from_user(a, args, len))
		return -EFAULT;

	err = audit_socketcall(nargs[call] / sizeof(unsigned long), a);
	if (err)
		return err;

	a0 = a[0];
	a1 = a[1];

	switch (call) {
	case SYS_SOCKET:
		err = sys_socket(a0, a1, a[2]);
		break;
	case SYS_BIND:
		err = sys_bind(a0, (struct sockaddr __user *)a1, a[2]);
		break;
	case SYS_CONNECT:
		err = sys_connect(a0, (struct sockaddr __user *)a1, a[2]);
		break;
	case SYS_LISTEN:
		err = sys_listen(a0, a1);
		break;
	case SYS_ACCEPT:
		err = sys_accept4(a0, (struct sockaddr __user *)a1,
				  (int __user *)a[2], 0);
		break;
	case SYS_GETSOCKNAME:
		err =
		    sys_getsockname(a0, (struct sockaddr __user *)a1,
				    (int __user *)a[2]);
		break;
	case SYS_GETPEERNAME:
		err =
		    sys_getpeername(a0, (struct sockaddr __user *)a1,
				    (int __user *)a[2]);
		break;
	case SYS_SOCKETPAIR:
		err = sys_socketpair(a0, a1, a[2], (int __user *)a[3]);
		break;
	case SYS_SEND:
		err = sys_send(a0, (void __user *)a1, a[2], a[3]);
		break;
	case SYS_SENDTO:
		err = sys_sendto(a0, (void __user *)a1, a[2], a[3],
				 (struct sockaddr __user *)a[4], a[5]);
		break;
	case SYS_RECV:
		err = sys_recv(a0, (void __user *)a1, a[2], a[3]);
		break;
	case SYS_RECVFROM:
		err = sys_recvfrom(a0, (void __user *)a1, a[2], a[3],
				   (struct sockaddr __user *)a[4],
				   (int __user *)a[5]);
		break;
	case SYS_SHUTDOWN:
		err = sys_shutdown(a0, a1);
		break;
	case SYS_SETSOCKOPT:
		err = sys_setsockopt(a0, a1, a[2], (char __user *)a[3], a[4]);
		break;
	case SYS_GETSOCKOPT:
		err =
		    sys_getsockopt(a0, a1, a[2], (char __user *)a[3],
				   (int __user *)a[4]);
		break;
	case SYS_SENDMSG:
		err = sys_sendmsg(a0, (struct user_msghdr __user *)a1, a[2]);
		break;
	case SYS_SENDMMSG:
		err = sys_sendmmsg(a0, (struct mmsghdr __user *)a1, a[2], a[3]);
		break;
	case SYS_RECVMSG:
		err = sys_recvmsg(a0, (struct user_msghdr __user *)a1, a[2]);
		break;
	case SYS_RECVMMSG:
		err = sys_recvmmsg(a0, (struct mmsghdr __user *)a1, a[2], a[3],
				   (struct timespec __user *)a[4]);
		break;
	case SYS_ACCEPT4:
		err = sys_accept4(a0, (struct sockaddr __user *)a1,
				  (int __user *)a[2], a[3]);
		break;
	default:
		err = -EINVAL;
		break;
	}
	return err;
}
```


#测试socket底层调用过程
- 创建server.c服务器端代码

- 创建client.c客户端代码


#执行服务器端代码
```
编译服务器端代码
gcc -o server server.c
终端执行
strace ./server 127.0.0.1 12345

从下面log可以看到调用链路过程
socket->bind->listen->accept(阻塞)
log:
strace ./server 127.0.0.1 12345
execve("./server", ["./server", "127.0.0.1", "12345"], [/* 33 vars */]) = 0
brk(NULL)                               = 0x1f18000
access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64/tls/x86_64", 0x7ffeb97fc7a0) = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64/tls", 0x7ffeb97fc7a0) = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64/x86_64", 0x7ffeb97fc7a0) = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64", {st_mode=S_IFDIR|0755, st_size=4096, ...}) = 0
open("tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
open("tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
open("x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
open("libc.so.6", O_RDONLY|O_CLOEXEC)   = -1 ENOENT (No such file or directory)
open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
fstat(3, {st_mode=S_IFREG|0644, st_size=116465, ...}) = 0
mmap(NULL, 116465, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7f901d7f8000
close(3)                                = 0
access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0P\t\2\0\0\0\0\0"..., 832) = 832
fstat(3, {st_mode=S_IFREG|0755, st_size=1868984, ...}) = 0
mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f901d7f7000
mmap(NULL, 3971488, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7f901d226000
mprotect(0x7f901d3e6000, 2097152, PROT_NONE) = 0
mmap(0x7f901d5e6000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c0000) = 0x7f901d5e6000
mmap(0x7f901d5ec000, 14752, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7f901d5ec000
close(3)                                = 0
mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f901d7f6000
mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7f901d7f5000
arch_prctl(ARCH_SET_FS, 0x7f901d7f6700) = 0
mprotect(0x7f901d5e6000, 16384, PROT_READ) = 0
mprotect(0x601000, 4096, PROT_READ)     = 0
mprotect(0x7f901d815000, 4096, PROT_READ) = 0
munmap(0x7f901d7f8000, 116465)          = 0
socket(PF_INET, SOCK_STREAM, IPPROTO_IP) = 3
bind(3, {sa_family=AF_INET, sin_port=htons(12345), sin_addr=inet_addr("127.0.0.1")}, 16) = 0
listen(3, 5)                            = 0
accept(3, 0x7ffeb97fcfe0, 0x7ffeb97fcfbc) = ? ERESTARTSYS (To be restarted if SA_RESTART is set)
--- SIGWINCH {si_signo=SIGWINCH, si_code=SI_KERNEL} ---
accept(3, 
```

#执行客户端代码
```
编译服务器端代码
gcc -o client client.c
终端执行
strace ./client 127.0.0.1 12345


从下面log可以看到调用链路过程
socket-->connect-->write->read(阻塞)

log:
 strace ./client 127.0.0.1 12345
execve("./client", ["./client", "127.0.0.1", "12345"], [/* 33 vars */]) = 0
brk(NULL)                               = 0xce5000
access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
access("/etc/ld.so.preload", R_OK)      = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64/tls/x86_64", 0x7ffdc20c3330) = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64/tls", 0x7ffdc20c3330) = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64/x86_64", 0x7ffdc20c3330) = -1 ENOENT (No such file or directory)
open("/usr/local/cuda-9.0/lib64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
stat("/usr/local/cuda-9.0/lib64", {st_mode=S_IFDIR|0755, st_size=4096, ...}) = 0
open("tls/x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
open("tls/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
open("x86_64/libc.so.6", O_RDONLY|O_CLOEXEC) = -1 ENOENT (No such file or directory)
open("libc.so.6", O_RDONLY|O_CLOEXEC)   = -1 ENOENT (No such file or directory)
open("/etc/ld.so.cache", O_RDONLY|O_CLOEXEC) = 3
fstat(3, {st_mode=S_IFREG|0644, st_size=116465, ...}) = 0
mmap(NULL, 116465, PROT_READ, MAP_PRIVATE, 3, 0) = 0x7fd3609a3000
close(3)                                = 0
access("/etc/ld.so.nohwcap", F_OK)      = -1 ENOENT (No such file or directory)
open("/lib/x86_64-linux-gnu/libc.so.6", O_RDONLY|O_CLOEXEC) = 3
read(3, "\177ELF\2\1\1\3\0\0\0\0\0\0\0\0\3\0>\0\1\0\0\0P\t\2\0\0\0\0\0"..., 832) = 832
fstat(3, {st_mode=S_IFREG|0755, st_size=1868984, ...}) = 0
mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fd3609a2000
mmap(NULL, 3971488, PROT_READ|PROT_EXEC, MAP_PRIVATE|MAP_DENYWRITE, 3, 0) = 0x7fd3603d1000
mprotect(0x7fd360591000, 2097152, PROT_NONE) = 0
mmap(0x7fd360791000, 24576, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_DENYWRITE, 3, 0x1c0000) = 0x7fd360791000
mmap(0x7fd360797000, 14752, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_FIXED|MAP_ANONYMOUS, -1, 0) = 0x7fd360797000
close(3)                                = 0
mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fd3609a1000
mmap(NULL, 4096, PROT_READ|PROT_WRITE, MAP_PRIVATE|MAP_ANONYMOUS, -1, 0) = 0x7fd3609a0000
arch_prctl(ARCH_SET_FS, 0x7fd3609a1700) = 0
mprotect(0x7fd360791000, 16384, PROT_READ) = 0
mprotect(0x600000, 4096, PROT_READ)     = 0
mprotect(0x7fd3609c0000, 4096, PROT_READ) = 0
munmap(0x7fd3609a3000, 116465)          = 0
socket(PF_INET, SOCK_STREAM, IPPROTO_IP) = 3
connect(3, {sa_family=AF_INET, sin_port=htons(12345), sin_addr=inet_addr("127.0.0.1")}, 16) = 0
fstat(1, {st_mode=S_IFCHR|0620, st_rdev=makedev(136, 9), ...}) = 0
brk(NULL)                               = 0xce5000
brk(0xd06000)                           = 0xd06000
fstat(0, {st_mode=S_IFCHR|0620, st_rdev=makedev(136, 9), ...}) = 0
write(1, ">", 1>)                        = 1
read(0, 0xce5420, 1024)                 = ? ERESTARTSYS (To be restarted if SA_RESTART is set)
read(0, 
```

三次握手:

- 1.Client->tcp_connect_init(sk)->tcp_transmit_skb(sk, buff, 1, sk->sk_allocation);->Server;
  tcp_v4_connect()->tcp_connect()->tcp_transmit_skb()
- 2.Server->inet_csk_accept(struct sock *sk, int flags, int *err)->tcp_rcv_synsent_state_process->Client;
  tcp_v4_do_rcv()->tcp_rcv_state_process()->tcp_v4_conn_request()->tcp_v4_send_synack().
- 3.Client->tcp_rcv_state_process()->Server
  tcp_v4_do_rcv()->tcp_rcv_state_process().当前客户端处于TCP_SYN_SENT状态


客户端调用__sys_connect将连接请求SYN报文段发出，是第一次握手
之后服务器调用__sys_accpt4响应连接请求，完成后两次握手过程
从调用__sys_connect到__sys_accept4函数响应连接请求并返回之间就是三次握手的时间。

refers to:
https://www.cnblogs.com/ustc-hwq/p/12069661.html
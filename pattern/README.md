# Java Service Provider Interface
## 1.Overview
### Java 6 has introduced a feature for discovering and loading implementations matching a given interface: Service Provider Interface (SPI).

## 2.Terms and Definitions of Java SPI
### Java SPI defines four main components

### 2.1. Service
#### A well-known set of programming interfaces and classes that provide access to some specific application functionality or feature.

### 2.2. Service Provider Interface
#### An interface or abstract class that acts as a proxy or an endpoint to the service.
#### If the service is one interface, then it is the same as a service provider interface.Service and SPI together are well-known in the Java Ecosystem as API.

### 2.3. Service Provider
#### A specific implementation of the SPI. The Service Provider contains one or more concrete classes that implement or extend the service type.
#### A Service Provider is configured and identified through a provider configuration file which we put in the resource directory META-INF/services. The file name is the fully-qualified name of the SPI and its content is the fully-qualified name of the SPI implementation.
#### The Service Provider is installed in the form of extensions, a jar file which we place in the application classpath, the Java extension classpath or the user-defined classpath.

### 2.4. ServiceLoader
#### At the heart of the SPI is the ServiceLoader class. This has the role of discovering and loading implementations lazily. It uses the context classpath to locate provider implementations and put them in an internal cache.
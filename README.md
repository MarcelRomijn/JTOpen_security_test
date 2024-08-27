# JTOpen - Testing the security enhancement for the JTOpen library

This repository contains test code to test the security enhancement for the JTOpen library.
The security enhancement allows setting a custom `SSLSocketFactory` for making secure connections to a host.
This enhancement is available for both JTOpen connections made with the `SecureAS400` class and with the `AS400JDBCDriver` class.

**Note**: In order to successfully compile and run the tests, a built version of `jt400-dev.jar` must be in the `/lib` folder.

## Enhancement for the `SecureAS400` class

The class has a new method: `public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory)`
When this method is called, secure connections to the host will be secured using the provided `SSLSocketFactory`.

## Enhancement for the `AS400JDBCDriver` class

This class offers a property name: `AS400JDBCDriver.PROPERTY_SSL_SOCKET_FACTORY`
This can be used to set an instance of `SSLSocketFactory` in the `Properties` that is provided to method: `DriverManager.getConnection(String url, Properties info)`

## Test class: `TestHostAccessSecure`

This class uses the `SecurityHelper` class to retrieve the certificate from a host and create a custom `SSLSocketFactory` with it.
A new instance of `SecureAS400` is created and the custom `SSLSocketFactory` is set with `secureAS400.setSSLSocketFactory(sslSocketFactory)`.
Then, the `SecureAS400` is used to retrieve the serial number of the host.

This class has a `main` method and on order to run it, three system properties have to be set:
1. `systemName` :  the name of the host to connect to
1. `userId` : the user id of a user that is allowed to retrieve the system's serial number
1. `password` : the password of the user

## Test class: `TestDatabaseAccessSecure`

This class uses the `SecurityHelper` class to retrieve the certificate from a host and create a custom `SSLSocketFactory` with it.
A new instance of `Properties` is created and it is used to make the JDBC connection secure:
1. `properties.put(AS400JDBCDriver.PROPERTY_SSL_SOCKET_FACTORY, sslSocketFactory)`
1. `jdbcProperties.setProperty("secure", "true")`

This instance of `Properties` is then used to create a JDBC connection: `Connection connection = DriverManager.getConnection(jdbcUrl, jdbcProperties)`
The JDBC connection is then used to execute SQL statement: `SELECT DBNAME, NAME  FROM QSYS2.TABLES WHERE DBNAME = 'QSYS'`

This class has a `main` method and on order to run it, three system properties have to be set:
1. `systemName` :  the name of the host to connect to
1. `userId` : the user id of a user that is allowed to execute the SQL statement
1. `password` : the password of the user

## Test class `SecurityHelper`

This class has two helper methods.

### Method `collectHostCertificate`

This method:
1. Connects to a host with a TLS connection
1. Retrieves the host's certificate
1. Creates an instance of `KeyStore` with it
1. Returns the `KeyStore`

The port it connects to, is hard-coded to the secure Telnet port: `992`

### Method `buildSSLSocketFactory`

This method:
1. Gets an instance of a `TrustManagerFactory` and initializes it with the provided `KeyStore`
1. Uses the instance of `TrustManagerFactory` to initialize an instance of `SSLContext`
1. Uses the instance of `SSLContext` to get an instance of `SSLSocketFactory`

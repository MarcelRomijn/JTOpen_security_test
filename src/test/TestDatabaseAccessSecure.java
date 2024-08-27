package test;

import java.security.KeyStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import com.ibm.as400.access.AS400JDBCDriver;

public class TestDatabaseAccessSecure {

    public static void main(String[] args) throws Exception {
        String systemName = System.getProperty("systemName");
        String userId = System.getProperty("userId");
        String password = System.getProperty("password");

        if (password == null) {
            password = "";
        }

        KeyStore trustStore = SecurityHelper.collectHostCertificate(systemName, SecurityHelper.SECURE_TELNET_PORT);
        SSLSocketFactory sslSocketFactory = SecurityHelper.buildSSLSocketFactory(trustStore);

        System.out.printf("Connecting to system '%s' with userId '%s' and a password of %d characters%n", systemName, userId, password.length());

        final Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("translate binary", "true");
        jdbcProperties.setProperty("user", userId);
        jdbcProperties.setProperty("password", password);
        jdbcProperties.put(AS400JDBCDriver.PROPERTY_SSL_SOCKET_FACTORY, sslSocketFactory);
        jdbcProperties.setProperty("secure", "true");
        final String jdbcUrl = String.format("jdbc:as400://%s", systemName);

        final String selectString = "SELECT DBNAME, NAME  FROM QSYS2.TABLES WHERE DBNAME = 'QSYS'";

        String format = "%-10s  %-32s%n";
        System.out.printf(format, "DBNAME", "NAME");
        System.out.printf(format, "==========", "================================");

        try (Connection connection = DriverManager.getConnection(jdbcUrl, jdbcProperties)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectString)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        System.out.printf(format, resultSet.getString(1), resultSet.getString(2));
                    }
                }
            }
        }
    }

}

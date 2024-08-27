package test;

import java.security.KeyStore;

import javax.net.ssl.SSLSocketFactory;

import com.ibm.as400.access.SecureAS400;
import com.ibm.as400.access.SystemValue;

public class TestHostAccessSecure {

    public static void main(String[] args) throws Exception {
        String systemName = System.getProperty("systemName");
        String userId = System.getProperty("userId");
        String passwordProperty = System.getProperty("password");

        char[] password = passwordProperty == null ? new char[0] : passwordProperty.toCharArray();

        KeyStore trustStore = SecurityHelper.collectHostCertificate(systemName, SecurityHelper.SECURE_TELNET_PORT);
        SSLSocketFactory sslSocketFactory = SecurityHelper.buildSSLSocketFactory(trustStore);

        System.out.printf("Connecting securely to system '%s' with userId '%s' and a password of %d characters%n", systemName, userId, password.length);

        final SecureAS400 secureAS400 = new SecureAS400(systemName, userId, password);
        secureAS400.setSSLSocketFactory(sslSocketFactory);

        String serialNumber = ((String) (new SystemValue(secureAS400, "QSRLNBR")).getValue()).trim();
        System.out.printf("System's serial number is: %s%n", serialNumber);
    }

}

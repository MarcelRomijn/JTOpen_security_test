/*
 *
 * Copyright (C) 2003-2024 Rocket Software BV
 *
 * Security.java
 * Created on Aug 27, 2024
 *
 */
package test;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

public class SecurityHelper {

    private static final String SSLCONTEXT_ALGORITHM = "TLS";
    private static final String[] ENABLED_PROTOCOLS = new String[] { "TLSv1.2", "TLSv1.3" };

    private static final String KEYSTORE_TYPE = "pkcs12";
    private static final String KEYSTORE_PASSWORD = "password";
    private static final String KEYSTORE_ALIAS = "trust_alias";

    public static final int SECURE_TELNET_PORT = 992;

    public static KeyStore collectHostCertificate(String systemName, int port) throws GeneralSecurityException, IOException {
        System.out.printf("Collecting certificate from systen '%s' and port %d%n", systemName, port);
        KeyStore keyStore = null;
        SSLSocket sslSocket = null;
        try {
            System.out.println("    Building SSLContext");
            final SSLContext sslContext = SSLContext.getInstance(SSLCONTEXT_ALGORITHM);
            sslContext.init(null, new TrustManager[] { createTrustAllManager() }, new SecureRandom());
            final SocketFactory socketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) socketFactory.createSocket(systemName, port);
            sslSocket.setEnabledProtocols(ENABLED_PROTOCOLS);

            System.out.println("    Connecting with TLS");
            sslSocket.startHandshake();
            final SSLSession sslSession = sslSocket.getSession();

            Certificate[] peerCertificates = sslSession.getPeerCertificates();
            System.out.printf("    TLS Protocol                   : %s%n", sslSession.getProtocol());
            System.out.printf("    TLS CipherSuite                : %s%n", sslSession.getCipherSuite());
            System.out.printf("    Peer Principal                 : %s%n", sslSession.getPeerPrincipal());
            System.out.printf("    Peer certificates chain length : %d%n", peerCertificates.length);
            for (int index = 0; index < peerCertificates.length; index++) {
                Certificate certificate = peerCertificates[index];
                if (!(certificate instanceof X509Certificate)) {
                    System.err.printf("Peer certificate chain contains a non-X509 certificate: %s%n", certificate);
                    return null;
                }
                X509Certificate x509Certificate = (X509Certificate) certificate;
                System.out.printf("        [%d] : %s%n", index, x509Certificate.getSubjectX500Principal().getName());
            }

            System.out.println("    Creating trust store");
            keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            keyStore.load(null, KEYSTORE_PASSWORD.toCharArray());
            keyStore.setCertificateEntry(KEYSTORE_ALIAS, peerCertificates[0]);
        } finally {
            if (sslSocket != null) {
                try {
                    System.out.println("    Disconnecting from TLS");
                    sslSocket.close();
                } catch (final IOException e) {
                    // Deliberately deafened
                }
            }
        }
        System.out.printf("Certificate collected from systen '%s' and port %d%n", systemName, port);
        System.out.println();
        return keyStore;
    }

    public static SSLSocketFactory buildSSLSocketFactory(KeyStore trustStore) throws GeneralSecurityException {
        System.out.println("Building SSLSocketFactory from KeyStore");
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        SSLContext sslContext = SSLContext.getInstance(SSLCONTEXT_ALGORITHM);
        sslContext.init(null, trustManagers, new SecureRandom());
        System.out.println("SSLSocketFactory built from KeyStore");
        System.out.println();
        return sslContext.getSocketFactory();
    }

    /**
     * Create a {@link X509TrustManager} that accepts all certificates.
     *
     * @return the {@link X509TrustManager}
     */
    public static X509TrustManager createTrustAllManager() {
        final X509TrustManager x509TrustManager = new X509ExtendedTrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) throws CertificateException {
            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket) throws CertificateException {
            }
        };
        return x509TrustManager;
    }

}

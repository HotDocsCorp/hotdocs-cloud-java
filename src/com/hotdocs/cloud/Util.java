/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Static utility methods.
 */
public class Util {

    /**
     * Reads a whole stream into a string.
     * @param stream
     * @return
     */
    public static String readString(InputStream stream) {

        try (Scanner scanner = new Scanner(stream)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    /**
     * Copies one stream to another.
     * @param from
     * @param to
     * @throws IOException
     */
    public static void copyStream(InputStream from, OutputStream to) throws IOException {

        byte[] buffer = new byte[1024];
        int len;
        while ((len = from.read(buffer)) != -1) {
            to.write(buffer, 0, len);
        }
        from.close();
        to.close();
    }

    /**
     * Determines whether an HTTP status code indicates success.
     * @param status
     * @return
     */
    public static boolean httpOk(int status) {
        return status >= 200 && status < 300;
    }
    
    /**
     * Configures the default SSLContext to ignore certificate errors.
     * For testing only!  Do not call this in a production deployment!
     */
    public static void setSSLContextToIgnoreCertErrors() {

        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0],
                    new TrustManager[] { new IgnoreCertErrorsTrustManager() },
                    new SecureRandom());
            SSLContext.setDefault(ctx);
        } catch (NoSuchAlgorithmException|KeyManagementException ex) {
            // Since this method is for testing only, ignore all exceptions.
        }
    }
    
    /**
     * Configures an HttpsURLConnection to ignore host name errors.
     * For testing only!  Do not call this in a production deployment!
     * @param conn
     */
    public static void setConnToIgnoreHostNameErrors(HttpsURLConnection conn) {
        conn.setHostnameVerifier(new HostnameVerifier() { 
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    /**
     * Custom trust manager that ignores cert errors.
     * For testing only!  Do not use this in a production deployment!
     */
    private static class IgnoreCertErrorsTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}


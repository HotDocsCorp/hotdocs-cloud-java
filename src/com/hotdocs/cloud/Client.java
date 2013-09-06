/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

/**
 * The Client class sends requests to HotDocs Cloud Services.
 */
public class Client {

    private String subscriberId;
    private String signingKey;
    private String address;
    private String proxy;
    private int proxyPort;
    private boolean areCertErrorsIgnored = false;

    /**
     * Client constructor
     * 
     * @param subscriberId
     * @param signingKey
     */
    public Client(String subscriberId, String signingKey) {

        this(subscriberId, signingKey, "cloud.hotdocs.ws", null, 0);
    }

    /**
     * Client constructor
     * 
     * @param subscriberId
     * @param signingKey
     * @param address                    The address of HotDocs Cloud Services, e.g. "cloud.hotdocs.ws"
     */
    public Client(String subscriberId, String signingKey, String address) {

        this(subscriberId, signingKey, address, null, 0);
    }

    /**
     * Client constructor
     * 
     * @param subscriberId
     * @param signingKey
     * @param address                    The address of HotDocs Cloud Services, e.g. "cloud.hotdocs.ws"
     * @param proxy                      The address of the web proxy, e.g. "myproxy.mycompany.com"
     * @param proxyPort                  The port of the web proxy, e.g. 8888
     */
    public Client(
            String subscriberId,
            String signingKey,
            String address,
            String proxy,
            int proxyPort) {

        this.subscriberId = subscriberId;
        this.signingKey = signingKey;
        this.address = address;
        this.proxy = proxy;
        this.proxyPort = proxyPort;
    }

    /**
     * This sends a request to HotDocs Cloud Services.
     * If the request refers to a package that is not in the
     * Cloud Services cache, this method will automatically
     * upload the package and retry the request.
     * 
     * @param request              The request to send
     * @param responseBuffer       A buffer in which the response body is returned
     * @return                     The HTTP status code
     * @throws URISyntaxException
     * @throws IOException
     * @throws HmacException
     */
    public int sendRequest(Request request, StringBuilder responseBuffer)
            throws URISyntaxException, IOException, HmacException {

        int status = sendRequestImpl(request, responseBuffer);

        if (status == 404 && request.getPackageStreamGetter() != null) {
            // The package isn't in the HDCS cache, so upload it
            status = sendRequestImpl(new UploadPackageRequest(request.getPackageId(),
                    request.getPackageStreamGetter()), responseBuffer);

            if (Util.httpOk(status)) {
                // The upload succeeded, so retry the original request
                status = sendRequestImpl(request, responseBuffer);
            } else if (status == 409) {
                // The package was already in the cache.
                // This means that there was something else wrong with
                // the request URL path, so indicate this with an error 404.
                status = 404;
            }
        }

        return status;
    }
    
    /**
     * This sends a request to HotDocs Cloud Services.
     * If the request refers to a package that is not in the
     * Cloud Services cache, this method will automatically
     * upload the package and retry the request.
     * 
     * @param request              The request to send
     * @return                     The response body
     * @throws URISyntaxException
     * @throws IOException
     * @throws HmacException
     */
    public String sendRequest(Request request)
            throws URISyntaxException, IOException, HmacException {
        
        StringBuilder responseBuffer = new StringBuilder();
        int status = sendRequest(request, responseBuffer);
        if (!Util.httpOk(status)) {
            throw new IOException("HTTP error " + status);
        }
        return responseBuffer.toString();
    }
    
    /**
     * This causes the client object to ignore all certificate errors.
     * This is for testing only!  Do not call this in a production deployment!
     */
    public void ignoreCertErrors() {
        
        Util.setSSLContextToIgnoreCertErrors();
        areCertErrorsIgnored = true;
    }
   
    private int sendRequestImpl(Request request, StringBuilder responseBuffer)
            throws URISyntaxException, IOException, HmacException {

        String path = String.format("%s/%s", request.getPathPrefix(), subscriberId);

        if (request.getPackageId() != null) {
            path += "/" + request.getPackageId();
        }
        
        // Use the URI constructor to percent-encode the URL
        URL httpsUrl = (new URI("https", address, path, request.getQuery(), null)).toURL();

        Proxy httpsProxy = Proxy.NO_PROXY;
        if (proxy != null && !proxy.equals("")) {
            httpsProxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxy, proxyPort));
        }

        HttpsURLConnection conn = (HttpsURLConnection) httpsUrl.openConnection(httpsProxy);
        signAndDate(conn, request.getHmacParams());

        String method = request.getMethod();
        
        if (method.equals("POST") || method.equals("PUT")) {
            conn.setDoOutput(true);
        }
        conn.setRequestMethod(method);
        conn.setAllowUserInteraction(false);
        
        if (areCertErrorsIgnored) {
            Util.setConnToIgnoreHostNameErrors(conn);
        }
        
        InputStream contentStream = request.getContentStream();

        if (contentStream != null) {
            conn.setFixedLengthStreamingMode(request.getContentStreamGetter().getLength());
            Util.copyStream(contentStream, conn.getOutputStream());
        } else {
            conn.setFixedLengthStreamingMode(0);
        }

        int status = conn.getResponseCode();
        InputStream responseStream = Util.httpOk(status) ? conn.getInputStream() : conn.getErrorStream();
        responseBuffer.setLength(0);
        responseBuffer.append(Util.readString(responseStream));

        return status;
    }

    private void signAndDate(URLConnection conn, Collection<Object> params)
            throws HmacException {

        Date timestamp = new Date();

        ArrayList<Object> p = new ArrayList<Object>();
        p.add(timestamp);
        p.add(subscriberId);
        p.addAll(params);
        String signature = Hmac.calculateHmac(signingKey, p);
        conn.setRequestProperty("Authorization", signature);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        conn.setRequestProperty("x-hd-date", dateFormat.format(timestamp));
    }
}

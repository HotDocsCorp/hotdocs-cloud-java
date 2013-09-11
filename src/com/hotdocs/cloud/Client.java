/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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
    private MultipartMimeParser multipartParser = new MultipartMimeParser();

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
     * @param address
     *            The address of HotDocs Cloud Services, e.g. cloud.hotdocs.ws
     */
    public Client(String subscriberId, String signingKey, String address) {
        this(subscriberId, signingKey, address, null, 0);
    }

    /**
     * Client constructor
     * 
     * @param subscriberId
     * @param signingKey
     * @param address
     *            The address of HotDocs Cloud Services, e.g. cloud.hotdocs.ws
     * @param proxy
     *            The address of the web proxy, e.g. myproxy.mycompany.com
     * @param proxyPort
     *            The port of the web proxy, e.g. 8888
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
     * Sends a request to HotDocs Cloud Services and returns
     * the response in the provided StringBuilder.
     * 
     * If the request refers to a package that is not in the
     * Cloud Services cache, this method will automatically
     * upload the package and retry the request.
     * 
     * @param request
     *            The request to send
     * @param responseBuffer
     *            A buffer in which the response body is returned
     * @return The HTTP status code
     * @throws URISyntaxException
     * @throws IOException
     * @throws HmacException
     */
    public int sendRequest(Request request, StringBuilder responseBuffer)
            throws URISyntaxException, IOException, HmacException {
        responseBuffer.setLength(0);
        HttpURLConnection conn = sendRequestImpl(request);
        if (Util.getResponseStream(conn) != null) {
            responseBuffer
                    .append(Util.readString(Util.getResponseStream(conn)));
        }
        return conn.getResponseCode();
    }

    /**
     * Sends a request to HotDocs Cloud Services and returns
     * the resulting content in a file or files. If the returned
     * content is of type multipart/????, the filePath is
     * interpreted as a directory and the MIME parts are written
     * to files in that directory.
     * 
     * If the request refers to a package that is not in the
     * Cloud Services cache, this method will automatically
     * upload the package and retry the request.
     * 
     * @param request
     * @param filePath
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws HmacException
     */
    public int sendRequest(Request request, String filePath)
            throws URISyntaxException, IOException, HmacException {
        HttpURLConnection conn = sendRequestImpl(request);
        if (Util.httpOk(conn)) {
            if (conn.getContentType().startsWith("multipart")) {
                handleMultipart(conn, filePath);
            }
            else {
                FileOutputStream outStream = new FileOutputStream(filePath);
                Util.copyStream(Util.getResponseStream(conn), outStream);
            }
        }

        return conn.getResponseCode();
    }

    /**
     * Sends a request to HotDocs Cloud Services and returns
     * the response in a String. HTTP errors result in an
     * IOException being thrown.
     * 
     * If the request refers to a package that is not in the
     * Cloud Services cache, this method will automatically
     * upload the package and retry the request.
     * 
     * @param request
     *            The request to send
     * @return The response body
     * @throws URISyntaxException
     * @throws IOException
     * @throws HmacException
     */
    public String sendRequest(Request request)
            throws URISyntaxException, IOException, HmacException {
        HttpURLConnection conn = sendRequestImpl(request);
        if (!Util.httpOk(conn)) {
            throw new IOException("HTTP error " + conn.getResponseCode());
        }
        return Util.readString(Util.getResponseStream(conn));
    }

    /**
     * This causes the client object to ignore all certificate errors.
     * This is for testing only! Do not call this in a production deployment!
     */
    public void ignoreCertErrors() {
        Util.setSSLContextToIgnoreCertErrors();
        areCertErrorsIgnored = true;
    }

    /**
     * Gets an HttpURLConnection according to the
     * provided Request object.
     * 
     * @param request
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws HmacException
     */
    private HttpURLConnection getConn(Request request)
            throws URISyntaxException, IOException, HmacException {
        String path = String.format("%s/%s", request.getPathPrefix(),
                subscriberId);

        if (request.getPackageId() != null) {
            path += "/" + request.getPackageId();
        }

        if (request.getTemplateName() != null) {
            path += "/" + request.getTemplateName();
        }

        // Use the URI constructor to percent-encode the URL
        URL httpsUrl = (new URI("https", address, path, request.getQuery(),
                null)).toURL();

        Proxy httpsProxy = Proxy.NO_PROXY;
        if (proxy != null && !proxy.equals("")) {
            httpsProxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(proxy, proxyPort));
        }

        HttpsURLConnection conn = (HttpsURLConnection) httpsUrl
                .openConnection(httpsProxy);
        signAndDate(conn, request.getHmacParams());

        String method = request.getMethod();

        conn.setRequestMethod(method);
        conn.setAllowUserInteraction(false);

        if (areCertErrorsIgnored) {
            Util.setConnToIgnoreHostNameErrors(conn);
        }

        if (method.equals("POST") || method.equals("PUT")) {
            conn.setDoOutput(true);
            InputStream contentStream = request.getContentStream();
            if (contentStream != null) {
                conn.setFixedLengthStreamingMode(request
                        .getContentStreamGetter().getLength());
                Util.copyStream(contentStream, conn.getOutputStream());
            } else {
                conn.setFixedLengthStreamingMode(0);
            }
        }

        return conn;
    }

    // Common implementation of sendRequest
    private HttpURLConnection sendRequestImpl(Request request)
            throws URISyntaxException, IOException, HmacException {
        HttpURLConnection conn = getConn(request);

        if (conn.getResponseCode() == 404
                && request.getPackageStreamGetter() != null) {

            // The package isn't in the HDCS cache, so upload it
            HttpURLConnection uploadConn = getConn(new UploadPackageRequest(
                    request.getPackageId(),
                    request.getPackageStreamGetter()));

            if (Util.httpOk(uploadConn)) {
                uploadConn.disconnect();
                conn.disconnect();
                // The upload succeeded, so retry the original request.
                conn = getConn(request);
            } else if (uploadConn.getResponseCode() != 409) {
                conn.disconnect();
                conn = uploadConn; // Return the response from the upload.
            } else {
                uploadConn.disconnect();
            }

            // If the upload status is 409, then the package was already in the
            // cache, so we'll return the response from the original request.
        }

        return conn;
    }

    // Adds HMAC and date headers to a connection
    private void signAndDate(URLConnection conn, Collection<Object> params)
            throws HmacException {
        Date timestamp = new Date();

        ArrayList<Object> p = new ArrayList<Object>();
        p.add(timestamp);
        p.add(subscriberId);
        p.addAll(params);
        String signature = Hmac.calculateHmac(signingKey, p);
        conn.setRequestProperty("Authorization", signature);

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        conn.setRequestProperty("x-hd-date", dateFormat.format(timestamp));
    }

    private void handleMultipart(HttpURLConnection conn, String dir)
            throws IOException {
        new File(dir).mkdir();
        multipartParser.writePartsToStreams(
                conn.getInputStream(),
                new FileOutputStreamGetter(dir),
                Util.getNamedValue(conn.getContentType(), ";", "boundary"));
    }
}

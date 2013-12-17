/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

/**
 * Encapsulates the parameters for a ResumeSession request.
 */
public class ResumeSessionRequest extends Request {
    
    private String snapshot;
    private boolean snapshotParsed = false;
    
    public ResumeSessionRequest(String snapshot) {
        this.snapshot = snapshot;
        this.contentStreamGetter = new StringInputStreamGetter(snapshot);
    }
    
    @Override
    public String getPackageId() {
        if (!snapshotParsed) {
            parseSnapshot();
        }
        return packageId;
    }
    
    @Override
    public String getBillingRef() {
        if (!snapshotParsed) {
            parseSnapshot();
        }
        return billingRef;
    }
    
    @Override
    String getPathPrefix() {
        return "/embed/resumesession";
    }

    @Override
    String getQuery() {
        return null;
    }

    @Override
    String getMethod() {
        return "POST";
    }

    @Override
    Collection<Object> getHmacParams() {
        return Arrays.asList((Object)snapshot);
    }
    
    /**
     * This saves us from having to include a JSON parsing library.
     * This is quite limited -- it simply returns the value of the
     * first occurrence of a string variable with the given name,
     * no matter how deeply nested it is.
     * @param varName
     * @return String value
     */
    private String getStringValueFromJson(String json, String varName) {
        Pattern pattern = Pattern.compile("\"" + varName
                + "\"[\\s\\r\\n]*:[\\s\\r\\n]*\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    private void parseSnapshot() {
        // When this method is called, it means that the client needs to
        // upload the package.  ResumeSessionRequest is unique in that it
        // doesn't require the consumer to provide the package ID or billing
        // ref.  Instead, we extract those two items from the snapshot.
        int indexOfPound = snapshot.indexOf('#');
        String base64 = snapshot.substring(0, indexOfPound);
        byte[] bytes = DatatypeConverter.parseBase64Binary(base64);
        String json;
        try {
            json = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            json = ""; // This will never occur, as all platforms support UTF8.
        }
        
        setPackageId(getStringValueFromJson(json, "PackageID"));
        setBillingRef(getStringValueFromJson(json, "BillingRef"));

        snapshotParsed = true;
    }
}

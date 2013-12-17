/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * An abstract class that encapsulates the parameters
 * in a HotDocs Cloud Services request.
 */
abstract class Request {
    
    protected String billingRef;
    protected String packageId;
    protected InputStreamGetter contentStreamGetter;
    protected InputStreamGetter packageStreamGetter;
    protected String templateName;
    
    Request() {
    }
    
    Request(String packageId, InputStreamGetter packageStreamGetter) {
        this.packageId = packageId;
        this.packageStreamGetter = packageStreamGetter;
    }
    
    Request(String packageId, String packageFile) {
        this(packageId, new FileInputStreamGetter(packageFile));
    }
    
    Request(String packageId, String packageFile, String content) {
        this(packageId, packageFile);
        contentStreamGetter = new StringInputStreamGetter(content);
    }
    
    Request(
            String packageId,
            String packageFile,
            String content,
            String billingRef) {
        this(packageId, packageFile, content);
        this.billingRef = billingRef;
    }
    
    // Getters/Setters

    String getBillingRef() {
        return billingRef;
    }
    
    void setBillingRef(String billingRef) {
        this.billingRef = billingRef;
    }
    
    String getPackageId() {
        return packageId;
    }
    
    void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    InputStreamGetter getContentStreamGetter() {
        return contentStreamGetter;
    }
    
    void setContentStreamGetter(InputStreamGetter contentStreamGetter) {
        this.contentStreamGetter = contentStreamGetter;
    }
    
    InputStream getContentStream() throws IOException {
        if (contentStreamGetter != null) {
            return contentStreamGetter.getStream();
        }
        return null;
    }
    
    InputStreamGetter getPackageStreamGetter() {
        return packageStreamGetter;
    }
    
    void setPackageStreamGetter(InputStreamGetter packageStreamGetter) {
        this.packageStreamGetter = packageStreamGetter;
    }
    
    InputStream getPackageStream() throws IOException {
        if (packageStreamGetter != null) {
            return packageStreamGetter.getStream();
        }
        return null;
    }
    
    String getTemplateName() {
        return templateName;
    }
    
    void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    // The following methods must be implemented
    // in derived classes.
    abstract String getPathPrefix();
    abstract String getQuery();
    abstract String getMethod();
    abstract Collection<Object> getHmacParams();
}

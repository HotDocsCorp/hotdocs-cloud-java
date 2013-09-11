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
public abstract class Request {
    
    protected String billingRef;
    protected String packageId;
    protected InputStreamGetter contentStreamGetter;
    protected InputStreamGetter packageStreamGetter;
    protected String templateName;
    
    public Request() {
    }
    
    public Request(String packageId, InputStreamGetter packageStreamGetter) {
        this.packageId = packageId;
        this.packageStreamGetter = packageStreamGetter;
    }
    
    public Request(String packageId, String packageFile) {
        this(packageId, new FileInputStreamGetter(packageFile));
    }
    
    public Request(String packageId, String packageFile, String content) {
        this(packageId, packageFile);
        contentStreamGetter = new StringInputStreamGetter(content);
    }
    
    public Request(
            String packageId,
            String packageFile,
            String content,
            String billingRef) {
        this(packageId, packageFile, content);
        this.billingRef = billingRef;
    }
    
    // Getters/Setters

    public String getBillingRef() {
        return billingRef;
    }
    
    public void setBillingRef(String billingRef) {
        this.billingRef = billingRef;
    }
    
    public String getPackageId() {
        return packageId;
    }
    
    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public InputStreamGetter getContentStreamGetter() {
        return contentStreamGetter;
    }
    
    public void setContentStreamGetter(InputStreamGetter contentStreamGetter) {
        this.contentStreamGetter = contentStreamGetter;
    }
    
    public InputStream getContentStream() throws IOException {
        if (contentStreamGetter != null) {
            return contentStreamGetter.getStream();
        }
        return null;
    }
    
    public InputStreamGetter getPackageStreamGetter() {
        return packageStreamGetter;
    }
    
    public void setPackageStreamGetter(InputStreamGetter packageStreamGetter) {
        this.packageStreamGetter = packageStreamGetter;
    }
    
    public InputStream getPackageStream() throws IOException {
        if (packageStreamGetter != null) {
            return packageStreamGetter.getStream();
        }
        return null;
    }
    
    public String getTemplateName() {
        return templateName;
    }
    
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
    
    // The following methods must be implemented
    // in derived classes.
    abstract String getPathPrefix();
    abstract String getQuery();
    abstract String getMethod();
    abstract Collection<Object> getHmacParams();
}

/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.InputStream;
import java.util.Collection;

/**
 * An abstract class that encapsulates the parameters
 * in a HotDocs Cloud Services request.
 */
public abstract class Request {
    
    protected String billingRef;
    protected String packageId;
    protected StreamGetter contentStreamGetter;
    protected StreamGetter packageStreamGetter;
    
    public Request() {
    }
    
    public Request(String packageId, StreamGetter packageStreamGetter) {
        this.packageId = packageId;
        this.packageStreamGetter = packageStreamGetter;
    }
    
    public Request(String packageId, String packageFile) {
        this(packageId, new FileStreamGetter(packageFile));
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

    public StreamGetter getContentStreamGetter() {
        return contentStreamGetter;
    }
    
    public void setContentStreamGetter(StreamGetter contentStreamGetter) {
        this.contentStreamGetter = contentStreamGetter;
    }
    
    public InputStream getContentStream() {
        if (contentStreamGetter != null) {
            return contentStreamGetter.getStream();
        }
        return null;
    }
    
    public StreamGetter getPackageStreamGetter() {
        return packageStreamGetter;
    }
    
    public void setPackageStreamGetter(StreamGetter packageStreamGetter) {
        this.packageStreamGetter = packageStreamGetter;
    }
    
    public InputStream getPackageStream() {
        if (packageStreamGetter != null) {
            return packageStreamGetter.getStream();
        }
        return null;
    }
    
    // The following methods must be implemented
    // in derived classes.
    abstract String getPathPrefix();
    abstract String getQuery();
    abstract String getMethod();
    abstract Collection<Object> getHmacParams();
}

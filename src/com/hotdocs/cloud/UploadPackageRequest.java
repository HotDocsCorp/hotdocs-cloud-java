/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the parameters for an UploadPackage request.
 */
public class UploadPackageRequest extends Request {
    
    public UploadPackageRequest(String packageId, StreamGetter packageStreamGetter) {
        this.packageId = packageId;
        this.contentStreamGetter = packageStreamGetter;
    }

    @Override
    String getPathPrefix() {
        return "/hdcs";
    }

    @Override
    String getQuery() {
        return null;
    }

    @Override
    String getMethod() {
        return "PUT";
    }

    @Override
    Collection<Object> getHmacParams() {
        return Arrays.asList(
                packageId,
                (Object) null,
                Boolean.TRUE,
                billingRef);
    }
}

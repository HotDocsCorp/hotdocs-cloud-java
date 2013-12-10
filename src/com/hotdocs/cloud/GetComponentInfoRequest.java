/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the parameters for a GetComponentInfo request.
 */
public class GetComponentInfoRequest extends Request {
    
    private boolean includeDialogs = false;
    
    public GetComponentInfoRequest(String packageId, String packageFile) {
        super(packageId, packageFile);
    }

    public GetComponentInfoRequest(
            String packageId,
            String packageFile,
            String templateName,
            boolean includeDialogs) {
        super(packageId, packageFile);
        this.templateName = templateName;
        this.includeDialogs = includeDialogs;
    }
    @Override
    String getPathPrefix() {
        return "/hdcs/componentinfo";
    }

    @Override
    String getQuery() {
        if (includeDialogs) {
            return "includedialogs=true";
        }
        return null;
    }

    @Override
    String getMethod() {
        return "GET";
    }

    @Override
    Collection<Object> getHmacParams() {
        return Arrays.asList(
                (Object)packageId,
                templateName,
                new Boolean(false),
                billingRef,
                new Boolean(includeDialogs));
    }

}

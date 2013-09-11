/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsulates the parameters for a ResumeSession request.
 */
public class ResumeSessionRequest extends Request {
    
    private String snapshot;
    
    public ResumeSessionRequest(String snapshot) {
        this.snapshot = snapshot;
        this.contentStreamGetter = new StringInputStreamGetter(snapshot);
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
}

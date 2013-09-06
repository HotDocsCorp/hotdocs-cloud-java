/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class StringStreamGetter implements StreamGetter {
    
    private String str;
    
    public StringStreamGetter(String str) {
        this.str = str;
    }
    
    @Override
    public int getLength() {
        return str.length();
    }

    @Override
    public InputStream getStream() {
        try {
            return new ByteArrayInputStream(str.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException ex) {
            return null; // This exception will never occur, since UTF-8 is a supported encoding.
        }
    }

}

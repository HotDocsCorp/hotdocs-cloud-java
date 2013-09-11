/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Use this with MultipartMimeParser.
 */
public interface OutputStreamGetter {
    OutputStream getStream(Map<String, String> headers) throws IOException;
}

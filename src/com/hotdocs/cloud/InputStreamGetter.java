/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.IOException;
import java.io.InputStream;

/**
 * A delegate for obtaining an input stream and its length.
 */
public interface InputStreamGetter {
    // Return a -1 from getLength() if the length is unknown.
    int getLength();
    InputStream getStream() throws IOException;
}

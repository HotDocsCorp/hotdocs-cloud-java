/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.InputStream;

/**
 * A delegate for obtaining a stream and its length.
 */
public interface StreamGetter {
    int getLength();
    InputStream getStream();
}

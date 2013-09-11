/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * An InputStreamGetter that gets a stream from a file.
 */
public class FileInputStreamGetter implements InputStreamGetter {

    private String filePath;
    
    public FileInputStreamGetter(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int getLength() {
        File file = new File(filePath);
        return (int)file.length();
    }

    @Override
    public InputStream getStream() throws FileNotFoundException {
        return new FileInputStream(filePath);
    }

}

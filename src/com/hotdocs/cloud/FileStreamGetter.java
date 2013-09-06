/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileStreamGetter implements StreamGetter {
    
    private String filePath;
    
    public FileStreamGetter(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public int getLength() {
        File file = new File(filePath);
        return (int)file.length();
    }

    @Override
    public InputStream getStream() {
         try {
            return new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            return null; // A null stream indicates that the file was not found.
        }
    }

}

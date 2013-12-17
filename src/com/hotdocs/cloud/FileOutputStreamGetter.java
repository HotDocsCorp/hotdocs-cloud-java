package com.hotdocs.cloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * An OutputStreamGetter that routes multipart MIME parts to files.
 */
class FileOutputStreamGetter implements OutputStreamGetter {
    
    private String dir;
    
    FileOutputStreamGetter(String dir) {
        this.dir = dir;
    }
    
    /**
     * Returns a file stream, where the file is named after the filename value
     * in the Content-Disposition header.
     */
    @Override
    public OutputStream getStream(Map<String, String> headers)
            throws FileNotFoundException {
        String fileName = null;
        String disp = headers.get("Content-Disposition");
        if (disp != null) {
            fileName = Util.getNamedValue(disp, ";", "filename");
            if (fileName != null) {
                return new FileOutputStream(new File(dir, fileName));
            }
        }
        return null;
    }

}

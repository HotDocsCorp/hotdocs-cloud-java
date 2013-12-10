/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses an input stream of multipart MIME content.
 */
public class MultipartMimeParser {

    /**
     * A subclass of ByteArrayOutputStream that allows access to the buffer.
     */
    private class ScratchPadOutputStream extends ByteArrayOutputStream {
        private ScratchPadOutputStream() {
            // Allocate a scratchpad large enough for a long header field,
            // so reallocations will be rare.
            super(512);
        }

        private byte[] getBuf() {
            return buf;
        }
    }

    private static final byte[] CRLF = new byte[] { 0x0d, 0x0a };
    private static final byte DASH = 0x2d;

    private ScratchPadOutputStream scratchPad = new ScratchPadOutputStream();
    private StreamSplitter splitter = new StreamSplitter();

    /**
     * MultipartMimeParser constructor
     */
    public MultipartMimeParser() {
    }

    /**
     * Writes the MIME parts from streamIn to the provided output streams.
     * Each output stream is closed after the MIME part is written to it.
     * 
     * @param streamIn
     * @param outputStreamGetter
     *            Provides an output stream based on MIME part header values.
     * @param boundary
     * @throws IOException
     */
    public void writePartsToStreams(
            InputStream streamIn,
            OutputStreamGetter outputStreamGetter,
            String boundary)
            throws IOException {
        byte[] boundaryBytes = ("\r\n--" + boundary).getBytes("UTF-8");
        splitter.init(streamIn);

        // Discard everything until the first boundary, and skip the CR-LF.
        splitter.writeUntilPattern(null, boundaryBytes);
        splitter.writeBytes(null, 2);

        do {
            // Get the output stream and copy the input stream to it
            // until we find the boundary in the input stream.
            OutputStream streamOut = outputStreamGetter
                    .getStream(getHeaders(streamIn));
            splitter.writeUntilPattern(streamOut, boundaryBytes);

            // Grab the 2 bytes that follow the boundary.
            scratchPad.reset();
            splitter.writeBytes(scratchPad, 2);

            // Close the stream.
            if (streamOut != null) {
                streamOut.close();
            }

            // The two bytes will be CR-LF for normal boundaries,
            // but they will be "--" for the terminating boundary.
        } while (scratchPad.getBuf()[0] != DASH
                || scratchPad.getBuf()[1] != DASH);
    }

    /**
     * Gets the MIME part headers and returns them in a name-value table.
     * 
     * @param streamIn
     * @return
     * @throws IOException
     */
    private Map<String, String> getHeaders(InputStream streamIn)
            throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        String line;

        do {
            scratchPad.reset();
            int length = splitter.writeUntilPattern(scratchPad, CRLF);
            line = new String(scratchPad.getBuf(), 0, length);
            if (length > 0) {
                // Split into name and value, and store in the table
                String[] header = line.split(":", 2);
                headers.put(header[0].trim(), header[1].trim());
            }
        } while (!line.isEmpty());

        return headers;
    }
}

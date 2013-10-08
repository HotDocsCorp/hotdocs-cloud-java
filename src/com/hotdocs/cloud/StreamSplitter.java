/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is a helper for the MultipartMimeParser class.
 * 
 * It copies an input stream to an output stream or to multiple output streams.
 * For each copy operation, you can copy a certain number of bytes, or copy
 * until a given pattern is found.
 * 
 * This is implemented with a circular buffer to handle arbitrarily long
 * streams.
 */
class StreamSplitter {
    private static final int defaultBufferSize = 8 * 1024;

    private byte[] buffer;
    private int head;
    private int virtualSize;
    private int physicalSize;
    private InputStream streamIn;

    StreamSplitter() {
        buffer = new byte[defaultBufferSize];
        physicalSize = defaultBufferSize;
    }

    StreamSplitter(int bufferSize) {
        buffer = new byte[bufferSize];
        physicalSize = bufferSize;
    }

    /**
     * Starts a new parsing session.
     * 
     * @param streamIn
     */
    void init(InputStream streamIn) {
        this.streamIn = streamIn;
        head = 0;
        virtualSize = 0;
    }

    /**
     * Copies N bytes to the output stream.
     * 
     * @param streamOut
     * @param count
     * @throws IOException
     */
    void writeBytes(OutputStream streamOut, int count) throws IOException {
        while (count > 0)
        {
            fillFromStream();
            count -= writeToStream(streamOut, Math.min(count, virtualSize));
        }
    }

    /**
     * Copies from the input to the output stream until it finds the pattern.
     * The pattern is not written to the output, and is skipped in the input.
     * 
     * @param streamOut
     * @param pattern
     * @return The number of bytes written.
     * @throws IOException
     */
    int writeUntilPattern(OutputStream streamOut, byte[] pattern)
            throws IOException {
        int indexOfPattern;
        int bytesWritten = 0;

        do {
            int bytesRead = fillFromStream();
            indexOfPattern = indexOf(pattern);
            if (indexOfPattern == -1) { // No patterns in buffer
                if (bytesRead == 0) { // End of stream
                    throw new IOException("Pattern not found.");
                }
                // Here we try to be clever by leaving pattern.length-1 bytes
                // unwritten. That way, if we have part of the pattern at the
                // end of the buffer, it will be joined with the other part
                // when we fill the buffer again, making a whole pattern,
                // which will be detected in the next call to indexOf(pattern).
                bytesWritten += writeToStream(
                        streamOut,
                        virtualSize - (pattern.length - 1));
            }
        } while (indexOfPattern == -1);

        // Write out the rest until the pattern, then skip the pattern.
        bytesWritten += writeToStream(streamOut, indexOfPattern);
        skip(pattern.length);

        return bytesWritten;
    }

    /**
     * Reads as many bytes as it can to fill the empty space in the buffer.
     * 
     * @return The number of bytes read.
     * @throws IOException
     */
    private int fillFromStream() throws IOException {
        int oldVirtualSize = virtualSize;

        // If the physical buffer isn't full
        if (virtualSize < physicalSize) {
            // If the virtual buffer doesn't wrap
            if (head + virtualSize < physicalSize) {
                // streamIn.read may return -1, so force it to be at least 0.
                virtualSize += Math.max(streamIn.read(buffer, getTail(),
                        physicalSize - getTail()), 0);

                // If we got all the bytes we asked for, then head+virtualSize
                // now equals physicalSize. We'll fill in the space at the
                // beginning of the physical buffer with the following read.
            }

            // If the virtual buffer wraps
            if (head + virtualSize >= physicalSize) {
                // streamIn.read may return -1, so force it to be at least 0.
                virtualSize += Math.max(streamIn.read(buffer, getTail(),
                        physicalSize - virtualSize), 0);
            }
        }
        return virtualSize - oldVirtualSize;
    }

    /**
     * Writes up to N bytes to the output stream. If N is greater than
     * the number of bytes available, only the available bytes will be written.
     * 
     * @param streamOut
     * @param count
     * @return The number of bytes written.
     * @throws IOException
     */
    private int writeToStream(OutputStream streamOut, int count)
            throws IOException {
        int oldVirtualSize = virtualSize;
        if (virtualSize > 0) { // If the buffer isn't empty
            int bytesToWrite;

            // If the virtual buffer wraps
            if (head + virtualSize >= physicalSize && count > 0) {
                bytesToWrite = Math.min(count, physicalSize - head);
                if (streamOut != null) {
                    streamOut.write(buffer, head, bytesToWrite);
                }
                count -= bytesToWrite;
                skip(bytesToWrite); // head may wrap to zero with this call
            }

            // If the virtual buffer doesn't wrap
            if (head + virtualSize < physicalSize && count > 0) {
                bytesToWrite = Math.min(count, getTail() - head);
                if (streamOut != null) {
                    streamOut.write(buffer, head, bytesToWrite);
                }
                skip(bytesToWrite);
            }
        }
        return oldVirtualSize - virtualSize;
    }

    // Add two numbers, wrapping at the end of the buffer.
    private int plus(int i, int j) {
        return (i + j) % buffer.length;
    }

    // Get byte from index i in the virtual buffer.
    private byte getByte(int i) {
        return buffer[plus(head, i)];
    }

    // Get the position in the physical buffer just beyond
    // the end of the virtual buffer.
    private int getTail() {
        return plus(head, virtualSize);
    }

    // Move the head of the virtual buffer forward.
    private void skip(int count) {
        head = plus(head, count);
        virtualSize -= count;
    }

    // This uses a naive search algorithm that backtracks after
    // partial matches, which is fine for our purposes.
    private int indexOf(byte[] pattern) {
        int count = virtualSize - (pattern.length - 1);
        for (int i = 0; i < count; i++) {
            for (int j = 0; getByte(i + j) == pattern[j]; j++) {
                if (j == pattern.length - 1) {
                    return i;
                }
            }
        }
        return -1;
    }
}

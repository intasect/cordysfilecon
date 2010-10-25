
/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.cordys.coe.ac.fileconnector.methods.largexml;

import com.cordys.coe.util.FileUtils;

import java.io.IOException;
import java.io.Reader;

import java.nio.CharBuffer;

/**
 * Reader which combines input from multiple readers. This works like java.io.SequenceInputStream
 *
 * @author  mpoyhone
 */
public class SequenceReader extends Reader
{
    /**
     * Pointer to the current reader in the readers array.
     */
    private int currentReader;
    /**
     * Contains readers.
     */
    private Reader[] readers;

    /**
     * Reads from the given array of readers.
     *
     * @param  readers
     */
    public SequenceReader(Reader[] readers)
    {
        this.readers = readers;
    }

    /**
     * Constructor for SequenceReader. Reads from the two given readers.
     *
     * @param  r1  First reader.
     * @param  r2  Second reader.
     */
    public SequenceReader(Reader r1, Reader r2)
    {
        this.readers = new Reader[] { r1, r2 };
    }

    /**
     * @see  java.io.Reader#close()
     */
    @Override
    public void close()
               throws IOException
    {
        for (Reader r : readers)
        {
            FileUtils.closeReader(r);
        }
    }

    /**
     * @see  java.io.Reader#mark(int)
     */
    @Override
    public void mark(int readAheadLimit)
              throws IOException
    {
        throw new IOException("mark() method is not supported.");
    }

    /**
     * @see  java.io.Reader#markSupported()
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }

    /**
     * @see  java.io.Reader#read()
     */
    @Override
    public int read()
             throws IOException
    {
        Reader r = getCurrent();

        while (r != null)
        {
            int ch = r.read();

            if (ch < 0)
            {
                r = getNext();
                continue;
            }
            else
            {
                return ch;
            }
        }

        return -1;
    }

    /**
     * @see  java.io.Reader#read(char[])
     */
    @Override
    public int read(char[] cbuf)
             throws IOException
    {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * @see  java.io.Reader#read(java.nio.CharBuffer)
     */
    @Override
    public int read(CharBuffer target)
             throws IOException
    {
        int count = 0;
        Reader r = getCurrent();

        while ((r != null) && (target.remaining() > 0))
        {
            int read = r.read(target);

            if (read < 0)
            {
                r = getNext();
                continue;
            }

            count += read;
        }

        return (count > 0) ? count : -1;
    }

    /**
     * @see  java.io.Reader#read(char[], int, int)
     */
    @Override
    public int read(char[] cbuf, int off, int len)
             throws IOException
    {
        int count = 0;
        int currentPos = off;
        int remaining = len;
        Reader r = getCurrent();

        while ((r != null) && (remaining > 0))
        {
            int read = r.read(cbuf, currentPos, remaining);

            if (read < 0)
            {
                r = getNext();
                continue;
            }

            count += read;
            currentPos += read;
            remaining -= read;
        }

        return (count > 0) ? count : -1;
    }

    /**
     * @see  java.io.Reader#ready()
     */
    @Override
    public boolean ready()
                  throws IOException
    {
        Reader r = getCurrent();

        return (r != null) ? r.ready() : false;
    }

    /**
     * @see  java.io.Reader#reset()
     */
    @Override
    public void reset()
               throws IOException
    {
        throw new IOException("reset() method is not supported.");
    }

    /**
     * @see  java.io.Reader#skip(long)
     */
    @Override
    public long skip(long n)
              throws IOException
    {
        long count = 0;

        for (; count < n; count++)
        {
            if (read() < 0)
            {
                break;
            }
        }

        return count;
    }

    /**
     * Returns the current reader.
     *
     * @return  Current reader or <code>null</code> if no readers are left.
     */
    private Reader getCurrent()
    {
        if (currentReader >= readers.length)
        {
            return null;
        }

        return readers[currentReader];
    }

    /**
     * Returns the next reader and closes the current one.
     *
     * @return  Next reader or <code>null</code> if no readers are left.
     */
    private Reader getNext()
    {
        if (currentReader < readers.length)
        {
            // Close the reader and allow it to be garbage collected.
            FileUtils.closeReader(readers[currentReader]);
            readers[currentReader] = null;

            // Move to the next one.
            currentReader++;
        }

        return getCurrent();
    }
}

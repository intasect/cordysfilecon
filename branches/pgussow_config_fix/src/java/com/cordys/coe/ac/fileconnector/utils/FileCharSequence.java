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
 package com.cordys.coe.ac.fileconnector.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * An utility class to allow a file to be treated as a character sequence. This allows the user to
 * access the file as it was a simple string. The class keeps the current position in a buffer so
 * local access is fast, reading from positions that are more apart than the buffer size will be
 * much slower.<br/>
 * <b>NOTE: This class does NOT work with multibyte UNICODE encodings!</b>
 *
 * @author  mpoyhone
 */
public class FileCharSequence
    implements CharSequence
{
    /**
     * The buffer resize increment.
     */
    private static final int DEFAULT_BUFFER_RESIZE_INCREMENT = 2048;
    /**
     * Default character set for input text.
     */
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    /**
     * Character set decoder used to convert the bytes to a string.
     */
    private CharsetDecoder cdDecoder = null;

    /**
     * The file channel object that is used to read the file.
     */
    private FileChannel fcFileChannel;
    /**
     * Buffer is increased by this amount every time a it needs to be resized.
     */
    private int iBufferResizeIncrement = DEFAULT_BUFFER_RESIZE_INCREMENT;
    /**
     * The sequence length. Usually file length - lFileOffset.
     */
    private int iLength;
    /**
     * The file length.
     */
    private long lFileLength;
    /**
     * Current offset in the file.
     */
    private long lFileOffset;
    /**
     * Cached string of baBuffer.
     */
    private StringBuffer sbBuffer = null;

    /**
     * Creates a new FileCharSequence object.
     *
     * @param   fcFileChannel  The file object that is used to access to file.
     * @param   lFileLength    File length.
     * @param   lFileOffset    The offset in the file that indicates the sequence start position.
     *
     * @throws  IOException  Thrown if file access failed.
     */
    public FileCharSequence(FileChannel fcFileChannel, long lFileLength, long lFileOffset)
                     throws IOException
    {
        initialize(fcFileChannel, lFileLength, lFileOffset, null);
    }

    /**
     * Creates a new FileCharSequence object.
     *
     * @param   fcFileChannel           The file object that is used to access to file.
     * @param   lFileLength             File length.
     * @param   lFileOffset             The offset in the file that indicates the sequence start
     *                                  position.
     * @param   iBufferResizeIncrement  Buffer is increased by this amount every time a it needs to
     *                                  be resized.
     *
     * @throws  IOException  Thrown if file access failed.
     */
    public FileCharSequence(FileChannel fcFileChannel, long lFileLength, long lFileOffset,
                            int iBufferResizeIncrement)
                     throws IOException
    {
        this.iBufferResizeIncrement = iBufferResizeIncrement;
        initialize(fcFileChannel, lFileLength, lFileOffset, null);
    }

    /**
     * Creates a new FileCharSequence object.
     *
     * @param   fcFileChannel           The file object that is used to access to file.
     * @param   lFileLength             File length.
     * @param   lFileOffset             The offset in the file that indicates the sequence start
     *                                  position.
     * @param   iBufferResizeIncrement  Buffer is increased by this amount every time a it needs to
     *                                  be resized.
     * @param   cCharsetName            Character set to be used. This must be a single byte
     *                                  character set.
     *
     * @throws  IOException  Thrown if file access failed.
     */
    public FileCharSequence(FileChannel fcFileChannel, long lFileLength, long lFileOffset,
                            int iBufferResizeIncrement, Charset cCharsetName)
                     throws IOException
    {
        this.iBufferResizeIncrement = iBufferResizeIncrement;
        initialize(fcFileChannel, lFileLength, lFileOffset, cCharsetName);
    }

    /**
     * Returns the character at the specified position. If the position is outside the current
     * buffer position, the buffer is extended to cover the new area.
     *
     * @param   iIndex  The sequence position.
     *
     * @return  The character at the specified position, or zero on error.
     */
    public char charAt(int iIndex)
    {
        if ((iIndex < 0) || (iIndex >= iLength))
        {
            throw new IllegalArgumentException("Index " + iIndex +
                                               " is outside the sequence range.");
        }

        try
        {
            // Check if we need to re-align the buffer.
            if ((sbBuffer == null) || (iIndex >= (sbBuffer.length())))
            {
                resizeBuffer(iIndex);

                if ((sbBuffer == null) || (iIndex >= (sbBuffer.length())))
                {
                    // Possibly end of file.
                    return 0;
                }
            }

            assert (iIndex >= 0) && (iIndex < sbBuffer.length());

            return sbBuffer.charAt(iIndex);
        }
        catch (IOException e)
        {
            return 0;
        }
    }

    /**
     * Return the sequence length.
     *
     * @return  The sequence length.
     */
    public int length()
    {
        return iLength;
    }

    /**
     * Resets the sequence to the beginning of the file.
     *
     * @throws  IOException  Thrown on file access error.
     */
    public void reset()
               throws IOException
    {
        reset(0);
    }

    /**
     * Resets the sequence to the give file offset.
     *
     * @param   lNewFileOffset  The file offset that is to be the new sequence stating point.
     *
     * @throws  IOException  Thrown on file access error.
     */
    public void reset(long lNewFileOffset)
               throws IOException
    {
        // Calculate the new length.
        long lLength = lFileLength - lNewFileOffset;

        // Do some sanity checks.
        if (lLength >= Integer.MAX_VALUE)
        {
            throw new IOException("File bigger than 2GB: " + fcFileChannel);
        }

        // Check if we can save some of the old buffer
        if (sbBuffer != null)
        {
            long lOld = lFileOffset;
            boolean bCopySuccessful = false;

            // The new offset must be higher than the old one as otherwise
            // we won't have the new data in the buffer.
            if ((lNewFileOffset > lOld) && (lNewFileOffset < (lOld + sbBuffer.length())))
            {
                int iStart = (int) (lNewFileOffset - lOld);

                // Check that we have something to copy and don't copy too small amount
                if (iStart < (sbBuffer.length() - 128))
                {
                    StringBuffer sbNewBuffer = new StringBuffer(iBufferResizeIncrement);

                    sbNewBuffer.append(sbBuffer.subSequence(iStart, sbBuffer.length()));
                    sbBuffer = sbNewBuffer;

                    bCopySuccessful = true;
                }
            }

            // If we could not copy, clear the buffer.
            if (!bCopySuccessful)
            {
                this.sbBuffer.setLength(0);
            }
        }

        // Set the parameters.
        this.iLength = (int) lLength;
        this.lFileOffset = lNewFileOffset;
    }

    /**
     * Returns a new subsequence that can be used to narrow down on this sequence. This method
     * returns a subsequence of the internal string buffer.
     *
     * @param   iStart  Sequence start position relative to this sequence.
     * @param   iEnd    Sequence end position relative to this sequence.
     *
     * @return  The new string buffer.
     */
    public CharSequence subSequence(int iStart, int iEnd)
    {
        if (sbBuffer == null)
        {
            throw new IllegalArgumentException("Uninitialized.");
        }

        if ((iStart < 0) || (iStart >= sbBuffer.length()))
        {
            throw new IllegalArgumentException("Start index " + iStart +
                                               " is out of buffer range.");
        }

        if ((iEnd < 0) || (iEnd > sbBuffer.length()))
        {
            throw new IllegalArgumentException("End index " + iStart +
                                               " is out of buffer range.");
        }

        return sbBuffer.subSequence(iStart, iEnd);
    }

    /**
     * Returns the string representation of the internal buffer.
     *
     * @return  The current buffer in string format.
     */
    @Override
    public String toString()
    {
        return (sbBuffer != null) ? sbBuffer.toString() : "";
    }

    /**
     * Returns the absolute file offset from the sequence index.
     *
     * @param   iBufferPos  The sequence index
     *
     * @return  The absolute file offset.
     */
    public long getFileOffset(int iBufferPos)
    {
        return lFileOffset + iBufferPos;
    }

    /**
     * Returns true if the file start offset is set at the end of the file.
     *
     * @return  Return if the file start offset is set at the end of the file.
     *
     * @throws  IOException  Thrown in file access error.
     */
    public boolean isAtEnd()
                    throws IOException
    {
        if (fcFileChannel == null)
        {
            throw new IOException("Uninitialized.");
        }

        return lFileOffset >= lFileLength;
    }

    /**
     * Resizes the internal buffer so that the requested index fits inside the buffer.
     *
     * @param   iIndex  The index that this buffer contains this index..
     *
     * @throws  IOException  Thrown on file access error.
     */
    protected void resizeBuffer(int iIndex)
                         throws IOException
    {
        // Check argument sanity.
        assert iIndex >= 0;
        assert (sbBuffer == null) || (iIndex < sbBuffer.length());

        int iNewSize = (((iIndex + 1) / iBufferResizeIncrement) + 1) * iBufferResizeIncrement;
        int iReadStart;
        int iReadEnd;

        if (sbBuffer == null)
        {
            iReadStart = 0;
            sbBuffer = new StringBuffer(iNewSize);
        }
        else
        {
            iReadStart = sbBuffer.length();
        }

        iReadEnd = iNewSize - 1;

        assert (iReadStart >= sbBuffer.length()) && (iReadStart < iNewSize);
        assert iReadEnd > iReadStart;

        // Allocate a temporary read buffer.
        ByteBuffer bbBuffer = ByteBuffer.allocate(iReadEnd - iReadStart + 1);
        int iBytesRead;

        // Read the new block.
        iBytesRead = fcFileChannel.read(bbBuffer, lFileOffset + iReadStart);
        bbBuffer.limit(iBytesRead);
        bbBuffer.position(0);

        if (iBytesRead <= 0)
        {
            return;
        }

        CharBuffer cbDecodedBuffer = cdDecoder.decode(bbBuffer);

        sbBuffer.append(cbDecodedBuffer.array());
    }

    /**
     * Initializes the sequence.
     *
     * @param   fcFileChannel  The file to be read from
     * @param   lFileLength    File length.
     * @param   lFileOffset    The offset in the file that is the starting point of this sequence.
     * @param   cCharset       Character set that will be used to convert the bytes to a string or
     *                         null if the default is to be used.
     *
     * @throws  IOException  Throw on file access error.
     */
    private void initialize(FileChannel fcFileChannel, long lFileLength, long lFileOffset,
                            Charset cCharset)
                     throws IOException
    {
        this.fcFileChannel = fcFileChannel;
        this.lFileLength = lFileLength;

        sbBuffer = null;

        if (cCharset == null)
        {
            cCharset = Charset.forName(DEFAULT_CHARSET);

            if (cCharset == null)
            {
                throw new IOException("Character set " + DEFAULT_CHARSET + " could not be loaded.");
            }
        }

        cdDecoder = cCharset.newDecoder();

        if ((cdDecoder.averageCharsPerByte() != 1) || (cdDecoder.maxCharsPerByte() != 1))
        {
            throw new IOException("Character set " + cCharset.name() +
                                  " is not a single byte character set.");
        }

        reset(lFileOffset);
    }
}

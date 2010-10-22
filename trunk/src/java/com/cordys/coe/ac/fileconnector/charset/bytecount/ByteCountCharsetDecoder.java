/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.charset.bytecount;

import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Character set decoder that keeps track of the byte position.
 *
 * @author  mpoyhone
 */
public class ByteCountCharsetDecoder extends CharsetDecoder
{
    /**
     * Default char position to byte position map size.
     */
    private static final int DEFAULT_MAP_SIZE = 2048;
    /**
     * Contains the byte order mark (BOM) header values.
     */
    private static final byte[][] BOM_TABLE =
    {
        { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF }, // UTF-8
        { (byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF }, // UTF-32, big-endian
        { (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00 }, // UTF-32, little-endian
        { (byte) 0xFE, (byte) 0xFF }, // UTF-16, big-endian
        { (byte) 0xFF, (byte) 0xFE }, // UTF-16, little-endian
    };
    /**
     * Contains character set names for the BOM table.
     */
    private static final String[] BOM_CHARSETS =
    { "UTF-8", "UTF-32BE", "UTF-32LE", "UTF-16BE", "UTF-16LE", };
    /**
     * If <code>true</code>, only the end of tag (>) characters are mapped.
     */
    private static final boolean mapOnlyEndOfTag = false;
    /**
     * Debug output flag (to standard out).
     */
    private static final boolean debug = false;
    /**
     * Character set name determined from the BOM header.
     */
    private String bomCharacterSet;
    /**
     * Flag indicating if the BOM header is already read.
     */
    private boolean bomHeaderRead;
    /**
     * Current byte position.
     */
    private long bytePosition;
    /**
     * Current character position.
     */
    private long charPosition;
    /**
     * The actual decoder used to decode the input into characters.
     */
    private CharsetDecoder decoder;
    /**
     * Flag indicating if the map is full (i.e. index has rolled over the end).
     */
    private boolean mapFull;
    /**
     * Current index in the position map.
     */
    private int mapIndex;
    /**
     * Contains the minimum number of characters passed to the decoder at one time. The value is
     * determined from the decoder's average chars per byte value.
     */
    private int minCharWidth = 1;
    /**
     * Contains mapped byte and character positions.
     */
    private long[] positionMap;
    /**
     * Temporary character buffer used during decoding.
     */
    private CharBuffer tempBuffer = CharBuffer.allocate(5);

    /**
     * Constructor.
     *
     * @param  cs  Character set to be used.
     */
    public ByteCountCharsetDecoder(Charset cs)
    {
        super(cs, 1, 1);

        setCharacterSet(cs);
        setBufferSize(DEFAULT_MAP_SIZE);
        tempBuffer.limit(0);
    }

    /**
     * Returns the byte position for the given character position.
     *
     * @param   charPos  Character position to be looked for.
     *
     * @return  Byte position or -1 if the position could not be found.
     */
    public long findMappedBytePosition(long charPos)
    {
        if (mapFull)
        {
            int mapSize = positionMap.length;
            int start = mapIndex;

            for (int i = 0; i < mapSize; i += 2)
            {
                int index = (start + i) % mapSize;

                if (positionMap[index + 1] == charPos)
                {
                    return positionMap[index];
                }
            }
        }
        else
        {
            int end = mapIndex;

            for (int i = 0; i < end; i += 2)
            {
                if (positionMap[i + 1] == charPos)
                {
                    return positionMap[i];
                }
            }
        }

        return -1;
    }

    /**
     * Sets the buffer size.
     *
     * @param  newSize  New size.
     */
    public void setBufferSize(int newSize)
    {
        positionMap = new long[2 * newSize];
        setPosition(0, 0);
    }

    /**
     * Sets current position.
     *
     * @param  bytePos  New byte position.
     * @param  charPos  New character position.
     */
    public void setPosition(long bytePos, long charPos)
    {
        mapIndex = 0;
        mapFull = false;
        charPosition = charPos;
        bytePosition = bytePos;
    }

    /**
     * Sets read BOM flag.
     *
     * @param  b  If <code>true</code> BOM will be read.
     */
    public void setReadBom(boolean b)
    {
        bomHeaderRead = !b;
    }

    /**
     * @see  java.nio.charset.CharsetDecoder#decodeLoop(java.nio.ByteBuffer, java.nio.CharBuffer)
     */
    @Override
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out)
    {
        if (in.remaining() <= 0)
        {
            return CoderResult.UNDERFLOW;
        }

        if (!bomHeaderRead)
        {
            try
            {
                readBomHeader(in);
                bomHeaderRead = true;
            }
            catch (UnsupportedEncodingException e)
            {
                throw new IllegalStateException(e);
            }
        }

        // Flush the temporary buffer contents first.
        while (tempBuffer.hasRemaining())
        {
            if (!out.hasRemaining())
            {
                return CoderResult.OVERFLOW;
            }

            out.put(tempBuffer.get());
        }

        if (debug)
        {
            System.out.println("START: " + in.position() + ", LIMIT: " + in.limit());
        }

        while ((in.remaining() > 0) && (out.remaining() > 0))
        {
            // Try to decode the byte buffer contents into one ore more characters.
            CoderResult res = decodeOneChar(in, out);

            if (res != null)
            {
                // Decoder returned an error.
                if (debug)
                {
                    System.out.println("RES: " + res);
                }
                return res;
            }
        }

        return (in.remaining() <= 0) ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW;
    }

    /**
     * Decodes one character from the input buffer to the output buffer. For Unicode code points, it
     * is possible that this method will output two characters. For each decoded character, the
     * starting byte offset it put into the map.
     *
     * @param   in   Input byte buffer.
     * @param   out  Output character buffer.
     *
     * @return  Decoder result in case of an error, or <code>null</code> if the decoding succeeded.
     */
    private CoderResult decodeOneChar(ByteBuffer in, CharBuffer out)
    {
        int inStartLimit = in.limit();
        int inStartPos = in.position();
        boolean tempBufferEmpty = true;

        // Prepare the buffer for writing.
        tempBuffer.clear();

        try
        {
            for (int i = minCharWidth; i < 10; i += minCharWidth)
            {
                if ((inStartPos + i) > inStartLimit)
                {
                    // Need more data in the input buffer.
                    return CoderResult.UNDERFLOW;
                }

                in.limit(inStartPos + i);

                CoderResult res = decoder.decode(in, tempBuffer, false);
                int count = tempBuffer.position();

                if (count > 0)
                {
                    // We successfully converted one more more characters, so now
                    // map the position of our byte input offset and the current character
                    // position. For a multiple character code point the byte offset will
                    // be same for each character.
                    for (int j = 0; j < count; j++)
                    {
                        char ch = tempBuffer.get(tempBuffer.position() - 1);

                        if (!mapOnlyEndOfTag || (ch == '>'))
                        {
                            if (debug)
                            {
                                System.out.print(ch + " : " + in.position() + " : ");
                            }

                            mapPosition(bytePosition, charPosition + j);
                        }
                    }

                    bytePosition += in.position() - inStartPos;
                    charPosition += count;

                    // Copy the temporary buffer contents to the output buffer.
                    tempBuffer.flip();

                    while (tempBuffer.hasRemaining())
                    {
                        if (!out.hasRemaining())
                        {
                            tempBufferEmpty = false;
                            return CoderResult.OVERFLOW;
                        }

                        out.put(tempBuffer.get());
                    }

                    return null;
                }

                if (!res.isUnderflow())
                {
                    // Either output buffer overflow or an error, so reset the input buffer
                    // and return the result.
                    return res;
                }

                if (in.capacity() <= in.limit())
                {
                    // Not enough bytes in the input buffer, so reset the input buffer
                    // and return the result.
                    return res;
                }
            }
        }
        finally
        {
            // Restore the original limit of the input buffer.
            in.limit(inStartLimit);

            if (tempBufferEmpty)
            {
                // Indicate that the temporary buffer is empty.
                tempBuffer.limit(0);
            }
        }

        throw new IllegalStateException("Decoding loop failed to decode the input buffer.");
    }

    /**
     * Adds a new position mapping to the buffer.
     *
     * @param  bytePos  Byte position to be mapped.
     * @param  charPos  Character position to be mapped.
     */
    private void mapPosition(long bytePos, long charPos)
    {
        if (mapIndex >= positionMap.length)
        {
            mapIndex = 0;
            mapFull = true;
        }

        positionMap[mapIndex++] = bytePos;
        positionMap[mapIndex++] = charPos;

        if (debug)
        {
            System.out.printf("Index=%04d, byte pos=%04d, char pos=%04d\n", (mapIndex - 2) / 2,
                              positionMap[mapIndex - 2], positionMap[mapIndex - 1]);
        }
    }

    /**
     * Tries to read the byte order mark (BOM) header and determine the character set encoding from
     * that. This method will set the character set if a valid BOM was encountered.
     *
     * @param   in  Input.
     *
     * @return  tries to read the byte order mark (BOM) header and determine the character set
     *          encoding from that.
     *
     * @throws  UnsupportedEncodingException
     */
    private boolean readBomHeader(ByteBuffer in)
                           throws UnsupportedEncodingException
    {
        int count = in.remaining();

        if (count < 1)
        {
            return false;
        }

        int inStartPos = in.position();
        int bomLength = 0;

        for (int i = 0; i < BOM_TABLE.length; i++)
        {
            byte[] bomHeader = BOM_TABLE[i];

            if (count < bomHeader.length)
            {
                continue;
            }

            boolean match = true;

            for (int j = 0; j < bomHeader.length; j++)
            {
                if (in.get(inStartPos + j) != bomHeader[j])
                {
                    match = false;
                    break;
                }
            }

            if (match)
            {
                bomCharacterSet = BOM_CHARSETS[i];
                setCharacterSet(bomCharacterSet);
                bomLength = bomHeader.length;
                break;
            }
        }

        if (bomLength > 0)
        {
            in.position(inStartPos + bomLength);
            bytePosition = bomLength;

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Sets the character set.
     *
     * @param   name  Character set name.
     *
     * @throws  UnsupportedEncodingException
     */
    private void setCharacterSet(String name)
                          throws UnsupportedEncodingException
    {
        setCharacterSet(Charset.forName(name));
    }

    /**
     * Sets the characeter set.
     *
     * @param  cs  Character set.
     */
    private void setCharacterSet(Charset cs)
    {
        decoder = cs.newDecoder();

        if (decoder.averageCharsPerByte() < 0.51)
        {
            // This is a two byte character set.
            minCharWidth = 2;
        }
    }
}

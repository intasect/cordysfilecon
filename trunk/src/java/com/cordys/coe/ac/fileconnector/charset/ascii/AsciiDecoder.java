
/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys File Connector. 
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
 package com.cordys.coe.ac.fileconnector.charset.ascii;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * Character set encoder class for Latin-1 flattening to Ascii.
 *
 * @author  mpoyhone
 * @see     AsciiCharset
 */
public class AsciiDecoder extends CharsetDecoder
{
    /**
     * Constructor.
     *
     * @param  acCharset  Character set object to be used.
     */
    protected AsciiDecoder(AsciiCharset acCharset)
    {
        super(acCharset, 1, 1);
    }

    /* (non-Javadoc)
     * @see java.nio.charset.CharsetDecoder#decodeLoop(java.nio.ByteBuffer, java.nio.CharBuffer)
     */
    @Override
    protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out)
    {
        if (AsciiCharset.caConversionTable == null)
        {
            return null;
        }

        while ((in.remaining() > 0) && (out.remaining() > 0))
        {
            int bIn = in.get() & 0xFF;
            char cOut;

            if (bIn >= AsciiCharset.CONVERSION_START_CHARACTER)
            {
                cOut = AsciiCharset.caConversionTable[bIn - AsciiCharset.CONVERSION_START_CHARACTER];
            }
            else
            {
                cOut = (char) bIn;
            }

            out.put(cOut);
        }

        return (in.remaining() == 0) ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW;
    }
}

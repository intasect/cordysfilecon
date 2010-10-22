/**
 * © 2005 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.charset.ascii;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * Character set decoder class for Latin-1 flattening to Ascii.
 *
 * @author  mpoyhone
 * @see     AsciiCharset
 */
public class AsciiEncoder extends CharsetEncoder
{
    /**
     * Constructor.
     *
     * @param  acCharset  Character set object to be used.
     */
    protected AsciiEncoder(AsciiCharset acCharset)
    {
        super(acCharset, 1, 1);
    }

    /* (non-Javadoc)
     * @see java.nio.charset.CharsetEncoder#encodeLoop(java.nio.CharBuffer, java.nio.ByteBuffer)
     */
    @Override
    protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out)
    {
        if (AsciiCharset.caConversionTable == null)
        {
            return null;
        }

        while ((in.remaining() > 0) && (out.remaining() > 0))
        {
            char cIn = (char) (in.get() & 0xFF);
            byte bOut;

            if (cIn >= AsciiCharset.CONVERSION_START_CHARACTER)
            {
                bOut = (byte)
                           AsciiCharset
                           .caConversionTable[cIn - AsciiCharset.CONVERSION_START_CHARACTER];
            }
            else
            {
                bOut = (byte) cIn;
            }

            out.put(bOut);
        }

        return (in.remaining() == 0) ? CoderResult.UNDERFLOW : CoderResult.OVERFLOW;
    }
}

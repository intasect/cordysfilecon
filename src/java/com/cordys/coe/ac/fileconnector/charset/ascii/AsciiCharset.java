/**
 * © 2005 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.charset.ascii;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * Character set definition class for encoder and decoder classes. This character set encode/decode
 * implementations flatten normal Latin-1 characters to Ascii characters. They also try to preserve
 * character outlook by e.g. replacing 'É' with 'E'.
 *
 * @author  mpoyhone
 */
public class AsciiCharset extends Charset
{
    /**
     * Name of this character set.
     */
    public static String CHARSETNAME = "X-CORDYS-LATIN1-ASCII";
    /**
     * Indentifies the starting character in the <code>caConversion</code> table.
     */
    static char CONVERSION_START_CHARACTER = ' ';
    /**
     * Character conversion table. Contains mappings from Latin-1 character code (minus start
     * character) to an ascii character.
     */
    static char[] caConversionTable;

    /**
     * Constructor.
     *
     * @param  canonicalName  Characer set name.
     * @param  aliases        An array of character set aliases or null if none is specified.
     */
    public AsciiCharset(String canonicalName, String[] aliases)
    {
        super(canonicalName, aliases);
    }

    /* (non-Javadoc)
     * @see java.nio.charset.Charset#contains(java.nio.charset.Charset)
     */
    @Override
    public boolean contains(Charset cs)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see java.nio.charset.Charset#newDecoder()
     */
    @Override
    public CharsetDecoder newDecoder()
    {
        return new AsciiDecoder(this);
    }

    /* (non-Javadoc)
     * @see java.nio.charset.Charset#newEncoder()
     */
    @Override
    public CharsetEncoder newEncoder()
    {
        return new AsciiEncoder(this);
    }
}

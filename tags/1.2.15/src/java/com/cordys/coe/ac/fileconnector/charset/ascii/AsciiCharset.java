
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

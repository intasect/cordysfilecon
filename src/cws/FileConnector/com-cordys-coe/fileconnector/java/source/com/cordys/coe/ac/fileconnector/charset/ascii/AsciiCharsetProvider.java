
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

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Character set provider class for custon ascii character set. This class loads conversion
 * properties from file CharacterMapping.properties that must reside in the same package as this
 * class.
 *
 * @author  mpoyhone
 */
public class AsciiCharsetProvider extends CharsetProvider
{
    /**
     * Mapping property file name. This is loaded from the same package as this class.
     */
    public static final String PROPERTY_FILE_NAME = "CharacterMapping.properties";
    /**
     * Character set object containing the conversion information.
     */
    private AsciiCharset mcCharset;
    /**
     * Name of supported character sets. Contains only the configured name.
     */
    private Set<String> sCharsetNames = new HashSet<String>();
    /**
     * Supported character set objects. Contains only the configured character set.
     */
    private Set<Charset> sCharsets = new HashSet<Charset>();

    /**
     * Creates a new AsciiCharsetProvider object.
     */
    public AsciiCharsetProvider()
    {
        this(PROPERTY_FILE_NAME);
    }

    /**
     * Creates a new AsciiCharsetProvider object.
     *
     * @param  sFileName  Property file name.
     */
    public AsciiCharsetProvider(String sFileName)
    {
        try
        {
            loadProperties(sFileName);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Unable to load character set properties : " + e);
        }

        mcCharset = new AsciiCharset(AsciiCharset.CHARSETNAME, null);

        sCharsetNames.add(AsciiCharset.CHARSETNAME);
        sCharsets.add(mcCharset);
    }

    /* (non-Javadoc)
     * @see java.nio.charset.spi.CharsetProvider#charsetForName(java.lang.String)
     */
    @Override
    public Charset charsetForName(String charsetName)
    {
        if (!sCharsetNames.contains(charsetName))
        {
            return null;
        }

        return mcCharset;
    }

    /**
     * @see java.nio.charset.spi.CharsetProvider#charsets()
     */
    @Override
    public Iterator<Charset> charsets()
    {
        return sCharsets.iterator();
    }

    /**
     * Loads conversion parameters from the property file.
     *
     * @param   sFileName  Property file name.
     *
     * @throws  IOException  Thrown if loading failed.
     */
    private void loadProperties(String sFileName)
                         throws IOException
    {
        InputStream isInputStream = null;

        try
        {
            isInputStream = getClass().getResourceAsStream(sFileName);

            Properties pProps = new Properties();
            char[] caMappings = new char[256];

            for (int i = 0; i < caMappings.length; i++)
            {
                caMappings[i] = (char) i;
            }

            pProps.load(isInputStream);

            String sCharsetName = pProps.getProperty("character.set.name", "");

            if (sCharsetName.length() == 0)
            {
                throw new IOException("Character set name not set in the property file " +
                                      sFileName);
            }

            for (Iterator<?> iIter = pProps.keySet().iterator(); iIter.hasNext();)
            {
                String sKeyName = (String) iIter.next();

                if (!sKeyName.startsWith("map.") || (sKeyName.length() <= 4))
                {
                    continue;
                }

                int iCode;

                try
                {
                    iCode = Integer.parseInt(sKeyName.substring(4));
                }
                catch (NumberFormatException e)
                {
                    throw new IOException("Invalid key code : " + sKeyName.substring(4));
                }

                if ((iCode < 0) || (iCode > 255))
                {
                    throw new IOException("Invalid key code : " + iCode);
                }

                String sValue = pProps.getProperty(sKeyName, "");

                if (sValue.length() == 0)
                {
                    throw new IOException("Missing mapping value for code " + iCode);
                }

                caMappings[iCode] = sValue.charAt(0);
            }

            AsciiCharset.CHARSETNAME = sCharsetName;
            AsciiCharset.caConversionTable = caMappings;
            AsciiCharset.CONVERSION_START_CHARACTER = (char) 0;
        }
        finally
        {
            try
            {
                isInputStream.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

/*    public static void main(String[] args) {
 *     new AsciiCharsetProvider();      for (int i = 0; i <  AsciiCharset.caConversionTable .length;
 * i++) {        char ch =  AsciiCharset.caConversionTable[i];         System.out.println((char) i +
 * " = " + ch + (i  != (int) ch ? "   # changed" : ""));    }        try {     String sInput =
 * "Testin123\näbbä\tBÄbbä ölÖmölÖ. Één twéé. Çÿ";                String sOutput = new
 * String(sInput.getBytes("ISO-8859-1"), AsciiCharset.CHARSETNAME); System.out.println(sOutput);
 *            String sOutput2 = new String(sInput.getBytes(AsciiCharset.CHARSETNAME));
 *  System.out.println(sOutput2);
 *   } catch (UnsupportedEncodingException e) {     }              }*/
}

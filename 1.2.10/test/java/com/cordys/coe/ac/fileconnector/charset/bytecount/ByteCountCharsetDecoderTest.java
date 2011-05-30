/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.charset.bytecount;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;

import junit.framework.TestCase;

/**
 * Test cases for the ByteCountCharsetDecoder class
 *
 * @author mpoyhone
 */
public class ByteCountCharsetDecoderTest extends TestCase
{

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testDecoder_Simple() throws Exception
    {
        execute("abcdef", null);
    }
    
    public void testDecoder_Simple_ISO_8859_1() throws Exception
    {
        execute("abcdefÄÖäöÅåabcdef", "ISO-8859-1");
    }
    
    public void testDecoder_Simple_UTF8() throws Exception
    {
        execute("abcdefÄÖäöÅåabcdef", "UTF-8");
    }
    
    public void testDecoder_Simple_UTF16() throws Exception
    {
        execute("abcdefÄÖäöÅåabcdef", "UTF-16");
    }
    
    public void testDecoder_Simple_UTF16LE() throws Exception
    {
        execute("abcdefÄÖäöÅåabcdef", "UTF-16LE");
    }

    public void testDecoder_Simple_UTF16BE() throws Exception
    {
        execute("abcdefÄÖäöÅåabcdef", "UTF-16BE");
    }
    
    public void testDecoder_WideChar_UTF8() throws Exception
    {
        execute("abcd\u5B66\uD800\uDF30_", "UTF-8");
    }
    
    public void testDecoder_WideChar_UTF16() throws Exception
    {
        execute("abcd\u5B66\uD800\uDF30_", "UTF-16");
    }
    
    public void testDecoder_WideChar_UTF16LE() throws Exception
    {
        execute("abcd\u5B66\uD800\uDF30_", "UTF-16LE");
    }
    
    public void testDecoder_WideChar_UTF16BE() throws Exception
    {
        execute("abcd\u5B66\uD800\uDF30_", "UTF-16BE");
    }
    
    public void testDecoder_Buffer() throws Exception
    {
        execute("12345", 0, null, 5);
        execute("123456", 1, null, 5);
        execute("1234567890", 5, null, 5);
        execute("12345678901", 6, null, 5);
    }
    
    public void testDecoder_Buffer_ISO_8859_1() throws Exception
    {
        String charset = "ISO-8859-1";
        execute("ÄÖ_öÅ", 0, charset, 5);
        execute("ÄÖ_öÅå", 1, charset, 5);
        execute("ÄÖ_öÅåÄÖäö", 5, charset, 5);
        execute("ÄÖ_öÅåÄÖäöÅ", 6, charset, 5);
    }   
    
    public void testDecoder_Buffer_UTF8() throws Exception
    {
        String charset = "UTF-8";
        execute("ÄÖ_öÅ", 0, charset, 5);
        execute("ÄÖ_öÅå", 1, charset, 5);
        execute("ÄÖ_öÅåÄÖäö", 5, charset, 5);
        execute("ÄÖ_öÅåÄÖäöÅ", 6, charset, 5);
    }    
    
    public void testDecoder_Buffer_UTF16() throws Exception
    {
        String charset = "UTF-16";
        execute("ÄÖ_öÅ", 0, charset, 5);
        execute("ÄÖ_öÅå", 1, charset, 5);
        execute("ÄÖ_öÅåÄÖäö", 5, charset, 5);
        execute("ÄÖ_öÅåÄÖäöÅ", 6, charset, 5);
    }    
    
    public void testDecoder_Buffer_UTF16LE() throws Exception
    {
        String charset = "UTF-16LE";
        execute("ÄÖ_öÅ", 0, charset, 5);
        execute("ÄÖ_öÅå", 1, charset, 5);
        execute("ÄÖ_öÅåÄÖäö", 5, charset, 5);
        execute("ÄÖ_öÅåÄÖäöÅ", 6, charset, 5);
    }    
    
    public void testDecoder_Buffer_UTF16BE() throws Exception
    {
        String charset = "UTF-16BE";
        execute("ÄÖ_öÅ", 0, charset, 5);
        execute("ÄÖ_öÅå", 1, charset, 5);
        execute("ÄÖ_öÅåÄÖäö", 5, charset, 5);
        execute("ÄÖ_öÅåÄÖäöÅ", 6, charset, 5);
    }    
    
    public void testDecoder_BufferUnderflow_UTF8() throws Exception
    {
        String charset = "UTF-8";
        String teststr = "Ä";
        ByteBuffer in = ByteBuffer.allocate(10);
        CharBuffer out = CharBuffer.allocate(10);
        ByteCountCharsetDecoder decoder = new ByteCountCharsetDecoder(Charset.forName(charset));
        CoderResult res;
        
        in.put(teststr.getBytes(charset));
        in.position(0);

        // The decoder must return an underflow when there is not enough bytes to decoder that
        // data
        in.limit(1);
        res = decoder.decode(in, out, false);
        assertTrue("Invalid decoding result: " + res, res.isUnderflow());
        assertEquals(0, in.position());
        assertEquals(1, in.limit());

        // Now there are enough bytes to decode the buffer. This must succeed.
        in.limit(2);
        res = decoder.decode(in, out, false);
        assertTrue("Invalid decoding result: " + res, res.isUnderflow() || res.isUnderflow() || res.isUnmappable() || res.isError());
        assertEquals(2, in.position());
        assertEquals(2, in.limit());
        out.flip();
        assertEquals(teststr, out.toString());
    }
    
    public void testDecoder_BufferOverflow_UTF8() throws Exception
    {
        String charset = "UTF-8";
        String teststr = "ÄÖ";
        ByteBuffer in = ByteBuffer.allocate(10);
        CharBuffer out = CharBuffer.allocate(10);
        ByteCountCharsetDecoder decoder = new ByteCountCharsetDecoder(Charset.forName(charset));
        CoderResult res;
        
        in.put(teststr.getBytes(charset));
        in.flip();

        // The decoder must return an overflow when there is not enough space in the output buffer.
        // This should read the first character.
        out.limit(1);
        res = decoder.decode(in, out, false);
        assertTrue("Invalid decoding result: " + res, res.isOverflow());
        assertEquals(1, out.position());
        assertEquals(1, out.limit());
        assertEquals(teststr.charAt(0), out.get(0));

        // Now there is enough space in the out buffer. This must succeed.
        out.limit(2);
        res = decoder.decode(in, out, false);
        assertTrue("Invalid decoding result: " + res, res.isUnderflow() || res.isUnderflow() || res.isUnmappable() || res.isError());
        assertEquals(2, out.position());
        assertEquals(2, out.limit());
        out.flip();
        assertEquals(teststr, out.toString());
    }
    
    private void execute(String str, String charset) throws Exception
    {
        execute(str, 0, charset, 0);
    }
    
    private void execute(String str, int strOffset, String charset, int bufferSize) throws Exception
    {    
        byte[] strBytes;
        Reader reader;
        ByteCountCharsetDecoder decoder;
        
        if (charset != null) {
            strBytes = str.getBytes(charset);
            decoder = new ByteCountCharsetDecoder(Charset.forName(charset));
            reader = new InputStreamReader(new ByteArrayInputStream(strBytes), decoder);
        } else {
            strBytes = str.getBytes();
            decoder = new ByteCountCharsetDecoder(Charset.defaultCharset());
            reader = new InputStreamReader(new ByteArrayInputStream(strBytes), decoder);
        }
        
        execute(str, strOffset, strBytes, decoder, reader, bufferSize, charset);
    }
    
    private void execute(String str, int strOffset, byte[] strBytes, ByteCountCharsetDecoder decoder, Reader reader, int bufferSize, String charset) throws Exception
    {
        if (bufferSize > 0) {
            decoder.setBufferSize(bufferSize);
        }
        
        for (int i = 0; i < str.length(); i++) {
            int ch = reader.read();
            
            assertEquals(str.charAt(i), ch);
        }
        
        for (int i = strOffset; i < str.length(); ) {
            long pos = decoder.findMappedBytePosition(i);
            int length = 0;
            
            assertTrue("Character position not found: " + i, pos >= 0);

            for (int j = 1; length == 0; j++) {
                long nextpos = decoder.findMappedBytePosition(i + j);
                
                if (nextpos < 0) {
                    length = (int) (strBytes.length - pos);
                } else {
                    length = (int) (nextpos - pos);
                }
            }
            
            int codePoints = i < str.length() - 1 ? str.codePointCount(i, i + 2) : 2;
            int chars = codePoints == 2 ? 1 : 2;
            
            if (charset != null) {
                assertEquals(str.substring(i, i + chars), new String(strBytes, (int) pos, length, charset));
            } else {
                assertEquals(str.substring(i, i + chars), new String(strBytes, (int) pos, length));
            }
            
            i += chars;
        }
    }
}

/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

import junit.framework.TestCase;

/**
 * Test cases for the SequenceReader class
 *
 * @author mpoyhone
 */
public class SequenceReaderTest extends TestCase
{
    private static final String[] TEST_STRINGS = {
        "test1",
        "tESt2",
        "tesT\u00003",
        "tEst4\u0000",
        "",
        "\u0000TEST6"
    };
    
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

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read()}.
     */
    public void testRead() throws IOException
    {
        Reader r = openReaders(TEST_STRINGS);
        StringBuilder sb = new StringBuilder(100);
        int ch;
        
        while ((ch = r.read()) >= 0) {
            sb.append((char) ch);
        }
        
        assertEquals(getString(TEST_STRINGS), sb.toString());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read()}.
     */
    public void testRead_Empty() throws IOException
    {
        Reader r = openReaders(new String[] { "" });
        int ch;
        
        ch = r.read();
        assertEquals(-1, ch);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read()}.
     */
    public void testRead_FirstEmpty() throws IOException
    {
        Reader r = openReaders(new String[] { "", "a" });
        int ch;
        
        ch = r.read();
        assertEquals('a', ch);
        ch = r.read();
        assertEquals(-1, ch);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read()}.
     */
    public void testRead_SecondEmpty() throws IOException
    {
        Reader r = openReaders(new String[] { "a", "" });
        int ch;
        
        ch = r.read();
        assertEquals('a', ch);
        ch = r.read();
        assertEquals(-1, ch);
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read(char[])}.
     */
    public void testReadCharArray_size_1() throws IOException
    {
        Reader r = openReaders(TEST_STRINGS);
        StringBuilder sb = new StringBuilder(100);
        int count;
        char[] buf = new char[1]; 
        
        while ((count = r.read(buf)) > 0) {
            sb.append(buf, 0, count);
        }
        
        assertEquals(getString(TEST_STRINGS), sb.toString());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read(char[])}.
     */
    public void testReadCharArray_size_2() throws IOException
    {
        Reader r = openReaders(TEST_STRINGS);
        StringBuilder sb = new StringBuilder(100);
        int count;
        char[] buf = new char[2]; 
        
        while ((count = r.read(buf)) > 0) {
            sb.append(buf, 0, count);
        }
        
        assertEquals(getString(TEST_STRINGS), sb.toString());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read(char[])}.
     */
    public void testReadCharArray_size_5() throws IOException
    {
        Reader r = openReaders(TEST_STRINGS);
        StringBuilder sb = new StringBuilder(100);
        int count;
        char[] buf = new char[5]; 
        
        while ((count = r.read(buf)) > 0) {
            sb.append(buf, 0, count);
        }
        
        assertEquals(getString(TEST_STRINGS), sb.toString());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read(char[])}.
     */
    public void testReadCharArray_size_10() throws IOException
    {
        Reader r = openReaders(TEST_STRINGS);
        StringBuilder sb = new StringBuilder(100);
        int count;
        char[] buf = new char[10]; 
        
        while ((count = r.read(buf)) > 0) {
            sb.append(buf, 0, count);
        }
        
        assertEquals(getString(TEST_STRINGS), sb.toString());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read(char[], int, int)}.
     */
    public void testReadCharArrayIntInt() throws IOException
    {
        Reader r = openReaders(TEST_STRINGS);
        StringBuilder sb = new StringBuilder(100);
        int count;
        int pos = 0;
        char[] buf = new char[50]; 
        
        count = r.read(buf, pos, 1);
        assertEquals(count, 1);
        pos += count;
        count = r.read(buf, pos, 2);
        assertEquals(count, 2);
        pos += count;
        count = r.read(buf, pos, 3);
        assertEquals(count, 3);
        pos += count;
        
        while ((count = r.read(buf, pos, buf.length - pos)) > 0) {
            sb.append(buf, 0, count + pos);
            pos = 0;
        }
        
        assertEquals(getString(TEST_STRINGS), sb.toString());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#skip(long)}.
     */
    public void testSkip() throws IOException
    {
        Reader r = openReaders(new String[] { "abc", "def", "ghi" });
        int ch;
        long count;
        
        ch = r.read();
        assertEquals('a', ch);
        count = r.skip(4);
        assertEquals(count, 4);
        ch = r.read();
        assertEquals('f', ch);
        count = r.skip(2);
        assertEquals(count, 2); 
        ch = r.read();
        assertEquals('i', ch);
        ch = r.read();
        assertEquals(-1, ch);
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.SequenceReader#read(java.nio.CharBuffer)}.
     */
    public void testReadCharBuffer() throws IOException
    {
       CharBuffer buf1 = CharBuffer.allocate(1);
       CharBuffer buf2 = CharBuffer.allocate(2);
       CharBuffer buf3 = CharBuffer.allocate(3);
       CharBuffer buf50 = CharBuffer.allocate(2);
       Reader r = openReaders(TEST_STRINGS);
       StringBuilder sb = new StringBuilder(100);
       int count;
       
       count = r.read(buf1);
       assertEquals(count, 1);
       buf1.flip();
       sb.append(buf1);
       count = r.read(buf2);
       assertEquals(count, 2);
       buf2.flip();
       sb.append(buf2);
       count = r.read(buf3);
       assertEquals(count, 3);
       buf3.flip();
       sb.append(buf3);
       
       while ((count = r.read(buf50)) > 0) {
           buf50.flip();
           sb.append(buf50);
           buf50.clear();
       }
       
       assertEquals(getString(TEST_STRINGS), sb.toString());
       
    }

    private SequenceReader openReaders(String[] src)
    {
        Reader[] arr = new Reader[src.length];
        
        for (int i = 0; i < src.length; i++)
        {
            arr[i] = new StringReader(src[i]);
        }
        
        return new SequenceReader(arr);
    }
    
    private String getString(String[] src) {
        StringBuilder sb = new StringBuilder(100);
        
        for (int i = 0; i < src.length; i++)
        {
            sb.append(src[i]);
        }
        
        return sb.toString();
    }
}

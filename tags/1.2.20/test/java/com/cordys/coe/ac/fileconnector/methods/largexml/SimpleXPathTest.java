/**
 * (c) 2007 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test class for the SimpleXPath class.
 *
 * @author mpoyhone
 */
public class SimpleXPathTest extends TestCase
{
    /**
     * Test method for {@link org.mikko.woodstox.file.SimpleXPath#match(javax.xml.stream.XMLStreamReader)}.
     */
    public void testParts()
    {
        SimpleXPath xp;
        String[] test;
        String[] control;
        
        xp = new SimpleXPath("/a/b/c");
        test = xp.getPartsAsString();
        control = new String[] { "a", "b", "c" };
        assertEquals(Arrays.toString(control), Arrays.toString(test));
        
        xp = new SimpleXPath("a");
        test = xp.getPartsAsString();
        control = new String[] { "a" };
        assertEquals(Arrays.toString(control), Arrays.toString(test));
        
        xp = new SimpleXPath("/a");
        test = xp.getPartsAsString();
        control = new String[] { "a" };
        assertEquals(Arrays.toString(control), Arrays.toString(test));
        
        xp = new SimpleXPath("/a//b");
        test = xp.getPartsAsString();
        control = new String[] { "a", "b" };
        assertEquals(Arrays.toString(control), Arrays.toString(test));
    }
    
    /**
     * Test method for {@link org.mikko.woodstox.file.SimpleXPath#moveToNext()}.
     */
    public void testMoveToNext()
    {
        SimpleXPath xp;
        
        xp = new SimpleXPath("/a/b");
        assertEquals("a", xp.getCurrentPart().getName());
        xp.moveToNext();
        assertTrue(! xp.isAtEnd());
        assertEquals("b", xp.getCurrentPart().getName());
        xp.moveToNext();
        assertTrue(xp.isAtEnd());
        
        try {
            xp.moveToNext();
            fail("moveToNext didn't throw an exception.");
        }
        catch(IllegalStateException e) {
        }
    }

    /**
     * Test method for {@link org.mikko.woodstox.file.SimpleXPath#moveToPrevious()}.
     */
    public void testMoveToPrevious()
    {
        SimpleXPath xp;
        
        xp = new SimpleXPath("/a/b");
        assertEquals("a", xp.getCurrentPart().getName());
        xp.moveToNext();
        assertEquals("b", xp.getCurrentPart().getName());
        xp.moveToPrevious();
        assertEquals("a", xp.getCurrentPart().getName());
        
        try {
            xp.moveToPrevious();
            fail("moveToPrevious didn't throw an exception.");
        }
        catch(IllegalStateException e) {
        }
    }

}

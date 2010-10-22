/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.io.File;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;

/**
 * Test class for the LargeXmlFileReader class.
 *
 * @author mpoyhone
 */
public class LargeXmlFileReaderTest extends FileConnectorTestCase
{

    /**
     * @see junit.framework.TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#findNext(com.cordys.coe.ac.fileconnector.methods.largexml.SimpleXPath)}.
     */
    public void testFindNext_Simple() throws Exception
    {
        String controlXml =
            "  <item1 />";
        String xml = 
            "<root>" + 
            controlXml +
            "</root>";
        
        File file = createTextFile("input.xml", xml, "UTF-8");
        LargeXmlFileReader reader = new LargeXmlFileReader(file, null, true);

        try {
            int nomTree = reader.findNext(dDoc, new SimpleXPath("/root/item1"), true);
            
            addNomGarbage(nomTree);
            assertNodesEqual(parse(controlXml), nomTree);
        }
        finally {
            reader.close();
        }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#findNext(com.cordys.coe.ac.fileconnector.methods.largexml.SimpleXPath)}.
     */
    public void testFindNext_SimpleWithExtraText() throws Exception
    {
        String controlXml =
            "  <item1 />";
        String xml = 
            "<root>" + 
            "dummy text\r\n" +
            controlXml +
            "another dummy text\r\n" +
            "</root>";
        
        File file = createTextFile("input.xml", xml, "UTF-8");
        LargeXmlFileReader reader = new LargeXmlFileReader(file, null, true);

        try {
            int nomTree = reader.findNext(dDoc, new SimpleXPath("/root/item1"), true);
            
            addNomGarbage(nomTree);
            assertNodesEqual(parse(controlXml), nomTree);
        }
        finally {
            reader.close();
        }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testFindNext_Multiple() throws Exception
    {
        String[] controlXml = {
            "  <item1 />",
            "  <item1><item2 a=\"123\" /></item1>",
            "  <item1><item2><item3/></item2></item1>",
            "  <item1 />",
        };
        String xml = 
            "<root>" + 
            controlXml[0] + controlXml[1] + controlXml[2] + controlXml[3] +
            "</root>";

        File file = createTextFile("input.xml", xml, "UTF-8");
        LargeXmlFileReader reader = new LargeXmlFileReader(file, null, true);
        SimpleXPath xpath = new SimpleXPath("/root/item1");
        
        try {
            for (String cx : controlXml)
            {
                int nomTree = reader.findNext(dDoc, xpath, true);

                addNomGarbage(nomTree);
                assertNodesEqual(parse(cx), nomTree);
            }
        }
        finally {
            reader.close();
        }
    }    

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testOpenFile_Simple() throws Exception
    {
        String controlXml =
            "  <item1 />";
        String xml = 
            "<root>" + 
            controlXml +
            "</root>";
        
        File file = createTextFile("input.xml", xml, "UTF-8");
        LargeXmlFileReader reader = null;

        try {
            reader = new LargeXmlFileReader(file, true);
            
            int nomTree = reader.findNext(dDoc, new SimpleXPath("/root/item1"), true);
            
            addNomGarbage(nomTree);
            assertNodesEqual(parse(controlXml), nomTree);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testOpenFile_Multiple() throws Exception
    {
        String[] controlXml = {
            "  <item1 />",
            "  <item1><item2 a=\"123\" /></item1>",
            "  <item1><item2><item3/></item2></item1>",
            "  <item1 />",
        };
        String xml = 
            "<root>" + 
            controlXml[0] + controlXml[1] + controlXml[2] + controlXml[3] +
            "</root>";

        File file = createTextFile("input.xml", xml, "UTF-8");
        LargeXmlFileReader reader = null;
        XmlTraverseContext ctx = null;
        int iter = 0;
        try {
            for (String cx : controlXml)
            {
                SimpleXPath xpath = new SimpleXPath("/root/item1");
                
                //System.out.println("Open #" + (iter + 1));
                
                reader = new LargeXmlFileReader(file, ctx, true);
                
                int nomTree = reader.findNext(dDoc, xpath, true);
               
                addNomGarbage(nomTree);
                assertNodesEqual(parse(cx), nomTree);
                
                ctx = reader.getCurrentContext();
                reader.close();
                reader = null;
                iter++;
            }
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
    }    
}

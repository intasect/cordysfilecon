/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.xml.stream.XMLStreamConstants;

import junit.framework.TestCase;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

/**
 * Test cases for the XmlTraverseContext class.
 *
 * @author mpoyhone
 */
public class XmlTraverseContextTest extends TestCase
{
    private XMLStreamReader2 reader;
    
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
        
        if (reader != null) {
            try {
                reader.close();
            }
            catch (Exception ignored) {
            }
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XmlTraverseContext#createFromReader(javax.xml.stream.XMLStreamReader)}.
     */
    public void testCreateFromReader() throws Exception
    {
        String encoding = "UTF-8";
        String xml = 
            "<root>" + 
            "  <a1:item1 xmlns:a1=\"xx\">" + 
            "    <item2 xmlns=\"yy\">" + 
            "      <item3>Data1</item3>" + 
            "      <item3>Data2</item3>" + 
            "    </item2>" + 
            "  </a1:item1>" +
            "</root>";
        byte[] xmlBytes = xml.getBytes(encoding);
        
        XmlTraverseContext ctx = execute(xmlBytes, 0, "item3");
        String serStr = XmlTraverseContext.serializeToBase64String(ctx);
        
        XmlTraverseContext ctx2 = XmlTraverseContext.serializeFromBase64String(serStr);
        
        assertEquals("<root><a1:item1 xmlns:a1=\"xx\"><item2 xmlns=\"yy\">", ctx2.getHeaderXml());
    }
    
    private XmlTraverseContext execute(byte[] xmlBytes, int offset, String destElemName) throws Exception
    {
        Reader offsetReader = 
            new InputStreamReader(new ByteArrayInputStream(xmlBytes, offset, xmlBytes.length - offset));
        
        return execute(offsetReader, destElemName);
    }
    
    private XmlTraverseContext execute(Reader offsetReader, String destElemName) throws Exception
    {
        XMLInputFactory2 xif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        
        xif.configureForLowMemUsage();
        reader = (XMLStreamReader2) xif.createXMLStreamReader(offsetReader);        

        XmlTraverseContext res = new XmlTraverseContext();
        
        while (true) {
            int event = reader.next();
            
            switch (event) {
            case XMLStreamConstants.START_ELEMENT:
            {
                res.pushLevelFromReader(reader);
                
                if (destElemName.equals(reader.getLocalName())) {
                    return res;
                }
            } break;
                
            case XMLStreamConstants.END_ELEMENT:
            {
                res.popLevel();
            } break;
                
            case XMLStreamConstants.END_DOCUMENT:
                fail("Unexpected end of document.");
                break;
            }
        }            
    }
}

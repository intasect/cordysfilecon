/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.io.ByteArrayInputStream;

import javax.xml.stream.XMLStreamReader;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.cordys.coe.util.test.junit.NomTestCase;

/**
 * Test class for XMLStreamReader NOM writer. 
 *
 * @author mpoyhone
 */
public class XMLStreamNomWriterTest extends NomTestCase
{
    private XMLStreamReader reader;
    
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
        
        if (reader != null) {
            try {
                reader.close();
            }
            catch (Exception ignored) {
            }
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_OneLevel() throws Exception
    {
        String controlXml =
            "  <item1 display-length=\"749\" level=\"05\" />"; 
        String xml = 
            "<root attr=\"attr1\">" + 
            controlXml +
            "</root>";
        
        execute(xml, controlXml);
    }
    
        /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_OneLevelTwice() throws Exception
    {
        String controlXml =
            "  <item1 display-length=\"749\" level=\"05\" />"; 
        String xml = 
            "<root attr=\"attr1\">" + 
            controlXml +
            controlXml +
            "</root>";
        
        execute(xml, controlXml);
        
        reader.nextTag();
        assertEquals("item1", reader.getLocalName());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_TwoLevels() throws Exception
    {
        String controlXml =
            "  <item1 display-length=\"749\" level=\"05\">" + 
            "    <item2 display-length=\"12\" level=\"07\" />" + 
            "  </item1>";
        String xml = 
            "<root attr=\"attr1\">" + 
            controlXml +
            "</root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_ElementText() throws Exception
    {
        String controlXml =
            "  <item1 display-length=\"749\" level=\"05\">" + 
            "    <item2>" + 
            "      <item3>Data1</item3>" + 
            "      <item3>Data2</item3>" + 
            "    </item2>" + 
            "  </item1>";
        String xml = 
            "<root attr=\"attr1\">" + 
            controlXml +
            "</root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_ElementCData() throws Exception
    {
        String controlXml =
            "  <item1 display-length=\"749\" level=\"05\">" + 
            "    <item2>" + 
            "      <item3><![CDATA[Data1]]></item3>" + 
            "      <item3><![CDATA[Data2]]></item3>" + 
            "    </item2>" + 
            "  </item1>";
        String xml = 
            "<root attr=\"attr1\">" + 
            controlXml +
            "</root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_TopLevelNamespace() throws Exception
    {
        String controlXml =
            "  <item1 xmlns=\"my-namespace-uri\">" + 
            "    <item2 />" + 
            "  </item1>";
        String xml = 
            "<root xmlns=\"my-namespace-uri\">" + 
            "  <item1>" + 
            "    <item2 />" + 
            "  </item1>" +
            "</root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_TopLevelNamespaceWithPrefix() throws Exception
    {
        String controlXml =
            "  <ns:item1 xmlns:ns=\"my-namespace-uri\">" + 
            "    <ns:item2 />" + 
            "  </ns:item1>";
        String xml = 
            "<ns:root xmlns:ns=\"my-namespace-uri\">" + 
            "  <ns:item1>" + 
            "    <ns:item2 />" + 
            "  </ns:item1>" +
            "</ns:root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_TopLevelNamespaceChildPrefix() throws Exception
    {
        String controlXml =
            "  <ns:item1 xmlns:ns=\"my-namespace-uri2\">" + 
            "    <ns:item2/>" + 
            "  </ns:item1>";
        String xml = 
            "<ns:root xmlns:ns=\"my-namespace-uri\">" + 
            "  <ns:item1 xmlns:ns=\"my-namespace-uri2\">" + 
            "    <ns:item2 />" + 
            "  </ns:item1>" +
            "</ns:root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_TopLevelNamespaceChildPrefix2() throws Exception
    {
        String controlXml =
            "  <ns:item1 xmlns:ns=\"my-namespace-uri\">" + 
            "    <ns:item2>" + 
            "       <ns:item3 xmlns:ns=\"my-namespace-uri3\" />" + 
            "    </ns:item2>" + 
            "  </ns:item1>";
        String xml = 
            "<ns:root xmlns:ns=\"my-namespace-uri\">" + 
            controlXml +
            "</ns:root>";
        
        execute(xml, controlXml);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_TopLevelChildNamespace() throws Exception
    {
        String controlXml =
            "<item1 xmlns:ns=\"my-namespace-uri\">" + 
            "  <item2>" + 
            "     <ns:item3/>" + 
            "  </item2>" + 
            "</item1>";
        String xml = 
            "<root xmlns:ns=\"my-namespace-uri\">" + 
            "  <item1>" + 
            "    <item2>" + 
            "       <ns:item3/>" + 
            "    </item2>" + 
            "  </item1>" + 
            "</root>";
        
        XmlTraverseContext ctx = new XmlTraverseContext();
        
        openReader(xml);
        reader.nextTag();
        ctx.pushLevelFromReader(reader);
        
        execute(xml, controlXml, ctx);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.XMLStreamNomWriter#createNomTree()}.
     */
    public void testCreateNomTree_AttribNamespace() throws Exception
    {
        String controlXml =
            "  <item1 xmlns:attr=\"attr-uri\" attr:a=\"attr1\">" + 
            "    <item2 a=\"attr2\">" + 
            "       <item3 attr:a=\"attr3\" />" + 
            "    </item2>" + 
            "  </item1>";
        String xml = 
            "<root>" + 
            controlXml +
            "</root>";
        
        execute(xml, controlXml);
    }    
    
    
    private void execute(String xml, String controlXml) throws Exception
    {
        execute(xml, controlXml, null);
    }
    
    private void execute(String xml, String controlXml, XmlTraverseContext ctx) throws Exception
    {    
        openReader(xml);
        reader.nextTag();
        assertEquals("root", reader.getLocalName());
        reader.nextTag();
        assertEquals("item1", reader.getLocalName());
        
        XMLStreamNomWriter writer = new XMLStreamNomWriter(reader, dDoc, ctx);
        
        int nomTree = writer.createNomTree();
        
        addNomGarbage(nomTree);
        assertNodesEqual(parse(controlXml), nomTree, true);
    }

    private void openReader(String xml) throws Exception {
        XMLInputFactory2 xif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        
        xif.configureForLowMemUsage();
        reader = (XMLStreamReader2) xif.createXMLStreamReader(new ByteArrayInputStream(xml.getBytes("UTF-8")));        
    }
}

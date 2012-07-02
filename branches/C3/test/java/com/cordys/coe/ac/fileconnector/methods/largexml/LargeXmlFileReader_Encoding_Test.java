/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.util.FileUtils;

/**
 * Test class for the LargeXmlFileReader class.
 *
 * @author mpoyhone
 */
public class LargeXmlFileReader_Encoding_Test extends FileConnectorTestCase
{
    private static final String[] controlXmlElems = {
        "  <item1 />",
        "  <item1><item2 a=\"ÄÖ_öÅåÄÖäöÅ\" /></item1>",
        "  <item1><item2><item3 a=\"abcd\u5B66\uD800\uDF30\"/></item2></item1>",
        "  <item1>\u6C34</item1>",
        "  <item1><Ääkkönen><\u6C34>water</\u6C34></Ääkkönen></item1>",
    };
    private static final String inputXml = 
        "<root>" + 
        controlXmlElems[0] + controlXmlElems[1] + controlXmlElems[2] + controlXmlElems[3] + controlXmlElems[4] +
        "</root>";
    private static final String xmlHeader = "<?xml version=\"1.0\" encoding=\"{0}\"?>";
    private static final char[] BOM_UTF_8 = "\u00EF\u00BB\u00BF".toCharArray();
    private static final char[] BOM_UTF_16LE = "\u00FF\u00FE".toCharArray();
    private static final char[] BOM_UTF_16BE = "\u00FE\u00FF".toCharArray();
    //private static final char[] BOM_UTF_32LE = "\u00FF\u00FE\u0000\u0000".toCharArray();
    //private static final char[] BOM_UTF_32BE = "\u0000\u0000\u00FE\u00FF".toCharArray();

    private static boolean folderCleaned = false;
    
    /**
     * @see com.cordys.coe.ac.fileconnector.FileConnectorTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        cleanTempFolder = ! folderCleaned;
        folderCleaned = true;
        
        super.setUp();
    }
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testEncoding_Latin1() throws Exception
    {
         String[] xmlElems = {
            "  <item1 />",
            "  <item1><item2 a=\"ÄÖ_öÅåÄÖäöÅ\" /></item1>",
            "  <item1><Ääkkönen /></item1>",
        };
        String xml = 
            "<root>" + 
            xmlElems[0] + xmlElems[1] + xmlElems[2] +
            "</root>";
        
        String charset = "ISO-8859-1";
        String header = MessageFormat.format(xmlHeader, charset);
        
        // With header
        execute("/root/item1", xmlElems, null, header, xml, charset);
    }    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testEncoding_UTF8() throws Exception
    {
        String charset = "UTF-8";
        String header = MessageFormat.format(xmlHeader, charset);
        char[] bom = BOM_UTF_8;
        
        // No header
        execute("/root/item1", controlXmlElems, null, null, inputXml, charset);
        // With header
        execute("/root/item1", controlXmlElems, null, header, inputXml, charset);
        // With BOM
        execute("/root/item1", controlXmlElems, bom, null, inputXml, charset);
        // With BOM and header
        execute("/root/item1", controlXmlElems, bom, header, inputXml, charset);
    }    
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testEncoding_UTF16LE() throws Exception
    {
        String charset = "UTF-16LE";
        String header = MessageFormat.format(xmlHeader, "UTF-16");
        char[] bom = BOM_UTF_16LE;
        
        // With BOM
        execute("/root/item1", controlXmlElems, bom, null, inputXml, charset);
        // With BOM and header
        execute("/root/item1", controlXmlElems, bom, header, inputXml, charset);
    }    
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testEncoding_UTF16BE() throws Exception
    {
        String charset = "UTF-16BE";
        String header = MessageFormat.format(xmlHeader, "UTF-16");
        char[] bom = BOM_UTF_16BE;
        
        // With BOM
        execute("/root/item1", controlXmlElems, bom, null, inputXml, charset);
        // With BOM and header
        execute("/root/item1", controlXmlElems, bom, header, inputXml, charset);
    }    
    
        /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader#openFile()}.
     */
    public void testEncoding_UTFGB18030() throws Exception
    {
        String charset = "GB18030";
        String header = MessageFormat.format(xmlHeader, "GB18030");
        
        // With header
        execute("/root/item1", controlXmlElems, null, header, inputXml, charset);
    }    
    
    private void execute(String xpathStr, String[] elems, char[] bom, String header, String xml, String charset) throws Exception
    {
        String fileName = String.format("input-%s-%sheader-%sbom.xml", 
                                        charset, 
                                        header != null ? "with_" : "no_",
                                        bom != null ? "with_" : "no_");
        File file = new File(tempFolder, fileName);
        OutputStream os = null;
        
        try {
            os = new FileOutputStream(file);
            
            if (bom != null) {
                for (char c : bom)
                {
                    os.write(c & 0xFF);
                }
            }
            
            if (header != null) {
                os.write(header.getBytes(charset));
            }
            
            byte[] data = xml.getBytes(charset);

            os.write(data);
        }
        finally {
            FileUtils.closeStream(os);
        }
        
        LargeXmlFileReader reader = null;
        String ctxStr = null;
        int iter = 0;
        
        try {
            for (String cx : elems)
            {
                SimpleXPath xpath = new SimpleXPath(xpathStr);
                XmlTraverseContext ctx = null;
                
                if (ctxStr != null) {
                    ctx = XmlTraverseContext.serializeFromBase64String(ctxStr);
                }
                
                //System.out.printf("ITER: %d, pos=%04x\n",  iter + 1, ctx != null ? ctx.getCurrentEndOffset() : 0);
                
                reader = new LargeXmlFileReader(file, ctx, true);
                
                int nomTree = reader.findNext(dDoc, xpath, true);
               
                addNomGarbage(nomTree);
                assertNodesEqual(parse(cx), nomTree);
                
                ctx = reader.getCurrentContext();
                ctxStr = XmlTraverseContext.serializeToBase64String(ctx);
                reader.close();
                reader = null;
                iter++;
            }
        } 
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception ignored) {
                }
            }
        }
    }
}
 
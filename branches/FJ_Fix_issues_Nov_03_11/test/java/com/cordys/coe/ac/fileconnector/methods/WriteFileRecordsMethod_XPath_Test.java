/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods;

import java.io.File;
import java.text.MessageFormat;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.writer.WriterConfig;

/**
 * Test cases for the WriteFileRecords method. These test XPath expressions and namespace binding.
 *
 * @author mpoyhone
 */
public class WriteFileRecordsMethod_XPath_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Namespaces() throws Exception
    {
        String configXml =
            "<configuration>" +
            "<namespaces>"+
            "   <binding prefix=\"ns1\" uri=\"http://ns-uri-1\" />"+
            "   <binding prefix=\"ns2\" uri=\"http://ns-uri-2\" />"+
            "   <binding prefix=\"ns3\" uri=\"http://ns-uri-3\" />"+
            "</namespaces>"+
            "<filetype name=\"XPATH\">\r\n" + 
            "   <output path=\"./ns2:a/ns2:test\" />" + 
            "   <output type=\"text\"><input fixed=\"|\" /></output>\r\n" +
            "   <output path=\"./ns3:a/ns3:test\" />" + 
            "   <output type=\"text\"><input fixed=\"|\" /></output>\r\n" +
            "   <output path=\"./*[local-name() = 'a']/*[local-name() = 'test']\" />" +
            "   <output type=\"text\"><input fixed=\"|\" /></output>\r\n" +
            "   <output path=\".//*[local-name() = 'test']\" />" + 
            "   <output type=\"text\"><input fixed=\"|\" /></output>\r\n" +
            "</filetype>" +
            "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords " +
                "       xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\"" +
                "       xmlns:ns1=\"http://ns-uri-1\"" +     
                "       xmlns:ns2=\"http://ns-uri-2\"" +     
                "       xmlns:ns3=\"http://ns-uri-3\">" +     
                "    <filename>{0}</filename>" + 
                "    <filetype>XPATH</filetype>" + 
                "    <append>false</append>" + 
                "    <records>" +
                "      <ns1:root>" +
                "        <ns2:a>" +
                "           <ns2:test>xxx</ns2:test>" +      
                "        </ns2:a>" +
                "        <ns3:a>" +
                "           <ns3:test>yyy</ns3:test>" +      
                "        </ns3:a>" +
                "       </ns1:root>" +
                "    </records>" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "xxx|yyy|xxx|xxx|";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>16</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.txt");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        ApplicationConfiguration cfg = createAppConfig();
        WriterConfig writerCfg = new WriterConfig(parse(configXml), true);
        
        method.setConfiguration(writerCfg);
        cfg.setUseSimpleXPath(false);
        
        ISoapRequestContext request = executeMethod(cfg, method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertEquals(outputFileContents, readTextFile(outputFile, "ISO-8859-1"));
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }    
}

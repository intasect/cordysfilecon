/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods;

import java.io.File;
import java.text.MessageFormat;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.writer.WriterConfig;

/**
 * Test cases for the WriteFileRecords method. These test the sample configurations 
 * which are delivered with the connector.
 *
 * @author mpoyhone
 */
public class WriteFileRecordsMethod_Samples_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_CSV_Sample() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/writer-config.xml"));
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <filename>{0}</filename>\r\n" + 
                "    <filetype>csv-sample</filetype>\r\n" + 
                "    <append>true</append>\r\n" + 
                "    <records>\r\n" + 
                "        <tuple>\r\n" + 
                "            <old>\r\n" + 
                "                <Record>\r\n" + 
                "                    <ID>100</ID>\r\n" + 
                "                    <Name>CSV Test</Name>\r\n" + 
                "                    <Address>CSV Test Address</Address>\r\n" + 
                "                </Record>\r\n" + 
                "            </old>\r\n" + 
                "        </tuple>\r\n" + 
                "    </records>\r\n" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "100;CSV Test;CSV Test Address;\r\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>32</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.csv");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        WriterConfig cfg = new WriterConfig(configNode, true);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertEquals(outputFileContents, readTextFile(outputFile, "ISO-8859-1"));
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_FixedLength_Sample() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/writer-config.xml"));
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <filename>{0}</filename>\r\n" + 
                "    <filetype>fixedlength-sample</filetype>\r\n" + 
                "    <append>false</append>\r\n" + 
                "    <records>\r\n" + 
                "        <tuple>\r\n" + 
                "            <old>\r\n" + 
                "               <Record>\r\n" + 
                "                   <ID>100</ID>\r\n" + 
                "                   <Name>Fixed Length Test</Name>\r\n" + 
                "                   <Address>Fixed Length Test Address</Address>\r\n" + 
                "               </Record>"+ 
                "            </old>\r\n" + 
                "        </tuple>\r\n" + 
                "    </records>\r\n" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "   100   Fixed Length Test     Fixed Length Test Address\r\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>58</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.txt");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        WriterConfig cfg = new WriterConfig(configNode, true);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertEquals(outputFileContents, readTextFile(outputFile, "ISO-8859-1"));
    }    
}

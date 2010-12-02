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

/**
 * Test cases for the WriteFile method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class WriteFileMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents = "testfile contents";
        String writeContents = "WriteFile test data";
        String inputSoapXml = 
                "<WriteFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <append>false</append>\r\n" + 
                "  <encoded>false</encoded>\r\n" + 
                "  <data>{1}</data>\r\n" + 
                "</WriteFile>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />";
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        WriteFileMethod method = new WriteFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath(),
                                                                                 writeContents));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertTrue("File does not exists.", srcFile.exists());
        assertEquals(readTextFile(srcFile, encoding), writeContents);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Append() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents = "testfile contents";
        String writeContents = "WriteFile test data";
        String inputSoapXml = 
                "<WriteFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <append>true</append>\r\n" + 
                "  <encoded>false</encoded>\r\n" + 
                "  <data>{1}</data>\r\n" + 
                "</WriteFile>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />";
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        WriteFileMethod method = new WriteFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath(),
                                                                                 writeContents));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertTrue("File does not exists.", srcFile.exists());
        assertEquals(readTextFile(srcFile, encoding), inputFileContents + writeContents);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Encoded() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents = "testfile contents";
        String writeContents = "WriteFile test data";
        String writeEncodedContents = "V3JpdGVGaWxlIHRlc3QgZGF0YQ==\r\n";
        String inputSoapXml = 
                "<WriteFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <append>false</append>\r\n" + 
                "  <encoded>true</encoded>\r\n" + 
                "  <data>{1}</data>\r\n" + 
                "</WriteFile>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />";
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        WriteFileMethod method = new WriteFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath(),
                                                                                 writeEncodedContents));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertTrue("File does not exists.", srcFile.exists());
        assertEquals(readTextFile(srcFile, encoding), writeContents);
    }
}

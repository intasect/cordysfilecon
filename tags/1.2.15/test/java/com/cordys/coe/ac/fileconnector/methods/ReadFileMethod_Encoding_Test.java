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

/**
 * Test cases for the ReadFile method. These test character set encoding issues.
 *
 * @author mpoyhone
 */
public class ReadFileMethod_Encoding_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_ISO_8859_1() throws Exception
    {
        String encoding = "ISO-8859-1";
        String fileContents = "Latin1:ÄÖÅäöÅ";
        
        executeEncodingTestProcess(fileContents, encoding, fileContents, encoding, null);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_UTF_8() throws Exception
    {
        String encoding = "UTF-8";
        String fileContents = "Latin1:ÄÖÅäöÅ";
        
        executeEncodingTestProcess(fileContents, encoding, fileContents, encoding, null);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_CORDYS_LATIN1_ASCII() throws Exception
    {
        String encoding = "X-CORDYS-LATIN1-ASCII";
        String fileContents = "Latin1:ÄÖÅäöÅ";
        
        executeEncodingTestProcess(fileContents, "ISO-8859-1", fileContents, encoding, null);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcessWithSoapCharset_ISO_8859_1() throws Exception
    {
        String encoding = "ISO-8859-1";
        String fileContents = "Latin1:ÄÖÅäöÅ";
        
        // Configuration: UTF-8
        // SOAP: Latin-1
        executeEncodingTestProcess(fileContents, encoding, fileContents, "UTF-8", encoding);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcessWithSoapCharset_UTF_8() throws Exception
    {
        String encoding = "UTF-8";
        String fileContents = "Latin1:ÄÖÅäöÅ";
        
        // Configuration: Latin-1
        // SOAP: UTF-8
        executeEncodingTestProcess(fileContents, encoding, fileContents, "ISO-8859-1", encoding);
    }
    
    private void executeEncodingTestProcess(String inputFileContent, String fileEncoding, String responseContent, String configEncoding, String reqEncoding) throws Exception    {
        String inputSoapXml = 
                "<ReadFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <encoded>false</encoded>\r\n" +
                (reqEncoding != null ?  "  <charset>{1}</charset>\r\n" : "") +
                "</ReadFile>";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "   <data>{0}</data>" + 
                "</response>";        
        File srcFile = createTextFile("src.txt", inputFileContent, fileEncoding);
        ReadFileMethod method = new ReadFileMethod();
        
        ApplicationConfiguration acConfig = createAppConfig(configEncoding, configEncoding, null);
        ISoapRequestContext request = executeMethod(acConfig, method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath(),
                                                                                 reqEncoding));
        
        assertNodesEqual(parse(MessageFormat.format(outputSoapXml, responseContent)), request.getResponseRootNode());
    }    
}

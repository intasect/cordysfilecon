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
 * Test cases for the ReadFile method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class ReadFileMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents = "  > ReadFile test data <&";
        String inputSoapXml = 
                "<ReadFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <encoded>false</encoded>\r\n" + 
                "</ReadFile>";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "   <data>  &gt; ReadFile test data &lt;&amp;</data>" + 
                "</response>";        
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        ReadFileMethod method = new ReadFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Encoded() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents = "  > ReadFile test data <&";
        String inputSoapXml = 
                "<ReadFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <encoded>true</encoded>\r\n" + 
                "</ReadFile>";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "   <data>ICA+IFJlYWRGaWxlIHRlc3QgZGF0YSA8Jg==</data>" + 
                "</response>";        
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        ReadFileMethod method = new ReadFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }    
}

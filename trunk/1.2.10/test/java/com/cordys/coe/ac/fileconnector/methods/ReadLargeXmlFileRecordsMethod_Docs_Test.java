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
 * Test cases for the ReadLargeXMLFileRecords method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class ReadLargeXmlFileRecordsMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadLargeXmlFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Basic() throws Exception
    {
        String inputFileContents =
                "<?xml version=\"1.0\"?>\r\n" + 
                "<menu xmlns=\"\">\r\n" + 
                "    <menuitem>\r\n" + 
                "        <node>1</node>\r\n" + 
                "        <name>node 1</name>\r\n" + 
                "    </menuitem>\r\n" + 
                "    <menuitem>\r\n" + 
                "        <node>2</node>\r\n" + 
                "        <name>node 2</name>\r\n" + 
                "    </menuitem>\r\n" + 
                "</menu>";
        File inputFile = createTextFile("input.xml", inputFileContents);
        ReadLargeXmlFileRecordsMethod method;
        ISoapRequestContext request;
        
        String inputSoapXml1 = 
                "<ReadLargeXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <selectPath>menu/menuitem</selectPath>\r\n" + 
                "  <numrecords>1</numrecords>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "  <returnAsText>false</returnAsText>\r\n" + 
                "</ReadLargeXmlFileRecords>\r\n" + 
                "";
        String outputSoapXml1 = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data>\r\n" + 
                "      <menuitem xmlns=\"\">\r\n" + 
                "        <node>1</node>\r\n" + 
                "        <name>node 1</name>\r\n" + 
                "      </menuitem>\r\n" + 
                "    </data>\r\n" + 
                "    <recordsread>1</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "    <cursorData>AAExAQAFVVRGLTgAAAAAAAAAewABAQAIbWVudWl0ZW0AAAAAAAEBAARtZW51AAAAAAEAAQAAAA==</cursorData>" +
                "</response>";
        
        method = new ReadLargeXmlFileRecordsMethod();
        request = executeMethod(method, MessageFormat.format(inputSoapXml1, inputFile.getAbsolutePath()));
        assertNodesEqual(parse(outputSoapXml1), request.getResponseRootNode(), true);
        
        String inputSoapXml2 = 
                "<ReadLargeXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <selectPath>menu/menuitem</selectPath>\r\n" + 
                "  <numrecords>1</numrecords>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "  <returnAsText>false</returnAsText>\r\n" + 
                "  <cursorData>AAExAQAFVVRGLTgAAAAAAAAAewABAQAIbWVudWl0ZW0AAAAAAAEBAARtZW51AAAAAAEAAQAAAA==</cursorData>\r\n" + 
                "</ReadLargeXmlFileRecords>\r\n" + 
                "";
        String outputSoapXml2 = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data>\r\n" + 
                "      <menuitem xmlns=\"\">\r\n" + 
                "        <node>2</node>\r\n" + 
                "        <name>node 2</name>\r\n" + 
                "      </menuitem>\r\n" + 
                "    </data>\r\n" + 
                "    <recordsread>1</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "    <cursorData>AAExAQAFVVRGLTgAAAAAAAAA0AABAQAIbWVudWl0ZW0AAAAAAAEBAARtZW51AAAAAAEAAQAAAA==</cursorData>\r\n" + 
                "</response>";
        
        method = new ReadLargeXmlFileRecordsMethod();
        request = executeMethod(method, MessageFormat.format(inputSoapXml2, inputFile.getAbsolutePath()));
        assertNodesEqual(parse(outputSoapXml2), request.getResponseRootNode(), true);      
        
        String inputSoapXml3 = 
            "<ReadLargeXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
            "  <filename>{0}</filename>\r\n" + 
            "  <selectPath>menu/menuitem</selectPath>\r\n" + 
            "  <numrecords>1</numrecords>\r\n" + 
            "  <validateonly>false</validateonly>\r\n" + 
            "  <returnAsText>false</returnAsText>\r\n" + 
            "  <cursorData>AAExAQAFVVRGLTgAAAAAAAAA0AABAQAIbWVudWl0ZW0AAAAAAAEBAARtZW51AAAAAAEAAQAAAA==</cursorData>\r\n" + 
            "</ReadLargeXmlFileRecords>\r\n" + 
            "";
        String outputSoapXml3 = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data />\r\n" + 
                "    <recordsread>0</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "    <cursorData>AAExAQAFVVRGLTgAAAAAAAAAAAE=</cursorData>\r\n" + 
                "</response>";
        
        // We must get the same end response for multiple calls.
        for (int i = 0; i < 5; i++) {
            method = new ReadLargeXmlFileRecordsMethod();
            request = executeMethod(method, MessageFormat.format(inputSoapXml3, inputFile.getAbsolutePath()));
            assertNodesEqual(parse(outputSoapXml3), request.getResponseRootNode(), true);
        }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadLargeXmlFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_ReturnAsText() throws Exception
    {
        String inputFileContents =
                "<?xml version=\"1.0\"?>\r\n" + 
                "<menu xmlns=\"\">\r\n" + 
                "    <menuitem>\r\n" + 
                "        <node>1</node>\r\n" + 
                "        <name>node 1</name>\r\n" + 
                "    </menuitem>\r\n" + 
                "    <menuitem>\r\n" + 
                "        <node>2</node>\r\n" + 
                "        <name>node 2</name>\r\n" + 
                "    </menuitem>\r\n" + 
                "</menu>";
        File inputFile = createTextFile("input.xml", inputFileContents);
        ReadLargeXmlFileRecordsMethod method;
        ISoapRequestContext request;
        
        String inputSoapXml = 
                "<ReadLargeXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <selectPath>menu/menuitem</selectPath>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "  <returnAsText>true</returnAsText>\r\n" + 
                "</ReadLargeXmlFileRecords>\r\n" + 
                "";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data>&lt;menuitem xmlns=&quot;&quot;&gt;&lt;node&gt;1&lt;/node&gt;&lt;name&gt;node 1&lt;/name&gt;&lt;/menuitem&gt;&lt;menuitem xmlns=&quot;&quot;&gt;&lt;node&gt;2&lt;/node&gt;&lt;name&gt;node 2&lt;/name&gt;&lt;/menuitem&gt;</data>\r\n" + 
                "    <recordsread>2</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "    <cursorData>AAExAQAFVVRGLTgAAAAAAAAAAAE=</cursorData>" +
                "</response>";
        
        method = new ReadLargeXmlFileRecordsMethod();
        request = executeMethod(method, MessageFormat.format(inputSoapXml, inputFile.getAbsolutePath()));
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode(), true);
    }
}
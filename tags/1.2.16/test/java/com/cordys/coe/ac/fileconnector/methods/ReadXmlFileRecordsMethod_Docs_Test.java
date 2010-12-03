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
 * Test cases for the ReadXMLFileRecords method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class ReadXmlFileRecordsMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Basic() throws Exception
    {
        String inputFileContents =
                "<?xml version=\"1.0\"?> \r\n" + 
                "<menu xmlns=\"\"> \r\n" + 
                "    <menuitem> \r\n" + 
                "        <node>1</node> \r\n" + 
                "        <name>node 1</name> \r\n" + 
                "    </menuitem> \r\n" + 
                "    <menuitem> \r\n" + 
                "        <node>2</node> \r\n" + 
                "        <name>node 2</name> \r\n" + 
                "    </menuitem> \r\n" + 
                "    <menuitem> \r\n" + 
                "        <node>3</node> \r\n" + 
                "        <name>node 3</name> \r\n" + 
                "    </menuitem> \r\n" + 
                "</menu>\r\n" + 
                "";
        String inputSoapXml = 
                "<ReadXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <selectPath>menu/menuitem</selectPath>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>1</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "  <returnAsText>false</returnAsText>\r\n" + 
                "</ReadXmlFileRecords>\r\n" + 
                "";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data>\r\n" + 
                "      <menuitem>\r\n" + 
                "        <node>2</node>\r\n" + 
                "        <name>node 2</name>\r\n" + 
                "      </menuitem>\r\n" + 
                "      <menuitem>\r\n" + 
                "        <node>3</node>\r\n" + 
                "        <name>node 3</name>\r\n" + 
                "      </menuitem>\r\n" + 
                "    </data>\r\n" + 
                "    <endoffset>3</endoffset>\r\n" + 
                "    <recordsread>2</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File inputFile = createTextFile("input.xml", inputFileContents);
        ReadXmlFileRecordsMethod method = new ReadXmlFileRecordsMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, inputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode(), true);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_ReturnAsText() throws Exception
    {
        String inputFileContents =
                "<?xml version=\"1.0\"?> \r\n" + 
                "<menu xmlns=\"\"> \r\n" + 
                "    <menuitem> \r\n" + 
                "        <node>1</node> \r\n" + 
                "        <name>node 1</name> \r\n" + 
                "    </menuitem> \r\n" + 
                "    <menuitem> \r\n" + 
                "        <node>2</node> \r\n" + 
                "        <name>node 2</name> \r\n" + 
                "    </menuitem> \r\n" + 
                "    <menuitem> \r\n" + 
                "        <node>3</node> \r\n" + 
                "        <name>node 3</name> \r\n" + 
                "    </menuitem> \r\n" + 
                "</menu>\r\n" + 
                "";
        String inputSoapXml = 
                "<ReadXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <selectPath>menu/menuitem</selectPath>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>1</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "  <returnAsText>true</returnAsText>\r\n" + 
                "</ReadXmlFileRecords>\r\n" + 
                "";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data>&lt;menuitem&gt;&lt;node&gt;2&lt;/node&gt;&lt;name&gt;node 2&lt;/name&gt;&lt;/menuitem&gt;&lt;menuitem&gt;&lt;node&gt;3&lt;/node&gt;&lt;name&gt;node 3&lt;/name&gt;&lt;/menuitem&gt;</data>\r\n" + 
                "    <endoffset>3</endoffset>\r\n" + 
                "    <recordsread>2</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File inputFile = createTextFile("input.xml", inputFileContents);
        ReadXmlFileRecordsMethod method = new ReadXmlFileRecordsMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, inputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode(), true);
    }
}

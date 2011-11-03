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
import com.cordys.coe.ac.fileconnector.validator.ValidatorConfig;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Test cases for the ReadFileRecords method.
 *
 * @author mpoyhone
 */
public class ReadFileRecordsMethod_Samples_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_CSV_Sample() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/reader-config.xml"));
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>csv-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = 
                "100;CSV Test;CSV Test Adress;\n" + 
        		"101;Name-101;Adress-101;\n" + 
        		"102;Name-102;Adress-102;\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" +  
                "  <endoffset>80</endoffset>\r\n" + 
                "  <recordsread>3</recordsread>\r\n" + 
                "  <endoffile>true</endoffile>\r\n" +
                "  <errorcount>0</errorcount>\r\n" + 
                "  <data>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>100</ID>\r\n" + 
                "        <Name>CSV Test</Name>\r\n" + 
                "        <Address>CSV Test Adress</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>101</ID>\r\n" + 
                "        <Name>Name-101</Name>\r\n" + 
                "        <Address>Adress-101</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>102</ID>\r\n" + 
                "        <Name>Name-102</Name>\r\n" + 
                "        <Address>Adress-102</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "  </data>\r\n" + 
                "</response>";
        File file = createTextFile("input.csv", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_CSV_Sample_One_Record() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/reader-config.xml"));
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>csv-sample</filetype>\r\n" + 
                "  <numrecords>1</numrecords>\r\n" + 
                "  <offset>{1}</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = 
                "100;CSV Test;CSV Test Adress;\n" + 
                "101;Name-101;Adress-101;\n" + 
                "102;Name-101;Adress-102;\n";
        String[] outputSoapXmls = 
            {
                "  <data>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>100</ID>\r\n" + 
                "        <Name>CSV Test</Name>\r\n" + 
                "        <Address>CSV Test Adress</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" +
                "  </data>\r\n"         
                ,
                "  <data>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>101</ID>\r\n" + 
                "        <Name>Name-101</Name>\r\n" + 
                "        <Address>Adress-101</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" +
                "  </data>\r\n"           
                ,
                "  <data>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>102</ID>\r\n" + 
                "        <Name>Name-101</Name>\r\n" + 
                "        <Address>Adress-102</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" +
                "  </data>\r\n"            
            };
        File file = createTextFile("input.csv", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        long offset = 0;
        
        for (int i = 0; i < outputSoapXmls.length; i++)
        {
            ISoapRequestContext request = executeMethod(method, 
                                                        MessageFormat.format(inputSoapXml, 
                                                                             file.getAbsolutePath(),
                                                                             offset));
            
            offset = Long.parseLong(Node.getData(Find.firstMatch(request.getResponseRootNode(), "?<endoffset>")));
            assertNodesEqual(parse(outputSoapXmls[i]), Find.firstMatch(request.getResponseRootNode(), "?<data>"));
        }
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_CSV_Sample_NUL_Character() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/reader-config.xml"));
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>csv-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = 
                "100;CSV Test;CSV\000Test\000Adress;\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <endoffset>30</endoffset>\r\n" + 
                "  <recordsread>1</recordsread>\r\n" + 
                "  <endoffile>true</endoffile>\r\n" +
                "  <errorcount>0</errorcount>\r\n" + 
                "  <data>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <ID>100</ID>\r\n" + 
                "        <Name>CSV Test</Name>\r\n" + 
                "        <Address>CSV Test Adress</Address>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "  </data>\r\n" + 
                "</response>";
        File file = createTextFile("input.csv", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
        int controlNode = parse(outputSoapXml);
        int addrNode = Find.firstMatch(controlNode, "?<Address>");
        
        Node.setDataElement(addrNode, "", Node.getData(addrNode).replaceAll(" ", "\u0000"));
        assertNodesEqual(controlNode, request.getResponseRootNode());
    }    
    

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_CSV_Sample_Invalid_File() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/reader-config.xml"));
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>csv-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = 
                "100;CSV Test:CSV Test Adress;\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <endoffset>0</endoffset>\r\n" + 
                "  <recordsread>0</recordsread>\r\n" + 
                "  <endoffile>false</endoffile>\r\n" +
                "  <errors>\r\n" + 
                "    <item>Unable to the parse the file. * Unable to the parse the file. * At line 0 : No matching record found. Already read records: </item>\r\n" + 
                "  </errors>\r\n" + 
                "  <errorcount>1</errorcount>\r\n" + 
                "</response>";
        File file = createTextFile("input.csv", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
 
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }    
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_FixedLength_Sample() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/reader-config.xml"));
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>fixedlength-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = 
                "   100   Fixed Length Test      Fixed Length Test Adress\n" + 
                "   101            Name-101                    Adress-101\n" + 
                "   102            Name-102                    Adress-102\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" +  
                " <endoffset>171</endoffset>\r\n" + 
                " <recordsread>3</recordsread>\r\n" + 
                " <endoffile>true</endoffile>\r\n" +
                " <errorcount>0</errorcount>\r\n" + 
                " <data>\r\n" + 
                "   <tuple>\r\n" + 
                "     <line>\r\n" + 
                "       <ID>100</ID>\r\n" + 
                "       <Name>Fixed Length Test</Name>\r\n" + 
                "       <Address>Fixed Length Test Adress</Address>\r\n" + 
                "     </line>\r\n" + 
                "   </tuple>\r\n" + 
                "   <tuple>\r\n" + 
                "    <line>\r\n" + 
                "       <ID>101</ID>\r\n" + 
                "       <Name>Name-101</Name>\r\n" + 
                "       <Address>Adress-101</Address>\r\n" + 
                "     </line>\r\n" + 
                "   </tuple>\r\n" + 
                "   <tuple>\r\n" + 
                "     <line>\r\n" + 
                "       <ID>102</ID>\r\n" + 
                "       <Name>Name-102</Name>\r\n" + 
                "       <Address>Adress-102</Address>\r\n" + 
                "     </line>\r\n" + 
                "   </tuple>\r\n" + 
                " </data>\r\n" + 
                "</response>";
        File file = createTextFile("input.txt", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_FixedLength_Sample_NUL_Character() throws Exception
    {
        int configNode = loadXmlFile(new File("docs/external/samples/reader-config.xml"));
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>fixedlength-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = 
                "   100   Fixed Length Test      Fixed Length Test Adress\000\000\000\n";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                " <endoffset>60</endoffset>\r\n" + 
                " <recordsread>1</recordsread>\r\n" + 
                " <endoffile>true</endoffile>\r\n" +
                " <errorcount>0</errorcount>\r\n" + 
                " <data>\r\n" + 
                "   <tuple>\r\n" + 
                "     <line>\r\n" + 
                "       <ID>100</ID>\r\n" + 
                "       <Name>Fixed Length Test</Name>\r\n" + 
                "       <Address>Fixed Length Test Adress</Address>\r\n" + 
                "     </line>\r\n" + 
                "   </tuple>\r\n" + 
                " </data>\r\n" + 
                "</response>";
        File file = createTextFile("input.txt", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
        int controlNode = parse(outputSoapXml);
        int addrNode = Find.firstMatch(controlNode, "?<Address>");
        
        Node.setDataElement(addrNode, "", Node.getData(addrNode) + "\000\000\000");
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }    
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_FixedLength_Sample_EmptyFile() throws Exception
    {
        int configNode = parse(
                "<configuration>\r\n" + 
        		"    <filetype name=\"fixedlength-sample\" recordsequence=\"line\" allowempty=\"false\">\r\n" + 
        		"        <record name=\"line\" pattern=\"([^\\n\\r]*)[\\n\\r]+\" index=\"0\">\r\n" + 
        		"            <field name=\"ID\" pattern=\"\\s*(\\d+)\" index=\"0\" width=\"6\" />\r\n" + 
        		"        </record>\r\n" + 
        		"    </filetype> \r\n" + 
        		"</configuration>");
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>fixedlength-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = "";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <endoffset>0</endoffset>\r\n" + 
                "  <recordsread>0</recordsread>\r\n" + 
                "  <endoffile>true</endoffile>\r\n" +
                "  <errors>\r\n" + 
                "    <item>Unable to the parse the file. * Unable to the parse the file. * File is empty.</item>\r\n" + 
                "  </errors>\r\n" + 
                "  <errorcount>1</errorcount>\r\n" + 
                "</response>";
        File file = createTextFile("input.txt", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));

        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }    
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_FixedLength_Sample_EmptyFile_CheckEOF() throws Exception
    {
        int configNode = parse(
                "<configuration>\r\n" + 
                "    <filetype name=\"fixedlength-sample\" recordsequence=\"line\">\r\n" + 
                "        <record name=\"line\" pattern=\"([^\\n\\r]*)[\\n\\r]+\" index=\"0\">\r\n" + 
                "            <field name=\"ID\" pattern=\"\\s*(\\d+)\" index=\"0\" width=\"6\" />\r\n" + 
                "        </record>\r\n" + 
                "    </filetype> \r\n" + 
                "</configuration>");
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>fixedlength-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "</ReadFileRecords>";
        String inputFile = "";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <endoffset>0</endoffset>\r\n" + 
                "  <recordsread>0</recordsread>\r\n" + 
                "  <endoffile>true</endoffile>\r\n" +
                "  <errorcount>0</errorcount>\r\n" + 
                "  <data/>\r\n" +
                "</response>";
        File file = createTextFile("input.txt", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(configNode);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));

        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    } 
}

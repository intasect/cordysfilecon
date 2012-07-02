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

/**
 * Test cases for the ReadFileRecords method. These test cases come from real-life projects.
 *
 * @author mpoyhone
 */
public class ReadFileRecordsMethod_UserCases_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     * @throws Exception Thrown if something failed.
     */
    public void testCase1() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"bpd\" recordsequence=\"line\">\r\n" + 
        		"    <record name=\"line\" pattern=\"([^\\n\\r]*)[\\n\\r]+\" index=\"0\">\r\n" + 
        		"        <field name=\"kolom_a\" pattern=\"([^;]*);\" index=\"0\" />\r\n" + 
        		"        <field name=\"kolom_b\" pattern=\"([^;]*);\" index=\"0\" />\r\n" + 
        		"        <field name=\"kolom_c\" pattern=\"([^;]*);\" index=\"0\" />\r\n" + 
        		"        <field name=\"kolom_d\" pattern=\"([^;]*);\" index=\"0\" />\r\n" + 
        		"        <field name=\"kolom_e\" pattern=\"([^;]*);\" index=\"0\" />\r\n" + 
        		"        <field name=\"kolom_f\" pattern=\"([^;]*)\" index=\"0\" />\r\n" + 
        		"    </record>\r\n" + 
        		"</filetype>\r\n" + 
        		"</configuration>";
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>bpd</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>true</validateonly>\r\n" +
                "  <usetupleold>false</usetupleold> " + 
                "</ReadFileRecords>";
        String inputFile = 
                "001;300;00000022100;0000000;0000000000000,11;Kantoorinventaris (begin)     \n" + 
                "001;300;00000022101;0000000;0000000000000,00;Kantoorinventaris (inv)       \n" + 
                "001;300;00000023100;0000000;0000000000000,96;Computer Hardware (begin)     \n" + 
                "001;300;00000023101;0000000;0000000000000,12;Computer Hardware (inv)       \n";
        String outputSoapXml = 
                "<?xml version=\"1.0\"?>\r\n" + 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <endoffset>304</endoffset>\r\n" + 
                "  <recordsread>4</recordsread>\r\n" + 
                "  <endoffile>true</endoffile>\r\n" + 
                "  <errorcount>0</errorcount>\r\n" + 
                "</response>\r\n" + 
                "";
        File file = createTextFile("input.csv", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(parse(configXml));
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     * @throws Exception Thrown if something failed.
     */
    public void testCase2() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"fixedlength-sample\" recordsequence=\"line\">\r\n" + 
                "    <record name=\"line\" pattern=\"(.*)[\\r\\n]*\" index=\"0\">    \r\n" + 
                "      <field name=\"MessageType\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"OrderNumber\" pattern=\"([^\\|]*)\\|\" index=\"0\" />\r\n" + 
                "      <field name=\"ErrorCode\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"Description\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"DeliveryDate\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"PlannedDeliveryDate\" pattern=\"([^\\|]*)\\|\" index=\"0\" />\r\n" + 
                "      <field name=\"SomeNumber\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"Article\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"Unit\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"Number\" pattern=\"([^\\|]*)\\|\" index=\"0\"/>\r\n" + 
                "      <field name=\"OrderLineNumber\" pattern=\"([^\\|]*)$\" index=\"0\"/>\r\n" + 
                "    </record>\r\n" + 
                "</filetype>\r\n" + 
                "</configuration>";
        String inputSoapXml = 
                "<ReadFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <filetype>fixedlength-sample</filetype>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" +
                "  <usetupleold>false</usetupleold> " + 
                "</ReadFileRecords>";
        String inputFile = 
                "GLSRES20090225113830|xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1xGF9OM|0000|VerzendingxOK|20090225|x|x|845371|ST|1|0002\r\n" + 
                "GLSRES20090225113830|xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1xGF9OM|0000|VerzendingxOK|20090225|x|x|847619|ST|1|0003\r\n" + 
                "GLSRES20090225113830|xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1xGF9OM|0000|VerzendingxOK|20090225|x|x|845589|ST|1|0002";
        String outputSoapXml = 
                "<?xml version=\"1.0\"?>\r\n" + 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <endoffset>364</endoffset>\r\n" + 
                "  <recordsread>3</recordsread>\r\n" + 
                "  <endoffile>true</endoffile>\r\n" + 
                "  <errorcount>0</errorcount>\r\n" + 
                "  <data>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <MessageType>GLSRES20090225113830</MessageType>\r\n" + 
                "        <OrderNumber>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1xGF9OM</OrderNumber>\r\n" + 
                "        <ErrorCode>0000</ErrorCode>\r\n" + 
                "        <Description>VerzendingxOK</Description>\r\n" + 
                "        <DeliveryDate>20090225</DeliveryDate>\r\n" + 
                "        <PlannedDeliveryDate>x</PlannedDeliveryDate>\r\n" + 
                "        <SomeNumber>x</SomeNumber>\r\n" + 
                "        <Article>845371</Article>\r\n" + 
                "        <Unit>ST</Unit>\r\n" + 
                "        <Number>1</Number>\r\n" + 
                "        <OrderLineNumber>0002</OrderLineNumber>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <MessageType>GLSRES20090225113830</MessageType>\r\n" + 
                "        <OrderNumber>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1xGF9OM</OrderNumber>\r\n" + 
                "        <ErrorCode>0000</ErrorCode>\r\n" + 
                "        <Description>VerzendingxOK</Description>\r\n" + 
                "        <DeliveryDate>20090225</DeliveryDate>\r\n" + 
                "        <PlannedDeliveryDate>x</PlannedDeliveryDate>\r\n" + 
                "        <SomeNumber>x</SomeNumber>\r\n" + 
                "        <Article>847619</Article>\r\n" + 
                "        <Unit>ST</Unit>\r\n" + 
                "        <Number>1</Number>\r\n" + 
                "        <OrderLineNumber>0003</OrderLineNumber>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "    <tuple>\r\n" + 
                "      <line>\r\n" + 
                "        <MessageType>GLSRES20090225113830</MessageType>\r\n" + 
                "        <OrderNumber>xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx1xGF9OM</OrderNumber>\r\n" + 
                "        <ErrorCode>0000</ErrorCode>\r\n" + 
                "        <Description>VerzendingxOK</Description>\r\n" + 
                "        <DeliveryDate>20090225</DeliveryDate>\r\n" + 
                "        <PlannedDeliveryDate>x</PlannedDeliveryDate>\r\n" + 
                "        <SomeNumber>x</SomeNumber>\r\n" + 
                "        <Article>845589</Article>\r\n" + 
                "        <Unit>ST</Unit>\r\n" + 
                "        <Number>1</Number>\r\n" + 
                "        <OrderLineNumber>0002</OrderLineNumber>\r\n" + 
                "      </line>\r\n" + 
                "    </tuple>\r\n" + 
                "  </data>\r\n" + 
                "</response>\r\n" + 
                "";
        File file = createTextFile("input.csv", inputFile, "ISO-8859-1");
        ReadFileRecordsMethod method = new ReadFileRecordsMethod();
        ValidatorConfig cfg = new ValidatorConfig(parse(configXml));
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, file.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
    }
}

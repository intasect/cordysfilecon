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
 * Test cases for the WriteFileRecords method. This test different output types.
 *
 * @author mpoyhone
 */
public class WriteFileRecordsMethod_OutputTypes_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_TestDate() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"DATE\">\r\n" + 
                "   <output path=\"./a/date\" \r\n" + 
                "          type=\"date\"\r\n" + 
                "          informat=\"yyyy-MM-dd\'T\'HH:mm:ss.SSS\"\r\n" + 
                "          outformat=\"dd_MM_yy\" />\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +    
                "   <output path=\"./a/empty-date\" \r\n" + 
                "          type=\"date\"\r\n" + 
                "          informat=\"yyyy-MM-dd\'T\'HH:mm:ss.SSS\"\r\n" + 
                "          outformat=\"dd_MM_yy\" />\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +                  
                "   <output path=\"./a/empty-date\" \r\n" + 
                "          type=\"date\"\r\n" + 
                "          informat=\"yyyy-MM-dd\'T\'HH:mm:ss.SSS\"\r\n" + 
                "          outformat=\"dd_MM_yy\" " +
                "          default=\"9999-01-01T00:00:00.000\"/>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +  
                "   <output path=\"./a/null-date\" \r\n" + 
                "          type=\"date\"\r\n" + 
                "          informat=\"yyyy-MM-dd\'T\'HH:mm:ss.SSS\"\r\n" + 
                "          outformat=\"dd_MM_yy\" />\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +   
                "   <output path=\"./a/null-date\" \r\n" + 
                "          type=\"date\"\r\n" + 
                "          informat=\"yyyy-MM-dd\'T\'HH:mm:ss.SSS\"\r\n" + 
                "          outformat=\"dd_MM_yy\" " +
                "          default=\"9999-01-01T00:00:00.000\"/>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +                  
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">" + 
                "    <filename>{0}</filename>" + 
                "    <filetype>DATE</filetype>" + 
                "    <append>false</append>" + 
                "    <records>" +
                "      <root>" +
                "        <a>" +
                "           <date>2008-01-03T11:22:33.456</date>" +      
                "           <empty-date></empty-date>" +      
                "           <null-date/>" +      
                "        </a>" +
                "       </root>" +
                "    </records>" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "03_01_08||01_01_99||01_01_99|";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>29</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.txt");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        WriterConfig cfg = new WriterConfig(parse(configXml), true);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertEquals(outputFileContents, readTextFile(outputFile, "ISO-8859-1"));
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_TestNumber() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"NUMBER\">\r\n" + 
                "   <output path=\"./a/number\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          format=\"0\"\r\n" +
                "          align=\"right\"" +
                "          width=\"2\" />\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                   
                "   <output path=\"./a/number\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          width=\"2\"\r\n" + 
                "          format=\"00\"\r\n" +
                "          align=\"right\" />\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +    
                "   <output path=\"./a/number\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          width=\"2\"\r\n" + 
                "          format=\"0\"\r\n" +
                "          align=\"right\"" +                
                "          padchar=\"0\" />\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" +    
                "   </output>\r\n" +   
                "   <output path=\"./a/number-empty\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          width=\"2\"\r\n" + 
                "          align=\"right\"" +
                "          format=\"00\" />\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                   
                "   <output path=\"./a/number-empty\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          width=\"2\"\r\n" + 
                "          align=\"right\"" +
                "          format=\"0\"" +
                "          padchar=\"0\" />\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +    
                "   <output path=\"./a/number-null\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          width=\"2\"\r\n" + 
                "          align=\"right\"" +
                "          format=\"0\"" +
                "          padchar=\"0\" />\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                  
                "   <output path=\"./a/number-null\" \r\n" + 
                "          type=\"number\"\r\n" +
                "          width=\"2\"\r\n" + 
                "          align=\"right\"" +
                "          format=\"00\" />\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                  
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">" + 
                "    <filename>{0}</filename>" + 
                "    <filetype>NUMBER</filetype>" + 
                "    <append>false</append>" + 
                "    <records>" +
                "      <root>" +
                "        <a>" +
                "           <number>5</number>" +      
                "           <empty-number></empty-number>" +      
                "           <null-number/>" +      
                "        </a>" +
                "       </root>" +
                "    </records>" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                " 5|05|05|00|00|00|00|";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>21</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.txt");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        WriterConfig cfg = new WriterConfig(parse(configXml), true);
        
        method.setConfiguration(cfg);
        
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertEquals(outputFileContents, readTextFile(outputFile, "ISO-8859-1"));
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Encoding_UTF8() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"UTF_8\">\r\n" + 
                "   <output path=\"./a/text\" />" + 
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">" + 
                "    <filename>{0}</filename>" + 
                "    <filetype>UTF_8</filetype>" + 
                "    <append>false</append>" + 
                "    <records>" +
                "      <root>" +
                "        <a>" +
                "           <text>Latin1:ÄÖÅäöå</text>" +      
                "        </a>" +
                "       </root>" +
                "    </records>" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "Latin1:ÄÖÅäöå";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>19</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.txt");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        WriterConfig cfg = new WriterConfig(parse(configXml), true);
        
        method.setConfiguration(cfg);
        
        ApplicationConfiguration acConfig = createAppConfig("UTF-8", "UTF-8", null);
        ISoapRequestContext request = executeMethod(acConfig, method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertEquals(outputFileContents, readTextFile(outputFile, "UTF-8"));
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Encoding_CORDYS_LATIN1_ASCII() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"CORDYS_LATIN1_ASCII\">\r\n" + 
                "   <output path=\"./a/text\" />" + 
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">" + 
                "    <filename>{0}</filename>" + 
                "    <filetype>CORDYS_LATIN1_ASCII</filetype>" + 
                "    <append>false</append>" + 
                "    <records>" +
                "      <root>" +
                "        <a>" +
                "           <text>Latin1:ÄÖÅäöå</text>" +      
                "        </a>" +
                "       </root>" +
                "    </records>" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "Latin1:AOAaoa";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>13</endoffset>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        File outputFile = new File(tempFolder, "output.txt");
        WriteFileRecordsMethod method = new WriteFileRecordsMethod();
        WriterConfig cfg = new WriterConfig(parse(configXml), true);
        
        method.setConfiguration(cfg);
        
        ApplicationConfiguration acConfig = createAppConfig("X-CORDYS-LATIN1-ASCII", "X-CORDYS-LATIN1-ASCII", null);
        ISoapRequestContext request = executeMethod(acConfig, method, MessageFormat.format(inputSoapXml, outputFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertEquals(outputFileContents, readTextFile(outputFile, "ISO-8859-1"));
    }
}

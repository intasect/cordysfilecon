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
 * Test cases for the WriteFileRecords method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class WriteFileRecordsMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Basic() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"DOCS\">\r\n" + 
                "    <select path=\"a/b\">\r\n" + 
                "        <output path=\"./c\" type=\"text\" />\r\n" + 
                "    </select>\r\n" + 
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <filename>{0}</filename>\r\n" + 
                "    <filetype>DOCS</filetype>\r\n" + 
                "    <append>false</append>\r\n" + 
                "    <records>\r\n" +
                "       <a>\r\n" +
                "           <b>\r\n" +
                "               <c>Text value</c>\r\n" +
                "           </b>\r\n" +
                "       </a>\r\n" +
                "    </records>\r\n" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "Text value";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>10</endoffset>\r\n" + 
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
    public void testProcess_Examples() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"DOCS\">\r\n" + 
                "   <output path=\"./a/string\" \r\n" + 
                "           type=\"text\" \r\n" + 
                "           width=\"10\" \r\n" + 
                "           align=\"right\" \r\n" + 
                "           mustexist=\"true\" />" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                 
                "   <output path=\"./a/boolean/true\" type=\"boolean\" outvalues=\"N Y\" />" +
                "   <output path=\"./a/boolean/false\" type=\"boolean\" outvalues=\"N Y\" />" +
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" + 
                "   <output path=\"./a/date\" \r\n" + 
                "           type=\"date\"\r\n" + 
                "           informat=\"yyyy-MM-dd\'T\'HH:mm:ss.SSS\"\r\n" + 
                "           outformat=\"HH:mm:ss\" />" +
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"\\r\\n\" />\r\n" + 
                "   </output>\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <filter type=\"regexp-replaceall\" \r\n" + 
                "             regexp=\"\\s+\"\r\n" + 
                "             output=\"\">\r\n" + 
                "          <input path=\"./a/spaces\" />\r\n" + 
                "       </filter>\r\n" + 
                "   </output>" +
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <filter type=\"regexp-replacefirst\" \r\n" + 
                "             regexp=\".*@(.*)\"\r\n" + 
                "             output=\"$1\">\r\n" + 
                "        <input path=\"./a/email\"  />\r\n" + 
                "        </filter>\r\n" + 
                "    </output>\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <filter type=\"uppercase\">\r\n" + 
                "        <input path=\"./a/lowercase\" />\r\n" + 
                "       </filter>\r\n" + 
                "   </output>\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "   </output>\r\n" +                 
                "   <foreach path=\"./a/foreach/c\">\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "            <input path=\".\" type=\"text\" />\r\n" + 
                "            <input fixed=\"*\"/>\r\n" + 
                "        </output>\r\n" + 
                "   </foreach>\r\n" +
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +        
                "   <foreach path=\"./a/foreach/c\" mincount=\"3\">\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "            <input path=\".\" type=\"text\" />\r\n" + 
                "            <input fixed=\"*\"/>\r\n" + 
                "        </output>\r\n" + 
                "   </foreach>\r\n" +
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +      
                "   <if>\r\n" + 
                "      <condition>\r\n" + 
                "        <exists path=\"./a/string\"/>\r\n" + 
                "      </condition>\r\n" + 
                "      <then>\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "          <input fixed=\"found\"/>\r\n" + 
                "        </output>\r\n" + 
                "      </then>\r\n" + 
                "      <else>\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "          <input fixed=\"not found\"/>\r\n" + 
                "        </output>\r\n" + 
                "      </else>\r\n" + 
                "    </if>\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +            
                "   <if>\r\n" + 
                "      <condition>\r\n" + 
                "        <exists path=\"./a/nostring\"/>\r\n" + 
                "      </condition>\r\n" + 
                "      <then>\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "          <input fixed=\"found\"/>\r\n" + 
                "        </output>\r\n" + 
                "      </then>\r\n" + 
                "      <else>\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "          <input fixed=\"not found\"/>\r\n" + 
                "        </output>\r\n" + 
                "      </else>\r\n" + 
                "    </if>\r\n" + 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +                  
                "   <if>\r\n" + 
                "      <condition>\r\n" + 
                "        <xpath path=\"./a/string = 'Test'\"/>\r\n" + 
                "      </condition>\r\n" + 
                "      <then>\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "          <input fixed=\"found\"/>\r\n" + 
                "        </output>\r\n" + 
                "      </then>\r\n" + 
                "      <else>\r\n" + 
                "        <output type=\"text\">\r\n" + 
                "          <input fixed=\"not found\"/>\r\n" + 
                "        </output>\r\n" + 
                "      </else>\r\n" + 
                "    </if>\r\n" +                 
                "   <output type=\"text\">\r\n" + 
                "       <input fixed=\"|\" />\r\n" + 
                "    </output>\r\n" +                       
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">" + 
                "    <filename>{0}</filename>" + 
                "    <filetype>DOCS</filetype>" + 
                "    <append>false</append>" + 
                "    <records>" +
                "      <root>" +
                "        <a>" +
                "           <string>Test</string>" +
                "           <boolean><true>y</true><false>n</false></boolean>" +
                "           <date>2004-01-31T20:10:01.200</date>" +      
                "           <spaces>  1   </spaces>" +
                "           <email>testaddr@somewhere.com</email>" +
                "           <lowercase>this is lowercase</lowercase>" +    
                "           <foreach>" +  
                "              <c>XML Test</c>" + 
                "              <c>Test XML</c>" + 
                "           </foreach>" +     
                "        </a>" +
                "       </root>" +
                "    </records>" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "      Test|YN|20:10:01|\r\n|1|somewhere.com|THIS IS LOWERCASE|" +
                "XML Test*Test XML*|XML Test*Test XML**|found|not found|found|";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>121</endoffset>\r\n" + 
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
}

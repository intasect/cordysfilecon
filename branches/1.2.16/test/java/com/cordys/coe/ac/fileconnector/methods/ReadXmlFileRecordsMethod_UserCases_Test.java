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
 * Test cases for the ReadXMLFileRecords method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class ReadXmlFileRecordsMethod_UserCases_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_XPath() throws Exception
    {
        String inputFileContents =
                "<DEKRAConfig>\r\n" + 
                "    <ftp_user>i1</ftp_user>\r\n" + 
                "    <ftp_password>itu6fb6R</ftp_password>\r\n" + 
                "    <ftp_sourcedirectory>/</ftp_sourcedirectory>\r\n" + 
                "    <ftp_targetdirectory>D:\\cordys_workingdir\\allianz_dekra\\ftp_in</ftp_targetdirectory>\r\n" + 
                "    <ftp_host>195.145.184.115</ftp_host>\r\n" + 
                "    <aida_II_jobdirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB</aida_II_jobdirectory>\r\n" + 
                "    <aida_II_tiffdirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB\\AIDA_II_IN</aida_II_tiffdirectory>\r\n" + 
                "    <aida_II_resultdirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB\\AIDA_II_RESULT</aida_II_resultdirectory>\r\n" + 
                "    <aida_II_faildirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB\\AIDA_II_FAIL</aida_II_faildirectory>\r\n" + 
                "    <aida_II_TRACKINGID>20</aida_II_TRACKINGID>\r\n" + 
                "    <prokey_II_jobdirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB</prokey_II_jobdirectory>\r\n" + 
                "    <prokey_II_resultdirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB\\AIDA_II_RESULT</prokey_II_resultdirectory>\r\n" + 
                "    <prokey_II_faildirectory>Y:\\AIDA_CORDYS_WORKINGDIR\\ALLIANZ_DEKRA_JOB\\AIDA_II_FAIL</prokey_II_faildirectory>\r\n" + 
                "    <prokey_II_TRACKINGID>20</prokey_II_TRACKINGID>\r\n" + 
                " </DEKRAConfig>\r\n" + 
                "";
        String inputSoapXml = 
                "<ReadXmlFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <filename>{0}</filename>\r\n" + 
                "  <selectPath>{1}</selectPath>\r\n" + 
                "  <numrecords>-1</numrecords>\r\n" + 
                "  <offset>0</offset>\r\n" + 
                "  <validateonly>false</validateonly>\r\n" + 
                "  <returnAsText>false</returnAsText>\r\n" + 
                "</ReadXmlFileRecords>\r\n" + 
                "";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <data>{0}</data>" + 
                "    <endoffset>1</endoffset>\r\n" + 
                "    <recordsread>1</recordsread>\r\n" + 
                "    <errorcount>0</errorcount>\r\n" + 
                "</response>";
        String[] xpaths = {
                "/*",
                "/DEKRAConfig",
                "//DEKRAConfig",
                "/*/ftp_user",
                "/DEKRAConfig/ftp_user",
                "//DEKRAConfig/ftp_user",
                "//DEKRAConfig//ftp_user",
                "//ftp_user",
                "//*//ftp_user",
                "//*/ftp_user",
                "/*//ftp_user",
          };
        String[] results = {
                inputFileContents,
                inputFileContents,
                inputFileContents,
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
                "<ftp_user>i1</ftp_user>",
        };
        File inputFile = createTextFile("input.xml", inputFileContents);
        ApplicationConfiguration cfg = createAppConfig();
        
        cfg.setUseSimpleXPath(false);
        
        for (int i = 0; i < xpaths.length; i++) {
            String requestXml = MessageFormat.format(inputSoapXml, inputFile.getAbsolutePath(), xpaths[i]);
            String responseXml = MessageFormat.format(outputSoapXml, results[i]); 
            ReadXmlFileRecordsMethod method = new ReadXmlFileRecordsMethod();
            ISoapRequestContext request = executeMethod(cfg, method, requestXml); 
        
            assertNodesEqual(parse(responseXml), request.getResponseRootNode(), true);
        }
    }
    
}

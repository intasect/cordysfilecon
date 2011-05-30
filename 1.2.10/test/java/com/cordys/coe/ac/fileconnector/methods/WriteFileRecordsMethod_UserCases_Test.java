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
 * Test cases for the WriteFileRecords method. These test cases come from real-life projects.
 *
 * @author mpoyhone
 */
public class WriteFileRecordsMethod_UserCases_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Case1() throws Exception
    {
        String configXml =
                "<configuration>" +
                "<filetype name=\"USER\">\r\n" + 
                "    <select path=\"CustomPAPItem\">\r\n" + 
                "        <output path=\"./OwnerID\" type=\"text\" width=\"10\" align=\"left\" default=\"\" />      \r\n" + 
                "        <output path=\"./PAPSectionID\" type=\"text\" width=\"40\" align=\"left\" />\r\n" + 
                "        <output path=\"./VOEInstanceID\" type=\"text\" width=\"40\" align=\"left\" default=\"\" />\r\n" + 
                "        <output path=\"./ID\"   type=\"text\" width=\"40\" align=\"left\" />\r\n" + 
                "        <output path=\"./SVPInstanceID\" type=\"text\" width=\"40\" align=\"left\" default=\"\" />            \r\n" + 
                "        <output type=\"text\">\r\n" + 
                "            <input fixed=\"\\r\\n\" />\r\n" + 
                "        </output>\r\n" + 
                "    </select>\r\n" + 
                "</filetype>" +
                "</configuration>";
        String inputSoapXml = 
                "<WriteFileRecords xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <filename>{0}</filename>\r\n" + 
                "    <filetype>USER</filetype>\r\n" + 
                "    <append>false</append>\r\n" + 
                "    <records>\r\n" +
                "       <CustomPAPItem>\r\n" + 
                "          <PAPID>'{A8DCAB53-0A01-1FE6-00BC-312FFD9EF40F}'</PAPID>\r\n" + 
                "          <OwnerID>studentxyz1937</OwnerID>\r\n" + 
                "          <PAPSectionStatus>3</PAPSectionStatus>\r\n" + 
                "          <PAPSectionName>Studieplan 1</PAPSectionName>\r\n" + 
                "          <Description null=\"true\"/>\r\n" + 
                "          <JudgmentInvitationActivityID null=\"true\"/>\r\n" + 
                "          <ProposedScore null=\"true\"/>\r\n" + 
                "          <JudgmentProposedResult null=\"true\"/>\r\n" + 
                "          <JudgmentProposedDate null=\"true\"/>\r\n" + 
                "          <JudgmentComment null=\"true\"/>\r\n" + 
                "          <JudgmentProposedID null=\"true\"/>\r\n" + 
                "          <Score null=\"true\"/>\r\n" + 
                "          <JudgmentResult null=\"true\"/>\r\n" + 
                "          <JudgmentDate null=\"true\"/>\r\n" + 
                "          <JudgmentID null=\"true\"/>\r\n" + 
                "          <OptionName null=\"true\"/>\r\n" + 
                "          <JudgmentInvitationSent null=\"true\"/>\r\n" + 
                "          <ID>'{A8DCAD56-0A01-1FE6-00BC-312F9EC13A74}'</ID>\r\n" + 
                "          <PAPSectionID>'{A8DCAB43-0A01-1FE6-00BC-312F8AC43DB1}'</PAPSectionID>\r\n" + 
                "          <DataTemplateInstanceRevisionID>'{2820F7C1-912C-1073-00BF-1D3BBF9F5C8D}'</DataTemplateInstanceRevisionID>\r\n" + 
                "          <ParentDataTemplateInstanceRevisionID>'{C1B62EA6-912C-F049-01CA-1A686F8B317C}'</ParentDataTemplateInstanceRevisionID>\r\n" + 
                "          <Status>2</Status>\r\n" + 
                "          <Name>BT07-Wiskunde/Mechanica</Name>\r\n" + 
                "          <NumberOfPAPTasksNotJudged>2</NumberOfPAPTasksNotJudged>\r\n" + 
                "          <NumberOfPAPTasksJudgmentInProcess>0</NumberOfPAPTasksJudgmentInProcess>\r\n" + 
                "          <NumberOfPAPTasksJudgmentPositive>0</NumberOfPAPTasksJudgmentPositive>\r\n" + 
                "          <NumberOfPAPTasksJudgmentNegative>0</NumberOfPAPTasksJudgmentNegative>\r\n" + 
                "          <NumberOfPAPTasksInactive>0</NumberOfPAPTasksInactive>\r\n" + 
                "          <JudgmentInvitationFullName/>\r\n" + 
                "          <JudgmentUserFullName/>\r\n" + 
                "          <VOEInstanceID>'{8E5E0F1F-912C-F049-00F9-CBE54878E038}'</VOEInstanceID>\r\n" + 
                "          <SVPInstanceID>'{C1B62EA6-912C-F049-01CA-1A68DB857A95}'</SVPInstanceID>\r\n" + 
                "        </CustomPAPItem>\r\n" + 
                "    </records>\r\n" + 
                "</WriteFileRecords>";
        String outputFileContents = 
                "studentxyz{A8DCAB43-0A01-1FE6-00BC-312F8AC43DB1}  {8E5E0F1F-912C-F049-00F9-CBE54878E038}  {A8DCAD56-0A01-1FE6-00BC-312F9EC13A74}  {C1B62EA6-912C-F049-01CA-1A68DB857A95}  \r\n"; 
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "    <endoffset>172</endoffset>\r\n" + 
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

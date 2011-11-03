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
import com.cordys.coe.util.test.junit.FileTestUtils;

/**
 * Test cases for the CopyFile method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class CopyFileMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for CopyFile method.
     * @throws Exception Thrown
     */
    public void testProcess() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents =
                "testfile contents\u0000\uFFFFwith binary data";
        String inputSoapXml = 
                "<CopyFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <oldFileName>{0}</oldFileName>\r\n" + 
                "  <newFileName>{1}</newFileName>\r\n" + 
                "</CopyFile>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />"; 
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        File destFile = new File(tempFolder, "dest.txt");
        CopyFileMethod method = new CopyFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath(),
                                                                                 destFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertTrue("Source file does not exist.", srcFile.exists());
        assertTrue("Destination file does not exist.", destFile.exists());
        FileTestUtils.assertFilesEqual(srcFile, destFile);
    }
}

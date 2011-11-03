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
 * Test cases for the DeleteFile method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class DeleteFileMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for DeleteFile method.
     * @throws Exception Thrown.
     */
    public void testProcess() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents = "testfile contents";
        String inputSoapXml = 
                "<DeleteFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <fileName>{0}</fileName>\r\n" + 
                "</DeleteFile>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />"; 
        File srcFile = createTextFile("src.txt", inputFileContents, encoding);
        DeleteFileMethod method = new DeleteFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 srcFile.getAbsolutePath()));
        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertFalse("File still exists.", srcFile.exists());
    }
}

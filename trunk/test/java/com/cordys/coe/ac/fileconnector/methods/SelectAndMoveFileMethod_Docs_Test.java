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
 * Test cases for the SelectAndMoveFile method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class SelectAndMoveFileMethod_Docs_Test extends FileConnectorTestCase
{
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Oldest() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents1 = "testfile contents 1";
        String inputFileContents2 = "testfile contents 2";
        String inputSoapXml = 
                "<SelectAndMoveFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <srcdir>{0}</srcdir>\r\n" + 
                "  <destdir>{1}</destdir>\r\n" + 
                "  <type>oldest</type>\r\n" + 
                "</SelectAndMoveFile>";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "   <tuple>\r\n" + 
                "       <filename>{0}</filename>\r\n" + 
                "       <filepath>{1}</filepath>\r\n" + 
                "   </tuple>" +
                "</response>";
        File srcFile1 = createTextFile("src1.txt", inputFileContents1, encoding);
        Thread.sleep(300L);
        File srcFile2 = createTextFile("src2.txt", inputFileContents2, encoding);
        File destFolder = new File(tempFolder, "dest");
        File destFile = new File(destFolder, srcFile1.getName());
        
        destFolder.mkdirs();
        
        SelectAndMoveFileMethod method = new SelectAndMoveFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 tempFolder.getAbsolutePath(),
                                                                                 destFolder.getAbsolutePath()));
        
        assertNodesEqual(parse(MessageFormat.format(outputSoapXml, 
                                                    destFile.getName(),
                                                    destFile.getAbsolutePath())), 
                         request.getResponseRootNode());
        assertFalse("Source file 1 still exists.", srcFile1.exists());
        assertTrue("Source file 2 does not exist.", srcFile2.exists());
        assertTrue("Destination file does not exist.", destFile.exists());
        assertEquals("File contents are different.", inputFileContents1, readTextFile(destFile, encoding));
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.methods.ReadXMLFileRecordsMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)}.
     */
    public void testProcess_Newest() throws Exception
    {
        String encoding = "UTF-8";
        String inputFileContents1 = "testfile contents 1";
        String inputFileContents2 = "testfile contents 2";
        String inputSoapXml = 
                "<SelectAndMoveFile xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <srcdir>{0}</srcdir>\r\n" + 
                "  <destdir>{1}</destdir>\r\n" + 
                "  <type>newest</type>\r\n" + 
                "</SelectAndMoveFile>";
        String outputSoapXml = 
                "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "   <tuple>\r\n" + 
                "       <filename>{0}</filename>\r\n" + 
                "       <filepath>{1}</filepath>\r\n" + 
                "   </tuple>" +
                "</response>";
        File srcFile1 = createTextFile("src1.txt", inputFileContents1, encoding);
        Thread.sleep(300L);
        File srcFile2 = createTextFile("src2.txt", inputFileContents2, encoding);
        File destFolder = new File(tempFolder, "dest");
        File destFile = new File(destFolder, srcFile2.getName());
        
        destFolder.mkdirs();
        
        SelectAndMoveFileMethod method = new SelectAndMoveFileMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
                                                                                 tempFolder.getAbsolutePath(),
                                                                                 destFolder.getAbsolutePath()));
        
        assertNodesEqual(parse(MessageFormat.format(outputSoapXml, 
                                                    destFile.getName(),
                                                    destFile.getAbsolutePath())), 
                         request.getResponseRootNode());
        assertTrue("Source file 1 does not exist.", srcFile1.exists());
        assertFalse("Source file 2 still exists.", srcFile2.exists());
        assertTrue("Destination file does not exist.", destFile.exists());
        assertEquals("File contents are different.", inputFileContents2, readTextFile(destFile, encoding));
    }
}

package com.cordys.coe.ac.fileconnector.methods;

import java.io.File;
import java.text.MessageFormat;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;

public class CreateDirectoryMethodTest extends FileConnectorTestCase
{
	
    /**
     * Test method for CreateDirectory method.
     * @throws Exception Thrown
     */
    public void testCreateDirectoryProcess() throws Exception
    {
        String inputSoapXml = 
                "<CreateDirectory xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <newDirectoryName>{0}</newDirectoryName>\r\n" + 
                "  <parentDirectoryPath>{1}</parentDirectoryPath>\r\n" + 
                "</CreateDirectory>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />"; 
        
        String newDirName = "subdir";
        File parentDir = tempFolder;
        CreateDirectoryMethod method = new CreateDirectoryMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
        																		 newDirName,
        																		 parentDir.getAbsolutePath()));
        
        File newAbsDir = GeneralUtils.getAbsoluteFile(newDirName,parentDir);        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertTrue("New Destination folder does not exist.", newAbsDir.exists());       
    }
    
    /**
     * Test method for CreateDirectory method.
     * @throws Exception Thrown
     */
    public void testCreateDirectoryWithMultipleDirProcess() throws Exception
    {
        String inputSoapXml = 
                "<CreateDirectory xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
                "  <newDirectoryName>{0}</newDirectoryName>\r\n" + 
                "  <parentDirectoryPath>{1}</parentDirectoryPath>\r\n" + 
                "</CreateDirectory>";
        String outputSoapXml = 
                "<response  xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\" />"; 
        
        String newDirName = "subdir1/subdir2";
        File parentDir = tempFolder;
        CreateDirectoryMethod method = new CreateDirectoryMethod();
        ISoapRequestContext request = executeMethod(method, MessageFormat.format(inputSoapXml, 
        																		 newDirName,
        																		 parentDir.getAbsolutePath()));
        
        File newAbsDir = GeneralUtils.getAbsoluteFile(newDirName,parentDir);        
        assertNodesEqual(parse(outputSoapXml), request.getResponseRootNode());
        assertTrue("New Destination folder does not exist.", newAbsDir.exists());       
    }

}
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
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.cordys.coe.util.xml.nom.XmlUtils;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;

/**
 * Test cases for the GetListOfFiles method. These test the examples from documentation.
 *
 * @author mpoyhone
 */
public class GetListOfFilesMethod_Docs_Test extends FileConnectorTestCase
{
    private static String[][] fileContent = {
            { "test1.txt", "test file 1" },
            { "test2.bin", "binrary test file\u0000\u0001...." },
            { "file-with-&-character.txt", "test file with & in it." },
            { "file name with spaces.txt", "test file with spaces in the name." },
    };
    
    private static String[] folders = {
        "subfolder", "subfolder/other", "other-subfolder" 
    };
    
    /**
     * Test method for GetListOfFiles method.
     * @throws Exception Thrown.
     */
    public void testProcess_Absolute() throws Exception
    {
        String outputSoapXmlTemplate = 
            "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
            "   <tuple>\r\n" + 
            "      <directory>{0}</directory>\r\n" + 
            "      <entries>\r\n" + 
            "        <file type=\"file\" modified=\"\">{1}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{2}</file>\r\n" +
            "        <file type=\"directory\" modified=\"\">{3}</file>\r\n" +
            "        <file type=\"directory\" modified=\"\">{4}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{5}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{6}</file>\r\n" +
            "      </entries>\r\n" +
            "   </tuple>\r\n" +
            "</response>";
        String outputSoapXml = MessageFormat.format(outputSoapXmlTemplate, 
                                                    tempFolder.getAbsolutePath(),
                                                    new File(tempFolder, "file name with spaces.txt").getAbsolutePath(),
                                                    new File(tempFolder, "file-with-&amp;-character.txt").getAbsolutePath(),
                                                    new File(tempFolder, "other-subfolder" ).getAbsolutePath(),
                                                    new File(tempFolder, "subfolder").getAbsolutePath(),
                                                    new File(tempFolder, "test1.txt").getAbsolutePath(),
                                                    new File(tempFolder, "test2.bin").getAbsolutePath());
        int response = execute("absolute", null);
        
        XPathHelper.setNodeValue(response, XPath.getXPathInstance("//file/@modified"), null, "", true);
        
        assertNodesEqual(parse(outputSoapXml), response, true);
        
    }
    
    /**
     * Test method for GetListOfFiles method.
     * @throws Exception Thrown.
     */
    public void testProcess_Relative() throws Exception
    {
        String outputSoapXmlTemplate = 
            "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
            "   <tuple>\r\n" + 
            "      <directory>{0}</directory>\r\n" + 
            "      <entries>\r\n" + 
            "        <file type=\"file\" modified=\"\">{1}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{2}</file>\r\n" +
            "        <file type=\"directory\" modified=\"\">{3}</file>\r\n" +
            "        <file type=\"directory\" modified=\"\">{4}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{5}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{6}</file>\r\n" +
            "      </entries>\r\n" +
            "   </tuple>\r\n" +
            "</response>";
        String outputSoapXml = MessageFormat.format(outputSoapXmlTemplate, 
                                                    tempFolder.getAbsolutePath(),
                                                    "file name with spaces.txt",
                                                    "file-with-&amp;-character.txt",
                                                    "other-subfolder",
                                                    "subfolder",
                                                    "test1.txt",
                                                    "test2.bin");
        int response = execute("relative", null);
        
        XPathHelper.setNodeValue(response, XPath.getXPathInstance("//file/@modified"), null, "", true);
        
        assertNodesEqual(parse(outputSoapXml), response, true);
        
    }
    
    
    /**
     * Test method for GetListOfFiles method.
     * @throws Exception Thrown.
     */
    public void testProcess_Filter() throws Exception
    {
        String outputSoapXmlTemplate = 
            "<response xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
            "   <tuple>\r\n" + 
            "      <directory>{0}</directory>\r\n" + 
            "      <entries>\r\n" + 
            "        <file type=\"file\" modified=\"\">{1}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{2}</file>\r\n" +
            "        <file type=\"file\" modified=\"\">{3}</file>\r\n" +
            "      </entries>\r\n" +
            "   </tuple>\r\n" +
            "</response>";
        String outputSoapXml = MessageFormat.format(outputSoapXmlTemplate, 
                                                    tempFolder.getAbsolutePath(),
                                                    "file name with spaces.txt",
                                                    "file-with-&amp;-character.txt",
                                                    "test1.txt");
        int response = execute("relative", ".*\\.txt");
        
        XPathHelper.setNodeValue(response, XPath.getXPathInstance("//file/@modified"), null, "", true);
        
        assertNodesEqual(parse(outputSoapXml), response, true);
        
    }
    
    /**
     * Executes the test.
     * 
     * @param resultpathtype Result path type.
     * @param filter File filter.
     * @return Execution response.
     * @throws Exception Thrown.
     */
    private int execute(String resultpathtype, String filter) throws Exception
    {
        for (String folder : folders)
        {
            File f = new File(tempFolder, folder);
            
            if (! f.mkdirs()) {
                fail("Unable to create folder: " + f);
            }
        }
        
        for (String[] fileDef : fileContent)
        {
            createTextFile(fileDef[0], fileDef[1], "UTF-8");
        }
        
        String inputSoapXmlTemplate = 
            "<GetListOfFiles xmlns=\"http://schemas.cordys.com/1.0/ac/FileConnector\">\r\n" + 
            "  <directory>{0}</directory>\r\n" + 
            "  <resultpathtype>{1}</resultpathtype>\r\n" + 
            "  {2}\r\n" + 
            "</GetListOfFiles>";
        String inputSoapXml = MessageFormat.format(inputSoapXmlTemplate,
                                                   tempFolder.getAbsolutePath(),
                                                   resultpathtype, 
                                                   filter != null ? "<filter>" + filter + "</filter>" : "");
        GetListOfFilesMethod method = new GetListOfFilesMethod();
        ISoapRequestContext request = executeMethod(method, inputSoapXml);     
        int response = request.getResponseRootNode();
        
        XmlUtils.sortNodes(response, XPath.getXPathInstance("//file"), XPath.getXPathInstance("."), null);
        
        // Remove the extra namespace declaration introduced by sorting.
        int[] nodes = XPath.getXPathInstance("//file").selectElementNodes(response);
        
        for (int n : nodes)
        {
            Node.removeAttribute(n, "xmlns");
        }
        
        return response;
    }
}
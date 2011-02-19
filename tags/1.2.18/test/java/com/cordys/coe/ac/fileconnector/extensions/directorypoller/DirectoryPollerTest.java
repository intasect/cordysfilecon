/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import java.io.File;
import java.text.MessageFormat;

import sun.misc.BASE64Encoder;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.ExtensionContextStub;
import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.StateLog_ProcessingFolder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateError;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

/**
 * Test cases for the DirectoryPoller extension. These are move high level tests.
 *
 * @author mpoyhone
 */
public class DirectoryPollerTest extends FileConnectorTestCase
{
    private File configFile;
    private File inputFolder;
    private File processFolder;
    private File appProcessFolder;
    private File errorFolder;
    private File triggerFile;
    
    /**
     * Tests the basic trigger operation where one file is picked up.  
     */
    public void testTrigger_Basic() throws Exception
    {
        String configFileXml =
            "<configuration xmlns:FCDP=\"http://schemas.cordys.com/coe/FileConnector/Poller/1.0\">" +
            "   <folder>" +
            "       <name>input</name>" +
            "       <location>{0}</location>" +
            "       <track-time>0.1</track-time>" +
            "       <trigger>" +  
            "           <method>MyMethod</method>" +
            "           <namespace>http://MyMethodNamespace</namespace>" +
            "           <organization>myorgdn</organization>" +
            "           <user>myuserdn</user>" +
            "           <parameters>" +
            "               <param1>value1</param1>" +
            "               <param2>value2</param2>" +
            "               <param3 FCDP:element-data=\"filepath\" />" +
            "               <param4 FCDP:element-data=\"content-base64\" />" +
            "               <nested>" +
            "                   <a/>" +
            "               </nested>" +
            "           </parameters>" + 
            "       </trigger>" +  
            "   </folder>" +
            "</configuration>";
    
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param1>value1</param1>" +
                "   <param2>value2</param2>" +
                "   <param3>{0}</param3>" +
                "   <param4><![CDATA[{1}]]></param4>" +
                "   <nested>" +
                "       <a/>" +
                "   </nested>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        
        int soapRequest = execute(configFileXml, triggerFileContents, null);
        String processingFile = Node.getDataElement(soapRequest, "param3", "");
        String tmp;
        
        tmp = processingFile.replaceFirst("(.*[\\\\/])process[\\\\/]INPUT-[A-Za-z0-9]+([\\\\/][^\\\\/]+)$", "$1input$2");
        Node.setDataElement(soapRequest, "param3", tmp);

        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, 
                                                    triggerFile.getAbsolutePath(), 
                                                    new BASE64Encoder().encode(triggerFileContents.getBytes()))),
                         soapRequest);
        
        assertFalse("Input file still exists.", triggerFile.exists());
        assertFalse("Processing file still exists.", new File(processingFile).exists());        
    } 
    
    /**
     * Tests the basic trigger operation where one file is picked up by the given filter.  
     */
    public void testTrigger_Basic_WithGlobFilter() throws Exception
    {
        String configFileXml =
            "<configuration xmlns:FCDP=\"http://schemas.cordys.com/coe/FileConnector/Poller/1.0\">" +
            "   <folder>" +
            "       <name>input</name>" +
            "       <location>{0}</location>" +
            "       <track-time>0.1</track-time>" +
            "       <filter>*.XML</filter>" +
            "       <trigger>" +  
            "           <method>MyMethod</method>" +
            "           <namespace>http://MyMethodNamespace</namespace>" +
            "           <organization>myorgdn</organization>" +
            "           <user>myuserdn</user>" +
            "           <parameters>" +
            "              <param FCDP:element-data=\"filepath\" />" +
            "           </parameters>" + 
            "       </trigger>" +  
            "   </folder>" +
            "</configuration>";
    
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param>{0}</param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        String triggerFileName = "input.xml";
        
        // Create a file with another extension.
        prepare();
        
        File otherFile = createTextFile(new File(inputFolder, "input.txt"), "another file", "UTF-8");
        
        assertTrue("Other files does not exist.", otherFile.exists());
        
        // Start the poller.
        int soapRequest = execute(configFileXml, triggerFileContents, triggerFileName, null);
        String processingFile = Node.getDataElement(soapRequest, "param", "");
        String tmp;
        
        tmp = processingFile.replaceFirst("(.*[\\\\/])process[\\\\/]INPUT-[A-Za-z0-9]+([\\\\/][^\\\\/]+)$", "$1input$2");
        Node.setDataElement(soapRequest, "param", tmp);

        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, 
                                                    triggerFile.getAbsolutePath(), 
                                                    triggerFileName,
                                                    new BASE64Encoder().encode(triggerFileContents.getBytes()))),
                         soapRequest);
        
        assertFalse("Input file still exists.", triggerFile.exists());
        assertFalse("Processing file still exists.", new File(processingFile).exists());
        assertTrue("Other files does not exist.", otherFile.exists());
    } 
    
    /**
     * Tests the trigger operation where one file is moved to the application processing folder
     * before executing the trigger..  
     */
    public void testTrigger_AppProcessingFolder() throws Exception
    {
        String configFileXml =
            "<configuration xmlns:FCDP=\"http://schemas.cordys.com/coe/FileConnector/Poller/1.0\">" +
            "   <folder>" +
            "       <name>input</name>" +
            "       <location>{0}</location>" +
            "       <track-time>0.1</track-time>" +
            "       <trigger>" +  
            "           <method>MyMethod</method>" +
            "           <namespace>http://MyMethodNamespace</namespace>" +
            "           <organization>myorgdn</organization>" +
            "           <user>myuserdn</user>" +
            "           <move-file>true</move-file>" +
            "           <parameters>" +
            "               <param1 FCDP:element-data=\"filepath\" />" +
            "           </parameters>" + 
            "       </trigger>" +  
            "   </folder>" +
            "</configuration>";
    
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param1>{0}</param1>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        
        appProcessFolder = new File(tempFolder, "app-process");
        
        int soapRequest = execute(configFileXml, triggerFileContents, null);
        String appProcessingFile = Node.getDataElement(soapRequest, "param1", "");
        String tmp;
        
        tmp = appProcessingFile.replaceFirst("(.*[\\\\/])app-process([\\\\/])INPUT-[A-Za-z0-9]+(\\.txt)$", "$1input$2input.txt");
        Node.setDataElement(soapRequest, "param1", tmp);

        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, 
                                                    triggerFile.getAbsolutePath(), 
                                                    new BASE64Encoder().encode(triggerFileContents.getBytes()))),
                         soapRequest);
        
        assertFalse("Input file still exists.", triggerFile.exists());
        assertTrue("Application processing file does not exists.", new File(appProcessingFile).exists());
    } 
    
    /**
     * Tests the trigger operation where the trigger returns a SOAP:Fault.  
     */
    public void testTrigger_Error() throws Exception
    {
        String configFileXml =
            "<configuration xmlns:FCDP=\"http://schemas.cordys.com/coe/FileConnector/Poller/1.0\">" +
            "   <folder>" +
            "       <name>input</name>" +
            "       <location>{0}</location>" +
            "       <track-time>0.1</track-time>" +
            "       <trigger>" +  
            "           <method>MyMethod</method>" +
            "           <namespace>http://MyMethodNamespace</namespace>" +
            "           <organization>myorgdn</organization>" +
            "           <user>myuserdn</user>" +
            "           <parameters>" +
            "               <param1>value1</param1>" +
            "           </parameters>" + 
            "       </trigger>" +  
            "   </folder>" +
            "</configuration>";
    
        String soapFaultXml = 
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
                "   <soap:Body>" +
                "       <soap:Fault xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
                "           <faultcode>Some.Problem</faultcode>\r\n" + 
                "           <faultstring>Some problem occurred.</faultstring>" + 
                "       </soap:Fault>" +
                "   </soap:Body>" +
                "</soap:Envelope>";
        String triggerFileContents = 
                "MyTestData";
        
        execute(configFileXml, triggerFileContents, soapFaultXml);

        File errorFile = findErrorFile();
        
        assertFalse("Input file still exists.", triggerFile.exists());
        assertNotNull("Error file not found.", errorFile);
        assertTrue("Error file does not exist.", errorFile.exists());
        assertEquals(triggerFileContents, readTextFile(errorFile));
        assertTrue("Processing folder is not empty.", processFolder.listFiles().length == 0);
    }
    
    public void testTrigger_Error_Resume() throws Exception
    {
        // First move the file into the error folder.
        testTrigger_Error();

        File errorFile = findErrorFile();
        
        assertNotNull("Error file not found.", errorFile);
        
        // Move file's error folder back to the processing folder and restart the poller.
        // This should resume the processing.
        File fileErrorFolder = errorFile.getParentFile();
        File fileProcFolder = new File(processFolder, fileErrorFolder.getName());
        
        fileErrorFolder.renameTo(fileProcFolder);
        triggerFile = new File(fileProcFolder, "input.txt");
        
        String configFileXml =
            "<configuration xmlns:FCDP=\"http://schemas.cordys.com/coe/FileConnector/Poller/1.0\">" +
            "   <folder>" +
            "       <name>input</name>" +
            "       <location>{0}</location>" +
            "       <track-time>0.1</track-time>" +
            "       <trigger>" +  
            "           <method>MyMethod</method>" +
            "           <can-retry>true</can-retry>" +
            "           <namespace>http://MyMethodNamespace</namespace>" +
            "           <organization>myorgdn</organization>" +
            "           <user>myuserdn</user>" +
            "           <parameters>" +
            "               <param1 FCDP:element-data=\"filepath\" />" +
            "           </parameters>" + 
            "       </trigger>" +  
            "   </folder>" +
            "</configuration>";
    
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param1>{0}</param1>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        
        int soapRequest = execute(configFileXml, null, null);
        
        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, 
                                                    triggerFile.getAbsolutePath(), 
                                                    new BASE64Encoder().encode(triggerFileContents.getBytes()))),
                         soapRequest);
        
        assertFalse("Input file still exists.", triggerFile.exists());
    }
    
    private File findErrorFile()
    {
        File[] folders = errorFolder.listFiles();
        
        if (folders == null || folders.length == 0) {
            return null;
        }
        
        File[] files = folders[0].listFiles();
        
        if (files == null || files.length == 0) {
            return null;
        }
        
        for (File file : files)
        {
            String name = file.getName();
           
            if (StateError.ERROR_INFO_FILE.equals(name) ||
                StateLog_ProcessingFolder.LOGFILE_NAME.equals(name)) {
                continue;
            }
            
            return file;
        }
        
        return null;
    }
    
    private void prepare()
    {
        configFile = new File(tempFolder, "config.xml");
        inputFolder = new File(tempFolder, "input");
        processFolder = new File(tempFolder, "process");
        errorFolder = new File(tempFolder, "error");
        
        inputFolder.mkdirs();
        processFolder.mkdirs();
        errorFolder.mkdirs();
    }
    
    private int execute(String configFileXml, String triggerFileContents, String soapResponseXml) throws Exception
    {
        return execute(configFileXml, triggerFileContents, null, soapResponseXml);
    }
    
    private int execute(String configFileXml, String triggerFileContents, String triggerFileName, String soapResponseXml) throws Exception
    {
        prepare();
        
        if (triggerFileName == null || triggerFileName.length() == 0)
        {
            triggerFileName = "input.txt";
        }
        
        int appConfig = createAppConfigXml(null, null, null);
        int pollerConfig = Node.createElement("component", Find.firstMatch(appConfig, "<>"));
        
        Node.setAttribute(pollerConfig, "name", "Directory Poller");
        Node.createTextElement("enabled", "true", pollerConfig);
        Node.createTextElement("configuration-file", configFile.getAbsolutePath(), pollerConfig);
        Node.createTextElement("processing-folder", processFolder.getAbsolutePath(), pollerConfig);
        Node.createTextElement("error-folder", errorFolder.getAbsolutePath(), pollerConfig);
        Node.createTextElement("poll-interval", "0.1", pollerConfig);
        
        if (appProcessFolder != null) {
            Node.createTextElement("app-processing-folder", appProcessFolder.getAbsolutePath(), pollerConfig);
        }

        FolderConfiguration config = 
            new FolderConfiguration(parse(MessageFormat.format(configFileXml, 
                                                               inputFolder.getAbsolutePath())),
                                    tempFolder,
                                    "myuserdn");
        ExtensionContextStub extStub = new ExtensionContextStub(dDoc);
        DirectoryPoller poller = new DirectoryPoller();
        
        try {
            poller.setConfiguration(config);
            if (! poller.initialize(extStub, new ApplicationConfiguration(appConfig))) {
                fail("Failed to initialize the poller.");
            }
            
            if (triggerFileContents != null) {
                triggerFile = createTextFile(new File(inputFolder, triggerFileName), triggerFileContents, "UTF-8");
            }

            int soapRequest;
            
            if (soapResponseXml == null) {
                soapRequest = extStub.getNomConnector().waitForRequest();
            } else {
                int methodNode = Node.getFirstChild(Node.getLastChild(parse(soapResponseXml)));
                soapRequest = extStub.getNomConnector().waitForRequest(methodNode);
            }
            
            addNomGarbage(Node.getRoot(soapRequest));

            return soapRequest;
        }
        finally {
            poller.cleanup();
            Thread.sleep(200L);
        }
    }
}

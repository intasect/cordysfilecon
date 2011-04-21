/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import java.io.File;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

import com.cordys.coe.ac.fileconnector.ExtensionContextStub;
import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Folder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter;
import com.eibus.xml.nom.Node;

/**
 * Test cases for the file state TRIGGER.
 *
 * @author mpoyhone
 */
public class StateTriggerTest extends FileConnectorTestCase
{
    private File inputFolder;
    private File processingFolder;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        inputFolder = new File(tempFolder, "input");
        inputFolder.mkdirs();
        processingFolder = new File(tempFolder, "processing");
        processingFolder.mkdirs();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#execute()}.
     */
    public void testExecute_FilePath() throws Exception
    {
        String paramsXml =
            "<parameters>" +
            "   <param FCDP:element-data=\"filepath\" />" +
            "</parameters>"; 
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param>{0}</param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        
        File triggerFile = createTextFile(new File(inputFolder, "input.txt"), triggerFileContents, "UTF-8");
        int soapResponse = execute(paramsXml, triggerFile);
            
        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, triggerFile.getAbsolutePath())),
                         soapResponse);
        setSuccess(true);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#execute()}.
     */
    public void testExecute_FileName() throws Exception
    {
        String paramsXml =
            "<parameters>" +
            "   <param FCDP:element-data=\"filename\" />" +
            "</parameters>"; 
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param>{0}</param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        
        File triggerFile = createTextFile(new File(inputFolder, "XXX_YYYY.txt"), triggerFileContents, "UTF-8");
        int soapResponse = execute(paramsXml, triggerFile);
            
        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, triggerFile.getName())),
                         soapResponse);
        setSuccess(true);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#execute()}.
     */
    public void testExecute_FileSize() throws Exception
    {
        String paramsXml =
            "<parameters>" +
            "   <param FCDP:element-data=\"filesize\" />" +
            "</parameters>"; 
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param>{0}</param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "MyTestData";
        
        File triggerFile = createTextFile(new File(inputFolder, "input.txt"), triggerFileContents, "UTF-8");
        int soapResponse = execute(paramsXml, triggerFile);
            
        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, triggerFile.length())),
                         soapResponse);
        setSuccess(true);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#execute()}.
     */
    public void testExecute_ContentText() throws Exception
    {
        String paramsXml =
            "<parameters>" +
            "   <param FCDP:element-data=\"content-text\" />" +
            "</parameters>"; 
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param><![CDATA[{0}]]></param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "0123456789_ƒ‰÷ˆ≈Â";
        
        File triggerFile = createTextFile(new File(inputFolder, "input.txt"), triggerFileContents, "UTF-8");
        int soapResponse = execute(paramsXml, triggerFile);
            
        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, triggerFileContents)),
                         soapResponse);
        setSuccess(true);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#execute()}.
     */
    public void testExecute_ContentBase64() throws Exception
    {
        String paramsXml =
            "<parameters>" +
            "   <param FCDP:element-data=\"content-base64\" />" +
            "</parameters>"; 
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param><![CDATA[MDEyMzQ1Njc4OV/DhMOkw5bDtsOFw6U=]]></param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "0123456789_ƒ‰÷ˆ≈Â";
        
        File triggerFile = createTextFile(new File(inputFolder, "input.txt"), triggerFileContents, "UTF-8");
        int soapResponse = execute(paramsXml, triggerFile);
            
        assertNodesEqual(parse(triggerSoapXml),
                         soapResponse);
        setSuccess(true);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#execute()}.
     */
    public void testExecute_ContentXml() throws Exception
    {
        String paramsXml =
            "<parameters>" +
            "   <param FCDP:element-data=\"content-xml\" />" +
            "</parameters>"; 
        String triggerSoapXml = 
                "<MyMethod xmlns=\"http://MyMethodNamespace\">\r\n" + 
                "   <param><myxml><test>123</test></myxml></param>" +
                "</MyMethod>";
        String triggerFileContents = 
                "<myxml><test>123</test></myxml>";
        
        File triggerFile = createTextFile(new File(inputFolder, "input.txt"), triggerFileContents, "UTF-8");
        int soapResponse = execute(paramsXml, triggerFile);
            
        assertNodesEqual(parse(MessageFormat.format(triggerSoapXml, triggerFileContents)),
                         soapResponse);
        setSuccess(true);
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#isFinished()}.
     */
    public void testIsFinished()
    {
        assertFalse(new StateTrigger(null, null).isFinished());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#resume()}.
     */
    public void testResume() throws Exception
    {
        // TODO
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateTrigger#getStateType()}.
     */
    public void testGetEnumValue()
    {
        assertEquals(EFileState.TRIGGER, new StateTrigger(null, null).getStateType());
    }

    private int execute(String paramsXml, final File inputFile) throws Exception
    {
        File inputFolder = inputFile.getParentFile();
        String configXmlTemplate =
            "<folder xmlns:FCDP=\"http://schemas.cordys.com/coe/FileConnector/Poller/1.0\">" +
            "    <name>input</name>" +
            "    <location>{0}</location>" +
            "    <track-time>0.1</track-time>" +
            "    <trigger>" +  
            "        <method>MyMethod</method>" +
            "        <namespace>http://MyMethodNamespace</namespace>" +
            "        <organization>myorgdn</organization>" +
            "        <user>myuserdn</user>" +
            paramsXml +
            "    </trigger>" +  
            "</folder>";        
        String configXml = MessageFormat.format(configXmlTemplate, inputFolder.getAbsolutePath());
        Folder folder = new Folder(parse(configXml), tempFolder, "myuserdn");
        ExtensionContextStub extStub = new ExtensionContextStub(dDoc);
        final AtomicReference<Exception> threadExpection = new AtomicReference<Exception>();
        final FileContext fileContext = new FileContext(inputFile, 
                                                  folder,
                                                  extStub.getNomConnector(),
                                                  null,
                                                  null,
                                                  null);
        final IFileState state = new StateTrigger(null, fileContext);
        
        fileContext.setOriginalFile(inputFile);
        fileContext.setProcessingFolder(processingFolder);
        
        Thread worker = new Thread() {
            /**
             * @see java.lang.Thread#run()
             */
            @Override
            public void run()
            {
                try
                {
                    if (! state.execute()) {
                        threadExpection.set(new FileException("Trigger state returned false."));
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    threadExpection.set(e);
                }
                finally {
                    try
                    {
                        IStateLogWriter writer = fileContext.getLogWriter();
                        
                        if (writer != null) {
                            writer.close();
                        }
                    }
                    catch (Exception ignored)
                    {
                    }
                }
            }
            
        };
        worker.start();
        
        int soapRequest;
        
        try {
            soapRequest = extStub.getNomConnector().waitForRequest();
            addNomGarbage(Node.getRoot(soapRequest));
            worker.join(20000L);

            if (threadExpection.get() != null) {
                throw threadExpection.get();
            }          
        }
        finally {
            folder.cleanup();
        }
        
        return soapRequest;
    }
}

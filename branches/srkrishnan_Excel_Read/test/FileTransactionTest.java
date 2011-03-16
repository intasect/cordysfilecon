/**
 * (c) 2006 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import static com.cordys.coe.actester.utils.AssertUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

import com.cordys.coe.actester.AcConfig;
import com.cordys.coe.actester.SoapRequestInstance;
import com.cordys.coe.util.FileUtils;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.SOAPTransaction;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * TODO Describe the class.
 *
 * @author mpoyhone
 */
public class FileTransactionTest extends TestCase
{
    private static Document dDoc = new Document();
    private AcConfig<FileConnector> testConfig;
    
    protected void setUp() throws Exception {
        // Needed so that we don't have to copy the 
        // message bundle file to C:\Cordys\localization.
        EIBProperties.setInstallDir("src/content");
        
        String sConfig =
            "<test-configuration>\r\n" + 
            "   <soapprocessor name=\"SP@JMS Connector\" description=\"\" computer=\"grnap062\" osprocesshost=\"\" automaticstart=\"true\">\r\n" + 
            "     <soapprocessorconfiguration>\r\n" + 
            "       <configurations autoStartCount=\"3\">\r\n" + 
            "          <configuration implementation=\"com.cordys.coe.ac.fileconnector.FileConnector\">\r\n" + 
            "          </configuration>\r\n" + 
            "       </configurations>\r\n" + 
            "     </soapprocessorconfiguration>\r\n" + 
            "   </soapprocessor>\r\n" + 
            "</test-configuration>";
        testConfig = new AcConfig<FileConnector>();
        testConfig.readConfig(dDoc.parseString(sConfig), FileConnector.class);
        
        //PropertyConfigurator.configure("test/lib/log4j.properties");
        
        testConfig.soapProc.open();
    }
    
    protected void tearDown() throws Exception {
        if (testConfig != null) {
            testConfig.soapProc.close();
        }
    }
    
    public void testMoveFile() throws Exception {
        File fBaseDir = new File("build/test/methods/moveFile");
        
        if (! prepareFolder(fBaseDir)) {
            fail("Unable to create the test folder.");
        }
        
        String sExpectedData = "TEST DATA.";
        File fSourceFile = new File(fBaseDir, "source.txt");
        File fDestFile = new File(fBaseDir, "dest.txt");
        SoapRequestInstance soapReq = createSoapRequest("MoveFile");
        int xSoapReqNode = soapReq.getRequest().getXMLNode();
        
        Node.createTextElement("oldFileName", fSourceFile.toString(), xSoapReqNode);
        Node.createTextElement("newFileName", fDestFile.toString(), xSoapReqNode);
        
        try {
            if (! writeFile(fSourceFile, sExpectedData)) {
                fail("Unable to create the source file.");
            }
    
            Document.setLeakInfoBaseline();
            SOAPTransaction spTxn = soapReq.createSoapTransaction();
            ApplicationTransaction appTxn = testConfig.appConn.createTransaction(spTxn);
            
            appTxn.process(soapReq.getRequest(), soapReq.getResponse());
            assertNoSoapFault(soapReq.getResponse());
            
            assertFalse("Source file still exists.", fSourceFile.exists());
            assertTrue("Destination file does not exists.", fDestFile.exists());
            
            String sTestData = readTextFile(fDestFile); 
            
            assertNotNull(sTestData, "Unable to read the destination file.");
            assertEquals(sExpectedData, sTestData);
            
            assertNoNomLeaks();
        } 
        finally {
            soapReq.clear();
            fSourceFile.delete();
            fDestFile.delete();
        }
    }
    
    public void testMoveFileDestExists() throws Exception {
        File fBaseDir = new File("build/test/methods/moveFile");
        
        if (! prepareFolder(fBaseDir)) {
            fail("Unable to create the test folder.");
        }
        
        String sExpectedData = "TEST DATA.";
        File fSourceFile = new File(fBaseDir, "source.txt");
        File fDestFile = new File(fBaseDir, "dest.txt");
        SoapRequestInstance soapReq = createSoapRequest("MoveFile");
        int xSoapReqNode = soapReq.getRequest().getXMLNode();
        
        Node.createTextElement("oldFileName", fSourceFile.toString(), xSoapReqNode);
        Node.createTextElement("newFileName", fDestFile.toString(), xSoapReqNode);
        
        try {
            if (! writeFile(fSourceFile, sExpectedData)) {
                fail("Unable to create the source file.");
            }
            
            if (! writeFile(fDestFile, sExpectedData)) {
                fail("Unable to create the destination file.");
            }
    
            Document.setLeakInfoBaseline();
            SOAPTransaction spTxn = soapReq.createSoapTransaction();
            ApplicationTransaction appTxn = testConfig.appConn.createTransaction(spTxn);
            
            appTxn.process(soapReq.getRequest(), soapReq.getResponse());
            assertNoNomLeaks();

            assertSoapFault(soapReq.getResponse(), ".*file exists.*");
        } 
        finally {
            soapReq.clear();
            fSourceFile.delete();
            fDestFile.delete();
        }
    }
    
    public void testMoveFileWithOverWriteDestExists() throws Exception {
        File fBaseDir = new File("build/test/methods/moveFile");
        
        if (! prepareFolder(fBaseDir)) {
            fail("Unable to create the test folder.");
        }
        
        String sExpectedData = "TEST DATA.";
        File fSourceFile = new File(fBaseDir, "source.txt");
        File fDestFile = new File(fBaseDir, "dest.txt");
        SoapRequestInstance soapReq = createSoapRequest("MoveFile");
        int xSoapReqNode = soapReq.getRequest().getXMLNode();
        
        Node.createTextElement("oldFileName", fSourceFile.toString(), xSoapReqNode);
        Node.createTextElement("newFileName", fDestFile.toString(), xSoapReqNode);
        Node.createTextElement("overwriteExisting", "true", xSoapReqNode);
        
        try {
            if (! writeFile(fSourceFile, sExpectedData)) {
                fail("Unable to create the source file.");
            }
            
            if (! writeFile(fDestFile, sExpectedData)) {
                fail("Unable to create the destination file.");
            }
    
            Document.setLeakInfoBaseline();
            SOAPTransaction spTxn = soapReq.createSoapTransaction();
            ApplicationTransaction appTxn = testConfig.appConn.createTransaction(spTxn);
            
            appTxn.process(soapReq.getRequest(), soapReq.getResponse());
            assertNoSoapFault(soapReq.getResponse());
            
            assertFalse("Source file still exists.", fSourceFile.exists());
            assertTrue("Destination file does not exists.", fDestFile.exists());
            
            String sTestData = readTextFile(fDestFile); 
            
            assertNotNull(sTestData, "Unable to read the destination file.");
            assertEquals(sExpectedData, sTestData);
            
            assertNoNomLeaks();
        } 
        finally {
            soapReq.clear();
            fSourceFile.delete();
            fDestFile.delete();
        }
    }
    
    private static boolean writeFile(File fFile, String sData) {
        return writeFile(fFile, sData.getBytes());
    }
    
    private static boolean writeFile(File fFile, byte[] baData) {
        OutputStream osOutput = null;
        
        try {
            osOutput = new FileOutputStream(fFile);
            osOutput.write(baData);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try
            {
                if (osOutput != null) {
                    osOutput.close();
                }
            }
            catch (IOException ignored)
            {
            }
        }
        
        return true;
    }
    
    private static String readTextFile(File fFile) {
        return new String(readFile(fFile));
    }
    
    private static byte[] readFile(File fFile) {
        InputStream isInput = null;
        
        try {
            isInput = new FileInputStream(fFile);
            return FileUtils.readStreamContents(isInput);
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            try
            {
                if (isInput != null) {
                    isInput.close();
                }
            }
            catch (IOException ignored)
            {
            }
        }
    }
    
    private boolean prepareFolder(File fDir) {
        if (! fDir.exists() && ! fDir.mkdirs()) {
            return false;
        }
        
        return deleteRecursively(fDir.listFiles());
    }

    private boolean deleteRecursively(File[] faFiles)
    {
        for (File fFile : faFiles)
        {
            if (fFile.isDirectory()) {
                if (! deleteRecursively(fFile.listFiles())) {
                    return false;
                }
            }
            
            if (! fFile.delete()) {
                return false;
            }
        }
        
        return true;
    }
    
    private SoapRequestInstance createSoapRequest(String sAction) throws Exception {
        String sRequest =
            "<soap-request id=\"template\">\r\n" + 
            "  <implementation type=\"FILECONNECTOR\">\r\n" +
            "    <action>" + sAction + "</action>\r\n" +
            "  </implementation>\r\n" +
            "  <request-xml>\r\n" + 
            "    <SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">\r\n" + 
            "      <SOAP:Body>\r\n" +
            "        <Request xmlns=\"\">\r\n" +
            "        </Request>\r\n" +
            "      </SOAP:Body>\r\n" + 
            "    </SOAP:Envelope>\r\n" + 
            "  </request-xml>\r\n" + 
            "</soap-request>";
        SoapRequestInstance soapReq = new SoapRequestInstance(dDoc.parseString(sRequest));
                
        return soapReq;
    }
}

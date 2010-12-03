/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.cordys.coe.ac.fileconnector.charset.ascii.AsciiCharsetProvider;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.test.junit.FileTestUtils;
import com.cordys.coe.util.test.junit.NomTestCase;
import com.eibus.util.logger.config.LoggerConfigurator;
import com.eibus.xml.nom.Node;

/**
 * Base class for FileConnector test cases.
 *
 * @author mpoyhone
 */
public abstract class FileConnectorTestCase extends NomTestCase
{
    protected File tempFolder;
    protected boolean cleanTempFolder = true;
    private static final boolean remoteProcessDebugging = false;
    
    /**
     * Contains file where the current logger configuration was loaded.
     */
    protected static File loggerConfigFile;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        String className = getClass().getName().replaceFirst(".*\\.([^.]+)$", "$1");
        
        tempFolder = new File("./build/test/" + className);

        if (cleanTempFolder) {
            try
            {
                FileTestUtils.initializeFolder(tempFolder);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                fail(e.getMessage());
            }        
        }
        
        initLogging("Log4jConfiguration.xml", FileConnectorTestCase.class);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    /**
     * Same as com.cordys.coe.ac.fileconnector.FileConnectorTestCase.initLogging(File), but loads 
     * the configuration from a resource.
     * 
     * @param logFilePath Log file path relative to the given class.
     * @param refClass Reference class.
     */
    public static void initLogging(String logFilePath, Class<?> refClass)
    {
        File file = FileUtils.getResourceFile(logFilePath, refClass);
        
        initLogging(file);
    }
    
    /**
     * Initializes the Cordys logging from the given configuration file.
     * If the logging is already initialized from a file with the same
     * name, nothing is done.
     * @param logFile Logger configuration file.
     */
    public static void initLogging(File logFile)
    {
        if (loggerConfigFile != null && loggerConfigFile.equals(logFile)) {
            return;
        }
        
        LoggerConfigurator.initLogger(logFile.getAbsolutePath());
        loggerConfigFile = logFile;
    }
    
    public File createTextFile(String name, String contents) throws IOException {
        return createTextFile(name, contents, "UTF-8");
    }
    
    public File createTextFile(String name, String contents, String encoding) throws IOException {
        return createTextFile(new File(tempFolder, name), contents, encoding);
    }
    
    public File createTextFile(File f, String contents) throws IOException {
        return createTextFile(f, contents, "UTF-8");
    }
    
    public File createTextFile(File f, String contents, String encoding) throws IOException {
        OutputStream os = null;
        
        try {
            os = new FileOutputStream(f);
            os.write(contents.getBytes(encoding));
        }
        finally {
            FileUtils.closeStream(os);
        }
        
        return f;
    }
    
    public String readTextFile(File file) throws IOException {
        return readTextFile(file, "UTF-8");
    }
    
    public String readTextFile(File file, String encoding) throws IOException {
        InputStream is = null;
        
        try {
            is = new FileInputStream(file);
            return FileUtils.readTextStreamContents(is, encoding);
        }
        finally {
            FileUtils.closeStream(is);
        }
    }
    
    public ISoapRequestContext createSoapRequest(String requestXml)
    {
        int requestNode = parse(requestXml);
        int responseNode = dDoc.createElement("response");
        
        Node.setAttribute(responseNode, "xmlns", Node.getNamespaceURI(requestNode));
        
        addNomGarbage(responseNode);
        
        return new SoapRequestStub(requestNode, responseNode);
    }
    
    public int createAppConfigXml(String readerCharset, String writerCharset, String[][] extraParams)
    {
        if (readerCharset == null || readerCharset.length() == 0)
        {
            readerCharset = "ISO-8859-1";
        }

        if (writerCharset == null || writerCharset.length() == 0)
        {
            writerCharset = "ISO-8859-1";
        }

        int node = parse("<configuration>" +
                         "   <Configuration>" +
                         "      <readercharset>" + readerCharset + "</readercharset>" +
                         "      <writercharset>" + writerCharset + "</writercharset>" +
                         "   </Configuration>" +
                         "</configuration>");
        
        if (extraParams != null) {
            for (String[] param : extraParams)
            {
                Node.createTextElement(param[0], param[1], node);
            }
        }
        
        return node;
    }
    
    public ApplicationConfiguration createAppConfig() throws ConfigException
    {
        return createAppConfig(null, null, null);
    }
    
    public ApplicationConfiguration createAppConfig(String[][] extraParams) throws ConfigException
    {
        return createAppConfig(null, null, extraParams);
    }
    
    public ApplicationConfiguration createAppConfig(String readerCharset, String writerCharset, String[][] extraParams) throws ConfigException
    {
        if (readerCharset != null && writerCharset == null) {
            writerCharset = readerCharset;
        }
        
        ApplicationConfiguration acConfig = new ApplicationConfiguration(createAppConfigXml(readerCharset, writerCharset, extraParams));
        
        // Load the custom character set provider.
        AsciiCharsetProvider cpCustomProvider = new AsciiCharsetProvider(AsciiCharsetProvider.PROPERTY_FILE_NAME);
        
        acConfig.setCustomCharsetProvider(cpCustomProvider);
        
        return acConfig;
    }    
    
    public ISoapRequestContext executeMethod(IFileConnectorMethod method, String requestXml) throws FileException, ConfigException {
        ISoapRequestContext req = createSoapRequest(requestXml);
        ApplicationConfiguration cfg = createAppConfig();
        
        method.initialize(cfg);
        try {
            method.process(req);
        }
        finally {
            method.cleanup();
        }
        
        return req;
    }
    
    public ISoapRequestContext executeMethod(ApplicationConfiguration cfg, IFileConnectorMethod method, String requestXml) throws FileException, ConfigException {
        ISoapRequestContext req = createSoapRequest(requestXml);
        
        method.initialize(cfg);
        try {
            method.process(req);
        }
        finally {
            method.cleanup();
        }
        
        return req;
    }
    
    public static Process executeJavaProcess(Class<?> c, String ... args) throws Exception
    {
        String javaHome = System.getProperty("java.home");
        
        if (javaHome == null || javaHome.length() == 0)
        {
            throw new IllegalStateException("Unable to determine the Java home.");
        }
        
        String classPath = System.getProperty("java.class.path");
        
        if (classPath == null || classPath.length() == 0)
        {
            throw new IllegalStateException("Unable to determine the Java classpath.");
        }
        
        List<String> argList = new ArrayList<String>(10);
        
        argList.add(javaHome + File.separator + "bin" + File.separator + "java");
        
        if (remoteProcessDebugging) {
            argList.add("-Xdebug");
            argList.add("-Xrunjdwp:transport=dt_socket,server=n,suspend=n,address=localhost:20001");
        }

        argList.add("-cp");
        argList.add(classPath);
        argList.add(c.getName());
        
        for (String a : args)
        {
            argList.add(a);
        }
        
        String[] argArr = (String[]) argList.toArray(new String[argList.size()]);
        
        return Runtime.getRuntime().exec(argArr);
    }
}

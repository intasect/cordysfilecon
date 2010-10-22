package com.cordys.coe.ac.fileconnector.methods;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;
import com.cordys.coe.ac.fileconnector.writer.RecordWriter;
import com.cordys.coe.ac.fileconnector.writer.WriterConfig;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xmlstore.XMLStoreWrapper;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import java.nio.charset.Charset;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements the WriteFileRecords SOAP methods.
 *
 * @author  mpoyhone
 */
public class WriteFileRecordsMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "WriteFileRecords";
    /**
     * Filename request parameter for ReadFileRecords, ReadFile, WriteFileRecords and WriteFile
     * methods.
     */
    private static final String PARAM_FILENAME = "filename";
    /**
     * File type request parameter for ReadFileRecords.
     */
    private static final String PARAM_FILETYPE = "filetype";
    /**
     * The append boolean parameter for WriteFileRecords.
     */
    private static final String PARAM_APPEND = "append";
    /**
     * Contains the FileConnector configuration.
     */
    private ApplicationConfiguration acConfig;
    /**
     * Contains the loaded method configuration.
     */
    private volatile WriterConfig configuration;
    /**
     * Configuration reload flag.
     */
    private boolean reloadConfiguration;

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#cleanup()
     */
    public void cleanup()
                 throws ConfigException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#initialize(com.cordys.coe.ac.fileconnector.ApplicationConfiguration)
     */
    public boolean initialize(ApplicationConfiguration acConfig)
                       throws ConfigException
    {
        this.acConfig = acConfig;

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#onReset()
     */
    public void onReset()
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)
     */
    public EResult process(ISoapRequestContext req)
                    throws FileException
    {
        int requestNode = req.getRequestRootNode();

        // Get the configuration parameters.
        reloadConfiguration = acConfig.isConfigurationReloadEnabled();

        // Get the needed parameters from the SOAP request
        String sFileName = XmlUtils.getStringParameter(requestNode, PARAM_FILENAME, true);
        String sFileType = XmlUtils.getStringParameter(requestNode, PARAM_FILETYPE, true);
        boolean bAppend = XmlUtils.getBooleanParameter(requestNode, PARAM_APPEND);

        // Create File objects for the destination file
        File fFile = new File(sFileName);

        if (!acConfig.isFileAllowed(fFile))
        {
            throw new FileException("File access is not allowed.");
        }

        // Get the input XML nodes.
        int[] iaRecords = Find.match(requestNode, "<><records><>");

        // Find out the character set to be used.
        String sCharsetName = acConfig.getWriterCharacterSet();
        Charset cCharset = GeneralUtils.findCharacterSet(sCharsetName, acConfig);

        // Fetch the writer configuration object.
        WriterConfig wcConfig = getConfiguration(req);

        // Create the method object
        int iResultNode = 0;
        List<Exception> lErrorList = new LinkedList<Exception>();
        long lEndFileOffset = 0;

        // Call the method
        try
        {
            FileOutputStream os;
            RecordWriter rwRecordWriter = new RecordWriter(wcConfig);
            Writer wWriter = null;

            // Open the file to be written
            try
            {
                os = new FileOutputStream(fFile, bAppend);
                wWriter = new BufferedWriter(new OutputStreamWriter(os, cCharset));
            }
            catch (IOException e)
            {
                throw new FileException("Unable to open the output file " + fFile, e);
            }

            // Write the nodes
            try
            {
                for (int i = 0; i < iaRecords.length; i++)
                {
                    int iNode = iaRecords[i];
                    StringWriter swStringWriter;

                    // First write the nodes to a string so we won't
                    // get incomplete records in case of an exception.
                    swStringWriter = new StringWriter(4096);

                    // Write the record to the string writer.
                    rwRecordWriter.writeRecord(sFileType, iNode, swStringWriter);

                    // Write the string buffer to the file.
                    wWriter.write(swStringWriter.getBuffer().toString());
                }

                wWriter.flush();
                lEndFileOffset = os.getChannel().position();
            }
            catch (Exception e)
            {
                throw new FileException("Unable to write the output file " + fFile, e);
            }
            finally
            {
                FileUtils.closeWriter(wWriter);
            }
        }
        catch (Exception e)
        {
            lErrorList.add(e);
        }

        Document dDoc = req.getNomDocument();

        req.addResponseElement("endoffset", Long.toString(lEndFileOffset));

        // Add the errors to the reply
        if (lErrorList.size() > 0)
        {
            int iErrorsNode = dDoc.createElement("errors");

            // Iterate over the exceptions and create an error line element for each line
            for (Iterator<Exception> iter = lErrorList.iterator(); iter.hasNext();)
            {
                Throwable eException = iter.next();
                StringBuffer sbLine = new StringBuffer(80);

                while (eException != null)
                {
                    if (sbLine.length() > 0)
                    {
                        sbLine.append(" * ");
                    }

                    sbLine.append(eException.getMessage());

                    eException = eException.getCause();
                }

                dDoc.createTextElement("item", sbLine.toString(), iErrorsNode);
            }

            req.addResponseElement(iErrorsNode);
        }

        // Add error count to all replies
        req.addResponseElement("errorcount", Long.toString(lErrorList.size()));

        if (iResultNode != 0)
        {
            req.addResponseElement(iResultNode);
        }

        return EResult.FINISHED;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#getMethodName()
     */
    public String getMethodName()
    {
        return METHOD_NAME;
    }

    /**
     * Sets the configuration object.
     *
     * @param  cfg  New configuration.
     */
    public synchronized void setConfiguration(WriterConfig cfg)
    {
        configuration = cfg;
    }

    /**
     * Returns method configuration object.
     *
     * @param   req  Current SOAP request.
     *
     * @return  Configuration object.
     *
     * @throws  FileException  Thrown if the configuration could not be read.
     */
    private synchronized WriterConfig getConfiguration(ISoapRequestContext req)
                                                throws FileException
    {
        if (configuration != null)
        {
            return configuration;
        }

        // Get configuration file name
        String sConfigFileName = acConfig.getWriterConfigFileLocation();

        if ((sConfigFileName == null) || sConfigFileName.equals(""))
        {
            throw new FileException("Configuration file not set for this connector.");
        }

        // Set the XMLStore SOAP connection information.
        SOAPWrapper swSoap = new SOAPWrapper(req.getNomConnector());

        swSoap.setUser(req.getRequestUserDn());

        // This reads the configuration from XMLStore and parses it.
        WriterConfig result = null;

        try
        {
            XMLStoreWrapper xmlStoreWrapper = new XMLStoreWrapper(swSoap);
            int configNode;

            // Get the file from XMLStore.
            configNode = xmlStoreWrapper.getXMLObject(sConfigFileName);

            // Find the actual file node from the response.
            if ((configNode != 0) && (Node.getNumChildren(configNode) > 0))
            {
                // Get the response node
                configNode = Find.firstMatch(configNode, "?<tuple><old><>");

                // The check that the response is valid.
                if (configNode == 0)
                {
                    // No it was not.
                    throw new ConfigException("Invalid response received from XMLStore.");
                }
            }

            // Check if we have a file node.
            if (configNode == 0)
            {
                // No, it probably wasn't found.
                return null;
            }

            result = new WriterConfig(configNode, acConfig.isUseSimpleXPath());
        }
        catch (Exception e)
        {
            throw new FileException("Unable to load WriteFileRecords configuration: " +
                                    e.getMessage(), e);
        }
        finally
        {
            swSoap.freeXMLNodes();
        }

        if (!reloadConfiguration)
        {
            configuration = result;
        }

        return result;
    }
}

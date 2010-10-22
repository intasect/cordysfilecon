/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.methods.largexml.LargeXmlFileReader;
import com.cordys.coe.ac.fileconnector.methods.largexml.SimpleXPath;
import com.cordys.coe.ac.fileconnector.methods.largexml.XmlTraverseContext;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.File;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements ReadLargeXMLFileRecords SOAP method.
 *
 * @author  mpoyhone
 */
public class ReadLargeXmlFileRecordsMethod
    implements IFileConnectorMethod
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(ReadLargeXmlFileRecordsMethod.class);
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "ReadLargeXmlFileRecords";
    /**
     * Filename request parameter for ReadLargeXMLFileRecords methods.
     */
    private static final String PARAM_FILENAME = "filename";
    /**
     * XML select path request parameter for ReadLargeXMLFileRecords.
     */
    private static final String PARAM_SELECTPATH = "selectPath";
    /**
     * Return as text request parameter for ReadLargeXMLFileRecords.
     */
    private static final String PARAM_RETURNASTEXT = "returnAsText";
    /**
     * Number of records request parameter for ReadLargeXMLFileRecords.
     */
    private static final String PARAM_NUMRECORDS = "numrecords";
    /**
     * Validation only request parameter for ReadLargeXMLFileRecords.
     */
    private static final String PARAM_VALIDATEONLY = "validateonly";
    /**
     * Cursor data parameter for ReadLargeXMLFileRecords. When reading is started in the middle of
     * the file, this parameter must be passed from the previous response.
     */
    private static final String PARAM_CURSORDATA = "cursorData";
    /**
     * Contains the FileConnector configuration.
     */
    private ApplicationConfiguration acConfig;

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

/*        XMLProperties config = null;
 *           try     {         config = acConfig.getSection("largexmlfile");     }     catch
 * (GeneralException e)     {         throw new ConfigException("Unable to load configuration.", e);
 *     }*/

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

        // Get the needed parameters from the SOAP request
        String sFileName = XmlUtils.getStringParameter(requestNode, PARAM_FILENAME, true);
        String sFileSelectPath = XmlUtils.getStringParameter(requestNode, PARAM_SELECTPATH, true);
        int iNumRecords = (int) XmlUtils.getLongParameter(requestNode, PARAM_NUMRECORDS, true);
        boolean bValidateOnly = XmlUtils.getBooleanParameter(requestNode, PARAM_VALIDATEONLY);
        boolean bReturnAsText = XmlUtils.getBooleanParameter(requestNode, PARAM_RETURNASTEXT);
        String sCursorData = XmlUtils.getStringParameter(requestNode, PARAM_CURSORDATA, false);

        if (LOG.isDebugEnabled())
        {
            LOG.debug(String.format("ReadLargeXMLFileRecords: File=%s, XPath=%s, Num Records=%d",
                                    sFileName, sFileSelectPath, iNumRecords));
        }

        // Create File objects for the source file
        File fFile = new File(sFileName);

        // Parse the cursor data into a traverse context.
        XmlTraverseContext ctx = null;

        if ((sCursorData != null) && (sCursorData.length() > 0))
        {
            try
            {
                ctx = XmlTraverseContext.serializeFromBase64String(sCursorData);
            }
            catch (Exception e)
            {
                throw new FileException("Unable to parse the cursor data.", e);
            }
        }

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fFile))
        {
            throw new FileException("File access is not allowed.");
        }

        if (!fFile.exists())
        {
            throw new FileException("File does not exist.");
        }

        // Create the file reader object. For previously opened file, this opens
        // the file where the previous read was finished.
        LargeXmlFileReader reader;

        try
        {
            reader = new LargeXmlFileReader(fFile, ctx, true);
        }
        catch (Exception e)
        {
            throw new FileException("Unable to open the file: " + fFile, e);
        }

        reader.setCurrentXPath(new SimpleXPath(sFileSelectPath));

        // Process the file based on the request parameters.
        int iResultNode = 0;
        List<FileException> lErrorList = new LinkedList<FileException>();
        Document dDoc = req.getNomDocument();
        int iRecordsRead = 0;

        try
        {
            if (!bValidateOnly)
            {
                iResultNode = req.addResponseElement("data");
            }

            if (iNumRecords < 0)
            {
                iNumRecords = Integer.MAX_VALUE;
            }

            if (!reader.isAtEnd())
            {
                // Read the requested requested records.
                for (int i = 0; i < iNumRecords; i++)
                {
                    int res = reader.findNext(dDoc, !bValidateOnly);

                    if (res == 0)
                    {
                        break;
                    }

                    if (!bValidateOnly)
                    {
                        Node.appendToChildren(res, iResultNode);
                    }

                    iRecordsRead++;
                }
            }

            // If we are returning the XML as text replace the node contents
            // with text.
            if ((iResultNode != 0) && bReturnAsText)
            {
                String sContents = Node.writeToString(iResultNode, false);
                int iStartPos;
                int iEndPos;

                // Remove <data> and </data> tags.
                iStartPos = sContents.indexOf("<data>");
                iEndPos = sContents.lastIndexOf("</data>");

                if ((iStartPos >= 0) && (iEndPos > iStartPos))
                {
                    sContents = sContents.substring(iStartPos + 6, iEndPos);
                }

                // Replace the contents with the string version.
                Node.delete(Node.getFirstChild(iResultNode), Node.getLastChild(iResultNode));
                Node.setDataElement(iResultNode, "", sContents);
            }

            ctx = reader.getCurrentContext();
        }
        catch (Exception e)
        {
            lErrorList.add(new FileException("Unable to perform the XML query '" + fFile + "'.",
                                             e));
        }
        finally
        {
            reader.close();
        }

        String sResponseCursorData;

        try
        {
            sResponseCursorData = XmlTraverseContext.serializeToBase64String(ctx);
        }
        catch (Exception e)
        {
            throw new FileException("Unable to create the response cursor data.", e);
        }

        req.addResponseElement("recordsread", Integer.toString(iRecordsRead));

        // Add the errors to the reply
        if (lErrorList.size() > 0)
        {
            int iErrorsNode = req.addResponseElement("errors");

            // Iterate over the exceptions and create an error line element for each line
            for (Iterator<FileException> iter = lErrorList.iterator(); iter.hasNext();)
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
        }

        // Add error count to all replies
        req.addResponseElement("errorcount", Integer.toString(lErrorList.size()));

        // Add the cursor data to the reply.
        req.addResponseElement(PARAM_CURSORDATA, sResponseCursorData);

        return EResult.FINISHED;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#getMethodName()
     */
    public String getMethodName()
    {
        return METHOD_NAME;
    }
}

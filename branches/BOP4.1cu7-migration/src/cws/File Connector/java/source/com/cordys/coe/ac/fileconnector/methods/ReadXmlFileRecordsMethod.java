
/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys File Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.cordys.coe.ac.fileconnector.methods;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.LogMessages;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.XPathWrapper;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;
import com.cordys.coe.util.FileUtils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements ReadXMLFileRecords SOAP method.
 *
 * @author  mpoyhone
 */
public class ReadXmlFileRecordsMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "ReadXmlFileRecords";
    /**
     * Filename request parameter for ReadXmlFileRecords methods.
     */
    private static final String PARAM_FILENAME = "filename";
    /**
     * XML select path request parameter for ReadXmlFileRecords.
     */
    private static final String PARAM_SELECTPATH = "selectPath";
    /**
     * Return as text request parameter for ReadXmlFileRecords.
     */
    private static final String PARAM_RETURNASTEXT = "returnAsText";
    /**
     * Number of records request parameter for ReadXmlFileRecords.
     */
    private static final String PARAM_NUMRECORDS = "numrecords";
    /**
     * File offset request parameter for ReadXmlFileRecords.
     */
    private static final String PARAM_OFFSET = "offset";
    /**
     * Validation only request parameter for ReadXmlFileRecords.
     */
    private static final String PARAM_VALIDATEONLY = "validateonly";
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
        int iSelectOffset = (int) XmlUtils.getLongParameter(requestNode, PARAM_OFFSET, true);
        boolean bValidateOnly = XmlUtils.getBooleanParameter(requestNode, PARAM_VALIDATEONLY);
        boolean bReturnAsText = XmlUtils.getBooleanParameter(requestNode, PARAM_RETURNASTEXT);

        // Create File objects for the source fileacCo
        File fFile = new File(sFileName);

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fFile))
        {
            throw new FileException(LogMessages.FILE_ACCESS_NOT_ALLOWED);
        }

        if (!fFile.exists())
        {
            throw new FileException(LogMessages.FILE_NOT_EXIST);
        }

        // Fix the XPath for the new NOM implementation.
        if (!sFileSelectPath.startsWith("/"))
        {
            sFileSelectPath = "/" + sFileSelectPath;
        }

        int iResultNode = 0;
        List<FileException> lErrorList = new LinkedList<FileException>();
        int iFileContentsNode = 0;
        InputStream isInput = null;
        Document dDoc = req.getNomDocument();
        int iRecordsRead = 0;

        try
        {
            try
            {
                // Read and parse file contents.
                isInput = new FileInputStream(fFile);

                String sFileContents = FileUtils.readTextStreamContents(isInput);

                iFileContentsNode = dDoc.parseString(sFileContents);
            }
            catch (Exception e)
            {
                lErrorList.add(new FileException(e,LogMessages.UNABLE_TO_READ_INPUT_FILE,fFile ));
            }
            finally
            {
                FileUtils.closeStream(isInput);
            }

            try
            {
                // Find the start node
                XPathWrapper xqQuery = acConfig.getXPathFactory().createWrapper(sFileSelectPath);
                int[] iaNodes = xqQuery.findAllNodes(iFileContentsNode);

                if ((iaNodes == null) || (iaNodes.length == 0))
                {
                    lErrorList.add(new FileException(LogMessages.XMLQUERY_FOUND_NO_ELEMENTS));
                }
                else
                {
                    if (!bValidateOnly)
                    {
                        iResultNode = req.addResponseElement("data");
                    }

                    if (iNumRecords < 0)
                    {
                        iNumRecords = iaNodes.length;
                    }

                    for (int i = iSelectOffset;
                             (i < iaNodes.length) && ((i - iSelectOffset) < iNumRecords); i++)
                    {
                        if (!bValidateOnly)
                        {
                            int iSelectNode = iaNodes[i];
                            boolean isRoot = Node.getParent(iSelectNode) == 0;
                                
                            iSelectNode = Node.appendToChildren(iSelectNode, iResultNode);
                            if (isRoot) {
                                iFileContentsNode = 0;
                            }

                            String nsUri = Node.getNamespaceURI(iSelectNode);

                            if ((nsUri == null) || (nsUri.length() == 0))
                            {
                                // Set the node's namespace to the method namespace.
                                Node.removeAttribute(iSelectNode, "xmlns");
                            }
                        }

                        iRecordsRead++;
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

                        // Delete node's children
                        Node.delete(Node.getFirstChildElement(iResultNode),
                                    Node.getLastChildElement(iResultNode));

                        // Set node's data to the XML text.
                        Node.setDataElement(iResultNode, "", sContents);
                    }
                }
            }
            catch (Exception e)
            {
                lErrorList.add(new FileException(e,LogMessages.UNABLE_TO_PERFORM_XMLQUERY,fFile));

                if (iResultNode != 0)
                {
                    Node.delete(iResultNode);
                    iResultNode = 0;
                }
            }
        }
        finally
        {
            if (iFileContentsNode != 0)
            {
                Node.delete(iFileContentsNode);
                iFileContentsNode = 0;
            }
        }

        req.addResponseElement("endoffset", Integer.toString(iSelectOffset + iRecordsRead));
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

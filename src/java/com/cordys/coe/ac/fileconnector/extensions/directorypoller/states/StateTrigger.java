
/**
 * Copyright 2005 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys SAP Connector. 
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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import com.cordys.coe.ac.fileconnector.INomConnector;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Folder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FolderConfiguration;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter;
import com.cordys.coe.ac.fileconnector.utils.NodeWrapper;
import com.cordys.coe.util.FileUtils;

import com.eibus.directory.soap.DirectoryException;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.NodeSet;
import com.eibus.xml.xpath.ResultNode;
import com.eibus.xml.xpath.XPath;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import sun.misc.BASE64Encoder;

/**
 * File state where web service trigger was executed successfully.
 *
 * @author  mpoyhone
 */
public class StateTrigger
    implements IFileState
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateTrigger.class);
    /**
     * XPath expression for finding the parameters from the configuration XML.
     */
    private static final XPath PARAM_ATTRIB_XPATH = new XPath("//@*[name() = 'FCDP:element-data']");
    /**
     * Contains the file context object.
     */
    private FileContext fileContext;
    /**
     * Contains the previous state.
     */
    private IFileState prevState;
    /**
     * <code>true</code> if the SOAP request has already been sent.
     */
    private boolean triggerExecuted = false;
    /**
     * <code>true</code> if the trigger SOAP message was sent successfully.
     */
    private boolean triggerSucceeded = false;

    /**
     * Constructor for StateTriggerSucceeded.
     *
     * @param  currentState  Current state which will become the parent state of this state
     * @param  fileContext   File processing context object.
     */
    public StateTrigger(IFileState currentState, FileContext fileContext)
    {
        this.fileContext = fileContext;
        this.prevState = currentState;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#execute()
     */
    public boolean execute()
                    throws FileStateException
    {
        if (triggerExecuted)
        {
            if (triggerSucceeded)
            {
                // Trigger has already been sent.
                return true;
            }
            else
            {
                if (fileContext.getInputFolder().isCanRetry())
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("SOAP request has already been sent, but the trigger can be retried.");
                    }
                }
                else
                {
                    // SOAP request has been sent, but the execution ended in an error,
                    throw new FileStateException(FileStateException.EType.ABORT,
                                                 "SOAP request has already been sent and probably failed.");
                }
            }
        }

        IStateLogWriter logWriter = fileContext.getLogWriter();

        sendTriggerRequest(logWriter);

        // Set the next state.
        moveToNextState();

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#readFromLog(DataInputStream,
     *       boolean, XMLStreamWriter)
     */
    public void readFromLog(DataInputStream in, boolean finished, XMLStreamWriter logWriter)
                     throws IOException, XMLStreamException
    {
        triggerExecuted = true;
        triggerSucceeded = finished;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#writeToLog(DataOutputStream)
     */
    public void writeToLog(DataOutputStream out)
                    throws IOException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#getPreviousState()
     */
    public IFileState getPreviousState()
    {
        return prevState;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#getStateType()
     */
    public EFileState getStateType()
    {
        return EFileState.TRIGGER;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#isFinished()
     */
    public boolean isFinished()
    {
        return triggerSucceeded;
    }

    /**
     * This method recursively goes through the XML looking for the source="filename" to replace it.
     *
     * @param   iCurrentNode     The current XML node.
     * @param   fProcessingFile  The actual processing file.
     * @param   originalName     Original file name.
     *
     * @throws  FileException  Thrown if the operation failed.
     */
    private void fillParameters(int iCurrentNode, File fProcessingFile, String originalName)
                         throws FileException
    {
        Document dDoc = Node.getDocument(iCurrentNode);
        NodeSet resAttribs = PARAM_ATTRIB_XPATH.selectNodeSet(iCurrentNode);

        while (resAttribs.hasNext())
        {
            long resHandle = resAttribs.next();
            int elemNode = ResultNode.getElementNode(resHandle);
            String attrName = ResultNode.getName(resHandle);
            String type = ResultNode.getStringValue(resHandle);
            String value = null;
            boolean useCData = false;

            if ((elemNode != 0) && (type != null))
            {
                if (type.equals("filename"))
                {
                    value = originalName;
                }
                else if (type.equals("filepath"))
                {
                    value = fProcessingFile.getAbsolutePath();
                }
                else if (type.equals("filesize"))
                {
                    value = Long.toString(fProcessingFile.length());
                }
                else if (type.equals("content-text"))
                {
                    value = getFileContentsAsText(fProcessingFile, "UTF-8");
                    useCData = true;
                }
                else if (type.equals("content-base64"))
                {
                    value = getFileContentsAsBase64(fProcessingFile);
                    useCData = true;
                }
                else if (type.equals("content-xml"))
                {
                    getFileContentsAsXml(fProcessingFile, elemNode);
                }
                else if (type.equals("configured-folder"))
                {
                    getConfiguredFolder(elemNode);
                }
                else
                {
                    throw new FileException("Invalid parameter type: " + type);
                }

                if (value != null)
                {
                    if (useCData)
                    {
                        int iTmpData = dDoc.createCData(value);
                        Node.appendToChildren(iTmpData, elemNode);
                    }
                    else
                    {
                        Node.setDataElement(elemNode, "", value);
                    }
                }

                if (!attrName.startsWith("FCDP"))
                {
                    // Needed for C3.
                    attrName = "FCDP:" + attrName;
                }

                Node.removeAttribute(elemNode, attrName);
            }
        }
    }

    /**
     * Moves the state in the content to the next state of this state.
     */
    private void moveToNextState()
    {
        fileContext.setCurrentState(EFileState.FINISHED.createState(fileContext.getCurrentState(),
                                                                    fileContext));
    }

    /**
     * Sends the trigger SOAP request.
     *
     * @param   logWriter  fileLog File state log to use.
     *
     * @throws  FileStateException
     */
    private void sendTriggerRequest(IStateLogWriter logWriter)
                             throws FileStateException
    {
        INomConnector nomConnector = fileContext.getNomConnector();
        Folder inputFolder = fileContext.getInputFolder();
        File currentFile = fileContext.getCurrentFile();
        File inputFile = fileContext.getOriginalFile();
        int parametersNode = inputFolder.getParametersNode();
        int iEnvRequest = 0;
        int iEnvResponse = 0;
        int iParams = 0;

        if (inputFolder == null)
        {
            throw new FileStateException(FileStateException.EType.INTERNAL,
                                         "Input folder it not set.");
        }

        if (currentFile == null)
        {
            throw new FileStateException(FileStateException.EType.INTERNAL,
                                         "Current file it not set.");
        }

        if (inputFile == null)
        {
            throw new FileStateException(FileStateException.EType.INTERNAL,
                                         "Original input file it not set.");
        }

        if (nomConnector == null)
        {
            throw new FileStateException(FileStateException.EType.INTERNAL,
                                         "NOM connector it not set.");
        }

        try
        {
            try
            {
                // Create the method.
                int iMethod = nomConnector.createSoapMethod(inputFolder.getOrganization(),
                                                            inputFolder.getOrganizationalUser(),
                                                            inputFolder.getMethodName(),
                                                            inputFolder.getNamespace());
                iEnvRequest = Node.getRoot(iMethod);

                // Fill the parameters
                iParams = Node.duplicate(parametersNode);
                fillParameters(iParams, currentFile, inputFile.getName());
                Node.removeAttribute(iParams, "xmlns:FCDP");
                Node.appendToChildren(NodeWrapper.getFirstChildElement(iParams),
                                      NodeWrapper.getLastChildElement(iParams), iMethod);
            }
            catch (DirectoryException e)
            {
                throw new FileStateException(FileStateException.EType.RETRY_BLOCK_INPUT,
                                             "Unable to create SOAP method.", e);
            }
            catch (Exception e)
            {
                throw new FileStateException(FileStateException.EType.RETRY,
                                             "Unable to create trigger SOAP request.", e);
            }

            try
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Sending a SOAP request for file: " + currentFile + "\n" +
                              Node.writeToString(Node.getRoot(iEnvRequest), true));
                }

                // Log that we are starting to execute the trigger.
                if (logWriter != null)
                {
                    logWriter.startLogEntry(this, false);
                }

                // Mark that the SOAP request has been sent.
                triggerExecuted = true;

                // Send the request and wait for a response.
                int iResponseMethod = nomConnector.sendAndWait(iEnvRequest, true);

                // Log that the trigger was executed successfully.
                if (logWriter != null)
                {
                    logWriter.finishLogEntry();
                }

                iEnvResponse = Node.getRoot(iResponseMethod);

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Received a SOAP response: " +
                              Node.writeToString(iEnvResponse, true));
                }
            }
            catch (Exception e)
            {
                FileStateException.EType errorType;

                if (fileContext.getInputFolder().isCanRetry())
                {
                    errorType = FileStateException.EType.RETRY;
                }
                else
                {
                    // TODO: Here we should check for SOAP processor being down or other
                    // recoverable error.
                    errorType = FileStateException.EType.ABORT;
                }

                throw new FileStateException(errorType,
                                             "Error triggering the webservice for " + currentFile,
                                             e);
            }
        }
        finally
        {
            if (iParams != 0)
            {
                Node.delete(iParams);
                iParams = 0;
            }

            if (iEnvRequest != 0)
            {
                Node.delete(iEnvRequest);
            }

            if (iEnvResponse != 0)
            {
                Node.delete(iEnvResponse);
            }
        }
    }

    /**
     * Sets the configured folder path as the given node text.
     *
     * @param   node  Folder path in set under this node.
     *
     * @throws  FileException
     */
    private void getConfiguredFolder(int node)
                              throws FileException
    {
        FolderConfiguration config = fileContext.getFolderConfig();

        if (config == null)
        {
            throw new FileException("Folder configuration is not set.");
        }

        String folderName = Node.getData(node);

        if ((folderName == null) || (folderName.length() == 0))
        {
            throw new FileException("Configured folder name is not set.");
        }

        folderName = folderName.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();

        Folder folder = config.getFolderByName(folderName);

        if (folder == null)
        {
            throw new FileException("Configured folder not found: " + folderName);
        }

        Node.setDataElement(node, "", folder.getDirectory().getAbsolutePath());
    }

    /**
     * Returns the file contexts as a base64 encoded string.
     *
     * @param   file  File to be read.
     *
     * @return  File contents as a base64 encoded string.
     *
     * @throws  FileException
     */
    private String getFileContentsAsBase64(File file)
                                    throws FileException
    {
        InputStream is = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BASE64Encoder beEncoder = new BASE64Encoder();

        try
        {
            is = new FileInputStream(file);
            beEncoder.encode(is, baos);
        }
        catch (Exception e)
        {
            throw new FileException("Error encoding input file: " + file, e);
        }
        finally
        {
            FileUtils.closeStream(is);
        }

        return baos.toString();
    }

    /**
     * Returns the file contexts as text.
     *
     * @param   file      File to be read.
     * @param   encoding  Character set encoding.
     *
     * @return  File contents as text.
     *
     * @throws  FileException
     */
    private String getFileContentsAsText(File file, String encoding)
                                  throws FileException
    {
        InputStream is = null;

        try
        {
            is = new FileInputStream(file);
            return FileUtils.readTextStreamContents(is, encoding);
        }
        catch (Exception e)
        {
            throw new FileException("Error reading input file: " + file, e);
        }
        finally
        {
            FileUtils.closeStream(is);
        }
    }

    /**
     * Returns the file contents parsed into a XML node.
     *
     * @param   file        File to be read.
     * @param   parentNode  File XML is appended under this node.
     *
     * @throws  FileException
     */
    private void getFileContentsAsXml(File file, int parentNode)
                               throws FileException
    {
        try
        {
            int node = Node.getDocument(parentNode).load(file.getAbsolutePath());

            Node.appendToChildren(node, parentNode);
        }
        catch (Exception e)
        {
            throw new FileException("Error reading input file: " + file, e);
        }
    }
}


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

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.DirectoryPollerThread;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Utils;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogReader;
import com.cordys.coe.util.FileUtils;
import com.cordys.coe.util.general.Util;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Date;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Moves the file to the error folder. This state is not part of the normal processing, but is
 * created by the DirectoryPollerThread. It is implemented as a state for consistency.
 *
 * @author  mpoyhone
 */
public class StateError
    implements IFileState
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateError.class);
    /**
     * Name of the error message file in the error folder.
     */
    public static final String ERROR_INFO_FILE = "__FC_ERROR_INFO.xml";
    /**
     * Contains the file context object.
     */
    private FileContext fileContext;
    /**
     * Exception which cause the file processing to terminate.
     */
    private Throwable fileException;
    /**
     * The main directory poller thread.
     */
    private DirectoryPollerThread pollerThread;

    /**
     * Constructor for StateError.
     *
     * @param  fileContext    File processing context object.
     * @param  fileException  Exception which cause the file processing to terminate.
     * @param  pollerThread   The main directory poller thread.
     */
    public StateError(FileContext fileContext, Throwable fileException,
                      DirectoryPollerThread pollerThread)
    {
        this.fileContext = fileContext;
        this.fileException = fileException;
        this.pollerThread = pollerThread;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#execute()
     */
    public boolean execute()
                    throws FileStateException
    {
        if (fileContext == null)
        {
            LOG.log(Severity.ERROR, "Internal error: File context object is not set.");
            return true;
        }

        // First close the state log so that we can move the processing folder.
        try
        {
            fileContext.closeLog();
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Unable to close file state log file.", e);
        }

        // Find out the correct folder under the error folder.
        File errorRootFolder = fileContext.getErrorRootFolder();

        if (errorRootFolder == null)
        {
            LOG.log(Severity.ERROR, "Internal error: Error root folder is not set.");
            return true;
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Moving file to the error folder: " + errorRootFolder);
        }

        File destErrorFolder;

        try
        {
            destErrorFolder = moveFile(fileContext, errorRootFolder);
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "Unable to move file to the error folder: " + fileContext, e);
            return true;
        }

        // Indicate the the file is no longer in the processing folder.
        pollerThread.fileProcessingFinished(fileContext);

        // Write an error description file.
        String origFilePath;
        String currentFilePath;

        if (fileContext.getOriginalFile() != null)
        {
            origFilePath = fileContext.getOriginalFile().getAbsolutePath();
        }
        else
        {
            origFilePath = "*UNKNOWN*";
        }

        if (fileContext.getCurrentFile() != null)
        {
            currentFilePath = fileContext.getCurrentFile().getAbsolutePath();
        }
        else
        {
            currentFilePath = "*UNKNOWN*";
        }

        File errorFile = new File(destErrorFolder, ERROR_INFO_FILE);
        OutputStream out = null;

        try
        {
            XMLStreamWriter xmlWriter;

            out = new FileOutputStream(errorFile);
            xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

            xmlWriter.writeStartDocument();
            xmlWriter.writeStartElement("error-info");
            Utils.writeXmlElement(xmlWriter, "error-time", new Date());
            Utils.writeXmlElement(xmlWriter, "current-file", currentFilePath);
            xmlWriter.writeStartElement("original-file");
            Utils.writeXmlElement(xmlWriter, "path", origFilePath);
            Utils.writeXmlElement(xmlWriter, "last-modified",
                                  new Date(fileContext.getFileLastModified()));
            xmlWriter.writeEndElement(); // original-file
            Utils.writeXmlElementCData(xmlWriter, "trace", Util.getStackTrace(fileException));
            xmlWriter.writeStartElement("file-states");

            try
            {
                // We try to open the state log file and append all log entries into the error
                // XML. We need to set the processing folder now to the error folder for
                // this to work.
                File curProcFolder = fileContext.getProcessingFolder();

                fileContext.setProcessingFolder(destErrorFolder);

                IStateLogReader reader = fileContext.getLogReader();

                if (reader != null)
                {
                    reader.readLog(fileContext, xmlWriter);
                }

                fileContext.setProcessingFolder(curProcFolder);
            }
            catch (Exception e)
            {
                LOG.log(Severity.INFO,
                        "Unable write file state log into error info file: " + errorFile, e);
            }

            xmlWriter.writeEndElement(); // file-states
            xmlWriter.writeEndElement(); // error-info
            xmlWriter.writeEndDocument();
            xmlWriter.flush();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Wrote error info file: " + errorFile);
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "Unable write error info file: " + errorFile, e);
        }
        finally
        {
            FileUtils.closeStream(out);
        }

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#readFromLog(java.io.DataInputStream,
     *       boolean, javax.xml.stream.XMLStreamWriter)
     */
    public void readFromLog(DataInputStream in, boolean finished, XMLStreamWriter logWriter)
                     throws IOException, XMLStreamException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#writeToLog(java.io.DataOutputStream)
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
        return null;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#getStateType()
     */
    public EFileState getStateType()
    {
        return EFileState.ERROR;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#isFinished()
     */
    public boolean isFinished()
    {
        // File processing cannot continue after this state.
        return true;
    }

    /**
     * Moves the file to the error folder depending on the file state. A new folder will be created
     * where the file is moved with some other information.
     *
     * @param   fileContext      Processing context for the file.
     * @param   rootErrorFolder  Configured error folder.
     *
     * @return  File's error folder.
     *
     * @throws  IOException
     */
    private File moveFile(FileContext fileContext, File rootErrorFolder)
                   throws IOException
    {
        File processingFolder = fileContext.getProcessingFolder();

        if ((processingFolder == null) || !processingFolder.exists())
        {
            throw new IOException("Processing folder not set or it does not exist: " +
                                  processingFolder);
        }

        File destErrorFolder = new File(rootErrorFolder, processingFolder.getName());

        if (destErrorFolder.exists())
        {
            throw new IOException("Destination error folder already exists: " + destErrorFolder);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Moving processing folder " + processingFolder + " to error folder " +
                      destErrorFolder);
        }

        if (!processingFolder.renameTo(destErrorFolder))
        {
            throw new IOException("Unable to move processing folder : " + processingFolder);
        }

        // Set the current file location in the context.
        File curFile = fileContext.getCurrentFile();

        if (curFile != null)
        {
            fileContext.setCurrentFile(new File(destErrorFolder, curFile.getName()));
        }

        return destErrorFolder;
    }
}


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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Folder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Utils;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter;

import com.eibus.util.logger.CordysLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * File state for files that have been moved to the processing folder. This is the first state that
 * will be executed by the worker thread. This will also create the state log and write all previous
 * states in it.
 *
 * @author  mpoyhone
 */
public class StateInProcessing
    implements IFileState
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateInProcessing.class);
    /**
     * Contains the file context object.
     */
    private FileContext fileContext;
    /**
     * Contains the previous state.
     */
    private IFileState prevState;
    /**
     * File location in the processing folder.
     */
    private File processingFile;

    /**
     * Constructor for StateInProcessing.
     *
     * @param  currentState  Current state which will become the parent state of this state
     * @param  fileContext   File processing context object.
     */
    public StateInProcessing(IFileState currentState, FileContext fileContext)
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
        processingFile = fileContext.getCurrentFile();

        if ((processingFile == null) || !processingFile.exists())
        {
            // File does not exist anymore.
            throw new FileStateException(FileStateException.EType.ABORT,
                                         "File does not exist: " + processingFile);
        }

        // Open the state log and write the state information.
        IStateLogWriter logWriter = fileContext.getLogWriter();

        if (LOG.isDebugEnabled())
        {
            LOG.debug("File in processing folder: " + processingFile);
        }

        // Mark our state.
        logWriter.startLogEntry(this, true);

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
        String id;
        File origFile;

        id = in.readUTF();
        origFile = Utils.readFilePath(in);
        processingFile = Utils.readFilePath(in);
        fileContext.setFileSize(in.readLong());
        fileContext.setFileLastModified(in.readLong());

        fileContext.setFileId(id);
        fileContext.setOriginalFile(origFile);
        fileContext.setCurrentFile(processingFile);

        if (logWriter != null)
        {
            Utils.writeXmlElement(logWriter, "file-id", id);
            Utils.writeXmlElement(logWriter, "original-file", origFile);
            Utils.writeXmlElement(logWriter, "processing-file", processingFile);
            Utils.writeXmlElement(logWriter, "original-size",
                                  Long.toString(fileContext.getFileSize()));
            Utils.writeXmlElement(logWriter, "original-lastmodified",
                                  new Date(fileContext.getFileLastModified()));
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#writeToLog(DataOutputStream)
     */
    public void writeToLog(DataOutputStream out)
                    throws IOException
    {
        out.writeUTF(fileContext.getFileId());
        Utils.writeFilePath(out, fileContext.getOriginalFile());
        Utils.writeFilePath(out, processingFile);
        out.writeLong(fileContext.getFileSize());
        out.writeLong(fileContext.getFileLastModified());
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
        return EFileState.IN_PROCESSING;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#isFinished()
     */
    public boolean isFinished()
    {
        return false;
    }

    /**
     * Moves the state in the content to the next state of this state.
     */
    private void moveToNextState()
    {
        Folder inputFolder = fileContext.getInputFolder();
        IFileState nextState;

        if (inputFolder == null)
        {
            throw new IllegalStateException("Configured input folder is not set.");
        }

        if (inputFolder.isUseAppProcessingFolder())
        {
            nextState = EFileState.MOVE_TO_APP_PROCESSING.createState(fileContext.getCurrentState(),
                                                                      fileContext);
        }
        else
        {
            nextState = EFileState.TRIGGER.createState(fileContext.getCurrentState(), fileContext);
        }

        fileContext.setCurrentState(nextState);
    }
}

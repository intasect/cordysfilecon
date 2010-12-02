
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

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Utils;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter;

import com.eibus.util.logger.CordysLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * File state for moving the file to the processing folder.
 *
 * @author  mpoyhone
 */
public class StateMoveToAppProcessing
    implements IFileState
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateMoveToAppProcessing.class);
    /**
     * Destination file after move.
     */
    private File destFile;
    /**
     * Contains the file context object.
     */
    private FileContext fileContext;
    /**
     * Contains the previous state.
     */
    private IFileState prevState;
    /**
     * Source file before move.
     */
    private File srcFile;

    /**
     * Constructor for StateMoveToAppProcessing.
     *
     * @param  currentState  Current state which will become the parent state of this state
     * @param  fileContext   File processing context object.
     */
    public StateMoveToAppProcessing(IFileState currentState, FileContext fileContext)
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
        IStateLogWriter logWriter = fileContext.getLogWriter();
        File appProcessingFolder = fileContext.getAppProcessingRootFolder();
        String fileId = fileContext.getFileId();

        if (appProcessingFolder == null)
        {
            throw new FileStateException(FileStateException.EType.ABORT,
                                         "Trigger requires an application processing folder but it has not been configured.");
        }

        if ((fileId == null) || (fileId.length() == 0))
        {
            throw new FileStateException(FileStateException.EType.INTERNAL, "File ID is not set.");
        }

        if (srcFile == null)
        {
            srcFile = fileContext.getCurrentFile();
        }

        if (destFile == null)
        {
            destFile = new File(appProcessingFolder, fileId + Utils.getFileExtension(srcFile));
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Moving file " + srcFile + " to the application processing folder file " +
                      destFile);
        }

        // Mark start of our state.
        logWriter.startLogEntry(this, false);

        // Move the file to the new folder.
        try
        {
            Utils.moveFile(srcFile, destFile);
        }
        catch (IOException e)
        {
            throw new FileStateException(FileStateException.EType.RETRY,
                                         "The renaming of file " + srcFile + " to " + destFile +
                                         " failed.");
        }

        fileContext.setCurrentFile(destFile);
        fileContext.setInAppProcessingFolder(true);

        // Mark the state finished.
        logWriter.finishLogEntry();

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
        srcFile = Utils.readFilePath(in);
        destFile = Utils.readFilePath(in);

        if (finished)
        {
            fileContext.setCurrentFile(destFile);
        }

        if (logWriter != null)
        {
            Utils.writeXmlElement(logWriter, "src-file", srcFile);
            Utils.writeXmlElement(logWriter, "dest-file", destFile);
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#writeToLog(DataOutputStream)
     */
    public void writeToLog(DataOutputStream out)
                    throws IOException
    {
        Utils.writeFilePath(out, srcFile);
        Utils.writeFilePath(out, destFile);
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
        return EFileState.MOVE_TO_APP_PROCESSING;
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
        fileContext.setCurrentState(EFileState.TRIGGER.createState(fileContext.getCurrentState(),
                                                                   fileContext));
    }
}

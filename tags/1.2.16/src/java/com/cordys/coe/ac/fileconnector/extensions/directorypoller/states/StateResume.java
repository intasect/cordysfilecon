
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
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.StateLog_ProcessingFolder;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Marker state that indicates the file processing has been resumed after a crash.
 *
 * @author  mpoyhone
 */
public class StateResume
    implements IFileState
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateResume.class);
    /**
     * Contains the file context object.
     */
    private FileContext fileContext;
    /**
     * Contains the previous state.
     */
    private IFileState prevState;
    /**
     * Contains the processing folder read from state log.
     */
    private File processingFolder;

    /**
     * Constructor for StateResume.
     *
     * @param  currentState  Current state which will become the parent state of this state
     * @param  fileContext   File processing context object.
     */
    public StateResume(IFileState currentState, FileContext fileContext)
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
        processingFolder = fileContext.getProcessingFolder();

        // Open the state log and write the state information.
        IStateLogWriter logWriter = fileContext.getLogWriter();

        // Mark the start of the resume operation.
        logWriter.startLogEntry(this, false);

        if (prevState != null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Trying to resume last state found from state log: " +
                          prevState.getStateType());
            }

            // Set the previous state as the next state to be executed.
            fileContext.setCurrentState(prevState);
        }
        else
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No previous state in the state log. Trying to find the file in the processing folder.");
            }

            // There is not previous state, so the file has been moved but state log
            // was not written successfully. We just check that the input file
            // exists in the processing folder and create a IN_PROCESSING state.
            File file = getProcessingFile();

            if (file == null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("No file found from the processing folder");
                }

                throw new FileStateException(FileStateException.EType.ABORT,
                                             "No file found from the processing folder: " +
                                             processingFolder);
            }
            else
            {
                // File found, so we can start the processing from the initial processing folder
                // state.
                fileContext.setOriginalFile(file);
                fileContext.setCurrentFile(file);
                fileContext.setCurrentState(EFileState.IN_PROCESSING.createState(this,
                                                                                 fileContext));
            }
        }

        // Mark that the resume operation is finished.
        logWriter.finishLogEntry();

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#readFromLog(DataInputStream,
     *       boolean, XMLStreamWriter)
     */
    public void readFromLog(DataInputStream in, boolean finished, XMLStreamWriter logWriter)
                     throws IOException, XMLStreamException
    {
        processingFolder = Utils.readFilePath(in);
        fileContext.setProcessingFolder(processingFolder);

        if (logWriter != null)
        {
            Utils.writeXmlElement(logWriter, "processing-folder", processingFolder);
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#writeToLog(DataOutputStream)
     */
    public void writeToLog(DataOutputStream out)
                    throws IOException
    {
        Utils.writeFilePath(out, processingFolder);
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
        return EFileState.RESUME;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#isFinished()
     */
    public boolean isFinished()
    {
        return (prevState != null) ? prevState.isFinished() : false;
    }

    /**
     * Tries to locate the actual file from the processing folder.
     *
     * @return
     */
    private File getProcessingFile()
    {
        File processingFolder = fileContext.getProcessingFolder();

        if (processingFolder == null)
        {
            LOG.log(Severity.ERROR, "File processing folder is not set.");

            return null;
        }

        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return !StateLog_ProcessingFolder.LOGFILE_NAME.equals(name) &&
                       !StateError.ERROR_INFO_FILE.equals(name);
            }
        };

        File[] files = processingFolder.listFiles(filter);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Found " + files.length + " in the processing folder.");
        }

        if (files.length == 0)
        {
            return null;
        }

        if (LOG.isWarningEnabled())
        {
            if (files.length > 1)
            {
                LOG.log(Severity.WARN,
                        "More than one file found in the processing folder. Returning the first one.");
            }
        }

        return files[0];
    }
}


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
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Utils;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * File state for moving the file to the processing folder.
 *
 * @author  mpoyhone
 */
public class StateMoveToProcessing
    implements IFileState
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateMoveToProcessing.class);
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
     * Constructor for StateMoveToProcessing.
     *
     * @param  currentState  Current state which will become the parent state of this state
     * @param  fileContext   File processing context object.
     */
    public StateMoveToProcessing(IFileState currentState, FileContext fileContext)
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
        String fileId = fileContext.getFileId();

        if ((fileId == null) || (fileId.length() == 0))
        {
            throw new FileStateException(FileStateException.EType.INTERNAL, "File ID is not set.");
        }

        if (srcFile == null)
        {
            srcFile = fileContext.getCurrentFile();
        }

        try
        {
            if (!lockFile(srcFile))
            {
                // File cannot be locked (maybe used by another process), so we cannot move it yet.
                return false;
            }
        }
        catch (IOException e)
        {
            throw new FileStateException(FileStateException.EType.RETRY, "File locking failed.", e);
        }

        if (destFile == null)
        {
            // File is now locked, so we move it to the processing folder. First we create the
            // folder itself.
            File procesingRootFolder = fileContext.getProcessingRootFolder();
            String fileName = srcFile.getName();
            File parentFolder;

            if (fileContext.getProcessingFolder() == null)
            {
                parentFolder = new File(procesingRootFolder, fileId);

                if (!parentFolder.mkdir())
                {
                    throw new FileStateException(FileStateException.EType.ABORT,
                                                 "Unable to create the processing folder: " +
                                                 parentFolder);
                }

                fileContext.setProcessingFolder(parentFolder);
            }
            else
            {
                parentFolder = fileContext.getProcessingFolder();
            }

            destFile = new File(parentFolder, fileName);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Moving file " + srcFile + " to the processing folder file " + destFile);
        }

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
        return EFileState.MOVE_TO_PROCESSING;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#isFinished()
     */
    public boolean isFinished()
    {
        return false;
    }

    /**
     * This method tries to determine whether or not the file on the OS is still locked. The only
     * way to determine this is to try to open the file for writing. If that succeeds then the OS
     * file is not locked anymore.
     *
     * @param   file  File to be locked.
     *
     * @return  true if the locking succeeded. Otherwise false.
     *
     * @throws  IOException         Thrown if the file does not exist or is not writable.
     * @throws  FileStateException  Thrown if the file could not be opened for reading (no
     *                              permissions).
     */
    private static boolean lockFile(File file)
                             throws IOException, FileStateException
    {
        boolean bReturn = true;

        // Now try to create the random access file.
        RandomAccessFile raf = null;

        // Check if the file still exists. If not, an exception is thrown.
        // This is because the new RandomAccessFile() creates the file if it doesn't exist
        // If this happens an empty file will be transfered to the destination.
        if (!file.exists())
        {
            throw new FileNotFoundException("The file " + file.getAbsolutePath() +
                                            " does not exist anymore.");
        }

        if (!file.canRead())
        {
            throw new IOException("File is not readable: " + file);
        }

        if (!file.canWrite())
        {
            throw new IOException("File is not writable: " + file);
        }

        try
        {
            raf = new RandomAccessFile(file, "rw");

            FileChannel channel = raf.getChannel();

            // Wait for the lock.
            FileLock lock = channel.tryLock();

            try
            {
                if (lock != null)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Lock on file " + file + " obtained.");
                    }
                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Could not obtain lock on " + file);
                    }
                    bReturn = false;
                }
            }
            finally
            {
                if (lock != null)
                {
                    lock.release();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            // Lock could not be obtained. This will come here only when the file cannot be opened
            // for writing as we are checking if the file exists before this try-catch block.
            String msg = "File could not be locked. Cordys user probably does not have write permissions. File: " +
                         file;

            LOG.log(Severity.ERROR, msg, e);

            throw new FileStateException(FileStateException.EType.ABORT, msg, e);
        }
        catch (Exception e)
        {
            // Lock could not be obtained. Apparently the file is still being locked by another
            // process.
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Lock could not be obtained. It is probably being locked by another process.",
                          e);
            }

            bReturn = false;
        }
        finally
        {
            if (raf != null)
            {
                try
                {
                    raf.close();
                }
                catch (IOException e)
                {
                    // Ignore it.
                }
            }
        }

        return bReturn;
    }

    /**
     * Moves the state in the content to the next state of this state.
     */
    private void moveToNextState()
    {
        IFileState nextState;

        nextState = EFileState.IN_PROCESSING.createState(fileContext.getCurrentState(),
                                                         fileContext);

        fileContext.setCurrentState(nextState);
    }
}


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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.LogMessages;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Utils;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.EFileState;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.FileStateException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.util.Date;

import javax.xml.stream.XMLStreamWriter;

/**
 * Implements a file state log file which is used to keep track of file's state in the processing
 * folder. This is needed to be able to check if a SOAP request has been sent for this file when
 * file processing is resumed after a restart (e.g. after a crash).
 *
 * @author  mpoyhone
 */
public class StateLog_ProcessingFolder
    implements IStateLogReader, IStateLogWriter
{
    /**
     * The log file name.
     */
    public static final String LOGFILE_NAME = "__FC_STATELOG.log";
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(StateLog_ProcessingFolder.class);
    /**
     * Marker value for the entry start.
     */
    private static final int ENTRY_START_MARKER = 0x57;
    /**
     * Marker value for the entry end.
     */
    private static final int ENTRY_END_MARKER = 0xED;

    /**
     * Debug flag for disabling logging.
     */
    private static boolean enabled = true;
    /**
     * File object used for writing when the file is open.
     */
    private RandomAccessFile file = null;
    /**
     * File position last start entry. Used for writing the finished timestamp.
     */
    private long lastStartEntryPos = -1;
    /**
     * Contains the log file path.
     */
    private File logFile;

    /**
     * Factory method for creating a new instance.
     *
     * @param   logFile  Log gile.
     *
     * @return  New instance.
     */
    public static StateLog_ProcessingFolder getInstance(File logFile)
    {
        if (!logFile.exists())
        {
            return null;
        }

        StateLog_ProcessingFolder log = new StateLog_ProcessingFolder();

        log.logFile = logFile;

        return log;
    }

    /**
     * Factory method for creating a new instance.
     *
     * @param   ctx     File context.
     * @param   create  If <code>true</code> the log file can be created.
     *
     * @return  New instance.
     */
    public static StateLog_ProcessingFolder getInstance(FileContext ctx, boolean create)
    {
        File folder = ctx.getProcessingFolder();

        if ((folder == null) || !folder.exists())
        {
            return null;
        }

        File logFile = new File(folder, LOGFILE_NAME);

        if (!logFile.exists() && !create)
        {
            return null;
        }

        StateLog_ProcessingFolder log = new StateLog_ProcessingFolder();

        log.logFile = logFile;

        return log;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter#close()
     */
    public void close()
    {
        if (file != null)
        {
            try
            {
                file.close();
            }
            catch (Exception e)
            {
                LOG.log(Severity.WARN, "Log file closing failed: " + logFile, e);
            }

            file = null;
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter#finishLogEntry()
     */
    public void finishLogEntry()
                        throws FileStateException
    {
        if (lastStartEntryPos == -1)
        {
            throw new FileStateException(FileStateException.EType.INTERNAL,
                                         LogMessages.NO_START_ENTRY_WRITTEN);
        }

        try
        {
            long timeStamp = System.currentTimeMillis();
            long offset = 11;
            long current;

            // Move the file pointer to the finished timestamp.
            current = file.getFilePointer();
            file.seek(lastStartEntryPos + offset);
            file.writeLong(timeStamp);
            file.seek(current);
        }
        catch (Exception e)
        {
            throw new FileStateException(e,FileStateException.EType.ABORT,
                                         LogMessages.UNABLE_TO_WIRTE_STATE_TIMESTAMP,
                                         logFile);
        }

        lastStartEntryPos = -1;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter#open(boolean)
     */
    public void open(boolean forWriting)
              throws FileStateException
    {
        if (file != null)
        {
            return;
        }

        long pos;

        try
        {
            file = new RandomAccessFile(logFile, forWriting ? "rws" : "r");

            if (forWriting)
            {
                pos = file.length();

                if (pos > 0)
                {
                    file.seek(pos);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            throw new FileStateException(e,FileStateException.EType.ABORT,
                                         LogMessages.UNABLE_TO_OPEN_STATE_LOG_FILE,logFile);
        }
        catch (IOException e)
        {
            throw new FileStateException(e,FileStateException.EType.ABORT,
                                         LogMessages.UNABLE_TO_READ_STATE_LOG_FILE,logFile);
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogReader#readLog(com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext,
     *       javax.xml.stream.XMLStreamWriter)
     */
    public IFileState readLog(FileContext fileContext, XMLStreamWriter xmlWriter)
                       throws FileStateException
    {
        if (logFile.exists())
        {
            close();

            return parseLogFile(fileContext, xmlWriter);
        }
        else
        {
            throw new FileStateException(FileStateException.EType.ABORT,
                                         LogMessages.LOG_FILE_NOT_EXIST,logFile);
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter#startLogEntry(com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState,
     *       boolean)
     */
    public void startLogEntry(IFileState state, boolean finished)
                       throws FileStateException
    {
        if (!enabled)
        {
            return;
        }

        // Open just to be sure.
        open(true);

        try
        {
            // Create the entry.
            long timeStamp = System.currentTimeMillis();
            ByteArrayOutputStream out = new ByteArrayOutputStream(512);
            DataOutputStream dataOut = new DataOutputStream(out);
            byte[] entryData;
            int dataLength;

            dataOut.write(ENTRY_START_MARKER);
            dataOut.writeShort(0); // This will contain the entry length.
            dataOut.writeLong(timeStamp);
            dataOut.writeLong((!finished) ? -1 : timeStamp); // This is the finish timestamp.
            dataOut.writeByte(state.getStateType().ordinal());
            state.writeToLog(dataOut);
            dataOut.write(ENTRY_END_MARKER);
            dataOut.flush();
            entryData = out.toByteArray();
            dataLength = entryData.length - 3; // Entry length after entry length.
            entryData[1] = (byte) (0xFF & (dataLength >> 8));
            entryData[2] = (byte) (0xFF & dataLength);

            // Write the entry data to the file.
            lastStartEntryPos = file.getFilePointer();
            file.write(entryData);
        }
        catch (Exception e)
        {
            throw new FileStateException(FileStateException.EType.ABORT,
                                         LogMessages.UNABLE_TO_WRITE_STATE_TO_LOG_FILE,
                                         state.getStateType(),logFile);
        }
    }

    /**
     * Returns a state enumeration by the ID.
     *
     * @param   id  State ID
     *
     * @return  Enumeration object or <code>null</code> if the ID was not valid.
     */
    private static EFileState getStateById(int id)
    {
        EFileState[] states = EFileState.values();

        if ((id < 0) || (id >= states.length))
        {
            return null;
        }

        return states[id];
    }

    /**
     * Parses the log file entries in to this object.
     *
     * @param   fileContext  File context needed to create the states.
     * @param   logWriter    Optional XML writer which writes the entry into XML.
     *
     * @return  Last state that was read successfully.
     *
     * @throws  FileStateException  Thrown if file was corrupt.
     */
    private IFileState parseLogFile(FileContext fileContext, XMLStreamWriter logWriter)
                             throws FileStateException
    {
        RandomAccessFile file = null;
        IFileState currentState = null;

        try
        {
            long length;

            // Open the file for reading.
            file = new RandomAccessFile(logFile, "r");
            length = file.length();

            // Read all entries.
            while (file.getFilePointer() < length)
            {
                int marker;

                // Start marker is read first. We must be able to read it properly,
                // or the log file is corrupt.
                marker = file.read();

                if (marker != ENTRY_START_MARKER)
                {
                    LOG.log(Severity.INFO,
                            "Invalid entry start marker " + marker + " in state log: " + logFile);
                    break;
                }

                // Read the entry length and the entry data into an array.
                short entryLength = file.readShort();
                byte[] entryData = new byte[entryLength];

                if (file.read(entryData) < entryLength)
                {
                    LOG.log(Severity.INFO,
                            "End of file reached while reading state log file: " + logFile);
                    break;
                }

                // These read operations might fail, if the entry is not complete.
                try
                {
                    DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(entryData));
                    long startTimestamp;
                    long finishedTimestamp;
                    int stateId;
                    EFileState stateEnum;
                    IFileState state = null;

                    startTimestamp = dataIn.readLong();
                    finishedTimestamp = dataIn.readLong();
                    stateId = dataIn.readByte();
                    stateEnum = getStateById(stateId);

                    if (stateEnum == null)
                    {
                        LOG.log(Severity.INFO,
                                "Invalid state ID " + stateId + " in state log file: " + logFile);
                        break;
                    }

                    if ((currentState == null) || (currentState.getStateType() != stateEnum))
                    {
                        state = stateEnum.createState(currentState, fileContext);
                    }
                    else
                    {
                        state = currentState;
                    }

                    if (state == null)
                    {
                        LOG.log(Severity.INFO,
                                "Current state not found from in state log: " + logFile);
                        break;
                    }

                    if (logWriter != null)
                    {
                        logWriter.writeStartElement("state");
                        logWriter.writeAttribute("type", stateEnum.toString());
                        Utils.writeXmlAttribute(logWriter, "started",
                                                (startTimestamp != -1) ? new Date(startTimestamp)
                                                                       : null);
                        Utils.writeXmlAttribute(logWriter, "finished",
                                                (finishedTimestamp != -1)
                                                ? new Date(finishedTimestamp) : null);
                    }

                    state.readFromLog(dataIn, finishedTimestamp != -1, logWriter);

                    marker = dataIn.read();

                    if (marker != ENTRY_END_MARKER)
                    {
                        LOG.log(Severity.ERROR,
                                "Invalid entry end marker " + marker + " in state log: " + logFile);
                        break;
                    }

                    if (logWriter != null)
                    {
                        logWriter.writeEndElement();
                    }

                    currentState = state;
                }
                catch (Exception e)
                {
                    LOG.log(Severity.INFO, "Error while reading entry from state log: " + logFile,
                            e);
                    break;
                }
            }
        }
        catch (Exception e)
        {
            throw new FileStateException(e,FileStateException.EType.ABORT,
                                         LogMessages.UNABLE_TO_READ_STATE_LOG,logFile);
        }
        finally
        {
            if (file != null)
            {
                try
                {
                    file.close();
                }
                catch (Exception e)
                {
                    LOG.log(Severity.INFO, "Log file closing failed: " + logFile, e);
                }
            }
        }

        return currentState;
    }
}


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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.FileStateException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState;

/**
 * Used to store file processing state information, so that the processing can be resumed later.
 *
 * @author  mpoyhone
 */
public interface IStateLogWriter
{
    /**
     * Closes the state log. If log is not open, this method returns silently.
     */
    void close();

    /**
     * Marks the log entry previously written by startLogEntry() as finished.
     *
     * @throws  FileStateException  Thrown if the writing failed or no entry was created with
     *                              startLogEntry().
     */
    void finishLogEntry()
                 throws FileStateException;

    /**
     * Opens the state log for writing. If log is already open, this method returns silently.
     *
     * @param   forWriting  If <code>true</code>, the file is opened for writing, otherwise for
     *                      reading.
     *
     * @throws  FileStateException  Thrown if the log could not be opened.
     */
    void open(boolean forWriting)
       throws FileStateException;

    /**
     * Writes a start log entry. If finished if <code>false</code>, then the finishLogEntry() must
     * be called when the state has finished.
     *
     * @param   state     File state to be written.
     * @param   finished  If <code>true</code>, the entry is marked as finished.
     *
     * @throws  FileStateException  Thrown if the writing failed.
     */
    void startLogEntry(IFileState state, boolean finished)
                throws FileStateException;
}

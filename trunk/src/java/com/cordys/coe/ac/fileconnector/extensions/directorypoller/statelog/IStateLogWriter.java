/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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

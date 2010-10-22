/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Interface for different file states.
 *
 * @author  mpoyhone
 */
public interface IFileState
{
    /**
     * Executes this file state. This method will advance current the state in the FileContext. This
     * method can be called later if the state processing failed or when file processing is resumed
     * after a crash. This method must be able to determine the current state and proceed processing
     * from that.
     *
     * @return  <code>true</code> if successfull or <code>null</code> if this state is not yet
     *          complete.
     *
     * @throws  FileStateException  Thrown if the state processing failed. This must indicate if the
     *                              processing can be retried later.
     */
    boolean execute()
             throws FileStateException;

    /**
     * Called by the file state log, so that this state can read any state specific data from the
     * log entry.
     *
     * @param   in         File to be read from.
     * @param   finished   State specific phase value.
     * @param   logWriter  Optional XML writer which writes the entry into XML.
     *
     * @throws  IOException
     * @throws  XMLStreamException  Thrown if the XML log writing fails.
     */
    void readFromLog(DataInputStream in, boolean finished, XMLStreamWriter logWriter)
              throws IOException, XMLStreamException;

    /**
     * Called by the file state log, so that this state can write any state specific data into the
     * log entry.
     *
     * @param   out  File to be written to.
     *
     * @throws  IOException
     */
    void writeToLog(DataOutputStream out)
             throws IOException;

    /**
     * Returns the previous state, if it is set in this state.
     *
     * @return  Previous state or <code>null</code> if none is set.
     */
    IFileState getPreviousState();

    /**
     * Returns the state enumeration value for this state.
     *
     * @return  The state enumeration value for this state.
     */
    EFileState getStateType();

    /**
     * Indicates whether the file processing is finished.
     *
     * @return  <code>true</code> if file processing is finished.
     */
    boolean isFinished();
}

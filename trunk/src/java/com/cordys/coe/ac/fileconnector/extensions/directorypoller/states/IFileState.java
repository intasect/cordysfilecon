
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

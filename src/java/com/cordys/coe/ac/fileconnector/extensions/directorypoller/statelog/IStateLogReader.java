/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.FileStateException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState;

import javax.xml.stream.XMLStreamWriter;

/**
 * Reads entries from a file state log.
 *
 * @author  mpoyhone
 */
public interface IStateLogReader
{
    /**
     * Reads the log entries and returns the last successfully read state.
     *
     * @param   fileContext  File context needed to create the states.
     * @param   xmlWriter    Optional XML writer which writes the entries into XML.
     *
     * @return  Last state that was read successfully.
     *
     * @throws  FileStateException  Thrown if file could not be properly read.
     */
    IFileState readLog(FileContext fileContext, XMLStreamWriter xmlWriter)
                throws FileStateException;
}


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


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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.StateLog_ProcessingFolder;

import java.io.File;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

/**
 * Utility class to writing state log file contents to the standard output.
 *
 * @author  mpoyhone
 */
public class StateLogDumper
{
    /**
     * Main method.
     *
     * @param   args  Arguments.
     *
     * @throws  Exception  Thrown if failed.
     */
    public static void main(String[] args)
                     throws Exception
    {
        if (args.length < 0)
        {
            System.err.println("Folder or log file path missing.");
            return;
        }

        File file = new File(args[0]);

        if (file.isDirectory())
        {
            file = new File(file, StateLog_ProcessingFolder.LOGFILE_NAME);
        }

        if (!file.canRead())
        {
            System.err.println("File does not exist or is not readable: " + file);
            return;
        }

        FileContext ctx = new FileContext(null, null, null, null);
        StateLog_ProcessingFolder log = StateLog_ProcessingFolder.getInstance(file);
        XMLStreamWriter xmlWriter;

        log.open(false);

        xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);

        xmlWriter.writeStartDocument();
        xmlWriter.writeStartElement("file-states");

        log.readLog(ctx, xmlWriter);

        xmlWriter.writeEndElement(); // file-states
        xmlWriter.writeEndDocument();
        xmlWriter.flush();
    }
}

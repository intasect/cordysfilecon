/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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

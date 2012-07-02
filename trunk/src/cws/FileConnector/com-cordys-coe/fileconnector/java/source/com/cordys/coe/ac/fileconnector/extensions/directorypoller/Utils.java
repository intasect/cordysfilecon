
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

import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.StateLog_ProcessingFolder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateError;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;

import com.eibus.util.system.Native;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;

import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Utility methods for the states.
 *
 * @author  mpoyhone
 */
public class Utils
{
    /**
     * DOCUMENTME.
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Creates a unique file ID.
     *
     * @param   ctx  folderObject File's input folder.
     *
     * @return
     */
    public static String createFileId(FileContext ctx)
    {
        Folder folder = ctx.getInputFolder();
        String folderGuid = Native.createGuid().replaceAll("[^A-Za-z0-9]", "");

        if (folder != null)
        {
            String inputFolderName = folder.getName();

            return inputFolderName + "-" + folderGuid;
        }
        else
        {
            return folderGuid;
        }
    }

    /**
     * Creates a 'safe' file name in the parent folder. The name of the file will not clash with the
     * internal files used by DirectoryPoller.
     *
     * @param   file    File's name.
     * @param   parent  Parent folder.
     *
     * @return  A new file path in the parent folder.
     */
    public static File createSafeFile(File file, File parent)
    {
        if (file == null)
        {
            throw new NullPointerException("Source file object is not set.");
        }

        if (parent == null)
        {
            throw new NullPointerException("Parent folder object is not set.");
        }

        String name = file.getName();

        if (StateError.ERROR_INFO_FILE.equals(name) ||
                StateLog_ProcessingFolder.LOGFILE_NAME.equals(name))
        {
            // Remove the "__" from the beginning of the name.
            name = name.substring(2);
        }

        return new File(parent, name);
    }

    /**
     * DOCUMENTME.
     *
     * @param   srcFile       DOCUMENTME
     * @param   parentFolder  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public static File createUniqueFile(File srcFile, File parentFolder)
    {
        if (srcFile == null)
        {
            throw new NullPointerException("Source file object is not set.");
        }

        if (parentFolder == null)
        {
            throw new NullPointerException("Parent folder object is not set.");
        }

        // Create a file with a GUID name in the parent folder.
        String fileGuid = Native.createGuid().replaceAll("[^A-Za-z0-9]", "");
        String fileName = srcFile.getName();
        String fileExt = "";
        int extPos = fileName.lastIndexOf('.');

        if (extPos > 0)
        {
            fileExt = fileName.substring(extPos);
        }

        return new File(parentFolder, fileGuid + fileExt);
    }

    /**
     * DOCUMENTME.
     *
     * @param   folderObject  DOCUMENTME
     * @param   parentFolder  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  IOException  DOCUMENTME
     */
    public static File createUniqueFolder(Folder folderObject, File parentFolder)
                                   throws IOException
    {
        if (parentFolder == null)
        {
            throw new NullPointerException("Parent folder object is not set.");
        }

        // Generate the unique folder name with a GUID.
        String folderGuid = Native.createGuid().replaceAll("[^A-Za-z0-9]", "");
        File res;

        if (folderObject != null)
        {
            String inputFolderName = folderObject.getName();
            res = new File(parentFolder, inputFolderName + "-" + folderGuid);
        }
        else
        {
            res = new File(parentFolder, folderGuid);
        }

        if (!res.mkdir())
        {
            throw new IOException("Unable to create folder: " + res);
        }

        return res;
    }

    /**
     * Moves the source file to the destination file name. This method will check that the
     * destination file does not exist.
     *
     * @param   srcFile   Source file path.
     * @param   destFile  Destination file path.
     *
     * @throws  IOException  Thrown if the file could not be moved.
     */
    public static void moveFile(File srcFile, File destFile)
                         throws IOException
    {
        if (srcFile == null)
        {
            throw new NullPointerException("Source file object is not set.");
        }

        if (destFile == null)
        {
            throw new NullPointerException("Destination file object is not set.");
        }

        if (destFile.exists())
        {
            throw new IOException("Destination file already exists: " + destFile);
        }

        boolean res = srcFile.renameTo(destFile);

        if (!res || !destFile.exists())
        {
            
        	File parentDir = destFile.getParentFile();
        	if(!parentDir.exists())
        	{
        		parentDir.mkdir();
        	}
            try {
				// Files might be on different file systems, so copy the source file contents to the
				// destination file
				GeneralUtils.copyFile(srcFile, destFile);

				// Delete the source file
				if (!srcFile.delete())
				{
					destFile.delete();
				    throw new FileException(LogMessages.UNABLE_TO_DELETE_SRC_FILE);
				}
			} catch (FileException e) {
				throw new IOException("Moving of file " + srcFile + " to " + destFile +
                " failed.");
			}
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   in  DOCUMENTME
     *
     * @return  DOCUMENTME
     *
     * @throws  IOException  DOCUMENTME
     */
    public static File readFilePath(DataInputStream in)
                             throws IOException
    {
        String str = in.readUTF();

        if ((str != null) && (str.length() > 0))
        {
            return new File(str);
        }
        else
        {
            return null;
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   out   DOCUMENTME
     * @param   path  DOCUMENTME
     *
     * @throws  IOException  DOCUMENTME
     */
    public static void writeFilePath(DataOutputStream out, File path)
                              throws IOException
    {
        String str = ((path != null) ? path.getAbsolutePath() : "");

        out.writeUTF(str);
    }

    /**
     * DOCUMENTME.
     *
     * @param   out    DOCUMENTME
     * @param   name   DOCUMENTME
     * @param   value  DOCUMENTME
     *
     * @throws  XMLStreamException  DOCUMENTME
     */
    public static void writeXmlAttribute(XMLStreamWriter out, String name, Date value)
                                  throws XMLStreamException
    {
        String str = null;

        if (value != null)
        {
            SimpleDateFormat format = (SimpleDateFormat) dateFormat.clone();

            str = format.format(value);
        }
        else
        {
            str = "";
        }

        out.writeAttribute(name, str);
    }

    /**
     * DOCUMENTME.
     *
     * @param   out    DOCUMENTME
     * @param   name   DOCUMENTME
     * @param   value  DOCUMENTME
     *
     * @throws  XMLStreamException  DOCUMENTME
     */
    public static void writeXmlElement(XMLStreamWriter out, String name, String value)
                                throws XMLStreamException
    {
        if (value != null)
        {
            out.writeStartElement(name);
            out.writeCharacters(value);
            out.writeEndElement();
        }
        else
        {
            out.writeEmptyElement(name);
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   out    DOCUMENTME
     * @param   name   DOCUMENTME
     * @param   value  DOCUMENTME
     *
     * @throws  XMLStreamException  DOCUMENTME
     */
    public static void writeXmlElement(XMLStreamWriter out, String name, Date value)
                                throws XMLStreamException
    {
        if (value != null)
        {
            SimpleDateFormat format = (SimpleDateFormat) dateFormat.clone();

            out.writeStartElement(name);
            out.writeCharacters(format.format(value));
            out.writeEndElement();
        }
        else
        {
            out.writeEmptyElement(name);
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   out   DOCUMENTME
     * @param   name  DOCUMENTME
     * @param   path  DOCUMENTME
     *
     * @throws  XMLStreamException  DOCUMENTME
     */
    public static void writeXmlElement(XMLStreamWriter out, String name, File path)
                                throws XMLStreamException
    {
        if (path != null)
        {
            out.writeStartElement(name);
            out.writeCharacters(path.getAbsolutePath());
            out.writeEndElement();
        }
        else
        {
            out.writeEmptyElement(name);
        }
    }

    /**
     * DOCUMENTME.
     *
     * @param   out    DOCUMENTME
     * @param   name   DOCUMENTME
     * @param   value  DOCUMENTME
     *
     * @throws  XMLStreamException  DOCUMENTME
     */
    public static void writeXmlElementCData(XMLStreamWriter out, String name, String value)
                                     throws XMLStreamException
    {
        if (value != null)
        {
            out.writeStartElement(name);
            out.writeCData(value);
            out.writeEndElement();
        }
        else
        {
            out.writeEmptyElement(name);
        }
    }

    /**
     * Returns file's extension.
     *
     * @param   f  File in question.
     *
     * @return  File extension including the dot or an empty string.
     */
    public static String getFileExtension(File f)
    {
        String name = (f != null) ? f.getName() : null;

        if ((name == null) || (name.length() == 0))
        {
            return "";
        }

        int pos = name.lastIndexOf('.');

        return ((pos > 0) && (pos < (name.length() - 1))) ? name.substring(pos) : "";
    }
}

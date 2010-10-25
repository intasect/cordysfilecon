
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
 package com.cordys.coe.ac.fileconnector.utils;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.exception.FileException;

import com.eibus.util.logger.CordysLogger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;

import java.util.regex.Pattern;

/**
 * Contains general utility methods.
 *
 * @author  mpoyhone
 */
public class GeneralUtils
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(GeneralUtils.class);

    /**
     * Copies the file contents from one file to another. The destination file is overwritten.
     *
     * @param   fSrcFile   Source file
     * @param   fDestFile  Destination file
     *
     * @throws  FileException  Thrown if the operation was not successful
     */
    public static void copyFile(File fSrcFile, File fDestFile)
                         throws FileException
    {
        InputStream in = null;
        OutputStream out = null;
        final int iCopyBufferSize = 32768;
        boolean bSuccess = false;

        try
        {
            // Open the source file
            try
            {
                in = new BufferedInputStream(new FileInputStream(fSrcFile));
            }
            catch (Exception e)
            {
                throw new FileException("Unable to open source file.", e);
            }

            // Create the destination file
            try
            {
                out = new BufferedOutputStream(new FileOutputStream(fDestFile));
            }
            catch (Exception e)
            {
                throw new FileException("Unable to create destination file.", e);
            }

            // Create the buffer that is used while copying the data
            byte[] baBuffer = new byte[iCopyBufferSize];
            int iReadCount;

            try
            {
                // Copy the file contents
                while ((iReadCount = in.read(baBuffer)) > 0)
                {
                    out.write(baBuffer, 0, iReadCount);
                }
            }
            catch (Exception e)
            {
                throw new FileException("Error while copying the file.", e);
            }

            bSuccess = true;
        }
        finally
        {
            // Close the input stream
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (Exception e)
                {
                    // Nothing to be done.
                }
            }

            // Close the output stream
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (Exception e)
                {
                    // Nothing to be done.
                }
            }

            // If the copy operation failed, delete the destination file.
            if (!bSuccess && fDestFile.exists())
            {
                fDestFile.delete();
            }
        }
    }

    /**
     * Creates the given folder and all parent folders, if needed. This method also checks that the
     * folder is readable and writable.
     *
     * @param   folder  Folder to be created.
     *
     * @throws  FileException  Thrown if the folder or any parent folders cannot be created or the
     *                         folder is not valid.
     */
    public static void createFolder(File folder)
                             throws FileException
    {
        if (!folder.exists())
        {
            if (!folder.mkdirs())
            {
                throw new FileException("Unable to create processing folder: " + folder);
            }
        }
        else
        {
            if (!folder.isDirectory())
            {
                throw new FileException("Processing folder is not a folder: " + folder);
            }
        }

        if (!folder.canRead())
        {
            throw new FileException("Processing folder is not readable: " + folder);
        }

        if (!folder.canWrite())
        {
            throw new FileException("Processing folder is not writable: " + folder);
        }
    }

    /**
     * Creates a regular expression from glob-pattern. Supported formats:
     *
     * <pre>
         a/**&nbsp;/b
         a/*.xml
         a/*
         a*&nbsp;/**&nbsp;/b.xml
     * </pre>
     *
     * @param   globPattern        Glob pattern to be converted.
     * @param   useForwardSlashes  If <code>true</code> backslashes are converted to forward slashes
     *                             in the regexp.
     * @param   regexFlags         Standard regex flags to be used in the returned pattern.
     *
     * @return  Converted regexp.
     */
    public static Pattern createGlobRegex(String globPattern, boolean useForwardSlashes,
                                          int regexFlags)
    {
        StringBuilder sb = new StringBuilder(512);

        sb.append('^');

        for (int j = 0; j < globPattern.length(); j++)
        {
            char ch = globPattern.charAt(j);

            switch (ch)
            {
                case '\\':
                    if (useForwardSlashes)
                    {
                        sb.append('/');
                    }
                    else
                    {
                        // Escape \
                        sb.append("\\\\");
                    }
                    break;

                case '?':
                    sb.append('.');
                    break;

                case '*':
                    if ((j < (globPattern.length() - 1)) && (globPattern.charAt(j + 1) == '*'))
                    {
                        // This is **, so match everything.
                        sb.append(".+");
                        j++;
                    }
                    else
                    {
                        // This is *, so match only file and folder names.
                        sb.append("[^/]+");
                    }
                    break;

                case '.':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '^':
                case '$':
                case '|':
                    // Escape these regexp characters.
                    sb.append("\\").append(ch);
                    break;

                default:
                    sb.append(ch);
                    break;
            }
        }

        sb.append('$');

        return Pattern.compile(sb.toString(), regexFlags);
    }

    /**
     * Loads the given character set. This method is needed to be able to load our custom character
     * sets.
     *
     * @param   sCharsetName  Character set name.
     * @param   acConfig      Application connector configuration.
     *
     * @return  Character set <code>Charset</code> object, or null if none was found.
     *
     * @throws  FileException  Thrown if the character set could not be found.
     */
    public static Charset findCharacterSet(String sCharsetName, ApplicationConfiguration acConfig)
                                    throws FileException
    {
        CharsetProvider cpCustom = acConfig.getCustomCharsetProvider();

        if (cpCustom != null)
        {
            Charset csReturn = cpCustom.charsetForName(sCharsetName);

            if (csReturn != null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Using character set " + csReturn.displayName());
                }

                return csReturn;
            }
        }

        Charset csReturn = Charset.forName(sCharsetName);

        if (csReturn == null)
        {
            throw new FileException("Character set '" + sCharsetName + "' could not be found.");
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Using character set " + csReturn.displayName());
        }

        return csReturn;
    }

    /**
     * Returns file's contents in a byte array..
     *
     * @param   file  File to be read.
     *
     * @return  Contents as a byte array.
     *
     * @throws  IOException  Thrown if the reading failed.
     */
    public static byte[] readFile(File file)
                           throws IOException
    {
        byte[] cont = null;
        FileInputStream fi = new FileInputStream(file);

        try
        {
            long len = file.length();
            cont = new byte[(int) len];
            fi.read(cont);
        }
        finally
        {
            fi.close();
        }
        return cont;
    }

    /**
     * Returns an absolute file for the given path. If the path is a relative path, the parent
     * folder is used as the root folder for this path..
     *
     * @param   path             Path to be converted.
     * @param   relParentFolder  Parent folder for relative paths.
     *
     * @return  File object containing an absolute path.
     */
    public static File getAbsoluteFile(String path, File relParentFolder)
    {
        File res = new File(path);

        if (!res.isAbsolute())
        {
            res = new File(relParentFolder, path);
        }

        return res;
    }
}

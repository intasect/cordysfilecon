
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
 package com.cordys.coe.ac.fileconnector.methods;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;

import com.eibus.xml.nom.Node;

import java.io.File;

/**
 * Implements SelectAndMoveFile SOAP method.
 *
 * @author  mpoyhone
 */
public class SelectAndMoveFileMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "SelectAndMoveFile";
    /**
     * Source directory parameter for SelectAndMoveFile method.
     */
    private static final String PARAM_SRCDIRECTORY = "srcdir";
    /**
     * Destination directory parameter for SelectAndMoveFile method.
     */
    private static final String PARAM_DESTDIRECTORY = "destdir";
    /**
     * File type request parameter for SelectAndMoveFile.
     */
    private static final String PARAM_TYPE = "type";
    /**
     * Contains the FileConnector configuration.
     */
    private ApplicationConfiguration acConfig;

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#cleanup()
     */
    public void cleanup()
                 throws ConfigException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#initialize(com.cordys.coe.ac.fileconnector.ApplicationConfiguration)
     */
    public boolean initialize(ApplicationConfiguration acConfig)
                       throws ConfigException
    {
        this.acConfig = acConfig;

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#onReset()
     */
    public void onReset()
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)
     */
    public EResult process(ISoapRequestContext req)
                    throws FileException
    {
        int requestNode = req.getRequestRootNode();

        // Get the needed parameters from the SOAP request
        String sSrcDirName = XmlUtils.getStringParameter(requestNode, PARAM_SRCDIRECTORY, true);
        String sDestDirName = XmlUtils.getStringParameter(requestNode, PARAM_DESTDIRECTORY, true);
        String sFileType = XmlUtils.getStringParameter(requestNode, PARAM_TYPE, true);

        // Create File objects for the source and destination files
        File fSrcDir = new File(sSrcDirName);
        File fDestDir = new File(sDestDirName);
        FileSelector fsSelector = new FileSelector();

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fSrcDir))
        {
            throw new FileException("Source directory access is not allowed.");
        }

        if (!acConfig.isFileAllowed(fDestDir))
        {
            throw new FileException("Destination directory access is not allowed.");
        }

        if (!fSrcDir.exists())
        {
            throw new FileException("Source directory does not exist.");
        }

        if (!fSrcDir.isDirectory())
        {
            throw new FileException("Source directory is not a directory.");
        }

        if (!fDestDir.exists())
        {
            throw new FileException("Destination directory does not exist.");
        }

        if (!fDestDir.isDirectory())
        {
            throw new FileException("Destination directory is not a directory.");
        }

        if (!fsSelector.setType(sFileType))
        {
            throw new FileException("Invalid file type parameter.");
        }

        File fFile = fsSelector.select(fSrcDir);

        if (fFile == null)
        {
            // No file was found.
            return EResult.FINISHED;
        }

        // Move the file to the destination directory.
        File fDestFile = new File(fDestDir, fFile.getName());

        if (!fFile.renameTo(fDestFile))
        {
            throw new FileException("Unable to move file from " + fFile + " to " + fDestDir);
        }

        // Create the result elements.
        int iTupleNode = req.addResponseElement("tuple");

        Node.createTextElement("filename", fDestFile.getName(), iTupleNode);
        Node.createTextElement("filepath", fDestFile.getAbsolutePath(), iTupleNode);

        return EResult.FINISHED;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#getMethodName()
     */
    public String getMethodName()
    {
        return METHOD_NAME;
    }

    /**
     * Selects files from a directory based on the given criteria.
     *
     * @author  mpoyhone
     */
    public class FileSelector
    {
        /**
         * Select oldest files ID.
         */
        public static final int TYPE_OLDEST = 0;
        /**
         * Select newest files ID.
         */
        public static final int TYPE_NEWEST = 1;
        /**
         * Selection type.
         */
        int iType;

        /**
         * Returns a file based on the selection criteria from the directory.
         *
         * @param   fSelectDir  Selection directory
         *
         * @return  Selected file, or null if none was found.
         */
        public File select(File fSelectDir)
        {
            File fCurrent = null;
            long lCurrentTimestamp = 0;
            File[] faFiles = fSelectDir.listFiles();

            for (int i = 0; i < faFiles.length; i++)
            {
                File fFile = faFiles[i];

                if (!fFile.isFile())
                {
                    continue;
                }

                if (!acConfig.isFileAllowed(fFile))
                {
                    continue;
                }

                long lTimestamp = fFile.lastModified();

                switch (iType)
                {
                    case TYPE_NEWEST:

                        if ((fCurrent != null) && (lCurrentTimestamp >= lTimestamp))
                        {
                            continue;
                        }
                        fCurrent = fFile;
                        lCurrentTimestamp = lTimestamp;
                        break;

                    case TYPE_OLDEST:

                        if ((fCurrent != null) && (lCurrentTimestamp <= lTimestamp))
                        {
                            continue;
                        }
                        fCurrent = fFile;
                        lCurrentTimestamp = lTimestamp;
                        break;
                }
            }

            return fCurrent;
        }

        /**
         * Sets the selection type as string.
         *
         * @param   sType  Type to be set ('oldest' or 'newest').
         *
         * @return  True, if the type string was a valid and the type has been set.
         */
        public boolean setType(String sType)
        {
            if (sType.equals("oldest"))
            {
                iType = TYPE_OLDEST;
            }
            else if (sType.equals("newest"))
            {
                iType = TYPE_NEWEST;
            }
            else
            {
                return false;
            }

            return true;
        }
    }
}


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
 package com.cordys.coe.ac.fileconnector.methods;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;

import java.io.File;

/**
 * Implements MoveFile SOAP method.
 *
 * @author  mpoyhone
 */
public class MoveFileMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "MoveFile";
    /**
     * Old filename request parameter for CopyFile and MoveFile methods.
     */
    private static final String PARAM_OLDFILENAME = "oldFileName";
    /**
     * New filename request parameter for CopyFile and MoveFile methods.
     */
    private static final String PARAM_NEWFILENAME = "newFileName";
    /**
     * Name of the MoveFile parameter.
     */
    private static final String PARAM_OVERWRITE_EXISITING = "overwriteExisting";
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
        String sOldFileName = XmlUtils.getStringParameter(requestNode, PARAM_OLDFILENAME, true);
        String sNewFileName = XmlUtils.getStringParameter(requestNode, PARAM_NEWFILENAME, true);
        boolean bOverwriteExisting = XmlUtils.getBooleanParameter(requestNode,
                                                                  PARAM_OVERWRITE_EXISITING);

        // Create File objects for the source and destination files
        File fSrcFile = new File(sOldFileName);
        File fDestFile = new File(sNewFileName);

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fSrcFile))
        {
            throw new FileException("Source file access is not allowed.");
        }

        if (!acConfig.isFileAllowed(fDestFile))
        {
            throw new FileException("Destination file access is not allowed.");
        }

        if (!fSrcFile.exists())
        {
            throw new FileException("Source file does not exist.");
        }

        if (fSrcFile.isDirectory())
        {
            throw new FileException("Source file is a directory.");
        }

        if (fDestFile.exists())
        {
            if (fDestFile.isDirectory())
            {
                throw new FileException("Destination file exists and is a directory.");
            }

            if (!bOverwriteExisting)
            {
                throw new FileException("Destination file exists and overwrite is not specified.");
            }

            if (!fDestFile.delete())
            {
                throw new FileException("Unable to delete the destination file.");
            }
        }

        // First try to see if we can just rename the file.
        if (!fSrcFile.renameTo(fDestFile))
        {
            // Files might be on different file systems, so copy the source file contents to the
            // destination file
            GeneralUtils.copyFile(fSrcFile, fDestFile);

            // Delete the source file
            if (!fSrcFile.delete())
            {
                fDestFile.delete();
                throw new FileException("Unable to delete the source file.");
            }
        }

        return EResult.FINISHED;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#getMethodName()
     */
    public String getMethodName()
    {
        return METHOD_NAME;
    }
}

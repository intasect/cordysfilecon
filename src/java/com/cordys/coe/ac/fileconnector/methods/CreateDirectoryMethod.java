
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
 * Implements CreateDirectory SOAP method.
 *
 * @author  psriniva
 */
public class CreateDirectoryMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "CreateDirectory";
    /**
     * New directory name request parameter.
     */
    private static final String PARAM_NEWDIRNAME = "newDirectoryName";
    /**
     * Full path for parent directory parameter.
     */
    private static final String PARAM_PARENTDIRPATH = "parentDirectoryPath";        
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
        String sNewDirName = XmlUtils.getStringParameter(requestNode, PARAM_NEWDIRNAME, true);
        String sParentDirPath = XmlUtils.getStringParameter(requestNode, PARAM_PARENTDIRPATH, true);

        // Create File objects for the parent and new directory folders
        File fParentDir = new File(sParentDirPath);

        // Check if parent directory is listed in the allowed directories
        if (!acConfig.isFileAllowed(fParentDir))
        {
            throw new FileException("Parent directory access is not allowed.");
        }
        
        if (!fParentDir.exists())
        {
            throw new FileException("The specified parent directory cannot be found by the system.");
        }

        try 
        {	
            // Get the File which has full file path
            File fNewFullDir = GeneralUtils.getAbsoluteFile(sNewDirName, fParentDir);            
           
            // if does not exist create the new directories
            if (!fNewFullDir.exists())
            {
            	fNewFullDir.mkdirs();
            }
			
		} catch (Exception e) 
		{
			throw new FileException("Unable to create directory ",e);
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

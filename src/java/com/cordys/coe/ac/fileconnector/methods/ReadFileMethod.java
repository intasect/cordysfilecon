
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
import java.io.IOException;

import sun.misc.BASE64Encoder;

/**
 * Implements ReadFile SOAP method.
 *
 * @author  mpoyhone
 */
public class ReadFileMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "ReadFile";
    /**
     * Filename request parameter for ReadFileRecords, ReadFile, WriteFileRecords and WriteFile
     * methods.
     */
    private static final String PARAM_FILENAME = "filename";
    /**
     * The data parameter for WriteFile and ReadFile.
     */
    private static final String PARAM_ENCODED = "encoded";
    /**
     * The charset parameter for WriteFile and ReadFile.
     */
    private static final String PARAM_CHARSET = "charset";
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
        String fileName = XmlUtils.getStringParameter(requestNode, PARAM_FILENAME, true);
        boolean encoded = XmlUtils.getBooleanParameter(requestNode, PARAM_ENCODED);
        String charset = XmlUtils.getStringParameter(requestNode, PARAM_CHARSET, false);

        File file = new File(fileName);

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(file))
        {
            throw new FileException("File access is not allowed.");
        }

        if (!file.exists())
        {
            throw new FileException("File does not exist.");
        }

        try
        {
            byte[] cont = GeneralUtils.readFile(file);

            // Create the response.
            if (encoded)
            {
                BASE64Encoder encoder = new BASE64Encoder();
                req.addResponseElement("data", encoder.encode(cont));
            }
            else
            {
                if ((charset == null) || (charset.length() == 0))
                {
                    charset = acConfig.getStandardReaderCharacterSet();
                }

                req.addResponseElement("data", new String(cont, charset));
            }
        }
        catch (IOException e)
        {
            throw new FileException("Unable to read file: " + file, e);
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

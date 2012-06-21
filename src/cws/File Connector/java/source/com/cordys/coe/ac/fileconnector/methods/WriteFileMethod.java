
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
import com.cordys.coe.ac.fileconnector.LogMessages;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import sun.misc.BASE64Decoder;

/**
 * Implements WriteFile SOAP method.
 *
 * @author  mpoyhone
 */
public class WriteFileMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "WriteFile";
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
     * The append boolean parameter for WriteFileRecords.
     */
    private static final String PARAM_APPEND = "append";
    /**
     * The data parameter for WriteFile and ReadFile.
     */
    private static final String PARAM_DATA = "data";
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
        boolean append = XmlUtils.getBooleanParameter(requestNode, PARAM_APPEND);

        // Get data from the SOAP request
        int dataNode = Find.firstMatch(requestNode, "?<" + PARAM_DATA + ">");

        if (dataNode == 0)
        {
            throw new FileException(LogMessages.PARAM_MISSING,PARAM_DATA);
        }

        int dataChild = Node.getFirstElement(dataNode);
        String data = null;

        if (dataChild != 0)
        {
            data = Node.writeToString(dataChild, false);
        }
        else
        {
            data = Node.getData(dataNode);
        }

        if ((data == null) || (data.length() == 0))
        {
            throw new FileException(LogMessages.PARAM_EMPTY, PARAM_DATA);
        }

        // Create File objects for the destination file
        File file = new File(fileName);

        if (!acConfig.isFileAllowed(file))
        {
            throw new FileException(LogMessages.FILE_ACCESS_NOT_ALLOWED);
        }

        try
        {
            FileOutputStream fo = new FileOutputStream(file, append);

            try
            {
                if (encoded)
                {
                    BASE64Decoder decoder = new BASE64Decoder();
                    fo.write(decoder.decodeBuffer(data));
                }
                else
                {
                    if ((charset == null) || (charset.length() == 0))
                    {
                        charset = acConfig.getStandardWriterCharacterSet();
                    }

                    fo.write(data.getBytes(charset));
                }
            }
            finally
            {
                fo.close();
            }
        }
        catch (IOException e)
        {
            throw new FileException(e,LogMessages.UNABLE_TO_WRITE_FILE,file);
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

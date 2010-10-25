
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
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;

import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.FileFilter;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements GetListOfFiles SOAP method.
 *
 * @author  mpoyhone
 */
public class GetListOfFilesMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "GetListOfFiles";
    /**
     * Directory parameter for GetListOfFiles method.
     */
    private static final String PARAM_DIRECTORY = "directory";
    /**
     * Result path type parameter for GetListOfFiles method.
     */
    private static final String PARAM_RESULTPATH = "resultpathtype";
    /**
     * File name filter parameter for GetListOfFiles method.
     */
    private static final String PARAM_FILTER = "filter";
    /**
     * Date & Time format for GetListOfFiles entries.
     */
    private static final String sFileDateFormat = "yyyy-MM-dd'T'HH:mm:ss.S";
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
        String sDirName = XmlUtils.getStringParameter(requestNode, PARAM_DIRECTORY, true);
        String sResultPathType = XmlUtils.getStringParameter(requestNode, PARAM_RESULTPATH, false);
        String sFilter = XmlUtils.getStringParameter(requestNode, PARAM_FILTER, false);
        boolean bUseAbsolutePaths = true;

        if ((sResultPathType != null) && (sResultPathType.length() > 0))
        {
            if (sResultPathType.equals("absolute"))
            {
                bUseAbsolutePaths = true;
            }
            else if (sResultPathType.equals("relative"))
            {
                bUseAbsolutePaths = false;
            }
            else
            {
                throw new FileException("Invalid value for parameter " + PARAM_RESULTPATH);
            }
        }

        // Parse the filter parameter
        Pattern pFileFilter = null;

        if ((sFilter != null) && (sFilter.length() > 0))
        {
            if ((sFilter != null) && !sFilter.equals(""))
            {
                int iFlags = Pattern.CASE_INSENSITIVE;

                pFileFilter = Pattern.compile(sFilter, iFlags);
            }
        }

        // Create File objects for the source and destination files
        File fDir = new File(sDirName);

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fDir))
        {
            throw new FileException("Directory access is not allowed.");
        }

        if (!fDir.exists())
        {
            throw new FileException("Directory does not exist.");
        }

        if (!fDir.isDirectory())
        {
            throw new FileException("Directory is not a directory.");
        }

        // Get the directory listing
        File[] faDirList;

        // Get files specified by the filter.
        final Pattern pFinalFilter = pFileFilter;

        FileFilter ffFilterObject = new FileFilter()
        {
            public boolean accept(File fPathname)
            {
                if (!acConfig.isFileAllowed(fPathname))
                {
                    return false;
                }

                if (pFinalFilter != null)
                {
                    Matcher mMathcer = pFinalFilter.matcher(fPathname.getName());

                    return mMathcer.matches();
                }

                return true;
            }
        };

        faDirList = fDir.listFiles(ffFilterObject);

        if (faDirList == null)
        {
            throw new FileException("Unable to list files in the directory.");
        }

        // Create the response.
        int iTuple;
        int iEntries;

        iTuple = req.addResponseElement("tuple");
        Node.createTextElement("directory", fDir.getAbsolutePath(), iTuple);
        iEntries = Node.createElement("entries", iTuple);

        // Add all the file entries.
        for (int i = 0; i < faDirList.length; i++)
        {
            File fFile = faDirList[i];
            String sPath;
            String sType;
            int iFileNode;

            // Get the file path in requested format
            if (bUseAbsolutePaths)
            {
                sPath = fFile.getAbsolutePath();
            }
            else
            {
                sPath = fFile.getName();
            }

            // Get the file type string.
            if (fFile.isFile())
            {
                sType = "file";
            }
            else if (fFile.isDirectory())
            {
                sType = "directory";
            }
            else
            {
                sType = "unknown";
            }

            // Get the file modification time and create the
            // modification attribute value.
            long lModificationTime = fFile.lastModified();
            String sTimeString = new SimpleDateFormat(sFileDateFormat).format(new Date(lModificationTime));

            iFileNode = Node.createTextElement("file", sPath, iEntries);
            Node.setAttribute(iFileNode, "type", sType);
            Node.setAttribute(iFileNode, "modified", sTimeString);
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

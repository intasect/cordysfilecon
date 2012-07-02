
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Implements CountNumberOfLines SOAP method.
 *
 * @author  mpoyhone
 */
public class CountNumberOfLinesMethod
    implements IFileConnectorMethod
{
    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "CountNumberOfLines";
    /**
     * Filename request parameter for DeleteFile and methods.
     */
    private static final String PARAM_FILENAME = "fileName";
    /**
     * Line separator parameter for CountNumberOfLines method.
     */
    private static final String PARAM_LINESEPARATOR = "lineSeparator";
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
        String sFileName = XmlUtils.getStringParameter(requestNode, PARAM_FILENAME, true);
        String sLineSeparator = XmlUtils.getStringParameter(requestNode, PARAM_LINESEPARATOR, "");

        // Create File object and set the right line separator (if not specified).
        File fSrcFile = new File(sFileName);

        if (sLineSeparator.trim().length() == 0)
        {
            sLineSeparator = System.getProperty("line.separator");

            if (sLineSeparator == null)
            {
                sLineSeparator = "\\r\\n";
            }
        }
        else
        {
            sLineSeparator = sLineSeparator.replace("\\n", "\n").replace("\\r", "\r");
        }

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fSrcFile))
        {
            throw new FileException(LogMessages.SRC_FILE_ACCESS_NOT_ALLOWED);
        }

        if (!fSrcFile.exists())
        {
            throw new FileException(LogMessages.SRC_FILE_NOT_EXIST);
        }

        if (fSrcFile.isDirectory())
        {
            throw new FileException(LogMessages.SRC_FILE_IS_ADIRECTORY);
        }

        Reader rReader = null;
        int iLineCount;

        try
        {
            rReader = new FileReader(fSrcFile);
            iLineCount = countInputLines(rReader, sLineSeparator.toCharArray(), 2048);
        }
        catch (FileNotFoundException e)
        {
            throw new FileException(e,LogMessages.FILE_NOT_FOUND,fSrcFile);
        }
        catch (IOException e)
        {
            throw new FileException(e,LogMessages.UNABLE_TO_READ_FILE, fSrcFile);
        }
        finally
        {
            if (rReader != null)
            {
                try
                {
                    rReader.close();
                }
                catch (IOException ignored)
                {
                }
            }
        }

        // Add line count to the response
        req.addResponseElement("linecount", Integer.toString(iLineCount));

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
     * Counts the lines from the input reader data.
     *
     * @param   rReader            Data is read from this.
     * @param   caSeparatorChars   An array of line separator characters
     * @param   iReadBufferLength  Read buffer length to be used.
     *
     * @return  Number of lines encountered.
     *
     * @throws  IOException  Thrown if input could not be read.
     */
    protected static int countInputLines(Reader rReader, char[] caSeparatorChars,
                                         int iReadBufferLength)
                                  throws IOException
    {
        int iCurrentLine = 0;
        int iSepPos = 0;
        int iSepLength = caSeparatorChars.length;
        char[] caBuffer = new char[iReadBufferLength];
        int iRead;
        boolean bDataAfterEol = false;

        while ((iRead = rReader.read(caBuffer)) > 0)
        {
            for (int i = 0; i < iRead; i++)
            {
                if (caBuffer[i] == caSeparatorChars[iSepPos])
                {
                    iSepPos++;

                    if (iSepPos >= iSepLength)
                    {
                        iCurrentLine++;
                        iSepPos = 0;
                        bDataAfterEol = false;
                    }
                }
                else
                {
                    iSepPos = 0;
                    bDataAfterEol = true;
                }
            }
        }

        return bDataAfterEol ? (iCurrentLine + 1) : iCurrentLine;
    }
}

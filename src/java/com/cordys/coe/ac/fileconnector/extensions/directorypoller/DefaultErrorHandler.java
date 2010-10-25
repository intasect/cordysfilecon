
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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import com.eibus.management.IAlertDefinition;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

/**
 * Default error handler for the DirectoryPoller. This class moves the file to the error folder.
 *
 * @author  mpoyhone
 */
public class DefaultErrorHandler
    implements IFileErrorHandler
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DefaultErrorHandler.class);

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.IFileErrorHandler#handleFileError(com.cordys.coe.ac.fileconnector.extensions.directorypoller.IncomingFile,
     *       com.cordys.coe.ac.fileconnector.extensions.directorypoller.Folder,
     *       DirectoryPollerThread, java.lang.Throwable)
     */
    public void handleFileError(FileContext fileContext, DirectoryPollerThread poller,
                                Throwable error)
    {
        if (fileContext == null)
        {
            LOG.log(Severity.ERROR, "Internal error: File context object is not set.");
            return;
        }

        if (poller == null)
        {
            LOG.log(Severity.ERROR, "Internal error: Poller thread object is not set.");
            return;
        }

        try
        {
            JMXWrapperObject jmxWrapper = poller.getJmxWrapper();
            IAlertDefinition fileProcessingAlert = jmxWrapper.getFileProcessingAlert();

            if (fileProcessingAlert != null)
            {
                fileProcessingAlert.issueAlert(error, fileContext.getCurrentFile(),
                                               error.getMessage());
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "Unable to raise error alert.", e);
        }
    }
}

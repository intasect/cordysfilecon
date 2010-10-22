/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
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

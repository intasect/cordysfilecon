/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

/**
 * Interface for handling errors during file processing. The handler is responsible for moving the
 * file away from input or processing folder.
 *
 * @author  mpoyhone
 */
public interface IFileErrorHandler
{
    /**
     * Called when the error should be handled.
     *
     * @param  fileContext  File that ended in error.
     * @param  poller       The main poller thread object.
     * @param  error        Contains the exception that caused this error.
     */
    void handleFileError(FileContext fileContext, DirectoryPollerThread poller, Throwable error);
}

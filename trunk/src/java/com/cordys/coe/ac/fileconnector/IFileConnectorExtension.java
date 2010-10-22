/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;

/**
 * Interface for all FileConnector extensions. Each extension has a singleton object which is
 * created when the connector is started.
 *
 * @author  mpoyhone
 */
public interface IFileConnectorExtension
{
    /**
     * Called when the extension object is being cleaned up (removed from the connector).
     *
     * @throws  ConfigException
     * @throws  FileException
     */
    public void cleanup()
                 throws ConfigException, FileException;

    /**
     * Called when the extension object is loaded.
     *
     * @param   ecContext  Extension content for accessing the FileConnector functionality.
     * @param   acConfig   Contains the FileConnector configuration.
     *
     * @return  If <code>false</code> the method is not added to the connector.
     *
     * @throws  ConfigException
     * @throws  FileException
     */
    public boolean initialize(IExtensionContext ecContext, ApplicationConfiguration acConfig)
                       throws ConfigException, FileException;
}

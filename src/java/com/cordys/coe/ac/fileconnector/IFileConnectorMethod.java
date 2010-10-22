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
 * Interface for all FileConnector SOAP methods. Each method has a singleton object which is created
 * when the connector is started. The <code>process</code> method must be able to handle multiple
 * concurrent requests at the same time.
 *
 * @author  mpoyhone
 */
public interface IFileConnectorMethod
{
    /**
     * Called when the method object is being cleaned up (removed from the connector).
     *
     * @throws  ConfigException
     */
    public void cleanup()
                 throws ConfigException;

    /**
     * Called when the method object is loaded.
     *
     * @param   acConfig  Contains the FileConnector configuration.
     *
     * @return  If <code>false</code> the method is not added to the connector.
     *
     * @throws  ConfigException
     */
    public boolean initialize(ApplicationConfiguration acConfig)
                       throws ConfigException;

    /**
     * Called the the connector reset() method is called. The implementation can clean caches, if
     * needed.
     */
    public void onReset();

    /**
     * Called when a SOAP request needs to be processed.
     *
     * @param   req  Request context for this request.
     *
     * @return  Processing status enumeration value.
     *
     * @throws  FileException
     */
    public EResult process(ISoapRequestContext req)
                    throws FileException;

    /**
     * Returns method name. SOAP requests are mapped to these method using this name.
     *
     * @return  Method name.
     */
    public String getMethodName();

    /**
     * Enumeration for <code>process</code> method return values.
     *
     * @author  mpoyhone
     */
    public enum EResult
    {
        /**
         * SOAP request was processed successfully.
         */
        FINISHED,
        /**
         * SOAP request processing will continue in the background after the method returns.
         */
        BACKGROUND
    }
}

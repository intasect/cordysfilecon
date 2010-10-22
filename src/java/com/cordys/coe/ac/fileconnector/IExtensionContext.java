/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import com.eibus.management.IManagedComponent;

import java.io.File;

/**
 * Interface for extensions accessing the FileConnector functionality.
 *
 * @author  mpoyhone
 */
public interface IExtensionContext
{
    /**
     * Returns the installation folder.
     *
     * @return  Installation folder.
     */
    public File getInstallationFolder();

    /**
     * Returns the JMX component which can be used to register counters.
     *
     * @return  JMX Component of this connector.
     */
    public IManagedComponent getJmxComponent();

    /**
     * Returns the NOM connector for sending SOAP messages to the Cordys BCP bus.
     *
     * @return  NOM connector interface.
     */
    public INomConnector getNomConnector();

    /**
     * Returns the organizational user DN for the SYSTEM user. This user can be used the send SOAP
     * messages.
     *
     * @return  Organization SYSTEM user DN.
     */
    public String getOrganizationalSystemUserDn();

    /**
     * Returns the organization DN under which the FileConnector is running.
     *
     * @return  Current organization DN.
     */
    public String getOrganizationDn();
}

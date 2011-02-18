
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

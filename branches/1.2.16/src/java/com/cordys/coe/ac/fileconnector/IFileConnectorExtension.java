
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

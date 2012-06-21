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
 package com.cordys.coe.ac.fileconnector.exception;

import com.cordys.coe.exception.ServerLocalizableException;
import com.eibus.localization.IStringResource;

/**
 * Exception object for ConfigObject exceptions.
 *
 * @author  mpoyhone
 */
public class ConfigException extends ServerLocalizableException
{
		private static final long serialVersionUID = 4756606220375079175L;

		
    /**
     * Creates a new configuration exception object.
     *
     * @param  message  The error message.
     */
    public ConfigException(IStringResource message,Object... parameters)
    {
        super(message,parameters);
    }

    /**
     * Creates a new configuration exception object.
     *
     * @param  message  The error message.
     */
    public ConfigException(IStringResource message)
    {
        super(message);
    }
    
    /**
     * Creates a new configuration exception object.
     *
     * @param  message  The error message.
     * @param  cause    Causing exception for this error.
     */
    public ConfigException(Throwable cause,IStringResource message,Object... parameters)
    {
        super(cause,message,parameters);
    }
    
    /**
     * Creates a new configuration exception object.
     *
     * @param  message  The error message.
     * @param  cause    Causing exception for this error.
     */
    public ConfigException(Throwable cause,IStringResource message)
    {
        super(cause,message);
    }
}

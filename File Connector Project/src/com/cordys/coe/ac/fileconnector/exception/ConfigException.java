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

/**
 * Exception object for ConfigObject exceptions.
 *
 * @author  mpoyhone
 */
public class ConfigException extends Exception
{
    /**
     * Creates a new configuration exception object.
     */
    public ConfigException()
    {
        super();
    }

    /**
     * Creates a new configuration exception object.
     *
     * @param  message  The error message.
     */
    public ConfigException(String message)
    {
        super(message);
    }

    /**
     * Creates a new configuration exception object.
     *
     * @param  cause  Causing exception for this error.
     */
    public ConfigException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new configuration exception object.
     *
     * @param  message  The error message.
     * @param  cause    Causing exception for this error.
     */
    public ConfigException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

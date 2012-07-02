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
 * The exception class for RecordValidator.
 *
 * @author  mpoyhone
 */
public class ValidationException extends ServerLocalizableException
{
    /**
     * Creates a new FileException object.
     */
/*    public ValidationException()
    {
        super();
    }*/

    /**
     * Creates a new exception object.
     *
     * @param  cause  The original exception
     */
    /*public ValidationException(Throwable cause)
    {
        super(cause);
    }*/

    /**
   * 
   */
  private static final long serialVersionUID = -5520628923008321024L;

		/**
     * Creates a new exception object.
     *
     * @param  message  Exception message
     */
    public ValidationException(IStringResource message,Object... parameters)
    {
        super(message,parameters);
    }

    /**
     * Creates a new exception object.
     *
     * @param  message  Exception message
     * @param  cause    The original exception
     */
    public ValidationException(Throwable cause,IStringResource message,Object... parameters)
    {
        super(cause,message,parameters);
    }
}

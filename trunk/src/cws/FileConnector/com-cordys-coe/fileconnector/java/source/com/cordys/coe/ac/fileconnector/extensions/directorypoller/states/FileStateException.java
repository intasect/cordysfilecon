
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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import com.cordys.coe.exception.ServerLocalizableException;
import com.eibus.localization.IStringResource;

/**
 * Exception class for file states. This will contain state retry information.
 *
 * @author  mpoyhone
 */
public class FileStateException extends ServerLocalizableException
{
    /**
   * 
   */
  private static final long serialVersionUID = 3213245847373554775L;
		/**
     * Contains the exception type.
     */
    private EType type;

    /**
     * Constructor for FileStateException.
     *
     * @param  type  Exception type.
     */
/*    public FileStateException(EType type)
    {
        super();
        this.type = type;
    }*/

    /**
     * Constructor for FileStateException.
     *
     * @param  type     Exception type.
     * @param  message  Exception message.
     */
    public FileStateException(EType type, IStringResource message,Object... parameters)
    {
        super(message,parameters);
        this.type = type;
    }

    /**
     * Constructor for FileStateException.
     *
     * @param  type   Exception type.
     * @param  cause  Causing exception.
     */
/*    public FileStateException(EType type, Throwable cause)
    {
        super(cause);
        this.type = type;
    }*/

    /**
     * Constructor for FileStateException.
     *
     * @param  type     Exception type.
     * @param  message  Exception message.
     * @param  cause    Causing exception.
     */
    public FileStateException(Throwable cause,EType type, IStringResource message,Object... parameters)
    {
        super(cause,message,parameters);
        this.type = type;
    }

    /**
     * @see  java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage()
    {
        String msg = super.getMessage();

        if (type == EType.INTERNAL)
        {
            msg = "Internal Error: " + msg;
        }

        return msg;
    }

    /**
     * Returns the type.
     *
     * @return  Returns the type.
     */
    public EType getType()
    {
        return type;
    }

    /**
     * Exception type.
     *
     * @author  mpoyhone
     */
    public enum EType
    {
        /**
         * State processing cannot be resumed again and the
         * file should be moved to the error folder.
         */
        ABORT,
        /**
         * State processing cannot be resumed again because of an
         * internal error. File should be moved to the error folder..
         */
        INTERNAL,
        /**
         * State processing can be retried later.
         */
        RETRY,
        /**
         * State processing can be retried later. The retrying of all other
         * files from this input folder should be blocked until this
         * file has been processed.
         */
        RETRY_BLOCK_INPUT;
    }
}

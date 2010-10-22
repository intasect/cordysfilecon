/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

/**
 * Exception class for file states. This will contain state retry information.
 *
 * @author  mpoyhone
 */
public class FileStateException extends Exception
{
    /**
     * Contains the exception type.
     */
    private EType type;

    /**
     * Constructor for FileStateException.
     *
     * @param  type  Exception type.
     */
    public FileStateException(EType type)
    {
        super();
        this.type = type;
    }

    /**
     * Constructor for FileStateException.
     *
     * @param  type     Exception type.
     * @param  message  Exception message.
     */
    public FileStateException(EType type, String message)
    {
        super(message);
        this.type = type;
    }

    /**
     * Constructor for FileStateException.
     *
     * @param  type   Exception type.
     * @param  cause  Causing exception.
     */
    public FileStateException(EType type, Throwable cause)
    {
        super(cause);
        this.type = type;
    }

    /**
     * Constructor for FileStateException.
     *
     * @param  type     Exception type.
     * @param  message  Exception message.
     * @param  cause    Causing exception.
     */
    public FileStateException(EType type, String message, Throwable cause)
    {
        super(message, cause);
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

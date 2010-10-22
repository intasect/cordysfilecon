package com.cordys.coe.ac.fileconnector.exception;

/**
 * The exception class for FileConnector.
 *
 * @author  mpoyhone
 */
public class FileException extends Exception
{
    /**
     * Creates a new FileException object.
     */
    public FileException()
    {
        super();
    }

    /**
     * Creates a new exception object.
     *
     * @param  cause  The original exception
     */
    public FileException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception object.
     *
     * @param  msg  Exception message
     */
    public FileException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new exception object.
     *
     * @param  message  Exception message
     * @param  cause    The original exception
     */
    public FileException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

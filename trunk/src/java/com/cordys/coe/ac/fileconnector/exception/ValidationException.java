package com.cordys.coe.ac.fileconnector.exception;

/**
 * The exception class for RecordValidator.
 *
 * @author  mpoyhone
 */
public class ValidationException extends Exception
{
    /**
     * Creates a new FileException object.
     */
    public ValidationException()
    {
        super();
    }

    /**
     * Creates a new exception object.
     *
     * @param  cause  The original exception
     */
    public ValidationException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates a new exception object.
     *
     * @param  msg  Exception message
     */
    public ValidationException(String msg)
    {
        super(msg);
    }

    /**
     * Creates a new exception object.
     *
     * @param  message  Exception message
     * @param  cause    The original exception
     */
    public ValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

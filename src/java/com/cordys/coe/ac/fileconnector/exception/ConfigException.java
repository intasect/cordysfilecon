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

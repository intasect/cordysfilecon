package com.cordys.coe.ac.fileconnector.validator;

import com.cordys.coe.ac.fileconnector.exception.ConfigException;

import java.util.HashMap;
import java.util.Map;

/**
 * The record validator configuration container. Used for caching the configuration.
 *
 * @author  mpoyhone
 */
public class ValidatorConfig
{
    /**
     * A map from file type name to RecordValidator.FileType objects.
     */
    public Map<String, RecordValidator.FileType> mConfigMap = new HashMap<String, RecordValidator.FileType>();

    /**
     * Parsed the given configuration.
     *
     * @param   iNewFileNode  The XML structure loaded from XML store.
     *
     * @throws  ConfigException  DOCUMENTME
     */
    public ValidatorConfig(int iNewFileNode)
                    throws ConfigException
    {
        // Clear the old configuration, if any.
        mConfigMap = new HashMap<String, RecordValidator.FileType>();

        // Parse the configuration into the mConfigMap.
        RecordValidator.createConfigration(this, iNewFileNode);
    }
}

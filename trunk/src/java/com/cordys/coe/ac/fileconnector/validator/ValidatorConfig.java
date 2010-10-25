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

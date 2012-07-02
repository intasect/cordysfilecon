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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import com.cordys.coe.ac.fileconnector.exception.FileException;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Find;

import java.io.File;

import java.util.HashMap;
import java.util.Map;

/**
 * This class wraps around the folder configuration for the directory connector.
 *
 * @author  mpoyhone, pgussow
 */
public class FolderConfiguration
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(FolderConfiguration.class);
    /**
     * Contains all configured folders.
     */
    private Folder[] folders;
    /**
     * Maps folders by the logical name.
     */
    private Map<String, Folder> nameToFolderMap = new HashMap<String, Folder>();

    /**
     * Creates a new FolderConfiguration object.
     *
     * @param   configXml   Configuration XML.
     * @param   fRelFolder  Parent folder for relative paths.
     * @param   defaultUserDn 	The UserDn to use when no UserNn is specified in config file
     * @throws  FileException
     */
    public FolderConfiguration(int configXml, File fRelFolder, String defaultUserDn)
                        throws FileException
    {
        folders = parseConfiguration(configXml, fRelFolder, defaultUserDn);

        for (Folder f : folders)
        {
            if (nameToFolderMap.containsKey(f.getName()))
            {
                throw new FileException(LogMessages.FOLDER_IS_ALREADY_DEFINED,f.getName());
            }

            nameToFolderMap.put(f.getName(), f);
        }
    }

    /**
     * Returns folder by the locical name.
     *
     * @param   name  Logical folder name.
     *
     * @return  Folder object or <code>null</code> if no folder was found.
     */
    public Folder getFolderByName(String name)
    {
        return nameToFolderMap.get(name);
    }

    /**
     * Returns all configured folders.
     *
     * @return
     */
    public Folder[] getFolders()
    {
        return folders;
    }

    /**
     * This method returns an array of currently configured folders.
     *
     * @param   configXml   Configuration XML.
     * @param   fRelFolder  Parent folder for relative paths.
     * @param   defaultUserDn The UserDn to use when no UserDn is specified in config file
     * @return  The folders.
     *
     * @throws  FileException
     */
    private Folder[] parseConfiguration(int configXml, File fRelFolder, String defaultUserDn)
                                 throws FileException
    {
        int[] aiFolders = Find.match(configXml, "fChild<folder>");
        Folder[] res = new Folder[aiFolders.length];

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Found " + aiFolders.length + " folders.");
        }

        // Parse the folders into an array.
        for (int iCount = 0; iCount < aiFolders.length; iCount++)
        {
            int iFolderNode = aiFolders[iCount];
            Folder fTemp = new Folder(iFolderNode, fRelFolder, defaultUserDn);

            res[iCount] = fTemp;
        }

        return res;
    }
}

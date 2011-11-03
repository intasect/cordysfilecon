
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
 package com.cordys.coe.ac.fileconnector.extensions.drivemap;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IExtensionContext;
import com.cordys.coe.ac.fileconnector.IFileConnectorExtension;
import com.cordys.coe.ac.fileconnector.LogMessages;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.util.cmdline.CmdLineException;
import com.cordys.coe.util.win32.NetworkDrive;

import com.eibus.localization.StringFormatter;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;

/**
 * DriveMap extension for FileConnector.
 *
 * @author  mpoyhone
 */
public class DriveMap
    implements IFileConnectorExtension
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DriveMap.class);

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorExtension#cleanup()
     */
    public void cleanup()
                 throws ConfigException, FileException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorExtension#initialize(com.cordys.coe.ac.fileconnector.IExtensionContext,
     *       com.cordys.coe.ac.fileconnector.ApplicationConfiguration)
     */
    public boolean initialize(IExtensionContext ecContext, ApplicationConfiguration acConfig)
                       throws ConfigException, FileException
    {
        if (LOG.isDebugEnabled())
        {
            try
            {
                StringBuffer sbTemp = new StringBuffer("Network mappings BEFORE\n");
                NetworkDrive[] and = NetworkDrive.getNetworkMappings();

                for (NetworkDrive nd : and)
                {
                    sbTemp.append(nd.toString());
                    sbTemp.append("\n");
                }

                if (LOG.isDebugEnabled())
                {
                    LOG.debug(sbTemp.toString());
                }
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error getting all current drive mappings", e);
                }
            }
        }

        NetworkDrive[] andMappings = acConfig.getNetworkDrives();

        if (andMappings.length == 0)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No drive mappings defined.");
            }

            return false;
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Going to map " + andMappings.length + " network drives.");
        }

        for (int iCount = 0; iCount < andMappings.length; iCount++)
        {
            NetworkDrive ndDrive = andMappings[iCount];

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Creating network drive for " + ndDrive.getLocation() + " with user " +
                          ndDrive.getUsername() + ".");
            }

            try
            {
                ndDrive.recreate();
            }
            catch (Exception e)
            {
                if (LOG.isEnabled(Severity.ERROR))
                {
                    LOG.error(e, LogMessages.UNABLE_TO_CREATE_MAPPING,
                              new Object[] { ndDrive.getLocation() });
                }

                String errorMessage = StringFormatter.format(EIBProperties.get_ManagementLocale(),
                                                             LogMessages.UNABLE_TO_CREATE_MAPPING,
                                                             new Object[]
                                                             {
                                                                 ndDrive.getLocation()
                                                             });
                throw new ConfigException(errorMessage);
            }
        }

        if (LOG.isDebugEnabled())
        {
            try
            {
                StringBuffer networkDrivesString = new StringBuffer("");
                NetworkDrive[] nd = NetworkDrive.getNetworkMappings();

                for (int i = 0; i < nd.length; i++)
                {
                    networkDrivesString.append(nd[i].toString());
                }

                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Current Network mapings : " + networkDrivesString.toString());
                }
            }
            catch (CmdLineException e)
            {
            }
        }

        return true;
    }
}

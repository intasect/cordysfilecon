
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

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IExtensionContext;
import com.cordys.coe.ac.fileconnector.IFileConnectorExtension;
import com.cordys.coe.ac.fileconnector.INomConnector;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xmlstore.XMLStoreWrapper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;

/**
 * FileConnector extension class for the directory poller. This code is copied from the
 * DirectoryConnector project.
 *
 * @author  mpoyhone
 */
public class DirectoryPoller
    implements IFileConnectorExtension
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DirectoryPoller.class);
    /**
     * Contains the default folder poll interval in seconds.
     */
    private static final long DEFAULT_POLL_INTERVAL = 10L;
    /**
     * Minimum number of worker threads in the thread pool.
     */
    public static final int DEFAULT_WORKER_MIN_THREAD_COUNT = 1;
    /**
     * Maximum number of worker threads in the thread pool.
     */
    public static final int DEFAULT_WORKER_MAX_THREAD_COUNT = 10;
    /**
     * Contains the parsed configuration.
     */
    private FolderConfiguration configuration;
    /**
     * Contains the poller thread.
     */
    private DirectoryPollerThread pollerThread;

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorExtension#cleanup()
     */
    public void cleanup()
                 throws ConfigException
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Cleaning up DirectoryPoller.");
        }

        if (pollerThread != null)
        {
            pollerThread.terminate(true);
        }

        if (configuration != null)
        {
            Folder[] folders = configuration.getFolders();

            if ((folders != null) && (folders.length > 0))
            {
                for (Folder f : folders)
                {
                    f.cleanup();
                }
            }
            configuration = null;
        }
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
            LOG.debug("Initializing DirectoryPoller");
        }

        int configRoot = acConfig.getConfigurationNode();
        int pollerConfig = (configRoot != 0)
                           ? Find.firstMatch(configRoot, "<><component name=\"Directory Poller\">")
                           : 0;

        if (pollerConfig == 0)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("No DirectoryPoller configuration found.");
            }

            return false;
        }

        boolean configEnabled = XmlUtils.getBooleanParameter(pollerConfig, "enabled");

        if (!configEnabled)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("DirectoryPoller configuration is not enabled.");
            }

            return false;
        }

        String configFile = XmlUtils.getStringParameter(pollerConfig, "configuration-file", true);
        String processingFolderPath = XmlUtils.getStringParameter(pollerConfig, "processing-folder",
                                                                  true);
        String appProcessingFolderPath = XmlUtils.getStringParameter(pollerConfig,
                                                                     "app-processing-folder",
                                                                     false);
        String errorFolderPath = XmlUtils.getStringParameter(pollerConfig, "error-folder", true);
        long pollInterval = (long) (1000 *
                                    XmlUtils.getDoubleParameter(pollerConfig, "poll-interval",
                                                                DEFAULT_POLL_INTERVAL));
        int minWorkerThreads = (int) XmlUtils.getLongParameter(pollerConfig, "minConcurrentWorkers",
                                                               DEFAULT_WORKER_MIN_THREAD_COUNT);
        int maxWorkerThreads = (int) XmlUtils.getLongParameter(pollerConfig, "maxConcurrentWorkers",
                                                               DEFAULT_WORKER_MAX_THREAD_COUNT);
        String defaultUserDn = XmlUtils.getStringParameter(pollerConfig, "defaultUserDn", false);        

        configuration = getConfiguration(ecContext.getNomConnector(),
                                         ecContext.getInstallationFolder(),
                                         ecContext.getOrganizationalSystemUserDn(), configFile,
                                         defaultUserDn);

        Folder[] folders = configuration.getFolders();

        if ((folders == null) || (folders.length == 0))
        {
            throw new ConfigException(LogMessages.NO_DIR_SPECIFIED_FOR_POLLER);
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Going to start the poller thread, because there are input folders.");
        }

        File processingFolder = GeneralUtils.getAbsoluteFile(processingFolderPath,
                                                             ecContext.getInstallationFolder());
        File errorFolder = GeneralUtils.getAbsoluteFile(errorFolderPath,
                                                        ecContext.getInstallationFolder());
        File appProcessingFolder = null;

        if ((appProcessingFolderPath != null) && (appProcessingFolderPath.length() > 0))
        {
            appProcessingFolder = GeneralUtils.getAbsoluteFile(appProcessingFolderPath,
                                                               ecContext.getInstallationFolder());
        }

        pollerThread = new DirectoryPollerThread(acConfig, configuration, pollInterval,
                                                 ecContext.getNomConnector(), processingFolder,
                                                 errorFolder, appProcessingFolder, minWorkerThreads,
                                                 maxWorkerThreads, ecContext.getJmxComponent());

        Thread tPoller = new Thread(pollerThread);
        tPoller.start();

        return true;
    }

    /**
     * Sets the poller configuration object.
     *
     * @param  config  Configuration.
     */
    public void setConfiguration(FolderConfiguration config)
    {
        this.configuration = config;
    }

    /**
     * Returns poller configuration from XMLStore.
     *
     * @param   nomConnector           NOM connector object.
     * @param   relFolder              Root folder for relative paths.
     * @param   userDn                 User DN.
     * @param   configurationFilePath  Configuration file XMLStore path.
     * @param 	defaultUserDn		   The UserDn to use when no UserDn is specified in config file
     * @return  Configuration object.
     *
     * @throws  ConfigException  Thrown if the operation failed.
     */
    private FolderConfiguration getConfiguration(INomConnector nomConnector, File relFolder,
                                                 String userDn, String configurationFilePath,
                                                 String defaultUserDn)
                                          throws ConfigException
    {
        if (configuration != null)
        {
            return configuration;
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Loading DirectoryPoller configuration from XMLStore path: " +
                      configurationFilePath);
        }

        // Get configuration file name
        if ((configurationFilePath == null) || configurationFilePath.equals(""))
        {
            throw new ConfigException(LogMessages.CONFIGURATION_FILE_NOT_SET);
        }

        // Set the XMLStore SOAP connection information.
        SOAPWrapper swSoap = new SOAPWrapper(nomConnector.getNomConnector());

        swSoap.setUser(userDn);

        // This reads the configuration from XMLStore and parses it.
        FolderConfiguration result = null;

        try
        {
            XMLStoreWrapper xmlStoreWrapper = new XMLStoreWrapper(swSoap);
            int configNode;

            // Get the file from XMLStore.
            configNode = xmlStoreWrapper.getXMLObject(configurationFilePath);

            // Find the actual file node from the response.
            if ((configNode != 0) && (Node.getNumChildren(configNode) > 0))
            {
                // Get the response node
                configNode = Find.firstMatch(configNode, "?<tuple><old><>");

                // The check that the response is valid.
                if (configNode == 0)
                {
                    // No it was not.
                    throw new ConfigException(LogMessages.INVALID_RESPONSE_RECIVED);
                }
            }

            // Check if we have a file node.
            if (configNode == 0)
            {
                throw new ConfigException(LogMessages.DIRECTORYPOLLER_CONFIG_FILE_NOT_FOUND,
                                          configurationFilePath);
            }

            result = new FolderConfiguration(configNode, relFolder, defaultUserDn);
        }
        catch (Exception e)
        {
            throw new ConfigException(e,LogMessages.UNABLE_TO_LOAD_CONFIGURATION);
        }
        finally
        {
            swSoap.freeXMLNodes();
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Configuration loaded successfully.");
        }

        return result;
    }
}

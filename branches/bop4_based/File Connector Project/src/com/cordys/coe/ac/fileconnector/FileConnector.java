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
package com.cordys.coe.ac.fileconnector;

import com.cordys.coe.ac.fileconnector.charset.ascii.AsciiCharsetProvider;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.DirectoryPoller;
import com.cordys.coe.ac.fileconnector.extensions.drivemap.DriveMap;
import com.cordys.coe.ac.fileconnector.methods.CopyFileMethod;
import com.cordys.coe.ac.fileconnector.methods.CountNumberOfLinesMethod;
import com.cordys.coe.ac.fileconnector.methods.CreateDirectoryMethod;
import com.cordys.coe.ac.fileconnector.methods.DeleteFileMethod;
import com.cordys.coe.ac.fileconnector.methods.GetListOfFilesMethod;
import com.cordys.coe.ac.fileconnector.methods.MoveFileMethod;
import com.cordys.coe.ac.fileconnector.methods.ReadFileMethod;
import com.cordys.coe.ac.fileconnector.methods.ReadFileRecordsMethod;
import com.cordys.coe.ac.fileconnector.methods.ReadLargeXmlFileRecordsMethod;
import com.cordys.coe.ac.fileconnector.methods.ReadXmlFileRecordsMethod;
import com.cordys.coe.ac.fileconnector.methods.SelectAndMoveFileMethod;
import com.cordys.coe.ac.fileconnector.methods.WriteFileMethod;
import com.cordys.coe.ac.fileconnector.methods.WriteFileRecordsMethod;
import com.cordys.coe.coelib.LibraryVersion;
import com.cordys.coe.util.general.Util;
import com.cordys.coe.util.soap.SOAPException;
import com.cordys.coe.util.soap.SoapFaultInfo;

import com.eibus.connector.nom.Connector;
import com.eibus.connector.nom.SOAPMessage;

import com.eibus.directory.soap.DirectoryException;

import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;

import com.eibus.localization.ILocalizableString;

import com.eibus.management.IManagedComponent;

import com.eibus.soap.ApplicationConnector;
import com.eibus.soap.ApplicationTransaction;
import com.eibus.soap.Processor;
import com.eibus.soap.SOAPTransaction;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.util.system.EIBProperties;

import com.eibus.xml.nom.Node;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The file application connector class.
 *
 * @author  mpoyhone
 */
public class FileConnector extends ApplicationConnector
{
    /**
     * Identifies the Logger.
     */
    private static CordysLogger LOG = CordysLogger.getCordysLogger(FileConnector.class);
    /**
     * Contains all configured method classes.
     */
    private static final Class<?>[] methodClasses =
    {
        CopyFileMethod.class, CountNumberOfLinesMethod.class, CreateDirectoryMethod.class, DeleteFileMethod.class,
        GetListOfFilesMethod.class, MoveFileMethod.class, ReadFileMethod.class, ReadFileRecordsMethod.class,
        ReadXmlFileRecordsMethod.class, ReadLargeXmlFileRecordsMethod.class, SelectAndMoveFileMethod.class,
        WriteFileMethod.class, WriteFileRecordsMethod.class,
    };

    /**
     * Contains all configured extension classes.
     */
    private static final Class<?>[] extensionClasses = { DriveMap.class, DirectoryPoller.class, };
    /**
     * Application connector configuration object.
     */
    private ApplicationConfiguration acConfig;
    /**
     * The connector object for SOAP connections.
     */
    private Connector cConnector;
    /**
     * Contains FileConnector extension objects.
     */
    private List<IFileConnectorExtension> extensionList = new ArrayList<IFileConnectorExtension>(10);
    /**
     * Contains the JMX component for this connector.
     */
    private IManagedComponent jmxComponent;
    /**
     * Contains SOAP method objects mapped by their name.
     */
    private Map<String, IFileConnectorMethod> methodMap = new HashMap<String, IFileConnectorMethod>();

    /**
     * Closes the connector object.
     *
     * @param  pProcessor
     */
    @Override public void close(Processor pProcessor)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Processor close.");
        }

        cleanupSoapMethods();
        cleanupExtensions();

        // Close the connector object
        if ((cConnector != null) && cConnector.isOpen())
        {
            cConnector.close();
        }
        cConnector = null;
    }

    /**
     * Creates the transaction object used with this request.
     *
     * @param   soapTransaction  The incoming SOAP transaction
     *
     * @return  The created transaction object
     */
    @Override public ApplicationTransaction createTransaction(SOAPTransaction soapTransaction)
    {
        if (cConnector == null)
        {
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(null, LogMessages.NO_CONNECTOR_FOUND);
            }

            return null;
        }

        if (acConfig == null)
        {
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(null, LogMessages.NO_CONFIGURATION_FOUND);
            }

            return null;
        }

        return new FileTransaction(this, cConnector, soapTransaction.getUserCredentials(), acConfig);
    }

    /**
     * Called when the connector is being initialized.
     *
     * @param  processor  The SOAP processor object
     */
    @Override public void open(Processor processor)
    {
        // Check the CoELib version.
        try
        {
            LibraryVersion.loadAndCheckLibraryVersionFromResource(this.getClass(), true);
        }
        catch (Exception e)
        {
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(e, LogMessages.COE_LIB_VERSION_INVALID);
            }

            throw new IllegalStateException(e.toString());
        }

        // Send a debug message to the Event Service
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Executing open-method.");
        }

        // Check if the coe.connector.startup.delay is set to an int bigger then 0.
        // If so we will sleep to allow attaching of the debugger.
        String sTemp = System.getProperty("coe.connector.startup.delay");

        if ((sTemp != null) && (sTemp.length() > 0))
        {
            try
            {
                long lTime = Long.parseLong(sTemp);

                if (lTime > 0)
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Going to pause for " + lTime + " ms to allow debugger attachment.");
                    }
                    Thread.sleep(lTime);
                }
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Error checking for debugger delay", e);
                }
            }
        }

        // Create a new connector object based on the SOAP
        // processor connector.
        cConnector = null;

        try
        {
            cConnector = Connector.getInstance(this.getClass().getName() + " Connector");

            if (!cConnector.isOpen())
            {
                cConnector.open();
            }
        }
        catch (Exception e)
        {
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(null, LogMessages.UNABLE_TO_OPEN_CONNECTOR);
            }
        }

        // Fetch the application connector configuration.
        try
        {
            acConfig = new ApplicationConfiguration(getConfiguration());
        }
        catch (Exception e)
        {
            String errorMsg = "Unable to get the configuration element. Exception " + Util.getStackTrace(e);

            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(e, LogMessages.UNABLE_TO_GET_CONFIGURATION_ELEMENT);
            }
            throw new IllegalStateException(errorMsg);
        }

        try
        {
            // Load the custom character set provider.
            AsciiCharsetProvider cpCustomProvider = new AsciiCharsetProvider(AsciiCharsetProvider.PROPERTY_FILE_NAME);

            acConfig.setCustomCharsetProvider(cpCustomProvider);
        }
        catch (Exception e)
        {
            if (LOG.isEnabled(Severity.ERROR))
            {
                LOG.error(e, LogMessages.UNABLE_TO_LOAD_CHARSET);
            }
        }

        try
        {
            loadExtensions();
            loadSoapMethods();
        }
        catch (RuntimeException e)
        {
            LOG.log(Severity.ERROR, "Unable to initialize extensions: " + e, e);

            // Just throw it.
            throw e;
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "Unable to initialize extensions: " + e, e);

            // Wrap the exception.
            String msg = e.getMessage();

            if (msg != null)
            {
                throw new IllegalStateException(msg, e);
            }
            else
            {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * This code will be executed when a SOAP Processor is requested to reset.
     *
     * @param  procesor  The SOAP processor object
     */
    @Override public void reset(Processor procesor)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Processor reset.");
        }

        for (IFileConnectorMethod method : methodMap.values())
        {
            try
            {
                method.onReset();
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("SOAP method " + method.getMethodName() + " reset failed.", e);
                }
            }
        }
    }

    /**
     * Returns the connector installation folder.
     *
     * @return
     */
    public File getConnectorInstallationFolder()
    {
        String sCordysDir = EIBProperties.getInstallDir();
        File fCordysDir;

        if (sCordysDir != null)
        {
            fCordysDir = new File(sCordysDir);
        }
        else
        {
            throw new IllegalStateException("Cordys installation folder could not be determined.");
        }
        return new File(fCordysDir, "coe/fileconnector");
    }

    /**
     * Returns a SOAP method implementation object from the configured methods.
     *
     * @param   name  Method name.
     *
     * @return  Implementation object.
     */
    public IFileConnectorMethod getMethod(String name)
    {
        return methodMap.get(name);
    }

    /**
     * Returns NOM connector for SOAP messaging.
     *
     * @return  NOM connector instance.
     */
    public Connector getNomConnector()
    {
        return cConnector;
    }

    /**
     * Returns DN of the SYSTEM user in the current organization.
     *
     * @return
     */
    public String getOrganizationalSystemUser()
    {
        String orgDn = getProcessor().getOrganization();

        return "cn=SYSTEM,cn=organizational users," + orgDn;
    }

    /**
     * @see  com.eibus.soap.ApplicationConnector#createManagedComponent()
     */
    @Override protected IManagedComponent createManagedComponent()
    {
        jmxComponent = super.createManagedComponent();

        return jmxComponent;
    }

    /**
     * @see  com.eibus.soap.ApplicationConnector#getManagementDescription()
     */
    @Override protected ILocalizableString getManagementDescription()
    {
        return LogMessages.CONNECTOR_MANAGEMENT_DESCRIPTION;
    }

    /**
     * @see  com.eibus.soap.ApplicationConnector#getManagementName()
     */
    @Override protected String getManagementName()
    {
        return "File Connector";
    }

    /**
     * Cleans up the loaded extensions.
     */
    private void cleanupExtensions()
    {
        for (IFileConnectorExtension ext : extensionList)
        {
            try
            {
                ext.cleanup();
            }
            catch (Exception e)
            {
                LOG.log(Severity.WARN, "Extension cleanup failed: " + ext.getClass().getName(), e);
            }
        }
    }

    /**
     * Cleans up the loaded SOAP methods.
     */
    private void cleanupSoapMethods()
    {
        for (IFileConnectorMethod method : methodMap.values())
        {
            try
            {
                method.cleanup();
            }
            catch (Exception e)
            {
                LOG.log(Severity.WARN, "SOAP method " + method.getMethodName() + " cleanup failed.", e);
            }
        }
    }

    /**
     * Loads extensions.
     *
     * @throws  FileException
     * @throws  ConfigException
     */
    private void loadExtensions()
                         throws FileException, ConfigException
    {
        IExtensionContext ctx = new IExtensionContext()
        {
            public INomConnector getNomConnector()
            {
                return new NomConnector();
            }

            public String getOrganizationDn()
            {
                return getOrganizationDn();
            }

            public String getOrganizationalSystemUserDn()
            {
                return getOrganizationalSystemUser();
            }

            public File getInstallationFolder()
            {
                return getConnectorInstallationFolder();
            }

            public IManagedComponent getJmxComponent()
            {
                return jmxComponent;
            }
        };

        for (Class<?> extensionClass : extensionClasses)
        {
            Object obj = null;

            try
            {
                obj = extensionClass.newInstance();
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Unable to load extension class: " + extensionClass.getName());
            }

            if (!(obj instanceof IFileConnectorExtension))
            {
                throw new IllegalStateException("Extension class: " + extensionClass.getName() +
                                                " does not implement interface " +
                                                IFileConnectorExtension.class.getName());
            }

            IFileConnectorExtension ext = (IFileConnectorExtension) obj;

            try
            {
                if (ext.initialize(ctx, acConfig))
                {
                    extensionList.add(ext);

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Loaded extension: " + extensionClass.getName());
                    }
                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Extension is not loaded: " + extensionClass.getName());
                    }
                }
            }
            catch (FileException e)
            {
                throw e;
            }
            catch (ConfigException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                String msg = "Extension initialization failed " + extensionClass.getName();

                LOG.log(Severity.WARN, msg, e);
                throw new IllegalStateException(msg, e);
            }
        }
    }

    /**
     * Loads SOAP methods.
     *
     * @throws  ConfigException
     */
    private void loadSoapMethods()
                          throws ConfigException
    {
        for (Class<?> methodClass : methodClasses)
        {
            Object obj = null;

            try
            {
                obj = methodClass.newInstance();
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Unable to load SOAP method class: " + methodClass.getName());
            }

            if (!(obj instanceof IFileConnectorMethod))
            {
                throw new IllegalStateException("SOAP method class: " + methodClass.getName() +
                                                " does not implement interface " +
                                                IFileConnectorMethod.class.getName());
            }

            IFileConnectorMethod method = (IFileConnectorMethod) obj;

            try
            {
                if (method.initialize(acConfig))
                {
                    methodMap.put(method.getMethodName(), method);
                }
                else
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("SOAP method is not loaded: " + methodClass.getName());
                    }
                }
            }
            catch (ConfigException e)
            {
                throw e;
            }
            catch (Exception e)
            {
                LOG.log(Severity.WARN, "SOAP method " + method.getMethodName() + " initialization failed.", e);

                throw new IllegalStateException("SOAP method " + method.getMethodName() + " initialization failed.", e);
            }
        }
    }

    /**
     * Wrapper for the NOM Connector class for SOAP messaging.
     *
     * @author  mpoyhone
     */
    private class NomConnector
        implements INomConnector
    {
        /**
         * @see  com.cordys.coe.ac.fileconnector.INomConnector#createSoapMethod(java.lang.String,java.lang.String,
         *       java.lang.String, java.lang.String)
         */
        public int createSoapMethod(String organization, String orgUser, String methodName, String namespace)
                             throws DirectoryException
        {
            return cConnector.createSOAPMethod(orgUser, organization, namespace, methodName);
        }

        /**
         * @see  com.cordys.coe.ac.fileconnector.INomConnector#sendAndWait(int, boolean)
         */
        public int sendAndWait(int requestMethodNode, boolean checkSoapFault)
                        throws TimeoutException, ExceptionGroup, SOAPException
        {
            int res = cConnector.sendAndWait(Node.getRoot(requestMethodNode));

            if (checkSoapFault)
            {
                SoapFaultInfo faultInfo = SoapFaultInfo.findSoapFault(res);

                if (faultInfo != null)
                {
                    String msg = faultInfo.toString();

                    Node.delete(Node.getRoot(res));
                    res = 0;

                    throw new SOAPException(msg);
                }
            }

            int iBody = SOAPMessage.createBodyNode(res);
            int iMethod = (iBody != 0) ? Node.getFirstElement(iBody) : 0;

            if (iMethod == 0)
            {
                Node.delete(Node.getRoot(res));
                throw new SOAPException("SOAP method not found from the response.");
            }

            return iMethod;
        }

        /**
         * @see  com.cordys.coe.ac.fileconnector.INomConnector#getNomConnector() 
         */
        public Connector getNomConnector()
        {
            return cConnector;
        }
    }
}

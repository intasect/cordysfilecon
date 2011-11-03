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
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;
import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.XMLProperties;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.FilenameFilter;

import java.util.regex.Pattern;

/**
 * This class wraps around a configuration folder.
 *
 * @author  mpoyhone, pgussow
 */
public class Folder
{
    /**
     * If <code>true</code> the trigger SOAP message can be sent even if it was sent earlier and it
     * failed. The web service must be able to recognize duplicate request for the same file.
     */
    private boolean canRetry;
    /**
     * The actual directory for this folder.
     */
    private File directory;
    /**
     * Contains the error handler for this folder.
     */
    private IFileErrorHandler errorHandler;
    /**
     * Optional filename filter for this folder.
     */
    private FilenameFilter filter;
    /**
     * Holds the name of the method.
     */
    private String methodName;
    /**
     * The functional name for this folder.
     */
    private String name;
    /**
     * Holds the namespace of the method.
     */
    private String namespace;
    /**
     * Holds the organization.
     */
    private String organizationDn;
    /**
     * Holds the organizational user to use.
     */
    private String orgUserDn;
    /**
     * Holds the parameters for the method.
     */
    private int parametersNode;
    /**
     * Holds the folder XML node for the configuration
     */
    private int folderNode;
    /**
     * Tracking time for changes in the file before it is moved to the processing folder.
     */
    private long trackTime;
    /**
     * If <code>true</code> the file is first moved to the application processing folder before
     * executing the web service.
     */
    private boolean useAppProcessingFolder;
    /**
     * Runtime flag for keeping track of failed folders.
     */
    private boolean lockFailed;

    /**
     * Default constructor.
     */
    public Folder()
    {
    }

    /**
     * Creates a new Folder object.
     *
     * @param   folderNode  The folder XML configuration node.
     * @param   relFolder   Parent folder for relative paths.
     * @param 	defaultUserDn The UserDn to use when no userdn is specified in config file
     * 
     * @throws  FileException  Thrown if the configuration was not valid.
     */
    public Folder(int folderNode, File relFolder, String defaultUserDn)
           throws FileException
    {
        this();
        this.folderNode = Node.duplicate(folderNode);
        parseFromXML(folderNode, relFolder, defaultUserDn);
    }

    /**
     * Called when the configuration should be cleaned up.
     */
    public void cleanup()
    {
        if (parametersNode != 0)
        {
            Node.delete(parametersNode);
            parametersNode = 0;
        }
        if (folderNode != 0)
        {
            Node.delete(folderNode);
            folderNode = 0;
        }
    }

    /**
     * This method parses the XML for the given folder node.
     *
     * @param   folderNode  The XML describing the folder.
     * @param   relFolder   Parent folder for relative paths.
     * @param   defaultUserDn  The UserDn to use when no userdn is specified in config file
     * @throws  FileException  If one of the mandatory fields is not filled.
     */
    public void parseFromXML(int folderNode, File relFolder, String defaultUserDn)
                      throws FileException
    {
        String tempStr;

        tempStr = Find.firstDataWithDefault(folderNode, "fChild<name>", "");

        if (tempStr.length() == 0)
        {
            throw new FileException("The name needs to be specified.");
        }
        setName(tempStr);

        tempStr = Find.firstDataWithDefault(folderNode, "fChild<location>", "");

        if (tempStr.length() == 0)
        {
            throw new FileException("The actual location (directory) needs to be specified.");
        }

        File dir = new File(tempStr);

        if (!dir.isAbsolute())
        {
            dir = new File(relFolder, tempStr);
        }

        if (!dir.exists())
        {
            if (!dir.mkdirs())
            {
                throw new FileException("Unable to create input folder: " + dir);
            }
        }
        else
        {
            if (!dir.isDirectory())
            {
                throw new FileException("Input folder is not a folder: " + dir);
            }
        }

        if (!dir.canRead())
        {
            throw new FileException("Input folder is not readable: " + dir);
        }

        if (!dir.canWrite())
        {
            throw new FileException("Input folder is not writable: " + dir);
        }
        setDirectory(dir);

        tempStr = Find.firstDataWithDefault(folderNode, "fChild<track-time>", "");

        if (tempStr.length() == 0)
        {
            throw new FileException("The track-time parameter needs to be specified.");
        }

        try
        {
            trackTime = (long) (Double.parseDouble(tempStr) * 1000);
        }
        catch (Exception e)
        {
            throw new FileException("Invalid track-time value: " + tempStr);
        }

        tempStr = Find.firstDataWithDefault(folderNode, "fChild<error-handler-class>", "");
        errorHandler = loadErrorHandler(tempStr);

        filter = parseFilter(Find.firstMatch(folderNode, "fChild<filter>"));

        int triggerNode = Find.firstMatch(folderNode, "fChild<trigger>");

        if (triggerNode == 0)
        {
            throw new FileException("The trigger node is undefined.");
        }

        try
        {
            XMLProperties xpProp = new XMLProperties(triggerNode);
            methodName = xpProp.getStringValue("method", "");

            if (methodName.length() == 0)
            {
                throw new FileException("The methodname is not specified for the folder.");
            }
            namespace = xpProp.getStringValue("namespace", "");

            if (namespace.length() == 0)
            {
                throw new FileException("The namespace for the trigger method is not specified for the folder.");
            }

            organizationDn = xpProp.getStringValue("organization", "");
            orgUserDn = xpProp.getStringValue("user", "");
            if ("".equals(orgUserDn))
            {
            	orgUserDn = defaultUserDn;
            	organizationDn = com.cordys.coe.util.general.Util.getOrganizationFromUser(orgUserDn);
            }
            useAppProcessingFolder = xpProp.getBooleanValue("move-file", false);
            canRetry = xpProp.getBooleanValue("can-retry", false);

            int tempNode = xpProp.getXMLNode("parameters");

            if (tempNode != 0)
            {
                parametersNode = Node.duplicate(tempNode);

                Node.removeAttribute(parametersNode, "xmlns");
                Node.removeAttribute(parametersNode, "xmlns:SOAP");
            }
        }
        catch (FileException e)
        {
            throw e;
        }
        catch (GeneralException e)
        {
            throw new FileException(e);
        }
    }

    /**
     * This method returns the string representation of the folder.
     *
     * @return  A string representation.
     */
    @Override
    public String toString()
    {
        StringBuffer sbReturn = new StringBuffer("[");
        sbReturn.append(getName());
        sbReturn.append(": [");
        sbReturn.append(getDirectory());
        sbReturn.append("]]");

        return sbReturn.toString();
    }

    /**
     * This method gets the actual directory for this folder.
     *
     * @return  The actual directory for this folder.
     */
    public File getDirectory()
    {
        return directory;
    }

    /**
     * Returns the errorHandler.
     *
     * @return  Returns the errorHandler.
     */
    public IFileErrorHandler getErrorHandler()
    {
        return errorHandler;
    }

    /**
     * Returns the filter.
     *
     * @return  Returns the filter.
     */
    public FilenameFilter getFilter()
    {
        return filter;
    }

    /**
     * This method gets the method name for the trigger.
     *
     * @return  The method name for the trigger.
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * This method gets the functional name for this folder.
     *
     * @return  The functional name for this folder.
     */
    public String getName()
    {
        return name;
    }

    /**
     * This method gets the namespace of the trigger method.
     *
     * @return  The namespace of the trigger method.
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * This method gets the organization.
     *
     * @return  The organization.
     */
    public String getOrganization()
    {
        return organizationDn;
    }

    /**
     * This method gets the organizational user to use.
     *
     * @return  The organizational user to use.
     */
    public String getOrganizationalUser()
    {
        return orgUserDn;
    }

    /**
     * Returns the parametersNode.
     *
     * @return  Returns the parametersNode.
     */
    public int getParametersNode()
    {
        return parametersNode;
    }
    
    /**
     * Returns the folderNode.
     *
     * @return  Returns the folderNode.
     */
    public int getFolderNode()
    {
        return folderNode;
    }

    /**
     * This method gets the tracking time for files in this folder.
     *
     * @return  The tracking time for files in this folder.
     */
    public long getTrackTime()
    {
        return trackTime;
    }

    /**
     * Returns the useAppProcessingFolder.
     *
     * @return  Returns the useAppProcessingFolder.
     */
    public boolean isUseAppProcessingFolder()
    {
        return useAppProcessingFolder;
    }

    /**
     * Returns the canRetry.
     *
     * @return  Returns the canRetry.
     */
    public boolean isCanRetry()
    {
        return canRetry;
    }

    /**
     * This method sets the actual directory for this folder.
     *
     * @param  directory  The actual directory for this folder.
     */
    public void setDirectory(File directory)
    {
        this.directory = directory;
    }

    /**
     * Sets the errorHandler.
     *
     * @param  errorHandler  The errorHandler to be set.
     */
    public void setErrorHandler(IFileErrorHandler errorHandler)
    {
        this.errorHandler = errorHandler;
    }

    /**
     * This method sets the method name for the trigger.
     *
     * @param  methodName  The method name for the trigger.
     */
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    /**
     * This method sets the functional name for this folder.
     *
     * @param  name  The functional name for this folder.
     */
    public void setName(String name)
    {
        this.name = name.replaceAll("[^A-Za-z0-9]", "_").toUpperCase();
    }

    /**
     * This method sets the namespace of the trigger method.
     *
     * @param  namespace  The namespace of the trigger method.
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * This method sets the tracking time for files in this folder.
     *
     * @param  time  sName The functional tracking time for files in this folder.
     */
    public void setTrackTime(long time)
    {
        this.trackTime = time;
    }

    /**
     * Sets the useAppProcessingFolder.
     *
     * @param  useAppProcessingFolder  The useAppProcessingFolder to be set.
     */
    public void setUseAppProcessingFolder(boolean useAppProcessingFolder)
    {
        this.useAppProcessingFolder = useAppProcessingFolder;
    }

    /**
     * Loads the error handler for this folder.
     *
     * @param   className  Error handler class.
     *
     * @return  Loaded error handler.
     *
     * @throws  FileException
     */
    private IFileErrorHandler loadErrorHandler(String className)
                                        throws FileException
    {
        if ((className == null) || (className.length() == 0))
        {
            return new DefaultErrorHandler();
        }

        try
        {
            Class<?> c = Class.forName(className);

            return (IFileErrorHandler) c.newInstance();
        }
        catch (Exception e)
        {
            throw new FileException("Unable to load error handler class: " + className, e);
        }
    }

    /**
     * Parses the file name filter from the configuration node.
     *
     * @param   node  Configuration 'filter' node. Can be zero.
     *
     * @return  Filename filter or <code>null</code> if none was configured.
     *
     * @throws  FileException  Thrown if the filter configuration was invalid.
     */
    private FilenameFilter parseFilter(int node)
                                throws FileException
    {
        if (node == 0)
        {
            return null;
        }

        String type = Node.getAttribute(node, "type", "glob");
        String pattern = Node.getData(node);

        if ((pattern == null) || (pattern.length() == 0))
        {
            return null;
        }

        try
        {
            final Pattern p;

            if ("glob".equals(type) || "".equals(type))
            {
                p = GeneralUtils.createGlobRegex(pattern, false, Pattern.CASE_INSENSITIVE);
            }
            else if ("regex".equals(type))
            {
                p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            }
            else
            {
                throw new FileException("Invalid filter type: " + type);
            }

            return new FilenameFilter()
            {
                /**
                 * @see  java.io.FilenameFilter#accept(java.io.File, java.lang.String)
                 */
                public boolean accept(File dir, String name)
                {
                    return p.matcher(name).matches();
                }
            };
        }
        catch (FileException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new FileException("Invalid file filter: " + pattern);
        }
    }

    /**
     * Returns the lockFailed.
     *
     * @return Returns the lockFailed.
     */
    public boolean isLockFailed()
    {
        return lockFailed;
    }

    /**
     * Sets the lockFailed.
     *
     * @param lockFailed The lockFailed to be set.
     */
    public void setLockFailed(boolean lockFailed)
    {
        this.lockFailed = lockFailed;
    }
}

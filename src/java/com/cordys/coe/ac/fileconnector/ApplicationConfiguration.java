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
 package com.cordys.coe.ac.fileconnector;

import java.io.File;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import com.cordys.coe.ac.fileconnector.charset.ascii.AsciiCharset;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.utils.XPathWrapperFactory;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;
import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.XMLProperties;
import com.cordys.coe.util.win32.NetworkDrive;
import com.cordys.coe.util.xml.nom.XPathHelper;
import com.eibus.util.Base64;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;

/**
 * This class holds the configuration details for the FileConnector. The configuration specifies the
 * actual configuration file location in the XMLStore.
 *
 * @author  mpoyhone
 */
public class ApplicationConfiguration
{
    /**
     * The tag name which holds the XMLStore record reader configuration file name in the processor
     * configuration xml.
     */
    private static final String READER_CONFIG_FILENAME = "/configuration/Configuration/readerconfigfile";
    /**
     * The tag name which holds character set name for ReadFileRecords method.
     */
    private static final String READER_CHARACTER_SET = "/configuration/Configuration/readercharset";
    /**
     * The tag name which holds character set name for WriteFileRecords method.
     */
    private static final String WRITER_CHARACTER_SET = "/configuration/Configuration/writercharset";
    /**
     * The tag name which holds the XMLStore record writer configuration file name in the processor
     * configuration xml.
     */
    private static final String WRITER_CONFIG_FILENAME = "/configuration/Configuration/writerconfigfile";
    /**
     * The tag name which holds the configuration reload flag name.
     */
    private static final String RELOAD_CONFIG = "/configuration/Configuration/reload-configuration";
    /**
     * The tag name which holds the simple XPath flag name.
     */
    private static final String USE_SIMPLE_XPATH = "/configuration/Configuration/use-simple-xpath";
    /**
     * The name of the tag holding all the drive mappings.
     */
    private static final String PROP_DRIVE_MAPPINGS = "/configuration/Configuration/drivemappings/drivemapping";
    /**
     * Identifies the Logger.
     */
    private static CordysLogger LOGGER = CordysLogger.getCordysLogger(FileConnector.class);
    /**
     * Custom character set provider for Latin-1 to Ascii conversion feature.
     */
    private CharsetProvider cpCustomProvider;
    /**
     * Contains all directory and file names that are allowed to be accessed through this connector.
     */
    private Pattern[] paAllowedDirectoryNames = null;
    /**
     * If <code>true</code> simple XPath expressions are used.
     */
    private boolean useSimpleXPath;
    /**
     * Contains the XPath wrapper factory which contains namespace bindings from the configuration.
     */
    private XPathWrapperFactory xpathFactory;
    /**
     * Holds the XMLProperties object to extract the value for different configuration keys.
     */
    private XMLProperties xpBase;

    /**
     * Creates the constructor.This loads the configuration object and pass it to XMLProperties for
     * processing.
     *
     * @param   iConfigNode  The xml-node that contains the configuration.
     *
     * @throws  ConfigException  Thrown if the configuration XML was invalid.
     */
    public ApplicationConfiguration(int iConfigNode)
                             throws ConfigException
    {
        if (iConfigNode == 0)
        {
            throw new ConfigException("Configuration not found");
        }

        if (!Node.getName(iConfigNode).equals("configuration"))
        {
            throw new ConfigException("Root-tag of the configuration should be <configuration>");
        }

        try
        {
            xpBase = new XMLProperties(iConfigNode);
        }
        catch (GeneralException e)
        {
            throw new ConfigException("Exception while creating the configuration-object.", e);
        }

        useSimpleXPath = xpBase.getBooleanValue(USE_SIMPLE_XPATH, true);
        xpathFactory = XmlUtils.parseNamespaceBindings(iConfigNode, false);
        setUseSimpleXPath(useSimpleXPath);

        String sAllowedStr = xpBase.getStringValue("included-directories", "");

        if (!sAllowedStr.equals(""))
        {
            String[] saAllowedLines = sAllowedStr.split("[\r\n]+");

            if ((saAllowedLines != null) && (saAllowedLines.length > 0))
            {
                List<Pattern> lList = new LinkedList<Pattern>();

                for (int i = 0; i < saAllowedLines.length; i++)
                {
                    String sLine = saAllowedLines[i];

                    if (sLine.length() == 0)
                    {
                        continue;
                    }

                    if (File.pathSeparatorChar == '\\')
                    {
                        // Convert / to \ on windows platform
                        sLine = sLine.replaceAll("/", "\\\\");
                    }

                    Pattern pPat;

                    try
                    {
                        pPat = convertWildCardToPattern(sLine, File.separator);
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new ConfigException("Unable to parse the directory specification.",
                                                  e);
                    }

                    lList.add(pPat);
                }

                if (lList.size() > 0)
                {
                    paAllowedDirectoryNames = lList.toArray(new Pattern[lList.size()]);
                }
            }
        }
    }

    /**
     * Sets the useSimpleXPath.
     *
     * @param useSimpleXPath The useSimpleXPath to be set.
     */
    public void setUseSimpleXPath(boolean useSimpleXPath)
    {
        this.useSimpleXPath = useSimpleXPath;
        
        if (xpathFactory != null) {
            xpathFactory.setUseSimpleXPath(useSimpleXPath);
        }
    }

    /**
     * Returns the refresh interval for the configuration file.
     *
     * @return  The interval in milliseconds.
     */
    public long getConfigFileRefreshInterval()
    {
        return 20000L;
    }

    /**
     * Returns the root configuration node.
     *
     * @return  Root node
     */
    public int getConfigurationNode()
    {
        return (xpBase != null) ? xpBase.getConfigNode() : 0;
    }

    /**
     * Returns the custom character set provider or null if none was loaded.
     *
     * @return  The custom character set provider or null if none was loaded.
     */
    public CharsetProvider getCustomCharsetProvider()
    {
        return cpCustomProvider;
    }

    /**
     * This method returns the network drives that should be created for this directory connector.
     *
     * @return  An array containing all the network drives.
     */
    public NetworkDrive[] getNetworkDrives()
    {
        int[] aiMapping = XPathHelper.selectNodes(xpBase.getConfigNode(), PROP_DRIVE_MAPPINGS);
        
        ArrayList<NetworkDrive> alTemp = new ArrayList<NetworkDrive>();

        for (int iCount = 0; iCount < aiMapping.length; iCount++)
        {
            try
            {
                XMLProperties xpTemp = new XMLProperties(aiMapping[iCount]);
                String password = Base64.decode(xpTemp.getStringValue("password"));
                NetworkDrive ndTemp = new NetworkDrive(xpTemp.getStringValue("location"),
                                                       getDriveLetter(xpTemp),
                                                       xpTemp.getStringValue("username"), password);
                alTemp.add(ndTemp);
            }
            catch (Exception e)
            {
                if (LOGGER.isEnabled(Severity.ERROR))
                {
                    LOGGER.error(e, LogMessages.DRIVE_MAPPING_UNREADABLE);
                }
                throw new IllegalStateException(e);
            }
        }

        return alTemp.toArray(new NetworkDrive[alTemp.size()]);
    }

    /**
     * This method returns the character set to be used for reading file records.
     *
     * @return  The reader character set.
     */
    public String getReaderCharacterSet()
    {
        return xpBase.getStringValue(READER_CHARACTER_SET, "");
    }

    /**
     * This method returns the record reader configuration file path in XMLStore.
     *
     * @return  The location for the configuration file
     */
    public String getReaderConfigFileLocation()
    {
        String sReturn = "";
        sReturn = xpBase.getStringValue(READER_CONFIG_FILENAME, "");
        return sReturn;
    }

    /**
     * Returns a configuration section.
     *
     * @param   path  Section path
     *
     * @return  Section.
     *
     * @throws  GeneralException
     */
    public XMLProperties getSection(String path)
                             throws GeneralException
    {
        return xpBase.getXMLProperties(path);
    }

    /**
     * This method returns the character set to be used for reading file records. This does not
     * return the X-CORDYS-LATIN1-ASCII character set.
     *
     * @return  The read character set.
     */
    public String getStandardReaderCharacterSet()
    {
        String res = getReaderCharacterSet();

        if (AsciiCharset.CHARSETNAME.equals(res))
        {
            return "ISO-8859-1";
        }
        else
        {
            return res;
        }
    }

    /**
     * This method returns the character set to be used for writing file records. This does not
     * return the X-CORDYS-LATIN1-ASCII character set.
     *
     * @return  The read character set.
     */
    public String getStandardWriterCharacterSet()
    {
        String res = getWriterCharacterSet();

        if (AsciiCharset.CHARSETNAME.equals(res))
        {
            return "ISO-8859-1";
        }
        else
        {
            return res;
        }
    }

    /**
     * Returns the read buffer size int bytes for record validator.
     *
     * @return
     */
    public int getValidatorWindowSize()
    {
        return 4096;
    }

    /**
     * This method returns the character set to be used for writing file records.
     *
     * @return  The writer character set.
     */
    public String getWriterCharacterSet()
    {
        return xpBase.getStringValue(WRITER_CHARACTER_SET, "ISO-8859-1");
    }

    /**
     * This method returns the record writer configuration file path in XMLStore.
     *
     * @return  The location for the configuration file
     */
    public String getWriterConfigFileLocation()
    {
        String sReturn = "";
        sReturn = xpBase.getStringValue(WRITER_CONFIG_FILENAME, "");
        return sReturn;
    }

    /**
     * Returns the associated XPathFactory.
     *
     * @return  XPath factory.
     */
    public XPathWrapperFactory getXPathFactory()
    {
        return xpathFactory;
    }

    /**
     * Returns the configuration reload flag value.
     *
     * @return  <code>true</code> if configuration should be loaded with each request.
     */
    public boolean isConfigurationReloadEnabled()
    {
        return xpBase.getBooleanValue(RELOAD_CONFIG, false);
    }

    /**
     * Checks if the file is allowed to be accesses by the configuration settings.
     *
     * @param   fFile  File to be checked.
     *
     * @return  True, if file is allowed, false otherwise.
     */
    public boolean isFileAllowed(File fFile)
    {
        String sFilePath = fFile.getAbsolutePath();

        if ((paAllowedDirectoryNames == null) || (paAllowedDirectoryNames.length == 0))
        {
            return true;
        }

        for (int i = 0; i < paAllowedDirectoryNames.length; i++)
        {
            Pattern pFilePattern = paAllowedDirectoryNames[i];

            if (pFilePattern.matcher(sFilePath).matches())
            {
                return true;
            }
        }

        if (fFile.isDirectory())
        {
            // We need to add directory separator to the path in order
            // to catch the directory itself for patterns like /path/**
            sFilePath += File.separator;

            for (int i = 0; i < paAllowedDirectoryNames.length; i++)
            {
                Pattern pFilePattern = paAllowedDirectoryNames[i];

                if (pFilePattern.matcher(sFilePath).matches())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the useSimpleXPath.
     *
     * @return  Returns the useSimpleXPath.
     */
    public boolean isUseSimpleXPath()
    {
        return useSimpleXPath;
    }

    /**
     * Sets the custom character set provider.
     *
     * @param  prov  The custom character set provider to be set.
     */
    public void setCustomCharsetProvider(CharsetProvider prov)
    {
        cpCustomProvider = prov;
    }

    /**
     * Converts wild card string of forms \a\\b, \a\\b \a\.txt to a regular expression.
     *
     * @param   sWildCardString  Wild card string to be converted.
     * @param   sPathSep         File path separator (e.g. \ or /)
     *
     * @return  Regular expression <code>Pattern</code> object
     *
     * @throws  IllegalArgumentException  Thrown if the parsing failed.
     */
    protected static Pattern convertWildCardToPattern(String sWildCardString,
                                                      String sPathSep)
                                               throws IllegalArgumentException
    {
        String sPatternString = sWildCardString;

        if (sPathSep.equals("\\"))
        {
            sPathSep = "\\\\";
        }

        // Convert simple wild-card to a regexp. This uses unicode character \u0001 as a marker
        // for * character so it will not be replaced twice.
        // Also path separator string is marked by \u0002
        // Escape all path separators to \u0002
        sPatternString = sPatternString.replaceAll(sPathSep, "\u0002");

        // Escape special characters \[](){}.$^
        sPatternString = sPatternString.replaceAll("([\\\\\\[\\]\\(\\)\\{\\}\\.$\\^])", "\\\\$1");

        // Change /path/** notation to /path/.*
        sPatternString = sPatternString.replaceAll("\u0002\\*\\*$", "\u0002.\u0001");
        // Change /path/**/path notation to /path/.*/
        sPatternString = sPatternString.replaceAll("\u0002\\*\\*\u0002", "\u0002.\u0001\u0002");
        // Change /path/*/path notation /path/[^/]*/path
        sPatternString = sPatternString.replaceAll("\\*", "[^\u0002]\u0001");
        // Replace the marker with *
        sPatternString = sPatternString.replace('\u0001', '*');

        if (sPathSep.equals("\\\\"))
        {
            // A weird behavior in replaceAll seems to require this.
            sPathSep = "\\\\\\\\";
        }

        // Escape all \u0002's to path separators.
        sPatternString = sPatternString.replaceAll("\u0002", sPathSep);

        try
        {
            return Pattern.compile(sPatternString, Pattern.CASE_INSENSITIVE);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid wildcard pattern '" + sWildCardString +
                                               "' : " + e);
        }
    }

    /**
     * This method returns the drive letter for the given drive.
     *
     * @param   xpTemp  The property definition.
     *
     * @return  The drive letter (if any specified).
     *
     * @throws  ConfigException
     */
    private String getDriveLetter(XMLProperties xpTemp)
                           throws ConfigException
    {
        String driveLetter = xpTemp.getStringValue("driveletter");

        if (driveLetter != null)
        {
            driveLetter = driveLetter.toUpperCase();

            if ((driveLetter != null) && (driveLetter.length() > 0))
            {
                if (!driveLetter.matches("[a-zA-Z]:{0,1}"))
                {
                    throw new ConfigException("Driveletter '" + driveLetter +
                                              "' is not a valid drive name");
                }

                if (driveLetter.length() == 1)
                {
                    driveLetter = driveLetter + ":";
                }
            }
        }
        return driveLetter;
    }
}

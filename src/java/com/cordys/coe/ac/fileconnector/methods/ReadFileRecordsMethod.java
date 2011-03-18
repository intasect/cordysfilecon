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
package com.cordys.coe.ac.fileconnector.methods;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.IFileConnectorMethod;
import com.cordys.coe.ac.fileconnector.ISoapRequestContext;
import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.utils.ExcelRead;
import com.cordys.coe.ac.fileconnector.utils.FileCharSequence;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;
import com.cordys.coe.ac.fileconnector.utils.XmlUtils;
import com.cordys.coe.ac.fileconnector.validator.RecordValidator;
import com.cordys.coe.ac.fileconnector.validator.ValidatorConfig;
import com.cordys.coe.util.soap.SOAPWrapper;
import com.cordys.coe.util.xmlstore.XMLStoreWrapper;

import com.eibus.util.logger.CordysLogger;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;

import java.io.File;
import java.io.RandomAccessFile;

import java.nio.channels.FileChannel;

import java.nio.charset.Charset;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements ReadFileRecords SOAP method.
 *
 * @author  mpoyhone
 */
public class ReadFileRecordsMethod
        implements IFileConnectorMethod {

    /**
     * Contains the SOAP method name.
     */
    public static final String METHOD_NAME = "ReadFileRecords";
    /**
     * Identifies the Logger.
     */
    private static final CordysLogger LOGGER = CordysLogger.getCordysLogger(ReadFileRecordsMethod.class);
    /**
     * Filename request parameter for ReadFileRecords, ReadFile, WriteFileRecords and WriteFile
     * methods.
     */
    private static final String PARAM_FILENAME = "filename";
    /**
     * File type request parameter for ReadFileRecords.
     */
    private static final String PARAM_FILETYPE = "filetype";
    /**
     * Use tuple-old structure parameter for ReadFileRecords.
     */
    private static final String PARAM_USETUPLEOLD = "usetupleold";
    /**
     * Number of records request parameter for ReadFieldRecords.
     */
    private static final String PARAM_NUMRECORDS = "numrecords";
    /**
     * File offset request parameter for ReadFieldRecords.
     */
    private static final String PARAM_OFFSET = "offset";
    /**
     * Validation only request parameter for ReadFieldRecords.
     */
    private static final String PARAM_VALIDATEONLY = "validateonly";
    /**
     * Contains the FileConnector configuration.
     */
    private ApplicationConfiguration acConfig;
    /**
     * Contains the loaded method configuration.
     */
    private volatile ValidatorConfig configuration;
    /**
     * Configuration reload flag.
     */
    private boolean reloadConfiguration;

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#cleanup()
     */
    public void cleanup()
            throws ConfigException {
    }

    /**
     * Closes the input file.
     *
     * @param  w  File wrapper to be closed.
     */
    public void closeFile(FileWrapper w) {
        if (w.fcFileChannel != null) {
            try {
                w.fcFileChannel.close();
            } catch (Exception e) {
            }
            w.fcFileChannel = null;
        }

        if (w.raFile != null) {
            try {
                w.raFile.close();
            } catch (Exception e) {
            }
            w.raFile = null;
        }

        w.fcsInputSeq = null;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#initialize(com.cordys.coe.ac.fileconnector.ApplicationConfiguration)
     */
    public boolean initialize(ApplicationConfiguration acConfig)
            throws ConfigException {
        this.acConfig = acConfig;

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#onReset()
     */
    public void onReset() {
    }

    /**
     * Opens the input file.
     *
     * @param   w  fInputFile Input file to be opened.
     *
     * @throws  FileException  Thrown if the operation failed.
     */
    public void openFile(FileWrapper w)
            throws FileException {
        closeFile(w);

        assert w.fInputFile != null;

        boolean bSuccess = false;

        // Open the file and the file reader.
        try {
            w.raFile = new RandomAccessFile(w.fInputFile, "r");
            w.fcFileChannel = w.raFile.getChannel();

            w.fcsInputSeq = new FileCharSequence(w.fcFileChannel, w.raFile.length(), 0, 10240,
                    w.cReadCharSet);

            bSuccess = true;
        } catch (Exception e) {
            throw new FileException("Unable to open the input file.", e);
        } finally {
            // Close the file if there was an exception
            if (!bSuccess) {
                closeFile(w);
            }
        }
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#process(com.cordys.coe.ac.fileconnector.ISoapRequestContext)
     */
    public EResult process(ISoapRequestContext req)
            throws FileException {
        int requestNode = req.getRequestRootNode();

        // Get the configuration parameters.
        reloadConfiguration = acConfig.isConfigurationReloadEnabled();

        // Get the needed parameters from the SOAP request
        String sFileName = XmlUtils.getStringParameter(requestNode, PARAM_FILENAME, true);
        String sFileType = XmlUtils.getStringParameter(requestNode, PARAM_FILETYPE, true);
        int iNumRecords = (int) XmlUtils.getLongParameter(requestNode, PARAM_NUMRECORDS, true);
        long lOffset = XmlUtils.getLongParameter(requestNode, PARAM_OFFSET, true);
        boolean bValidateOnly = XmlUtils.getBooleanParameter(requestNode, PARAM_VALIDATEONLY);
        boolean bUseTupleOld = XmlUtils.getBooleanParameter(requestNode, PARAM_USETUPLEOLD);

        int iSheetNumber=-1;
        if (lOffset > Integer.MAX_VALUE) {
            throw new FileException("Files bigger than 2GB are not supported.");
        }

        if (sFileType.equalsIgnoreCase("Excel")) { //Check for Excel File Type to remove sheetindex (filename#sheetno)
            if(sFileName.contains("#"))
            {
                iSheetNumber=Integer.parseInt(sFileName.substring(sFileName.lastIndexOf("#")+1));
                sFileName=sFileName.substring(0, sFileName.lastIndexOf("#"));
            }
         }

        // Create File objects for the source and destination files
        File fFile = new File(sFileName);

        // Do some sanity checking.
        if (!acConfig.isFileAllowed(fFile)) {
            throw new FileException("File access is not allowed.");
        }

        if (!fFile.exists()) {
            throw new FileException("File does not exist: " + fFile);
        }

        // Find out the character set to be used.
        String sCharsetName = acConfig.getReaderCharacterSet();
        Charset cCharset = GeneralUtils.findCharacterSet(sCharsetName, acConfig);

        // Fetch the validator configuration object.
        ValidatorConfig vcConfig = getConfiguration(req);

        Document dDoc = req.getNomDocument();
        int iResultNode = 0;
        List<FileException> lErrorList = new LinkedList<FileException>();
        int iNumberOfReadRecords = 0;
        int iStartRecordNumber = 0;
        long lEndFileOffset = 0;
        long lFileSize = -1;
        FileWrapper w = new FileWrapper(fFile, cCharset);

        try {
            // Try to open the input file
            openFile(w);

            // Check argument sanity.
            assert w.raFile != null;
            assert w.fcFileChannel != null;
            assert w.fcsInputSeq != null;


            if (sFileType.equalsIgnoreCase("Excel")) { //Check for Excel File Type
                //read excel file
                iResultNode = dDoc.createElement("data");
                ExcelRead.readall(sFileName, dDoc, iResultNode, iSheetNumber, (int) lOffset, iNumRecords, -1, -1);
            } else { //For other file types
                // Create the validator object
                RecordValidator rvValidator = new RecordValidator(vcConfig);

                boolean bSuccess = false;
                int iResNode = 0;
                long lCurrentFileOffset = lOffset;


                 
                int iCurrentRecord = iStartRecordNumber; // Note that this is relative to the start
                // offset.
                
                // If we are returning the records, create the root element for them.
                if (!bValidateOnly) {
                    iResNode = dDoc.createElement("data");
                }

                lFileSize = w.raFile.length();

                boolean atEndOfFile = false;

                // Read the required records.
                try {
                    for (int i = 0; (i < iNumRecords) || (iNumRecords < 0); i++) {
                        int iNode;

                        // Set the buffer start offset
                        w.fcsInputSeq.reset(lCurrentFileOffset);

                        // Set the start record number for error messages.
                        rvValidator.setStartRecordNumber(iCurrentRecord);

                        // Call the validator. It returns the record in XML format.
                        iNode = rvValidator.parseAndValidateRecord(sFileType, w.fcsInputSeq, 0,
                                (!bValidateOnly) ? dDoc : null);

                        // Append it to the data-element.
                        if (!bValidateOnly && (iResNode != 0)) {
                            if ((iNode != 0) && (Node.getNumChildren(iNode) > 0)) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Succesfully read record: \n"
                                            + Node.writeToString(iNode, true));
                                }

                                if (bUseTupleOld) {
                                    int iOldNode = dDoc.createElement("old");

                                    // Move all children under the tuple node to the old node.
                                    while (Node.getNumChildren(iNode) > 0) {
                                        int iChildNode = Node.getFirstChild(iNode);

                                        Node.unlink(iChildNode);
                                        Node.appendToChildren(iChildNode, iOldNode);
                                    }

                                    // Add the old node to the tuple node.
                                    Node.appendToChildren(iOldNode, iNode);
                                }

                                Node.appendToChildren(iNode, iResNode);
                            } else {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("End of file reached");
                                }

                                Node.delete(iNode);
                                atEndOfFile = true;
                            }
                        } else {
                            // For validation only we need to check if the record count has changed.
                            if (rvValidator.getEndRecordNumber() == iCurrentRecord) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("End of file reached");
                                }

                                Node.delete(iNode);
                                atEndOfFile = true;
                            }
                        }

                        if (!atEndOfFile) {
                            // Increment the record count.
                            iNumberOfReadRecords++;
                        }

                        // Get the actual offset where the parsing stopped.
                        lCurrentFileOffset = w.fcsInputSeq.getFileOffset(rvValidator.getValidationEndPosition());

                        // Get the end record number where the parsing stopped.
                        iCurrentRecord = rvValidator.getEndRecordNumber();

                        // If we are at the end of the file, stop.
                        if (atEndOfFile) {
                            break;
                        }
                    }

                    bSuccess = true;
                } catch (Exception e) {
                    throw new FileException("Unable to the parse the file.", e);
                } finally {
                    // On error, delete the result node.
                    if (!bSuccess) {
                        if (iResNode != 0) {
                            Node.delete(iResNode);
                        }
                    }
                }

                lEndFileOffset = lCurrentFileOffset;

                iResultNode = iResNode;
            }
        } catch (Exception e) {
            lErrorList.add(new FileException("Unable to the parse the file.", e));
        } finally {
            closeFile(w);
        }

        if (!sFileType.equalsIgnoreCase("Excel")) { //Check for files not of Type Excel
            req.addResponseElement("endoffset", Long.toString(lEndFileOffset));
            req.addResponseElement("recordsread", Long.toString(iNumberOfReadRecords));
            req.addResponseElement("endoffile", Boolean.toString(lFileSize != -1 && lEndFileOffset >= lFileSize));
        }
            
        // Add the errors to the reply
        if (lErrorList.size() > 0) {
            int iErrorsNode = dDoc.createElement("errors");

            // Iterate over the exceptions and create an error line element for each line
            for (Iterator<FileException> iter = lErrorList.iterator(); iter.hasNext();) {
                Throwable eException = iter.next();
                StringBuffer sbLine = new StringBuffer(80);

                while (eException != null) {
                    if (sbLine.length() > 0) {
                        sbLine.append(" * ");
                    }

                    sbLine.append(eException.getMessage());

                    eException = eException.getCause();
                }

                dDoc.createTextElement("item", sbLine.toString(), iErrorsNode);
            }

            req.addResponseElement(iErrorsNode);
        }

        // Add error count to all replies
        req.addResponseElement("errorcount", Long.toString(lErrorList.size()));

        if (iResultNode != 0) {
            req.addResponseElement(iResultNode);
        }

        return EResult.FINISHED;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.IFileConnectorMethod#getMethodName()
     */
    public String getMethodName() {
        return METHOD_NAME;
    }

    /**
     * Sets the configuration object.
     *
     * @param  cfg  New configuration.
     */
    public synchronized void setConfiguration(ValidatorConfig cfg) {
        configuration = cfg;
    }

    /**
     * Returns method configuration object.
     *
     * @param   req  Current SOAP request.
     *
     * @return  Configuration object.
     *
     * @throws  FileException  Thrown if the configuration could not be read.
     */
    private synchronized ValidatorConfig getConfiguration(ISoapRequestContext req)
            throws FileException {
        if (configuration != null) {
            return configuration;
        }

        // Get configuration file name
        String sConfigFileName = acConfig.getReaderConfigFileLocation();

        if ((sConfigFileName == null) || sConfigFileName.equals("")) {
            throw new FileException("Configuration file not set for this connector.");
        }

        // Set the XMLStore SOAP connection information.
        SOAPWrapper swSoap = new SOAPWrapper(req.getNomConnector());

        swSoap.setUser(req.getRequestUserDn());

        // This reads the configuration from XMLStore and parses it.
        ValidatorConfig result = null;

        try {
            XMLStoreWrapper xmlStoreWrapper = new XMLStoreWrapper(swSoap);
            int configNode;

            // Get the file from XMLStore.
            configNode = xmlStoreWrapper.getXMLObject(sConfigFileName);

            // Find the actual file node from the response.
            if ((configNode != 0) && (Node.getNumChildren(configNode) > 0)) {
                // Get the response node
                configNode = Find.firstMatch(configNode, "?<tuple><old><>");

                // The check that the response is valid.
                if (configNode == 0) {
                    // No it was not.
                    throw new ConfigException("Invalid response received from XMLStore.");
                }
            }

            // Check if we have a file node.
            if (configNode == 0) {
                // No, it probably wasn't found.
                return null;
            }

            result = new ValidatorConfig(configNode);
        } catch (Exception e) {
            throw new FileException("Unable to load ReadFileRecords configuration: " + e.getMessage(), e);
        } finally {
            swSoap.freeXMLNodes();
        }

        if (!reloadConfiguration) {
            configuration = result;
        }

        return result;
    }

    /**
     * Wrapper object for all used file objects.
     *
     * @author  mpoyhone
     */
    private class FileWrapper {

        /**
         * File character set.
         */
        Charset cReadCharSet;
        /**
         * The input file channel object.
         */
        FileChannel fcFileChannel = null;
        /**
         * The file input sequence object.
         */
        FileCharSequence fcsInputSeq = null;
        /**
         * Actual file to be read.
         */
        File fInputFile;
        /**
         * The input file object.
         */
        RandomAccessFile raFile = null;

        /**
         * Constructor for FileWrapper.
         *
         * @param  file     Actual file to be read.
         * @param  charset  File character set.
         */
        public FileWrapper(File file, Charset charset) {
            this.fInputFile = file;
            this.cReadCharSet = charset;
        }
    }
}


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
 package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import com.cordys.coe.ac.fileconnector.INomConnector;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogReader;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogWriter;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.StateLog_ProcessingFolder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.FileStateException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState;

import java.io.File;

/**
 * Contains file processing information.
 *
 * @author  mpoyhone
 */
public class FileContext
{
    /**
     * Contains the application processing root folder.
     */
    private File appProcessingRootFolder;
    /**
     * Contains the current location of the file.
     */
    private File currentFile;
    /**
     * Contains the current file processing state.
     */
    private IFileState currentState;
    /**
     * Contains the error folder where the processing folder will be moved when the file processing
     * has ended in an error.
     */
    private File errorRootFolder;
    /**
     * Unique ID for the file. This is used e.g. in the processing folder name.
     */
    private String fileId;
    /**
     * Contains the file's last modification timestamp.
     */
    private long fileLastModified;
    /**
     * Contains the file size.
     */
    private long fileSize;
    /**
     * File state log file object.
     */
    private StateLog_ProcessingFolder fileStateLog;
    /**
     * Contains the folder configuration.
     */
    private FolderConfiguration folderConfig;
    /**
     * Indicates if the file is in the application processing folder.
     */
    private boolean inAppProcessingFolder;
    /**
     * Contains the configured input folder for this file.
     */
    private Folder inputFolder;
    /**
     * Contains the timestamp of the last modification check when the file didn't grow or
     * modification time didn't change.
     */
    private long lastNonModifiedCheck;
    /**
     * Contains the NOM connector for SOAP messages.
     */
    private INomConnector nomConnector;
    /**
     * Original file in the input folder.
     */
    private File originalFile;
    /**
     * Contains the processing folder for this file. <code>null</code> if the file is still in the
     * input folder.
     */
    private File processingFolder;
    /**
     * Contains the configured processing root folder.
     */
    private File processingRootFolder;
    /**
     * Number of times file processing has been restarted after an error.
     */
    private int retryCount;

    /**
     * Constructor for FileContext. This version is used when resuming file processing.
     *
     * @param  inputFolder          The configured input folder for this file.
     * @param  nomConnector         The NOM connector for SOAP messages.
     * @param  folderConfig         Folder configuration object.
     * @param  appProcessingFolder  Application processing folder.
     */
    public FileContext(Folder inputFolder, INomConnector nomConnector,
                       FolderConfiguration folderConfig, File appProcessingFolder)
    {
        super();
        this.inputFolder = inputFolder;
        this.nomConnector = nomConnector;
        this.folderConfig = folderConfig;
        this.appProcessingRootFolder = appProcessingFolder;
    }

    /**
     * Constructor for FileContext.
     *
     * @param  currentFile              The current location of the file.
     * @param  inputFolder              The configured input folder for this file.
     * @param  nomConnector             The NOM connector for SOAP messages.
     * @param  folderConfig             Folder configuration object.
     * @param  processingRootFolder     Configured processing root folder.
     * @param  appProcessingRootFolder  Configured application processing root folder.
     */
    public FileContext(File currentFile, Folder inputFolder, INomConnector nomConnector,
                       FolderConfiguration folderConfig, File processingRootFolder,
                       File appProcessingRootFolder)
    {
        super();
        this.currentFile = currentFile;
        this.inputFolder = inputFolder;
        this.nomConnector = nomConnector;
        this.folderConfig = folderConfig;
        this.processingRootFolder = processingRootFolder;
        this.appProcessingRootFolder = appProcessingRootFolder;

        fileSize = this.currentFile.length();
        fileLastModified = this.currentFile.lastModified();
        lastNonModifiedCheck = -1;
    }

    /**
     * Closes the state log.
     */
    public void closeLog()
    {
        if (fileStateLog != null)
        {
            fileStateLog.close();
            fileStateLog = null;
        }
    }

    /**
     * @see  java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return (currentFile != null) ? currentFile.toString() : super.toString();
    }

    /**
     * Returns the appProcessingRootFolder.
     *
     * @return  Returns the appProcessingRootFolder.
     */
    public File getAppProcessingRootFolder()
    {
        return appProcessingRootFolder;
    }

    /**
     * Returns the currentFile.
     *
     * @return  Returns the currentFile.
     */
    public File getCurrentFile()
    {
        return currentFile;
    }

    /**
     * Returns the currentState.
     *
     * @return  Returns the currentState.
     */
    public IFileState getCurrentState()
    {
        return currentState;
    }

    /**
     * Returns the errorRootFolder.
     *
     * @return  Returns the errorRootFolder.
     */
    public File getErrorRootFolder()
    {
        return errorRootFolder;
    }

    /**
     * Returns the fileId.
     *
     * @return  Returns the fileId.
     */
    public String getFileId()
    {
        return fileId;
    }

    /**
     * Returns the fileLastModified.
     *
     * @return  Returns the fileLastModified.
     */
    public long getFileLastModified()
    {
        return fileLastModified;
    }

    /**
     * Returns the fileSize.
     *
     * @return  Returns the fileSize.
     */
    public long getFileSize()
    {
        return fileSize;
    }

    /**
     * Returns the folderConfig.
     *
     * @return  Returns the folderConfig.
     */
    public FolderConfiguration getFolderConfig()
    {
        return folderConfig;
    }

    /**
     * Returns the inputFolder.
     *
     * @return  Returns the inputFolder.
     */
    public Folder getInputFolder()
    {
        return inputFolder;
    }

    /**
     * Returns the lastNonModifiedCheck.
     *
     * @return  Returns the lastNonModifiedCheck.
     */
    public long getLastNonModifiedCheck()
    {
        return lastNonModifiedCheck;
    }

    /**
     * Returns a state log reader.
     *
     * @return  State log reader or <code>null</code> if state log doesn't exist.
     *
     * @throws  FileStateException  Thrown if the reader could not be created.
     */
    public IStateLogReader getLogReader()
                                 throws FileStateException
    {
        if (fileStateLog == null)
        {
            fileStateLog = StateLog_ProcessingFolder.getInstance(this, false);

            if (fileStateLog != null)
            {
                fileStateLog.open(false);
            }
        }

        return fileStateLog;
    }

    /**
     * Returns a state log writer.
     *
     * @return  State log writer.
     *
     * @throws  FileStateException  Thrown if the writer could not be created.
     */
    public IStateLogWriter getLogWriter()
                                 throws FileStateException
    {
        return getLogWriter(true);
    }

    /**
     * Returns a state log writer.
     *
     * @param   create  If <code>true</code> and the log does not exists, it is created.
     *
     * @return  State log writer or <code>null</code> if create was <code>false</code> and log
     *          didn't exist.
     *
     * @throws  FileStateException  Thrown if the writer could not be created.
     */
    public IStateLogWriter getLogWriter(boolean create)
                                 throws FileStateException
    {
        if ((fileStateLog == null) && create)
        {
            fileStateLog = StateLog_ProcessingFolder.getInstance(this, true);

            if (fileStateLog != null)
            {
                fileStateLog.open(true);
            }
        }

        return fileStateLog;
    }

    /**
     * Returns the nomConnector.
     *
     * @return  Returns the nomConnector.
     */
    public INomConnector getNomConnector()
    {
        return nomConnector;
    }

    /**
     * Returns the originalFile.
     *
     * @return  Returns the originalFile.
     */
    public File getOriginalFile()
    {
        return originalFile;
    }

    /**
     * Returns the processingFolder.
     *
     * @return  Returns the processingFolder.
     */
    public File getProcessingFolder()
    {
        return processingFolder;
    }

    /**
     * Returns the processingRootFolder.
     *
     * @return  Returns the processingRootFolder.
     */
    public File getProcessingRootFolder()
    {
        return processingRootFolder;
    }

    /**
     * Returns the retryCount.
     *
     * @return  Returns the retryCount.
     */
    public int getRetryCount()
    {
        return retryCount;
    }

    /**
     * Returns the inAppProcessingFolder.
     *
     * @return  Returns the inAppProcessingFolder.
     */
    public boolean isInAppProcessingFolder()
    {
        return inAppProcessingFolder;
    }

    /**
     * Sets the currentFile.
     *
     * @param  currentFile  The currentFile to be set.
     */
    public void setCurrentFile(File currentFile)
    {
        this.currentFile = currentFile;
    }

    /**
     * Sets the currentState.
     *
     * @param  currentState  The currentState to be set.
     */
    public void setCurrentState(IFileState currentState)
    {
        this.currentState = currentState;
    }

    /**
     * Sets the errorRootFolder.
     *
     * @param  errorRootFolder  The errorRootFolder to be set.
     */
    public void setErrorRootFolder(File errorRootFolder)
    {
        this.errorRootFolder = errorRootFolder;
    }

    /**
     * Sets the fileId.
     *
     * @param  fileId  The fileId to be set.
     */
    public void setFileId(String fileId)
    {
        this.fileId = fileId;
    }

    /**
     * Sets the fileLastModified.
     *
     * @param  fileLastModified  The fileLastModified to be set.
     */
    public void setFileLastModified(long fileLastModified)
    {
        this.fileLastModified = fileLastModified;
    }

    /**
     * Sets the fileSize.
     *
     * @param  fileSize  The fileSize to be set.
     */
    public void setFileSize(long fileSize)
    {
        this.fileSize = fileSize;
    }

    /**
     * Sets the inAppProcessingFolder.
     *
     * @param  inAppProcessingFolder  The inAppProcessingFolder to be set.
     */
    public void setInAppProcessingFolder(boolean inAppProcessingFolder)
    {
        this.inAppProcessingFolder = inAppProcessingFolder;
    }

    /**
     * Sets the inputFolder.
     *
     * @param  inputFolder  The inputFolder to be set.
     */
    public void setInputFolder(Folder inputFolder)
    {
        this.inputFolder = inputFolder;
    }

    /**
     * Sets the lastNonModifiedCheck.
     *
     * @param  lastNonModifiedCheck  The lastNonModifiedCheck to be set.
     */
    public void setLastNonModifiedCheck(long lastNonModifiedCheck)
    {
        this.lastNonModifiedCheck = lastNonModifiedCheck;
    }

    /**
     * Sets the nomConnector.
     *
     * @param  nomConnector  The nomConnector to be set.
     */
    public void setNomConnector(INomConnector nomConnector)
    {
        this.nomConnector = nomConnector;
    }

    /**
     * Sets the originalFile.
     *
     * @param  originalFile  The originalFile to be set.
     */
    public void setOriginalFile(File originalFile)
    {
        this.originalFile = originalFile;
    }

    /**
     * Sets the processingFolder.
     *
     * @param  processingFolder  The processingFolder to be set.
     */
    public void setProcessingFolder(File processingFolder)
    {
        this.processingFolder = processingFolder;
    }

    /**
     * Sets the processingRootFolder.
     *
     * @param  processingRootFolder  The processingRootFolder to be set.
     */
    public void setProcessingRootFolder(File processingRootFolder)
    {
        this.processingRootFolder = processingRootFolder;
    }

    /**
     * Sets the retryCount.
     *
     * @param  retryCount  The retryCount to be set.
     */
    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }
}

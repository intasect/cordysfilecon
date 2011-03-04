
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

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.FileStateException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState;
import com.cordys.coe.util.FileUtils;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.File;

/**
 * Worker class that will do the actual file processing. This class also contains the file lock
 * object for checking if the file is ready to be sent to the processing queue.
 *
 * @author  mpoyhone
 */
public class FileProcessWorker
    implements Runnable
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(FileProcessWorker.class);
    /**
     * File processing context object.
     */
    private FileContext fileContext;
    /**
     * Contains the JMX wrapper object.
     */
    private JMXWrapperObject jmxWrapper;
    /**
     * Contains the parent poller thread.
     */
    private DirectoryPollerThread pollerThread;
    /**
     * Time when the file was scheduled to be processed.
     */
    private long scheduleTime;

    /**
     * Constructor for FileProcessWorker. This version is used for new files.
     *
     * @param  poller       Owning poller thread object.
     * @param  fileContext  File processing context object.
     * @param  jmxWrapper   JMX wrapper object.
     */
    public FileProcessWorker(DirectoryPollerThread poller, FileContext fileContext,
                             JMXWrapperObject jmxWrapper)
    {
        this.pollerThread = poller;
        this.fileContext = fileContext;
        this.jmxWrapper = jmxWrapper;
        this.scheduleTime = jmxWrapper.onProcessingStart(fileContext.getFileSize());
    }

    /**
     * @see  java.lang.Runnable#run()
     */
    public void run()
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Processing file: " + fileContext);
        }

        long triggerStartTime = jmxWrapper.onTriggerStart();

        try
        {
            // Execute the states until we reach a finish state.
            IFileState state = fileContext.getCurrentState();

            while ((state != null) && !state.isFinished())
            {
                if (!state.execute())
                {
                    throw new FileStateException(FileStateException.EType.INTERNAL,
                                                 "File state cannot return false in the processing folder!");
                }

                state = fileContext.getCurrentState();
            }

            // Processing is now complete.
            // First close the state log file.
            try
            {
                fileContext.closeLog();
            }
            catch (Exception e)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.log(Severity.WARN, "State log closing failed for file: " + fileContext, e);
                }
            }

            // The the poller thread the this file is finished.
            pollerThread.fileProcessingFinished(fileContext);

            // Delete the processing folder.
            File processingFolder = fileContext.getProcessingFolder();

            if (LOG.isDebugEnabled())
            {
                LOG.debug("File processing finished. Deleting the processing folder: " +
                          processingFolder);
            }

            if ((processingFolder != null) && processingFolder.exists())
            {
                FileUtils.deleteRecursively(processingFolder);
            }

            jmxWrapper.onProcessingEnd(true, scheduleTime);
        }
        catch (Exception e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("File processing failed: " + fileContext, e);
            }

            pollerThread.handleFileError(fileContext, e);
            jmxWrapper.onProcessingEnd(false, scheduleTime);
        }
        finally
        {
            jmxWrapper.onTriggerEnd(triggerStartTime);
        }
    }
}

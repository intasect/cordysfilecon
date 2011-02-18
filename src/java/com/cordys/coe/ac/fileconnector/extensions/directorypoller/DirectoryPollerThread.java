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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.cordys.coe.ac.fileconnector.ApplicationConfiguration;
import com.cordys.coe.ac.fileconnector.INomConnector;
import com.cordys.coe.ac.fileconnector.exception.FileException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.statelog.IStateLogReader;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.EFileState;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.FileStateException;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateError;
import com.cordys.coe.ac.fileconnector.utils.GeneralUtils;
import com.cordys.coe.ac.fileconnector.utils.TimestampQueue;
import com.cordys.coe.util.FileUtils;
import com.eibus.management.IManagedComponent;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

/**
 * This class will poll the configured directories for new files. If files are found, they are
 * picked up and the appropriate trigger is fired.
 *
 * @author  mpoyhone, pgussow
 */
public class DirectoryPollerThread
    implements Runnable
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(DirectoryPollerThread.class);
    /**
     * Maximum amount of processing tasks scheduled for execution.
     */
    private static final int MAX_FILES_IN_PROCESS = 100;
    /**
     * Timeout after which idle worker threads are terminated (in seconds).
     */
    private static final int WORKER_THREAD_TIMEOUT = 60;
    /**
     * Contains file processing retry wait times in seconds (10 secs, 30 secs, 1 min, 5 mins, 15
     * mins).
     */
    private static final int[] RETRY_WAIT_TIMES = { 10, 30, 60, 5 * 60, 15 * 60 };
    /**
     * Holds the folder to which the files will be moved before calling the web service. This is
     * used only if the trigger is configured to move the file to this folder.
     */
    private File appProcessingFolder;
    /**
     * Holds the folder to which the files will be moved if an error occurs during processing.
     */
    private File errorFolder;
    /**
     * Contains the executor for file processing.
     */
    private ExecutorService fileExecutor;
    /**
     * Contains the task queue for the executor service.
     */
    private BlockingQueue<Runnable> fileExecutorQueue;
    /**
     * Contains files that are being tracked for modification in the input folder.
     */
    private Map<File, FileContext> fileTrackMap = new HashMap<File, FileContext>();
    /**
     * Holds the list of folders to poll.
     */
    private FolderConfiguration folderConfig;
    /**
     * Counter for the number of files which are in the processing folder. This is used to limit the
     * number of files picked up by the scanner method, so that the processing folder does not fill
     * up.
     */
    private AtomicInteger inProcessCount = new AtomicInteger(0);
    /**
     * Contains the JMX counters.
     */
    private JMXWrapperObject jmxWrapper;
    /**
     * Contains the SOAP connector.
     */
    private INomConnector nomConnector;
    /**
     * The interval of sleeping between poll intervals.
     */
    private long pollingInterval = 10000;
    /**
     * Holds the folder to which the files will be moved to clean out the polling folder.
     */
    private File processingFolder;
    /**
     * Lock file in the processing folder.
     */
    private LockFile processingFolderLock;
    /**
     * Contains files which are waiting for a retry operation.
     */
    private TimestampQueue<FileContext> retryQueue = new TimestampQueue<FileContext>(100);
    /**
     * Indicates whether or not the thread should stop.
     */
    private volatile boolean stopFlag = false;
    /**
     * Contains the current thread for this poller.
     */
    private volatile Thread workerThread = null;

    /**
     * Constructor. Creates the poller object.
     *
     * @param   acConfig             Connector configuration object.
     * @param   folderConfig         The folders to poll.
     * @param   interval             The poll interval.
     * @param   connector            The connector to use for sending messages.
     * @param   processingFolder     Files are moved to this folder for processing.
     * @param   errorFolder          Files are moved in this folder if an error occurred during
     *                               processing.
     * @param   appProcessingFolder  Application processing folder.
     * @param   minWorkerThreads     Minimum worker thread count.
     * @param   maxWorkerThreads     Maximum worker thread count.
     * @param   jmxComp              Parent JMX managed component.
     *
     * @throws  FileException  Thrown if the operation failed.
     */
    public DirectoryPollerThread(ApplicationConfiguration acConfig,
                                 FolderConfiguration folderConfig, long interval,
                                 INomConnector connector, File processingFolder, File errorFolder,
                                 File appProcessingFolder, int minWorkerThreads,
                                 int maxWorkerThreads, IManagedComponent jmxComp)
                          throws FileException
    {
        // Create the needed folders.
        GeneralUtils.createFolder(processingFolder);
        GeneralUtils.createFolder(errorFolder);

        if (appProcessingFolder != null)
        {
            GeneralUtils.createFolder(appProcessingFolder);
        }

        // Lock the processing folder.
        processingFolderLock = new LockFile(processingFolder);

        try
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Locking the processing folder: " + processingFolder);
            }

            processingFolderLock.acquire();
        }
        catch (IOException e)
        {
            throw new FileException("Unable to lock the processing folder: " + processingFolder, e);
        }

        this.folderConfig = folderConfig;
        this.pollingInterval = interval;
        this.nomConnector = connector;
        this.processingFolder = processingFolder;
        this.errorFolder = errorFolder;
        this.appProcessingFolder = appProcessingFolder;

        // Create the input folders, if needed.
        for (Folder f : folderConfig.getFolders())
        {
            GeneralUtils.createFolder(f.getDirectory());
        }

        // Create the worker thread pool.
        ThreadPoolExecutor pool;

        fileExecutorQueue = new ArrayBlockingQueue<Runnable>(30);
        pool = new ThreadPoolExecutor(minWorkerThreads, maxWorkerThreads, WORKER_THREAD_TIMEOUT,
                                      TimeUnit.SECONDS, fileExecutorQueue);
        fileExecutor = pool;

        // Initialize JMX management settings, counters and alerts.
        this.jmxWrapper = new JMXWrapperObject(jmxComp, pool, fileExecutorQueue, inProcessCount);

        // Restart processing of files in the processing folder, if any.
        try
        {
            restartFileProcessing();
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "Restart of file processing failed.", e);
        }
    }

    /**
     * Called by the worker thread when it has successfully finished processing the file and the
     * file's processing folder is deleted.
     *
     * @param  fileContext File context.
     */
    public void fileProcessingFinished(FileContext fileContext)
    {
        inProcessCount.decrementAndGet();
    }

    /**
     * Handles a file which cannot be processed.
     *
     * @param  fileContext  file File in question.
     * @param  exception    Causing exception.
     */
    public void handleFileError(FileContext fileContext, Throwable exception)
    {
        if ((exception != null) && (exception instanceof FileStateException))
        {
            // Check if we can retry the file processing.
            FileStateException stateError = (FileStateException) exception;
            FileStateException.EType errorType = stateError.getType();

            switch (errorType)
            {
                case RETRY:
                case RETRY_BLOCK_INPUT:

                    int retryCount = fileContext.getRetryCount();

                    if (retryCount < RETRY_WAIT_TIMES.length)
                    {
                        long timeNow = System.currentTimeMillis();
                        int waitTime = RETRY_WAIT_TIMES[retryCount];

                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("Retrying file processing. Retry #" + (retryCount + 1) +
                                      ". Wait time " + waitTime + " seconds.");
                        }

                        synchronized (retryQueue)
                        {
                            retryQueue.add(fileContext, timeNow + (waitTime * 1000L));
                        }

                        fileContext.setRetryCount(retryCount + 1);
                        return;
                    }
                    break;

                case INTERNAL:
                    LOG.log(Severity.ERROR, "File processing ended because of an internal error.",
                            exception);
                    break;
            }
        }

        try
        {
            // Create an error state which moves the file to the error folder.
            IFileState errorState = new StateError(fileContext, exception, this);

            errorState.execute();

            // Call the error handler. The default implementation will
            // just raise an alert.
            Folder fileFolder = fileContext.getInputFolder();
            IFileErrorHandler errorHandler = (fileFolder != null) ? fileFolder.getErrorHandler()
                                                                  : null;

            if (errorHandler == null)
            {
                LOG.log(Severity.ERROR, "Error handler not set for input folder: " + fileFolder);
                return;
            }

            errorHandler.handleFileError(fileContext, this, exception);
        }
        catch (Exception e)
        {
            LOG.log(Severity.ERROR, "File error handler execution failed.", e);
        }
    }

    /**
     * This method is called when the thread is started. It will browse through the list of folders
     * to find any files in that folder that need to be processed.
     */
    public void run()
    {
        workerThread = Thread.currentThread();
        workerThread.setName("Directory Poller Thread");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("DirectoryPoller started.");
        }

        try
        {
            main_loop:
            while (!shouldStop())
            {
                long startTime = System.currentTimeMillis();
                long jmxStartTime;

                jmxStartTime = jmxWrapper.onScanStart();

                // Allocate workers for any ready files in the retry queue.
                while (true)
                {
                    FileContext retryFile;

                    synchronized (retryQueue)
                    {
                        retryFile = retryQueue.getFirst(startTime, true);
                    }

                    if (retryFile != null)
                    {
                        if (!scheduleFileWorker(retryFile))
                        {
                            synchronized (retryQueue)
                            {
                                // Unable to allocate a new worker, so add the file back.
                                retryQueue.add(retryFile, startTime);
                            }

                            // Do not scan any new files.
                            continue main_loop;
                        }
                    }
                    else
                    {
                        break;
                    }
                }

                // Scan all folders for new files.
                Set<File> seenFiles = new HashSet<File>();
                boolean executeTracking = true;

                if (inProcessCount.get() < MAX_FILES_IN_PROCESS)
                {
                    for (Folder folder : folderConfig.getFolders())
                    {
                        // Try to acquire a lock for the input folder.
                        LockFile folderLock = new LockFile(folder.getDirectory());

                        try
                        {
                            if (!folderLock.acquire(true))
                            {
                                if (LOG.isDebugEnabled())
                                {
                                    LOG.debug("Input folder " + folder.getDirectory() +
                                              " is locked by another process. Skipping it for now");
                                }

                                continue;
                            }
                            
                            if (folder.isLockFailed()) {
                                if (LOG.isInfoEnabled()) {
                                    LOG.log(Severity.INFO, "Input folder locking succeeded after a previous failure: " + folder.getDirectory());
                                }
                                
                                folder.setLockFailed(false);
                            }
                        }
                        catch (Exception e)
                        {
                            if (! folder.isLockFailed()) {
                                LOG.log(Severity.ERROR,
                                        "Unable to lock the input folder: " + folder.getDirectory(), e);
                                
                                folder.setLockFailed(true);
                            }
                            continue;
                        }

                        // Get a list of files in this folder and track them for changes.
                        try
                        {
                            File[] files = getFolderContent(folder);

                            for (File file : files)
                            {
                                // Check for stopping, so that we don't process unnecessary files.
                                if (shouldStop())
                                {
                                    break main_loop;
                                }

                                if (!file.exists())
                                {
                                    throw new FileException("File " + file + " does not exist.");
                                }

                                if (!file.isFile())
                                {
                                    continue;
                                }

                                if (file.equals(folderLock.getFile()))
                                {
                                    // This is the lock file.
                                    continue;
                                }

                                // Mark the file as seen in this round.
                                seenFiles.add(file);

                                if (executeTracking)
                                {
                                    // Execute the tracking states.
                                    if (!trackFile(folder, file))
                                    {
                                        // Do not scan any more file for now.
                                        executeTracking = false;
                                    }
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            if (LOG.isWarningEnabled())
                            {
                                LOG.log(Severity.WARN,
                                        "Exception while scanning files in folder " + folder, e);
                            }
                        }
                        finally
                        {
                            // Release the folder lock.
                            folderLock.release();
                        }
                    }
                }

                // Clean up all the non-existing file entries in the track map.
                Iterator<Map.Entry<File, FileContext>> entryIter = fileTrackMap.entrySet()
                                                                               .iterator();

                while (entryIter.hasNext())
                {
                    Map.Entry<File, FileContext> entry = entryIter.next();
                    File f = entry.getKey();

                    if (!seenFiles.contains(f))
                    {
                        // File has disappeared from the input folder, so
                        // don't process it any further.
                        entryIter.remove();
                    }
                }

                jmxWrapper.onScanEnd(jmxStartTime);

                long endTime = System.currentTimeMillis();
                long sleepTime = pollingInterval - (endTime - startTime);

                if (sleepTime > 10L)
                {
                    // Go to sleep
                    try
                    {
                        Thread.sleep(pollingInterval);
                    }
                    catch (InterruptedException e)
                    {
                        if (LOG.isDebugEnabled())
                        {
                            LOG.debug("DirectoryPoller thread interrupted.", e);
                        }

                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            if (LOG.isWarningEnabled())
            {
                LOG.log(Severity.WARN, "Error occurred in the main polling loop.", e);
            }
        }
        finally
        {
            cleanup();
        }

        workerThread = null;

        if (LOG.isDebugEnabled())
        {
            LOG.debug("DirectoryPoller stopped.");
        }
    }

    /**
     * Terminates this thread.
     *
     * @param  waitForTermination  If <code>true</code> this method returns when the thread has
     *                             terminated.
     */
    public void terminate(boolean waitForTermination)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Requesting DirectoryPoller thread to terminate.");
        }

        this.stopFlag = true;

        if (waitForTermination)
        {
            while (true)
            {
                Thread t = workerThread;

                if (t == null)
                {
                    break;
                }

                t.interrupt();

                try
                {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ignored)
                {
                }
            }

            // Wait until all worker threads are finished.
            while (true)
            {
                if (fileExecutor.isTerminated())
                {
                    break;
                }

                try
                {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ignored)
                {
                }
            }
        }
    }

    /**
     * Returns the appProcessingFolder.
     *
     * @return  Returns the appProcessingFolder.
     */
    public File getAppProcessingFolder()
    {
        return appProcessingFolder;
    }

    /**
     * Returns the error folder.
     *
     * @return  The error folder.
     */
    public File getErrorFolder()
    {
        return errorFolder;
    }

    /**
     * Returns the jmxWrapper.
     *
     * @return  Returns the jmxWrapper.
     */
    public JMXWrapperObject getJmxWrapper()
    {
        return jmxWrapper;
    }

    /**
     * Returns the NOM connector for SOAP messaging.
     *
     * @return  NOM connector for SOAP messaging.
     */
    public INomConnector getNomConnector()
    {
        return nomConnector;
    }

    /**
     * Returns the processing folder.
     *
     * @return  The processing folder.
     */
    public File getProcessingFolder()
    {
        return processingFolder;
    }

    /**
     * Checks if the file is ready for the worker thread.
     *
     * @param   ctx  File context.
     *
     * @return  <code>true</code> if the worker thread can be scheduled.
     *
     * @throws  FileStateException
     */
    private boolean canStartProcessing(FileContext ctx)
                                throws FileStateException
    {
        IFileState origState = ctx.getCurrentState();
        IFileState fileState = origState;

        if (fileState == null)
        {
            throw new FileStateException(FileStateException.EType.INTERNAL,
                                         "File state is not set.");
        }

        // Execute the file states, until we have the IN_PROCESSING state.
        // these states must not block or the polling loop will not work.
        while (fileState.getStateType() != EFileState.IN_PROCESSING)
        {
            if (fileState.isFinished())
            {
                throw new FileStateException(FileStateException.EType.INTERNAL,
                                             "Finished state reached while scanning.");
            }

            // Execute the state.
            if (!fileState.execute())
            {
                return false;
            }

            // Get the next state set by the execute() method.
            fileState = ctx.getCurrentState();

            if (fileState == null)
            {
                throw new FileStateException(FileStateException.EType.INTERNAL,
                                             "File state is not set.");
            }
        }

        if (origState.getStateType() != EFileState.IN_PROCESSING)
        {
            // File was just now moved to the processing folder.
            inProcessCount.incrementAndGet();
        }

        return true;
    }

    /**
     * Cleans up this thread and releases the processing folder lock.
     */
    private void cleanup()
    {
        // Shut down the processing threads.
        fileExecutor.shutdown();

        // Release the processing folder lock.
        if (processingFolderLock != null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Releasing the lock on the processing folder: " + processingFolder);
            }

            processingFolderLock.release();
        }

        jmxWrapper.cleanup();
    }

    /**
     * Restarts any files that are present in the processing folder.
     */
    private void restartFileProcessing()
    {
        // Get all folders under the processing folder.
        File[] fileFolders = processingFolder.listFiles();

        for (File folder : fileFolders)
        {
            if (!folder.isDirectory())
            {
                // This is not a temporary folder for the file.
                continue;
            }

            String folderName = folder.getName();
            int nameSepPos = folderName.lastIndexOf('-');

            if (nameSepPos <= 0)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Unkwown processing folder found: " + folder);
                }
                continue;
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Found existing file folder in the processing folder: " + folder);
            }

            String folderLogicalName = folderName.substring(0, nameSepPos);
            String fileId = (nameSepPos < (folderName.length() - 1))
                            ? folderName.substring(nameSepPos + 1) : null;

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Folder logical name is: " + folderLogicalName);
            }

            // Try to find the configured input folder for this file.
            Folder inputFolder = folderConfig.getFolderByName(folderLogicalName);

            if (inputFolder == null)
            {
                LOG.log(Severity.ERROR,
                        "Unable to resume file processing as input folder not found with logical name: " +
                        folderLogicalName);
                continue;
            }

            // This file should be ready for processing.
            FileContext fileContext = new FileContext(inputFolder, nomConnector, folderConfig,
                                                      appProcessingFolder);

            inProcessCount.incrementAndGet();

            try
            {
                // Read the state log and create a resume state
                // which is attached to the last good state read from the log.
                IStateLogReader logReader;

                fileContext.setProcessingFolder(folder);
                fileContext.setErrorRootFolder(errorFolder);
                logReader = fileContext.getLogReader();

                IFileState lastState = (logReader != null) ? logReader.readLog(fileContext, null)
                                                           : null;
                IFileState resumeState = EFileState.RESUME.createState(lastState, fileContext);

                fileContext.setCurrentState(resumeState);

                if (fileContext.getFileId() == null)
                {
                    // If the file ID was not read from the state log, then set it from the folder
                    // name.
                    fileContext.setFileId(fileId);
                }

                if (resumeState.isFinished())
                {
                    // File processing is finished, so delete the folder.
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Processing is finished for file: " + fileContext);
                    }

                    FileUtils.deleteRecursively(folder);
                    inProcessCount.decrementAndGet();
                }
                else
                {
                    // Schedule the file for further processing.
                    synchronized (retryQueue)
                    {
                        retryQueue.add(fileContext, System.currentTimeMillis());
                    }
                }
            }
            catch (Exception e)
            {
                // Resume failed, so move the file to the error folder.
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Resume failed for file: " + fileContext, e);
                }

                handleFileError(fileContext,
                                new FileException("File processing could not be resumed."));
            }
        }
    }

    /**
     * Tries to schedule the given file worker for executions.
     *
     * @param   ctx  w File worker object.
     *
     * @return  <code>true</code> if the worker was scheduled successfully.
     */
    private boolean scheduleFileWorker(FileContext ctx)
    {
        FileProcessWorker worker = new FileProcessWorker(this, ctx, jmxWrapper);

        try
        {
            fileExecutor.execute(worker);

            return true;
        }
        catch (RejectedExecutionException e)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Worker scheduling failed: " + ctx, e);
            }

            return false;
        }
    }

    /**
     * Returns <code>true</code> if this thread is requested to stop.
     *
     * @return  <code>true</code> if this thread is requested to stop.
     */
    private boolean shouldStop()
    {
        if (stopFlag)
        {
            return true;
        }

        return false;
    }

    /**
     * Executes the TRACKING and MOVE_TO_PROCESSING states.
     *
     * @param   folder  Input folder object.
     * @param   file    File to be scanned.
     *
     * @return  <code>true</code> if the scanning can continue. <code>false</code> if no more files
     *          should be scanned for now.
     */
    private boolean trackFile(Folder folder, File file)
    {
        if (inProcessCount.get() >= MAX_FILES_IN_PROCESS)
        {
            // Maximum number of files in the processing folder reached,
            // so don't scan any more files for now.
            return false;
        }

        // Try to see if we are already processing this file.
        FileContext fileContext = fileTrackMap.get(file);

        if (fileContext == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Found a new file. Tracking it for modification: " + file);
            }

            // We haven't seen this file before, so create a new entry.
            fileContext = new FileContext(file, folder, nomConnector, folderConfig,
                                          processingFolder, appProcessingFolder);
            fileContext.setFileId(Utils.createFileId(fileContext));
            fileContext.setOriginalFile(file);
            fileContext.setCurrentState(EFileState.TRACKING.createState(null, fileContext));
            fileContext.setErrorRootFolder(errorFolder);
            fileTrackMap.put(file, fileContext);
            jmxWrapper.onNewFile();
        }
        else
        {
            IFileState fileState = fileContext.getCurrentState();

            if (fileState == null)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("File state is not set!");
                }

                fileTrackMap.remove(file);

                return true;
            }

            try
            {
                // Try to see if the file is ready for processing.
                if (canStartProcessing(fileContext))
                {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("File moved to the processing folder: " + fileContext);
                    }

                    // File was moved successfully, so remove the file
                    // from tracking and schedule a worker thread for
                    // the file.
                    fileTrackMap.remove(file);

                    if (!scheduleFileWorker(fileContext))
                    {
                        // Put this file in the restart list and
                        // don't try to schedule others now.
                        synchronized (retryQueue)
                        {
                            retryQueue.add(fileContext, System.currentTimeMillis() + 100);
                        }
                        return false;
                    }
                }
            }
            catch (Exception e)
            {
                // File processing cannot continue, so move the file
                // to the error folder.
                fileTrackMap.remove(file);
                handleFileError(fileContext, e);
                return true;
            }
        }

        return true;
    }

    /**
     * This method returns the list of files in the configured folder.
     *
     * @param   fFolder  Folder to be scanned.
     *
     * @return  The list of files in the passed on folder.
     *
     * @throws  FileException  Thrown if the operation failed.
     */
    private File[] getFolderContent(Folder fFolder)
                             throws FileException
    {
        File fDirectory = fFolder.getDirectory();

        if (!fDirectory.exists())
        {
            throw new FileException("Folder " + fDirectory +
                                    " could not be found on the filesystem.");
        }

        FilenameFilter filter = fFolder.getFilter();

        if (filter != null)
        {
            return fDirectory.listFiles(filter);
        }
        
        return fDirectory.listFiles();
    }
}


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

import com.eibus.management.AlertLevel;
import com.eibus.management.IAlertDefinition;
import com.eibus.management.IManagedComponent;
import com.eibus.management.counters.CounterFactory;
import com.eibus.management.counters.IEventOccurrenceCounter;
import com.eibus.management.counters.IEventValueCounter;
import com.eibus.management.counters.ITimerEventValueCounter;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wrapper object for JMX counters. Implemented in this class so that it is easy to pass to the
 * worker threads.
 *
 * @author  mpoyhone
 */
public class JMXWrapperObject
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(JMXWrapperObject.class);

    /**
     * Contains the JMX Component.
     */
    private IManagedComponent comp;
    /**
     * Error file count JMX counter.
     */
    private IEventOccurrenceCounter errorFileCount;
    /**
     * File processing JMX alert definition.
     */
    private IAlertDefinition fileProcessingAlert;
    /**
     * File size JMX counter.
     */
    private IEventValueCounter fileSize;

    /**
     * True if the counters have been successfully initialized.
     */
    private boolean initialized = false;
    /**
     * File processing time JMX counter.
     */
    private ITimerEventValueCounter processingTime;
    /**
     * Restarted file count JMX counter.
     */
    private IEventOccurrenceCounter restartedFileCount;
    /**
     * File scan time JMX counter.
     */
    private ITimerEventValueCounter scanTime;
    /**
     * Seen file count JMX counter.
     */
    private IEventOccurrenceCounter seenFileCount;
    /**
     * Successfully processed file count JMX counter.
     */
    private IEventOccurrenceCounter successfulFileCount;
    /**
     * Contains JMX settings and counter for the worker thread pool.
     */
    private ThreadPoolSettings threadPoolSettings;
    /**
     * Total processed file count JMX counter.
     */
    private IEventOccurrenceCounter totalFileCount;
    /**
     * SOAP trigger time JMX counter.
     */
    private ITimerEventValueCounter triggerTime;

    /**
     * Constructor for StatsObject.
     */
    public JMXWrapperObject()
    {
        initialized = false;
    }

    /**
     * Constructor for StatsObject.
     *
     * @param  parentComp        subComp
     * @param  threadPool        Thread pool to be monitored.
     * @param  workQueue         Work queue to be monitored.
     * @param  inProcessCounter  Counter for files being processed.
     */
    public JMXWrapperObject(IManagedComponent parentComp, ThreadPoolExecutor threadPool,
                            BlockingQueue<Runnable> workQueue, AtomicInteger inProcessCounter)
    {
        if (parentComp == null)
        {
            initialized = false;
            return;
        }

        IManagedComponent subComp = parentComp.createSubComponent("DirectoryPoller",
                                                                  "Directory Poller",
                                                                  LogMessages.CONNECTOR_MANAGEMENT_DESCRIPTION,
                                                                  parentComp);

        seenFileCount = (IEventOccurrenceCounter) subComp.createPerformanceCounter("Seen files",
                                                                                   LogMessages.CNTR_INPUT_FILES,
                                                                                   CounterFactory.EVENT_OCCURRENCE_COUNTER);
        restartedFileCount = (IEventOccurrenceCounter) subComp.createPerformanceCounter("Restarted files",
                                                                                        LogMessages.CNTR_RESTARTED_FILES,
                                                                                        CounterFactory.EVENT_OCCURRENCE_COUNTER);
        successfulFileCount = (IEventOccurrenceCounter) subComp.createPerformanceCounter("Successfull files",
                                                                                         LogMessages.CNTR_PROCESSED_FILES_SUCCESS,
                                                                                         CounterFactory.EVENT_OCCURRENCE_COUNTER);
        errorFileCount = (IEventOccurrenceCounter) subComp.createPerformanceCounter("Error files",
                                                                                    LogMessages.CNTR_PROCESSED_FILES_ERROR,
                                                                                    CounterFactory.EVENT_OCCURRENCE_COUNTER);
        totalFileCount = (IEventOccurrenceCounter) subComp.createPerformanceCounter("Total files",
                                                                                    LogMessages.CNTR_PROCESSED_FILES_TOTAL,
                                                                                    CounterFactory.EVENT_OCCURRENCE_COUNTER);
        scanTime = (ITimerEventValueCounter) subComp.createPerformanceCounter("File scanning time",
                                                                              LogMessages.CNTR_FILE_SCAN_TIME,
                                                                              CounterFactory.TIMER_EVENT_VALUE_COUNTER);
        triggerTime = (ITimerEventValueCounter) subComp.createPerformanceCounter("SOAP trigger time",
                                                                                 LogMessages.CNTR_TRIGGER_TIME,
                                                                                 CounterFactory.TIMER_EVENT_VALUE_COUNTER);
        processingTime = (ITimerEventValueCounter) subComp.createPerformanceCounter("File processing time",
                                                                                    LogMessages.CNTR_PROCESSING_TIME,
                                                                                    CounterFactory.TIMER_EVENT_VALUE_COUNTER);
        fileSize = (IEventValueCounter) subComp.createPerformanceCounter("Input file size",
                                                                         LogMessages.CNTR_FILE_SIZE,
                                                                         CounterFactory.EVENT_VALUE_COUNTER);

        fileProcessingAlert = subComp.defineAlert(AlertLevel.WARNING, LogMessages.ALERT_FILE_ERROR,
                                                  LogMessages.ALERT_FILE_ERROR_DESC);

        threadPoolSettings = new ThreadPoolSettings(subComp, threadPool, workQueue,
                                                    inProcessCounter);

        subComp.registerComponentTree();

        this.comp = subComp;
        initialized = true;
    }

    /**
     * Removes the JMX counters.
     */
    public void cleanup()
    {
        if (initialized)
        {
            if (threadPoolSettings != null)
            {
                threadPoolSettings.cleanup();
            }

            comp.unregisterComponentTree();
        }
    }

    /**
     * Called when a file processing is restarted after connector restart.
     */
    public void onFileRestart()
    {
        try
        {
            if (initialized)
            {
                restartedFileCount.addEvent();
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
        }
    }

    /**
     * Called when a new file is seen.
     */
    public void onNewFile()
    {
        try
        {
            if (initialized)
            {
                seenFileCount.addEvent();
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
        }
    }

    /**
     * Called when the file processing ends.
     *
     * @param  success    <code>true</code> if the processing succeeded.
     * @param  startTime  Processing start time from the onProcessingStart() method.
     */
    public void onProcessingEnd(boolean success, long startTime)
    {
        try
        {
            if (initialized)
            {
                if (success)
                {
                    successfulFileCount.addEvent();
                }
                else
                {
                    errorFileCount.addEvent();
                }

                totalFileCount.addEvent();
                processingTime.finish(startTime);
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
        }
    }

    /**
     * Called when the file processing starts.
     *
     * @param   size  File size.
     *
     * @return  Returns the processing start time. This must be passed to the finish method.
     */
    public long onProcessingStart(long size)
    {
        try
        {
            if (initialized)
            {
                long t = processingTime.start();

                fileSize.addEvent((int) size);

                return t;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
            return -1;
        }
    }

    /**
     * Called when the file scanning ends.
     *
     * @param  startTime  Scan start time from the onScanStart() method.
     */
    public void onScanEnd(long startTime)
    {
        try
        {
            if (initialized)
            {
                scanTime.finish(startTime);
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
        }
    }

    /**
     * Called when the file scanning starts.
     *
     * @return  Returns the scan start time. This must be passed to the finish method.
     */
    public long onScanStart()
    {
        try
        {
            if (initialized)
            {
                return scanTime.start();
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
            return -1;
        }
    }

    /**
     * Called when the SOAP trigger ends.
     *
     * @param  startTime  Trigger time from the onTriggerStart() method.
     */
    public void onTriggerEnd(long startTime)
    {
        try
        {
            if (initialized)
            {
                triggerTime.finish(startTime);
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
        }
    }

    /**
     * Called when the SOAP trigger starts.
     *
     * @return  Returns the trigger start time. This must be passed to the finish method.
     */
    public long onTriggerStart()
    {
        try
        {
            if (initialized)
            {
                return triggerTime.start();
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            LOG.log(Severity.WARN, "Error while updating a JMX counter.", e);
            return -1;
        }
    }

    /**
     * Returns the fileProcessingAlert.
     *
     * @return  Returns the fileProcessingAlert.
     */
    public IAlertDefinition getFileProcessingAlert()
    {
        return fileProcessingAlert;
    }

    /**
     * Returns the managed component.
     *
     * @return  Returns the managed component.
     */
    public IManagedComponent getManagedComponent()
    {
        if (initialized)
        {
            return comp;
        }
        else
        {
            return null;
        }
    }
}

/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import com.eibus.management.IManagedComponent;
import com.eibus.management.ISettingsCollection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements JMX settings and counters for a thread pool.
 *
 * @author  mpoyhone
 */
public class ThreadPoolSettings
{
    /**
     * Counter.
     */
    private AtomicInteger inProcessCounter;

    /**
     * Constructor for ThreadPoolSettings.
     *
     * @param  comp              JMX component.
     * @param  threadPool        Thread pool to be monitored.
     * @param  workQueue         Work queue to be monitored.
     * @param  inProcessCounter  In-process file counter to be monitored.
     */
    public ThreadPoolSettings(IManagedComponent comp, ThreadPoolExecutor threadPool,
                              BlockingQueue<Runnable> workQueue, AtomicInteger inProcessCounter)
    {
        this.inProcessCounter = inProcessCounter;

        if (comp != null)
        {
            ISettingsCollection coll = comp.getSettingsCollection();

            if ((coll != null) && (threadPool != null))
            {
                coll.defineHotSetting("minConcurrentWorkers", LogMessages.HOTSETTING_MIN_WORKERS,
                                      "corePoolSize", threadPool, null,
                                      DirectoryPoller.DEFAULT_WORKER_MIN_THREAD_COUNT, 1, 200);
                coll.defineHotSetting("maxConcurrentWorkers", LogMessages.HOTSETTING_MIN_WORKERS,
                                      "maximumPoolSize", threadPool, null,
                                      DirectoryPoller.DEFAULT_WORKER_MAX_THREAD_COUNT, 1, 200);
            }

            comp.createPropertyBasedValueCounter("Number of active worker threads",
                                                 LogMessages.CNTR_CUR_WORKER_THREADS, "activeCount",
                                                 threadPool);

            comp.createPropertyBasedValueCounter("Number of files in the processing folder",
                                                 LogMessages.CNTR_CUR_FILES_IN_PROCESS,
                                                 "inProcessCount", this);

            comp.createPropertyBasedValueCounter("Current thread pool queue size",
                                                 LogMessages.CNTR_CUR_WORK_QUEUE_SIZE, "size",
                                                 new QueueWrapper(workQueue));
        }
    }

    /**
     * Cleans up this object.
     */
    public void cleanup()
    {
    }

    /**
     * Returns in-process file count value.
     *
     * @return  In-process file count value.
     */
    public int getInProcessCount()
    {
        return (inProcessCounter != null) ? inProcessCounter.get() : 0;
    }

    /**
     * JavaBean wrapper for a queue.
     *
     * @author  $author$
     */
    public static class QueueWrapper
    {
        /**
         * Queue.
         */
        private BlockingQueue<Runnable> workQueue;

        /**
         * Creates a new QueueWrapper object.
         *
         * @param  workQueue  Queue to be wrapped.
         */
        QueueWrapper(BlockingQueue<Runnable> workQueue)
        {
            this.workQueue = workQueue;
        }

        /**
         * Returns queue's size.
         *
         * @return  Current size.
         */
        public int getSize()
        {
            return workQueue.size();
        }
    }
}

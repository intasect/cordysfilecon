
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

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Implements a file which is used for exclusive access to a folder.
 *
 * @author  mpoyhone
 */
public class LockFile
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(LockFile.class);
    /**
     * Name of the default lock file.
     */
    private static final String DEFAULT_LOCK_FILE = "__FCLOCK.LCK";
    /**
     * Contains the file channel from which the lock was acquired from.
     */
    private FileChannel channel;
    /**
     * Contains the file handle.
     */
    private RandomAccessFile file;
    /**
     * Contains the file lock.
     */
    private FileLock lock;
    /**
     * Locking state.
     */
    private boolean lockAquired = false;
    /**
     * Lock file path.
     */
    private File lockFile;

    /**
     * Constructor for LockFile. This version uses the default lock file name.
     *
     * @param  folder  Lock file parent folder.
     */
    public LockFile(File folder)
    {
        lockFile = new File(folder, DEFAULT_LOCK_FILE);
    }

    /**
     * Constructor for LockFile.
     *
     * @param  folder        Lock file parent folder.
     * @param  lockFileName  Name of the lock file in the parent folder.
     */
    public LockFile(File folder, String lockFileName)
    {
        lockFile = new File(folder, lockFileName);
    }

    /**
     * Opens the lock file and tries to acquire the lock. If the method succeeds, then the lock must
     * be release using the release() method.
     *
     * @throws  IOException  Thrown if the locking failed.
     */
    public void acquire()
                 throws IOException
    {
        acquire(false);
    }

    /**
     * Opens the lock file and tries to acquire the lock. If the method succeeds, then the lock must
     * be release using the release() method.
     *
     * @param   returnStatus  If <code>true</code> this method returns the lock status: <code>
     *                        true</code> if the lock was acquired or <code>false</code> the file
     *                        was locked by another process. If this parameter is <code>false</code>
     *                        this method will throw an exception even when the lock is held by
     *                        another process.
     *
     * @return  opens the lock file and tries to acquire the lock.
     *
     * @throws  IOException  Thrown if the locking failed.
     */
    public boolean acquire(boolean returnStatus)
                    throws IOException
    {
        if (file != null)
        {
            throw new IOException("Lock is already acquired.");
        }

        try
        {
            file = new RandomAccessFile(lockFile, "rw");
            channel = file.getChannel();
            lock = channel.tryLock();
        }
        catch (Throwable e)
        {
            // Sun file locking can thrown an java.lang.Error in some cases, e.g. when
            // a network share is disconnected! 
            // That is why we have to catch all exceptions.
            
            if (LOG.isDebugEnabled())
            {
                LOG.debug("An error occurred while trying to acquire lock file: " + lockFile, e);
            }

            release();

            if (e instanceof IOException) {
                // Just re-throw the exception.
                throw (IOException) e;
            }
             
            // Wrap the exception.
            throw new IOException("Unexpected exception while trying to acquire lock file: " + lockFile, e);
        }

        if (lock == null)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Unable to acquire the lock file: " + file);
            }

            release();

            if (returnStatus)
            {
                return false;
            }
            else
            {
                throw new IOException("Unable to acquire the lock file: " + lockFile +
                                      " It is probably used by another process.");
            }
        }
        else
        {
            lockAquired = true;
            return true;
        }
    }

    /**
     * Releases the lock file.
     */
    public void release()
    {
        if (lock != null)
        {
            try
            {
                lock.release();
            }
            catch (Throwable e)
            {
                // Same as for acquire, but here we just catch all exceptions to be sure. 
                
                if (LOG.isWarningEnabled())
                {
                    LOG.log(Severity.WARN, "An error occurred while releasing the file lock.", e);
                }
            }
            lock = null;
        }

        if (channel != null)
        {
            try
            {
                channel.close();
            }
            catch (Throwable e)
            {
                // Same as for acquire, but here we just catch all exceptions to be sure. 
                
                if (LOG.isWarningEnabled())
                {
                    LOG.log(Severity.WARN, "An error occurred while closing the lock file channel.",
                            e);
                }
            }
            channel = null;
        }

        if (file != null)
        {
            try
            {
                file.close();
            }
            catch (Throwable e)
            {
                // Same as for acquire, but here we just catch all exceptions to be sure. 
                
                if (LOG.isWarningEnabled())
                {
                    LOG.log(Severity.WARN, "An error occurred while closing the lock file.", e);
                }
            }
            file = null;
        }

        if ((lockFile != null) && lockAquired)
        {
            if (lockFile.exists() && !lockFile.delete())
            {
                if (LOG.isWarningEnabled())
                {
                    LOG.log(Severity.WARN, "Unable to delete lock file: " + lockFile);
                }
            }
        }

        lockAquired = false;
    }

    /**
     * Returns the lock file object.
     *
     * @return  Lock file.
     */
    public File getFile()
    {
        return lockFile;
    }

    /**
     * @see  java.lang.Object#finalize()
     */
    @Override
    protected void finalize()
                     throws Throwable
    {
        // Release the lock. Just to be sure, this is
        // wrapped inside a try-catch.
        try
        {
            release();
        }
        catch (Throwable ignored)
        {
        }

        super.finalize();
    }
}

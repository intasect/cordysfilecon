/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.util.FileUtils;

/**
 * Test cases for the LockFile class.
 *
 * @author mpoyhone
 */
public class LockFileTest extends FileConnectorTestCase
{

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.LockFile#LockFile(java.io.File)}.
     */
    public void testLockFileFile()
    {
        LockFile f = new LockFile(tempFolder);
        
        assertEquals(f.getFile().getParentFile(), tempFolder);
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.LockFile#LockFile(java.io.File, java.lang.String)}.
     */
    public void testLockFileFileString()
    {
        LockFile f = new LockFile(tempFolder, "mylockfile.tmp");
        
        assertEquals(f.getFile().getParentFile(), tempFolder);
        assertEquals(f.getFile(), new File(tempFolder, "mylockfile.tmp"));
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.LockFile#acquire()}.
     */
    public void testAcquire_NotLocked()
    {
        LockFile f = new LockFile(tempFolder);
        
        try
        {
            f.acquire();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Locking failed: " + e.getMessage());
        }
        finally {
            f.release();
        }
    }
    

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.LockFile#acquire()}.
     */
    public void testAcquire_Locked() throws Exception
    {
        LockFile f = new LockFile(tempFolder);
        LockerProcess proc = new LockerProcess(f.getFile());
        
        // Check that we can lock the file.
        try {
            f.acquire();
            f.release();
        }
        catch (IOException e) {
            e.printStackTrace();
            fail("Locking failed: " + e.getMessage());
        }
        
        try
        {
            // Ask the other process to lock it
            proc.start();
            proc.acquire();
            
            // Check that we cannot acquire the lock
            try {
                f.acquire();
                fail("Locking succeeded even though the file was locked.");
            }
            catch (IOException expected) {
            }
            
            // Ask the other process to release the lock
            proc.release();
            
            // Try to acquire the lock.
            try {
                f.acquire();
            }
            catch (IOException e) {
                e.printStackTrace();
                fail("Locking failed: " + e.getMessage());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Locking process failed: " + e.getMessage());
        }
        finally {
            f.release();
            proc.stop();
        }
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.LockFile#acquire(boolean)}.
     */
    public void testAcquireBoolean_NotLocked()
    {
        LockFile f = new LockFile(tempFolder);
        
        try
        {
            boolean status = f.acquire(true);
            
            assertEquals(true, status);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Locking failed: " + e.getMessage());
        }
        finally {
            f.release();
        }
    }
    
    private static class LockerProcess
    {
        File file;
        Process proc;
        BufferedReader in;
        BufferedWriter out;
        
        public LockerProcess(File lockFile) {
            file = lockFile;
        }
        
        public void start() throws Exception 
        {
            proc = executeJavaProcess(FileLockerProcess.class, file.getParentFile().getAbsolutePath(), file.getName());
            in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            out =  new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
            
            String cmd = in.readLine();
            
            if (! "HELLO".equals(cmd)) {
                throw new IllegalStateException("Invalid client hello received: " + cmd);
            }
        }
        
        public void stop() throws Exception
        {
            try {
                writeCmd("exit");
            }
            catch (IOException ignored) {
                // Ignore any exception which might be caused by the pipe being closed.
            }
        }
        
        public void end() throws Exception
        {
            FileUtils.closeReader(in);
            FileUtils.closeWriter(out);
        }
        
        public void acquire() throws Exception
        {
            writeCmd("acquire");
        }

        public void release() throws Exception
        {
            writeCmd("release");
        }

        public void writeCmd(String cmd) throws Exception
        {
            if (out == null || in == null) {
                throw new IOException("Not initialized.");
            }
            
            out.write(cmd + "\n");
            out.flush();
            
            String res = in.readLine();
            
            if (! ("ACK-" + cmd).equals(res)) {
                throw new IllegalStateException("Invalid client response received: " + res);
            }
        }
    }
}

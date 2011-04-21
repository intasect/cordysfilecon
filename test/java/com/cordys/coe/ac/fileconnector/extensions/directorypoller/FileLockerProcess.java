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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Arrays;

/**
 * External process which tries to lock the file. This is needed because file locks
 * are process specific.
 *
 * @author mpoyhone
 */
public class FileLockerProcess
{
    private static PrintStream dbg = null;
    
    public static void main(String[] args)
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
        
        try {
            dbg = new PrintStream("d:/temp/locker.log");
            
            System.setOut(dbg);
            System.setErr(dbg);
            
            if (dbg != null) {
                dbg.println("FileLockerProcess starting. Args: " + Arrays.toString(args));
                dbg.flush();
            }
            
            LockFile lockFile = new LockFile(new File(args[0]), args[1]);
            
            out.write("HELLO\n");
            out.flush();
            
            if (dbg != null) {
                dbg.println("Wrote client HELLO.");
                dbg.flush();
            }
        
            while (true) {
                String cmd = in.readLine();
                
                if (dbg != null) {
                    dbg.println("Command: " + cmd);
                    dbg.flush();
                }

                if ("acquire".equals(cmd)) {
                    lockFile.acquire();
                } else if ("release".equals(cmd)) {
                    lockFile.release();
                } else if ("exit".equals(cmd)) {
                    out.write("ACK-exit\n");
                    out.flush();
                    break;
                } else {
                    throw new IllegalArgumentException("Invalid command: " + cmd);
                }
                
                out.write("ACK-" + cmd + "\n");
                out.flush();
            }
        }
        catch (Exception e) {
            if (dbg != null) {
                dbg.println("Got an exception.");
                e.printStackTrace(dbg);
                dbg.flush();
            }
        
            try {
                out.write("ERROR: " + e.getMessage());
                out.flush();
            }
            catch (Exception ignored) {
            }
            
            System.exit(50);
        }
        
        if (dbg != null) {
            dbg.println("FileLockerProcess exiting.");
            dbg.flush();
            dbg.close();
        }
        
        System.exit(0);
    }
}
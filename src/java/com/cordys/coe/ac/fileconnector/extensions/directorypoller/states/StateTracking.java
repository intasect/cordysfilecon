/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Folder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * File state for tracking for modifications in the input folder. This state is not written to the
 * file state log during execute (as it is called by the poller thread loop), but by the
 * StateMoveToProcessing execute method.
 *
 * @author  mpoyhone
 */
public class StateTracking
    implements IFileState
{
    /**
     * Contains the file context object.
     */
    private FileContext fileContext;
    /**
     * Contains the previous state.
     */
    private IFileState prevState;

    /**
     * Constructor for StateTracking.
     *
     * @param  currentState  Current state which will become the parent state of this state
     * @param  fileContext   File processing context object.
     */
    public StateTracking(IFileState currentState, FileContext fileContext)
    {
        this.fileContext = fileContext;
        this.prevState = currentState;
    }

    /**
     * Checks if the file has been modified. If the file size hasn't been growing and the
     * modification time has stayed the same for the duration of the trackTime parameter, the file
     * is considered not to have been modified.
     *
     * @return  <code>true</code> if the file was modified.
     */
    public boolean checkForModification()
    {
        File file = fileContext.getCurrentFile();
        Folder inputFolder = fileContext.getInputFolder();
        long curLength = file.length();
        long curLastModified = file.lastModified();

        if ((curLength != fileContext.getFileSize()) ||
                (curLastModified != fileContext.getFileLastModified()))
        {
            // Length or last modified date has changed.
            fileContext.setFileSize(curLength);
            fileContext.setFileLastModified(curLastModified);
            fileContext.setLastNonModifiedCheck(-1);
            return true;
        }

        long now = System.currentTimeMillis();

        if (fileContext.getLastNonModifiedCheck() == -1)
        {
            // This is the first time the file stays the same.
            fileContext.setLastNonModifiedCheck(now);
            return true;
        }
        else
        {
            if ((now - fileContext.getLastNonModifiedCheck()) < inputFolder.getTrackTime())
            {
                // We haven't reached the track time yet.
                return true;
            }
        }

        // File hasn't been modified in the track time, so indicate
        // that status.
        return false;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#execute()
     */
    public boolean execute()
                    throws FileStateException
    {
        if (checkForModification())
        {
            // File was modified, so we cannot move it yet.
            return false;
        }

        // Set the next state.
        moveToNextState();

        return true;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#readFromLog(DataInputStream,
     *       boolean, XMLStreamWriter)
     */
    public void readFromLog(DataInputStream in, boolean finished, XMLStreamWriter logWriter)
                     throws IOException, XMLStreamException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#writeToLog(DataOutputStream)
     */
    public void writeToLog(DataOutputStream out)
                    throws IOException
    {
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#getPreviousState()
     */
    public IFileState getPreviousState()
    {
        return prevState;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#getStateType()
     */
    public EFileState getStateType()
    {
        return EFileState.TRACKING;
    }

    /**
     * @see  com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.IFileState#isFinished()
     */
    public boolean isFinished()
    {
        return false;
    }

    /**
     * Moves the state in the content to the next state of this state.
     */
    private void moveToNextState()
    {
        fileContext.setCurrentState(EFileState.MOVE_TO_PROCESSING.createState(fileContext
                                                                              .getCurrentState(),
                                                                              fileContext));
    }
}

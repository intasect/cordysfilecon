/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;

/**
 * Contains file state enumeration values. This also contains factory methods for creating the state
 * objects.
 *
 * @author  mpoyhone
 */
public enum EFileState
{
    /**
     * File state for tracking for modifications in the input folder.
     */
    TRACKING,
    /**
     * File state for moving the file to the processing folder.
     */
    MOVE_TO_PROCESSING,
    /**
     * File state for files that have been moved to the processing folder.
     */
    IN_PROCESSING,
    /**
     * File state for moving the file to the processing folder.
     */
    MOVE_TO_APP_PROCESSING,
    /**
     * File state where web service trigger is call is prepared and will be executed.
     * From this state on the web service is considered to be executed
     * and will not be executed again.
     */
    TRIGGER,
    /**
     * File state when file processing is finished.
     */
    FINISHED,
    /**
     * Marker state that indicates the file processing has been resumed after a crash.
     */
    RESUME,
    /**
     * File processing has ended in an error and it should be moved to the error folder.
     */
    ERROR;

    /**
     * Creates a new file state object for this enumeration value.
     *
     * @param   currentState  Current state or <code>null</code> if this is the initial state.
     * @param   fileContext   fileWorker File worker object which contains all necessary information
     *                        for processing the file.
     *
     * @return  New state object.
     */
    public IFileState createState(IFileState currentState, FileContext fileContext)
    {
        switch (this)
        {
            case TRACKING:
                return new StateTracking(currentState, fileContext);

            case MOVE_TO_PROCESSING:
                return new StateMoveToProcessing(currentState, fileContext);

            case IN_PROCESSING:
                return new StateInProcessing(currentState, fileContext);

            case MOVE_TO_APP_PROCESSING:
                return new StateMoveToAppProcessing(currentState, fileContext);

            case TRIGGER:
                return new StateTrigger(currentState, fileContext);

            case FINISHED:
                return new StateFinished();

            case RESUME:
                return new StateResume(currentState, fileContext);

            default:
                throw new IllegalStateException("Unhandle file state: " + this);
        }
    }
}

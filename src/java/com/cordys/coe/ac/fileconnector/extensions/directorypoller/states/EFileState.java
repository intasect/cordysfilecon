
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


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

/**
 * Interface for handling errors during file processing. The handler is responsible for moving the
 * file away from input or processing folder.
 *
 * @author  mpoyhone
 */
public interface IFileErrorHandler
{
    /**
     * Called when the error should be handled.
     *
     * @param  fileContext  File that ended in error.
     * @param  poller       The main poller thread object.
     * @param  error        Contains the exception that caused this error.
     */
    void handleFileError(FileContext fileContext, DirectoryPollerThread poller, Throwable error);
}

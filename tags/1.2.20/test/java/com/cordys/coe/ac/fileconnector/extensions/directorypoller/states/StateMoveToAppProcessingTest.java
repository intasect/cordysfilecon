/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.extensions.directorypoller.states;

import java.io.File;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.FileContext;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Folder;
import com.cordys.coe.ac.fileconnector.extensions.directorypoller.Utils;

/**
 * Tests file moving to the application processing folder. 
 *
 * @author mpoyhone
 */
public class StateMoveToAppProcessingTest extends FileConnectorTestCase
{
    private File inputFolder;
    private File processingFolder;
    private File appProcessingFolder;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        inputFolder = new File(tempFolder, "input");
        inputFolder.mkdirs();    
        processingFolder = new File(tempFolder, "processing");
        processingFolder.mkdirs();    
        appProcessingFolder = new File(tempFolder, "app-processing");
        appProcessingFolder.mkdirs();        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateMoveToProcessing#execute()}.
     */
    public void testExecute() throws Exception
    {
        File inputFile = createTextFile(new File(inputFolder, "input.text"), "input data");
        FileContext fc = new FileContext(inputFile, null, null, null, processingFolder, appProcessingFolder);
        Folder folder = new Folder();
        
        folder.setUseAppProcessingFolder(true);
        fc.setFileId("TEST123");
        fc.setInputFolder(folder);
        fc.setProcessingFolder(processingFolder);
        
        try {
            moveFileToAppProcessingFolder(fc);
        }
        finally {
            fc.closeLog();
        }
        
        IFileState state = fc.getCurrentState();
        File appProcFile = new File(appProcessingFolder, fc.getFileId() + Utils.getFileExtension(inputFile));
        
        assertEquals(EFileState.TRIGGER, state.getStateType());
        assertTrue("File not in the application processing folder.", appProcFile.exists());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateMoveToProcessing#isFinished()}.
     */
    public void testIsFinished()
    {
        assertFalse(new StateMoveToProcessing(null, null).isFinished());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateMoveToProcessing#resume()}.
     * 
     * File is left in the processing folder by execute(). Test if the resume will move it in the application processing folder.
     */
    public void testResume_FileInProcessing() throws Exception
    {
        FileContext fc = resumeInit();
        IFileState state = new StateMoveToAppProcessing(null, fc);

        appProcessingFolder.delete();
        
        try {
            executeFailingMove(state);
        }
        finally {
            fc.closeLog();
        }
          
        appProcessingFolder.mkdir();
        
        FileContext fc2 = testResume(fc, null, false, true);
        
        assertEquals(EFileState.TRIGGER, fc2.getCurrentState().getStateType());
    }

    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateMoveToProcessing#resume()}.
     * 
     * File is left in the processing folder by execute(). Test if the resume fail if the file cannot still be moved.
     */
    public void testResume_FileInProcessing_Fail() throws Exception
    {
        FileContext fc = resumeInit();
        IFileState state = new StateMoveToAppProcessing(null, fc);

        appProcessingFolder.delete();
        
        try {
            executeFailingMove(state);
        }
        finally {
            fc.closeLog();
        }
        
        FileContext fc2 = testResume(fc, FileStateException.EType.RETRY, true, false);
        
        assertEquals(EFileState.MOVE_TO_APP_PROCESSING, fc2.getCurrentState().getStateType());
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateMoveToProcessing#getStateType()}.
     */
    public void testGetEnumValue()
    {
        assertEquals(EFileState.MOVE_TO_PROCESSING, new StateMoveToProcessing(null, null).getStateType());
    }
    
    /**
     * Executes all states until the file is in the processing folder.
     * 
     * @param fileContext File context.
     */
    private void moveFileToAppProcessingFolder(FileContext fileContext) throws Exception
    {
        IFileState state;

        state = new StateTracking(null, fileContext);
        fileContext.setCurrentState(state);
        
        while (true) {
            state.execute();
            
            if (state.getStateType() == EFileState.MOVE_TO_APP_PROCESSING) {
                break;
            }
            
            state = fileContext.getCurrentState();
            assertNotNull(state);
        }
    }
    
    private void executeFailingMove(IFileState state) throws Exception
    {
        try {
            state.execute();
            fail("Execute didn't fail.");
        }
        catch (Exception expected) {
        }
    }

    private FileContext resumeInit() throws Exception
    {
        File processingFile = new File(processingFolder, "input.text");
        FileContext fc = new FileContext(processingFile, null, null, null, processingFolder, appProcessingFolder);
        Folder inputFolder = new Folder();
        
        fc.setFileId("TEST123");
        fc.setInputFolder(inputFolder);
        fc.setCurrentFile(processingFile);
        fc.setProcessingFolder(processingFolder);   
        
        createTextFile(processingFile, "input data");
        
        return fc;
    }
    
    private FileContext testResume(FileContext fc, FileStateException.EType expectedResult, boolean fileInProcessing, boolean fileInAppProcessing) throws Exception
    {
        File processingFile = fc.getCurrentFile();
        FileContext fc2 = new FileContext(fc.getInputFolder(), null, null, appProcessingFolder);
        
        fc2.setProcessingFolder(processingFolder);
        fc2.setFileId(fc.getFileId());
        
        fc.closeLog();
        
        IFileState state2 = fc2.getLogReader().readLog(fc2, null);
        
        assertNotNull(state2);
        fc2.setCurrentState(state2);        
        
        try {
            if (expectedResult != null) {
                try {
                    state2.execute();
                }
                catch (FileStateException e) {
                    FileStateException.EType res = e.getType();
                    
                    assertEquals(expectedResult, res);
                }
            } else {
                try {
                    state2.execute();
                }
                catch (FileStateException e) {
                    e.printStackTrace();
                    fail("Resume failed: " + e.getMessage());
                }
            }
        }
        finally {
            fc2.closeLog();
        }
        
        File fileProcFolder = fc2.getProcessingFolder();
        
        assertNotNull("File processing folder not set.", fileProcFolder);
        assertTrue("File processing folder does not exist.", fileProcFolder.exists());
        
        File appProcessingFile = fc2.getCurrentFile();
        
        if (fileInAppProcessing) {
            assertTrue("File is not in the application processing folder.", appProcessingFile != null && appProcessingFile.exists());
        } else {
            assertFalse("File is in the application processing folder.", appProcessingFile != null && appProcessingFile.exists());
        }
        
        if (fileInProcessing) {
            assertTrue("File is not in the processing folder.", processingFile.exists());
        } else {
            assertFalse("File still exists in the processing folder.", processingFile.exists());
        }
        
        return fc2;
    }
}

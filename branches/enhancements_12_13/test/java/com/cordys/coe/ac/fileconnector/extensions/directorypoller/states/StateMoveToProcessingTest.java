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

/**
 * Test case for moving file to the processing folder.
 *
 * @author mpoyhone
 */
public class StateMoveToProcessingTest extends FileConnectorTestCase
{
    private File inputFolder;
    private File processingFolder;
    
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
        FileContext fc = initFile();
 
        if (! fc.getCurrentState().execute()) {
            fail("Execute returned false.");
        }
        
        assertExecuteResult(fc, true);
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
     * File is left in the input folder by execute().
     */
    public void testResume_FileInInput_Fail() throws Exception
    {
        FileContext fc = initFile();
        File procFile = new File(fc.getProcessingRootFolder(), fc.getFileId() + "/" + fc.getCurrentFile().getName());

        procFile.getParentFile().mkdirs();
        procFile.createNewFile();
        
        executeFailingMove(fc.getCurrentState());
        assertExecuteResult(fc, false);
    }
    
    /**
     * Test method for {@link com.cordys.coe.ac.fileconnector.extensions.directorypoller.states.StateMoveToProcessing#getStateType()}.
     */
    public void testGetEnumValue()
    {
        assertEquals(EFileState.MOVE_TO_PROCESSING, new StateMoveToProcessing(null, null).getStateType());
    }
    
    private FileContext initFile() throws Exception
    {
        File inputFile = createTextFile(new File(inputFolder, "input.text"), "input data");
        FileContext fc = new FileContext(inputFile, null, null, null, processingFolder, null);
        IFileState state = new StateMoveToProcessing(null, fc);
        Folder inputFolder = new Folder();
        
        fc.setOriginalFile(inputFile);
        fc.setFileId("TEST123");
        fc.setInputFolder(inputFolder);
        fc.setCurrentState(state);
        
        return fc;
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
    
    private void assertExecuteResult(FileContext fc, boolean expectSuccess)
    {        
        File fileProcFolder = fc.getProcessingFolder();
        File inputFile = fc.getOriginalFile();
        String name = inputFile.getName();
        File procFile = new File(fileProcFolder, name);
        
        if (expectSuccess) {
            assertNotNull("File processing folder not set.", fileProcFolder);
            assertTrue("File processing folder does not exist.", fileProcFolder.exists());
            assertTrue("Input file not in the processing folder.", procFile.exists());
            assertFalse("Input file still exists in the input folder.", inputFile.exists());
            assertEquals(EFileState.IN_PROCESSING, fc.getCurrentState().getStateType());
        } else {
            assertFalse("Input file moved to the processing folder.", procFile.exists());
            assertTrue("Input file does exists in the input folder.", inputFile.exists());
        }
    }
}

/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.cordys.coe.ac.fileconnector.FileConnectorTestCase;

/**
 * Test cases for the CountNumberOfLines method.
 *
 * @author mpoyhone
 */
public class CountNumberOfLinesMethodTest extends FileConnectorTestCase
{
    private Object[][] oaTestCases = { 
            // Windows line-feeds
            { "", "\r\n", 0 },
            { "\r\n123\r\n", "\r\n", 2 },
            { "\r\n123", "\r\n", 2 },
            { "321\r\n123", "\r\n", 2 },
            { "321\n123", "\r\n", 1 }, // Invalid eol separator.
            { "321\r123", "\r\n", 1 }, // Invalid eol separator.
            { "321\n\r123", "\r\n", 1 }, // Invalid eol separator.
            { "321\r\n123\n", "\r\n", 2 }, // Invalid eol separator.
            { "321\r\n123\r", "\r\n", 2 }, // Invalid eol separator.
            { "321\r\n123\n\r", "\r\n", 2 }, // Invalid eol separator.
            { "\r\n", "\r\n", 1 },
            { "\r\n123\r\n1", "\r\n", 3 },  
            
            // Unix line-feeds
            { "", "\n", 0 },
            { "\n123\n", "\n", 2 },
            { "\n123", "\n", 2 },
            { "321\n123", "\n", 2 },
            { "321\r123", "\n", 1 }, // Invalid eol separator.
            { "321\r123\r", "\n", 1 }, // Invalid eol separator.
            { "321\n123\r", "\n", 2 }, // Invalid eol separator.
            { "\n", "\n", 1 },
            { "\n123\n1", "\n", 3 },  
            
            // Read buffer boundary.
            { "1234_", "_",   1, 3 },
            { "1234_", "_",   1, 4 },
            { "1234_", "_",   1, 5 },
            { "1234_", "_",   1, 6 },
            { "1234_1", "_",   2, 4 },
            { "1234_1", "_",   2, 5 },
            { "1234_1", "_",   2, 6 },
            { "1234_1", "_",   2, 7 },            
    };
    
    /**
     * Test method for CountNumberOfLinesMethod.countInputLines.
     * @throws IOException Thrown
     */
    public void testCountInputLines() throws IOException
    {
        for (Object[] oaTestCase : oaTestCases)
        {
            String sInput = (String) oaTestCase[0];
            String sSeparator = (String) oaTestCase[1];
            int iExpectedLines = (Integer) oaTestCase[2];
            int iBufferSize = (oaTestCase.length >= 4? (Integer) oaTestCase[3] : 100);
            
            Reader rReader = new StringReader(sInput);
            int iActualLines = CountNumberOfLinesMethod.countInputLines(rReader, sSeparator.toCharArray(), iBufferSize);
            
            assertEquals("Input failed: " + sInput, iExpectedLines, iActualLines);
        }
    }
    
}

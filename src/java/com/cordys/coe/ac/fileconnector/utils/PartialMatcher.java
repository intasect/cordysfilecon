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
 package com.cordys.coe.ac.fileconnector.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An utility class to check match if a string matches the beginning of a regular expression. Can be
 * used to find early matches while going though substrings that are part of the final string that
 * fully matches the regural expression. The regular expression have a simple form "a b ..." and not
 * e.g. "(a(b))".
 *
 * @author  mpoyhone
 */
public class PartialMatcher
{
    /**
     * The array holding the patterns to be matched in the same order.
     */
    public Pattern[] paRegExp;

    /**
     * Creates a new PartialMatcher object.
     *
     * @param   saRegExpElements  Array holding the patterns to be matched in the same order
     *
     * @throws  IllegalArgumentException  Thrown if parsing of a pattern failed.
     */
    public PartialMatcher(String[] saRegExpElements)
                   throws IllegalArgumentException
    {
        // Create Pattern classes for every expression.
        paRegExp = new Pattern[saRegExpElements.length];

        for (int i = 0; i < saRegExpElements.length; i++)
        {
            String sRegExp = saRegExpElements[i];

            try
            {
                paRegExp[i] = Pattern.compile(sRegExp);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Unable to parse pattern " + sRegExp + " : " +
                                                   e);
            }
        }
    }

    /**
     * Returns true if the input matches completely with all the match patterns.
     *
     * @param   caInput  The input to be matched.
     *
     * @return  True, if the input matched completely, false otherwise.
     */
    public boolean isCompleteMatch(CharSequence caInput)
    {
        int iScanPos = 0;
        int i;

        // Go through all the patterns and see if every one matches.
        for (i = 0; i < paRegExp.length; i++)
        {
            Matcher mMatcher = paRegExp[i].matcher(caInput);

            // See if this part matches the current position.
            if (!mMatcher.find(iScanPos) || (mMatcher.start() != iScanPos))
            {
                return false;
            }

            // See if we have matched the whole string.
            if (mMatcher.end() >= caInput.length())
            {
                // Yes. This is at least a partial match of the regular expression.
                iScanPos = mMatcher.end();
                break;
            }

            // Scan the next part at the end of this part.
            iScanPos = mMatcher.end();
        }

        // If the whole string was matched and all the parts were also matched then
        // this is a complete match.
        return (i >= (paRegExp.length - 1)) && (iScanPos == caInput.length());
    }

    /**
     * Returns true if the input matches partially with all the match patterns. This means that the
     * input string must be matched to the end before all patterns are matched. This method return
     * true also in the case the input was parsed completely.
     *
     * @param   caInput  The input to be matched.
     *
     * @return  True, if the input matched partially or completely, false otherwise.
     */
    public boolean isPartialMatch(CharSequence caInput)
    {
        int iScanPos = 0;

// Go through all the patterns and see if all or some from the beginning matches.
        for (int i = 0; i < paRegExp.length; i++)
        {
            Matcher mMatcher = paRegExp[i].matcher(caInput);

            // See if this part matches the current position.
            if (!mMatcher.find(iScanPos) || (mMatcher.start() != iScanPos))
            {
                return false;
            }

            // See if we have matched the whole string.
            if (mMatcher.end() >= caInput.length())
            {
                // Yes. This is at least a partial match of the regular expression.
                return true;
            }

            // Scan the next part at the end of this part.
            iScanPos = mMatcher.end();
        }

        // The last expression did not match the string completely
        // so this isn't a match.
        return false;
    }
}

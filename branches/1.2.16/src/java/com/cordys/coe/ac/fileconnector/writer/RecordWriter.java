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
 package com.cordys.coe.ac.fileconnector.writer;

import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.utils.DateTimeUtils;

import com.eibus.xml.nom.Node;

import java.io.IOException;
import java.io.Writer;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;

/**
 * A class to write formatted records from XML structures to a text file.
 *
 * @author  mpoyhone
 */
public class RecordWriter
{
    /**
     * Contains valid values for boolean data type.
     */
    protected static Map<String, Boolean> mBooleanValueMap = new HashMap<String, Boolean>();

    // Static initializer for this class
    static
    {
        // Insert valid boolean values to the map.
        mBooleanValueMap.put("false", new Boolean(false));
        mBooleanValueMap.put("true", new Boolean(true));
        mBooleanValueMap.put("0", new Boolean(false));
        mBooleanValueMap.put("1", new Boolean(true));
        mBooleanValueMap.put("n", new Boolean(false));
        mBooleanValueMap.put("y", new Boolean(true));
        mBooleanValueMap.put("off", new Boolean(false));
        mBooleanValueMap.put("on", new Boolean(true));
    }

    /**
     * The configuration object for this writer.
     */
    protected WriterConfig wcConfig;

    /**
     * Creates a new RecordWriter object.
     *
     * @param  wcConfig  The configuration to be used when writing records.
     */
    public RecordWriter(WriterConfig wcConfig)
    {
        this.wcConfig = wcConfig;
    }

    /**
     * Writes the record from the XML structure to the output writer according to the configuration/
     *
     * @param   sFileType    File type in the configuration
     * @param   iRecordNode  The XML node to be written
     * @param   wOutput      The output writer object
     *
     * @throws  IOException      Thrown if the writing failed.
     * @throws  ConfigException  Thrown on an invalid configuration
     */
    public void writeRecord(String sFileType, int iRecordNode, Writer wOutput)
                     throws IOException, ConfigException
    {
        // Check that we have a proper config.
        if ((wcConfig == null) || (wcConfig.mConfigMap == null))
        {
            throw new ConfigException("RecordWriter is uninitialized.");
        }

        WriterConfig.FileType ftConfigType;

        // Find the FileType object form the configuration that matches this file type.
        if ((ftConfigType = (WriterConfig.FileType) wcConfig.mConfigMap.get(sFileType)) == null)
        {
            throw new ConfigException("File type '" + sFileType + "' not found in configuration.");
        }

        // Output the record recursively.
        if (ftConfigType.sRootSelect != null)
        {
            handleSelect(iRecordNode, ftConfigType.sRootSelect, wOutput, true);
        }
    }

    /**
     * Formats a boolean output field according to the configuration.
     *
     * @param   orOutput  The output configuration element.
     * @param   sValue    The value to be formatted.
     *
     * @return  The formatted value.
     *
     * @throws  IOException
     */
    protected String formatBoolean(WriterConfig.OutputRule orOutput, String sValue)
                            throws IOException
    {
        assert orOutput.iType == WriterConfig.OutputRule.ORT_FLOAT;
        assert orOutput.saBooleanValueNames != null;
        assert orOutput.saBooleanValueNames.length == 2;

        // Remove extra spaces from the input.
        sValue = sValue.trim();

        // Convert it to lower case
        sValue = sValue.toLowerCase();

        // For empty (and non-existing) values use 'false'.
        if (sValue.length() == 0)
        {
            sValue = "false";
        }

        // Find the boolean object based on the value
        Boolean bValue = (Boolean) mBooleanValueMap.get(sValue);

        if (bValue == null)
        {
            throw new IOException("Illegal boolean value '" + sValue + "'");
        }

        // Return the configured output value.
        return orOutput.saBooleanValueNames[bValue.booleanValue() ? 1 : 0];
    }

    /**
     * Formats the current date output field according to the configuration.
     *
     * @param   orOutput  The output configuration element.
     *
     * @return  The formatted value.
     *
     * @throws  IOException
     */
    protected String formatCurrentDate(WriterConfig.OutputRule orOutput)
                                throws IOException
    {
        assert orOutput.iType == WriterConfig.OutputRule.ORT_DATE;
        assert orOutput.dfOutDateFormat != null;

        // Get the current date and time.
        Date dDate = new Date();

        // Date format object have to be cloned because it is not thread safe.
        SimpleDateFormat dsfOutFormat = (SimpleDateFormat) orOutput.dfOutDateFormat.clone();

        // Convert the date to output type.
        try
        {
            return dsfOutFormat.format(dDate);
        }
        catch (Exception e)
        {
            throw new IOException("Unable to format the date : " + e);
        }
    }

    /**
     * Formats a date output field according to the configuration.
     *
     * @param   orOutput  The output configuration element.
     * @param   sValue    The value to be formatted.
     *
     * @return  The formatted value.
     *
     * @throws  IOException
     */
    protected String formatDate(WriterConfig.OutputRule orOutput, String sValue)
                         throws IOException
    {
        assert orOutput.iType == WriterConfig.OutputRule.ORT_DATE;
        assert orOutput.dfInDateFormat != null;
        assert orOutput.dfOutDateFormat != null;

        // Remove extra spaces from the input.
        sValue = sValue.trim();

        // Date format objects have to be cloned because they are not thread safe.
        SimpleDateFormat dsfInFormat = (SimpleDateFormat) orOutput.dfInDateFormat.clone();
        SimpleDateFormat dsfOutFormat = (SimpleDateFormat) orOutput.dfOutDateFormat.clone();
        Date dDate;

        // Fix the millisecond problem with SimpleDateFormat
        sValue = DateTimeUtils.fixSoapDateTimeMillis(sValue, orOutput.sInDateFormat);

        // Parse the date from input string.
        try
        {
            dDate = dsfInFormat.parse(sValue);
        }
        catch (Exception e)
        {
            throw new IOException("Invalid input date '" + sValue + "' : " + e);
        }

        // Convert the date to output type.
        try
        {
            return dsfOutFormat.format(dDate);
        }
        catch (Exception e)
        {
            throw new IOException("Unable to format the date : " + e);
        }
    }

    /**
     * Formats a float output field according to the configuration.
     *
     * @param   orOutput  The output configuration element.
     * @param   sValue    The value to be formatted.
     *
     * @return  The formatted value.
     *
     * @throws  IOException
     */
    protected String formatFloat(WriterConfig.OutputRule orOutput, String sValue)
                          throws IOException
    {
        assert orOutput.iType == WriterConfig.OutputRule.ORT_FLOAT;

        if (sValue != null) {
            // Remove extra spaces from the input.
            sValue = sValue.trim();
        }
        
        // For empty (and non-existing) values use zero.
        if (sValue == null || sValue.length() == 0)
        {
            sValue = "0";
        }

        double dValue;

        // Parse the number from input string.
        try
        {
            dValue = Double.parseDouble(sValue);
        }
        catch (Exception e)
        {
            throw new IOException("Invalid input number '" + sValue + "'");
        }

        // If we have a number format configured, use it here.
        if (orOutput.nfNumberFormat != null)
        {
            try
            {
                return orOutput.nfNumberFormat.format(dValue);
            }
            catch (Exception e)
            {
                throw new IOException("Unable to format the numner : " + e);
            }
        }
        else
        {
            return "" + dValue;
        }
    }

    /**
     * Generates the output based on the current node and the given input configuration.
     *
     * @param   iCurrentNode  The current XML node.
     * @param   lRuleList     The configuration output element.
     *
     * @return  The output read from the XML strucuture.
     *
     * @throws  IOException
     */
    protected String generateInputText(int iCurrentNode, List<?> lRuleList)
                                throws IOException
    {
        // Check if we have input elements.
        if (lRuleList == null)
        {
            // No input elements found.
            return null;
        }

        // Yes we have. Process each element into a string buffer.
        StringBuffer sbRes = new StringBuffer(128);

        for (Iterator<?> iIter = lRuleList.iterator(); iIter.hasNext();)
        {
            Object oObj = iIter.next();

            if (oObj instanceof WriterConfig.InputRule)
            {
                WriterConfig.InputRule iInput = (WriterConfig.InputRule) oObj;

                readInput(iCurrentNode, iInput, sbRes);
            }
            else if (oObj instanceof WriterConfig.FilterRule)
            {
                WriterConfig.FilterRule frFilter = (WriterConfig.FilterRule) oObj;

                handleFilter(iCurrentNode, frFilter, sbRes);
            }
        }

        return sbRes.toString();
    }

    /**
     * Formats field according to the configured width and alignment parameters.
     *
     * @param   orOutput   The configuration element for this output field.
     * @param   sValue     The field input value to be formatted.
     * @param   bTruncate  If true and the field is longer than the specified with, the field is
     *                     truncated.
     *
     * @return  The formated value.
     *
     * @throws  IOException  Thrown if the action failed.
     */
    protected String handleFieldWidth(WriterConfig.OutputRule orOutput, String sValue,
                                      boolean bTruncate)
                               throws IOException
    {
        if (orOutput.iWidth <= 0)
        {
            // No field width specified.
            return sValue;
        }

        // Calculate the amount of padding necessary.
        int iNeededChars = (orOutput.iWidth - sValue.length());
        int iNeededBefore;
        int iNeededAfter;

        if (iNeededChars <= 0)
        {
            // No padding needed or possible.
            // Check if we need to truncate the field.
            if (bTruncate && (sValue.length() > orOutput.iWidth))
            {
                sValue = sValue.substring(0, orOutput.iWidth);
            }

            return sValue;
        }

        switch (orOutput.iAlign)
        {
            case WriterConfig.OutputRule.AT_LEFT:
                iNeededBefore = 0;
                iNeededAfter = iNeededChars;
                break;

            case WriterConfig.OutputRule.AT_RIGHT:
                iNeededBefore = iNeededChars;
                iNeededAfter = 0;
                break;

            case WriterConfig.OutputRule.AT_MIDDLE:
                iNeededBefore = iNeededChars / 2;
                iNeededAfter = iNeededChars - iNeededBefore;
                break;

            default:
                // Unknown alignment.
                return sValue;
        }

        // Add the paddings
        char chPadChar = orOutput.sFieldPadString.charAt(0);
        StringBuffer sbBuffer = new StringBuffer(128);

        for (int i = 0; i < iNeededBefore; i++)
        {
            sbBuffer.append(chPadChar);
        }

        sbBuffer.append(sValue);

        for (int i = 0; i < iNeededAfter; i++)
        {
            sbBuffer.append(chPadChar);
        }

        return sbBuffer.toString();
    }

    /**
     * Executes a filter element. Used to modify input text.
     *
     * @param   iCurrentNode    Current XML node
     * @param   frFilter        Filter rule object from configuration
     * @param   sbAppendBuffer  StringBuffer that received the modified text.
     *
     * @throws  IOException  Thrown if the operation failed.
     */
    protected void handleFilter(int iCurrentNode, WriterConfig.FilterRule frFilter,
                                StringBuffer sbAppendBuffer)
                         throws IOException
    {
        String sInputValue;

        // Generate the input value for this filter.
        sInputValue = generateInputText(iCurrentNode, frFilter.lInputList);

        if (sInputValue == null)
        {
            // No input found.
            return;
        }

        // Process the input according to filter type.
        switch (frFilter.iType)
        {
            case WriterConfig.FilterRule.FRT_LOWERCASE:
                sbAppendBuffer.append(sInputValue.toLowerCase());
                break;

            case WriterConfig.FilterRule.FRT_UPPERCASE:
                sbAppendBuffer.append(sInputValue.toUpperCase());
                break;

            case WriterConfig.FilterRule.FRT_REGEXP_REPLACE_ALL:
            {
                assert frFilter.pRegexpPattern != null;

                Matcher mMatcher = frFilter.pRegexpPattern.matcher(sInputValue);

                sInputValue = mMatcher.replaceAll(frFilter.sRegexpOutputString);

                if (sInputValue != null)
                {
                    sbAppendBuffer.append(sInputValue);
                }
            }
            break;

            case WriterConfig.FilterRule.FRT_REGEXP_REPLACE_FIRST:
            {
                assert frFilter.pRegexpPattern != null;

                Matcher mMatcher = frFilter.pRegexpPattern.matcher(sInputValue);

                sInputValue = mMatcher.replaceFirst(frFilter.sRegexpOutputString);

                if (sInputValue != null)
                {
                    sbAppendBuffer.append(sInputValue);
                }
            }
            break;
        }
    }

    /**
     * Processes the foreach-element.
     *
     * @param   iCurrentNode  The current top node.
     * @param   flForLoop     The foreach element that should be handled at this level.
     * @param   wOutput       Output from output-elements is written to this writer.
     *
     * @throws  IOException  Thrown if the writing failed.
     */
    protected void handleForEach(int iCurrentNode, WriterConfig.ForLoop flForLoop, Writer wOutput)
                          throws IOException
    {
        if (flForLoop.sLoopSelect == null)
        {
            return;
        }

        int[] xaElements = null;

        if ((flForLoop.xqQuery != null) && (iCurrentNode != 0))
        {
            xaElements = flForLoop.xqQuery.findAllNodes(iCurrentNode);
        }

        if (xaElements == null)
        {
            xaElements = new int[0];
        }

        int iLoopCount = xaElements.length;

        if ((flForLoop.iMaxCount != -1) && (iLoopCount > flForLoop.iMaxCount))
        {
            iLoopCount = flForLoop.iMaxCount;
        }

        if ((flForLoop.iMinCount != -1) && (iLoopCount < flForLoop.iMinCount))
        {
            iLoopCount = flForLoop.iMinCount;
        }

        for (int i = 0; i < iLoopCount; i++)
        {
            int xNode = ((i < xaElements.length) ? xaElements[i] : 0);

            handleSelect(xNode, flForLoop.sLoopSelect, wOutput, false);
        }
    }

    /**
     * Processes the if-element.
     *
     * @param   iCurrentNode  The current top node.
     * @param   iIf           The if element that should be handled at this level.
     * @param   wOutput       Output from output-elements is written to this writer.
     *
     * @throws  IOException  Thrown if the writing failed.
     */
    protected void handleIf(int iCurrentNode, WriterConfig.If iIf, Writer wOutput)
                     throws IOException
    {
        WriterConfig.IfBranch ibBranch = iIf.execute(iCurrentNode);

        if (ibBranch == null)
        {
            return;
        }

        if (ibBranch.sBranchSelect == null)
        {
            return;
        }

        handleSelect(iCurrentNode, ibBranch.sBranchSelect, wOutput, false);
    }

    /**
     * Outputs the current node according to the output rule.
     *
     * @param   iCurrentNode  The current node.
     * @param   orOutput      The output configuration node
     * @param   wOutput       Output from output-elements is written to this stream.
     *
     * @throws  IOException  Thrown if the writing failed.
     */
    protected void handleOutput(int iCurrentNode, WriterConfig.OutputRule orOutput, Writer wOutput)
                         throws IOException
    {
        String sNodeValue = null;

        // Check if the output element has a query path.
        if (orOutput.xqQuery != null)
        {
            if (iCurrentNode != 0)
            {
                sNodeValue = orOutput.xqQuery.findValue(iCurrentNode, "");
            }
        }
        else
        {
            // No path was specified, so generate it by the input/filter elements.
            sNodeValue = generateInputText(iCurrentNode, orOutput.lInputList);
        }

        if ("".equals(sNodeValue) && (orOutput.iType != WriterConfig.OutputRule.ORT_STRING))
        {
            // For non-string values an empty string equals to null.
            sNodeValue = null;
        }

        // If the node value could not be found
        if (sNodeValue == null)
        {
            // Check if the input must exists.
            if (orOutput.bMustExist)
            {
                throw new IOException("The mandatory element '" + orOutput.sQueryString +
                                      "' not found from input XML.");
            }

            // Use the default value.
            sNodeValue = orOutput.sDefaultValue;
        }

        // Handle the type specific formatting.
        switch (orOutput.iType)
        {
            case WriterConfig.OutputRule.ORT_FLOAT:
                sNodeValue = formatFloat(orOutput, sNodeValue);
                break;

            case WriterConfig.OutputRule.ORT_DATE:

                if (sNodeValue != null)
                {
                    sNodeValue = formatDate(orOutput, sNodeValue);
                }
                break;

            case WriterConfig.OutputRule.ORT_CURRENT_DATE:
                sNodeValue = formatCurrentDate(orOutput);
                break;

            case WriterConfig.OutputRule.ORT_BOOLEAN:

                if (sNodeValue != null)
                {
                    sNodeValue = formatBoolean(orOutput, sNodeValue);
                }
                break;
        }

        // If the value is not set, use an empty string to
        // handle the formatting correctly.
        if (sNodeValue == null)
        {
            sNodeValue = "";
        }

        // Handle the field width and alignment.
        sNodeValue = handleFieldWidth(orOutput, sNodeValue, true);

        // Write the output.
        wOutput.write(sNodeValue);
    }

    /**
     * Recursively processes the select-elements and the output-elements.
     *
     * @param   iCurrentNode  The current top node.
     * @param   sSelect       The select element that should be handled at this level.
     * @param   wOutput       Output from output-elements is written to this writer.
     * @param   bIsRoot       Indicates whether this node is the top element.
     *
     * @throws  IOException  Thrown if the writing failed.
     */
    protected void handleSelect(int iCurrentNode, WriterConfig.Select sSelect, Writer wOutput,
                                boolean bIsRoot)
                         throws IOException
    {
        int iSelectNode;

        // Find the node to be selected.
        if (sSelect.xqQuery != null)
        {
            // Find the node that this select element is referring to.
            iSelectNode = ((iCurrentNode != 0) ? sSelect.xqQuery.findNode(iCurrentNode) : 0);
        }
        else
        {
            // No query was set, so use the current node.
            // This usually means the root select element.
            iSelectNode = iCurrentNode;
        }

        if (iSelectNode == 0)
        {
            // The node was not found, check if it must exists.
            if (sSelect.bMustExist)
            {
                throw new IOException("Element '" + sSelect.sQueryString + "' was not found.");
            }
        }

        // Handle the sub-elements.
        for (Iterator<?> iIter = sSelect.lSubElements.iterator(); iIter.hasNext();)
        {
            Object oObj = iIter.next();

            if (oObj instanceof WriterConfig.OutputRule)
            {
                WriterConfig.OutputRule orOutput = (WriterConfig.OutputRule) oObj;

                handleOutput(iSelectNode, orOutput, wOutput);
            }
            else if (oObj instanceof WriterConfig.Select)
            {
                WriterConfig.Select sSubSelect = (WriterConfig.Select) oObj;

                handleSelect(iSelectNode, sSubSelect, wOutput, false);
            }
            else if (oObj instanceof WriterConfig.If)
            {
                WriterConfig.If iIf = (WriterConfig.If) oObj;

                handleIf(iSelectNode, iIf, wOutput);
            }
            else if (oObj instanceof WriterConfig.ForLoop)
            {
                WriterConfig.ForLoop flLoop = (WriterConfig.ForLoop) oObj;

                handleForEach(iSelectNode, flLoop, wOutput);
            }
        }
    }

    /**
     * Reads the input from XML node based on the input configuration.
     *
     * @param   iCurrentNode    The current XML node.
     * @param   iInput          The input configuration object.
     * @param   sbAppendBuffer  The string buffer that receives this input
     *
     * @throws  IOException  Thrown if the reading failed.
     */
    protected void readInput(int iCurrentNode, WriterConfig.InputRule iInput,
                             StringBuffer sbAppendBuffer)
                      throws IOException
    {
        String sValue = null;

        // Get the value by the method specified in configuration.
        if (iInput.xqQuery != null)
        {
            if (iCurrentNode == 0)
            {
                // No current node to be selected, so we cannot do anything.
                return;
            }

            switch (iInput.iInputType)
            {
                case WriterConfig.InputRule.IT_TEXT:
                    // Use XML node.
                    sValue = iInput.xqQuery.findValue(iCurrentNode, "");
                    break;

                case WriterConfig.InputRule.IT_XML:
                case WriterConfig.InputRule.IT_XML_FORMATED:
                {
                    int iNode = iInput.xqQuery.findNode(iCurrentNode);

                    if (iNode != 0)
                    {
                        sValue = Node.writeToString(iNode,
                                                    iInput.iInputType ==
                                                    WriterConfig.InputRule.IT_XML_FORMATED);
                        sValue = sValue.trim();
                    }
                }
                break;
            }
        }
        else if (iInput.sFixedValue != null)
        {
            // Use a fixed value.
            sValue = iInput.sFixedValue;
        }

        // Add the input value to the buffer.
        if (sValue != null)
        {
            sbAppendBuffer.append(sValue);
        }
        else
        {
            // Check if the input must exists.
            if (iInput.bMustExist)
            {
                throw new IOException("The mandatory element '" + iInput.sQueryString +
                                      "' not found from input XML.");
            }
        }
    }
}

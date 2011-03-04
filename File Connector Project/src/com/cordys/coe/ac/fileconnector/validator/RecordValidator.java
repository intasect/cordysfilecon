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
 package com.cordys.coe.ac.fileconnector.validator;

import com.cordys.coe.ac.fileconnector.exception.ValidationException;
import com.cordys.coe.ac.fileconnector.utils.PartialMatcher;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for reading records from a text file and parsing them according to the configuration.
 *
 * @author  mpoyhone
 */
public class RecordValidator
{
    /**
     * The current record number while scanning records.
     */
    protected int iCurrentRecordNumber = 0;
    /**
     * The record number at the end of validation.
     */
    protected int iEndRecordNumber = 0;
    /**
     * The starting record number. Used for error messages.
     */
    protected int iStartRecordNumber = 0;
    /**
     * Indicates the current read position while and after record read and validation.
     */
    protected int iValidationEndPosition = -1;
    /**
     * The validation configuration object.
     */
    protected ValidatorConfig vcConfig = null;

    /**
     * Creates a new RecordValidator object.
     *
     * @param  vcConfig  The validator configuration object.
     */
    public RecordValidator(ValidatorConfig vcConfig)
    {
        this.vcConfig = vcConfig;
    }

    /**
     * Creates a new configuration object from the XML structure.
     *
     * @param   vcConfig     The configuration object that will receive the new configuration.
     * @param   iConfigNode  The XML structure containing the configuration.
     *
     * @return  The new configuration object.
     *
     * @throws  IllegalArgumentException  Thrown if there was errors in the configuration.
     */
    public static ValidatorConfig createConfigration(ValidatorConfig vcConfig,
                                                     int iConfigNode)
                                              throws IllegalArgumentException
    {
        // Parse the configuration node.
        readConfiguration(vcConfig, iConfigNode);

        return vcConfig;
    }

    /**
     * Parses records from the input and if the result document is given, returns the resulting
     * record XML structure.
     *
     * @param   sFileType  File type to be used, as given in the configuration.
     * @param   csInput    Input character sequence.
     * @param   dResDoc    If not null, this document is used to create the record XML structure.
     *
     * @return  The record XML structure root node, or zero, if only validation is needed.
     *
     * @throws  ValidationException  Thrown if the validation failed.
     */
    public int parseAndValidateRecord(String sFileType, CharSequence csInput, Document dResDoc)
                               throws ValidationException
    {
        return parseAndValidateRecord(sFileType, csInput, 0, dResDoc);
    }

    /**
     * Parses records from the input and if the result document is given, returns the resulting
     * record XML structure.
     *
     * @param   sFileType  File type to be used, as given in the configuration.
     * @param   csInput    Input character sequence.
     * @param   iInputPos  Validation start position in the input sequence.
     * @param   dResDoc    If not null, this document is used to create the record XML structure.
     *
     * @return  The record XML structure root node, or zero, if only validation is needed.
     *
     * @throws  ValidationException  Thrown if the validation failed.
     */
    public int parseAndValidateRecord(String sFileType, CharSequence csInput, int iInputPos,
                                      Document dResDoc)
                               throws ValidationException
    {
        assert (vcConfig != null) && (vcConfig.mConfigMap != null);

        // Get the file type object is should be used for validation.
        FileType ftFileType = vcConfig.mConfigMap.get(sFileType);

        if (ftFileType == null)
        {
            throw new ValidationException("Invalid file type " + sFileType);
        }
        
        if (! ftFileType.bAllowEmptyFiles && csInput.length() == 0) {
            throw new ValidationException("File is empty.");
        }

        StringBuffer sbReadRecordNames = new StringBuffer(128); // Used record patterns
        List<RecordType> lReadRecordList = new LinkedList<RecordType>();
        List<List<String>> lReadRecordFieldValuesList = new LinkedList<List<String>>(); // Contains List-elements of record field values for matched records.

        iCurrentRecordNumber = iStartRecordNumber;

        // Scan the sequence until we have validated all the configured records,
        // or we scanned past the end.
        while (iInputPos < csInput.length())
        {
            RecordType rtMatchedRecord = null;
            List<String> lResFieldValueList = null; // Contains the read field values for the
                                                    // matching record.

            // Try to match all the records until we find a record that matches the current
            // input and the file type record sequence pattern.
            for (Iterator<RecordType> iter = ftFileType.lRecordList.iterator(); iter.hasNext();)
            {
                RecordType rtRecord = iter.next();
                int iNextPos;

                // If we are returning the XML structure, create the field list that
                // will contain the field values.
                if (dResDoc != null)
                {
                    lResFieldValueList = new LinkedList<String>();
                }

                // Try to match the record
                iNextPos = matchRecord(rtRecord, iInputPos, csInput, lResFieldValueList);

                if (iNextPos < 0)
                {
                    // Match failed, try the next one.
                    continue;
                }

                // Append to record name to the read records string and
                // see if it matches the file type record sequence pattern.
                int iStrOldLength = sbReadRecordNames.length();

                // Add the record name to the list.
                if (sbReadRecordNames.length() > 0)
                {
                    sbReadRecordNames.append(" ");
                }
                sbReadRecordNames.append(rtRecord.sRecordName);

                // See if the beginning of the record pattern matches
                // the string of already scanned records.
                if (!ftFileType.pmPartialRecordMatcher.isPartialMatch(sbReadRecordNames))
                {
                    // Nope. Discard this record.
                    sbReadRecordNames.setLength(iStrOldLength);
                    continue;
                }

                // The record matched fine.
                rtMatchedRecord = rtRecord;
                iInputPos = iNextPos;
                break;
            }

            // If we have matched a record, keep on scanning. The file type
            // record sequence is matched completely until no record matches
            // is anymore.
            if (rtMatchedRecord != null)
            {
                // Add the matched record to the list and continue matching.
                lReadRecordList.add(rtMatchedRecord);

                if (dResDoc != null)
                {
                    if (lResFieldValueList == null)
                    {
                        throw new IllegalArgumentException("INTERNAL_ERROR: lResFieldValueList is null.");
                    }
                }

                // Increment the record counter.
                iCurrentRecordNumber++;

                // Add the record to the read record list.
                lReadRecordFieldValuesList.add(lResFieldValueList);

                continue;
            }

            // As the previous record failed to match, see the file type record sequence pattern
            // still matches our record list.
            if (!ftFileType.pmPartialRecordMatcher.isCompleteMatch(sbReadRecordNames))
            {
                // The pattern was not matched completely.
                throw new ValidationException("At line " + iCurrentRecordNumber + " : " +
                                              "No matching record found. " +
                                              "Already read records: " + sbReadRecordNames);
            }

            // Now we have matched the whole record list properly and we can stop matching.
            break;
        }

        int iResultNode = 0;

        // If we are creating the resulting XML tree, create it now from the record list.
        if (dResDoc != null)
        {
            iResultNode = dResDoc.createElement("tuple");

            if (lReadRecordList.size() != lReadRecordFieldValuesList.size())
            {
                throw new IllegalArgumentException("INTERNAL_ERROR: Record list and record field value list size mismatch.");
            }

            Iterator<RecordType> iRecordIter = lReadRecordList.iterator();
            Iterator<List<String>> iFieldValueIter = lReadRecordFieldValuesList.iterator();

            while (iRecordIter.hasNext() && iFieldValueIter.hasNext())
            {
                RecordType rtRecord = iRecordIter.next();
                List<String> lFieldValueList = iFieldValueIter.next();

                createRecordNode(rtRecord, lFieldValueList, dResDoc, iResultNode);
            }
        }

        iEndRecordNumber = iCurrentRecordNumber;
        iValidationEndPosition = iInputPos;

        return iResultNode;
    }

    /**
     * Returns the end record number that is the start record number plus number of read records.
     *
     * @return  The end record number.
     */
    public int getEndRecordNumber()
    {
        return iEndRecordNumber;
    }

    /**
     * Returns the start record number.
     *
     * @return  The start record number.
     */
    public int getStartRecordNumber()
    {
        return iStartRecordNumber;
    }

    /**
     * Returns the current validation position after validation.
     *
     * @return  The current validation position.
     */
    public int getValidationEndPosition()
    {
        return iValidationEndPosition;
    }

    /**
     * Sets the start record number used in error messages.
     *
     * @param  startRecordNumber  The start record number
     */
    public void setStartRecordNumber(int startRecordNumber)
    {
        iStartRecordNumber = startRecordNumber;
    }

    /**
     * Reads the configuration from the XML structure and parses it to an object structure.
     *
     * @param   vcReadConfig  The configuration object that the read configuration is put into.
     * @param   iNode         The configuration XML structure.
     *
     * @throws  IllegalArgumentException  Thrown if there was an error in the configuration.
     */
    protected static void readConfiguration(ValidatorConfig vcReadConfig, int iNode)
                                     throws IllegalArgumentException
    {
        // Check to root node name.
        if (!Node.getName(iNode).equals("configuration"))
        {
            throw new IllegalArgumentException("Configuration root element missing.");
        }

        if (Node.getNumChildren(iNode) == 0)
        {
            throw new IllegalArgumentException("No file types defined in the configuration.");
        }

        int iFileTypeNode;

        // Loop all 'filetype' nodes.
        iFileTypeNode = Node.getFirstChild(iNode);

        while (iFileTypeNode != 0)
        {
            // Check the file type node name.
            if (!Node.getName(iFileTypeNode).equals("filetype"))
            {
                throw new IllegalArgumentException("Illegal configuration element. Expecting filetype.");
            }

            // Get the file type attributes
            String sFileType = Node.getAttribute(iFileTypeNode, "name");
            String sValidRecordSequence = Node.getAttribute(iFileTypeNode, "recordsequence");
            boolean bAllowEmptyFiles = "true".equals(Node.getAttribute(iFileTypeNode, "allowempty", "true"));
            FileType ftFileType = new FileType();

            // Check that the file type name attribute is given.
            if ((sFileType == null) || sFileType.equals(""))
            {
                throw new IllegalArgumentException("Filetype name not set");
            }

            // Check that the record sequence attribute is given.
            if ((sValidRecordSequence == null) || sValidRecordSequence.equals(""))
            {
                throw new IllegalArgumentException("Record sequence not defined for file type " +
                                                   sFileType);
            }

            // The file type must have at least one record definition.
            if (Node.getNumChildren(iFileTypeNode) == 0)
            {
                throw new IllegalArgumentException("No records defined for file type " + sFileType);
            }

            // Add the attributes to the file type object
            ftFileType.sFileType = sFileType;
            ftFileType.lRecordList = new LinkedList<RecordType>();
            ftFileType.bAllowEmptyFiles = bAllowEmptyFiles;

            // Parse the file type record sequence string.
            try
            {
                // Replace all white spaces and commas with space.
                // This normalizes the pattern to be used with the validator.
                String[] saParts = sValidRecordSequence.split("[,\\s\\t\\r\\n]+");

                if ((saParts == null) || (saParts.length == 0))
                {
                    throw new IllegalArgumentException("Invalid record sequence pattern file type " +
                                                       sFileType);
                }

                // Create the valid record sequence and partial matcher data. The parts need a
                // space before them and parenthesis when using regexp operators, so they will
                // match the partial record list in the order it is being generated.
                for (int i = 1; i < saParts.length; i++)
                {
                    String sPart = saParts[i];

                    sPart = " " + sPart;

                    // If we have a regexp operator at the end of the part we need to change
                    // the part from form " part*" to "( part)*" so the
                    switch (sPart.charAt(sPart.length() - 1))
                    {
                        case '*':
                        case '+':
                        case '?':
                            sPart = "(" + sPart.substring(0, sPart.length() - 1) + ")" +
                                    sPart.charAt(sPart.length() - 1);
                            break;
                    }

                    saParts[i] = sPart;
                }

                ftFileType.pmPartialRecordMatcher = new PartialMatcher(saParts);
            }
            catch (Exception e)
            {
                throw new IllegalArgumentException("Invalid record sequence pattern file type " +
                                                   sFileType + ":" + e);
            }

            // Map the file type object with its name.
            vcReadConfig.mConfigMap.put(sFileType, ftFileType);

            // Read all the file type's records.
            int iRecordNode = Node.getFirstChild(iFileTypeNode);

            while (iRecordNode != 0)
            {
                // Check the record node name.
                if (!Node.getName(iRecordNode).equals("record"))
                {
                    throw new IllegalArgumentException("Illegal configuration element. Expecting record.");
                }

                // Get the record attributes.
                String sRecordName = Node.getAttribute(iRecordNode, "name");
                String sRecordPattern = Node.getAttribute(iRecordNode, "pattern");
                String sRecordGroup = Node.getAttribute(iFileTypeNode, "index");
                int iRecordGroup = 0;

                // Check that the record name exists.
                if ((sRecordName == null) || sRecordName.equals(""))
                {
                    throw new IllegalArgumentException("Record name is missing.");
                }

                // Check that the record pattern regexp exists.
                if ((sRecordPattern == null) || sRecordPattern.equals(""))
                {
                    throw new IllegalArgumentException("Record pattern is missing for record " +
                                                       sRecordName);
                }

                // If the record group index is given, parse it to integer format.
                if (sRecordGroup != null)
                {
                    // Parse the correct index.
                    try
                    {
                        iRecordGroup = Integer.parseInt(sRecordGroup);
                    }
                    catch (Exception e)
                    {
                        throw new IllegalArgumentException("Invalid record index value: " +
                                                           sRecordGroup);
                    }
                }

                // Check that the record has one or more field definitions.
                if (Node.getNumChildren(iRecordNode) == 0)
                {
                    throw new IllegalArgumentException("No fields specified for record " +
                                                       sRecordName);
                }

                // Create the record object and add the parameters to it.
                RecordType rtRecord = new RecordType();

                rtRecord.sRecordName = sRecordName;
                rtRecord.lFieldList = new LinkedList<FieldType>();
                rtRecord.iRecordPatternGroup = iRecordGroup;

                // Compile the record regexp pattern.
                try
                {
                    rtRecord.pRecordPattern = Pattern.compile(sRecordPattern);
                }
                catch (Exception e)
                {
                    throw new IllegalArgumentException("Invalid pattern for record " + sRecordName +
                                                       ": " + e);
                }

                // Add the record to the file type record list.
                ftFileType.lRecordList.add(rtRecord);

                // Read the record fields.
                int iFieldNode;

                iFieldNode = Node.getFirstChild(iRecordNode);

                while (iFieldNode != 0)
                {
                    // Check the field node name
                    if (!Node.getName(iFieldNode).equals("field"))
                    {
                        throw new IllegalArgumentException("Illegal configuration element. Expecting field.");
                    }

                    // Get the field attributes.
                    String sFieldName = Node.getAttribute(iFieldNode, "name");
                    String sPattern = Node.getAttribute(iFieldNode, "pattern");
                    String sGroupIndex = Node.getAttribute(iFieldNode, "index");
                    String sFieldLength = Node.getAttribute(iFieldNode, "width");
                    String sTrimField = Node.getAttribute(iFieldNode, "trim");
                    int iGroupIndex = 0; // Default is the first group
                    int iFieldLength = -1;

                    // Check that the field name exists
                    if ((sFieldName == null) || sFieldName.equals(""))
                    {
                        throw new IllegalArgumentException("Field name is missing.");
                    }

                    // Check that the field pattern exists.
                    if ((sPattern == null) || sPattern.equals(""))
                    {
                        throw new IllegalArgumentException("Field pattern is missing.");
                    }

                    // If the field pattern group index is given, parse
                    // it to integer format. The default value is zero.
                    if (sGroupIndex != null)
                    {
                        if (sGroupIndex.equals("none"))
                        {
                            // This is the setting for no text selection for this field.
                            iGroupIndex = -1;
                        }
                        else
                        {
                            // Parse the correct index.
                            try
                            {
                                iGroupIndex = Integer.parseInt(sGroupIndex);
                            }
                            catch (Exception e)
                            {
                                throw new IllegalArgumentException("Invalid group index value: " +
                                                                   sGroupIndex);
                            }
                        }
                    }

                    // If the field length is given, parse it to integer format.
                    if ((sFieldLength != null) && (sFieldLength.length() > 0))
                    {
                        try
                        {
                            iFieldLength = Integer.parseInt(sFieldLength);
                        }
                        catch (Exception e)
                        {
                            throw new IllegalArgumentException("Invalid field length: " +
                                                               sFieldLength);
                        }
                    }

                    // Create the field object and add attributes to it.
                    FieldType ftField = new FieldType();

                    ftField.sFieldName = sFieldName;
                    ftField.iResultGroup = iGroupIndex;

                    if (iFieldLength >= 0)
                    {
                        ftField.fixedLength = iFieldLength;
                    }

                    // If the trim field parameter is given, parse it as a boolean.
                    if ((sTrimField != null) && (sTrimField.length() > 0))
                    {
                        try
                        {
                            ftField.trimField = Boolean.parseBoolean(sTrimField);
                        }
                        catch (Exception e)
                        {
                            throw new IllegalArgumentException("Invalid field trim parameter: " +
                                                               sTrimField);
                        }
                    }

                    // Compile the field regexp pattern.
                    try
                    {
                        ftField.pPattern = Pattern.compile(sPattern);
                    }
                    catch (Exception e)
                    {
                        throw new IllegalArgumentException("Illegal pattern: " + sPattern);
                    }

                    // Add the field to the record.
                    rtRecord.lFieldList.add(ftField);

                    // Scan the next field
                    iFieldNode = Node.getNextSibling(iFieldNode);
                }

                // Set the record field count.
                rtRecord.iNumFields = rtRecord.lFieldList.size();

                // Scan the next record.
                iRecordNode = Node.getNextSibling(iRecordNode);
            }

            // Scan the next file type
            iFileTypeNode = Node.getNextSibling(iFileTypeNode);
        }
    }

    /**
     * Creates the record XML structure that was parsed from the file.
     *
     * @param   rtRecord         The record configuration object.
     * @param   lFieldValueList  A list containing the field values as read from the file.
     * @param   dDoc             The Document object that is used for creating the XML structure.
     * @param   iParentNode      The node that will contain the record XML structure.
     *
     * @return  The create XML structure root node.
     */
    protected int createRecordNode(RecordType rtRecord, List<String> lFieldValueList, Document dDoc,
                                   int iParentNode)
    {
        // Check parameter sanity
        if (rtRecord.lFieldList.size() != lFieldValueList.size())
        {
            throw new IllegalArgumentException("INTERNAL_ERROR: Record field list and field value list size mismatch.");
        }

        if ((dDoc == null) || (iParentNode == 0))
        {
            throw new IllegalArgumentException("INTERNAL_ERROR: Document or parent node not set.");
        }

        int iRecNode;

        // Create the record node
        iRecNode = dDoc.createElement(rtRecord.sRecordName, iParentNode);

        // Get iterators for field and field value lists
        Iterator<FieldType> iFieldIter = rtRecord.lFieldList.iterator();
        Iterator<String> iValueIter = lFieldValueList.iterator();

        // Scan through the fields.
        while (iFieldIter.hasNext() && iValueIter.hasNext())
        {
            FieldType ftField = iFieldIter.next();
            String sValue = iValueIter.next();

            // Return only the element that are requested to be returned.
            if (ftField.iResultGroup >= 0)
            {
                // Create the field node that has the configured field name
                // and the read field value.
                dDoc.createTextElement(ftField.sFieldName, sValue, iRecNode);
            }
        }

        return iRecNode;
    }

    /**
     * Tries to match the record from the input sequence.
     *
     * @param   rtRecord            The record configuration to be matched
     * @param   iInputPos           Matching start position
     * @param   csInput             The input sequence to be matched
     * @param   lResFieldValueList  The list that should receive the matched field values, or null,
     *                              if the record needs to be validated only.
     *
     * @return  The position in the input string after the match, or -1 if no match was found.
     *
     * @throws  ValidationException  Thrown if the record was not configured correctly.
     */
    private int matchRecord(RecordType rtRecord, int iInputPos, CharSequence csInput,
                            List<String> lResFieldValueList)
                     throws ValidationException
    {
        // First find the record boundaries based on the record pattern.
        CharSequence csRecordInput;
        int iRecordStart;
        int iRecordEnd;
        int iRecordMatchEnd;
        int iReadPos;
        Matcher mMatcher;

        iReadPos = iInputPos;
        mMatcher = rtRecord.pRecordPattern.matcher(csInput);

        if (!mMatcher.find(iReadPos) || (mMatcher.start() > iReadPos))
        {
            // Record boundaries not found or not at the beginning of input.
            return -1;
        }

        if ((rtRecord.iRecordPatternGroup < 0) ||
                (rtRecord.iRecordPatternGroup >= mMatcher.groupCount()))
        {
            throw new ValidationException("At line " + iCurrentRecordNumber + " : " +
                                          "Group index " + rtRecord.iRecordPatternGroup +
                                          " not found for field " + rtRecord.sRecordName);
        }

        // Get the record start and end positions from the specified group.
        iRecordStart = mMatcher.start(rtRecord.iRecordPatternGroup + 1); // This is the record start
                                                                         // for record fields.
        iRecordEnd = mMatcher.end(rtRecord.iRecordPatternGroup + 1); // This is the record end for
                                                                     // record fields.
        iRecordMatchEnd = mMatcher.end(); // This is the real record end, including a possible
                                          // record separators.

        // Get the record subsequence so that we match the fields only inside this record.
        csRecordInput = csInput.subSequence(iRecordStart, iRecordEnd);

        int iMatchedFields = 0;

        // Try to match the fields in the order that is specified for the record.
        iReadPos = 0;

        for (Iterator<FieldType> iter = rtRecord.lFieldList.iterator(); iter.hasNext();)
        {
            FieldType ftField = iter.next();

            if (ftField.fixedLength < 0)
            {
                // Run the regexp against the input.
                mMatcher = ftField.pPattern.matcher(csRecordInput);

                if (!mMatcher.find(iReadPos))
                {
                    // The field did not match, so the record does not match either.
                    return -1;
                }

                if (mMatcher.start() != iReadPos)
                {
                    // The match does not start at the beginning of input.
                    return -1;
                }

                iReadPos = mMatcher.end();
            }
            else
            {
                // This is a fixed length field, so read the correct amount and
                // try to match it against the regexp.
                int len = ftField.fixedLength;

                if (csRecordInput.length() < (iReadPos + len))
                {
                    // Not enough data to read for this field.
                    return -1;
                }

                CharSequence cdFieldData = csRecordInput.subSequence(iReadPos, iReadPos + len);

                mMatcher = ftField.pPattern.matcher(cdFieldData);

                if (!mMatcher.matches())
                {
                    // Field doesn't match.
                    return -1;
                }

                iReadPos += len;
            }

            // Add the field value to the list
            if (lResFieldValueList != null)
            {
                if (ftField.iResultGroup >= 0)
                {
                    // Get the correct regexp group
                    if (ftField.iResultGroup >= mMatcher.groupCount())
                    {
                        throw new ValidationException("At line " + iCurrentRecordNumber + " : " +
                                                      "Group index " + ftField.iResultGroup +
                                                      " not found for field " + ftField.sFieldName);
                    }

                    String sGroupValue = mMatcher.group(ftField.iResultGroup + 1);

                    if (ftField.trimField)
                    {
                        sGroupValue = sGroupValue.trim();
                    }

                    lResFieldValueList.add(sGroupValue);
                }
                else
                {
                    // We don't want a field value for this field.
                    lResFieldValueList.add("");
                }
            }

            iMatchedFields++;

            if (iReadPos >= csRecordInput.length())
            {
                // We matched at the end of input. The number of matched fields tell if we matched
                // the whole record.
                break;
            }
        }

        if (iMatchedFields != rtRecord.iNumFields)
        {
            // If the last field can match an empty string and that is the only field we are
            // missing, this we have succeeded.
            if ((iMatchedFields == (rtRecord.iNumFields - 1)) && (rtRecord.lFieldList.size() > 0))
            {
                FieldType ftField = rtRecord.lFieldList.get(rtRecord.lFieldList.size() - 1);

                mMatcher = ftField.pPattern.matcher("");

                if (mMatcher.matches())
                {
                    // Check if we need to fill the field value.
                    if (lResFieldValueList != null)
                    {
                        lResFieldValueList.add("");
                    }

                    return iRecordMatchEnd;
                }
            }

            // The record match failed.
            return -1;
        }

        return iRecordMatchEnd;
    }

    /**
     * Contains the record field configuration.
     *
     * @author  mpoyhone
     */
    public static class FieldType
    {
        /**
         * Length of a fixed field. If < 0 this is not a fixed field.
         */
        public int fixedLength = -1;
        /**
         * A group index in the regular expression that will be used to get the actual field
         * contents, or -1 if the field should not be put in the record XML structure.
         */
        public int iResultGroup;
        /**
         * The regular expression that defines the field contents inside the record.
         */
        public Pattern pPattern;
        /**
         * The field name that will be used while creating the record XML structure.
         */
        public String sFieldName;
        /**
         * If true the field contents are removed from space before returning it.
         */
        public boolean trimField = false;
    }

    /**
     * Contains the file type configuration.
     *
     * @author  mpoyhone
     */
    public static class FileType
    {
        /**
         * A list of RecordType objects that contain the file type record configuration.
         */
        public List<RecordType> lRecordList;
        /**
         * Defines the regular expression that matches the configured file type record sequence
         * pattern. Needs to be a PartialMatcher object as it needs to be able to match incomplete
         * input.
         */
        public PartialMatcher pmPartialRecordMatcher;
        /**
         * The file type name string.
         */
        public String sFileType;
        /**
         * If <code>true</code>, empty files are allowed.
         */
        public boolean bAllowEmptyFiles;
    }

    /**
     * Contains the file type record configuration.
     *
     * @author  mpoyhone
     */
    public static class RecordType
    {
        /**
         * Needed number of fields to be matched for this type of record. This is always the same as
         * the number of configured fields.
         */
        public int iNumFields;
        /**
         * The group index in the record regexp pattern that returns the field area inside this
         * record.
         */
        public int iRecordPatternGroup;
        /**
         * A list of FieldType object that contain the record field configuration.
         */
        public List<FieldType> lFieldList;
        /**
         * The regular expression that defines the whole record area.
         */
        public Pattern pRecordPattern;
        /**
         * The record name string that is used in the record XML structure.
         */
        public String sRecordName;
    }
}

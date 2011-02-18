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
 package com.cordys.coe.ac.fileconnector.utils;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;

/**
 * An utility class to read and write XML structures.
 *
 * @author  mpoyhone
 */
public class XMLSerializer
{
    /**
     * Creates a new node or node structure.
     *
     * @param   iParentNode   The node under which the new node is created.
     * @param   sElementName  The new node name.
     *
     * @return  The new node.
     *
     * @throws  XMLException  Thrown if the creation failed.
     */
    public static int createNode(int iParentNode, String sElementName)
                          throws XMLException
    {
        int iRes = findNode(iParentNode, sElementName, true);

        if (iRes == 0)
        {
            throw new XMLException("Unable to create node " + sElementName);
        }

        return iRes;
    }

    /**
     * Returns a node under the parent node based on the search path.
     *
     * @param   iParentNode   The parent node under which the node is searched.
     * @param   sElementName  The search path.
     *
     * @return  The node found.
     *
     * @throws  XMLException  Thrown if the node could not be found.
     */
    public static int findNode(int iParentNode, String sElementName)
                        throws XMLException
    {
        int iRes = findNode(iParentNode, sElementName, false);

        if (iRes == 0)
        {
            throw new XMLException("Unable to find node " + sElementName);
        }

        return iRes;
    }

    /**
     * Returns a node under the parent node based on the search path.
     *
     * @param   iParentNode   The parent node under which the node is searched.
     * @param   sElementName  The search path.
     * @param   iDefaultNode  This node is returned if the requested node was not found.
     *
     * @return  The node found.
     *
     * @throws  XMLException  Thrown if the node could not be found.
     */
    public static int findNode(int iParentNode, String sElementName, int iDefaultNode)
                        throws XMLException
    {
        int iRes = findNode(iParentNode, sElementName, false);

        if (iRes == 0)
        {
            return iDefaultNode;
        }

        return iRes;
    }

    /**
     * Reads a boolean value from the node text.
     *
     * @param   iParentNode  Parent node.
     * @param   sName        Element name.
     *
     * @return  Element value.
     *
     * @throws  XMLException  Thrown if the value was not found.
     */
    public static boolean readBoolean(int iParentNode, String sName)
                               throws XMLException
    {
        String sValue = readString(iParentNode, sName, null);

        if (sValue == null)
        {
            throw new XMLException("Unable to find node " + sName);
        }

        return parseBoolean(sValue);
    }

    /**
     * Reads a boolean value from the node text.
     *
     * @param   iParentNode  Parent node.
     * @param   sName        Element name.
     * @param   bDefault     Default value.
     *
     * @return  Read value or the default one.
     *
     * @throws  XMLException  Thrown if the value was not found.
     */
    public static boolean readBoolean(int iParentNode, String sName, boolean bDefault)
                               throws XMLException
    {
        String sValue = readString(iParentNode, sName, null);

        return (sValue != null) ? parseBoolean(sValue) : bDefault;
    }

    /**
     * Reads an enumerated value from the node text.
     *
     * @param   iParentNode   Current node.
     * @param   sName         Element name.
     * @param   saEnumNames   Enum names.
     * @param   iaEnumValues  Enum values.
     *
     * @return  Enum value.
     *
     * @throws  XMLException  Thrown if the operation failed.
     */
    public static int readEnum(int iParentNode, String sName, String[] saEnumNames,
                               int[] iaEnumValues)
                        throws XMLException
    {
        if (saEnumNames.length != iaEnumValues.length)
        {
            throw new IllegalArgumentException("Enum names array is of different length than the values array.");
        }

        if (saEnumNames.length == 0)
        {
            throw new IllegalArgumentException("Enum names array is empty.");
        }

        String sValue = readString(iParentNode, sName);

        for (int i = 0; i < saEnumNames.length; i++)
        {
            String sEnumName = saEnumNames[i];

            if (sEnumName.equals(sValue))
            {
                return iaEnumValues[i];
            }
        }

        throw new XMLException("Illegal value '" + sValue + "' for element " + sName);
    }

    /**
     * Reads an enumerated value from the node text.
     *
     * @param   iParentNode    Current node.
     * @param   sName          Element name.
     * @param   saEnumNames    Enum names.
     * @param   iaEnumValues   Enum values.
     * @param   iDefaultValue  Default enum value.
     *
     * @return  Enum value.
     *
     * @throws  XMLException  Thrown if the operation failed.
     */
    public static int readEnum(int iParentNode, String sName, String[] saEnumNames,
                               int[] iaEnumValues, int iDefaultValue)
                        throws XMLException
    {
        if (saEnumNames.length != iaEnumValues.length)
        {
            throw new IllegalArgumentException("Enum names array is of different length than the values array.");
        }

        if (saEnumNames.length == 0)
        {
            throw new IllegalArgumentException("Enum names array is empty.");
        }

        String sValue = readString(iParentNode, sName, null);

        if (sValue == null)
        {
            return iDefaultValue;
        }

        for (int i = 0; i < saEnumNames.length; i++)
        {
            String sEnumName = saEnumNames[i];

            if (sEnumName.equals(sValue))
            {
                return iaEnumValues[i];
            }
        }

        throw new XMLException("Illegal value '" + sValue + "' for element " + sName);
    }

    /**
     * Reads an integer value from the node text.
     *
     * @param   iParentNode  Parent node.
     * @param   sName        Element name.
     * @param   iDefault     Default value.
     *
     * @return  Found value or the default one.
     *
     * @throws  XMLException  Thrown if the operation failed.
     */
    public static int readInt(int iParentNode, String sName, int iDefault)
                       throws XMLException
    {
        String sValue = readString(iParentNode, sName, null);

        if ((sValue == null) || sValue.equals(""))
        {
            return iDefault;
        }

        try
        {
            return Integer.parseInt(sValue);
        }
        catch (Exception e)
        {
            throw new XMLException("Invalid number '" + sValue + "' : " + e);
        }
    }

    /**
     * Reads a string value.
     *
     * @param   iParentNode  Parent node.
     * @param   sName        Element name.
     *
     * @return  Element value.
     *
     * @throws  XMLException  Thrown if the value was not found.
     */
    public static String readString(int iParentNode, String sName)
                             throws XMLException
    {
        String sValue = getValue(iParentNode, sName);

        if (sValue == null)
        {
            throw new XMLException("Unable to find node " + sName);
        }

        return sValue;
    }

    /**
     * Reads a string value.
     *
     * @param   iParentNode    Parent node.
     * @param   sName          Element name.
     * @param   sDefaultValue  Default value.
     *
     * @return  Found value or the default one.
     *
     * @throws  XMLException  Thrown if the operation failed.
     */
    public static String readString(int iParentNode, String sName, String sDefaultValue)
                             throws XMLException
    {
        String sValue = getValue(iParentNode, sName);

        return (sValue != null) ? sValue : sDefaultValue;
    }

    /**
     * Finds a node from the XML.
     *
     * @param   iParentNode   Parent node.
     * @param   sElementName  Element name.
     * @param   bCreate       If <code>true</code> and the node was not found, it is created.
     *
     * @return  Found or created node.
     *
     * @throws  XMLException  Throw if the operation failed.
     */
    protected static int findNode(int iParentNode, String sElementName, boolean bCreate)
                           throws XMLException
    {
        // Check the element name type.
        if (sElementName.startsWith("@"))
        {
            // This is an attribute, so return the node.
            return iParentNode;
        }

        // Element is a node, so we must find it under the current node.

        int nPos;
        String sSubName = null;

        // Check if this is a path expression.
        if ((nPos = sElementName.indexOf('/')) > 0)
        {
            // Yes it is, get the next subtree name and adjust the current element name.
            sSubName = sElementName.substring(nPos + 1);
            sElementName = sElementName.substring(0, nPos);
        }

        // Check for wildcards
        if (sElementName.equals("*"))
        {
            return (Node.getNumChildren(iParentNode) > 0) ? Node.getFirstChild(iParentNode) : 0;
        }

        int iSubNode;

        if (!sElementName.equals("."))
        {
            // Find the node corresponding to element name.
            iSubNode = Find.firstMatch(iParentNode,
                                       "<" + Node.getName(iParentNode) + "><" + sElementName + ">");
        }
        else
        {
            // The node is the current node.
            iSubNode = iParentNode;
        }

        // Check if it was already there.
        if (iSubNode == 0)
        {
            Document dDoc = Node.getDocument(iParentNode);

            // The sub-node was not found.
            if (!bCreate)
            {
                return 0;
            }

            // Create a new node.
            iSubNode = dDoc.createElement(sElementName, iParentNode);
        }

        // If we have subnodes to search, do it recursively
        if (sSubName != null)
        {
            return findNode(iSubNode, sSubName, bCreate);
        }

        // This is the last node to be found, so return it.
        return iSubNode;
    }

    /**
     * Parses a boolean value.
     *
     * @param   sValue  String value.
     *
     * @return  <code>true</code> or <code>false</code> boolean value.
     *
     * @throws  XMLException  Thrown if the value was not a valid boolean value.
     */
    protected static boolean parseBoolean(String sValue)
                                   throws XMLException
    {
        sValue = sValue.toLowerCase();

        if (sValue.equals("true") || sValue.equals("on") || sValue.equals("1"))
        {
            return true;
        }
        else if (sValue.equals("false") || sValue.equals("off") || sValue.equals("0"))
        {
            return false;
        }
        else
        {
            throw new XMLException("Invalud boolean value " + sValue);
        }
    }

    /**
     * Returns nodes value.
     *
     * @param   iParentNode  Parent node.
     * @param   sName        Element name.
     *
     * @return  Node's value.
     *
     * @throws  XMLException  Thrown if the operation failed.
     */
    protected static String getValue(int iParentNode, String sName)
                              throws XMLException
    {
        String sValue;
        int iElemNode = findNode(iParentNode, sName, false);
        int nPos;

        if (iElemNode == 0)
        {
            return null;
        }

        // If this is a path expression, get the last node name.
        if ((nPos = sName.indexOf('/')) > 0)
        {
            // Yes it is, get the next subtree name and adjust the current element name.
            sName = sName.substring(nPos + 1);
        }

        if (sName.startsWith("@"))
        {
            // Return the attirbute value.
            sName = sName.substring(1);
            sValue = Node.getAttribute(iElemNode, sName);
        }
        else
        {
            // This the node contents.
            sValue = Node.getData(iElemNode);
        }

/*        if ((sValue != null) && sValue.equals(""))
 *      {         return null;     }*/

        return sValue;
    }
}

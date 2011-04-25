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

import com.cordys.coe.util.xml.nom.XPathHelper;

import com.eibus.xml.xpath.NodeSet;
import com.eibus.xml.xpath.ResultNode;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * Wrapper class for NOM XPath. This also holds the XPathMetainfo for namespace bindings.
 *
 * @author  mpoyhone
 */
public class XPathWrapper
{
    /**
     * Contains the compiled XPath.
     */
    protected XPath xpath;
    /**
     * Contains the namespace bindings.
     */
    protected XPathMetaInfo xpathInfo;
    /**
     * Holds the XPath query.
     */
    protected String xpathString;

    /**
     * Constructs a new XPath wrapper by the query string.
     *
     * @param  xpathStr  XPath expression
     * @param  xpath     XPath object
     * @param  info      XPath namespace bindings.
     */
    XPathWrapper(String xpathStr, XPath xpath, XPathMetaInfo info)
    {
        this.xpath = xpath;
        this.xpathInfo = info;
        this.xpathString = xpathStr;
    }

    /**
     * Returns all nodes pointed by this query path.
     *
     * @param   node  The XML structure root node to be searched.
     *
     * @return  The nodes pointed by this query path.
     */
    public int[] findAllNodes(int node)
    {
        if (node == 0)
        {
            return null;
        }

        if (xpath == null)
        {
            throw new UnsupportedOperationException("XPath path is not set.");
        }

        NodeSet nodeSet = xpath.selectNodeSet(node, xpathInfo);

        return nodeSet.getElementNodes();
    }

    /**
     * Returns the node pointed by this query path.
     *
     * @param   node  The XML structure root node to be searched.
     *
     * @return  The node pointed by this query path.
     */
    public int findNode(int node)
    {
        if (node == 0)
        {
            return 0;
        }

        if (xpath == null)
        {
            throw new UnsupportedOperationException("XPath is not set.");
        }

        NodeSet nodeSet = xpath.selectNodeSet(node, xpathInfo);

        if (nodeSet.hasNext())
        {
            return ResultNode.getElementNode(nodeSet.next());
        }
        else
        {
            return 0;
        }
    }

    /**
     * Returns the node or attribute value specified by this query path.
     *
     * @param   node          The XML structure root node to be searched.
     * @param   defaultValue  The default value returned if the node/attribute was not found.
     *
     * @return  The node/attribute value or the default value when no match was found. Returns null
     *          when the node was not found.
     */
    public String findValue(int node, String defaultValue)
    {
        if (node == 0)
        {
            return defaultValue;
        }

        if (xpath == null)
        {
            throw new UnsupportedOperationException("XPath path is not set.");
        }

        String value = XPathHelper.getStringValue(node, xpath, xpathInfo);

        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns a textual representation of this query.
     *
     * @return  A textual representation of this query.
     */
    @Override
    public String toString()
    {
        if (xpathString == null)
        {
            return "<unset>";
        }

        return xpathString;
    }

    /**
     * Evaluates the XPath into a boolean value.
     *
     * @param   node  The XML structure root node to be searched.
     *
     * @return
     */
    public boolean getBooleanValue(int node)
    {
        if (node == 0)
        {
            return false;
        }

        if (xpath == null)
        {
            throw new UnsupportedOperationException("XPath path is not set.");
        }

        return xpath.evaluateBooleanResult(node);
    }

    /**
     * Returns the XPath element.
     *
     * @return  The XPath string.
     */
    public String getXPath()
    {
        return xpathString;
    }

    /**
     * Returns the XPathMetaInfo.
     *
     * @return  Returns the XPathMetaInfo.
     */
    public XPathMetaInfo getXPathInfo()
    {
        return xpathInfo;
    }

    /**
     * Sets the XPath element.
     *
     * @param  xpath  The new XPath.
     */
    public void setXPath(String xpath)
    {
        this.xpath = XPath.getXPathInstance(xpath);
        this.xpathString = xpath;
    }

    /**
     * Sets the XPathMetaInfo.
     *
     * @param  xpathInfo  The XPathMetaInfo to be set.
     */
    public void setXPathInfo(XPathMetaInfo xpathInfo)
    {
        this.xpathInfo = xpathInfo;
    }
}

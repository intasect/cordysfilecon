/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.utils;

import com.cordys.coe.ac.fileconnector.exception.ConfigException;
import com.cordys.coe.ac.fileconnector.exception.FileException;

import com.eibus.xml.nom.Find;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPathMetaInfo;

/**
 * XML utility methods.
 *
 * @author  mpoyhone
 */
public class XmlUtils
{
    /**
     * Parses namespace bindings from an XML structure.
     *
     * @param   configNode  Configuration node.
     * @param   deleteNode  If <code>true</code> the configuration XML is deleted.
     *
     * @return  XPathWrapperFactory containing the bindings.
     *
     * @throws  ConfigException  Thrown if the parsing failed.
     */
    public static XPathWrapperFactory parseNamespaceBindings(int configNode, boolean deleteNode)
                                                      throws ConfigException
    {
        int namespacesNode = Find.firstMatch(configNode, "<><namespaces>");

        if (namespacesNode == 0)
        {
            return XPathWrapperFactory.createFactory();
        }

        int[] elems = Find.match(namespacesNode, "<><binding>");
        XPathMetaInfo bindings = new XPathMetaInfo();

        for (int node : elems)
        {
            String prefix = Node.getAttribute(node, "prefix");
            String uri = Node.getAttribute(node, "uri");

            if (prefix == null)
            {
                throw new ConfigException("Missing attribute 'prefix' from namespace binding element.");
            }

            if ((uri == null) || (uri.length() == 0))
            {
                throw new ConfigException("Missing attribute 'uri' from namespace binding element.");
            }

            bindings.addNamespaceBinding(prefix, uri);
        }

        if (deleteNode)
        {
            Node.delete(namespacesNode);
        }

        return XPathWrapperFactory.createFactory(bindings);
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode  Root XML node.
     * @param   elemName  Parameter element name.
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static boolean getBooleanParameter(int rootNode, String elemName)
                                       throws FileException
    {
        String value = getStringParameter(rootNode, elemName, "false");

        return "true".equals(value);
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode   Root XML node.
     * @param   elemName   Parameter element name.
     * @param   mandatory  Thrown if <code>true</code> and the parameter does not exist
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static boolean getBooleanParameter(int rootNode, String elemName, boolean mandatory)
                                       throws FileException
    {
        String value = getStringParameter(rootNode, elemName, mandatory);

        return "true".equals(value);
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode      Root XML node.
     * @param   elemName      Parameter element name.
     * @param   defaultValue  Value to be returned if the parameter did not exist.
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static double getDoubleParameter(int rootNode, String elemName, double defaultValue)
                                     throws FileException
    {
        String value = getStringParameter(rootNode, elemName, Double.toString(defaultValue));

        try
        {
            return Double.parseDouble(value);
        }
        catch (Exception e)
        {
            throw new FileException("Illegal value " + value + " for parameter " + elemName);
        }
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode   Root XML node.
     * @param   elemName   Parameter element name.
     * @param   mandatory  Thrown if <code>true</code> and the parameter does not exist
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static double getDoubleParameter(int rootNode, String elemName, boolean mandatory)
                                     throws FileException
    {
        String value = getStringParameter(rootNode, elemName, mandatory);

        try
        {
            return Double.parseDouble(value);
        }
        catch (Exception e)
        {
            throw new FileException("Illegal value " + value + " for parameter " + elemName);
        }
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode      Root XML node.
     * @param   elemName      Parameter element name.
     * @param   defaultValue  Value to be returned if the parameter did not exist.
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static long getLongParameter(int rootNode, String elemName, long defaultValue)
                                 throws FileException
    {
        String value = getStringParameter(rootNode, elemName, Long.toString(defaultValue));

        try
        {
            return Long.parseLong(value);
        }
        catch (Exception e)
        {
            throw new FileException("Illegal value " + value + " for parameter " + elemName);
        }
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode   Root XML node.
     * @param   elemName   Parameter element name.
     * @param   mandatory  Thrown if <code>true</code> and the parameter does not exist
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static long getLongParameter(int rootNode, String elemName, boolean mandatory)
                                 throws FileException
    {
        String value = getStringParameter(rootNode, elemName, mandatory);

        try
        {
            return Long.parseLong(value);
        }
        catch (Exception e)
        {
            throw new FileException("Illegal value " + value + " for parameter " + elemName);
        }
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode      Root XML node.
     * @param   elemName      Parameter element name.
     * @param   defaultValue  Value to be returned if the parameter did not exist.
     *
     * @return  Parameter value.
     */
    public static String getStringParameter(int rootNode, String elemName, String defaultValue)
    {
        int node;
        String value = null;

        if ((node = Find.firstMatch(rootNode, "<><" + elemName + ">")) != 0)
        {
            value = Node.getData(node);
        }

        if (value == null)
        {
            return defaultValue;
        }

        return value;
    }

    /**
     * Returns a parameter from the XML.
     *
     * @param   rootNode   Root XML node.
     * @param   elemName   Parameter element name.
     * @param   mandatory  Thrown if <code>true</code> and the parameter does not exist
     *
     * @return  Parameter value.
     *
     * @throws  FileException
     */
    public static String getStringParameter(int rootNode, String elemName, boolean mandatory)
                                     throws FileException
    {
        int node;
        String value = null;

        if ((node = Find.firstMatch(rootNode, "<><" + elemName + ">")) != 0)
        {
            value = Node.getData(node);
        }

        if (mandatory && ((value == null) || (value.length() == 0)))
        {
            throw new FileException("Parameter " + elemName + " is missing.");
        }

        return value;
    }
}

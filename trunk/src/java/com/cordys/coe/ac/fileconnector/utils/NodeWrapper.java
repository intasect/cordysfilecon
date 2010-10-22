/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.utils;

import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;

import com.eibus.xml.nom.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class contains NOM wrapper methods for dealing with differences between C2 and C3
 * interfaces.
 *
 * @author  mpoyhone
 */
public class NodeWrapper
{
    /**
     * Logger for log messages from this class.
     */
    private static final CordysLogger LOG = CordysLogger.getCordysLogger(XmlUtils.class);
    /**
     * Method reference for C3 method: public static int setNSDefinition(int node, java.lang.String
     * prefix, java.lang.String uri);
     */
    private static Method C3_method_Node_setNSDefinition;
    /**
     * Method reference for C3 method: public static int getFirstChildElement(int node);
     */
    private static Method C3_method_Node_getFirstChildElement;
    /**
     * Method reference for C3 method: public static int getLastChildElement(int node);
     */
    private static Method C3_method_Node_getLastChildElement;

    static
    {
        try
        {
            C3_method_Node_setNSDefinition = Node.class.getDeclaredMethod("setNSDefinition",
                                                                          Integer.TYPE,
                                                                          String.class,
                                                                          String.class);
            C3_method_Node_getLastChildElement = Node.class.getDeclaredMethod("getLastChildElement",
                                                                              Integer.TYPE);
        }
        catch (Exception e)
        {
            LOG.log(Severity.FATAL, "Unable to load NOM methods.");
        }
    }

    /**
     * Returns the first child element.
     *
     * @param   node  Current node.
     *
     * @return  First child element or zero if none was found.
     */
    public static int getFirstChildElement(int node)
    {
        if (C3_method_Node_getFirstChildElement != null)
        {
            invokeStatic(C3_method_Node_getFirstChildElement, node);
        }
        else
        {
            return Node.getFirstChild(node);
        }

        return 0;
    }

    /**
     * Returns the last child element.
     *
     * @param   node  Current node.
     *
     * @return  Last child element or zero if none was found.
     */
    public static int getLastChildElement(int node)
    {
        if (C3_method_Node_getLastChildElement != null)
        {
            invokeStatic(C3_method_Node_getLastChildElement, node);
        }
        else
        {
            return Node.getLastChild(node);
        }

        return 0;
    }

    /**
     * Calls C3 method: public static int setNSDefinition(int node, java.lang.String prefix,
     * java.lang.String uri);
     *
     * <p>For C2 this does nothing.</p>
     *
     * @param  node     Node reference.
     * @param  srcNode  Prefix and URI are taken from this node.
     */
    public static void setNSDefinition(int node, int srcNode)
    {
        setNSDefinition(node, Node.getPrefix(srcNode), Node.getNamespaceURI(srcNode));
    }

    /**
     * Calls C3 method: public static int setNSDefinition(int node, java.lang.String prefix,
     * java.lang.String uri);
     *
     * <p>For C2 this does nothing.</p>
     *
     * @param  node    Node reference.
     * @param  prefix  New prefix.
     * @param  uri     New URI.
     */
    public static void setNSDefinition(int node, String prefix, String uri)
    {
        if (C3_method_Node_setNSDefinition != null)
        {
            invokeStatic(C3_method_Node_setNSDefinition, node, prefix, uri);
        }
    }

    /**
     * Calls a static Java method. This allows the method to be called easily:
     *
     * <pre>
        invokeStatic(C3_method_Node_setNSDefinition, node, prefix, uri);
     * </pre>
     *
     * @param   m     Method object
     * @param   args  Arguments.
     *
     * @return  Method return value.
     */
    private static Object invokeStatic(Method m, Object... args)
    {
        try
        {
            return m.invoke(null, args);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalStateException("Unable to call method: " + m.getName(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException("Unable to call method.", e);
        }
        catch (InvocationTargetException e)
        {
            throw new IllegalStateException("Unable to call method.", e);
        }
    }
}

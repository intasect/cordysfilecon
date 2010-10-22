/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector.methods.largexml;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains XML namespace and prefix information.
 *
 * @author  mpoyhone
 */
public class NamespaceContext
{
    /**
     * DOCUMENTME.
     */
    public static final String DEFAULT_PREFIX = "";
    /**
     * DOCUMENTME.
     */
    private String defaultUri;
    /**
     * DOCUMENTME.
     */
    private NamespaceContext parent;
    /**
     * DOCUMENTME.
     */
    private Map<String, String> prefixToUriMap = new HashMap<String, String>();
    /**
     * DOCUMENTME.
     */
    private Map<String, String> uriToPrefixMap = new HashMap<String, String>();

    /**
     * Constructor for NamespaceContext.
     */
    public NamespaceContext()
    {
    }

    /**
     * Constructor for NamespaceContext.
     *
     * @param  parent  Parent namespace context.
     */
    public NamespaceContext(NamespaceContext parent)
    {
        this.parent = parent;
    }

    /**
     * DOCUMENTME.
     *
     * @param  prefix  DOCUMENTME
     * @param  uri     DOCUMENTME
     */
    public void addNamespace(String prefix, String uri)
    {
        if ((prefix == null) || DEFAULT_PREFIX.equals(prefix))
        {
            defaultUri = uri;
        }

        uriToPrefixMap.put(uri, prefix);
        prefixToUriMap.put(prefix, uri);
    }

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public NamespaceContext getParent()
    {
        return parent;
    }

    /**
     * DOCUMENTME.
     *
     * @param   uri  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public String getPrefix(String uri)
    {
        if ((defaultUri != null) && defaultUri.equals(uri))
        {
            return DEFAULT_PREFIX;
        }

        String res = uriToPrefixMap.get(uri);

        if ((res == null) && (parent != null))
        {
            res = parent.getPrefix(uri);
        }

        return res;
    }

    /**
     * DOCUMENTME.
     *
     * @param   prefix  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public String getUri(String prefix)
    {
        if ((prefix == null) || DEFAULT_PREFIX.equals(prefix))
        {
            if (defaultUri != null)
            {
                return defaultUri;
            }
            else if (parent != null)
            {
                return parent.getUri(prefix);
            }
            else
            {
                return null;
            }
        }

        String res = prefixToUriMap.get(prefix);

        if ((res == null) && (parent != null))
        {
            res = parent.getUri(prefix);
        }

        return res;
    }

    /**
     * DOCUMENTME.
     *
     * @param   prefix  DOCUMENTME
     * @param   uri     DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public boolean isDefined(String prefix, String uri)
    {
        String definedUri = getUri(prefix);

        return (definedUri != null) && definedUri.equals(uri);
    }
}

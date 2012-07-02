
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

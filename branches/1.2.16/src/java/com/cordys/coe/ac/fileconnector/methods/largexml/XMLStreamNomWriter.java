
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
 package com.cordys.coe.ac.fileconnector.methods.largexml;

import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

import java.util.Collection;

import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A class to create a NOM XML tree from an XMLStreamReader.
 *
 * @author  mpoyhone
 */
public class XMLStreamNomWriter
{
    /**
     * NOM document.
     */
    private Document doc;
    /**
     * Parent context.
     */
    private XmlTraverseContext parentContext;
    /**
     * Actual stream reader.
     */
    private XMLStreamReader xmlReader;

    /**
     * Constructor for XMLStreamNomWriter.
     *
     * @param  reader         Used from reading the XML. This must be positioned a START_ELEMEMT.
     * @param  doc            Document for creating NOM XML.
     * @param  parentContext  Optional context for top level namespaces.
     */
    public XMLStreamNomWriter(XMLStreamReader reader, Document doc,
                              XmlTraverseContext parentContext)
    {
        super();
        this.xmlReader = reader;
        this.doc = doc;
        this.parentContext = parentContext;
    }

    /**
     * Creates a NOM XML from the XML stream.
     *
     * @return  NOM root node.
     *
     * @throws  XMLStreamException  Thrown if the parsing failed.
     */
    public int createNomTree()
                      throws XMLStreamException
    {
        boolean first = true;
        int currentLevel = 0;
        NamespaceContext currentNamespace = new NamespaceContext();
        int currentNode = 0;

        try
        {
            while (true)
            {
                int event;

                if (!first)
                {
                    event = xmlReader.next();
                }
                else
                {
                    event = XMLStreamConstants.START_ELEMENT;
                    first = false;
                }

                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                    {
                        // Create a new namespace context.
                        currentNamespace = new NamespaceContext(currentNamespace);
                        currentLevel++;

                        // Create a node and proper namespace declarations.
                        QName elementName = xmlReader.getName();
                        int newNode = createElement(currentNamespace, elementName, currentNode);

                        currentNode = newNode;
                    }
                    break;

                    case XMLStreamConstants.END_ELEMENT:
                    {
                        if (currentNode == 0)
                        {
                            throw new IllegalStateException("Unexpected end element.");
                        }

                        if (--currentLevel <= 0)
                        {
                            int res = currentNode;

                            currentNode = 0;

                            return Node.getRoot(res);
                        }

                        currentNode = Node.getParent(currentNode);
                        currentNamespace = currentNamespace.getParent();
                    }
                    break;

                    case XMLStreamConstants.CHARACTERS:
                    {
                        if (currentNode == 0)
                        {
                            throw new IllegalStateException("Unexpected TEXT section.");
                        }

                        if (!xmlReader.isWhiteSpace())
                        {
                            String data = xmlReader.getText();

                            doc.createText(data, currentNode);
                        }
                    }
                    break;

                    case XMLStreamConstants.CDATA:
                    {
                        if (currentNode == 0)
                        {
                            throw new IllegalStateException("Unexpected CDATA section.");
                        }

                        String data = xmlReader.getText();

                        Node.appendToChildren(doc.createCData(data), currentNode);
                    }
                    break;

                    case XMLStreamConstants.END_DOCUMENT:
                        throw new IllegalStateException("Unexpected end of document encountered.");
                }
            }
        }
        finally
        {
            if (currentNode != 0)
            {
                Node.delete(currentNode);
                currentNode = 0;
            }
        }
    }

    /**
     * Creates a new NOM element.
     *
     * @param   ctx         Namespace context.
     * @param   name        Element qname.
     * @param   parentNode  Parent NOM node.
     *
     * @return  Created element.
     */
    private int createElement(NamespaceContext ctx, QName name, int parentNode)
    {
        String localName = name.getLocalPart();
        String prefix = name.getPrefix();
        String uri = name.getNamespaceURI();
        String elemName;

        if ((prefix != null) && (prefix.length() > 0))
        {
            elemName = prefix + ":" + localName;
        }
        else
        {
            elemName = localName;
        }

        int newNode;

        if (parentNode != 0)
        {
            newNode = Node.createElement(elemName, parentNode);
        }
        else
        {
            newNode = doc.createElement(elemName);

            if (parentContext != null)
            {
                Collection<String[]> nsDeclarations = parentContext.getDeclaredNamespaces();

                for (String[] nsDecl : nsDeclarations)
                {
                    declareNamespace(newNode, nsDecl[0], nsDecl[1], ctx);
                }
            }
        }

        if ((uri != null) && (uri.length() > 0))
        {
            if (!ctx.isDefined(prefix, uri))
            {
                declareNamespace(newNode, prefix, uri, ctx);
            }
        }

        int attrCount = xmlReader.getAttributeCount();

        for (int i = 0; i < attrCount; i++)
        {
            QName attrQName = xmlReader.getAttributeName(i);
            String attrLocalName = attrQName.getLocalPart();
            String attrPrefix = attrQName.getPrefix();
            String attrUri = attrQName.getNamespaceURI();
            String attrValue = xmlReader.getAttributeValue(i);
            String attrName;

            if ((attrPrefix != null) && (attrPrefix.length() > 0))
            {
                attrName = attrPrefix + ":" + attrLocalName;
            }
            else
            {
                attrName = attrLocalName;
            }

            if ((attrUri != null) && (attrUri.length() > 0))
            {
                if (!ctx.isDefined(attrPrefix, attrUri))
                {
                    declareNamespace(newNode, attrPrefix, attrUri, ctx);
                }
            }

            Node.setAttribute(newNode, attrName, attrValue);
        }

        return newNode;
    }

    /**
     * Adds a namespace declaration.
     *
     * @param  node    Target node.
     * @param  prefix  Prefix (can be <code>null</code>).
     * @param  uri     Namespace URI.
     * @param  ctx     Declaration is also added to this context.
     */
    private void declareNamespace(int node, String prefix, String uri, NamespaceContext ctx)
    {
        String nsAttrName;

        if ((prefix != null) && (prefix.length() > 0))
        {
            nsAttrName = "xmlns:" + prefix;
        }
        else
        {
            nsAttrName = "xmlns";
        }

        Node.setAttribute(node, nsAttrName, uri);
        ctx.addNamespace(prefix, uri);
    }
}

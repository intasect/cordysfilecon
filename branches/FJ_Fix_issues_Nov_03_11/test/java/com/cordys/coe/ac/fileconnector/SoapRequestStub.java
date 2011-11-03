/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import com.cordys.coe.exception.GeneralException;
import com.cordys.coe.util.XMLProperties;
import com.eibus.connector.nom.Connector;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Stub class for the FileConnector SOAP request.
 *
 * @author mpoyhone
 */
public class SoapRequestStub implements ISoapRequestContext
{
    private int requestNode;
    private int responseNode;

    /**
     * Constructor for SoapRequestStub
     * @param requestNode
     * @param responseNode
     */
    public SoapRequestStub(int requestNode, int responseNode)
    {
        super();
        this.requestNode = requestNode;
        this.responseNode = responseNode;
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getMethodImplementation()
     */
    public int getMethodImplementation()
    {
        throw new UnsupportedOperationException("getMethodImplementation");
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getNomConnector()
     */
    public Connector getNomConnector()
    {
        throw new UnsupportedOperationException("getNomConnector");
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getNomDocument()
     */
    public Document getNomDocument()
    {
        return Node.getDocument(requestNode);
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getRequestAsXmlProperties()
     */
    public XMLProperties getRequestAsXmlProperties() throws GeneralException
    {
        return new XMLProperties(requestNode);
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getRequestOrganizationDn()
     */
    public String getRequestOrganizationDn()
    {
        return "org-dn";
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getRequestRootNode()
     */
    public int getRequestRootNode()
    {
        return requestNode;
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getRequestUserDn()
     */
    public String getRequestUserDn()
    {
        return "user-dn";
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#getResponseRootNode()
     */
    public int getResponseRootNode()
    {
        return responseNode;
    }
    
    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#addResponseElement(java.lang.String, java.lang.String)
     */
    public int addResponseElement(String elemName, String elemValue)
    {
        return Node.createTextElement(elemName, elemValue, responseNode);
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#addResponseElement(java.lang.String)
     */
    public int addResponseElement(String elemName)
    {
        return Node.createElement(elemName, responseNode);
    }
    
    /**
     * @see com.cordys.coe.ac.fileconnector.ISoapRequestContext#addResponseElement(int)
     */
    public int addResponseElement(int node)
    {
        return Node.appendToChildren(node, responseNode);
    }
}

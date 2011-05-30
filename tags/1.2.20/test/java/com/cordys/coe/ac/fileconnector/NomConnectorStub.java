/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import java.text.MessageFormat;
import java.util.concurrent.Exchanger;

import com.cordys.coe.util.soap.SOAPException;
import com.cordys.coe.util.soap.SoapFaultInfo;
import com.eibus.connector.nom.Connector;
import com.eibus.directory.soap.DirectoryException;
import com.eibus.exception.ExceptionGroup;
import com.eibus.exception.TimeoutException;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;

/**
 * Stub for the NOM connector interface.
 *
 * @author mpoyhone
 */
public class NomConnectorStub implements INomConnector
{
    private Document doc;
    protected static String sSoapMessageTemplate = 
        "<SOAP:Envelope xmlns:SOAP=\"http://schemas.xmlsoap.org/soap/envelope/\">" + 
        "<SOAP:Header>" + 
        "<header xmlns=\"http://schemas.cordys.com/General/1.0/\">" + 
        "<sender>" + 
        "<component>NomConnectorStub</component>" + 
        "<reply-to></reply-to>" + 
        "<user>{2}</user>" + 
        "</sender>" + 
        "<receiver><component>{3}</component></receiver>" + 
        "<msg-id>Fixed Message ID</msg-id>" + 
        "</header>" + 
        "</SOAP:Header>" + 
        "<SOAP:Body>" + 
        "<{0} xmlns=\"{1}\">" +
        "</{0}>" +
        "</SOAP:Body>" + 
        "</SOAP:Envelope>";    
    private Exchanger<Integer> requestEnchanger = new Exchanger<Integer>();
    
    /**
     * Constructor for NomConnectorStub
     * @param doc
     */
    public NomConnectorStub(Document doc)
    {
        super();
        this.doc = doc;
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.INomConnector#createSoapMethod(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public int createSoapMethod(String organization, String orgUser,
            String methodName, String namespace) throws DirectoryException
    {
        int env = 0;
        
        try {
            env = doc.parseString(MessageFormat.format(sSoapMessageTemplate, methodName, namespace, orgUser, organization));
        }
        catch (Exception e) {
            throw new DirectoryException("Unable to parse the SOAP request template", 0, e);
        }
        
        return Node.getFirstChild(Node.getLastChild(env));
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.INomConnector#getNomConnector()
     */
    public Connector getNomConnector()
    {
        return null;
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.INomConnector#sendAndWait(int, boolean)
     */
    public int sendAndWait(int requestMethodNode, boolean checkSoapFault)
            throws TimeoutException, ExceptionGroup, SOAPException
    {
        int node = Node.getFirstChild(Node.getLastChild(Node.duplicate(Node.getRoot(requestMethodNode))));
        int res;
        
        try
        {
            res = requestEnchanger.exchange(node);
        }
        catch (InterruptedException e)
        {
            Node.delete(Node.getRoot(node));
            throw new SOAPException("sendAndWait was interrupted.");
        }
        
        if (checkSoapFault) {
            SoapFaultInfo faultInfo = SoapFaultInfo.findSoapFault(Node.getRoot(res));
            
            if (faultInfo != null) {
                String msg = faultInfo.toString();
                
                Node.delete(Node.getRoot(res));
                res = 0;
                
                throw new SOAPException(msg);
            }
        }
        
        return res;
    }
    
    public int waitForRequest() throws SOAPException
    {
        int request;
        
        try
        {
            request = createSoapMethod(null, null, "DummyResponse", "http://DummyNamespace");
        }
        catch (DirectoryException e)
        {
            throw new SOAPException("Unable to create the repsonse.");
        }

        try {
            return waitForRequest(request);
        }
        finally 
        {
            Node.delete(Node.getRoot(request));
        }
    }
    
    public int waitForRequest(int responseMethodNode) throws SOAPException
    {
        int node = Node.getFirstChild(Node.getLastChild(Node.duplicate(Node.getRoot(responseMethodNode))));
        
        try
        {
            return requestEnchanger.exchange(node);
        }
        catch (InterruptedException e)
        {
            Node.delete(Node.getRoot(node));
            throw new SOAPException("waitForRequest was interrupted.");
        }
    }

}

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

/**
 * Interface for accessing information about the incoming SOAP request and setting the SOAP
 * response.
 *
 * @author  mpoyhone
 */
public interface ISoapRequestContext
{
    /**
     * DOCUMENTME.
     *
     * @param   elemName  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public int addResponseElement(String elemName);

    /**
     * DOCUMENTME.
     *
     * @param   node  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public int addResponseElement(int node);

    /**
     * DOCUMENTME.
     *
     * @param   elemName   DOCUMENTME
     * @param   elemValue  DOCUMENTME
     *
     * @return  DOCUMENTME
     */
    public int addResponseElement(String elemName, String elemValue);

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public int getMethodImplementation();

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public Connector getNomConnector();

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public Document getNomDocument();

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     *
     * @throws  GeneralException  DOCUMENTME
     */
    public XMLProperties getRequestAsXmlProperties()
                                            throws GeneralException;

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getRequestOrganizationDn();

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public int getRequestRootNode();

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public String getRequestUserDn();

    /**
     * DOCUMENTME.
     *
     * @return  DOCUMENTME
     */
    public int getResponseRootNode();
}

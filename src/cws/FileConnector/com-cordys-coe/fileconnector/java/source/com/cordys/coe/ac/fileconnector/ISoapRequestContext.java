
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

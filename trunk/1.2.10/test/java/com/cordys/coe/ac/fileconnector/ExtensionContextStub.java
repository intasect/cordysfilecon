/**
 * (c) 2008 Cordys R&D B.V. All rights reserved. The computer program(s) is the
 * proprietary information of Cordys B.V. and provided under the relevant
 * License Agreement containing restrictions on use and disclosure. Use is
 * subject to the License Agreement.
 */
package com.cordys.coe.ac.fileconnector;

import java.io.File;

import com.eibus.management.IManagedComponent;
import com.eibus.xml.nom.Document;

/**
 * Stub class for the extension context interface
 *
 * @author mpoyhone
 */
public class ExtensionContextStub implements IExtensionContext  
{
    private NomConnectorStub connector;
    
    
    /**
     * Constructor for ExtensionContextStub
     * @param doc
     */
    public ExtensionContextStub(Document doc)
    {
        super();
        this.connector = new NomConnectorStub(doc);
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.IExtensionContext#getNomConnector()
     */
    public NomConnectorStub getNomConnector()
    {
        return connector;
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.IExtensionContext#getOrganizationDn()
     */
    public String getOrganizationDn()
    {
        return "dummy-org-dn";
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.IExtensionContext#getOrganizationalSystemUserDn()
     */
    public String getOrganizationalSystemUserDn()
    {
        return "SYSTEM@dummy-org-dn";
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.IExtensionContext#getInstallationFolder()
     */
    public File getInstallationFolder()
    {
        return new File("INSTALLATION_FOLDER");
    }

    /**
     * @see com.cordys.coe.ac.fileconnector.IExtensionContext#getJmxComponent()
     */
    public IManagedComponent getJmxComponent()
    {
        return null;
    }

}

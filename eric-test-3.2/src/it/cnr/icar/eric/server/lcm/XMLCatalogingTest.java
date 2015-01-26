/*
 * ====================================================================
 * This file is part of the ebXML Registry by Icar Cnr v3.2 
 * ("eRICv32" in the following disclaimer).
 *
 * "eRICv32" is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * "eRICv32" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License Version 3
 * along with "eRICv32".  If not, see <http://www.gnu.org/licenses/>.
 *
 * eRICv32 is a forked, derivative work, based on:
 * 	- freebXML Registry, a royalty-free, open source implementation of the ebXML Registry standard,
 * 	  which was published under the "freebxml License, Version 1.1";
 *	- ebXML OMAR v3.2 Edition, published under the GNU GPL v3 by S. Krushe & P. Arwanitis.
 * 
 * All derivative software changes and additions are made under
 *
 * Copyright (C) 2013 Ing. Antonio Messina <messina@pa.icar.cnr.it>
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the freebxml Software Foundation.  For more
 * information on the freebxml Software Foundation, please see
 * "http://www.freebxml.org/".
 *
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * ====================================================================
 */

package it.cnr.icar.eric.server.lcm;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;


/**
 * @author Tony Graham
 */
public class XMLCatalogingTest extends ServerTest {
    
    /**
     * Constructor for XMLCatalogingTest
     *
     * @param name
     */
    public XMLCatalogingTest(String name) {
        super(name);
    }
        
    /** Tests submission of an ExtrinsicObject without a RepositoryItem */
    @SuppressWarnings("static-access")
	public void testEoIdIsUuidNoRI() throws Exception {        
        String eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();

	try {
            submitExtrinsicObject(eoId,
                                  "eoId=UUID, no ri",
                                  bu.CPP_CLASSIFICATION_NODE_ID,
                                  null);
        } catch (Exception e) {
            e.printStackTrace();
            fail("submitExtrinsicObject() threw an exception");
        }
    }

    /** Tests submission of an ExtrinsicObject with a RepositoryItem */
    @SuppressWarnings("static-access")
	public void testEoIdIsUuidHasRI() throws Exception {
        URL url = getClass().getResource("/resources/CPP1.xml");

	DataHandler dataHandler =
	    new javax.activation.DataHandler(url);
        String eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        try {
            submitExtrinsicObject(eoId,
                                  "eoId=UUID has ri",
                                  bu.CPP_CLASSIFICATION_NODE_ID,
                                  dataHandler);
        } catch (Exception e) {
            e.printStackTrace();
            fail("submitExtrinsicObject() threw an exception");
        }
    }

    @SuppressWarnings("static-access")
	public void testEoIdIsNonUuidRHasRI() throws Exception {        
        URL url = getClass().getResource("/resources/CPP1.xml");

	DataHandler dataHandler =
	    new javax.activation.DataHandler(url);
        String eoId = "nonUUID";
	@SuppressWarnings("unused")
	String riId = eoId;

	try {
            submitExtrinsicObject(eoId,
                                  "eoId=nonUUID has ri",
                                  bu.CPP_CLASSIFICATION_NODE_ID,
                                  dataHandler);
        } catch (Exception e) {
            e.printStackTrace();
            fail("submitExtrinsicObject() threw an exception");
        }
    }

    void submitExtrinsicObject(String eoId,
			       String eoName,
			       String eoObjectType,
			       DataHandler riDataHandler)
	throws Exception {

	ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        eo.setId(eoId);
	eo.setObjectType(eoObjectType);

	eo.setName(bu.createInternationalStringType(eoName));
        eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());

        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createExtrinsicObject(eo));
        
        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);

	HashMap<String, Object> idToRepositoryItemMap = new HashMap<String,Object>();
	if (riDataHandler != null) {
	    eo.setMimeType(riDataHandler.getContentType());
	    RepositoryItem ri =
		createRepositoryItem(riDataHandler, eoId);
	    idToRepositoryItemMap.put(eoId, ri);
	}
           
        ServerRequestContext context = new ServerRequestContext("XMLCatalogingTest:submitExtrinsicObject", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);

        RegistryResponseType resp = lcm.submitObjects(context);
        BindingUtility.getInstance().checkRegistryResponse(resp);
    }


    /**
     * Creates a {@link CredentialInfo} for a {@link DataHandler}.
     *
     * <p>Uses a dummy X500 name for signing the {@link CredentialInfo}.
     *
     * @param dh the <code>DataHandler</code>
     * @return the <code>CredentialInfo</code> for the <code>DataHandler</code>
     * @exception Exception if an error occurs
     */
    CredentialInfo createCredentialInfo(DataHandler dh) throws Exception {
        //Generating 512 bit DSA key pair and self-signed certificate (SHA1WithDSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("DSA", "SHA1WithDSA");
        X500Name x500name = new X500Name("Tester", "Test Unit", "OMAR", "JKL", "KS", "FI");
        certandkeygen.generate(512);
        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] = certandkeygen.getSelfCertificate(x500name,  90 * 24 * 60 * 60);

        //Create the credentials to use.
        //TODO: this could be created only once per SOAPSender
        //TODO: now signing with null server alias. Might gen overhead by sending cert
	return new CredentialInfo((String)null,
				  ax509cert[0], ax509cert, privateKey);
    }

    public static Test suite() {
        return new TestSuite(XMLCatalogingTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
}

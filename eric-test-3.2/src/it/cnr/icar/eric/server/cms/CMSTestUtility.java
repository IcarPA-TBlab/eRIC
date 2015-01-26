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

package it.cnr.icar.eric.server.cms;


import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

import sun.security.x509.CertAndKeyGen;
import sun.security.x509.X500Name;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;

import java.net.URL;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.activation.DataHandler;

/**
 *
 * @author  tg127171
 */
public class CMSTestUtility {
    protected static URL cppaURL = CMSTestUtility.class.getResource(
            "/resources/CPP1.xml");
    protected static BindingUtility bu = BindingUtility.getInstance();
    private static CMSTestUtility instance = null;
    
    /** Creates a new instance of CMSTestUtility */
    private CMSTestUtility() {
    }

    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public static CMSTestUtility getInstance() {
        if (instance == null) {
            synchronized (CMSTestUtility.class) {
                if (instance == null) {
                    instance = new CMSTestUtility();
                }
            }
        }

        return instance;
    }
    /**
     * Creates an ExtrinsicObject of specific type.
     *
     * @return an <code>ExtrinsicObjectType</code> value
     * @exception Exception if an error occurs
     */
    static ExtrinsicObjectType createExtrinsicObject(String desc,
        String objectType) throws Exception {
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();

        if (desc != null) {
            eo.setDescription(bu.createInternationalStringType(desc));
        }

        String eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        eo.setId(eoId);
        eo.setObjectType(objectType);
        eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());

        return eo;
    }

    /**
     * Creates a CPP RepositoryItem.
     *
     * @param eoId id to use when signing the RepositoryItem
     * @return a <code>RepositoryItem</code> value
     * @exception Exception if an error occurs
     */
    RepositoryItem createCPPRepositoryItem(String eoId)
        throws Exception {
        DataHandler dataHandler = new javax.activation.DataHandler(cppaURL);

        return createRepositoryItem(dataHandler, eoId);
    }
    
    /**
     * Creates a {@link RepositoryItem}.
     *
     * @param dh the {@link DataHandler} representing the payload
     * @param id the ID to use for the {@link RepositoryItem}
     * @exception Exception if an error occurs
     */
    RepositoryItem createRepositoryItem(DataHandler dh, String id)
        throws Exception {
        RepositoryItem ri = new RepositoryItemImpl(id, dh);
        return ri;
    }
    

//    /**
//     * Creates a {@link RepositoryItem} that is signed with generated
//     * credentials.
//     *
//     * @param dh the {@link DataHandler} representing the payload
//     * @param id the ID to use for the {@link RepositoryItem}
//     * @return a signed {@link RepositoryItem}
//     * @exception Exception if an error occurs
//     */
//    RepositoryItem createSignedRepositoryItem(DataHandler dh, String id)
//        throws Exception {
//        return su.signPayload(dh, id, createCredentialInfo(dh));
//    }

    /**
     * Creates a {@link CredentialInfo} for a {@link DataHandler}.
     *
     * <p>Uses a dummy X500 name for signing the {@link CredentialInfo}.
     *
     * @param dh the <code>DataHandler</code>
     * @return the <code>CredentialInfo</code> for the <code>DataHandler</code>
     * @exception Exception if an error occurs
     */
    CredentialInfo createCredentialInfo(DataHandler dh)
        throws Exception {
        //Generating 512 bit DSA key pair and self-signed certificate (SHA1WithDSA) for TestUser-DN"));
        CertAndKeyGen certandkeygen = new CertAndKeyGen("DSA", "SHA1WithDSA");
        X500Name x500name = new X500Name("Tester", "Test Unit", "OMAR", "JKL",
                "KS", "FI");
        certandkeygen.generate(512);

        PrivateKey privateKey = certandkeygen.getPrivateKey();
        X509Certificate[] ax509cert = new X509Certificate[1];
        ax509cert[0] = certandkeygen.getSelfCertificate(x500name,
                90 * 24 * 60 * 60);

        //Create the credentials to use.
        //TODO: this could be created only once per SOAPSender
        //TODO: now signing with null server alias. Might gen overhead by sending cert
        return new CredentialInfo((String) null, ax509cert[0], ax509cert,
            privateKey);
    }
}

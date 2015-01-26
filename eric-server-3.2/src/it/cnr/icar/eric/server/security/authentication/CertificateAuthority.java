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
package it.cnr.icar.eric.server.security.authentication;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RegistryResponseHolder;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.exceptions.InvalidContentException;
import it.cnr.icar.eric.common.exceptions.MissingRepositoryItemException;
import it.cnr.icar.eric.common.security.KeyTool;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.security.auth.x500.X500Principal;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;
import org.oasis.ebxml.registry.bindings.rim.PostalAddressType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;


/**
 * CA that generates user certs signed by the RegistryOperator private key
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class CertificateAuthority {
    /*# private CertificateAuthority _certificateAuthority; */
    private static CertificateAuthority instance = null;
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(CertificateAuthority.class);

    private static AuthenticationServiceImpl ac = null;
    private static BindingUtility bu = BindingUtility.getInstance();

    private Certificate caCertificate = null;
    
    protected CertificateAuthority() {
    }
    
    public synchronized static CertificateAuthority getInstance() {
        if (instance == null) {
            instance = new CertificateAuthority();
            ac = AuthenticationServiceImpl.getInstance();
        }

        return instance;
    }
    
    @SuppressWarnings("static-access")
	private Certificate getCACertificate() throws RegistryException {
        if (caCertificate == null) {
            caCertificate = ac.getCertificate(ac.ALIAS_REGISTRY_OPERATOR);
        }
        return caCertificate;
    }
    
    /** Extension request to sign specified cert and return the signed cert. */
    @SuppressWarnings("static-access")
	public RegistryResponseHolder signCertificateRequest(UserType user,
        RegistryRequestType req, Map<?, ?> idToRepositoryItemMap) throws RegistryException {
        
        RegistryResponseHolder respHolder = null;
        RegistryResponseType ebRegistryResponseType = null;
        ServerRequestContext context = null;

        try {
            context = new ServerRequestContext("CertificateAUthority.signCertificateRequest", req);
            context.setUser(user);
            
            if (idToRepositoryItemMap.keySet().size() == 0) {
                throw new MissingRepositoryItemException(ServerResourceBundle.getInstance().getString("message.KSRepItemNotFound"));
            }
                        
            
            String id = (String)idToRepositoryItemMap.keySet().iterator().next();
            
            Object obj = idToRepositoryItemMap.get(id);
            if (!(obj instanceof RepositoryItem)) {
                throw new InvalidContentException();
            }
            RepositoryItem ri = (RepositoryItem)obj;    //This is the JKS keystore containing cert to be signed            
            
            //Read original cert from keystore
            InputStream is = ri.getDataHandler().getInputStream();            
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, bu.FREEBXML_REGISTRY_KS_PASS_REQ.toCharArray());            
            is.close();            
            X509Certificate cert = (X509Certificate)keyStore.getCertificate(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_REQ);
            
            //Sign the cert
            cert = signCertificate(cert);
            
            //Replace cert with signed cert in keystore
            keyStore.deleteEntry(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_REQ);
            keyStore.setCertificateEntry(bu.FREEBXML_REGISTRY_USERCERT_ALIAS_RESP, cert);
            
            //Add CA root cert (RegistryOPerator's cert) to keystore.
            keyStore.setCertificateEntry(bu.FREEBXML_REGISTRY_CACERT_ALIAS, getCACertificate());
            
            Certificate[] certChain = new Certificate[2];
            certChain[0] = cert;
            certChain[1] = getCACertificate();
            validateChain(certChain);
            
            File repositoryItemFile = File.createTempFile(".eric-ca-resp", ".jks");
            repositoryItemFile.deleteOnExit();
            FileOutputStream fos = new java.io.FileOutputStream(repositoryItemFile);
            keyStore.store(fos, bu.FREEBXML_REGISTRY_KS_PASS_RESP.toCharArray());
            fos.flush();
            fos.close();                        

            DataHandler dh = new DataHandler(new FileDataSource(repositoryItemFile));
            RepositoryItemImpl riNew = new RepositoryItemImpl(id, dh);
            
            ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
            ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
            
            HashMap<String, Object> respIdToRepositoryItemMap = new HashMap<String, Object>();
            respIdToRepositoryItemMap.put(id, riNew);
            
            respHolder = new RegistryResponseHolder(ebRegistryResponseType, respIdToRepositoryItemMap);
            
        } catch (RegistryException e) {
            context.rollback();
            throw e;
        } catch (Exception e) {
            context.rollback();
            throw new RegistryException(e);
        }

        context.commit();        
        return respHolder;
    }    
    
    /**
     * Signed specified cert using the private key of RegistryOperator.
     * Warning this uses Sun's JDK impl specific classes and will not work
     * with other JDK impls.
     *
     */
    @SuppressWarnings("static-access")
	X509Certificate signCertificate(X509Certificate inCert) throws RegistryException {
        X509CertImpl signedCert = null;
        
        try {
            X509CertImpl caCert = (X509CertImpl)getCACertificate();
            X509CertInfo caCertInfo = new X509CertInfo(caCert.getTBSCertificate());            
            X509CertInfo inCertInfo = new X509CertInfo(inCert.getTBSCertificate());

			// Use catch (certs subject name as signed cert's issuer name
			CertificateSubjectName caCertSubjectName = (CertificateSubjectName) caCertInfo.get(X509CertInfo.SUBJECT);
			CertificateIssuerName signedCertIssuerName = new CertificateIssuerName(
					(X500Name) caCertSubjectName.get(CertificateSubjectName.DN_NAME));
			
			inCertInfo.set(X509CertInfo.ISSUER, signedCertIssuerName);
			signedCert = new X509CertImpl(inCertInfo);
            
            //TODO: Need to remove hardcoding below and instead somehow use info.algId => algName
//            signedCert.sign(ac.getPrivateKey(ac.ALIAS_REGISTRY_OPERATOR, ac.ALIAS_REGISTRY_OPERATOR), "MD5WithRSA"); // JDK6
//            signedCert.sign(ac.getPrivateKey(ac.ALIAS_REGISTRY_OPERATOR, ac.ALIAS_REGISTRY_OPERATOR), "SHA256withRSA"); // JDK7
			
			// removed hardcoding
			signedCert.sign(
					ac.getPrivateKey(ac.ALIAS_REGISTRY_OPERATOR, ac.ALIAS_REGISTRY_OPERATOR), 
					inCert.getSigAlgName());
            
        } catch (java.security.GeneralSecurityException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ErrorSigningRegIssuedCert"), e);
        } catch (java.io.IOException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.ErrorSigningRegIssuedCert"), e);
        }
        
        return signedCert;
    }
    
    private boolean validateChain(Certificate[] certChain)
    {
        for (int i = 0; i < certChain.length-1; i++) {
            X500Principal issuerDN =
                ((X509Certificate)certChain[i]).getIssuerX500Principal();
            X500Principal subjectDN =
                ((X509Certificate)certChain[i+1]).getSubjectX500Principal();
            if (!(issuerDN.equals(subjectDN)))
                return false;
        }
        return true;
    }                                                                                   
        
    /** 
      * Generate a registry issued certificate signed by private key of RegistryOperator.
      */
    public X509Certificate generateRegistryIssuedCertificate(String dname) throws RegistryException {
        X509Certificate cert = null;
        
        File ksFile = null;
        try {
            String keystoreFileName = System.getProperty("java.io.tmpdir") + "/eric-temp-ks.jks";
            String keystoreType = "JKS";
            String alias = "ebxmlrr";
            String storePassStr = "ebxmlrr";
            String keyPassStr = "ebxmlrr";
            String keyAlg = "RSA"; //XWSS does not support DSA which is default is KeyTool. Hmm. Weird.

            String[] args = {
                "-genkey", "-keyAlg", keyAlg, "-alias", alias, "-keypass", keyPassStr,
                "-keystore", keystoreFileName, "-storepass", storePassStr,
                "-storetype", keystoreType, "-dname", dname
            };

            KeyTool keytool = new KeyTool();
            keytool.run(args, System.out);
                        
            ksFile = new File(keystoreFileName);
            
            //Now load the KeyStore and get the cert
            FileInputStream fis = new java.io.FileInputStream(ksFile);
            
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(fis, storePassStr.toCharArray());
            
            cert = (X509Certificate)keyStore.getCertificate(alias);
            cert = signCertificate(cert);
        
        } catch (Exception e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.CertGenError"), e);
        } finally {
            if (ksFile != null) {
                try {
                    ksFile.delete();
                } catch (Exception e) {
                    ksFile = null;
                }
            }
        }
        
        
        return cert;
    }
    
    /**
     * Gets the DN for specified User object.
     *
     * @param user DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws JAXRException DOCUMENT ME!
     */
    @SuppressWarnings("unused")
	private static String getDNameFromUser(UserType user) throws RegistryException {
        String dname = "CN=";

        List<PostalAddressType> addresses = user.getAddress();
		PersonNameType ebPersonNameType = user.getPersonName();

		//CN=Farrukh Najmi, OU=freebxml.org, O=ebxmlrr, L=Islamabad, ST=Punjab, C=PK
		if (ebPersonNameType == null) {
		    ebPersonNameType = bu.rimFac.createPersonNameType();
		    ebPersonNameType.setFirstName(user.getId());
		}

		PostalAddressType ebPostalAddressType = null;
		if ((addresses != null) && (addresses.size() > 0)) {
		    ebPostalAddressType = (addresses.iterator().next());
		} else {
		    ebPostalAddressType = bu.rimFac.createPostalAddressType();
		}

		String city = ebPostalAddressType.getCity();

		if ((city == null) || (city.length() == 0)) {
		    city = "Unknown";
		}

		String state = ebPostalAddressType.getStateOrProvince();

		if ((state == null) || (state.length() == 0)) {
		    state = "Unknown";
		}

		String country = ebPostalAddressType.getCountry();

		if ((country == null) || (country.length() == 0)) {
		    country = "Unknown";
		}

		if (country.length() > 0) {
		    country = country.substring(0, 2);
		}

		dname += (ebPersonNameType.getFirstName() + " " + ebPersonNameType.getMiddleName() +
		" " + ebPersonNameType.getLastName() + ", OU=Unknown, O=Unknown, L=" + city +
		", ST=" + state + ", C=" + country);

        return dname;
    }
    
}

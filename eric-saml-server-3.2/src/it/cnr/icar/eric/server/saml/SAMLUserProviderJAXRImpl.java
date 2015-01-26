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
package it.cnr.icar.eric.server.saml;

import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CommonProperties;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.exceptions.UserRegistrationException;
import it.cnr.icar.eric.server.common.ConnectionManager;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.HashSet;

import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.RegistryService;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.NameID;

public class SAMLUserProviderJAXRImpl implements SAMLUserProvider {

	private static final String SAML2_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent";
	private static String USER_REGISTRY_HOME = CommonProperties.getInstance().getProperty(
			"eric.common.userregistry.home");

	public SAMLUserProviderJAXRImpl() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.cnr.icar.eric.server.saml.SAMLUserProvider#getUserFromAssertion(org
	 * .opensaml.saml2.core.Assertion)
	 */
	@Override
	public UserType getUserFromAssertion(Assertion assertion) {

		UserType ebUserType = null;
		try {
			String userId = getUserIdFromAssertion(assertion);

			// evaluate user id as a valid UUID
			if (!(it.cnr.icar.eric.common.Utility.getInstance().isValidRegistryId(userId))) {
				throw new UserRegistrationException(ServerResourceBundle.getInstance().getString(
						"message.userRegistrationFailedUUID"));
			}

			ebUserType = getRemoteUserUsingJAXR(userId);
		} catch (RegistryException e) {
			e.printStackTrace();
		}
		
		return ebUserType;
	}

	/*
	 * Compliant to OASIS ebXML RegRep 3.0 (ebRS) ebRS SAML interface
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.cnr.icar.eric.server.saml.SAMLUserProvider#getUserIdFromAssertion
	 * (org.opensaml.saml2.core.Assertion)
	 */
	@Override
	public String getUserIdFromAssertion(Assertion assertion) {

		NameID nameId = assertion.getSubject().getNameID();
		if (nameId.getFormat().equals(SAML2_NAME_FORMAT)) {
			return nameId.getValue();
		}

		return null;
	}

	// This method acquires the user instance that matches the provided user
	// identifier
	// from the remote OASIS ebXML User Registry. To retrieve the remote user
	// instance
	// the (standard) JAXR interface is used.

	private UserType getRemoteUserUsingJAXR(String userId) throws RegistryException {

		UserType ebUserType = null;
		try {

			if (USER_REGISTRY_HOME.equals("")) {

				ObjectFactory rimFac = BindingUtility.getInstance().rimFac;

				ebUserType = rimFac.createUserType();
				ebUserType.setLid(userId);
				ebUserType.setId(userId);

				// create PersonNameType
				PersonNameType ebPersonNameType = rimFac.createPersonNameType();
				ebPersonNameType.setLastName("Generic SAML based user");
				ebUserType.setPersonName(ebPersonNameType);
				
			} else {

				// connection to remote user registry via SOAP as
				// RegistryOperator (with credentials -> message security)

				Connection connection = ConnectionManager.getInstance().getConnection(USER_REGISTRY_HOME);
				RegistryService service = connection.getRegistryService();

				connection.setCredentials(getX500PrivateCredentialsForRegistryOperator());

				// retrieve user instance through registry-registry
				// communication (query)
				DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl) service.getDeclarativeQueryManager();
				RegistryObjectImpl ro = (RegistryObjectImpl) dqm.getRegistryObject(userId);

				if (ro != null) {
					/*
					 * Situation that user is in LDAP and synchronized to
					 * UserRegistry
					 */
					ebUserType = (UserType) ro.toBindingObject();
					// we have to make sure that the home attribute is set
					ebUserType.setHome(USER_REGISTRY_HOME);

				} else {
					/*
					 * Situation that user is in LDAP but not yet synchronized
					 * to UserRegistry
					 */
					String msg = ServerResourceBundle.getInstance().getString("message.error.RemoteObjectNotFound",
							new Object[] { USER_REGISTRY_HOME, userId });
					throw new RegistryException(msg);
				}
			}

		} catch (JAXRException e) {
			String msg = ServerResourceBundle.getInstance().getString("message.error.ErrorGettingRemoteObject",
					new Object[] { USER_REGISTRY_HOME, userId });
			throw new RegistryException(msg, e);
		}

		return ebUserType;

	}

	private HashSet<X500PrivateCredential> getX500PrivateCredentialsForRegistryOperator() throws RegistryException {
		AuthenticationServiceImpl auService = AuthenticationServiceImpl.getInstance();
		PrivateKey privateKey = auService.getPrivateKey(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR,
				AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
		java.security.cert.Certificate[] certs = auService
				.getCertificateChain(AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);

		CredentialInfo credentialInfo = new CredentialInfo(null, (X509Certificate) certs[0], certs, privateKey, null);

		HashSet<X500PrivateCredential> credentials = new HashSet<X500PrivateCredential>();
		credentials
				.add(new X500PrivateCredential(credentialInfo.cert, credentialInfo.privateKey, credentialInfo.alias));
		return credentials;
	}

}

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
import it.cnr.icar.eric.common.exceptions.UnregisteredUserException;
import it.cnr.icar.eric.common.exceptions.UserRegistrationException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Registers new users with the registry. Registration involves saving the
 * public key certificate for the user in server KeyStore and storing their User
 * object In registry.
 * 
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class UserRegistrar {
	public static final String ASSOC_TYPE_HAS_CERTIFICATE = "ebxmlrr_HasCertificate";

	/** The log */
	private static final Log log = LogFactory.getLog(UserRegistrar.class.getName());

	/**
	 * @link
	 * @shapeType PatternLink
	 * @pattern Singleton
	 * @supplierRole Singleton factory
	 */

	/* # private UserRegistrar _authenticationServiceImpl; */
	private static UserRegistrar instance = null;

	protected UserRegistrar() {
	}

	/**
	 * It will try to register the user if the certificate in a signed
	 * SubmitObjectsRequest is not yet in the keystore. The SubmitObjectsRequest
	 * must contain a single User object and its id must be a valid UUID and
	 * equal to the alias parameter, which should be extracted from the KeyInfo
	 * of XML signature element.
	 * 
	 * @return the User object of the newly registered user
	 * @throws UserRegistrationException
	 *             if SubmitObjectsRequest has more than one User object, or its
	 *             alias is not equal to the id of the unique User object, or
	 *             the id is not a valid UUID.
	 */
	public UserType registerUser(X509Certificate cert, SubmitObjectsRequest ebSubmitObjectsRequest)
			throws RegistryException {

		UserType ebUserType = null;

		try {
			AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();

			// Get all User objects
			RegistryObjectListType ebRegistryObjectListType = ebSubmitObjectsRequest.getRegistryObjectList();
			List<IdentifiableType> identifiableTypeList = BindingUtility.getInstance().getRegistryObjectTypeFilteredList(ebRegistryObjectListType);

			List<UserType> users = new ArrayList<UserType>();
			Iterator<IdentifiableType> objIter = identifiableTypeList.iterator();

			while (objIter.hasNext()) {
				RegistryObjectType ebRegistryObjectType = (RegistryObjectType) objIter.next();

				if (ebRegistryObjectType instanceof UserType) {
					UserType _user = (UserType) ebRegistryObjectType;

					// check to see if a user ACL file exists, and
					// if it does, check to see if the user is in
					// the list
					boolean isInACLFile = isUserInACLFile(_user);

					if (isInACLFile) {
						log.info(ServerResourceBundle.getInstance().getString(
								"message.isAuthorized",
								new Object[] { _user.getPersonName().getFirstName(),
										_user.getPersonName().getLastName() }));
					} else {
						String message = ServerResourceBundle.getInstance().getString(
								"message.isNotAuthorized",
								new Object[] { _user.getPersonName().getFirstName(),
										_user.getPersonName().getLastName() });
						log.warn(message);
						throw new UserRegistrationException(message);
					}

					@SuppressWarnings("unused")
					String userId = _user.getId();
					users.add(_user);
				}
			}

			if (users.size() == 0) {
				// This Exception seems to be misleading. Should we throw
				// UserRegistrationException with message saying no user was
				// found, instead?
				// Then again I doubt that this can ever happen.
				throw new UnregisteredUserException(cert);
			}

			if (!(users.size() == 1)) {
				throw new UserRegistrationException(ServerResourceBundle.getInstance().getString(
						"message.userRegistrationFailedOneUser"));
			}

			ebUserType = users.get(0);

			String userId = ebUserType.getId();

			// System.err.println("UserId: " + userId);
			if (!(it.cnr.icar.eric.common.Utility.getInstance().isValidRegistryId(userId))) {
				throw new UserRegistrationException(ServerResourceBundle.getInstance().getString(
						"message.userRegistrationFailedUUID"));
			}

			if (log.isInfoEnabled()) {
				log.info(ServerResourceBundle.getInstance().getString("message.registeringNewUser",
						new Object[] { userId }));
			}

			ac.registerUserCertificate(userId, cert);

			if (log.isInfoEnabled()) {
				log.info(ServerResourceBundle.getInstance()
						.getString("message.userRegistered", new Object[] { userId }));
			}

		} catch (JAXRException e) {
			throw new RegistryException(e);
		}

		return ebUserType;
	}

	/*
	 * This method is used to determine if a user is allowed to self-register.
	 * If the ebxmlrr.security.selfRegistration.acl property does not exist or
	 * the value is an empty string, anyone can self-register. In this case, the
	 * method returns 'true'. If this property exists, it contains a
	 * comma-delimited list of users that are authorized to self-register. For
	 * example: ebxmlrr.security.selfRegistration.acl=Jane Doe, Srinivas Patel
	 * The list is parsed into tokens (e.g., "Jane Doe") This method will check
	 * to see if both firstName and lastName from the User object appear in one
	 * of the tokens. The firstName must also appear in the token before the
	 * lastName. If it does appear, this method returns 'true'. Otherwise
	 * 'false'.
	 */
	private boolean isUserInACLFile(UserType user) throws IllegalArgumentException {
		boolean isInACLFile = false;

		if (user == null) {
			throw new IllegalArgumentException(ServerResourceBundle.getInstance().getString("message.nilUserReference"));
		}

		RegistryProperties rp = RegistryProperties.getInstance();

		// The reason we reload the properties is that Registry Admins
		// will be updating the ACL list more often than the ebxmlrr is
		// recycled. Reloading the properties makes the latest edits
		// available to this class.
		rp.reloadProperties();

		String aclList = rp.getProperty("eric.security.selfRegistration.acl");

		// If property does not exist, or the property value is "", allow
		// all self-registrations. This is the default setting
		if ((aclList == null) || (aclList.length() == 0)) {
			return true;
		}

		org.oasis.ebxml.registry.bindings.rim.PersonNameType pName = user.getPersonName();
		String firstName = pName.getFirstName();
		String lastName = pName.getLastName();
		java.util.StringTokenizer st = new java.util.StringTokenizer(aclList, ",");

		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int firstNameIndex = token.indexOf(firstName);
			int lastNameIndex = token.indexOf(lastName);

			if ((firstNameIndex != -1) && (lastNameIndex != -1) && (firstNameIndex < lastNameIndex)) {
				isInACLFile = true;

				break;
			}
		}

		return isInACLFile;
	}

	public static void main(String[] args) throws Exception {
		@SuppressWarnings("unused")
		UserRegistrar service = UserRegistrar.getInstance();
	}

	public synchronized static UserRegistrar getInstance() {
		if (instance == null) {
			instance = new UserRegistrar();
		}

		return instance;
	}
}

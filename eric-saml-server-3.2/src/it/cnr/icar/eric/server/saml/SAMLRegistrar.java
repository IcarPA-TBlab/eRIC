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

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.opensaml.saml2.core.Assertion;

/**
 * This SAMLSubjectRegistrar covers all aspects of registration of a certain 
 * SAML Subject associated with a SAML Assertion provided by the current request.
 * 
 * A user referenced in a SAML assertion is registered through the following 
 * mechanism: (a) retrieve User ID from SAML NameId, (b) replicate the corresponding
 * user instance in the central User Registry to the local one.
 */

public class SAMLRegistrar {

    private static SAMLRegistrar instance = null;
  
    protected SAMLRegistrar() {}

    public synchronized static SAMLRegistrar getInstance() {
        if (instance == null) {
            instance = new it.cnr.icar.eric.server.saml.SAMLRegistrar();
        }
        return instance;
    }

    // this method determines the user id from a SAML assertion and create 
    // a new local user instance; to this end the remote user registry is
    // queried to retrieve the respective user instance
    
    public UserType registerUser(Assertion assertion) {
    	
    	UserType ebUserType = null;
    	
        try {
        	
        	/*
        	 * get user from UserRegistry 
        	 */
            ebUserType = new SAMLUserProviderJAXRImpl().getUserFromAssertion(assertion);
            
            if (ebUserType != null) {
            	
            	// create a local replication of the remote user; the respective
            	// context refers to the registry operator
            	ServerRequestContext context = null;
                context = new ServerRequestContext("SAMLRegistrar.registerUser", null);
                
                // impersonate as new user to be your own owner!
                context.setUser(ebUserType);
            	
                List<IdentifiableType> userList = new ArrayList<IdentifiableType>();
                userList.add(ebUserType);

                PersistenceManager persistenceManager = PersistenceManagerFactory.getInstance().getPersistenceManager();
                persistenceManager.insert(context, userList);                            

                context.commit();
                return ebUserType;
                
            }
        } catch (JAXRException e) {
        	e.printStackTrace();
        }
        
        return ebUserType;
    }

    

    
    // This method retrieve a user from the respective user identifier; This method
    // is conceptually derived from the 'getUserFromAlias' method from the freebXML
    // AuthenticationServiceImpl.

    public UserType getUser(Assertion assertion) {
   	
    	UserType user = null;
    	ServerRequestContext context = null;
    	
    	// the unique identifier of the user instance referenced by the SAML subject
    	// is built from the value and qualifier name of the nameId attribute
    	String userId = new SAMLUserProviderJAXRImpl().getUserIdFromAssertion(assertion);   	
    	
        try {

            // this request is processed in the context of the registry operator
            context = new ServerRequestContext("SAMLRegistrar.getUserFromAssertion", null);

            // the authentication service is used to retrieve the registry operator
            AuthenticationServiceImpl authnService = AuthenticationServiceImpl.getInstance();                
            context.setUser(authnService.registryOperator);            
            
            // the user instances matching the provided user identifier are retrieved from the
            // database via the persistence manager (local, non-SOAP request)
			ResponseOptionType ebResponseOptionType = BindingUtility.getInstance().queryFac.createResponseOptionType();

            ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
            ebResponseOptionType.setReturnComposedObjects(true);

            String sqlQuery = "SELECT u.* FROM User_ u WHERE UPPER(u.id) ='" + userId.toUpperCase() + "'";

			List<IdentifiableType> ebUserTypeList = PersistenceManagerFactory
					.getInstance()
					.getPersistenceManager()
					.executeSQLQuery(context, sqlQuery, ebResponseOptionType, "User_",
							new ArrayList<Object>());

			if (ebUserTypeList.size() > 0) 
				user = (UserType) ebUserTypeList.get(0);
 
            // users that are retrieved from SAML assertions are NOT classified
            // as registry administrators; this implies, that this user MUST be
            // registered using another mechanism
            
            context.commit();
            context = null;
            
        } catch (RegistryException e) {        	
        	e.printStackTrace();
            
        } catch (Exception e) {
        	e.printStackTrace();
            
        } finally {
        	try {
        		if (null != context) 
        			context.rollback();

        	} catch (RegistryException e) {
        		e.printStackTrace();
        	}

        }
        
        return user;
    }
        
}

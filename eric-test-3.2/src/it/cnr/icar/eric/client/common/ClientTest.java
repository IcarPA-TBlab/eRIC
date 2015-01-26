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

package it.cnr.icar.eric.client.common;

import it.cnr.icar.eric.client.xml.registry.BusinessLifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PersonImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.client.xml.registry.util.SecurityUtil;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.ericTest;
import it.cnr.icar.eric.server.common.RegistryProperties;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;




/**
 * @author Farrukh Najmi
 *
 * Common base class for client-side tests.
 * Client-side tests should extend this class.
 * Any code commonly useful to any client-side test
 * should be added to this class.
 *
 */
public abstract class ClientTest extends ericTest {
    protected SecurityUtil securityUtil = SecurityUtil.getInstance();
    protected RegistryService service;
    protected BusinessQueryManagerImpl bqm;
    protected BusinessLifeCycleManagerImpl lcm;
    protected DeclarativeQueryManagerImpl dqm;
    protected Connection connection;

    protected final String regSoapUrl = "http://localhost:8080/eric/registry/soap";
	/*ProviderProperties.getInstance().
	getProperty("jaxr-ebxml.soap.url",
		    "http://localhost:8080/eric/registry/soap");*/
    protected final String regHttpUrl = "http://localhost:8080/eric/registry/http";
	/*ProviderProperties.getInstance().
	getProperty("jaxr-ebxml.http.url",
		    regSoapUrl.replaceFirst("/soap","/http"));*/
    protected static final boolean localCallMode = false;
	/*Boolean.valueOf(ProviderProperties.getInstance().
		getProperty("it.cnr.icar.eric.client.xml.registry.localCall",
			    "false")).booleanValue();*/

    /**
     * Map from object identifiers to object types, listing all objects
     * tearDown() should delete.  We do not store the (in memory) objects
     * themselves since most are otherwise quite short-lived.  Put
     * information into deleteIdToTypeMap in the last (or only) individual
     * TestCase (method in a TestCase class) using the identified object.
     * tearDown() runs after each TestCase.
     */
    protected HashMap<String, String> deleteIdToTypeMap = new HashMap<String, String>();

    /** Creates a new instance of ClientTest */
    public ClientTest(String name) {
        this(name, true);        
    }
    
    /** Creates a new instance of ClientTest */
    public ClientTest(String name, boolean createDefaultConnection) {
        super(name);
        
        try {
            if (createDefaultConnection) {
                ConnectionFactory connFactory = JAXRUtility.getConnectionFactory();

                Properties  props = new Properties();
                props.put("javax.xml.registry.queryManagerURL", regSoapUrl);
                connFactory.setProperties(props);
                connection = connFactory.createConnection();

                // Set credentials
                String alias = getTestUserAlias();
                String keypass = getTestUserKeypass();
                HashSet<X500PrivateCredential> creds = new HashSet<X500PrivateCredential>();
                creds.add(securityUtil.aliasToX500PrivateCredential(alias, keypass));
                connection.setCredentials(creds);

                //  creds.add(secUtil.aliasToX500PrivateCredential(alias));
                //  connection.setCredentials(creds);

                service = connection.getRegistryService();
                bqm = (BusinessQueryManagerImpl)service.getBusinessQueryManager();
                lcm = (BusinessLifeCycleManagerImpl)service.getBusinessLifeCycleManager();
                dqm = (DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
            }
	} catch (RuntimeException re) {
		
	    throw re;
        } catch (Exception e) {
            throw new UndeclaredThrowableException(e);
        }
        
    }

    /**
     * Get rid of all objects allocated during this TestCase run.  This
     * method runs after each TestCase (method in a TestCase class)
     * completes.
     */
    public void tearDown() throws Exception {
	super.tearDown();

	Iterator<Entry<String, String>> iter = deleteIdToTypeMap.entrySet().iterator();
	while (iter.hasNext()) {
	    @SuppressWarnings("rawtypes")
		Map.Entry entry = iter.next();
	    deleteIfExist((String)entry.getKey(), (String)entry.getValue());
	}
    }

    /** Gets the Connection for specified pre-defined user in server keystore */
    protected Connection getConnection(String alias) throws Exception {
        return getConnection(alias, null, null);
    }
    
    /** Gets the Connection for specified pre-defined user in server keystore */
    protected Connection getConnection(String alias, String regUrl, String keystorePath) throws Exception {
        if (keystorePath == null) {
            keystorePath = RegistryProperties.getInstance().getProperty("eric.security.keystoreFile");
        }
        String storepass = RegistryProperties.getInstance().getProperty("eric.security.keystorePassword");
        
        if (regUrl == null) {
	    // If not provided, use default location for SOAP interface.
            regUrl = regSoapUrl;
        }
        
        System.setProperty("javax.xml.registry.ConnectionFactoryClass",
        "it.cnr.icar.eric.client.xml.registry.ConnectionFactoryImpl");
        ConnectionFactory connFactory = ConnectionFactory.newInstance();
        Properties props = new Properties();
        props.put("javax.xml.registry.queryManagerURL", regUrl);
        props.put("javax.xml.registry.lifeCycleManagerURL", regUrl);
        connFactory.setProperties(props);
        
        // create connection for specified alias
        Connection connection = connFactory.createConnection();
        Set<X500PrivateCredential> credentials = getCredentialsFromKeystore(keystorePath, storepass, alias, alias);
        connection.setCredentials(credentials);
        
        return connection;
    }
    
    public String getTestUserAlias() {
	return ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.test.alias", "mykey");
    }

    public String getTestUserKeypass() {
	return ProviderProperties.getInstance().getProperty("jaxr-ebxml.security.test.keypass", "mypass");
    }

    /**
     * Getter for property lcm.
     * @return Value of property lcm.
     */
    public javax.xml.registry.BusinessLifeCycleManager getLCM() {
        return lcm;
    }
    
    /**
     * Getter for property bqm.
     * @return Value of property bqm.
     */
    public javax.xml.registry.BusinessQueryManager getBQM() {
        return bqm;
    }
    
    /**
     * Getter for property dqm.
     * @return Value of property dqm.
     */
    public javax.xml.registry.DeclarativeQueryManager getDQM() {
        return dqm;
    }
    
    private Set<X500PrivateCredential> getCredentialsFromKeystore(String keystorePath, String storepass, String alias, String keypass) throws Exception {
        HashSet<X500PrivateCredential> credentials = new HashSet<X500PrivateCredential>();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new BufferedInputStream(new FileInputStream(keystorePath)), storepass.toCharArray());
        X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey)keyStore.getKey(alias, keypass.toCharArray());
        credentials.add(new X500PrivateCredential(cert, privateKey, alias));
        return credentials;
    }

    protected void assertResponseSuccess(BulkResponse br)
    throws JAXRException {
        assertResponseSuccess(null, br);
    }

    protected void assertResponseSuccess(String message, BulkResponse br)
    throws JAXRException {
        if (br.getExceptions() != null && !br.getExceptions().isEmpty()) {
            ((Exception)br.getExceptions().iterator().next()).printStackTrace();
        }
        if (message != null) {
            assertTrue(message, br.getStatus() == BulkResponse.STATUS_SUCCESS);
        } else {
            assertTrue(br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }
    }

    protected void assertResponseFailure(BulkResponse br)
    throws JAXRException {
        assertResponseFailure(null, br);
    }
    
    protected void assertResponseFailure(String message, BulkResponse br)
    throws JAXRException {
        if (br.getExceptions() != null && !br.getExceptions().isEmpty()) {
            ((Exception)br.getExceptions().iterator().next()).printStackTrace();
        }
        if (message != null) {
            assertTrue(message, br.getStatus() == BulkResponse.STATUS_FAILURE);
        } else {
            assertTrue(br.getStatus() == BulkResponse.STATUS_FAILURE);
        }
    }
    
    @SuppressWarnings("static-access")
	protected User createUser(String userName) throws Exception {
        PersonName personName = lcm.createPersonName(userName, userName, userName);
        PostalAddress addr = lcm.createPostalAddress("streetNumber",
                                         "street",
                                         "city",
                                         "stateOrProvince",
                                         "country",
                                         "postalCode",
                                         "type");
        TelephoneNumber tel = lcm.createTelephoneNumber();
        ArrayList<TelephoneNumber> tels = new ArrayList<TelephoneNumber>();
        tels.add(tel);

        User user = lcm.createUser();        
        ArrayList<PostalAddress> addrs = new ArrayList<PostalAddress>();
        addrs.add(addr);

        user.setPersonName(personName);
        user.setPostalAddresses(addrs);
        user.setTelephoneNumbers(tels);

	// Not perfect since id may change after this method returns...
	deleteIdToTypeMap.put(user.getKey().getId(), lcm.USER);

        return user;
    }
    
    @SuppressWarnings("static-access")
	protected PersonImpl createPerson(String userName) throws Exception {
        PersonName personName = lcm.createPersonName(userName, userName, userName);
        PostalAddress addr = lcm.createPostalAddress("streetNumber",
                                         "street",
                                         "city",
                                         "stateOrProvince",
                                         "country",
                                         "postalCode",
                                         "type");
        TelephoneNumber tel = lcm.createTelephoneNumber();
        ArrayList<TelephoneNumber> tels = new ArrayList<TelephoneNumber>();
        tels.add(tel);

        PersonImpl user = new PersonImpl(lcm);        
        ArrayList<PostalAddress> addrs = new ArrayList<PostalAddress>();
        addrs.add(addr);

        user.setPersonName(personName);
        user.setPostalAddresses(addrs);
        user.setTelephoneNumbers(tels);

	// Not perfect since id may change after this method returns...
	deleteIdToTypeMap.put(user.getKey().getId(), lcm.USER);

        return user;
    }
    
    @SuppressWarnings("static-access")
	protected Organization createOrganization(String orgName) throws Exception {
        PostalAddress addr = lcm.createPostalAddress("streetNumber",
                                         "street",
                                         "city",
                                         "stateOrProvince",
                                         "country",
                                         "postalCode",
                                         "type");
        TelephoneNumber tel = lcm.createTelephoneNumber();
        ArrayList<TelephoneNumber> tels = new ArrayList<TelephoneNumber>();
        tels.add(tel);
        
        Organization org = lcm.createOrganization(orgName);        
        org.setPostalAddress(addr);
        org.setTelephoneNumbers(tels);        

	// Not perfect since id may change after this method returns...
	deleteIdToTypeMap.put(org.getKey().getId(), lcm.ORGANIZATION);

        return org;
    }
    
    @SuppressWarnings("static-access")
	protected ExternalIdentifier createExternalIdentifier(String schemeId, String value) throws Exception {
        ClassificationScheme scheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, LifeCycleManager.CLASSIFICATION_SCHEME);
        ExternalIdentifier extId = lcm.createExternalIdentifier(scheme, value, value);

	// Not perfect since id may change after this method returns...
	deleteIdToTypeMap.put(extId.getKey().getId(), lcm.EXTERNAL_IDENTIFIER);

        return extId;
    }

    protected void deleteIfExist(String id) throws JAXRException {
        deleteIfExist(id, null);
    }
    
    protected void deleteIfExist(String id, String type) throws JAXRException {
        deleteIfExist(id, type, forceRemoveRequestSlotsMap);
    }
    
    protected void deleteIfExist(String id, String type, HashMap<?, ?> removeRequestSlots) throws JAXRException {
        deleteIfExist(dqm, lcm, id, type, removeRequestSlots);
    }
    
    /**
     * Allows passing lcm and qm.
     */
    @SuppressWarnings("unchecked")
	protected void deleteIfExist(
            DeclarativeQueryManagerImpl dqm,
            BusinessLifeCycleManagerImpl lcm,
            String id, String type, HashMap<?, ?> removeRequestSlots) throws JAXRException {
        
        if (type == null) {
            type = "RegistryObject";
        }
        if (dqm.getRegistryObject(id, type) != null) {
            lcm.deleteObjects(Collections.singletonList(lcm.createKey(id)),
			      null,
			      (HashMap<String, String>) removeRequestSlots,
			      null);
        }
    }
    
    /*
     * Executes specified stored query with specified parameters.
     *
     * @return the List of RegistryObjects that matched the query
     */
    @SuppressWarnings("static-access")
	protected Collection<?> executeQuery(String queryId, Map<String, String> queryParams) throws Exception {
        Collection<?> registryObjects = null;
        
        queryParams.put(BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID, queryId);
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL);
        
        BulkResponse bResponse = dqm.executeQuery(query, queryParams);
        registryObjects = bResponse.getCollection();        
        
        return registryObjects;
    }    
}

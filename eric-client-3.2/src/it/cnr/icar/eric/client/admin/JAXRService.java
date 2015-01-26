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
package it.cnr.icar.eric.client.admin;

import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;
import it.cnr.icar.eric.client.xml.registry.util.SecurityUtil;
import it.cnr.icar.eric.common.BindingUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;
import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */
public class JAXRService {
    static final AdminResourceBundle rb = AdminResourceBundle.getInstance();
    static final BindingUtility bu = BindingUtility.getInstance();
    static final String PROPERTY_ALIAS = "jaxr-ebxml.security.alias";
    static final String PROPERTY_HOME = "jaxr-ebxml.home";
    static final String PROPERTY_KEYPASS = "jaxr-ebxml.security.keypass";
    static final String PROPERTY_KEYSTORE = "jaxr-ebxml.security.keystore";
    static final String PROPERTY_SERVER_URL = "jaxr-ebxml.soap.url";
    static ConnectionFactory connFactory;

    static {
        try {
            connFactory = JAXRUtility.getConnectionFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    MimetypesFileTypeMap fileTypeMap;

    SecurityUtil securityUtil = SecurityUtil.getInstance();
    RegistryService service;
    BusinessQueryManager bqm;
    BusinessLifeCycleManager lcm;
    DeclarativeQueryManager dqm;
    Connection connection;
    String regUrl = "http://localhost:8080/" + ProviderProperties.getInstance().getProperty("eric.name") + "/registry/soap";
    String alias;
    boolean debug;
    String keyPass;
    boolean verbose;

    public JAXRService() {
        // Default registry URL
        regUrl = ProviderProperties.getInstance().getProperty(PROPERTY_SERVER_URL,
                regUrl);

        alias = ProviderProperties.getInstance().getProperty(PROPERTY_ALIAS);
    }

    public void connect() throws AdminException {
        try {
            if (debug) {
                System.err.println(rb.getString(
			    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			    "debug.home",
			    new Object[] {ProviderProperties.getInstance().
					  getProperty(PROPERTY_HOME)}));
                System.err.println(rb.getString(
			    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			    "debug.alias",
			    new Object[] {alias}));
                System.err.println(rb.getString(
			    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			    "debug.keyPass",
			    new Object[] {ProviderProperties.getInstance().
					  getProperty(PROPERTY_KEYPASS)}));
                System.err.println(rb.getString(
			    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			    "debug.keystore",
			    new Object[] {ProviderProperties.getInstance().
					  getProperty(PROPERTY_KEYSTORE)}));
            }

            Properties props = new Properties();
            props.put("javax.xml.registry.queryManagerURL", regUrl);
            connFactory.setProperties(props);
            connection = connFactory.createConnection();

            if (alias != null && !alias.equals("")) {
                // Set credentials
                HashSet<X500PrivateCredential> creds = new HashSet<X500PrivateCredential>();
                creds.add(securityUtil.aliasToX500PrivateCredential(alias));
                connection.setCredentials(creds);
            }

            service = connection.getRegistryService();
            bqm = service.getBusinessQueryManager();
            lcm = service.getBusinessLifeCycleManager();
            dqm = service.getDeclarativeQueryManager();
        } catch (Exception e) {
            throw new AdminException(e);
        }
    }

    /**
     * Closes a Connection when it is no longer needed
     */
    public void disconnect() throws Exception {
        connection.close();
        lcm = null;
        bqm = null;
        dqm = null;
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

    /**
     * Setter for the alias to use when getting user's certificate
     * from the keystore.
     *
     * @alias Alias for the user's certificate
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Setter for the debug level
     *
     * @debug Debugging output is enabled when true.
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Setter for the keypass to use when getting user's certificate
     * from the keystore.
     *
     * @newAlias Alias for the user's certificate
     */
    public void setKeyPass(String keyPass) {
        this.keyPass = keyPass;

        if (verbose || debug) {
            System.err.println(rb.getString(
                    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                    "reloadingForKeyPass"));
        }

        System.setProperty(PROPERTY_KEYPASS, keyPass);
        ProviderProperties.getInstance().reloadProperties();
    }

    public void setRegistry(String newRegistry) {
        regUrl = newRegistry;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public RegistryPackage getRegistryPackage(String locator)
        throws AdminException {
        return getRegistryPackage(locator, false);
    }

    @SuppressWarnings("static-access")
	public RegistryPackage getRegistryPackage(String locator, boolean create)
        throws AdminException {
        try {
            if (locator.matches("^" + bu.CANONICAL_ROOT_FOLDER_LOCATOR + "/?$")) {
                return getRegistryPackageByID(bu.CANONICAL_ROOT_FOLDER_ID);
            } else if (locator.equals("^" + bu.CANONICAL_USERDATA_FOLDER_LOCATOR + "/?$")) {
                return getRegistryPackageByID(bu.CANONICAL_USERDATA_FOLDER_ID);
            } else if (locator.equals("") || locator.equals("/")) {
                return null;
            }

            if (locator.charAt(0) != '/') {
                throw new AdminException(rb.getString(
                        AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                        "rootNotAbsolute"));
            }

            String[] segments = locator.split("/", -1);
            String cumulativeLocator = null;
            RegistryPackage rp = null;

            // Start at 'index = 1' because leading '/' means segments[0] == "".
            for (int index = 1; index < segments.length; index++) {
                String segment = segments[index];

                if (segment.equals("")) {
                    if (index == (segments.length - 1)) {
                        break;
                    } else {
                        throw new AdminException(rb.getString(
                                AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                                "nullSegment"));
                    }
                }

                String queryStr;

                if (cumulativeLocator == null) {
                    cumulativeLocator = "";

		    queryStr =
			"SELECT DISTINCT p.* from " +
			"RegistryPackage p, Name_ nm " +
			"WHERE" +
			"(nm.value = '" + segment + "' AND nm.parent=p.id) " +
			" AND " +
			"p.id NOT IN " +
			"(SELECT targetObject FROM Association WHERE " +
			"associationType='" + 
			bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
			"')";
                } else {
		    queryStr =
			"SELECT DISTINCT ro.* " +
			"FROM RegistryObject ro, RegistryPackage p, " +
			"Name_ nm, Association ass WHERE (nm.value = '" +
			segment +
			"' AND nm.parent = ro.id) AND (p.id = '" +
			rp.getKey().getId() +
			"') AND (ass.associationType='"
			+ bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
			"' AND ass.sourceObject = p.id AND " +
			"ass.targetObject = ro.id) ";
                }

                javax.xml.registry.Query query = dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                        queryStr);

                // make JAXR request
                javax.xml.registry.BulkResponse resp = dqm.executeQuery(query);

                JAXRUtility.checkBulkResponse(resp);

                Collection<?> coll = resp.getCollection();
                Iterator<?> iter = coll.iterator();

                if (debug) {
                    while (iter.hasNext()) {
                        rp = (RegistryPackage) iter.next();
                        System.err.println(rb.getString(
				    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				    "debug.registryPackage",
				    new Object[] {rp.getKey().getId(),
						  rp.getName()}));
                    }
                }

                if (coll.size() == 1) {
                    // The single root is the first and only iterator entry.
                    RegistryObject ro = (RegistryObject) coll.iterator().next();

                    if (ro instanceof RegistryPackage) {
                        rp = (RegistryPackage) ro;
                    } else {
                        Object[] formatArgs = { locator };
                        throw new AdminException(
				rb.getString(
					AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"notRegistryPackage",
					formatArgs));
                    }
                } else if (coll.size() == 0) {
                    if (create) {
                        if (debug) {
                            Object[] debugCreateArgs = { locator, segment };
			    // ??? This code has raised an exception since
			    // ??? rev. 1.1 but seems incorrect.  Should it
			    // ??? instead just output a message?
                            throw new AdminException(
				    rb.getString(
					    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					    "debug.create",
					    debugCreateArgs));
                        }

                        rp = createPathToRoot(rp, segments, index);

                        break;
                    } else {
                        Object[] noMatchingLocatorArgs = { locator };
                        throw new AdminException(
				rb.getString(
					AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"noMatchingLocator",
					noMatchingLocatorArgs));
                    }
                } else {
                    Object[] multipleMatchingLocatorArgs = { locator };
                    throw new AdminException(
			    rb.getString(
				    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				    "multipleMatchingLocator",
				    multipleMatchingLocatorArgs));
                }

                cumulativeLocator = cumulativeLocator + "/" + segment;
            }

            return rp;
        } catch (AdminException e) {
            throw e;
        } catch (Exception e) {
            throw new AdminException(e);
        }
    }

    public Collection<?> doSQLQuery(String queryStr) throws Exception {
        javax.xml.registry.Query query = dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                queryStr);

        // make JAXR request
        javax.xml.registry.BulkResponse response = dqm.executeQuery(query);

        JAXRUtility.checkBulkResponse(response);

        return response.getCollection();
    }

    public Collection<?> getSourceRegistryPackages(RegistryObject targetRO)
        throws Exception {
        if (targetRO == null) {
            throw new AdminException(rb.getString(
                    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                    "nullArgument"));
        }

        @SuppressWarnings("static-access")
		String queryStr = "SELECT DISTINCT ro.* " +
            "FROM RegistryObject ro, RegistryPackage p, " +
            "Association ass WHERE (ass.associationType='" +
            bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember +
            "' AND ass.targetObject = '" + targetRO.getKey().getId() +
            "' AND ass.sourceObject = p.id) AND (ro.id = p.id)";

        return doSQLQuery(queryStr);
    }

    RegistryPackage createPathToRoot(RegistryPackage rootRP, String[] segments,
        int startIndex) throws Exception {
        ArrayList<RegistryPackage> registryPackages = new ArrayList<RegistryPackage>();

        if (rootRP != null) {
            registryPackages.add(rootRP);
        }

        RegistryPackage parentRP = rootRP;
        RegistryPackage segmentRP = null;

        for (int index = startIndex; index < segments.length; index++) {
            String segment = segments[index];

            if (segment.equals("")) {
                if (index == (segments.length - 1)) {
                    break;
                } else {
                    throw new AdminException(rb.getString(
                            AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                            "nullSegment"));
                }
            }

            segmentRP = lcm.createRegistryPackage(segment);
            registryPackages.add(segmentRP);

            if (parentRP != null) {
                parentRP.addRegistryObject(segmentRP);
            }

            parentRP = segmentRP;
        }

        if (!registryPackages.isEmpty()) {
            BulkResponse response = lcm.saveObjects(registryPackages);

            JAXRUtility.checkBulkResponse(response);
        }

        return segmentRP;
    }

    public RegistryPackage getRegistryPackageByID(String uuid)
        throws AdminException {
        RegistryPackage rp = null;

        try {
            String queryStr = "SELECT rp.* from RegistryPackage rp " +
                "WHERE rp.id = '" + uuid + "'";

            javax.xml.registry.Query query = dqm.createQuery(javax.xml.registry.Query.QUERY_TYPE_SQL,
                    queryStr);

            // make JAXR request
            javax.xml.registry.BulkResponse resp = dqm.executeQuery(query);

            Collection<?> coll = resp.getCollection();
            Iterator<?> iter = coll.iterator();

            if (debug) {
                while (iter.hasNext()) {
                    rp = (RegistryPackage) iter.next();
                    System.err.println(rb.getString(
				    AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				    "debug.registryPackage",
				    new Object[] {rp.getKey().getId(),
						  rp.getName()}));
                }
            }

            if (coll.size() < 1) {
                Object[] noMatchingLocatorArgs = { uuid };
                throw new AdminException(
			rb.getString(
				AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				"noMatchingRegistryPackage",
				noMatchingLocatorArgs));
            }

            if (coll.size() > 1) {
                Object[] args = { uuid };
                throw new AdminException(
			rb.getString(
				AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				"multipleMatchingRegistryPackage",
				args));
            }

            // The single root is the first and only iterator entry.
            rp = (RegistryPackage) coll.iterator().next();

            return rp;
        } catch (AdminException e) {
            throw e;
        } catch (Exception e) {
            throw new AdminException(e);
        }
    }

    /**
     * Creates a RegistryPackage
     *
     * @param locator The relative URL to use as the value of the 'locator' slot.
     *
     * @return The created RegistryPackage.
     */
    public RegistryPackage createRegistryPackage(String locator)
        throws Exception {
        RegistryPackage rp = lcm.createRegistryPackage(locator);

        return rp;
    }

    /**
     * Creates an ExtrinsicObject for a file
     *
     * @param file The File representing the external resource
     *
     * @return The created ExtrinsicObject.
     */
    public ExtrinsicObject createExtrinsicObject(File file)
        throws Exception {
        javax.activation.FileDataSource fds = new javax.activation.FileDataSource(file);
        javax.activation.DataHandler dataHandler = new javax.activation.DataHandler(fds);

        ExtrinsicObject eo = lcm.createExtrinsicObject(dataHandler);

        if (fileTypeMap != null) {
            fds.setFileTypeMap(fileTypeMap);
        }

        eo.setMimeType(fds.getContentType());

        String eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        eo.getKey().setId(eoId);

        eo.setName(lcm.createInternationalString(file.getName()));

        return eo;
    }
}

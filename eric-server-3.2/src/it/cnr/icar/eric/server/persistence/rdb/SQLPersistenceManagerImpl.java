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
package it.cnr.icar.eric.server.persistence.rdb;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CommonResourceBundle;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.exceptions.ReferencesExistException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.PersonType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Class Declaration for SQLPersistenceManagerImpl.
 * 
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
public class SQLPersistenceManagerImpl implements it.cnr.icar.eric.server.persistence.PersistenceManager {
	/**
	 * @link
	 * @shapeType PatternLink
	 * @pattern Singleton
	 * @supplierRole Singleton factory
	 */

	/* # private SQLPersistenceManagerImpl _sqlPersistenceManagerImpl; */
	private static SQLPersistenceManagerImpl instance = null;
	private static final Log log = LogFactory.getLog(SQLPersistenceManagerImpl.class);
	private BindingUtility bu = BindingUtility.getInstance();
	private int numConnectionsOpen = 0;

	/**
	 * 
	 * @associates 
	 *             <{it.cnr.icar.eric.server.persistence.rdb.ExtrinsicObjectDAO}
	 *             >
	 */
	String databaseURL = null;
	java.sql.DatabaseMetaData metaData = null;
	private String driver;
	private String user;
	private String password;
	private boolean useConnectionPool;
	private boolean dumpStackOnQuery;
	private boolean skipReferenceCheckOnRemove;
	private ConnectionPool connectionPool;
	private int transactionIsolation;
	private DataSource ds = null;

	@SuppressWarnings("unused")
	private SQLPersistenceManagerImpl() {
		loadUsernamePassword();
		constructDatabaseURL();

		// define transaction isolation
		if ("TRANSACTION_READ_COMMITTED".equalsIgnoreCase(RegistryProperties.getInstance().getProperty(
				"eric.persistence.rdb.transactionIsolation"))) {
			transactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
		} else {
			transactionIsolation = Connection.TRANSACTION_READ_UNCOMMITTED;
		}

		useConnectionPool = Boolean.valueOf(
				RegistryProperties.getInstance().getProperty("eric.persistence.rdb.useConnectionPooling", "true"))
				.booleanValue();
		skipReferenceCheckOnRemove = Boolean.valueOf(
				RegistryProperties.getInstance()
						.getProperty("eric.persistence.rdb.skipReferenceCheckOnRemove", "false")).booleanValue();
		dumpStackOnQuery = Boolean.valueOf(
				RegistryProperties.getInstance().getProperty("eric.persistence.rdb.dumpStackOnQuery", "false"))
				.booleanValue();
		boolean debugConnectionPool = Boolean.valueOf(
				RegistryProperties.getInstance().getProperty("eric.persistence.rdb.pool.debug", "false"))
				.booleanValue();
		
		// Create JNDI context
		if (useConnectionPool) {
			if (!debugConnectionPool) {
				// Use Container's connection pooling
				String ericName = RegistryProperties.getInstance().getProperty("eric.name", "eric");
				String envName = "java:comp/env";
				String dataSourceName = "jdbc/" + ericName + "-registry";
				Context ctx = null;

				try {
					ctx = new InitialContext();
					if (null == ctx) {
						log.info(ServerResourceBundle.getInstance().getString("message.UnableToGetInitialContext"));
					}
				} catch (NamingException e) {
					log.info(ServerResourceBundle.getInstance().getString("message.UnableToGetInitialContext"), e);
					ctx = null;
				}

				if (null != ctx) {
					try {
						ctx = (Context) ctx.lookup(envName);

						if (null == ctx) {
							log.info(ServerResourceBundle.getInstance().getString(
									"message.UnableToGetJNDIContextForDataSource", new Object[] { envName }));
						}
					} catch (NamingException e) {
						log.info(
								ServerResourceBundle.getInstance().getString(
										"message.UnableToGetJNDIContextForDataSource", new Object[] { envName }), e);
						ctx = null;
					}
				}

				if (null != ctx) {
					try {
						ds = (DataSource) ctx.lookup(dataSourceName);
						
						if (null == ds) {
							log.info(ServerResourceBundle.getInstance().getString(
									"message.UnableToGetJNDIContextForDataSource",
									new Object[] { envName + "/" + dataSourceName }));
						}
					} catch (NamingException e) {
						log.info(
								ServerResourceBundle.getInstance().getString(
										"message.UnableToGetJNDIContextForDataSource",
										new Object[] { envName + "/" + dataSourceName }), e);
						ds = null;
					}
				}

				if (null != ds) {
					// Create a test connection to make sure all is well with
					// DataSource
					Connection connection = null;
					try {
						connection = ds.getConnection();
					} catch (Exception e) {
						log.info(
								ServerResourceBundle.getInstance().getString(
										"message.UnableToCreateTestConnectionForDataSource",
										new Object[] { envName + "/" + dataSourceName }), e);
						ds = null;
					} finally {
						if (connection != null) {
							try {
								connection.close();
							} catch (Exception e1) {
								// Do nothing.
								connection = null;
							}
						}
					}
				}
			}

			if (ds == null) {
				// No DataSource available so create our own ConnectionPool
				loadDatabaseDriver();
				createConnectionPool();
			}
		} else {
			loadDatabaseDriver();
		}
	}

	/** Look up the driver name and load the database driver */
	private void loadDatabaseDriver() {
		try {
			driver = RegistryProperties.getInstance().getProperty("eric.persistence.rdb.databaseDriver");
			Class.forName(driver);

			log.debug("Loaded jdbc driver: " + driver);
		} catch (ClassNotFoundException e) {
			log.error(e);
		}
	}

	/** Lookup up the db URL fragments and form the complete URL */
	private void constructDatabaseURL() {
		databaseURL = RegistryProperties.getInstance().getProperty("eric.persistence.rdb.databaseURL");

		log.info(ServerResourceBundle.getInstance().getString("message.dbURLEquals", new Object[] { databaseURL }));
	}

	/** Load the username and password for database access */
	private void loadUsernamePassword() {
		user = RegistryProperties.getInstance().getProperty("eric.persistence.rdb.databaseUser");
		password = RegistryProperties.getInstance().getProperty("eric.persistence.rdb.databaseUserPassword");
	}

	private void createConnectionPool() {
		try {
			RegistryProperties registryProperties = RegistryProperties.getInstance();
			String initialSize = registryProperties.getProperty("eric.persistence.rdb.pool.initialSize");
			int initConns = 1;

			if (initialSize != null) {
				initConns = Integer.parseInt(initialSize);
			}

			String maxSize = registryProperties.getProperty("eric.persistence.rdb.pool.maxSize");
			int maxConns = 1;

			if (maxSize != null) {
				maxConns = Integer.parseInt(maxSize);
			}

			String connectionTimeOut = registryProperties.getProperty("eric.persistence.rdb.pool.connectionTimeOut");
			int timeOut = 0;

			if (connectionTimeOut != null) {
				timeOut = Integer.parseInt(connectionTimeOut);
			}

			connectionPool = new ConnectionPool("ConnectionPool", databaseURL, user, password, maxConns, initConns,
					timeOut, transactionIsolation);
		} catch (java.lang.reflect.UndeclaredThrowableException t) {
			log.error(
					ServerResourceBundle.getInstance().getString("message.FailedToCreateConnectionPool",
							new Object[] { t.getClass().getName(), t.getMessage() }), t);
			throw t;
		}
	}

	/**
	 * Get a database connection. The connection is of autocommit off and with
	 * transaction isolation level "transaction read committed"
	 */
	public Connection getConnection(ServerRequestContext context) throws RegistryException {
		Connection connection = null;
		if (log.isTraceEnabled()) {
			log.debug("SQLPersistenceManagerImpl.getConnection");
			numConnectionsOpen++;
		}
		try {
			if (useConnectionPool) {
				if (ds != null) {
					connection = ds.getConnection();
					if (connection == null) {
						log.info(ServerResourceBundle.getInstance().getString(
								"message.ErrorUnableToOpenDbConnctionForDataSource=", new Object[] { ds }));
					}
				}

				if (connection == null) {
					// Default to registry server ConnectionPool
					connection = connectionPool.getConnection(context.getId());
				}
				connection.setTransactionIsolation(transactionIsolation);
				connection.setAutoCommit(false);
			} else {
				// create connection directly
				if ((user != null) && (user.length() > 0)) {
					connection = java.sql.DriverManager.getConnection(databaseURL, user, password);
				} else {
					connection = java.sql.DriverManager.getConnection(databaseURL);
				}
				// Set Transaction Isolation and AutoComit
				// WARNING: till present Oracle dirvers (9.2.0.5) do not accept
				// setTransactionIsolation being called after
				// setAutoCommit(false)
				connection.setTransactionIsolation(transactionIsolation);
				connection.setAutoCommit(false);
			}
		} catch (SQLException e) {
			throw new RegistryException(
					ServerResourceBundle.getInstance().getString("message.connectToDatabaseFailed"), e);
		}

		return connection;
	}

	public void releaseConnection(ServerRequestContext context, Connection connection) throws RegistryException {
		if (log.isTraceEnabled()) {
			log.debug("SQLPersistenceManagerImpl.releaseConnection");
			numConnectionsOpen--;
			log.debug("Number of connections open:" + numConnectionsOpen);
		}
		try {
			if (connection != null) {
				if (!connection.isClosed() && ((!useConnectionPool) || (ds != null))) {
					connection.close();
				} else if (useConnectionPool) {
					connectionPool.freeConnection(connection);
				}
			}
		} catch (Exception e) {
			throw new RegistryException(e);
		}
	}

	public synchronized static SQLPersistenceManagerImpl getInstance() {
		if (instance == null) {
			instance = new SQLPersistenceManagerImpl();
		}

		return instance;
	}

	// Sort objects by their type.
	private void sortRegistryObjects(
			List<IdentifiableType> registryObjects, 
			List<AssociationType1> associations,
			List<AuditableEventType> auditableEvents, 
			List<ClassificationType> classifications,
			List<ClassificationSchemeType> schemes, 
			List<ClassificationNodeType> classificationNodes,
			List<ExternalIdentifierType> externalIds, 
			List<ExternalLinkType> externalLinks, 
			List<ExtrinsicObjectType> extrinsicObjects,
			List<FederationType> federations, 
			List<ObjectRefType> objectRefs, 
			List<OrganizationType> organizations, 
			List<RegistryPackageType> packages,
			List<ServiceBindingType> serviceBindings, 
			List<ServiceType> services,
			List<SpecificationLinkType> specificationLinks, 
			List<AdhocQueryType> adhocQuerys,
			List<SubscriptionType> subscriptions, 
			List<UserType> users, 
			List<PersonType> persons, 
			List<RegistryType> registrys)
			throws RegistryException {
		associations.clear();
		auditableEvents.clear();
		classifications.clear();
		schemes.clear();
		classificationNodes.clear();
		externalIds.clear();
		externalLinks.clear();
		extrinsicObjects.clear();
		federations.clear();
		objectRefs.clear();
		organizations.clear();
		packages.clear();
		serviceBindings.clear();
		services.clear();
		specificationLinks.clear();
		adhocQuerys.clear();
		subscriptions.clear();
		users.clear();
		persons.clear();
		registrys.clear();

		Iterator<IdentifiableType> objIter = registryObjects.iterator();

		while (objIter.hasNext()) {
			// IdentifiableType obj = (IdentifiableType) objIter.next();
			Object obj = objIter.next();
			if (obj instanceof AssociationType1) {
				associations.add((AssociationType1) obj);
			} else if (obj instanceof AuditableEventType) {
				auditableEvents.add((AuditableEventType) obj);
			} else if (obj instanceof ClassificationType) {
				classifications.add((ClassificationType) obj);
			} else if (obj instanceof ClassificationSchemeType) {
				schemes.add((ClassificationSchemeType) obj);
			} else if (obj instanceof ClassificationNodeType) {
				classificationNodes.add((ClassificationNodeType) obj);
			} else if (obj instanceof ExternalIdentifierType) {
				externalIds.add((ExternalIdentifierType) obj);
			} else if (obj instanceof ExternalLinkType) {
				externalLinks.add((ExternalLinkType) obj);
			} else if (obj instanceof ExtrinsicObjectType) {
				extrinsicObjects.add((ExtrinsicObjectType) obj);
			} else if (obj instanceof FederationType) {
				federations.add((FederationType) obj);
			} else if (obj instanceof ObjectRefType) {
				objectRefs.add((ObjectRefType) obj);
			} else if (obj instanceof OrganizationType) {
				organizations.add((OrganizationType) obj);
			} else if (obj instanceof RegistryType) {
				registrys.add((RegistryType) obj);
			} else if (obj instanceof RegistryPackageType) {
				packages.add((RegistryPackageType) obj);
			} else if (obj instanceof ServiceBindingType) {
				serviceBindings.add((ServiceBindingType) obj);
			} else if (obj instanceof ServiceType) {
				services.add((ServiceType) obj);
			} else if (obj instanceof AdhocQueryType) {
				adhocQuerys.add((AdhocQueryType) obj);
			} else if (obj instanceof FilterQueryType) {
				adhocQuerys.add((AdhocQueryType) obj);
			} else if (obj instanceof SpecificationLinkType) {
				specificationLinks.add((SpecificationLinkType) obj);
			} else if (obj instanceof SubscriptionType) {
				subscriptions.add((SubscriptionType) obj);
			} else if (obj instanceof UserType) {
				users.add((UserType) obj);
			} else if (obj instanceof PersonType) {
				persons.add((PersonType) obj);
			} else if (obj instanceof RegistryType) {
				registrys.add((RegistryType) obj);
			} else {
				throw new RegistryException(CommonResourceBundle.getInstance().getString(
						"message.unexpectedObjectType",
						new Object[] { obj.getClass().getName(),
								"org.oasis.ebxml.registry.bindings.rim.IdentifiableType" }));
			}
		}
	}

	/**
	 * Does a bulk insert of a heterogeneous Collection of RegistrObjects.
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insert(ServerRequestContext context, List<IdentifiableType> registryObjects) throws RegistryException {

		List associations = new java.util.ArrayList();
		List auditableEvents = new java.util.ArrayList();
		List classifications = new java.util.ArrayList();
		List schemes = new java.util.ArrayList();
		List classificationNodes = new java.util.ArrayList();
		List externalIds = new java.util.ArrayList();
		List externalLinks = new java.util.ArrayList();
		List extrinsicObjects = new java.util.ArrayList();
		List federations = new java.util.ArrayList();
		List objectRefs = new java.util.ArrayList();
		List organizations = new java.util.ArrayList();
		List packages = new java.util.ArrayList();
		List serviceBindings = new java.util.ArrayList();
		List services = new java.util.ArrayList();
		List specificationLinks = new java.util.ArrayList();
		List adhocQuerys = new java.util.ArrayList();
		List subscriptions = new java.util.ArrayList();
		List users = new java.util.ArrayList();
		List persons = new java.util.ArrayList();
		List registrys = new java.util.ArrayList();

		sortRegistryObjects(registryObjects, associations, auditableEvents, classifications, schemes,
				classificationNodes, externalIds, externalLinks, extrinsicObjects, federations, objectRefs,
				organizations, packages, serviceBindings, services, specificationLinks, adhocQuerys, subscriptions,
				users, persons, registrys);

		if (auditableEvents.size() > 0) {
			AuditableEventDAO auditableEventDAO = new AuditableEventDAO(context);
			auditableEventDAO.insert(auditableEvents);
		}

		if (associations.size() > 0) {
			AssociationDAO associationDAO = new AssociationDAO(context);
			associationDAO.insert(associations);
		}

		if (classifications.size() > 0) {
			ClassificationDAO classificationDAO = new ClassificationDAO(context);
			classificationDAO.insert(classifications);
		}

		if (schemes.size() > 0) {
			ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO(context);
			classificationSchemeDAO.insert(schemes);
		}

		if (classificationNodes.size() > 0) {
			ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
			classificationNodeDAO.insert(classificationNodes);
		}

		if (externalIds.size() > 0) {
			ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
			externalIdentifierDAO.insert(externalIds);
		}

		if (externalLinks.size() > 0) {
			ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
			externalLinkDAO.insert(externalLinks);
		}

		if (extrinsicObjects.size() > 0) {
			ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
			extrinsicObjectDAO.insert(extrinsicObjects);
		}

		if (federations.size() > 0) {
			FederationDAO federationDAO = new FederationDAO(context);
			federationDAO.insert(federations);
		}

		if (registrys.size() > 0) {
			RegistryDAO registryDAO = new RegistryDAO(context);
			registryDAO.insert(registrys);
		}

		if (objectRefs.size() > 0) {
			ObjectRefDAO objectRefDAO = new ObjectRefDAO(context);
			objectRefDAO.insert(objectRefs);
		}

		if (organizations.size() > 0) {
			OrganizationDAO organizationDAO = new OrganizationDAO(context);
			organizationDAO.insert(organizations);
		}

		if (packages.size() > 0) {
			RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
			registryPackageDAO.insert(packages);
		}

		if (serviceBindings.size() > 0) {
			ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
			serviceBindingDAO.insert(serviceBindings);
		}

		if (services.size() > 0) {
			ServiceDAO serviceDAO = new ServiceDAO(context);
			serviceDAO.insert(services);
		}

		if (specificationLinks.size() > 0) {
			SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
			specificationLinkDAO.insert(specificationLinks);
		}

		if (adhocQuerys.size() > 0) {
			AdhocQueryDAO adhocQueryDAO = new AdhocQueryDAO(context);
			adhocQueryDAO.insert(adhocQuerys);
		}

		if (subscriptions.size() > 0) {
			SubscriptionDAO subscriptionDAO = new SubscriptionDAO(context);
			subscriptionDAO.insert(subscriptions);
		}

		if (users.size() > 0) {
			UserDAO userDAO = new UserDAO(context);
			userDAO.insert(users);
		}

		if (persons.size() > 0) {
			PersonDAO personDAO = new PersonDAO(context);
			personDAO.insert(persons);
		}

		if (registrys.size() > 0) {
			RegistryDAO registryDAO = new RegistryDAO(context);
			registryDAO.insert(registrys);
		}

	}

	/**
	 * Does a bulk update of a heterogeneous Collection of RegistrObjects.
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void update(ServerRequestContext context, List<IdentifiableType> registryObjects) throws RegistryException {

		List associations = new java.util.ArrayList();
		List auditableEvents = new java.util.ArrayList();
		List classifications = new java.util.ArrayList();
		List schemes = new java.util.ArrayList();
		List classificationNodes = new java.util.ArrayList();
		List externalIds = new java.util.ArrayList();
		List externalLinks = new java.util.ArrayList();
		List extrinsicObjects = new java.util.ArrayList();
		List federations = new java.util.ArrayList();
		List objectRefs = new java.util.ArrayList();
		List organizations = new java.util.ArrayList();
		List packages = new java.util.ArrayList();
		List serviceBindings = new java.util.ArrayList();
		List services = new java.util.ArrayList();
		List specificationLinks = new java.util.ArrayList();
		List adhocQuerys = new java.util.ArrayList();
		List subscriptions = new java.util.ArrayList();
		List users = new java.util.ArrayList();
		List persons = new java.util.ArrayList();
		List registrys = new java.util.ArrayList();

		sortRegistryObjects(registryObjects, associations, auditableEvents, classifications, schemes,
				classificationNodes, externalIds, externalLinks, extrinsicObjects, federations, objectRefs,
				organizations, packages, serviceBindings, services, specificationLinks, adhocQuerys, subscriptions,
				users, persons, registrys);

		if (associations.size() > 0) {
			AssociationDAO associationDAO = new AssociationDAO(context);
			associationDAO.update(associations);
		}

		if (classifications.size() > 0) {
			ClassificationDAO classificationDAO = new ClassificationDAO(context);
			classificationDAO.update(classifications);
		}

		if (schemes.size() > 0) {
			ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO(context);
			classificationSchemeDAO.update(schemes);
		}

		if (classificationNodes.size() > 0) {
			ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
			classificationNodeDAO.update(classificationNodes);
		}

		if (externalIds.size() > 0) {
			// ExternalId is no longer the first level, right?
			ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
			externalIdentifierDAO.update(externalIds);
		}

		if (externalLinks.size() > 0) {
			ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
			externalLinkDAO.update(externalLinks);
		}

		if (extrinsicObjects.size() > 0) {
			ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
			extrinsicObjectDAO.update(extrinsicObjects);
		}

		if (objectRefs.size() > 0) {
			ObjectRefDAO objectRefDAO = new ObjectRefDAO(context);
			objectRefDAO.update(objectRefs);
		}

		if (organizations.size() > 0) {
			OrganizationDAO organizationDAO = new OrganizationDAO(context);
			organizationDAO.update(organizations);
		}

		if (packages.size() > 0) {
			RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
			registryPackageDAO.update(packages);
		}

		if (serviceBindings.size() > 0) {
			ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
			serviceBindingDAO.update(serviceBindings);
		}

		if (services.size() > 0) {
			ServiceDAO serviceDAO = new ServiceDAO(context);
			serviceDAO.update(services);
		}

		if (specificationLinks.size() > 0) {
			SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
			specificationLinkDAO.update(specificationLinks);
		}

		if (adhocQuerys.size() > 0) {
			AdhocQueryDAO adhocQueryDAO = new AdhocQueryDAO(context);
			adhocQueryDAO.update(adhocQuerys);
		}

		if (subscriptions.size() > 0) {
			SubscriptionDAO subscriptionDAO = new SubscriptionDAO(context);
			subscriptionDAO.update(subscriptions);
		}

		if (users.size() > 0) {
			UserDAO userDAO = new UserDAO(context);
			userDAO.update(users);
		}

		if (persons.size() > 0) {
			PersonDAO personDAO = new PersonDAO(context);
			personDAO.update(persons);
		}

		if (registrys.size() > 0) {
			RegistryDAO registryDAO = new RegistryDAO(context);
			registryDAO.insert(registrys);
		}

		// Special case handling for AuditableEvents as they can only be created
		// by this class and not by clients of this class.

		// Ignore any client supplied AuditableEvents
		auditableEvents.clear();

	}

	/**
	 * Update the status of specified objects to the specified status.
	 * 
	 */
	@SuppressWarnings("static-access")
	public void updateStatus(ServerRequestContext context, List<String> registryObjectsIds, String status)
			throws RegistryException {

		try {
			// Make sure that status is a ref to a StatusType ClassificationNode
			context.checkClassificationNodeRefConstraint(status, bu.CANONICAL_CLASSIFICATION_SCHEME_ID_StatusType,
					"status");

			ObjectRefListType ebObjectRefListType = bu.rimFac.createObjectRefListType();

			List<ObjectRefType> refs = bu.getObjectRefsFromRegistryObjectIds(registryObjectsIds);
			Iterator<ObjectRefType> iter = refs.iterator();
			while (iter.hasNext()) {
				ObjectRefType ebObjectRefType = iter.next();
				RegistryObjectType ebRegistryObjectType = getRegistryObject(context, ebObjectRefType);
				RegistryObjectDAO roDAO = (RegistryObjectDAO) getDAOForObject(ebRegistryObjectType, context);
				roDAO.updateStatus(ebRegistryObjectType, status);

				ebObjectRefListType.getObjectRef().add(ebObjectRefType);
			}

			// repeat behavior of DAO.prepareToXXX methods for direct/indirect
			// setStatus
			if (context.getCurrentRegistryRequest() instanceof ApproveObjectsRequest) {
				context.getApproveEvent().setAffectedObjects(ebObjectRefListType);
				context.addAffectedObjectsToAuditableEvent(context.getApproveEvent(), ebObjectRefListType);
			} else if (context.getCurrentRegistryRequest() instanceof DeprecateObjectsRequest) {
				context.getDeprecateEvent().setAffectedObjects(ebObjectRefListType);
				context.addAffectedObjectsToAuditableEvent(context.getDeprecateEvent(), ebObjectRefListType);
			} else if (context.getCurrentRegistryRequest() instanceof UndeprecateObjectsRequest) {
				context.getUnDeprecateEvent().setAffectedObjects(ebObjectRefListType);
				context.addAffectedObjectsToAuditableEvent(context.getUnDeprecateEvent(), ebObjectRefListType);
			} else if (context.getCurrentRegistryRequest() instanceof SetStatusOnObjectsRequest) {
				SetStatusOnObjectsRequest req = (SetStatusOnObjectsRequest) context.getCurrentRegistryRequest();
				context.getSetStatusEvent().setAffectedObjects(ebObjectRefListType);
				context.getSetStatusEvent().setEventType(req.getStatus());
				context.addAffectedObjectsToAuditableEvent(context.getSetStatusEvent(), ebObjectRefListType);
			}
		} catch (JAXRException e) {
			throw new RegistryException(e);
		}
	}

	/**
	 * Given a Binding object returns the OMARDAO for that object.
	 * 
	 */
	private OMARDAO getDAOForObject(RegistryObjectType ro, ServerRequestContext context) throws RegistryException {
		OMARDAO dao = null;

		try {
			@SuppressWarnings("unused")
			String bindingClassName = ro.getClass().getName();
			
			// reuse same mapper as for tablenames
			// this handles Type1 endings correctly

			String daoClassName = Utility.getInstance().mapDAOName(ro);
			
			// Construct the corresponding DAO instance using reflections
			Class<?> daoClazz = Class.forName("it.cnr.icar.eric.server.persistence.rdb." + daoClassName + "DAO");

			Class<?>[] conParameterTypes = new Class[1];
			conParameterTypes[0] = context.getClass();
			Object[] conParameterValues = new Object[1];
			conParameterValues[0] = context;
			Constructor<?>[] cons = daoClazz.getDeclaredConstructors();

			// Find the constructor that takes RequestContext as its only arg
			Constructor<?> con = null;
			for (int i = 0; i < cons.length; i++) {
				con = cons[i];
				if ((con.getParameterTypes().length == 1) && (con.getParameterTypes()[0] == conParameterTypes[0])) {
					dao = (OMARDAO) con.newInstance(conParameterValues);
					break;
				}
			}

		} catch (Exception e) {
			throw new RegistryException(e);
		}

		return dao;

	}

	/**
	 * Does a bulk delete of a heterogeneous Collection of RegistrObjects. If
	 * any RegistryObject cannot be found, it will make no change to the
	 * database and throw RegistryException
	 * 
	 */
	public void delete(ServerRequestContext context, List<ObjectRefType> orefs) throws RegistryException {
		// Return if nothing specified to delete
		if (orefs.size() == 0) {
			return;
		}

		ArrayList<Object> idList = new ArrayList<Object>();
		idList.addAll(context.getObjectRefTypeMap().keySet());

		// First fetch the objects and then delete them
		String query = "SELECT * FROM RegistryObject ro WHERE ro.id IN ( " + bu.getIdListFromIds(idList) + " ) ";
		List<?> objs = getRegistryObjectsMatchingQuery(context, query, null, "RegistryObject");
		List<String> userAliases = null;
		Iterator<?> iter = objs.iterator();
		while (iter.hasNext()) {
			RegistryObjectType ro = (RegistryObjectType) iter.next();
			if (ro instanceof UserType) {
				if (userAliases == null) {
					userAliases = new ArrayList<String>();
				}
				userAliases.add(((UserType) ro).getId());
			}
			OMARDAO dao = getDAOForObject(ro, context);

			// Now call delete method
			List<RegistryObjectType> objectsToDelete = new ArrayList<RegistryObjectType>();
			objectsToDelete.add(ro);
			dao.delete(objectsToDelete);
		}

		// Now delete from ObjectRef table
		ObjectRefDAO dao = new ObjectRefDAO(context);
		dao.delete(orefs);
		// Now, if any of the deleted ROs were of UserType, delete the
		// credentials
		// from the server keystore
		if (userAliases != null) {
			Iterator<String> aliasItr = userAliases.iterator();
			String alias = null;
			while (aliasItr.hasNext()) {
				try {
					alias = aliasItr.next();
					AuthenticationServiceImpl.getInstance().deleteUserCertificate(alias);
				} catch (Throwable t) {
					ServerResourceBundle.getInstance().getString("message.couldNotDeleteCredentials",
							new Object[] { alias });
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private java.sql.DatabaseMetaData getMetaData(Connection connection) throws SQLException {
		if (metaData == null) {
			metaData = connection.getMetaData();
		}

		return metaData;
	}

	/**
	 * Executes an SQL Query with default values for IterativeQueryParamHolder.
	 */
	public List<IdentifiableType> executeSQLQuery(ServerRequestContext context, String sqlQuery,
			ResponseOptionType responseOption, String tableName, List<?> objectRefs) throws RegistryException {

		IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
		return executeSQLQuery(context, sqlQuery, responseOption, tableName, objectRefs, paramHolder);
	}

	/**
	 * Executes and SQL query using specified parameters. This variant is used
	 * to invoke fixed queries without PreparedStatements.
	 * 
	 * @return An List of RegistryObjectType instances
	 */
	public List<IdentifiableType> executeSQLQuery(ServerRequestContext context, String sqlQuery,
			ResponseOptionType responseOption, String tableName, List<?> objectRefs, IterativeQueryParams paramHolder)
			throws RegistryException {

		return executeSQLQuery(context, sqlQuery, null, responseOption, tableName, objectRefs, paramHolder);
	}

	/**
	 * Executes an SQL Query.
	 */
	public List<?> executeSQLQuery(ServerRequestContext context, String sqlQuery, List<String> queryParams,
			ResponseOptionType responseOption, String tableName, List<?> objectRefs) throws RegistryException {

		IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
		return executeSQLQuery(context, sqlQuery, queryParams, responseOption, tableName, objectRefs, paramHolder);
	}

	/**
	 * Executes an SQL Query.
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public List<IdentifiableType> executeSQLQuery(ServerRequestContext context, String sqlQuery, List<String> queryParams,
			ResponseOptionType ebResponseOptionType, String tableName, List<?> objectRefs, IterativeQueryParams paramHolder)
			throws RegistryException {
		@SuppressWarnings("rawtypes")
		List ebIdentifiableTypeResultList = null;
		Connection connection = null;
		int startIndex = paramHolder.startIndex;
		int maxResults = paramHolder.maxResults;
		int totalResultCount = -1;
		Statement stmt = null;

		try {
			connection = context.getConnection();

			java.sql.ResultSet rs = null;

			tableName = Utility.getInstance().mapTableName(tableName);

			ReturnType returnType = ebResponseOptionType.getReturnType();
			@SuppressWarnings("unused")
			boolean returnComposedObjects = ebResponseOptionType.isReturnComposedObjects();

			if (maxResults < 0) {
				if (queryParams == null) {
					stmt = connection.createStatement();
				} else {
					stmt = connection.prepareStatement(sqlQuery);
				}
			} else {
				if (queryParams == null) {
					stmt = connection.createStatement(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
							java.sql.ResultSet.CONCUR_READ_ONLY);
				} else {
					stmt = connection.prepareStatement(sqlQuery, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
							java.sql.ResultSet.CONCUR_READ_ONLY);
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("Executing query: '" + sqlQuery + "'");
				if (dumpStackOnQuery) {
					Thread.currentThread().dumpStack();
				}
			}
			
			if (queryParams == null) {
				rs = stmt.executeQuery(sqlQuery);
			} else {
				Iterator<String> iter = queryParams.iterator();
				int paramCount = 0;
				while (iter.hasNext()) {
					Object param = iter.next();
					((PreparedStatement) stmt).setObject(++paramCount, param);
				}
				rs = ((PreparedStatement) stmt).executeQuery();
			}
			if (maxResults >= 0) {
				rs.last();
				totalResultCount = rs.getRow();
				// Reset back to before first row so that DAO can correctly
				// scroll
				// through the result set
				rs.beforeFirst();
			}
			@SuppressWarnings("unused")
			Iterator<?> iter = null;

			log.debug("::3:: SQL result ReturnType" + returnType.toString());

			if (returnType == ReturnType.OBJECT_REF) {

				ebIdentifiableTypeResultList = new ArrayList<Object>();

				if (startIndex > 0) {
					rs.last();
					totalResultCount = rs.getRow();
					rs.beforeFirst();
					// calling rs.next() is a workaround for some drivers, such
					// as Derby's, that do not set the cursor during call to
					// rs.relative(...)
					rs.next();
					@SuppressWarnings("unused")
					boolean onRow = rs.relative(startIndex - 1);
				}

				int cnt = 0;
				while (rs.next()) {

					ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
					// TODO: JAXBElement

					String id = rs.getString(1);
					ebObjectRefType.setId(id);
					ebIdentifiableTypeResultList.add(ebObjectRefType);

					if (++cnt == maxResults) {
						break;
					}
				}
			} else if (returnType == ReturnType.REGISTRY_OBJECT) {
				context.setResponseOption(ebResponseOptionType);
				RegistryObjectDAO roDAO = new RegistryObjectDAO(context);
				ebIdentifiableTypeResultList = roDAO.getObjects(rs, startIndex, maxResults);

			} else if ((returnType == ReturnType.LEAF_CLASS)
					|| (returnType == ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM)) {
				ebIdentifiableTypeResultList = getObjects(context, connection, rs, tableName, ebResponseOptionType, objectRefs,
						startIndex, maxResults);

			} else {
				throw new RegistryException(ServerResourceBundle.getInstance().getString("message.invalidReturnType",
						new Object[] { returnType }));
			}

		} catch (SQLException e) {
			throw new RegistryException(e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException sqle) {
				log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
			}
		}

		paramHolder.totalResultCount = totalResultCount;

		return ebIdentifiableTypeResultList;
	}

	private List<?> getObjects(ServerRequestContext context, Connection connection, java.sql.ResultSet rs,
			String tableName, ResponseOptionType responseOption, List<?> objectRefs, int startIndex, int maxResults)
			throws RegistryException {
		List<?> ebIdentifiableTypeList = null;

		context.setResponseOption(responseOption);

		if (tableName.equalsIgnoreCase(AdhocQueryDAO.getTableNameStatic())) {
			AdhocQueryDAO adhocQueryDAO = new AdhocQueryDAO(context);
			ebIdentifiableTypeList = adhocQueryDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(AssociationDAO.getTableNameStatic())) {
			AssociationDAO associationDAO = new AssociationDAO(context);
			ebIdentifiableTypeList = associationDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(AuditableEventDAO.getTableNameStatic())) {
			AuditableEventDAO auditableEventDAO = new AuditableEventDAO(context);
			ebIdentifiableTypeList = auditableEventDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ClassificationDAO.getTableNameStatic())) {
			ClassificationDAO classificationDAO = new ClassificationDAO(context);
			ebIdentifiableTypeList = classificationDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ClassificationSchemeDAO.getTableNameStatic())) {
			ClassificationSchemeDAO classificationSchemeDAO = new ClassificationSchemeDAO(context);
			ebIdentifiableTypeList = classificationSchemeDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ClassificationNodeDAO.getTableNameStatic())) {
			ClassificationNodeDAO classificationNodeDAO = new ClassificationNodeDAO(context);
			ebIdentifiableTypeList = classificationNodeDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ExternalIdentifierDAO.getTableNameStatic())) {
			ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
			ebIdentifiableTypeList = externalIdentifierDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ExternalLinkDAO.getTableNameStatic())) {
			ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
			ebIdentifiableTypeList = externalLinkDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ExtrinsicObjectDAO.getTableNameStatic())) {
			ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
			ebIdentifiableTypeList = extrinsicObjectDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(FederationDAO.getTableNameStatic())) {
			FederationDAO federationDAO = new FederationDAO(context);
			ebIdentifiableTypeList = federationDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ObjectRefDAO.getTableNameStatic())) {
			ObjectRefDAO objectRefDAO = new ObjectRefDAO(context);
			ebIdentifiableTypeList = objectRefDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(OrganizationDAO.getTableNameStatic())) {
			OrganizationDAO organizationDAO = new OrganizationDAO(context);
			ebIdentifiableTypeList = organizationDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(RegistryObjectDAO.getTableNameStatic())) {
			RegistryObjectDAO registryObjectDAO = new RegistryObjectDAO(context);
			// TODO: Use reflection instead in future
			ebIdentifiableTypeList = registryObjectDAO.getObjectsHetero(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(RegistryPackageDAO.getTableNameStatic())) {
			RegistryPackageDAO registryPackageDAO = new RegistryPackageDAO(context);
			ebIdentifiableTypeList = registryPackageDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ServiceBindingDAO.getTableNameStatic())) {
			ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
			ebIdentifiableTypeList = serviceBindingDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(ServiceDAO.getTableNameStatic())) {
			ServiceDAO serviceDAO = new ServiceDAO(context);
			ebIdentifiableTypeList = serviceDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(SpecificationLinkDAO.getTableNameStatic())) {
			SpecificationLinkDAO specificationLinkDAO = new SpecificationLinkDAO(context);
			ebIdentifiableTypeList = specificationLinkDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(SubscriptionDAO.getTableNameStatic())) {
			SubscriptionDAO subscriptionDAO = new SubscriptionDAO(context);
			ebIdentifiableTypeList = subscriptionDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(UserDAO.getTableNameStatic())) {
			UserDAO userDAO = new UserDAO(context);
			ebIdentifiableTypeList = userDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(PersonDAO.getTableNameStatic())) {
			PersonDAO personDAO = new PersonDAO(context);
			ebIdentifiableTypeList = personDAO.getObjects(rs, startIndex, maxResults);
		} else if (tableName.equalsIgnoreCase(RegistryDAO.getTableNameStatic())) {
			RegistryDAO registryDAO = new RegistryDAO(context);
			ebIdentifiableTypeList = registryDAO.getObjects(rs, startIndex, maxResults);
		}

		return ebIdentifiableTypeList;
	}

	/**
	 * Gets the specified objects using specified query and className
	 * 
	 */
	public List<?> getRegistryObjectsMatchingQuery(ServerRequestContext context, String query,
			List<String> queryParams, String tableName) throws RegistryException {
		List<?> ebObjectRefTypeList = null;

		ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		ebResponseOptionType.setReturnComposedObjects(true);

		ebObjectRefTypeList = getIdentifiablesMatchingQuery(context, query, queryParams, tableName,
				ebResponseOptionType);

		return ebObjectRefTypeList;
	}

	/**
	 * Gets the specified objects using specified query and className
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public List<?> getIdentifiablesMatchingQuery(ServerRequestContext context, String query,
			List<String> queryParams, String tableName, ResponseOptionType responseOption) throws RegistryException {
		List<?> ebObjectRefTypeList = null;

		List objectRefs = new java.util.ArrayList();
		IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
		ebObjectRefTypeList = executeSQLQuery(context, query, queryParams, responseOption, tableName, objectRefs,
				paramHolder);
		return ebObjectRefTypeList;
	}

	/**
	 * Gets the first object matching specified query. TODO: This is a dangerous
	 * query to use and it should eventually be eliminated.
	 */
	public RegistryObjectType getRegistryObjectMatchingQuery(ServerRequestContext context, String query,
			List<String> queryParams, String tableName) throws RegistryException {
		RegistryObjectType ro = null;

		List<?> al = getRegistryObjectsMatchingQuery(context, query, queryParams, tableName);

		if (al.size() >= 1) {
			ro = (RegistryObjectType) al.get(0);
		}

		return ro;
	}

	/**
	 * Gets the specified object using specified id and className
	 * 
	 */
	public IdentifiableType getIdentifiableMatchingQuery(ServerRequestContext context, String query, List<String> queryParams,
			String tableName, ResponseOptionType responseOption) throws RegistryException {
		IdentifiableType obj = null;

		List<?> al = getIdentifiablesMatchingQuery(context, query, queryParams, tableName, responseOption);

		if (al.size() == 1) {
			obj = (IdentifiableType) al.get(0);
		}

		return obj;
	}

	/**
	 * Gets the specified object using specified id and className
	 * 
	 */
	public RegistryObjectType getRegistryObject(ServerRequestContext context, String id, String className)
			throws RegistryException {
		RegistryObjectType ro = null;

		ResponseOptionType ebResponseOptionType = bu.queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		ebResponseOptionType.setReturnComposedObjects(true);
		ro = (RegistryObjectType) getIdentifiable(context, id, className, ebResponseOptionType);

		return ro;
	}

	/**
	 * Gets the specified object using specified id and className
	 * 
	 */
	public IdentifiableType getIdentifiable(ServerRequestContext context, String id, String className,
			ResponseOptionType responseOption) throws RegistryException {
		IdentifiableType ebIdentifiableType = null;

		String tableName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(className);
		String sqlQuery = "SELECT * FROM " + tableName + " WHERE id = ?";
		ArrayList<String> queryParams = new ArrayList<String>();
		queryParams.add(id);

		ebIdentifiableType = getIdentifiableMatchingQuery(context, sqlQuery, queryParams, tableName, responseOption);

		return ebIdentifiableType;
	}

	/**
	 * Gets the specified object using specified ObjectRef
	 * 
	 */
	public RegistryObjectType getRegistryObject(ServerRequestContext context, ObjectRefType ref)
			throws RegistryException {

		return getRegistryObject(context, ref.getId(), "RegistryObject");
	}

	/**
	 * Get a HashMap with registry object id as key and owner id as value
	 */
	public HashMap<String, String> getOwnersMap(ServerRequestContext context, List<String> ids) throws RegistryException {
		RegistryObjectDAO roDAO = new RegistryObjectDAO(context);

		HashMap<String, String> ownersMap = roDAO.getOwnersMap(ids);

		return ownersMap;
	}

	/**
	 * Sets the owner on the specified objects based upon RequestContext.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void changeOwner(ServerRequestContext context, List objects) throws RegistryException {
		try {
			ObjectRefListType ebObjectRefListType = bu.rimFac.createObjectRefListType();
			ebObjectRefListType.getObjectRef().addAll(bu.getObjectRefTypeListFromRegistryObjects(objects));
			context.getRelocateEvent().setAffectedObjects(ebObjectRefListType);
		} catch (JAXRException e) {
			throw new RegistryException(e);
		}
	}

	/**
	 * Updates the idToLidMap in context entries with RegistryObject id as Key
	 * and RegistryObject lid as value for each object that matches specified
	 * id.
	 * 
	 */
	public void updateIdToLidMap(ServerRequestContext context, Set<String> ids, String tableName) throws RegistryException {
		if ((ids != null) && (ids.size() >= 0)) {

			Iterator<String> iter = ids.iterator();
			Statement stmt = null;

			try {
				stmt = context.getConnection().createStatement();

				StringBuffer sql = new StringBuffer("SELECT id, lid FROM " + tableName + " WHERE id IN (");
				@SuppressWarnings("unused")
				List<String> existingIdList = new ArrayList<String>();

				/*
				 * We need to count the number of item in "IN" list. We need to
				 * split the a single SQL Strings if it is too long. Some
				 * database such as Oracle, does not allow the IN list is too
				 * long
				 */
				int listCounter = 0;

				while (iter.hasNext()) {
					String id = iter.next();

					if (iter.hasNext() && (listCounter < IdentifiableDAO.identifiableExistsBatchCount)) {
						sql.append("'" + id + "',");
					} else {
						sql.append("'" + id + "')");

						// log.info("sql string=" + sql.toString());
						ResultSet rs = stmt.executeQuery(sql.toString());

						while (rs.next()) {
							String _id = rs.getString(1);
							String lid = rs.getString(2);
							context.getIdToLidMap().put(_id, lid);
						}

						sql = new StringBuffer("SELECT id, lid FROM " + tableName + " WHERE id IN (");
						listCounter = 0;
					}

					listCounter++;
				}
			} catch (SQLException e) {
				throw new RegistryException(e);
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException sqle) {
					log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
				}
			}
		}
	}

	/**
	 * Checks each object being deleted to make sure that it does not have any
	 * currently existing references. Objects must be fetched from the Cache or
	 * Server and not from the RequestContext??
	 * 
	 * @throws ReferencesExistException
	 *             if references exist to any of the RegistryObject ids
	 *             specified in roIds
	 * 
	 */
	@SuppressWarnings("static-access")
	public void checkIfReferencesExist(ServerRequestContext context, List<String> roIds) throws RegistryException {
		if (skipReferenceCheckOnRemove) {
			return;
		}

		Iterator<String> iter = roIds.iterator();

		HashMap<String, ArrayList<String>> idToReferenceSourceMap = new HashMap<String, ArrayList<String>>();
		while (iter.hasNext()) {
			String id = iter.next();

			StringBuffer query = new StringBuffer();
			query.append("SELECT id FROM RegistryObject WHERE objectType = ? UNION ");
			query.append("SELECT id FROM ClassificationNode WHERE parent = ? UNION ");
			query.append("SELECT id FROM Classification WHERE classificationNode = ? OR classificationScheme = ? OR classifiedObject = ? UNION ");
			query.append("SELECT id FROM ExternalIdentifier WHERE identificationScheme = ? OR registryObject = ? UNION ");
			query.append("SELECT id FROM Association WHERE associationType = ? OR sourceObject = ? OR targetObject= ?  UNION ");
			query.append("SELECT id FROM AuditableEvent WHERE user_ = ? OR requestId = ? UNION ");
			query.append("SELECT id FROM Organization WHERE parent = ? UNION ");
			query.append("SELECT id FROM Registry where operator = ? UNION ");
			query.append("SELECT id FROM ServiceBinding WHERE service = ? OR targetBinding = ? UNION ");
			query.append("SELECT id FROM SpecificationLink WHERE serviceBinding = ? OR specificationObject = ? UNION ");
			query.append("SELECT id FROM Subscription WHERE selector = ?  UNION ");
			query.append("SELECT s.parent FROM Slot s WHERE s.slotType = '" + bu.CANONICAL_DATA_TYPE_ID_ObjectRef
					+ "' AND s.value = ?");

			PreparedStatement stmt = null;
			try {
				stmt = context.getConnection().prepareStatement(query.toString());
				stmt.setString(1, id);
				stmt.setString(2, id);
				stmt.setString(3, id);
				stmt.setString(4, id);
				stmt.setString(5, id);
				stmt.setString(6, id);
				stmt.setString(7, id);
				stmt.setString(8, id);
				stmt.setString(9, id);
				stmt.setString(10, id);
				stmt.setString(11, id);
				stmt.setString(12, id);
				stmt.setString(13, id);
				stmt.setString(14, id);
				stmt.setString(15, id);
				stmt.setString(16, id);
				stmt.setString(17, id);
				stmt.setString(18, id);
				stmt.setString(19, id);
				stmt.setString(20, id);

				ResultSet rs = stmt.executeQuery();
				@SuppressWarnings("unused")
				boolean result = false;

				ArrayList<String> referenceSourceIds = new ArrayList<String>();
				while (rs.next()) {
					String referenceSourceId = rs.getString(1);
					if (!roIds.contains(referenceSourceId)) {
						referenceSourceIds.add(referenceSourceId);
					}
				}

				if (!referenceSourceIds.isEmpty()) {
					idToReferenceSourceMap.put(id, referenceSourceIds);
				}
			} catch (SQLException e) {
				throw new RegistryException(e);
			} finally {
				try {
					if (stmt != null) {
						stmt.close();
					}
				} catch (SQLException sqle) {
					log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), sqle);
				}
			}
		}

		if (!idToReferenceSourceMap.isEmpty()) {
			// At least one ref exists to at least one object so throw exception
			String msg = ServerResourceBundle.getInstance().getString("message.referencesExist");
			msg += "\n" + idToReferenceSourceMap.toString();

			throw new ReferencesExistException(msg);
		}
	}

}

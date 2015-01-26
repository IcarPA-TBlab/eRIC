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
package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.SQLQueryProvider;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.QueryManagerLocalProxy;
import it.cnr.icar.eric.common.QueryManagerSOAPProxy;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.QueryManager;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;



import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Implements JAXR API interface named QueryManager
 * 
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class QueryManagerImpl implements QueryManager {
	@SuppressWarnings("unused")
	private HashMap<?, ?> schemeToValueMap;
	@SuppressWarnings("unused")
	private HashMap<?, ?> valueToConceptMap;
	protected DeclarativeQueryManagerImpl dqm;
	protected RegistryServiceImpl regService;
	protected BusinessLifeCycleManagerImpl lcm;
	protected User callersUser;
	protected it.cnr.icar.eric.common.spi.QueryManager serverQMProxy;

	QueryManagerImpl(RegistryServiceImpl regService, BusinessLifeCycleManagerImpl lcm, DeclarativeQueryManagerImpl dqm)
			throws JAXRException {
		this.regService = regService;
		this.lcm = lcm;
		this.dqm = dqm;

		setCredentialInfo((regService.getConnection()).getCredentialInfo());

		if (this.dqm == null) {
			this.dqm = (DeclarativeQueryManagerImpl) this;
		}
	}

	/**
	 * Gets the RegistryObject specified by the Id and type of object.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param id
	 *            is the id of the Key for a RegistryObject.
	 * @param objectType
	 *            is a constant definition from LifeCycleManager that specifies
	 *            the type of object desired.
	 * @return RegistryObject Is the object is returned as their concrete type
	 *         (e.g. Organization, User etc.).
	 */
	public RegistryObject getRegistryObject(String id, String objectType) throws JAXRException {
		RegistryObject ro = null;

		try {
			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.QueryManagerImpl:getRegistryObject", null);
						
			RegistryObjectType ebRegistryObjectType = serverQMProxy.getRegistryObject(context, id,
					Utility.getInstance().mapTableName(objectType));
			List<RegistryObjectType> jaxbObjects = new ArrayList<RegistryObjectType>();
						
			if (ebRegistryObjectType != null) {
				jaxbObjects.add(ebRegistryObjectType);
			}

			List<RegistryObjectImpl> ros = JAXRUtility.getJAXRObjectsFromJAXBObjects(lcm, jaxbObjects, null);
			if (ros.size() > 0) {
				ro = ros.get(0);
			}
		} catch (ObjectNotFoundException e) {
			// Maintain backward compatibility and return null instead of
			// throwing exception.
//			System.out.println("--> getregistryObject silent catch ObjectNotFoundException<" + objectType + ">: " + id);
//			e.printStackTrace();
			ro = null;
		}

		return ro;
	}

	/**
	 * Gets the RegistryObject specified by the Id.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @return RegistryObject Is the object is returned as their concrete type
	 *         (e.g. Organization, User etc.).
	 */
	public RegistryObject getRegistryObject(String id) throws JAXRException {
		return getRegistryObject(id, "RegistryObject");
	}

	/**
	 * Gets the specified RegistryObjects. The objects are returned as their
	 * concrete type (e.g. Organization, User etc.).
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * 
	 * @return BulkResponse containing a hetrogeneous Collection of
	 *         RegistryObjects (e.g. Organization, User etc.).
	 */
	public BulkResponse getRegistryObjects(@SuppressWarnings("rawtypes") Collection objectKeys) throws JAXRException {
		return getRegistryObjects(objectKeys, "RegistryObject");
	}

	/**
	 * Gets the RegistryObjects owned by the Caller. The objects are returned as
	 * their concrete type (e.g. Organization, User etc.). For to JAXR 2.0??
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * 
	 * @return BulkResponse containing a hetrogeneous Collection of
	 *         RegistryObjects (e.g. Organization, User etc.).
	 */
	public BulkResponse getCallersRegistryObjects() throws JAXRException {
		return getRegistryObjects((String) null);
	}

	/**
	 * Gets the specified RegistryObjects of the specified object type. The
	 * objects are returned as their concrete type (e.g. Organization, User
	 * etc.).
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * 
	 * @return BulkResponse containing a heterogeneous Collection of
	 *         RegistryObjects (e.g. Organization, User etc.).
	 */
	public BulkResponse getRegistryObjects(@SuppressWarnings("rawtypes") Collection objectKeys, String objectType) throws JAXRException {

		// StringBuffer queryStr = new StringBuffer("SELECT * FROM ");
		// queryStr.append(it.cnr.icar.eric.common.Utility.getInstance().mapTableName(objectType));
		// queryStr.append(" WHERE id in (");
		//
		// Iterator iter = objectKeys.iterator();
		//
		// while (iter.hasNext()) {
		// String id = ((Key) iter.next()).getId();
		// queryStr.append("'").append(id).append("'");
		//
		// if (iter.hasNext()) {
		// queryStr.append(", ");
		// }
		// }
		//
		// queryStr.append(")");
		//
		// Query query = dqm.createQuery(Query.QUERY_TYPE_SQL,
		// queryStr.toString());

		Query query = SQLQueryProvider.getRegistryObjects(dqm, objectKeys, objectType);

		BulkResponse resp = dqm.executeQuery(query);

		return resp;
	}

	/**
	 * Gets the RegistryObjects owned by the caller. The objects are returned as
	 * their concrete type (e.g. Organization, User etc.).
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @return BulkResponse containing a heterogeneous Collection of
	 *         RegistryObjects (e.g. Organization, User etc.).
	 */
	public javax.xml.registry.BulkResponse getRegistryObjects() throws javax.xml.registry.JAXRException {
		// Rename this to getCallersRegistryObjects() in JAXR 2.0??
		return getCallersRegistryObjects();
	}

	/**
	 * Gets the RegistryObjects owned by the caller, that are of the specified
	 * type. The objects are returned as their concrete type (e.g. Organization,
	 * User etc.).
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param objectType
	 *            Is a constant that defines the type of object sought. See
	 *            LifeCycleManager for constants for object types.
	 * @see LifeCycleManager for supported objectTypes
	 * @return BulkResponse containing a hetrogeneous Collection of
	 *         RegistryObjects (e.g. Organization, User etc.).
	 */
	public BulkResponse getRegistryObjects(String objectType) throws JAXRException {
		BulkResponse resp = null;

		// TODO: xxx pa 111228 findConceptByPath() dependency

		String queryStr = "SELECT ro.* FROM "
				+ "RegistryObject ro, AuditableEvent ae, AffectedObject ao, User_ u WHERE ae.user_ = $currentUser AND ao.id = ro.id AND ao.eventId = ae.id";

		if (objectType != null) {
			String newObjectType = BindingUtility.mapJAXRNameToEbXMLName(objectType);

			// ??The following code is a little dodgy but works for now
			// Shouldn't this be searching for Concept.code instead of path?
			Concept objectTypeConcept;
			if ("User".equals(objectType)) {
				objectTypeConcept = regService.getBusinessQueryManager().findConceptByPath(
						"/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType
								+ "/RegistryObject/Person/User");
			} else {
				objectTypeConcept = regService.getBusinessQueryManager().findConceptByPath(
						"/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType + "/RegistryObject/"
								+ newObjectType);
			}
			if (objectTypeConcept == null) {
				// Try looking under ExtrinsicObject
				objectTypeConcept = regService.getBusinessQueryManager().findConceptByPath(
						"/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType
								+ "/RegistryObject/ExtrinsicObjects/" + newObjectType);
			}

			if (objectTypeConcept != null) {
				queryStr += " AND ro.objectType = '" + objectTypeConcept.getKey().getId() + "'";
			} else {
				throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString(
						"message.error.invalid.objecttype", new Object[] { objectType }));
			}
		}

		Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
		resp = dqm.executeQuery(query);

		return resp;
	}

	/*
	 * Gets the specified pre-defined concept as defined in Appendix A of the
	 * JAXR specification.
	 * 
	 * <p><DL><DT><B>Capability Level: 1 </B></DL>
	 * 
	 * @return The pre-defined Concept
	 * 
	 * Implementation internal
	 * 
	 * public Concept getPredefinedConcept(String schemeName, String value)
	 * throws JAXRException { if (schemeToValueMap == null) { schemeToValueMap =
	 * new HashMap(); } HashMap valueToConceptMap =
	 * (HashMap)schemeToValueMap.get(schemeName); if (valueToConceptMap == null)
	 * { valueToConceptMap = new HashMap(); schemeToValueMap.put(schemeName,
	 * valueToConceptMap); } ConceptImpl concept =
	 * (ConceptImpl)valueToConceptMap.get(value); if (concept == null) { //
	 * Existing ConceptImpl not found so create a new one concept = new
	 * ConceptImpl(lcm); concept.setValue(value); // XXX set other Concept
	 * parts, like path valueToConceptMap.put(value, concept); } return concept;
	 * }
	 */
	public javax.xml.registry.RegistryService getRegistryService() throws javax.xml.registry.JAXRException {
		return regService;
	}

	// Add as Level 1 call in JAXR 2.0??
	public User getCallersUser() throws JAXRException {
		if (callersUser == null) {
			HashMap<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put(BindingUtility.CANONICAL_SLOT_QUERY_ID, BindingUtility.CANONICAL_QUERY_GetCallersUser);
			Query getCallersUserQuery = dqm.createQuery(Query.QUERY_TYPE_SQL);
			BulkResponse br = dqm.executeQuery(getCallersUserQuery, paramsMap);

			callersUser = (User) ((BulkResponseImpl) br).getRegistryObject();
		}

		return callersUser;
	}

	// TODO: Add to JAXR 2.0 API??
	public RepositoryItem getRepositoryItem(String id) throws JAXRException {
		ClientRequestContext context = new ClientRequestContext(
				"it.cnr.icar.eric.client.xml.registry.QueryManagerImpl:getRepositoryItem", null);
		return serverQMProxy.getRepositoryItem(context, id);
	}

	void setCredentialInfo(CredentialInfo credentialInfo) throws JAXRException {
		// Since new credentials usually indicates a different rim:User,
		// clear the cached callersUser variable
		callersUser = null;
		boolean localCall = ((((RegistryServiceImpl) this.getRegistryService()).getConnection()))
				.isLocalCallMode();

		if (localCall) {
			serverQMProxy = new QueryManagerLocalProxy(
					(regService.getConnection()).getQueryManagerURL(), credentialInfo);
		} else {
			serverQMProxy = new QueryManagerSOAPProxy(
					(regService.getConnection()).getQueryManagerURL(), credentialInfo);
		}
	}
}

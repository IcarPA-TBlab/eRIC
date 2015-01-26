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
import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;

/**
 * 
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 */
class RegistryObjectDAO extends IdentifiableDAO {
	private static final Log log = LogFactory.getLog(RegistryObjectDAO.class);

	/**
	 * Use this constructor only.
	 */
	RegistryObjectDAO(ServerRequestContext context) {
		super(context);
	}

	public static String getTableNameStatic() {
		return "RegistryObject";
	}

	public String getTableName() {
		return getTableNameStatic();
	}

	protected void prepareToInsert(Object object) throws RegistryException {
		super.prepareToInsert(object);
		RegistryObjectType ebRegistryObjectType = (RegistryObjectType) object;

		// Need to distinguish between Created and Versioned events
		// An original object (Create) has either no versionName or
		// versionName of "1.1"

		String versionName = null;
		VersionInfoType versionInfo = ebRegistryObjectType.getVersionInfo();
		if (versionInfo != null) {
			versionName = versionInfo.getVersionName();
		}

		if (!(ebRegistryObjectType instanceof AuditableEventType)) {
			// Careful not to include event as affected by itself
			AuditableEventType ebAuditableEventType = null;
			if ((versionName != null) && (!versionName.equals("1.1"))) {
				// Add to affectedObjects of versionEvent
				ebAuditableEventType = context.getVersionEvent();
			} else {
				// Add to affectedObjects of createEvent
				ebAuditableEventType = context.getCreateEvent();
			}
			context.addAffectedObjectToAuditableEvent(ebAuditableEventType, ebRegistryObjectType);
		}
	}

	protected void prepareToUpdate(Object object) throws RegistryException {
		super.prepareToUpdate(object);
		RegistryObjectType ro = (RegistryObjectType) object;

		if (!(ro instanceof AuditableEventType)) {
			// Add to affectedObjects of updateEvent
			// But careful not to include event as affected by itself
			AuditableEventType ae = context.getUpdateEvent();
			context.addAffectedObjectToAuditableEvent(ae, ro);
		}
	}

	protected void prepareToDelete(Object object) throws RegistryException {
		super.prepareToDelete(object);
		RegistryObjectType ro = (RegistryObjectType) object;

		if (!(ro instanceof AuditableEventType)) {
			// Add to affectedObjects of deleteEvent
			// Careful not to include event as affected by itself
			AuditableEventType ae = context.getDeleteEvent();
			context.addAffectedObjectToAuditableEvent(ae, ro);
		}
	}

	/**
	 * Get the objectType for specified object. If it is an ExtrinsicObject or
	 * ExternalLink then get it from the object. Otherwise ignore the value in
	 * the object and get from the DAO the hardwired value.
	 */
	protected String getObjectType(RegistryObjectType ro) throws RegistryException {
		String objectType = null;
		String rimName = null;

		try {
			String roClassName = ro.getClass().getName();

			if (roClassName.endsWith("Type")) {
				// length 4
				rimName = roClassName.substring(roClassName.lastIndexOf('.') + 1, roClassName.length() - 4);
			} else if (roClassName.endsWith("Type1")) {
				// length 5
				rimName = roClassName.substring(roClassName.lastIndexOf('.') + 1, roClassName.length() - 5);
			}

			Field field = CanonicalSchemes.class.getDeclaredField("CANONICAL_OBJECT_TYPE_ID_" + rimName);
			objectType = field.get(null).toString();
		} catch (Exception e) {
			throw new RegistryException(e);
		}

		// TODO Get object type from leaf DAO if not ExtrinsicObject
		if ((ro instanceof ExtrinsicObjectType) || (ro instanceof ExternalLinkType)) {
			String _objectType = ro.getObjectType();
			if (_objectType != null) {
				objectType = _objectType;
			}

			// Make sure that objectType is a ref to a ObjectType
			// ClassificationNode
			//context.checkClassificationNodeRefConstraint(objectType, bu.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType, "objectType");
		}

		return objectType;
	}

	/*
	 * Indicate whether the type for this DAO has composed objects or not. Used
	 * in deciding whether to deleteComposedObjects or not during delete.
	 */
	protected boolean hasComposedObject() {
		return true;
	}

	/**
	 * Delete composed objects that have the specified registryObject as parent.
	 */
	protected void deleteComposedObjects(Object object) throws RegistryException {
		super.deleteComposedObjects(object);

		if (object instanceof RegistryObjectType) {
			RegistryObjectType registryObject = (RegistryObjectType) object;

			ClassificationDAO classificationDAO = new ClassificationDAO(context);
			classificationDAO.setParent(registryObject);
			DescriptionDAO descriptionDAO = new DescriptionDAO(context);
			descriptionDAO.setParent(registryObject);
			ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
			externalIdentifierDAO.setParent(registryObject);
			NameDAO nameDAO = new NameDAO(context);
			nameDAO.setParent(registryObject);

			// Delete name
			nameDAO.deleteByParent();

			// Delete description
			descriptionDAO.deleteByParent();

			// Delete ExternalIdentifier
			externalIdentifierDAO.deleteByParent();

			// Delete Classifications
			classificationDAO.deleteByParent();
		} else {
			@SuppressWarnings("unused")
			int i = 0;
		}
	}

	/**
	 * Insert the composed objects for the specified registryObject
	 */
	protected void insertComposedObjects(Object object) throws RegistryException {
		super.insertComposedObjects(object);

		if (object instanceof RegistryObjectType) {
			RegistryObjectType registryObject = (RegistryObjectType) object;
			ClassificationDAO classificationDAO = new ClassificationDAO(context);
			classificationDAO.setParent(registryObject);
			DescriptionDAO descriptionDAO = new DescriptionDAO(context);
			descriptionDAO.setParent(registryObject);
			ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
			externalIdentifierDAO.setParent(registryObject);
			NameDAO nameDAO = new NameDAO(context);
			nameDAO.setParent(registryObject);

			// Insert name
			InternationalStringType name = registryObject.getName();

			String id = registryObject.getId();

			if (name != null) {
				nameDAO.insert(id, name);
			}

			// Insert description
			InternationalStringType desc = registryObject.getDescription();

			if (desc != null) {
				descriptionDAO.insert(id, desc);
			}

			// Insert ExternalIdentifiers
			List<ExternalIdentifierType> extIds = registryObject.getExternalIdentifier();

			if (extIds.size() > 0) {
				externalIdentifierDAO.insert(extIds);
			}

			// Insert Classifications
			List<ClassificationType> classifications = registryObject.getClassification();

			if (classifications.size() > 0) {
				classificationDAO.insert(classifications);
			}
		} else {
			@SuppressWarnings("unused")
			int i = 0;
		}
	}

	/**
	 * Returns the SQL fragment string needed by insert or update statements
	 * within insert or update method of sub-classes. This is done to avoid code
	 * duplication.
	 */
	protected String getSQLStatementFragment(Object object) throws RegistryException {

		RegistryObjectType ro = (RegistryObjectType) object;

		String stmtFragment = super.getSQLStatementFragment(ro);

		String lid = ro.getLid();
		String id = ro.getId();

		if (lid == null) {
			// TODO:versioning: Need to get lid of first version if not first
			// version
			lid = id;
		}

		VersionInfoType versionInfo = ro.getVersionInfo();
		String versionName = null;
		String comment = null;

		if (versionInfo != null) {
			versionName = versionInfo.getVersionName();
			comment = versionInfo.getComment();
		}

		if ((versionName == null) || (versionName.length() == 0)) {
			versionName = "1.1";
		}
		versionName = "'" + versionName + "'";

		if ((comment != null) && (comment.length() > 0)) {
			comment = "'" + comment + "'";
		} else {
			comment = null;
		}

		String objectType = null;
		if (action == DAO_ACTION_INSERT) {
			objectType = getObjectType(ro);

			// Need to force the status to Submitted
			ro.setStatus(BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted);

			stmtFragment += ", '" + lid + "', '" + objectType + "', '"
					+ BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted + "', " + versionName + ", " + comment;
		} else if (action == DAO_ACTION_UPDATE) {
			objectType = getObjectType(ro);

			// Following [ebRIM, 2.5.9] requirements, ignore any status
			// value which may have come from client -- updateStatus() will
			// change persisted status, not update()
			stmtFragment += ", lid='" + lid + "', objectType='" + objectType + "', versionName=" + versionName
					+ ", comment_=" + comment + " ";
		} else if (action == DAO_ACTION_DELETE) {
			// ??? Should this branch do something?
		}

		return stmtFragment;
	}

	/**
	 * Sort registryObjectIds by their objectType.
	 * 
	 * @return The HashMap storing the objectType String as keys and List of ids
	 *         as values. For ExtrinsicObject, the objectType key is stored as
	 *         "ExtrinsicObject" rather than the objectType of the repository
	 *         items.
	 */
	public HashMap<String, List<String>> sortIdsByObjectType(List<?> registryObjectIds) throws RegistryException {
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		Statement stmt = null;

		try {
			if (registryObjectIds.size() > 0) {
				stmt = context.getConnection().createStatement();

				StringBuffer str = new StringBuffer("SELECT id, objectType FROM " + getTableName() + " WHERE id IN ( ");

				Iterator<?> iter = registryObjectIds.iterator();

				while (iter.hasNext()) {
					String id = (String) iter.next();
					str.append("'");
					str.append(id);

					if (iter.hasNext()) {
						str.append("', ");
					} else {
						str.append("' )");
					}
				}

				log.trace(ServerResourceBundle.getInstance().getString("message.stmtEquals", new Object[] { str }));

				ResultSet rs = stmt.executeQuery(str.toString());

				ArrayList<String> adhocQuerysIds = new ArrayList<String>();
				ArrayList<String> associationsIds = new ArrayList<String>();
				ArrayList<String> auditableEventsIds = new ArrayList<String>();
				ArrayList<String> classificationsIds = new ArrayList<String>();
				ArrayList<String> classificationSchemesIds = new ArrayList<String>();
				ArrayList<String> classificationNodesIds = new ArrayList<String>();
				ArrayList<String> externalIdentifiersIds = new ArrayList<String>();
				ArrayList<String> externalLinksIds = new ArrayList<String>();
				ArrayList<String> extrinsicObjectsIds = new ArrayList<String>();
				ArrayList<String> federationsIds = new ArrayList<String>();
				ArrayList<String> organizationsIds = new ArrayList<String>();
				ArrayList<String> registrysIds = new ArrayList<String>();
				ArrayList<String> registryPackagesIds = new ArrayList<String>();
				ArrayList<String> serviceBindingsIds = new ArrayList<String>();
				ArrayList<String> servicesIds = new ArrayList<String>();
				ArrayList<String> specificationLinksIds = new ArrayList<String>();
				ArrayList<String> subscriptionsIds = new ArrayList<String>();
				ArrayList<String> usersIds = new ArrayList<String>();

				while (rs.next()) {
					String id = rs.getString(1);
					String objectType = rs.getString(2);

					// log.info(ServerResourceBundle.getInstance().getString("message.objectType!!!!!!!",
					// new Object[]{objectType}));
					if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AdhocQuery)) {
						adhocQuerysIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Association)) {
						associationsIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AuditableEvent)) {
						auditableEventsIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Classification)) {
						classificationsIds.add(id);
					} else if (objectType
							.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationScheme)) {
						classificationSchemesIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode)) {
						classificationNodesIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier)) {
						externalIdentifiersIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalLink)) {
						externalLinksIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Federation)) {
						federationsIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Organization)) {
						organizationsIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Registry)) {
						registrysIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage)) {
						registryPackagesIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ServiceBinding)) {
						serviceBindingsIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Service)) {
						servicesIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_SpecificationLink)) {
						specificationLinksIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Subscription)) {
						subscriptionsIds.add(id);
					} else if (objectType.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_User)) {
						usersIds.add(id);
					} else {
						// TODO: Fix dangerous assumption that is is an
						// ExtrinsicObject
						// Need to compare if objectType is a subType of
						// ExtrinsicObject or not
						extrinsicObjectsIds.add(id);
					}
				}

				// end looping ResultSet
				// Now put the List of id of varios RO type into the HashMap
				if (adhocQuerysIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AdhocQuery, adhocQuerysIds);
				}

				if (associationsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Association, associationsIds);
				}

				if (auditableEventsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AuditableEvent, auditableEventsIds);
				}

				if (classificationsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Classification, classificationsIds);
				}

				if (classificationSchemesIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationScheme, classificationSchemesIds);
				}

				if (classificationNodesIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode, classificationNodesIds);
				}

				if (externalIdentifiersIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier, externalIdentifiersIds);
				}

				if (externalLinksIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalLink, externalLinksIds);
				}

				if (federationsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Federation, federationsIds);
				}

				if (organizationsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Organization, organizationsIds);
				}

				if (registrysIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Registry, registrysIds);
				}

				if (registryPackagesIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage, registryPackagesIds);
				}

				if (serviceBindingsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ServiceBinding, serviceBindingsIds);
				}

				if (servicesIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Service, servicesIds);
				}

				if (specificationLinksIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_SpecificationLink, specificationLinksIds);
				}

				if (subscriptionsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Subscription, subscriptionsIds);
				}

				if (usersIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_User, usersIds);
				}

				if (extrinsicObjectsIds.size() > 0) {
					map.put(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject, extrinsicObjectsIds);
				}
			}

			// end if checking the size of registryObjectsIds
		} catch (SQLException e) {
			log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
			throw new RegistryException(e);
		} finally {
			closeStatement(stmt);
		}

		return map;
	}

	/**
	 * Return true if the RegistryObject exist
	 */
	public boolean registryObjectExist(String id) throws RegistryException {
		PreparedStatement stmt = null;

		try {
			String sql = "SELECT id from RegistryObject where id=?";
			stmt = context.getConnection().prepareStatement(sql);
			stmt.setString(1, id);

			ResultSet rs = stmt.executeQuery();
			boolean result = false;

			if (rs.next()) {
				result = true;
			}

			return result;
		} catch (SQLException e) {
			throw new RegistryException(e);
		} finally {
			closeStatement(stmt);
		}
	}

	/**
	 * Updates any passed RegistryObjects if they already exists. Called by
	 * insert to handle implicit update of existing objects.
	 * 
	 * @return List of RegistryObjects that are not existing
	 */
	protected List<?> processExistingObjects(@SuppressWarnings("rawtypes") List ros) throws RegistryException {
		BindingUtility bindingUtility = BindingUtility.getInstance();

		@SuppressWarnings("unchecked")
		List<String> notExistIds = identifiablesExist(
		// getTableName() is the one which is the overidding one of subclass DAO
				bindingUtility.getIdsFromRegistryObjectTypes(ros), getTableName());
		List<RegistryObjectType> notExistROs = bindingUtility.getRegistryObjectsFromIds(ros, notExistIds);
		ArrayList<RegistryObjectType> existingROs = new ArrayList<RegistryObjectType>();
		@SuppressWarnings("unchecked")
		Iterator<IdentifiableType> rosIter = ros.iterator();

		while (rosIter.hasNext()) {
			RegistryObjectType ro = (RegistryObjectType) rosIter.next();

			if (!notExistROs.contains(ro)) {
				existingROs.add(ro);
			}
		}

		update(existingROs);

		return notExistROs;
	}

	/**
	 * Creates an unitialized binding object for the type supported by this DAO.
	 */
	Object createObject() throws JAXBException {
		RegistryObjectType ebRegistryObjectType = BindingUtility.getInstance().rimFac.createRegistryObjectType();

		return ebRegistryObjectType;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadObject(Object obj, ResultSet rs) throws RegistryException {
		try {
			if (!(obj instanceof RegistryObjectType)) {
				throw new RegistryException(ServerResourceBundle.getInstance().getString(
						"message.RegistryobjectTypeExpected", new Object[] { obj }));
			}

			RegistryObjectType ebRegistryObjectType = (RegistryObjectType) obj;

			super.loadObject(ebRegistryObjectType, rs);

			ClassificationDAO classificationDAO = new ClassificationDAO(context);
			classificationDAO.setParent(ebRegistryObjectType);
			DescriptionDAO descriptionDAO = new DescriptionDAO(context);
			descriptionDAO.setParent(ebRegistryObjectType);
			ExternalIdentifierDAO externalIdentifierDAO = new ExternalIdentifierDAO(context);
			externalIdentifierDAO.setParent(ebRegistryObjectType);
			NameDAO nameDAO = new NameDAO(context);
			nameDAO.setParent(ebRegistryObjectType);

			String id = rs.getString("id");

			String lid = rs.getString("lid");
			if (lid != null) {
				// TODO:versioning: remove this check as persistent objects
				// should always have a lid
				ebRegistryObjectType.setLid(lid);
			}

			String objectType = rs.getString("objectType");
			ebRegistryObjectType.setObjectType(objectType);

			InternationalStringType name = nameDAO.getNameByParent(id);
			ebRegistryObjectType.setName(name);

			InternationalStringType desc = descriptionDAO.getDescriptionByParent(id);
			ebRegistryObjectType.setDescription(desc);

			String status = rs.getString("status");
			if (status.equals("null")) {
				// Ignore earlier corruption of the database, losing whatever
				// status was before that problem occurred
				status = BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted;

				// TODO: unfortunately, can't turn read operation into a
				// write... (best fix probably a SQL script)
				// updateStatus(ro, status);
			}
			ebRegistryObjectType.setStatus(status);

			// Now set VersionInfo
			VersionInfoType versionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
			String versionName = rs.getString("versionName");
			if (versionName != null) {
				versionInfo.setVersionName(versionName);
			}

			String comment = rs.getString("comment_");
			if (comment != null) {
				versionInfo.setComment(comment);
			}
			ebRegistryObjectType.setVersionInfo(versionInfo);

			boolean returnComposedObjects = context.getResponseOption().isReturnComposedObjects();

			if (returnComposedObjects) {
				List classifications = classificationDAO.getByParent();
				ebRegistryObjectType.getClassification().addAll(classifications);

				List extIds = externalIdentifierDAO.getByParent();
				ebRegistryObjectType.getExternalIdentifier().addAll(extIds);
			}
		} catch (SQLException e) {
			log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
			throw new RegistryException(e);
		}
	}

	private void getRegistryObjectsIdsFromResultSet(ResultSet rs, int startIndex, int maxResults,
			StringBuffer adhocQuerys, StringBuffer associations, StringBuffer auEvents, StringBuffer classifications,
			StringBuffer schemes, StringBuffer classificationNodes, StringBuffer externalIds,
			StringBuffer externalLinks, StringBuffer extrinsicObjects, StringBuffer federations,
			StringBuffer organizations, StringBuffer registrys, StringBuffer packages, StringBuffer serviceBindings,
			StringBuffer services, StringBuffer specificationLinks, StringBuffer subscriptions, StringBuffer users,
			StringBuffer persons) throws SQLException, RegistryException {
		HashSet<String> processed = new HashSet<String>();

		if (startIndex > 0) {
			// calling rs.next() is a workaround for some drivers, such
			// as Derby's, that do not set the cursor during call to
			// rs.relative(...)
			rs.next();
			@SuppressWarnings("unused")
			boolean onRow = rs.relative(startIndex - 1);
		}

		int cnt = 0;
		while (rs.next()) {
			String id = rs.getString("id");

			// Only process if not already processed
			// This avoid OutOfMemoryError when huge number of objects match
			// Currently this happens when name and desc are null and their
			// predicates get pruned but the tablename stays.
			// TODO: Fix query pruning so tableName is pruned if not used.
			if (!(processed.contains(id))) {
				cnt++;
				String type = rs.getString("objectType");
				// System.err.println("id=" + id + " objectType=" + type +
				// " extrinsicObjects=" + extrinsicObjects);

				// log.info(ServerResourceBundle.getInstance().getString("message.objectType=''",
				// new Object[]{type}));
				if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AdhocQuery)) {
					if (adhocQuerys.length() == 0) {
						adhocQuerys.append("'" + id + "'");
					} else {
						adhocQuerys.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Association)) {
					if (associations.length() == 0) {
						associations.append("'" + id + "'");
					} else {
						associations.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_AuditableEvent)) {
					if (auEvents.length() == 0) {
						auEvents.append("'" + id + "'");
					} else {
						auEvents.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Classification)) {
					if (classifications.length() == 0) {
						classifications.append("'" + id + "'");
					} else {
						classifications.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationNode)) {
					if (classificationNodes.length() == 0) {
						classificationNodes.append("'" + id + "'");
					} else {
						classificationNodes.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ClassificationScheme)) {
					if (schemes.length() == 0) {
						schemes.append("'" + id + "'");
					} else {
						schemes.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier)) {
					if (externalIds.length() == 0) {
						externalIds.append("'" + id + "'");
					} else {
						externalIds.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExternalLink)) {
					if (externalLinks.length() == 0) {
						externalLinks.append("'" + id + "'");
					} else {
						externalLinks.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject)) {
					if (extrinsicObjects.length() == 0) {
						extrinsicObjects.append("'" + id + "'");
					} else {
						extrinsicObjects.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Federation)) {
					if (federations.length() == 0) {
						federations.append("'" + id + "'");
					} else {
						federations.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Organization)) {
					if (organizations.length() == 0) {
						organizations.append("'" + id + "'");
					} else {
						organizations.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Registry)) {
					if (registrys.length() == 0) {
						registrys.append("'" + id + "'");
					} else {
						registrys.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_RegistryPackage)) {
					if (packages.length() == 0) {
						packages.append("'" + id + "'");
					} else {
						packages.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_ServiceBinding)) {
					if (serviceBindings.length() == 0) {
						serviceBindings.append("'" + id + "'");
					} else {
						serviceBindings.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Service)) {
					if (services.length() == 0) {
						services.append("'" + id + "'");
					} else {
						services.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_SpecificationLink)) {
					if (specificationLinks.length() == 0) {
						specificationLinks.append("'" + id + "'");
					} else {
						specificationLinks.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Subscription)) {
					if (subscriptions.length() == 0) {
						subscriptions.append("'" + id + "'");
					} else {
						subscriptions.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_User)) {
					if (users.length() == 0) {
						users.append("'" + id + "'");
					} else {
						users.append(",'" + id + "'");
					}
				} else if (type.equalsIgnoreCase(BindingUtility.CANONICAL_OBJECT_TYPE_ID_Person)) {
					if (persons.length() == 0) {
						persons.append("'" + id + "'");
					} else {
						persons.append(",'" + id + "'");
					}
				} else {
					// Type is user defined. Table could be either
					// ExtrinsicObject or ExternalLink
					SQLPersistenceManagerImpl pm = SQLPersistenceManagerImpl.getInstance();

					ArrayList<String> queryParams = new ArrayList<String>();
					queryParams.add(id.toUpperCase());
					ExtrinsicObjectType eo = (ExtrinsicObjectType) pm.getRegistryObjectMatchingQuery(context,
							"SELECT * from ExtrinsicObject where UPPER(id) = ?", queryParams, "ExtrinsicObject");

					if (eo != null) {
						if (extrinsicObjects.length() == 0) {
							extrinsicObjects.append("'" + id + "'");
						} else {
							extrinsicObjects.append(",'" + id + "'");
						}
					} else {
						ExternalLinkType el = (ExternalLinkType) pm.getRegistryObjectMatchingQuery(context,
								"SELECT * from ExternalLink where UPPER(id) = ?", queryParams, "ExternalLink");

						if (el != null) {
							if (externalLinks.length() == 0) {
								externalLinks.append("'" + id + "'");
							} else {
								externalLinks.append(",'" + id + "'");
							}
						} else {
							throw new RegistryException(ServerResourceBundle.getInstance().getString(
									"message.unknownObjectType", new Object[] { type }));
						}
					}
				}
				processed.add(id);

				if (cnt == maxResults) {
					break;
				}
			}
		}

		if (cnt > 1000) {
			log.warn(ServerResourceBundle.getInstance().getString("message.WarningExcessiveResultSetSizeQUery",
					new Object[] { new Integer(cnt) }));
		}
	}

	/**
	 * Gets the List of binding objects for specified ResultSet. This method
	 * return leaf object types while the base class version returns
	 * RegistryObjects.
	 * 
	 */
	public List<?> getObjectsHetero(ResultSet rs, int startIndex, int maxResults) throws RegistryException {
		ArrayList<Object> res = new ArrayList<Object>();
		String sql = null;

		StringBuffer adhocQuerysIds = new StringBuffer();
		StringBuffer associationsIds = new StringBuffer();
		StringBuffer auditableEventsIds = new StringBuffer();
		StringBuffer classificationsIds = new StringBuffer();
		StringBuffer schemesIds = new StringBuffer();
		StringBuffer classificationNodesIds = new StringBuffer();
		StringBuffer externalIdsIds = new StringBuffer();
		StringBuffer externalLinksIds = new StringBuffer();
		StringBuffer extrinsicObjectsIds = new StringBuffer();
		StringBuffer federationsIds = new StringBuffer();
		StringBuffer organizationsIds = new StringBuffer();
		StringBuffer registrysIds = new StringBuffer();
		StringBuffer packagesIds = new StringBuffer();
		StringBuffer serviceBindingsIds = new StringBuffer();
		StringBuffer servicesIds = new StringBuffer();
		StringBuffer specificationLinksIds = new StringBuffer();
		StringBuffer subscriptionsIds = new StringBuffer();
		StringBuffer usersIds = new StringBuffer();
		StringBuffer personsIds = new StringBuffer();

		Statement stmt = null;

		try {
			stmt = context.getConnection().createStatement();

			getRegistryObjectsIdsFromResultSet(rs, startIndex, maxResults, adhocQuerysIds, associationsIds,
					auditableEventsIds, classificationsIds, schemesIds, classificationNodesIds, externalIdsIds,
					externalLinksIds, extrinsicObjectsIds, federationsIds, organizationsIds, registrysIds, packagesIds,
					serviceBindingsIds, servicesIds, specificationLinksIds, subscriptionsIds, usersIds, personsIds);

			ResultSet leafObjectsRs = null;

			if (adhocQuerysIds.length() > 0) {
				AdhocQueryDAO ahqDAO = new AdhocQueryDAO(context);
				sql = "SELECT * FROM " + ahqDAO.getTableName() + " WHERE id IN (" + adhocQuerysIds + ")";

				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(ahqDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (associationsIds.length() > 0) {
				AssociationDAO assDAO = new AssociationDAO(context);
				sql = "SELECT * FROM " + assDAO.getTableName() + " WHERE id IN (" + associationsIds + ")";

				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(assDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (auditableEventsIds.length() > 0) {
				AuditableEventDAO aeDAO = new AuditableEventDAO(context);
				sql = "SELECT * FROM " + aeDAO.getTableName() + " WHERE id IN (" + auditableEventsIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(aeDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (classificationsIds.length() > 0) {
				ClassificationDAO classificationDAO = new ClassificationDAO(context);
				sql = "SELECT * FROM " + classificationDAO.getTableName() + " WHERE id IN (" + classificationsIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(classificationDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (classificationNodesIds.length() > 0) {
				ClassificationNodeDAO nodeDAO = new ClassificationNodeDAO(context);
				sql = "SELECT * FROM " + nodeDAO.getTableName() + " WHERE id IN (" + classificationNodesIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(nodeDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (schemesIds.length() > 0) {
				ClassificationSchemeDAO schemeDAO = new ClassificationSchemeDAO(context);
				sql = "SELECT * FROM " + schemeDAO.getTableName() + " WHERE id IN (" + schemesIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(schemeDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (externalIdsIds.length() > 0) {
				ExternalIdentifierDAO externalIdDAO = new ExternalIdentifierDAO(context);
				sql = "SELECT * FROM " + externalIdDAO.getTableName() + " WHERE id IN (" + externalIdsIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(externalIdDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (externalLinksIds.length() > 0) {
				ExternalLinkDAO externalLinkDAO = new ExternalLinkDAO(context);
				sql = "SELECT * FROM " + externalLinkDAO.getTableName() + " WHERE id IN (" + externalLinksIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(externalLinkDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (extrinsicObjectsIds.length() > 0) {
				ExtrinsicObjectDAO extrinsicObjectDAO = new ExtrinsicObjectDAO(context);
				sql = "SELECT * FROM " + extrinsicObjectDAO.getTableName() + " WHERE id IN (" + extrinsicObjectsIds
						+ ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(extrinsicObjectDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (federationsIds.length() > 0) {
				FederationDAO fedDAO = new FederationDAO(context);
				sql = "SELECT * FROM " + fedDAO.getTableName() + " WHERE id IN (" + federationsIds + ")";

				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(fedDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (organizationsIds.length() > 0) {
				OrganizationDAO organizationDAO = new OrganizationDAO(context);
				sql = "SELECT * FROM " + organizationDAO.getTableName() + " WHERE id IN (" + organizationsIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(organizationDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (personsIds.length() > 0) {
				PersonDAO personDAO = new PersonDAO(context);
				sql = "SELECT * FROM " + personDAO.getTableName() + " WHERE id IN (" + personsIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(personDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (registrysIds.length() > 0) {
				RegistryDAO regDAO = new RegistryDAO(context);
				sql = "SELECT * FROM " + regDAO.getTableName() + " WHERE id IN (" + registrysIds + ")";

				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(regDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (packagesIds.length() > 0) {
				RegistryPackageDAO pkgDAO = new RegistryPackageDAO(context);
				sql = "SELECT * FROM " + pkgDAO.getTableName() + " WHERE id IN (" + packagesIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(pkgDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (serviceBindingsIds.length() > 0) {
				ServiceBindingDAO serviceBindingDAO = new ServiceBindingDAO(context);
				sql = "SELECT * FROM " + serviceBindingDAO.getTableName() + " WHERE id IN (" + serviceBindingsIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(serviceBindingDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (servicesIds.length() > 0) {
				ServiceDAO serviceDAO = new ServiceDAO(context);
				sql = "SELECT * FROM " + serviceDAO.getTableName() + " WHERE id IN (" + servicesIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(serviceDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (specificationLinksIds.length() > 0) {
				SpecificationLinkDAO specLinkDAO = new SpecificationLinkDAO(context);
				sql = "SELECT * FROM " + specLinkDAO.getTableName() + " WHERE id IN (" + specificationLinksIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(specLinkDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (subscriptionsIds.length() > 0) {
				SubscriptionDAO subDAO = new SubscriptionDAO(context);
				sql = "SELECT * FROM " + subDAO.getTableName() + " WHERE id IN (" + subscriptionsIds + ")";

				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(subDAO.getObjects(leafObjectsRs, 0, -1));
			}

			if (usersIds.length() > 0) {
				UserDAO userDAO = new UserDAO(context);
				sql = "SELECT * FROM " + userDAO.getTableName() + " WHERE id IN (" + usersIds + ")";
				leafObjectsRs = stmt.executeQuery(sql);
				res.addAll(userDAO.getObjects(leafObjectsRs, 0, -1));
			}

		} catch (SQLException e) {
			log.error(ServerResourceBundle.getInstance().getString("message.CaughtException"), e);
			throw new RegistryException(e);
		} finally {
			closeStatement(stmt);
		}

		return res;
	}

	/**
	 * Get a HashMap with registry object id as key and owner id as value
	 */
	public HashMap<String, String> getOwnersMap(List<String> ids) throws RegistryException {
		Statement stmt = null;
		List<?> resultSets = null;
		HashMap<String, String> ownersMap = new HashMap<String, String>();

		final String prefixPred = "SELECT ao.id, ae.user_, max(concat(ae.timeStamp_, ae.eventType)) FROM AuditableEvent ae, AffectedObject ao WHERE ao.eventId = ae.id";
		final String suffixPred = " AND (ae.eventType = '" + BindingUtility.CANONICAL_EVENT_TYPE_ID_Created
				+ "' OR ae.eventType = '" + BindingUtility.CANONICAL_EVENT_TYPE_ID_Versioned + "' OR ae.eventType = '"
				+ BindingUtility.CANONICAL_EVENT_TYPE_ID_Deleted + "' OR ae.eventType = '"
				+ BindingUtility.CANONICAL_EVENT_TYPE_ID_Relocated + "') GROUP BY ao.id, ae.user_";

		if (ids.size() == 0) {
			return ownersMap;
		}

		try {

			if (ids.size() == 1) {
				// Optmization for 1 term case
				stmt = context.getConnection().createStatement();
				StringBuffer query = new StringBuffer(prefixPred);
				query.append(" AND ao.id = '").append(ids.get(0)).append("'");
				query.append(suffixPred);

				ResultSet rs = stmt.executeQuery(query.toString());

				// contains one line for each id and user with latest event
				while (rs.next()) {

					if (!rs.getString(3).endsWith(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deleted))
						ownersMap.put(rs.getString(1), rs.getString(2));
				}

				// while (rs.next()) {
				// ownersMap.put(rs.getString(1), rs.getString(2));
				// }
			} else {
				// This will handle unlimited terms using buffered Selects
				StringBuffer query = new StringBuffer(prefixPred);
				query.append(" AND ao.id IN ( $InClauseTerms ) ");
				query.append(suffixPred);


				resultSets = executeBufferedSelectWithINClause(query.toString(), ids, inClauseTermLimit);
				
				Iterator<?> resultsSetsIter = resultSets.iterator();
				
				while (resultsSetsIter.hasNext()) {
					ResultSet rs = (ResultSet) resultsSetsIter.next();

					// contains one line for each id and user with latest event
					while (rs.next()) {

						if (!rs.getString(3).endsWith(BindingUtility.CANONICAL_EVENT_TYPE_ID_Deleted))
							ownersMap.put(rs.getString(1), rs.getString(2));
					}

					// while (rs.next()) {
					// ownersMap.put(rs.getString(1), rs.getString(2));
					// }
				}
			}

			return ownersMap;
		} catch (SQLException e) {
			log.error(ServerResourceBundle.getInstance().getString("message.CaughtException"), e);
			throw new RegistryException(e);
		} finally {
			if (stmt != null) {
				closeStatement(stmt);
			}

			if (resultSets != null) {
				Iterator<?> resultsSetsIter = resultSets.iterator();
				while (resultsSetsIter.hasNext()) {
					try {
						ResultSet rs = (ResultSet) resultsSetsIter.next();
						Statement stmt2 = rs.getStatement();
						closeStatement(stmt2);
					} catch (SQLException e) {
						log.error(e, e);
					}
				}
			}
		}
	}

	/**
	 * Update the status of specified objects (homogenous collection) to the
	 * specified status.
	 * 
	 * @param statusUnchanged
	 *            if an id in registryObjectIds is in this ArrayList, no
	 *            AuditableEvent generated for that RegistryObject
	 */
	public void updateStatus(RegistryObjectType ro, String status) throws RegistryException {
		Statement stmt = null;

		try {
			stmt = context.getConnection().createStatement();

			String str = "UPDATE " + getTableName() + " SET status = '" + status + "' WHERE id = '" + ro.getId() + "'";

			log.trace("stmt = " + str);
			stmt.execute(str);
		} catch (SQLException e) {
			log.error(ServerResourceBundle.getInstance().getString("message.CaughtException"), e);
			throw new RegistryException(e);
		} finally {
			closeStatement(stmt);
		}
	}

}

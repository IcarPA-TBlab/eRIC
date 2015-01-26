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
package it.cnr.icar.eric.server.lcm;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.common.CommonProperties;
import it.cnr.icar.eric.common.RegistryResponseHolder;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.UUIDFactory;
import it.cnr.icar.eric.common.exceptions.ObjectsNotFoundException;
import it.cnr.icar.eric.common.exceptions.QuotaExceededException;
import it.cnr.icar.eric.common.exceptions.UnauthorizedRequestException;
import it.cnr.icar.eric.common.spi.LifeCycleManager;
import it.cnr.icar.eric.common.spi.RequestContext;
import it.cnr.icar.eric.server.cms.CMSManager;
import it.cnr.icar.eric.server.cms.CMSManagerImpl;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.lcm.quota.QuotaServiceImpl;
import it.cnr.icar.eric.server.lcm.relocation.RelocationProcessor;
import it.cnr.icar.eric.server.repository.RepositoryItemKey;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.security.authentication.CertificateAuthority;
import it.cnr.icar.eric.server.security.authorization.AuthorizationResult;
import it.cnr.icar.eric.server.security.authorization.AuthorizationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * Implementation of the LifeCycleManager interface
 * 
 * @see
 * @author Farrukh S. Najmi
 * @author Adrian Chong
 * 
 *         TODO: Replace exception-handling code with calls to
 *         util.createRegistryResponseFromThrowable() where appropriate.
 */
public class LifeCycleManagerImpl implements LifeCycleManager {
	/**
	 * @link
	 * @shapeType PatternLink
	 * @pattern Singleton
	 * @supplierRole Singleton factory
	 */

	/* # private LifeCycleManagerImpl _objectManagerImpl; */
	private static LifeCycleManagerImpl instance = null;
	private static BindingUtility bu = BindingUtility.getInstance();
	private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();

	/**
	 * 
	 * @associates 
	 *             <{it.cnr.icar.eric.server.persistence.PersistenceManagerImpl}
	 *             >
	 */
	it.cnr.icar.eric.server.persistence.PersistenceManager pm = it.cnr.icar.eric.server.persistence.PersistenceManagerFactory
			.getInstance().getPersistenceManager();
	/**
	 * 
	 * @associates <{it.cnr.icar.eric.common.QueryManagerImpl}>
	 */
	it.cnr.icar.eric.common.spi.QueryManager qm = it.cnr.icar.eric.common.spi.QueryManagerFactory.getInstance()
			.getQueryManager();
	QuotaServiceImpl qs = QuotaServiceImpl.getInstance();
	it.cnr.icar.eric.server.common.Utility util = it.cnr.icar.eric.server.common.Utility.getInstance();
	RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
	UUIDFactory uf = UUIDFactory.getInstance();

	boolean bypassCMS = false;
	CMSManager cmsm = new CMSManagerImpl(); // CMSManagerFactory.getInstance().getContentManagementServiceManager();

	private static final Log log = LogFactory.getLog(LifeCycleManagerImpl.class);

	protected LifeCycleManagerImpl() {
		bypassCMS = Boolean.valueOf(
				RegistryProperties.getInstance().getProperty("it.cnr.icar.eric.server.lcm.bypassCMS", "false"))
				.booleanValue();

	}

	public synchronized static LifeCycleManagerImpl getInstance() {
		if (instance == null) {
			instance = new LifeCycleManagerImpl();
		}

		return instance;
	}

	/**
	 * Submits one or more RegistryObjects and one or more repository items. <br>
	 * <br>
	 * Note: One more special feature that is not in the RS spec. version 2. The
	 * SubmitObjectsRequest allows updating objects.If a object of a particular
	 * id already exist, it is updated instead of trying to be inserted.
	 * 
	 * @param idToRepositoryItemMap
	 *            is a HashMap with key that is id of a RegistryObject and value
	 *            that is a RepositoryItem instance.
	 */
	@SuppressWarnings("unused")
	public RegistryResponseType submitObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		SubmitObjectsRequest ebSubmitObjectsRequest = (SubmitObjectsRequest) context.getCurrentRegistryRequest();
		UserType user = context.getUser();
		HashMap<String, Object> idToRepositoryItemMap = context.getRepositoryItemsMap();
		String errorCodeContext = "LifeCycleManagerImpl.submitObjects";
		String errorCode = "unknown";

		RegistryResponseType ebRegistryResponseType = null;
		try {
			calculateEffectiveUser(((ServerRequestContext) context));
			RegistryObjectListType ebRegistryObjectListType = ebSubmitObjectsRequest.getRegistryObjectList();
			
			// insert member objects of RegistryPackages
			@SuppressWarnings("unchecked")
			List<IdentifiableType> ebIdentifiableTypeList = (List<IdentifiableType>) bu
					.getIdentifiableTypeList(ebRegistryObjectListType);
			
			int objsSize = ebIdentifiableTypeList.size();
			for (int i = 0; i < objsSize; i++) {
				IdentifiableType identObj = ebIdentifiableTypeList.get(i);

				if (identObj instanceof RegistryPackageType) {
					insertPackageMembers(ebRegistryObjectListType, (RegistryPackageType) identObj);
				}
			}

			// Split Identifiables by RegistryObjects and ObjectRefs
			bu.getObjectRefsAndRegistryObjects(ebRegistryObjectListType,
					((ServerRequestContext) context).getTopLevelRegistryObjectTypeMap(),
					((ServerRequestContext) context).getObjectRefTypeMap());

			processConfirmationAssociations(((ServerRequestContext) context));

			((ServerRequestContext) context).checkObjects();

			// Auth check must be after checkObjects as it needs objectRefsMap
			// etc.
			// for doing Access Control on references

			checkAuthorizedAll(((ServerRequestContext) context));

			if (!bypassCMS) {
				// Now perform any content cataloging and validation for
				// ExtrinsicObjects
				
				log.trace(ServerResourceBundle.getInstance().getString("message.CallingInvokeServicesAt",
						new Object[] { new Long(System.currentTimeMillis()) }));
				cmsm.invokeServices(((ServerRequestContext) context));
				log.trace(ServerResourceBundle.getInstance().getString("message.DoneCallingInvokeServicesAt",
						new Object[] { new Long(System.currentTimeMillis()) }));
			}

			/*
			 * For RegistryObjects, the DAO will take care which objects already
			 * exist and update them instead
			 */
			log.trace(ServerResourceBundle.getInstance().getString("message.CallingPminsertAt",
					new Object[] { new Long(System.currentTimeMillis()) }));
			List<IdentifiableType> list = new ArrayList<IdentifiableType>();
			list.addAll(((ServerRequestContext) context).getTopLevelRegistryObjectTypeMap().values());
			pm.insert(((ServerRequestContext) context), list);

			log.trace(ServerResourceBundle.getInstance().getString("message.DoneCallingPminsertAt",
					new Object[] { new Long(System.currentTimeMillis()) }));

			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

			if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
				// warning exists
				ebRegistryResponseType.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
			}

			// Must be after CMS since CMS could generate more repository items.
			if ((((ServerRequestContext) context).getRepositoryItemsMap() != null)
					&& (!(((ServerRequestContext) context).getRepositoryItemsMap().isEmpty()))) {
				submitRepositoryItems(((ServerRequestContext) context));
			}

		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (IllegalStateException e) {
			// ?? This is a JAXR spec bug that we do not send an
			// UnauthorizedRequestException
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	/**
	 * Iterates through the members of the RegistryPackage <code>regPkg</code>,
	 * creates and adds a 'HasMember' association for each member of the
	 * RegistryPackages to the RegistryObjectList <code>regObjs</code> together
	 * with the member.
	 * 
	 * @param ebRegistryObjectListType
	 *            RegistryObjectList to append RegistryPackage members and their
	 *            associations.
	 * @param ebRegistryPackageType
	 *            Package to get members from.
	 * @throws javax.xml.bind.JAXBException
	 */
	private void insertPackageMembers(RegistryObjectListType ebRegistryObjectListType,
			RegistryPackageType ebRegistryPackageType) throws JAXBException {

		RegistryObjectListType ebObjectListTypeMember = ebRegistryPackageType.getRegistryObjectList();

		if (ebObjectListTypeMember != null) {
			@SuppressWarnings("unchecked")
			List<IdentifiableType> ebIdentifiableTypeList = (List<IdentifiableType>) bu
					.getIdentifiableTypeList(ebObjectListTypeMember);

			if (ebIdentifiableTypeList.size() > 0) {

				for (int j = 0; j < ebIdentifiableTypeList.size(); j++) {

					RegistryObjectType ebRegistryObjectType = (RegistryObjectType) ebIdentifiableTypeList.get(j);

					if (ebRegistryObjectType instanceof RegistryPackageType) {
						insertPackageMembers(ebRegistryObjectListType, (RegistryPackageType) ebRegistryObjectType);
					}

					String assId = it.cnr.icar.eric.common.Utility.getInstance().createId();

					// create complextype
					AssociationType1 ebAssociationType = BindingUtility.getInstance().rimFac.createAssociationType1();
					ebAssociationType.setId(assId);
					ebAssociationType.setLid(assId);
					ebAssociationType.setAssociationType(CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);
					ebAssociationType.setSourceObject(ebRegistryPackageType.getId());
					ebAssociationType.setTargetObject(ebRegistryObjectType.getId());

					// create element
					JAXBElement<AssociationType1> ebAssociation = BindingUtility.getInstance().rimFac
							.createAssociation(ebAssociationType);

					// create element
					JAXBElement<RegistryObjectType> ebRegistryObject = BindingUtility.getInstance().rimFac
							.createRegistryObject(ebRegistryObjectType);

					// add both elements
					ebRegistryObjectListType.getIdentifiable().add(ebRegistryObject);
					ebRegistryObjectListType.getIdentifiable().add(ebAssociation);
				}
				ebRegistryPackageType.getRegistryObjectList().getIdentifiable().clear();
			}
		}
	}

	/**
	 * Processes Associations looking for Associations that already exist and
	 * are being submitted by source or target owner and are identical in state
	 * to existing Association in registry.
	 * 
	 * ebXML Registry 3.0 hasber discarded association confirmation in favour of
	 * Role Based access control. However, freebXML Registry supports it in an
	 * impl specific manner as this is required by JAXR 1.0 API. This SHOULD be
	 * removed once JAXR 2.0 no longer requires it for ebXML Registry in future.
	 * 
	 * The processing updates the Association to add a special Impl specific
	 * slot to remember the confirmation state change.
	 * 
	 * TODO: Need to do unconfirm when src or target owner removes an
	 * Association they had previously confirmed.
	 */
	private void processConfirmationAssociations(ServerRequestContext context) throws RegistryException {

		try {
			// Make a copy to avoid ConcurrentModificationException
			ArrayList<?> topLevelObjects = new ArrayList<Object>((context)
					.getTopLevelRegistryObjectTypeMap().values());
			Iterator<?> iter = topLevelObjects.iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof AssociationType1) {
					AssociationType1 ass = (AssociationType1) obj;
					HashMap<String, Serializable> slotsMap = bu.getSlotsFromRegistryObject(ass);
					@SuppressWarnings("static-access")
					String beingConfirmed = (String) slotsMap.remove(bu.IMPL_SLOT_ASSOCIATION_IS_BEING_CONFIRMED);
					if ((beingConfirmed != null) && (beingConfirmed.equalsIgnoreCase("true"))) {
						// Need to set slotMap again
						ass.getSlot().clear();
						bu.addSlotsToRegistryObject(ass, slotsMap);

						(context).getConfirmationAssociations().put(ass.getId(), ass);
					}
				}
			}
		} catch (javax.xml.bind.JAXBException e) {
			throw new RegistryException(e);
		}
	}

	/**
	 * Submits any RepositoryItems in ((ServerRequestContext)context).
	 * 
	 * <p>
	 * Checks quotas and fixes IDs as necessary.
	 * 
	 * @param context
	 *            a <code>RequestContext</code>
	 */
	private void submitRepositoryItems(ServerRequestContext context) throws QuotaExceededException, RegistryException {
		qs.checkQuota((context).getUser().getId());

		// fix ri ID to match
		// first ExtrinsicObject (in case where ri is submitted without id)
		// only works for submission of one ri and one ExtrinsicObject
		// correctRepositoryItemId(((ServerRequestContext)context).topLevelObjectsMap.values(),
		// ((ServerRequestContext)context).repositoryItemsMap);

		// It will select which repository items already exist and update
		Map<String, ?> idToNotExistingItemsMap = updateExistingRepositoryItems((context));
		storeRepositoryItems((context), idToNotExistingItemsMap);
	}

	/**
	 * Stores the repository items in idToRepositoryItemMap in the repository
	 * 
	 * @throws RegistryException
	 *             when the items already exist
	 */
	private void storeRepositoryItems(ServerRequestContext context, Map<String, ?> idToRepositoryItemMap) throws RegistryException {
		if (idToRepositoryItemMap != null) {
			Set<String> keySet = idToRepositoryItemMap.keySet();

			if (keySet != null) {
				Iterator<String> iter = keySet.iterator();

				while (iter.hasNext()) {
					String id = iter.next();
					RepositoryItem ri = (RepositoryItem) idToRepositoryItemMap.get(id);

					@SuppressWarnings("unused")
					DataHandler dh = ri.getDataHandler();

					// Inserting the repository item
					rm.insert((context), ri);
				}
			}
		}
	}

	/**
	 * Calculates the effective user to be used as the identity of the
	 * requestor. Implements ability to re-assign user to a different user than
	 * the caller if:
	 * 
	 * a) The actual caller is a RegistryAdministrator role, and b) The request
	 * specifies the CANONICAL_SLOT_LCM_OWNER.
	 * 
	 */
	@SuppressWarnings("static-access")
	void calculateEffectiveUser(ServerRequestContext context) throws RegistryException {
		try {
			UserType caller = (context).getUser();

			// See if CANONICAL_SLOT_LCM_OWNER defined on request
			HashMap<String, Object> slotsMap = bu.getSlotsFromRequest((context).getCurrentRegistryRequest());
			String effectiveUserId = (String) slotsMap.get(bu.CANONICAL_SLOT_LCM_OWNER);
			if (effectiveUserId != null) {
				if (ac.hasRegistryAdministratorRole(caller)) {
					UserType effectiveUser = (UserType) pm.getRegistryObject((context),
							effectiveUserId, "User");
					if (effectiveUser == null) {
						throw new RegistryException(ServerResourceBundle.getInstance().getString(
								"message.specifiedUserNotOwner", new Object[] { effectiveUserId }));
					}
					(context).setUser(effectiveUser);
				} else {
					throw new InvalidRequestException(ServerResourceBundle.getInstance().getString(
							"message.requestSlotInvalid", new Object[] { bu.CANONICAL_SLOT_LCM_OWNER }));
				}
			}
		} catch (javax.xml.bind.JAXBException e) {
			throw new RegistryException(e);
		} catch (InvalidRequestException e) {
			throw new RegistryException(e);
		}
	}

	/**
	 * Updates any RepositoryItems in ((ServerRequestContext)context).
	 * 
	 * <p>
	 * Checks quotas and fixes IDs as necessary.
	 * 
	 * @param context
	 *            a <code>RequestContext</code>
	 */
	@SuppressWarnings("unused")
	private void updateRepositoryItems(ServerRequestContext context) throws QuotaExceededException, RegistryException {
		qs.checkQuota((context).getUser().getId());

		// fix ri ID to match
		// first ExtrinsicObject (in case where ri is submitted
		// without id) only works for submission of one ri and one
		// ExtrinsicObject
		// correctRepositoryItemId(((ServerRequestContext)context).topLevelObjectsMap.values(),
		// ((ServerRequestContext)context).repositoryItemsMap);

		updateRepositoryItems((context),
				(context).getRepositoryItemsMap());
	}

	/**
	 * It should be called by submitObjects() to update existing Repository
	 * Items
	 * 
	 * @return HashMap of id To RepositoryItem, which are not existing
	 */
	private Map<String, ?> updateExistingRepositoryItems(ServerRequestContext context) throws RegistryException {

		// Create two maps to store existing and non-existing items
		HashMap<String, Object> notExistItems = new HashMap<String, Object>();
		HashMap<String, Object> existingItems = new HashMap<String, Object>();

		Iterator<String> itemsIdsIter = (context).getRepositoryItemsMap().keySet().iterator();
		while (itemsIdsIter.hasNext()) {
			String id = itemsIdsIter.next();
			ExtrinsicObjectType eo = (ExtrinsicObjectType) (context).getSubmittedObjectsMap()
					.get(id);
			boolean exists = rm.itemExists(new RepositoryItemKey(eo.getLid(), eo.getContentVersionInfo()
					.getVersionName()));

			if (exists) {
				existingItems.put(id, (context).getRepositoryItemsMap().get(id));
			} else {
				notExistItems.put(id, (context).getRepositoryItemsMap().get(id));
			}
		}

		updateRepositoryItems((context), existingItems);

		return notExistItems;
	}

	/** Approves one or more previously submitted objects */
	@SuppressWarnings({ "unused", "unchecked", "static-access" })
	public RegistryResponseType approveObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		ApproveObjectsRequest req = (ApproveObjectsRequest) context.getCurrentRegistryRequest();
		UserType user = context.getUser();
		String errorCodeContext = "LifeCycleManagerImpl.approveObjects";
		String errorCode = "unknown";

		RegistryResponseType ebRegistryResponseType = null;
		try {
			context = new ServerRequestContext("LifeCycleManagerImpl.approveObjects", req);
			((ServerRequestContext) context).setUser(user);

			checkAuthorizedAll(((ServerRequestContext) context));

			List<String> idList = new java.util.ArrayList<String>();
			// Add explicitly specified oref params
			List<ObjectRefType> ebObjectRefTypeList = bu.getObjectRefTypeListFromObjectRefListType(req.getObjectRefList());

			// Append those orefs specified via ad hoc query param
			ebObjectRefTypeList.addAll((List<ObjectRefType>) ((ServerRequestContext) context).getObjectsRefTypeListFromQueryResults(req
					.getAdhocQuery()));

			Iterator<ObjectRefType> orefsIter = ebObjectRefTypeList.iterator();

			while (orefsIter.hasNext()) {
				ObjectRefType ebObjectRefType = orefsIter.next();
				idList.add(ebObjectRefType.getId());
			}

			pm.updateStatus(((ServerRequestContext) context), idList, bu.CANONICAL_STATUS_TYPE_ID_Approved);
			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

			if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
				// warning exists
				ebRegistryResponseType.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
			}
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	/**
	 * @throws RegistryException
	 *             when the Repository items do not exist
	 */
	private void updateRepositoryItems(ServerRequestContext context, Map<String, ?> idToRepositoryItemMap)
			throws RegistryException {
		if (idToRepositoryItemMap != null) {
			Set<String> keySet = idToRepositoryItemMap.keySet();

			if (keySet != null) {
				Iterator<String> iter = keySet.iterator();

				while (iter.hasNext()) {
					String id = iter.next();
					RepositoryItem roNew = (RepositoryItem) idToRepositoryItemMap.get(id);

					// Updating the repository item
					rm.update((context), roNew);
				}
			}
		}
	}

	public RegistryResponseType updateObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		RegistryResponseType ebRegistryResponseType = null;
		UpdateObjectsRequest req = (UpdateObjectsRequest) ((ServerRequestContext) context).getCurrentRegistryRequest();
		HashMap<String, Object> idToRepositoryMap = ((ServerRequestContext) context).getRepositoryItemsMap();
		@SuppressWarnings("unused")
		UserType user = ((ServerRequestContext) context).getUser();

		try {
			calculateEffectiveUser(((ServerRequestContext) context));

			((ServerRequestContext) context).setRepositoryItemsMap(idToRepositoryMap);

			RegistryObjectListType objs = req.getRegistryObjectList();

			// Split Identifiables by RegistryObjects and ObjectRefs
			bu.getObjectRefsAndRegistryObjects(objs,
					((ServerRequestContext) context).getTopLevelRegistryObjectTypeMap(),
					((ServerRequestContext) context).getObjectRefTypeMap());

			((ServerRequestContext) context).checkObjects();

			// Auth check must be after checkObjects as it needs
			// objectRefsMap etc. for doing Access Control on
			// references
			checkAuthorizedAll(((ServerRequestContext) context));

			if ((((ServerRequestContext) context).getRepositoryItemsMap() != null)
					&& (!(((ServerRequestContext) context).getRepositoryItemsMap().isEmpty()))) {
				updateRepositoryItems(((ServerRequestContext) context),
						((ServerRequestContext) context).getRepositoryItemsMap());
			}

			ArrayList<IdentifiableType> list = new ArrayList<IdentifiableType>();
			list.addAll(((ServerRequestContext) context).getTopLevelRegistryObjectTypeMap().values());
			pm.update(((ServerRequestContext) context), list);

			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

			if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
				// warning exists
				ebRegistryResponseType.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
			}
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (IllegalStateException e) {
			// ?? This is a JAXR spec bug that we do not send an
			// UnauthorizedRequestException
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	/**
	 * Sets the status of specified objects. This is an extension request that
	 * will be adde to ebRR 3.1??
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public RegistryResponseType setStatusOnObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		RegistryResponseType ebRegistryResponseType = null;
		SetStatusOnObjectsRequest ebSetStatusOnObjectsRequest = (SetStatusOnObjectsRequest) ((ServerRequestContext) context)
				.getCurrentRegistryRequest();
		HashMap<String, Object> idToRepositoryMap = ((ServerRequestContext) context).getRepositoryItemsMap();
		UserType ebUserType = ((ServerRequestContext) context).getUser();

		try {
			checkAuthorizedAll(((ServerRequestContext) context));

			List<String> idList = new java.util.ArrayList<String>();
			// Add explicitly specified oref params
			List<ObjectRefType> ebObjectRefTypeList = bu.getObjectRefTypeListFromObjectRefListType(ebSetStatusOnObjectsRequest.getObjectRefList());

			// Append those orefs specified via ad hoc query param
			ebObjectRefTypeList.addAll((List<ObjectRefType>)((ServerRequestContext) context).getObjectsRefTypeListFromQueryResults(ebSetStatusOnObjectsRequest.getAdhocQuery()));

			Iterator<ObjectRefType> ebObjectRefTypeIter = ebObjectRefTypeList.iterator();

			while (ebObjectRefTypeIter.hasNext()) {
				ObjectRefType ebObjectRefType = ebObjectRefTypeIter.next();
				idList.add(ebObjectRefType.getId());
			}

			String statusId = ebSetStatusOnObjectsRequest.getStatus();
			pm.updateStatus(((ServerRequestContext) context), idList, statusId);
			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

			if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
				// warning exists
				ebRegistryResponseType.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
			}
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	/** Deprecates one or more previously submitted objects */
	@SuppressWarnings({ "unused", "unchecked", "static-access" })
	public RegistryResponseType deprecateObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		RegistryResponseType ebRegistryResponseType = null;
		DeprecateObjectsRequest ebDeprecateObjectsRequest = (DeprecateObjectsRequest) ((ServerRequestContext) context)
				.getCurrentRegistryRequest();
		Map<?, ?> idToRepositoryMap = ((ServerRequestContext) context).getRepositoryItemsMap();
		UserType ebUserType = ((ServerRequestContext) context).getUser();

		try {
			checkAuthorizedAll(((ServerRequestContext) context));

			List<String> idList = new java.util.ArrayList<String>();
			// Add explicitly specified oref params
			List<ObjectRefType> ebObjectRefTypeList = bu.getObjectRefTypeListFromObjectRefListType(ebDeprecateObjectsRequest.getObjectRefList());

			// Append those orefs specified via ad hoc query param
			ebObjectRefTypeList.addAll((List<ObjectRefType>)((ServerRequestContext) context).getObjectsRefTypeListFromQueryResults(ebDeprecateObjectsRequest.getAdhocQuery()));

			Iterator<ObjectRefType> ebObjectRefTypeIter = ebObjectRefTypeList.iterator();

			while (ebObjectRefTypeIter.hasNext()) {
				ObjectRefType ebObjectRefType = ebObjectRefTypeIter.next();
				idList.add(ebObjectRefType.getId());
			}

			pm.updateStatus(((ServerRequestContext) context), idList, bu.CANONICAL_STATUS_TYPE_ID_Deprecated);
			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

			if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
				// warning exists
				ebRegistryResponseType.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
			}
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	@SuppressWarnings({ "unused", "unchecked", "static-access" })
	public RegistryResponseType unDeprecateObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		RegistryResponseType ebRegistryResponseType = null;
		UndeprecateObjectsRequest ebUndeprecateObjectsRequest = (UndeprecateObjectsRequest) ((ServerRequestContext) context)
				.getCurrentRegistryRequest();
		HashMap<String, Object> idToRepositoryMap = ((ServerRequestContext) context).getRepositoryItemsMap();
		UserType ebUserType = ((ServerRequestContext) context).getUser();

		try {
			checkAuthorizedAll(((ServerRequestContext) context));

			List<String> idList = new java.util.ArrayList<String>();
			// Add explicitly specified oref params
			List<ObjectRefType> ebObjectRefTypeList = bu.getObjectRefTypeListFromObjectRefListType(ebUndeprecateObjectsRequest.getObjectRefList());

			// Append those orefs specified via ad hoc query param
			ebObjectRefTypeList.addAll((List<ObjectRefType>)((ServerRequestContext) context).getObjectsRefTypeListFromQueryResults(ebUndeprecateObjectsRequest.getAdhocQuery()));

			Iterator<ObjectRefType> ebObjectRefTypeIter = ebObjectRefTypeList.iterator();

			while (ebObjectRefTypeIter.hasNext()) {
				ObjectRefType ebObjectRefType = ebObjectRefTypeIter.next();
				idList.add(ebObjectRefType.getId());
			}

			pm.updateStatus(((ServerRequestContext) context), idList, bu.CANONICAL_STATUS_TYPE_ID_Submitted);
			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);

			if (((ServerRequestContext) context).getErrorList().getRegistryError().size() > 0) {
				// warning exists
				ebRegistryResponseType.setRegistryErrorList(((ServerRequestContext) context).getErrorList());
			}
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	/**
	 * Removes one or more previously submitted objects from the registry. If
	 * the deletionScope is "DeleteRepositoryItemOnly", it will assume all the
	 * ObjectRef under ObjectRefList is referencing repository items. If the
	 * deletionScope is "DeleteAll", the reference may be either RegistryObject
	 * or repository item. In both case, if the referenced object cannot be
	 * found, RegistryResponse with errors list will be returned.
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public RegistryResponseType removeObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		ServerRequestContext _context = (ServerRequestContext) context;
		RegistryResponseType ebRegistryResponseType = null;
		RemoveObjectsRequest req = (RemoveObjectsRequest) _context.getCurrentRegistryRequest();
		HashMap<String, Object> idToRepositoryMap = _context.getRepositoryItemsMap();
		UserType user = _context.getUser();

		// This request option instructs the server to delete objects even if
		// references exist to them
		boolean forceDelete = false;

		// This request option instructs the server to also delete the network
		// of objects reachable by
		// reference from the objects being deleted. This option is not
		// implemented yet.
		boolean cascadeDelete = false;

		try {
			// Get relevant request slots if any
			HashMap<String, Object> requestSlots = bu.getSlotsFromRequest(req);

			if (requestSlots.containsKey(CanonicalConstants.CANONICAL_SLOT_DELETE_MODE_FORCE)) {
				String val = (String) requestSlots.get(CanonicalConstants.CANONICAL_SLOT_DELETE_MODE_FORCE);
				if (val.trim().equalsIgnoreCase("true")) {
					forceDelete = true;
				}
			}

			if (requestSlots.containsKey(CanonicalConstants.CANONICAL_SLOT_DELETE_MODE_CASCADE)) {
				String val = (String) requestSlots.get(CanonicalConstants.CANONICAL_SLOT_DELETE_MODE_CASCADE);
				if (val.trim().equalsIgnoreCase("true")) {
					cascadeDelete = true;
				}
			}

			List<ObjectRefType> ebObjectRefTypeList = null;
			if (req.getObjectRefList() == null) {
				ObjectRefListType ebObjectRefListType = bu.rimFac.createObjectRefListType();
				req.setObjectRefList(ebObjectRefListType);
			}

			// Add explicitly specified oref params
			ebObjectRefTypeList = bu.getObjectRefTypeListFromObjectRefListType(req.getObjectRefList());

			// Append those orefs specified via ad hoc query param
			ebObjectRefTypeList.addAll((List<ObjectRefType>)_context.getObjectsRefTypeListFromQueryResults(req.getAdhocQuery()));

			Iterator<ObjectRefType> ebObjectRefTypeIter = ebObjectRefTypeList.iterator();

			while (ebObjectRefTypeIter.hasNext()) {
				ObjectRefType ebObjectRefType = ebObjectRefTypeIter.next();
				_context.getObjectRefTypeMap().put(ebObjectRefType.getId(), ebObjectRefType);
			}

			List<String> idList = new ArrayList<String>(_context.getObjectRefTypeMap().keySet());
			pm.updateIdToLidMap(_context, _context.getObjectRefTypeMap().keySet(), "RegistryObject");
			Set<String> idsNotInRegistry = _context.getIdsNotInRegistry(_context.getObjectRefTypeMap().keySet());
			if (idsNotInRegistry.size() > 0) {
				throw new ObjectsNotFoundException(new ArrayList<String>(idsNotInRegistry));
			}
			checkAuthorizedAll(_context);

			String deletionScope = BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll;

			if (req.getDeletionScope() != null) {
				deletionScope = req.getDeletionScope();
			}

			// DeletionScope=DeleteRepositoryItemOnly. If any repository item
			// does not exist, it will stop
			if (deletionScope.equals(BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteRepositoryItemOnly)) {
				List<String> notExist = rm.itemsExist(idList);

				if (notExist.size() > 0) {
					throw new ObjectsNotFoundException(notExist);
				}

				// Should RepositoryItem be deleted even when referencesExist??
				// if (!forceDelete) {
				// pm.checkIfReferencesExist((ServerRequestContext) context,
				// idList);
				// }
				rm.delete(idList);
			} else if (deletionScope.equals(BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll)) {
				// find out which id is not an id of a repository item (i.e.
				// referencing RO only
				List<String> nonItemsIds = rm.itemsExist(idList);

				// find out which id is an id of a repository item
				List<String> itemsIds = new java.util.ArrayList<String>();
				Iterator<String> idListIt = idList.iterator();

				while (idListIt.hasNext()) {
					String id = idListIt.next();

					if (!nonItemsIds.contains(id)) {
						itemsIds.add(id);
					}
				}

				if (!forceDelete) {
					pm.checkIfReferencesExist((ServerRequestContext) context, idList);
				}

				// Delete the repository items
				rm.delete(itemsIds);

				// Delete all ROs with the ids
				pm.delete(_context, ebObjectRefTypeList);
			} else {
				throw new RegistryException(ServerResourceBundle.getInstance().getString(
						"message.undefinedDeletionScope"));
			}

			ebRegistryResponseType = bu.rsFac.createRegistryResponseType();
			ebRegistryResponseType.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;
	}

	/** Sends an impl specific protocol extension request. */
	public RegistryResponseHolder extensionRequest(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		RegistryResponseHolder respHolder = null;
		RegistryRequestType ebRegistryRequestType = ((ServerRequestContext) context)
				.getCurrentRegistryRequest();
		HashMap<String, Object> idToRepositoryMap = ((ServerRequestContext) context).getRepositoryItemsMap();
		UserType ebUserType = ((ServerRequestContext) context).getUser();

		try {
			HashMap<String, Object> slotsMap = bu.getSlotsFromRequest(ebRegistryRequestType);
			String signCertProtocol = (String) slotsMap.get(BindingUtility.FREEBXML_REGISTRY_PROTOCOL_SIGNCERT);
			
			if ("true".equalsIgnoreCase(signCertProtocol)) {
				respHolder = CertificateAuthority.getInstance().signCertificateRequest(ebUserType, ebRegistryRequestType, idToRepositoryMap);
			} else {
				throw new RegistryException("Unknown extensionRequest");
			}
			
		} catch (RegistryException e) {
			throw e;
		} catch (Exception e) {
			throw new RegistryException(e);
		}

		return respHolder;
	}

	/*
	 * Fix Repository item's ID to match ID in first associated ExtrinsicObject.
	 * (in case where ri is submitted without id or id doesn't match id of
	 * ExtrinsicObject). Currently Only works for submission of one Repository
	 * item and its associated ExtrinsicObject. Called in updateObjects() and
	 * submitObjects().
	 * 
	 * Commented as it is trying to fix an error condition and caused problems
	 * in VersioningTest. private void correctRepositoryItemId(Collection objs,
	 * Map idToRepositoryItemMap) { if (objs.size() == 1) { Object obj =
	 * objs.iterator().next();
	 * 
	 * if (obj instanceof RegistryEntryType) { RegistryEntryType firstRe =
	 * (RegistryEntryType) obj;
	 * 
	 * if ((firstRe != null) && firstRe instanceof ExtrinsicObject) { String
	 * correctId = firstRe.getId();
	 * 
	 * if (idToRepositoryItemMap.size() == 1) { Iterator attachIter =
	 * idToRepositoryItemMap.keySet() .iterator(); String attachIdKey = (String)
	 * attachIter.next(); RepositoryItem attachRi = (RepositoryItem)
	 * idToRepositoryItemMap.get(attachIdKey); String attachId =
	 * attachRi.getId();
	 * 
	 * if ((correctId != null) && !correctId.equals(attachId)) {
	 * System.err.println( "[LifeCycleManager::correctRepositoryItemId()]" +
	 * " RepositoryItem id [" + attachId +
	 * "] does not match Registry Object id [" + correctId + "]");
	 * System.err.println( "[LifeCycleManager::correctRepositoryItemId()] " +
	 * " Updating RepositoryItem id to " + correctId);
	 * 
	 * //remove old key idToRepositoryItemMap.remove(attachRi.getId());
	 * attachRi.setId(correctId);
	 * 
	 * //add new key and ri idToRepositoryItemMap.put(correctId, attachRi); } }
	 * } } } }
	 */

	@SuppressWarnings("unused")
	public RegistryResponseType relocateObjects(RequestContext context) throws RegistryException {
		context = ServerRequestContext.convert(context);
		RegistryResponseType ebRegistryResponseType = null;
		RelocateObjectsRequest ebRelocateObjectsRequest = (RelocateObjectsRequest) ((ServerRequestContext) context)
				.getCurrentRegistryRequest();
		HashMap<String, Object> idToRepositoryMap = ((ServerRequestContext) context).getRepositoryItemsMap();
		UserType ebUserType = ((ServerRequestContext) context).getUser();

		try {
			RelocationProcessor relocationMgr = new RelocationProcessor(((ServerRequestContext) context));
			ebRegistryResponseType = relocationMgr.relocateObjects();
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		((ServerRequestContext) context).commit();
		return ebRegistryResponseType;

	}

	/**
	 * Dumps the old and new IDs in idMap.
	 * 
	 * @param idMap
	 *            a <code>HashMap</code> value
	 */
	@SuppressWarnings("unused")
	private void dumpIdMap(HashMap<String, Object> idMap) {
		Collection<String> ids = idMap.keySet();
		Iterator<String> idsIter = ids.iterator();

		while (idsIter.hasNext()) {
			String key = idsIter.next();
			log.trace(key + "    " + idMap.get(key));
		}

		log.trace(ServerResourceBundle.getInstance().getString("message.ObjectsFound",
				new Object[] { new Integer(ids.size()) }));
	}

	/**
	 * Checks that the user in the current context is authorized to do
	 * everything necessary to process the current request.
	 * 
	 * @param context
	 *            a <code>RequestContext</code> value
	 * @exception UnauthorizedRequestException
	 *                if an error occurs
	 * @exception RegistryException
	 *                if an error occurs
	 */
	private void checkAuthorizedAll(ServerRequestContext context) throws UnauthorizedRequestException,
			RegistryException {

		boolean noRegRequired = Boolean.valueOf(
				CommonProperties.getInstance().getProperty("eric.common.noUserRegistrationRequired", "false"))
				.booleanValue();

		if (!noRegRequired) {
			checkAuthorized((context), AuthorizationResult.PERMIT_NONE
					| AuthorizationResult.PERMIT_SOME);
		}
	}

	/**
	 * Checks that the user in the current context is authorized to do
	 * everything except the specified authorization levels.
	 * 
	 * @param context
	 *            a <code>RequestContext</code> value
	 * @param throwExceptionOn
	 *            Flags specifying when to throw exceptions
	 * @exception UnauthorizedRequestException
	 *                if an error occurs
	 * @exception RegistryException
	 *                if an error occurs
	 */
	private void checkAuthorized(ServerRequestContext context, int throwExceptionOn)
			throws UnauthorizedRequestException, RegistryException {
		AuthorizationResult authRes = AuthorizationServiceImpl.getInstance().checkAuthorization(
				(context));
		authRes.throwExceptionOn(throwExceptionOn);
	}
}

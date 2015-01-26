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
package it.cnr.icar.eric.server.query;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CanonicalSchemes;
import it.cnr.icar.eric.common.CommonResourceBundle;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryPlugin;
import it.cnr.icar.eric.common.spi.RequestContext;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.cms.CMSManager;
import it.cnr.icar.eric.server.cms.CMSManagerImpl;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.plugin.AbstractPluginManager;
import it.cnr.icar.eric.server.query.federation.FederatedQueryManager;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.saml.SAMLRegistrar;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.security.authorization.AuthorizationResult;
import it.cnr.icar.eric.server.security.authorization.AuthorizationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.StringReader;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.opensaml.saml2.core.Assertion;

/**
 * Implements the QueryManager interface for ebXML Registry as defined by ebRS
 * spec.
 * 
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class QueryManagerImpl extends AbstractPluginManager implements QueryManager {

	private static QueryManagerImpl instance = null;

	/* The logger */
	private static Log log = LogFactory.getLog(QueryManagerImpl.class.getName());

	/**
	 * @directed
	 */
	private it.cnr.icar.eric.server.query.filter.RRFilterQueryProcessor filterQueryProcessor = it.cnr.icar.eric.server.query.filter.RRFilterQueryProcessor
			.getInstance();

	/**
	 * @directed
	 */
	private it.cnr.icar.eric.server.query.sql.SQLQueryProcessor sqlQueryProcessor = it.cnr.icar.eric.server.query.sql.SQLQueryProcessor
			.getInstance();

	private RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
	private SAMLRegistrar subjectRegistrar = SAMLRegistrar.getInstance();


	private FederatedQueryManager fqm = null;

	// The prefix for properties that configure a QueryPlugin class
	public static final String QUERY_PLUGIN_PROPERTY_PREFIX = "eric.server.query.plugin";

	// The prefix for properties that configure a QueryFilterPlugin class
	public static final String QUERY_FILTER_PLUGIN_PROPERTY_PREFIX = "eric.server.query.filter.plugin";

	// Key: queryId value: QueryPlugin instance
	Map<String, Object> queryPluginsMap = new HashMap<String, Object>();

	boolean permitAllRead = Boolean.valueOf(
			RegistryProperties.getInstance().getProperty("eric.security.authorization.enableOverride.permitAllRead",
					"true")).booleanValue();

	boolean bypassCMS = false;
	CMSManager cmsm = new CMSManagerImpl(); // CMSManagerFactory.getInstance().getContentManagementServiceManager();

	boolean fetchChildObjsSrv = Boolean.valueOf(
			RegistryProperties.getInstance().getProperty("it.cnr.icar.eric.server.query.fetchChildObjects", "false"))
			.booleanValue();

	protected QueryManagerImpl() {
		bypassCMS = Boolean.valueOf(
				RegistryProperties.getInstance().getProperty("it.cnr.icar.eric.server.query.bypassCMS", "true"))
				.booleanValue();

		// Create and cache QueryPlugins
		getQueryPlugins();

		// Create and cache QueryFilterPlugins
		getQueryFilterPlugins();
	}

	public synchronized static QueryManagerImpl getInstance() {
		if (instance == null) {
			instance = new QueryManagerImpl();
		}

		return instance;
	}

	/**
	 * submitAdhocQuery
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public AdhocQueryResponse submitAdhocQuery(RequestContext context) throws RegistryException {

		AdhocQueryResponse ebAdhocQueryResponse = null;
		context = ServerRequestContext.convert(context);

		AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest) ((ServerRequestContext) context)
				.getCurrentRegistryRequest();
		ResponseOptionType ebResponseOptionType = ebAdhocQueryRequest.getResponseOption();
		ReturnType returnType = ebResponseOptionType.getReturnType();

		UserType user = ((ServerRequestContext) context).getUser();

		// The result of the query
		RegistryObjectListType ebRegistryObjectListType = null;

		try {
			ebAdhocQueryResponse = null;

			// Process request for the case where it is a parameterized
			// invocation of a stored query
			processForParameterizedQuery((ServerRequestContext) context);

			// TODO: May need a better way than checking
			// getSpecialQueryResults() to know if specialQuery was called.
			if (((ServerRequestContext) context).getSpecialQueryResults() != null) {
				ebAdhocQueryResponse = processForSpecialQueryResults((ServerRequestContext) context);
			} else {
				// Check if it is a federated query and process it using
				// FederatedQueryManager if so.
				boolean isFederated = ebAdhocQueryRequest.isFederated();
				if (isFederated) {
					// Initialize lazily. Otherwise we have an infinite create
					// loop
					if (fqm == null) {
						fqm = FederatedQueryManager.getInstance();
					}
					ebAdhocQueryResponse = fqm.submitAdhocQuery((ServerRequestContext) context);
				} else {
					int startIndex = ebAdhocQueryRequest.getStartIndex().intValue();
					int maxResults = ebAdhocQueryRequest.getMaxResults().intValue();
					IterativeQueryParams paramHolder = new IterativeQueryParams(startIndex, maxResults);

					org.oasis.ebxml.registry.bindings.rim.AdhocQueryType adhocQuery = ebAdhocQueryRequest
							.getAdhocQuery();

					QueryExpressionType queryExp = adhocQuery.getQueryExpression();
					String queryLang = queryExp.getQueryLanguage();
					if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
						String queryStr = (String) queryExp.getContent().get(0);
						queryStr = replaceSpecialVariables(user, queryStr);

						ebRegistryObjectListType = sqlQueryProcessor.executeQuery((ServerRequestContext) context, user,
								queryStr, ebResponseOptionType, paramHolder);

						processDepthParameter((ServerRequestContext) context);

						ebAdhocQueryResponse = BindingUtility.getInstance().queryFac.createAdhocQueryResponse();

						ebAdhocQueryResponse.setRegistryObjectList(ebRegistryObjectListType);
						ebAdhocQueryResponse.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
						ebAdhocQueryResponse.setStartIndex(BigInteger.valueOf(paramHolder.startIndex));
						ebAdhocQueryResponse.setTotalResultCount(BigInteger.valueOf(paramHolder.totalResultCount));

					} else if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery)) {
						String queryStr = (String) queryExp.getContent().get(0);
						Unmarshaller unmarshaller = BindingUtility.getInstance().getJAXBContext().createUnmarshaller();
						JAXBElement<FilterQueryType> ebFilterQuery = (JAXBElement<FilterQueryType>) unmarshaller.unmarshal(new StreamSource(
								new StringReader(queryStr)));

						// take ComplexType from Element
						FilterQueryType filterQuery = ebFilterQuery.getValue(); 

						ebRegistryObjectListType = filterQueryProcessor.executeQuery(((ServerRequestContext) context),
								user, filterQuery, ebResponseOptionType, paramHolder);

						ebAdhocQueryResponse = BindingUtility.getInstance().queryFac.createAdhocQueryResponse();
						ebAdhocQueryResponse.setRegistryObjectList(ebRegistryObjectListType);
						ebAdhocQueryResponse.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
						ebAdhocQueryResponse.setStartIndex(BigInteger.valueOf(paramHolder.startIndex));
						ebAdhocQueryResponse.setTotalResultCount(BigInteger.valueOf(paramHolder.totalResultCount));

					} else {
						throw new UnsupportedCapabilityException("Unsupported Query Language: ClassificationNode id: "
								+ queryLang);
					}
				}
			}

			// fetch child objects
			if (fetchChildObjsSrv) {
				HashMap<String, Object> slotsMap = bu.getSlotsFromRequest(ebAdhocQueryRequest);
				boolean fetchChildObjsClt = Boolean.valueOf(
						(String) slotsMap.get(CanonicalConstants.CANONICAL_SLOT_GET_CHILD_OBJECTS)).booleanValue();
				if (fetchChildObjsClt) {
					fetchChildObjects(ebAdhocQueryResponse.getRegistryObjectList().getIdentifiable(),
							(ServerRequestContext) context, ebResponseOptionType);
				}
			}

			// Add repositoryItems to repositoryItemMap if so requested in
			// responseOption
			if (returnType == returnType.LEAF_CLASS_WITH_REPOSITORY_ITEM) {
				addRepositoryItems(ebAdhocQueryResponse.getRegistryObjectList().getIdentifiable(), context);
			}

			if (!bypassCMS) {
				// Now perform any Role-Based Content Filtering on query results
				((ServerRequestContext) context).getQueryResults().clear();
				((ServerRequestContext) context).getQueryResults().addAll(
						ebAdhocQueryResponse.getRegistryObjectList().getIdentifiable());

				cmsm.invokeServices(((ServerRequestContext) context));
				ebAdhocQueryResponse.getRegistryObjectList().getIdentifiable().clear();
				ebAdhocQueryResponse.getRegistryObjectList().getIdentifiable()
						.addAll(((ServerRequestContext) context).getQueryResults());

				((ServerRequestContext) context).getQueryResults().clear();
			}
		} catch (RegistryException e) {
			((ServerRequestContext) context).rollback();
			throw e;
		} catch (Exception e) {
			((ServerRequestContext) context).rollback();
			throw new RegistryException(e);
		}

		removeObjectsDeniedAccess(((ServerRequestContext) context), ebAdhocQueryResponse.getRegistryObjectList()
				.getIdentifiable());

		if (isQueryFilterRequestBeingMade((ServerRequestContext) context)) {
			// Handle filter query requests
			processForQueryFilterPlugins((ServerRequestContext) context);
			// Filter queries produce special query results
			ebAdhocQueryResponse = processForSpecialQueryResults((ServerRequestContext) context);
		}

		((ServerRequestContext) context).commit();
		ebAdhocQueryResponse.setRequestId(ebAdhocQueryRequest.getId());

		return ebAdhocQueryResponse;
	}

	@SuppressWarnings("unchecked")
	private AdhocQueryResponse processForSpecialQueryResults(ServerRequestContext context) throws RegistryException {
		AdhocQueryResponse ebAdhocQueryResponse = null;
		try {

			// Must have been Optimization for a special query like
			// "urn:oasis:names:tc:ebxml-regrep:query:FindObjectByIdAndType"
			RegistryObjectListType ebRegistryObjectListType = BindingUtility.getInstance().rimFac
					.createRegistryObjectListType();
			ebRegistryObjectListType.getIdentifiable()
					.addAll((context).getSpecialQueryResults());
			(context).setSpecialQueryResults(null);

			ebAdhocQueryResponse = BindingUtility.getInstance().queryFac.createAdhocQueryResponse();
			ebAdhocQueryResponse.setRegistryObjectList(ebRegistryObjectListType);
			ebAdhocQueryResponse.setStatus(BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success);
			ebAdhocQueryResponse.setStartIndex(BigInteger.valueOf(0));
			ebAdhocQueryResponse.setTotalResultCount(BigInteger.valueOf((context)
					.getQueryResults().size()));
		} catch (Throwable t) {
			throw new RegistryException(t);
		}
		return ebAdhocQueryResponse;
	}

	private boolean isQueryFilterRequestBeingMade(ServerRequestContext context) {
		boolean isQueryFilterRequestBeingMade = false;
		Map<?, ?> map = context.getQueryParamsMap();
		if (map != null) {
			Object obj = map.get("$queryFilterIds");
			if (obj != null) {
				isQueryFilterRequestBeingMade = true;
			}
		}
		return isQueryFilterRequestBeingMade;
	}

	/*
	 * This method is used to process any configured QueryFilter plugins
	 */
	private void processForQueryFilterPlugins(ServerRequestContext context) throws RegistryException {
		try {
			Map<?, ?> paramsMap = context.getQueryParamsMap();
			if (paramsMap != null) {
				Object obj = context.getQueryParamsMap().get("$queryFilterIds");
				if (obj != null) {
					if (obj instanceof String) {
						String filterId = (String) obj;
						QueryPlugin plugin = (QueryPlugin) queryPluginsMap.get(filterId);
						plugin.processRequest(context);
					} else if (obj instanceof Collection) {
						Collection<?> filterIds = (Collection<?>) obj;
						Iterator<?> filterItr = filterIds.iterator();
						while (filterItr.hasNext()) {
							QueryPlugin plugin = (QueryPlugin) filterItr.next();
							plugin.processRequest(context);
						}
					} else {
						String msg = ServerResourceBundle.getInstance().getString("invalidFilterQueryParamter",
								new Object[] { obj.getClass().getName() });
						throw new RegistryException(msg);
					}
				}
			}
		} catch (RegistryException re) {
			throw re;
		} catch (Throwable t) {
			throw new RegistryException(t);
		}
	}

	/**
	 * Recursively fetches child objects of ClassificationSchemes,
	 * ClassificationNodes and RegistryPackages
	 */
	private void fetchChildObjects(List<?> objList, ServerRequestContext context, ResponseOptionType responseOption)
			throws RegistryException, JAXBException {

		PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
		List<?> results = null;
		String sqlQuery = "";

		for (int i = 0; i < objList.size(); i++) {
			ArrayList<String> queryParams = new ArrayList<String>();
			Object identifiable = objList.get(i);

			if (identifiable instanceof ClassificationSchemeType) {
				ClassificationSchemeType ebClassificationSchemeType = (ClassificationSchemeType) identifiable;
				sqlQuery = "SELECT * FROM classificationnode WHERE parent= ?";
				log.trace("Executing query: '" + sqlQuery + "'");
				queryParams.add(ebClassificationSchemeType.getId());
				results = pm.executeSQLQuery(context, sqlQuery, queryParams, responseOption, "classificationnode",
						new ArrayList<Object>());

				for (int j = 0; j < results.size(); j++) {
					ClassificationNodeType ebClassificationNodeType = (ClassificationNodeType) results.get(j);
					ebClassificationSchemeType.getClassificationNode().add(ebClassificationNodeType);
				}

				fetchChildObjects(results, context, responseOption);

			} else if (identifiable instanceof ClassificationNodeType) {
				ClassificationNodeType cn = (ClassificationNodeType) identifiable;
				sqlQuery = "SELECT * FROM classificationnode WHERE parent= ?";
				log.trace("Executing query: '" + sqlQuery + "'");
				queryParams.add(cn.getId());
				results = pm.executeSQLQuery(context, sqlQuery, queryParams, responseOption, "classificationnode",
						new ArrayList<Object>());

				for (int j = 0; j < results.size(); j++) {
					ClassificationNodeType ebClassificationNodeType = (ClassificationNodeType) results.get(j);
					cn.getClassificationNode().add(ebClassificationNodeType);
				}

				fetchChildObjects(results, context, responseOption);

			} else if (identifiable instanceof RegistryPackageType) {
				RegistryPackageType ebRegistryPackageType = (RegistryPackageType) identifiable;
				sqlQuery = "SELECT * FROM registryobject WHERE id in ";
				sqlQuery += "(SELECT ass.targetobject FROM association ass WHERE ass.sourceobject= ? and ";
				sqlQuery += "ass.associationType= ?)";
				log.trace("Executing query: '" + sqlQuery + "'");
				queryParams.add(ebRegistryPackageType.getId());
				queryParams.add(CanonicalSchemes.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);
				results = pm.executeSQLQuery(context, sqlQuery, queryParams, responseOption, "registryobject",
						new ArrayList<Object>());

				if (results.size() > 0) {
					ebRegistryPackageType.setRegistryObjectList(BindingUtility.getInstance().rimFac
							.createRegistryObjectListType());

					for (int j = 0; j < results.size(); j++) {
						// create element from complextype
						JAXBElement<? extends IdentifiableType> ebMember = BindingUtility.getInstance().rimFac
								.createIdentifiable((IdentifiableType) results.get(j));
						// add element
						ebRegistryPackageType.getRegistryObjectList().getIdentifiable().add(ebMember);
					}
				}

				fetchChildObjects(results, context, responseOption);
			}
		}
	}

	/**
	 * Recursively adds RepositoryItems of the ExtrinsicObjects in the
	 * <code>regObjs</code> list.
	 */
	private void addRepositoryItems(List<JAXBElement<? extends IdentifiableType>> regObjs, RequestContext context)
			throws RegistryException {
		for (int i = 0; i < regObjs.size(); i++) {

			// get ComplexType from Element
			Object obj = regObjs.get(i).getValue();

			if (obj instanceof ExtrinsicObjectType) {
				try {
					ExtrinsicObjectType ebExtrinsicObjectType = (ExtrinsicObjectType) obj;

					String id = ebExtrinsicObjectType.getId();
					RepositoryItem repositoryItem = RepositoryManagerFactory.getInstance().getRepositoryManager()
							.getRepositoryItem(id);

					context.getRepositoryItemsMap().put(id, repositoryItem);
				} catch (ObjectNotFoundException onfe) {
					// ignore, ExtrinsicObject had no RepositoryItem
					continue;
				}

			} else if (obj instanceof RegistryPackageType) {
				RegistryPackageType ebRegistryPackageType = (RegistryPackageType) obj;
				if (fetchChildObjsSrv && ebRegistryPackageType.getRegistryObjectList() != null
						&& ebRegistryPackageType.getRegistryObjectList().getIdentifiable().size() > 0) {
					addRepositoryItems(ebRegistryPackageType.getRegistryObjectList().getIdentifiable(), context);
				}
			}
		}
	}

	/**
	 * Removes any objects that the user doesn't have authorization to see.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void removeObjectsDeniedAccess(ServerRequestContext context, List objs) throws RegistryException {

		context.setQueryResults(objs);

		if (permitAllRead) {
			// Optimization: permit auth override to permit all reads
			return;
		}
		AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest) context.getCurrentRegistryRequest();

		// Remove any objects from the ad-hoc query result set that the
		// user is not permitted to see.
		AuthorizationResult authRes = AuthorizationServiceImpl.getInstance().checkAuthorization(
				(context));

		if (authRes.getDeniedResources().size() > 0) {
			if (isIterativeQuery(ebAdhocQueryRequest)) {
				int size = objs.size();
				for (int i = 0; i < size; i++) {
					IdentifiableType identifiableObject = (IdentifiableType) objs.get(i);
					String id = identifiableObject.getId();
					if (authRes.getDeniedResources().contains(id)) {
						objs.remove(i);
						// 'Replace denied resource with placeholder object
						// Use lightweight object, RegistryPackage
						// Workaround for bug - bugster id: 6239592
						RegistryPackageType ebRegistryPackageType = BindingUtility.getInstance().rimFac
								.createRegistryPackageType();
						ebRegistryPackageType
								.setObjectType("urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:RegistryPackage");
						InternationalStringType iString = BindingUtility.getInstance().rimFac
								.createInternationalStringType();
						LocalizedStringType ebLocalizedStringType = BindingUtility.getInstance().rimFac
								.createLocalizedStringType();
						// We'll i18n this string soon
						ebLocalizedStringType.setValue("(Access Denied)");
						ebLocalizedStringType.setLang("en-US");
						ebLocalizedStringType.setCharset("UTF-8");
						iString.getLocalizedString().add(ebLocalizedStringType);
						ebRegistryPackageType.setName(iString);
						objs.add(i, ebRegistryPackageType);

						// TODO: Should not expose an actual id to unauthorized
						// clients
						ebRegistryPackageType.setId(id);
						ebRegistryPackageType.setLid(id);
					}
				}
			} else {
				Iterator queryResultsIter = objs.iterator();
				while (queryResultsIter.hasNext()) {
					IdentifiableType identifiableObject = (IdentifiableType) queryResultsIter.next();
					String id = identifiableObject.getId();
					if (authRes.getDeniedResources().contains(id)) {
						queryResultsIter.remove();
					}
				}
			}
		}

		context.setQueryResults(objs);
	}

	/**
	 * Process optional impl specific depth paramater specified as request Slot.
	 * If present, prefetches referenced objects up to specified depth level.
	 * Depth = 0 (default) implies only fetch matched objects. Depth = n
	 * implies, also fetch all objects referenced by matched objects upto depth
	 * of n Depth = -1 implies, also fetch all objects referenced by matched
	 * objects upto any level. Direct and indirect circular references are
	 * handled to avoid infinite loop.
	 */
	private void processDepthParameter(ServerRequestContext context) throws RegistryException {
	}

	private boolean isIterativeQuery(AdhocQueryRequest ebAdhocQueryRequest) {
		return (ebAdhocQueryRequest.getMaxResults().intValue() != -1);
		/*
		if (ebAdhocQueryRequest.getMaxResults().intValue() == -1) {
			return false;
		} else {
			return true;
		}
		*/
	}

	/**
	 * Extracts the queryId and parameters from request and stores it in the
	 * context for later use by QueryPlugin.
	 * 
	 */
	private void getQueryParameters(ServerRequestContext context) throws RegistryException {
		AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest) (context)
				.getCurrentRegistryRequest();

		SlotListType slotList = ebAdhocQueryRequest.getRequestSlotList();

		if (slotList != null) {
			List<SlotType1> slots = slotList.getSlot();

			HashMap<String, Object> queryParamsMap = new HashMap<String, Object>();
			Iterator<SlotType1> iter = slots.iterator();
			while (iter.hasNext()) {
				SlotType1 slot = iter.next();
				String slotName = slot.getName();
				if (slotName.equals(BindingUtility.CANONICAL_SLOT_QUERY_ID)) {
					String value = (slot.getValueList().getValue()).get(0);
					context.setQueryId(value);
				} else if (slotName.charAt(0) == '$') {
					List<String> vlist = slot.getValueList().getValue();
					int vListSize = vlist.size();
					if (vListSize == 1) {
						String paramValue = vlist.get(0);
						queryParamsMap.put(slotName, paramValue); // <String,
																	// String>
					} else if (vListSize > 1) {
						// Need to support a Collection of Strings
						Collection<String> stringCollection = new ArrayList<String>(vListSize);
						Iterator<String> valItr = vlist.iterator();
						while (valItr.hasNext()) {
							String value = valItr.next();
							stringCollection.add(value);
						}
						queryParamsMap.put(slotName, stringCollection); // <String,
																		// Collection<String>>
					}
				}
			}

			context.setQueryParamsMap(queryParamsMap);
		}
	}

	/**
	 * Creates and caches all QueryPlugins
	 */
	private void getQueryPlugins() {
		RegistryProperties props = RegistryProperties.getInstance();
		Iterator<?> propsIter = props.getPropertyNamesStartingWith(QUERY_PLUGIN_PROPERTY_PREFIX);

		while (propsIter.hasNext()) {
			String prop = (String) propsIter.next();
			String queryId = prop.substring(QUERY_PLUGIN_PROPERTY_PREFIX.length() + 1);
			String pluginClassName = props.getProperty(prop);

			try {
				Object plugin = createPluginInstance(pluginClassName);
				if (!(plugin instanceof QueryPlugin)) {
					throw new JAXRException(CommonResourceBundle.getInstance().getString(
							"message.unexpectedObjectType",
							new String[] { plugin.getClass().toString(), QueryPlugin.class.toString() }));
				}
				queryPluginsMap.put(queryId, plugin);
			} catch (Exception e) {
				log.error(e);
			}
		}

	}

	/**
	 * Creates and caches all QueryFilterPlugins
	 */
	private void getQueryFilterPlugins() {
		RegistryProperties props = RegistryProperties.getInstance();
		Iterator<?> propsIter = props.getPropertyNamesStartingWith(QUERY_FILTER_PLUGIN_PROPERTY_PREFIX);

		while (propsIter.hasNext()) {
			String prop = (String) propsIter.next();
			String queryId = prop.substring(QUERY_FILTER_PLUGIN_PROPERTY_PREFIX.length() + 1);
			String pluginClassName = props.getProperty(prop);

			try {
				Object plugin = createPluginInstance(pluginClassName);
				if (!(plugin instanceof QueryPlugin)) {
					throw new JAXRException(CommonResourceBundle.getInstance().getString(
							"message.unexpectedObjectType",
							new String[] { plugin.getClass().toString(), QueryPlugin.class.toString() }));
				}
				queryPluginsMap.put(queryId, plugin);
			} catch (Exception e) {
				log.error(e);
			}
		}

	}

	/**
	 * Gets the QueryPlugin that can process this query.
	 * 
	 * @return the QueryPlugin if a match is found, otherwise return null
	 */
	QueryPlugin getQueryPlugin(ServerRequestContext context) throws RegistryException {
		QueryPlugin plugin = null;

		String queryId = context.getQueryId();

		if (queryId != null) {
			Object o = queryPluginsMap.get(queryId);
			if (o instanceof QueryPlugin) {
				plugin = (QueryPlugin) o;
			}
		}

		return plugin;
	}

	/**
	 * Checks if supplied query is a parameterized query. If not return the same
	 * query. If stored parameterized query then return a new query after
	 * fetching the specified parameterized query from registry, replacing its
	 * positional parameters with supplied parameters. If special parameterized
	 * query then invoke special query and set its results on
	 * context.getSpecialQueryResults(). If neither not a parameterized query at
	 * all then simply return the original request.
	 */
	private void processForParameterizedQuery(ServerRequestContext context) throws RegistryException {

		// First check if it is a
		AdhocQueryRequest ebAdhocQueryRequest = (AdhocQueryRequest) (context)
				.getCurrentRegistryRequest();

		getQueryParameters(context);
		String queryId = context.getQueryId();
		@SuppressWarnings("unused")
		Map<?, ?> queryParamsMap = context.getQueryParamsMap();

		@SuppressWarnings("unused")
		SlotListType slotList = ebAdhocQueryRequest.getRequestSlotList();

		// If queryId is not null then get the AdhocQuery from registry, plug
		// the parameters
		// and set it as newReq
		if (queryId != null) {
			// This is a parameterized query

			QueryPlugin plugin = getQueryPlugin(context);

			if (plugin != null) {
				// Found a plugin for this queryId. invoke it
				plugin.processRequest(context);
			} else {
				// Must be a stored query since no plugin was found.

				// TODO: Assumes SQLQuery. Needs to support FilterQuery
				AdhocQueryType adhocQuery = (AdhocQueryType) ServerCache.getInstance().getRegistryObject(context,
						queryId, "AdhocQuery");

				if (adhocQuery == null) {
					throw new ObjectNotFoundException(queryId, "AdhocQuery");
				}

				try {
					// Need to make a copy of the query before
					// plugQueryParameters as the query may be cached and should
					// not be modified in place.
					adhocQuery = (AdhocQueryType) BindingUtility.getInstance().cloneRegistryObject(adhocQuery);
					adhocQuery = plugQueryParameters(adhocQuery, context.getQueryParamsMap(),
							context.getStoredQueryParams());
					ebAdhocQueryRequest.setAdhocQuery(adhocQuery);
				} catch (JAXBException e) {
					throw new RegistryException(e);
				} catch (JAXRException e) {
					throw new RegistryException(e);
				}
			}
		}
	}

	/**
	 * Replaces named parameters in an SQLQuery with ? while placing
	 * corresponding positional paramValues in queryParams List.
	 * 
	 * @param query
	 *            the AdhocQuery to be executed
	 * @param queryParamsMap
	 *            the Map in which each entry is a query param name/value pair
	 * @param positionalParamValues
	 *            a List of param values for each positional parameter in the
	 *            PrepareStatment for the query
	 */
	private AdhocQueryType plugQueryParameters(AdhocQueryType query, Map<?, ?> queryParamsMap, List<String> positionalParamValues)
			throws JAXBException {
		AdhocQueryType newQuery = query;
		positionalParamValues.clear(); // Start with empty list

		// Get the queryString
		QueryExpressionType queryExp = query.getQueryExpression();
		@SuppressWarnings("unused")
		String queryLang = queryExp.getQueryLanguage();
		String queryStr = (String) queryExp.getContent().get(0);
		String newQueryStr = new String(queryStr);
		log.debug("queryStr=" + queryStr);

		// Now replace parameterNames in queryString with ?
		// and add corresponding paramValue to quereyParams List
		// If a paramName is used more than once in queryString then
		// add corresponding paramValue multiple times in appropriate
		// position in positionalParamValues list
		// This would be easier with JDBC 3.0 nameParameters but support
		// is optional in JDBC 3.0 drivers and therefore not reliable.
		Iterator<?> iter = queryParamsMap.keySet().iterator();

		Map<Integer, String> paramIndexToValueMap = new TreeMap<Integer, String>();

		// Process each $paramName by replacing it with ? in queryStr
		// and placing its paramValue in corresponding index on
		// positionalParamValues
		while (iter.hasNext()) {
			String paramName = (String) iter.next();
			String paramValue = queryParamsMap.get(paramName).toString();

			// Pattern to match $paramName optionally surrounded by single
			// quotes. First OR clause matches the longer option -- with
			// surrounding single quotes. Second OR clause matches
			// isolated parameter name.
			//
			// Some parameter names are prefixes of others ($considerPort
			// and $considerPortType for example) and this complicates the
			// second part. Must ensure we match the name only if it is
			// not a prefix. The zero-width negative lookahead checks this
			// case and does not mess up the replacement. Similar
			// zero-width negative lookbehind at start avoids matching (for
			// example) '$binding.transportType%' since that case would
			// result in '?%' and a parameter the underlying SQL parser
			// won't find. NOTE: Not checking for all parameters "buried"
			// in SQL string literals.
			//
			// TODO: The currently-used parameter name characters are
			// [a-zA-Z0-9.]. This goes beyond both [a-zA-Z0-9] from
			// [ebRS], section 6.3.1.1 and [a-zA-Z0-9_$#] previously in
			// SQLParser.jj. Rule below and latest SQLParser.jj <VARIABLE>
			// production are at least consistent. Should '.' be removed
			// from parameter names in WS Profile? Should both '.' and '_'
			// be disallowed in all parameter names?

			// Use Utility method as following is not in JDK 1.4
			// String quotedParamName = Pattern.quote(paramName);
			@SuppressWarnings("static-access")
			String quotedParamName = it.cnr.icar.eric.common.Utility.getInstance().quote(paramName);
			String paramNamePattern = "('" + quotedParamName + "')|" + "((?<!')" + quotedParamName
					+ "(?![a-zA-Z0-9._]))";
			Pattern p = Pattern.compile(paramNamePattern);
			Matcher m = p.matcher(queryStr);

			// Remember start index of each occurance of paramName
			boolean found = false;
			while (m.find()) {
				found = true;
				paramIndexToValueMap.put(new Integer(m.start()), paramValue);
			}

			// Replace substrings matching paramNamePattern with ? Must be
			// done using separate matcher and string to keep
			// paramIndexToValueMap sorted
			if (found) {
				newQueryStr = p.matcher(newQueryStr).replaceAll("?");
			}
		}

		if (paramIndexToValueMap.size() > 0) {
			positionalParamValues.addAll(paramIndexToValueMap.values());
		}

		// Now re-constitute as query
		queryExp.getContent().clear();
		queryExp.getContent().add(newQueryStr);

		return newQuery;
	}

	/**
	 * Replaces special environment variables within specified query string.
	 */
	private String replaceSpecialVariables(UserType user, String query) {
		String newQuery = query;

		// Replace $currentUser
		if (user != null) {
			newQuery = newQuery.replaceAll("\\$currentUser", "'" + user.getId() + "'");
		}

		// Replace $currentTime
		Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());

		// ??The timestamp is being truncated to work around a bug in PostgreSQL
		// 7.2.2 JDBC driver
		String currentTimeStr = currentTime.toString().substring(0, 19);
		newQuery = newQuery.replaceAll("\\$currentTime", currentTimeStr);

		return newQuery;
	}

	/**
	 * Gets RegistryObject matching specified id. This method is added for the
	 * REST
	 * 
	 */
	public RegistryObjectType getRegistryObject(RequestContext context, String id) throws RegistryException {

		return getRegistryObject(context, id, "RegistryObject");
	}

	public RegistryObjectType getRegistryObject(RequestContext context, String id, String typeName)
			throws RegistryException {

		RegistryObjectType ro = null;
		ServerRequestContext serverContext = ServerRequestContext.convert(context);
		boolean doCommit = false;

		try {
			// Code in AuthorizationServiceImpl and else where expects a request
			// so make one up
			AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest("SELECT * FROM DummyTable");
			serverContext.pushRegistryRequest(req);

			UserType user = serverContext.getUser();

			typeName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(typeName);
			ro = ServerCache.getInstance().getRegistryObject(serverContext, id, typeName);

			// Avoid overhead of access control checks for RegistryOperator
			// internal user
			if ((null != ro)
					&& ((null == user) || (!user.getId().equalsIgnoreCase(
							AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR)))) {
				List<RegistryObjectType> roList = new ArrayList<RegistryObjectType>();
				roList.add(ro);
				removeObjectsDeniedAccess(serverContext, roList);
				if (!(roList.contains(ro))) {
					ro = null;
				}
			}
			doCommit = true;
		} catch (JAXBException e) {
			log.error(e, e); // Can't happen
		} finally {
			if (context != serverContext) {
				if (doCommit) {
					serverContext.commit();
				} else {
					serverContext.rollback();
				}
			} else {
				serverContext.popRegistryRequest();
			}
		}

		return ro;
	}

	/**
	 * This method is added for the REST It returns the RepositroyItem give a
	 * UUID
	 * 
	 */
	public RepositoryItem getRepositoryItem(RequestContext context, String id) throws RegistryException {

		RepositoryItem ri = null;
		ServerRequestContext serverContext = ServerRequestContext.convert(context);
		boolean doCommit = false;

		try {
			@SuppressWarnings("unused")
			UserType user = serverContext.getUser();
			// Code in AuthorizationServiceImpl and else where expects a request
			// so make one up
			AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest("SELECT * FROM DummyTable");
			serverContext.pushRegistryRequest(req);

			// Following call is to force access control check which is
			// already implemented in getRegistryObject(...)
			RegistryObjectType ro = getRegistryObject(serverContext, id);
			if (ro != null) {
				ri = rm.getRepositoryItem(id);
			}
			doCommit = true;
		} catch (JAXBException e) {
			log.error(e, e); // Can't happen
		} finally {
			if (context != serverContext) {
				if (doCommit) {
					serverContext.commit();
				} else {
					serverContext.rollback();
				}
			} else {
				serverContext.popRegistryRequest();
			}
		}

		return ri;
	}

	/**
	 * Gets the RegistryObjects referenced by specified RegistryObject.
	 * 
	 * @param ro
	 *            specifies the RegistryObject whose referenced objects are
	 *            being sought.
	 * @param depth
	 *            specifies depth of fetch. -1 implies fetch all levels. 1
	 *            implies fetch immediate referenced objects.
	 * 
	 *            public Set getReferencedRegistryObjects(RegistryObjectType ro,
	 *            int depth) throws RegistryException { HashSet
	 *            referencedObjects = new HashSet();
	 * 
	 *            try { HashMap idMap = new HashMap(); Set immediateObjectRefs =
	 *            BindingUtility.getInstance().getObjectRefsInRegistryObject(ro,
	 *            idMap);
	 * 
	 *            --depth;
	 * 
	 *            //Get each immediately referenced RegistryObject Iterator iter
	 *            = immediateObjectRefs.iterator(); while (iter.hasNext()) {
	 *            String ref = (String)iter.next(); RegistryObjectType obj =
	 *            getRegistryObject(ref);
	 * 
	 *            referencedObjects.add(obj);
	 * 
	 *            //If depth != 0 then recurse and get referenced objects for
	 *            obj if (depth != 0) {
	 *            referencedObjects.addAll(getReferencedRegistryObjects(obj,
	 *            depth)); } } } catch (JAXRException e) { throw new
	 *            RegistryException(e); }
	 * 
	 *            return referencedObjects; }
	 */

	public UserType getUser(X509Certificate cert) throws RegistryException {
		return AuthenticationServiceImpl.getInstance().getUserFromCertificate(cert);
	}

	public UserType getUser(Assertion assertion) throws RegistryException {
		return getRequestUser(assertion);
	}

	private UserType getRequestUser(Assertion assertion) throws RegistryException {

		UserType user = null;

		if (user == null) {

			// try to get user from SAML assertion; if the user is not yet
			// locally registered, create a replica from remote user
			user = subjectRegistrar.getUser(assertion);
			if (user == null)
				user = subjectRegistrar.registerUser(assertion);
		}

		if (user == null) {
			// the authentication service is used to retrieve the registry guest
			AuthenticationServiceImpl authnService = AuthenticationServiceImpl.getInstance();
			user = authnService.registryGuest;
		}
		return user;
	}
}

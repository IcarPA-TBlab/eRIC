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
package it.cnr.icar.eric.server.event;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;

/**
 * Given an AuditableEvent find all Subscriptions whhose selectors potentially
 * match the AuditableEvent. This avoids having to check every single
 * subsription and is an important scalability design element.
 * 
 * TODO: Reliable delivery and retries.
 * 
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class SubscriptionMatcher {

	PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
	ResponseOptionType ebResponseOptionType = null;
	ArrayList<Object> objectRefs = new ArrayList<Object>();

	HashMap<String, List<IdentifiableType>> queryToObjectsMap = new HashMap<String, List<IdentifiableType>>();

	SubscriptionMatcher() throws RegistryException {
		ebResponseOptionType = BindingUtility.getInstance().queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
	}

	/*
	 * Gets the Map of Subscriptions that definitely match the specified event.
	 * Map.Entry key = subscription object Map.Entry value = subscribed objects
	 * Collection
	 */
	HashMap<SubscriptionType, List<IdentifiableType>> getMatchedSubscriptionsMap(ServerRequestContext context,
			AuditableEventType ebAuditableEventType) throws RegistryException {

		HashMap<SubscriptionType, List<IdentifiableType>> matchedSubscriptionsMap = new HashMap<SubscriptionType, List<IdentifiableType>>();

		queryToObjectsMap.clear();

		List<RegistryObjectType> matchedQuerys = getMatchedQuerys(context, ebAuditableEventType);

		if (matchedQuerys.size() > 0) {
			StringBuffer ids = BindingUtility.getInstance().getIdListFromRegistryObjects(matchedQuerys);

			// Get all Subscriptions that use the matched querys as selectors
			String query = "SELECT s.* FROM Subscription s WHERE s.selector IN ( " + ids + " )";
			ArrayList<Object> objectRefs = new ArrayList<Object>();
			List<IdentifiableType> matchedSubscriptions = pm.executeSQLQuery(context, query, ebResponseOptionType, "Subscription",
					objectRefs);

			// ????
			Iterator<IdentifiableType> iter = matchedSubscriptions.iterator();
			while (iter.hasNext()) {
				SubscriptionType ebSubscriptionType = (SubscriptionType) iter.next();

				matchedSubscriptionsMap.put(ebSubscriptionType, queryToObjectsMap.get(ebSubscriptionType.getSelector()));
			}
		}

		return matchedSubscriptionsMap;
	}

	/**
	 * Gets the List of AdhocQuery that match the specified event. Initialized
	 * the matchedQueryMap with queries matching the event as keys and objects
	 * matching the query as values.
	 * 
	 */
	private List<RegistryObjectType> getMatchedQuerys(ServerRequestContext context, AuditableEventType ebAuditableEventType) throws RegistryException {
		List<RegistryObjectType> matchedQuerys = new ArrayList<RegistryObjectType>();

		List<?> targetQuerys = getTargetQuerys(context, ebAuditableEventType);
		List<ObjectRefType> ebObjectRefTypeAffectedList = ebAuditableEventType.getAffectedObjects().getObjectRef();

		Iterator<?> iter = targetQuerys.iterator();
		while (iter.hasNext()) {
			AdhocQueryType ebAdhocQueryType = (AdhocQueryType) iter.next();

			if (queryMatches(context, ebAdhocQueryType, ebAuditableEventType.getId(), ebObjectRefTypeAffectedList)) {
				matchedQuerys.add(ebAdhocQueryType);
			}
		}
		return matchedQuerys;
	}

	/*
	 * Determines whether a specified target (potentially matching) query
	 * actually matches the list of affectedObjects or not.
	 */
	private boolean queryMatches(ServerRequestContext context, AdhocQueryType query, String currentEventId,
			List<ObjectRefType> affectedObjects) throws RegistryException {
		boolean match = false;

		QueryExpressionType queryExp = query.getQueryExpression();
		String queryLang = queryExp.getQueryLanguage();
		if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
			String queryStr = (String) queryExp.getContent().get(0);

			// Special parameter replacement for $currentEventId
			queryStr = queryStr.replaceAll("\\$currentEventId", currentEventId);

			// Get objects that match the selector query (selectedObjects)
			List<IdentifiableType> ebIdentifiableTypeSelectedList = pm.executeSQLQuery(context, queryStr, ebResponseOptionType, "RegistryObject",
					objectRefs);

			// match is true if the affectedObjects are a sub-set of the
			// selectedObjects
			List<String> selectedObjectIds = BindingUtility.getInstance().getIdsFromRegistryObjectTypes(ebIdentifiableTypeSelectedList);
			List<String> affectedObjectIds = BindingUtility.getInstance().getIdsFromObjectRefTypes(affectedObjects);

			if (selectedObjectIds.size() > 0) {
				Iterator<String> iter = affectedObjectIds.iterator();
				while (iter.hasNext()) {
					if (selectedObjectIds.contains(iter.next())) {
						match = true;
						break;
					}
				}
			}

			// Now remember which objects matched this query
			queryToObjectsMap.put(query.getId(), ebIdentifiableTypeSelectedList);
		} else {
			throw new RegistryException(ServerResourceBundle.getInstance().getString("message.onlySQLQuerySupported"));
		}

		return match;
	}

	/*
	 * Gets the List of AdhocQueries that potentially match the specifid event.
	 * This is an essential filtering mechanism to achieve scalability by
	 * narrowing the number of Subscriptions to test for a match. <p> Gets all
	 * AdhocQuerys that have event type and primary partition matching this
	 * event.
	 * 
	 * TODO: We need to get the List of objectType for affectedObjects somehow
	 * to help the filtering. This is a spec issue at the moment because
	 * AuditableEvent only contains ObjectRefs and not the actual objects.
	 * 
	 * </p>
	 */
	private List<?> getTargetQuerys(ServerRequestContext context, AuditableEventType ebAuditableEventType) throws RegistryException {
		List<?> querys = null;

		String eventType = ebAuditableEventType.getEventType();

		ResponseOptionType ebResponseOptionType = BindingUtility.getInstance().queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		List<Object> objectRefs = new ArrayList<Object>();

		// TODO: Filter down further based upon primary partitions in future to
		// scale better.

		// Get those AdhocQuerys that match the events eventType or have no
		// eventType specified
		String query = "SELECT q.* FROM AdhocQuery q, Subscription s WHERE q.id = s.selector AND ((q.query LIKE '%eventType%=%"
				+ eventType + "%') OR (q.query NOT LIKE '%eventType%=%'))";
		querys = pm.executeSQLQuery(context, query, ebResponseOptionType, "AdhocQuery", objectRefs);

		return querys;
	}

}

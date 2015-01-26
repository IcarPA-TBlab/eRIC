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

 package it.cnr.icar.eric.client.xml.registry.util;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.Utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.LocalizedString;


public class SQLQueryProvider {

	private static final String LIKE_KEYWORD = "LIKE";
	private static final String WHERE_KEYWORD = "WHERE";
	private static final String PRIMARY_TABLE_NAME = "ptn";

	private static int SORT_NONE = 0;
	private static int SORT_ASC = 1;
	private static int SORT_DESC = 2;
	public static final String CASE_INSENSITIVE_SORT = "caseInsensitiveSort";

	public static Query getAuditTrail(DeclarativeQueryManager dqm, String lid) throws InvalidRequestException,
			JAXRException {
		String queryStr = "SELECT ae.* FROM AuditableEvent ae, AffectedObject ao, RegistryObject ro WHERE ro.lid='"
				+ lid + "' AND ro.id = ao.id AND ao.eventId = ae.id ORDER BY ae.timeStamp_ ASC";
		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
	}

	public static Query getAssociatedObjects(DeclarativeQueryManager dqm, String id)
			throws InvalidRequestException, JAXRException {
		String queryStr = "SELECT ro.* FROM RegistryObject ro, Association ass WHERE ass.sourceObject = '" + id
				+ "' AND ass.targetObject = ro.id";
		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

	}

	public static Query getAllAssociations(DeclarativeQueryManager dqm, String id) throws InvalidRequestException,
			JAXRException {
		String queryStr = "SELECT ass.* FROM Association ass WHERE sourceObject = '" + id + "' OR targetObject = '"
				+ id + "'" + " ORDER BY " + "sourceObject, targetObject, associationType";
		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

	}

	public static Query getExternalLinks(DeclarativeQueryManager dqm, String id) throws InvalidRequestException,
			JAXRException {
		String queryStr = "SELECT el.* FROM ExternalLink el, Association ass WHERE ass.targetObject = '" + id
				+ "' AND ass.associationType = '" + BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks
				+ "' AND ass.sourceObject = el.id ";
		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
	}

	public static Query getProviderOfAssociation(DeclarativeQueryManager dqm, String id)
			throws InvalidRequestException, JAXRException {
		String queryStr = "SELECT ass.* FROM Association ass WHERE targetObject = '" + id + "' AND associationType ='"
				+ BindingUtility.ASSOCIATION_TYPE_ID_ProviderOf + "'";
		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
	}

	public static Query findAssociations(DeclarativeQueryManager dqm, String sourceObjectId, String targetObjectId,
			Collection<?> associationTypes) throws InvalidRequestException, JAXRException {

		String queryStr = "SELECT * FROM Association ";

		String sourceObjectPred = null;
		String targetObjectPred = null;
		String assocTypePred = null;

		boolean predicatesExist = false;

		if (sourceObjectId != null) {
			predicatesExist = true;
			sourceObjectPred = " (sourceObject = '" + sourceObjectId + "') ";
			predicatesExist = true;
		}

		if (targetObjectId != null) {
			predicatesExist = true;
			targetObjectPred = " (targetObject = '" + targetObjectId + "') ";
		}

		if ((associationTypes != null) && (associationTypes.size() > 0)) {
			predicatesExist = true;
			assocTypePred = " ( associationType IN ( ";

			int cnt = 0;
			Iterator<?> iter = associationTypes.iterator();

			while (iter.hasNext()) {
				Object assTypeObj = iter.next();
				String assTypeId = null;

				if (assTypeObj instanceof Concept) {
					assTypeId = ((Concept) assTypeObj).getKey().getId();
				} else if (assTypeObj instanceof String) {
					assTypeId = ((String) assTypeObj);
				} else {
					throw new JAXRException(JAXRResourceBundle.getInstance().getString(
							"message.error.expecting.concept.id", new Object[] { assTypeObj.getClass() }));
				}

				if (cnt++ > 0) {
					assocTypePred += ", ";
				}

				assocTypePred += ("'" + assTypeId + "'");
			}

			assocTypePred += " )) ";
		}

		if (predicatesExist) {
			queryStr += " WHERE ";
		}

		if (sourceObjectPred != null) {
			queryStr += sourceObjectPred;
		}

		if (targetObjectPred != null) {
			if (sourceObjectPred != null) {
				queryStr += " AND ";
			}

			queryStr += targetObjectPred;
		}

		if (assocTypePred != null) {
			if ((sourceObjectPred != null) || (targetObjectPred != null)) {
				queryStr += " AND ";
			}

			queryStr += assocTypePred;
		}

		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
	}

	public static Query findConceptsByPath(DeclarativeQueryManager dqm, String path)
			throws InvalidRequestException, JAXRException {
		String likeOrEqual = "=";

		if (path.indexOf('%') != -1) {
			likeOrEqual = LIKE_KEYWORD;
		}

		String queryStr = "SELECT cn.* from ClassificationNode cn WHERE cn.path " + likeOrEqual + " '" + path
				+ "' ORDER BY cn.path ASC";

		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

	}

	/**
	 * Creates a Query based on specified parameters
	 * 
	 * @param findQualifiers
	 *            UDDI find qualifiers to apply
	 *            
	 *            A Collection of find qualifiers as defined by the FindQualifier interface, 
	 *            which specifies qualifiers that affect string matching, sorting, 
	 *            boolean predicate logic, and the like.
	 *            
	 * @param tableName
	 *            Database table from which to select objects
	 *            
	 * @param namePatterns
	 *            Collection of Strings that are patterns of names to match
	 *            
	 *            A Collection that may consist of either String or LocalizedString objects. 
	 *            Each String or value within a LocalizedString is a partial or full name pattern 
	 *            with wildcard searching as specified by the SQL-92 LIKE specification. 
	 *            Unless otherwise specified in findQualifiers, this is a logical OR, and a 
	 *            match on any name qualifies as a match for this criterion.
	 *            
	 * @throws JAXRException
	 *             if an error occurs
	 * @return New query
	 */
	public static Query createQueryByName(DeclarativeQueryManager dqm, Collection<?> findQualifiers, String tableName,
			Collection<String> namePatterns) throws JAXRException {
		boolean caseSensitive = false;
		boolean exactNameMatch = false;
		int sortByName = SORT_NONE;

		tableName = Utility.getInstance().mapTableName(tableName);
		if (findQualifiers != null) {
			if (findQualifiers.contains(FindQualifier.CASE_SENSITIVE_MATCH)) {
				caseSensitive = true;
			}

			if (findQualifiers.contains(FindQualifier.EXACT_NAME_MATCH)) {
				exactNameMatch = true;
			}

			if (findQualifiers.contains(FindQualifier.SORT_BY_NAME_ASC)) {
				sortByName = SORT_ASC;
			} else if (findQualifiers.contains(FindQualifier.SORT_BY_NAME_DESC)) {
				sortByName = SORT_DESC;
			}
		}

		/* xxx pa 120207 bugfix for broken SQL construction 
		 * issued by testcase BusinessQueryManagerTest.testFindQualifiers()
		 
		  	[ERROR] RegistryBSTServlet - -Caught exception: java.sql.SQLException: ERROR: for SELECT DISTINCT, ORDER BY expressions
				 must appear in select list <javax.xml.registry.RegistryException: java.sql.SQLException: 
				 ERROR: for SELECT DISTINCT, ORDER BY expressions must appear in select list>
				 javax.xml.registry.RegistryException: java.sql.SQLException: ERROR: for
				SELECT DISTINCT, ORDER BY expressions must appear in select list

		 * When SORT_ASC (sort by name ascending) is used, SQL needs to have table "name" in select list
		 * Therefore  "DISTINCT *" will be used instead of "DISTINCT ptn.*"
		 * 
		 * Remark:
		 * 		Due to 1:n relation between <sometable>-1-n-<name> this change impacts count of result objects
		 */
		StringBuffer queryStr;
		
		
		String likeExpr = namePatternsToLikeExpr(namePatterns, "n.value", caseSensitive, exactNameMatch, sortByName);
		

		
		if (likeExpr != null) {
			// xxx 120214 pa one more decision to keep DISTINCT ptn.* as long as possible
			
			if (sortByName != SORT_NONE)
				// ORDER BY sorting needs joined table columns in select list
				// SELECT DISTINCT * FROM ...
				queryStr = new StringBuffer("SELECT DISTINCT " + "* FROM " + tableName + " "
					+ PRIMARY_TABLE_NAME);
			else
				// Without sorting DISTINCT have to be bound to PRIMARY_TABLE_NAME 
				// SELECT DISTINCT ptn.* FROM ...
				queryStr = new StringBuffer("SELECT DISTINCT " + PRIMARY_TABLE_NAME + ".* FROM " + tableName + " "
						+ PRIMARY_TABLE_NAME);
			
			queryStr.append(", Name_ n " + WHERE_KEYWORD + " " + likeExpr + " AND n.parent = " + PRIMARY_TABLE_NAME + ".id");


		} else {
			queryStr = new StringBuffer("SELECT DISTINCT " + PRIMARY_TABLE_NAME + ".* FROM " + tableName + " "
					+ PRIMARY_TABLE_NAME);

		}

		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
	}
	
	public static Query getRegistryObjects(DeclarativeQueryManager dqm, Collection<?> objectKeys, String objectType)
			throws JAXRException {

		StringBuffer queryStr = new StringBuffer("SELECT * FROM ");
		queryStr.append(it.cnr.icar.eric.common.Utility.getInstance().mapTableName(objectType));
		queryStr.append(" WHERE id in (");

		Iterator<?> iter = objectKeys.iterator();

		while (iter.hasNext()) {
			String id = ((Key) iter.next()).getId();
			queryStr.append("'").append(id).append("'");

			if (iter.hasNext()) {
				queryStr.append(", ");
			}
		}

		queryStr.append(')');

		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
	}	
	
	public static Query initializeObjectTypesMap(DeclarativeQueryManager dqm)
			throws JAXRException {
		String queryStr = "SELECT children.* FROM ClassificationNode children, ClassificationNode parent where (parent.path LIKE '/"
				+ BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
				+ "/RegistryObject%') AND (parent.path NOT LIKE '/"
				+ BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
				+ "/RegistryObject/ExtrinsicObject/%') AND parent.id = children.parent";

		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
		
	}

	public static Query getRegistryObjects(DeclarativeQueryManager dqm, String objectType, Collection<?> notCached)
			throws JAXRException {

		String tablename = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(objectType);
        StringBuffer queryStr = new StringBuffer("SELECT ro.* FROM ");
        queryStr.append(tablename).append(" ro WHERE id IN (");
        Iterator<?> idNotCached = notCached.iterator();
        while (idNotCached.hasNext()) {
            queryStr.append("'").append(idNotCached.next()).append("'");
            if (idNotCached.hasNext()) {
                queryStr.append(", ");
            }
        }
        queryStr.append(')');
	
		return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr.toString());
		
	}
	
	
	// public static Query findCallerAssociations(DeclarativeQueryManager
	// dqm, Boolean confirmedByCaller, Boolean confirmedByOtherParty,
	// Collection associationTypes) throws InvalidRequestException,
	// JAXRException {
	// //Find all Associations owned by caller's user ($currentUser is resolved
	// by registry automatically)
	// String queryStr =
	// "SELECT DISTINCT a.* FROM Association a, AuditableEvent e, AffectedObject o, Slot s1, Slot s2 WHERE "
	// +
	// "e.user_ = $currentUser AND ( e.eventType = '" +
	// BindingUtility.CANONICAL_EVENT_TYPE_ID_Created +
	// "' OR e.eventType = '" +
	// BindingUtility.CANONICAL_EVENT_TYPE_ID_Versioned +
	// "' OR e.eventType = '" +
	// BindingUtility.CANONICAL_EVENT_TYPE_ID_Relocated +
	// "') AND o.eventId = e.id AND (o.id = a.sourceObject OR o.id = a.targetObject)";
	//
	// if (associationTypes != null) {
	// //Add predicate for associationType filter
	// Iterator iter = associationTypes.iterator();
	//
	// while (iter.hasNext()) {
	// Object obj = iter.next();
	//
	// String assocTypeId = null;
	//
	// if (obj instanceof Concept) {
	// assocTypeId = ((Concept) obj).getKey().getId();
	// } else if (obj instanceof String) {
	// String str = (String) obj;
	//
	// if (str.startsWith("urn:uuid")) {
	// //str is already the assocTypeId
	// assocTypeId = str;
	// } else {
	// //Assume str is the code
	// Concept c = findConceptByPath("/" +
	// BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType +
	// "%/" + str);
	// assocTypeId = ((Concept) c).getKey().getId();
	// }
	// }
	//
	// if (assocTypeId != null) {
	// queryStr += (" AND a.associationType = '" + assocTypeId + "'");
	// }
	// }
	// }
	//
	// //Do further filtering based upon confirmedByCaller and
	// confirmedByOtherParty if needed.
	// if (confirmedByCaller != null) {
	// if (confirmedByCaller.booleanValue()) {
	// //ass is confirmed by caller
	// queryStr +=
	// (" AND (s1.parent = a.id AND s1.sequenceId = 1 AND ((s1.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
	// "' AND s1.value = $currentUser)" + " OR (s1.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
	// "' AND s1.value = $currentUser)))");
	// } else {
	// //ass is NOT confirmed by caller
	// queryStr +=
	// (" AND NOT (s1.parent = a.id AND s1.sequenceId = 1 AND ((s1.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
	// "' AND s1.value = $currentUser)" + " OR (s1.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
	// "' AND s1.value = $currentUser)))");
	// }
	// }
	//
	// if (confirmedByOtherParty != null) {
	// if (confirmedByOtherParty.booleanValue()) {
	// //ass is confirmed by other party
	// queryStr +=
	// (" AND (s2.parent = a.id AND s2.sequenceId = 1 AND ((s2.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
	// "' AND s2.value != $currentUser)" + " OR (s2.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
	// "' AND s2.value != $currentUser)))");
	// } else {
	// //ass is NOT confirmed by other party
	// queryStr +=
	// (" AND NOT (s2.parent = a.id AND s2.sequenceId = 1 AND ((s2.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
	// "' AND s2.value != $currentUser)" + " OR (s2.name_ = '" +
	// BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
	// "' AND s2.value != $currentUser)))");
	// }
	// }
	// return dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
	// }

	private static String namePatternsToLikeExpr(Collection<String> namePatterns, String term, boolean caseSensitive,
			boolean exactNameMatch, int sortByName) {
		String likeOrEqual = LIKE_KEYWORD;

		if (exactNameMatch) {
			likeOrEqual = "=";
		}

		if (sortByName == SORT_NONE) {
			if ((namePatterns == null) || (namePatterns.size() == 0)) {
				return null;
			} else if (namePatterns.size() == 1) {
				Object[] namesArray = namePatterns.toArray();

				if ((!exactNameMatch) && (getNamePattern(namesArray[0]).equals("%"))) {
					return null;
				}
			}
		} else {
			// Need to have "LIKE '%'" as namePattern if none specified but name
			// sorting is specified.
			if ((namePatterns == null) || (namePatterns.size() == 0)) {
				likeOrEqual = LIKE_KEYWORD;

				namePatterns = new ArrayList<String>();
				namePatterns.add("%");
			}
		}

		Iterator<String> i = namePatterns.iterator();
		StringBuffer result = new StringBuffer("(" + caseSensitise(term, caseSensitive) + " " + likeOrEqual + " "
				+ caseSensitise("'" + getNamePattern(i.next()) + "'", caseSensitive));

		while (i.hasNext()) {
			result.append(" OR " + caseSensitise(term, caseSensitive) + " " + likeOrEqual + " "
					+ caseSensitise("'" + getNamePattern(i.next()) + "'", caseSensitive));
		}

		return result.append(')').toString();
	}

	private static String getNamePattern(Object o) {
		String namePattern = o.toString();
		if (o instanceof LocalizedString) {
			try {
				namePattern = ((LocalizedString) o).getValue();
			} catch (JAXRException e) {
				// Cant happen
				// log.error(e);
			}
		}

		return namePattern;
	}

	private static String caseSensitise(String term, boolean caseSensitive) {
		String newTerm = term;

		if (!caseSensitive) {
			newTerm = "UPPER(" + term + ")";
		}

		return newTerm;
	}

}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.SQLQueryProvider;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.CommonProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.UnexpectedObjectException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;


/**
 * Implements JAXR API interface named BusinessQueryManager
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class BusinessQueryManagerImpl extends QueryManagerImpl
    implements BusinessQueryManager {
    
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(BusinessQueryManagerImpl.class);

    @SuppressWarnings("unused")
	private static final String LIKE_KEYWORD = "LIKE";
    private static final String WHERE_KEYWORD = "WHERE";
    private static final String PRIMARY_TABLE_NAME = "ptn";
    private static HashMap<String, String> schemeNameToIdMap = new HashMap<String, String>();
    private static int SORT_NONE = 0;
    private static int SORT_ASC = 1;
    private static int SORT_DESC = 2;
    public static final String CASE_INSENSITIVE_SORT = "caseInsensitiveSort";

    static {
        schemeNameToIdMap.put("ObjectType",
            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType);
        schemeNameToIdMap.put("PhoneType",
            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_PhoneType);
        schemeNameToIdMap.put("AssociationType",
            BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType);
        schemeNameToIdMap.put("URLType",
            "urn:uuid:7817755e-8842-44b2-84f4-bf8a765619be"); //??Only needed for UDDI providers. Need to fix JAXR spec and TCK
        schemeNameToIdMap.put("PostalAddressAttributes", "ClassScheme"); //??Only needed for UDDI providers. Need to fix JAXR spec and TCK
    }

    static boolean checkOrderBy = Boolean.valueOf(CommonProperties.getInstance().
                                    getProperty("eric.common.restrictedOrderBySupport", "true")).
                                    booleanValue();
    it.cnr.icar.eric.client.xml.registry.util.QueryUtil qu = it.cnr.icar.eric.client.xml.registry.util.QueryUtil.getInstance();

    BusinessQueryManagerImpl(RegistryServiceImpl regService,
        BusinessLifeCycleManagerImpl lcm) throws JAXRException {
        super(regService, lcm,
            (DeclarativeQueryManagerImpl) (regService.getDeclarativeQueryManager()));
    }

    public BulkResponse findAssociations(@SuppressWarnings("rawtypes") Collection findQualifiers,
        String sourceObjectId, String targetObjectId,
        @SuppressWarnings("rawtypes") Collection associationTypes) throws JAXRException {
    	
//        String queryStr = "SELECT * FROM Association ";
//        
//        String sourceObjectPred = null;
//        String targetObjectPred = null;
//        String assocTypePred = null;
//
//        boolean predicatesExist = false;
//
//        if (sourceObjectId != null) {
//            predicatesExist = true;
//            sourceObjectPred = " (sourceObject = '" + sourceObjectId + "') ";
//            predicatesExist = true;
//        }
//
//        if (targetObjectId != null) {
//            predicatesExist = true;
//            targetObjectPred = " (targetObject = '" + targetObjectId + "') ";
//        }
//
//        if ((associationTypes != null) && (associationTypes.size() > 0)) {
//            predicatesExist = true;
//            assocTypePred = " ( associationType IN ( ";
//
//            int cnt = 0;
//            Iterator iter = associationTypes.iterator();
//
//            while (iter.hasNext()) {
//                Object assTypeObj = iter.next();
//                String assTypeId = null;
//
//                if (assTypeObj instanceof Concept) {
//                    assTypeId = ((Concept) assTypeObj).getKey().getId();
//                } else if (assTypeObj instanceof String) {
//                    assTypeId = ((String) assTypeObj);
//                } else {
//                    throw new JAXRException(
//                        JAXRResourceBundle.getInstance().getString("message.error.expecting.concept.id",new Object[] {assTypeObj.getClass()}));
//                }
//
//                if (cnt++ > 0) {
//                    assocTypePred += ", ";
//                }
//
//                assocTypePred += ("'" + assTypeId + "'");
//            }
//
//            assocTypePred += " )) ";
//        }
//
//        if (predicatesExist) {
//            queryStr += " WHERE ";
//        }
//
//        if (sourceObjectPred != null) {
//            queryStr += sourceObjectPred;
//        }
//
//        if (targetObjectPred != null) {
//            if (sourceObjectPred != null) {
//                queryStr += " AND ";
//            }
//
//            queryStr += targetObjectPred;
//        }
//
//        if (assocTypePred != null) {
//            if ((sourceObjectPred != null) || (targetObjectPred != null)) {
//                queryStr += " AND ";
//            }
//
//            queryStr += assocTypePred;
//        }
//
//        //System.err.println(queryStr);
//        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
    	
    	Query query = SQLQueryProvider.findAssociations(dqm, sourceObjectId, targetObjectId, associationTypes);
    	    	
        BulkResponse resp = dqm.executeQuery(query);

        return resp;
    }

    public BulkResponse findCallerAssociations(@SuppressWarnings("rawtypes") Collection findQualifiers,
        Boolean confirmedByCaller, Boolean confirmedByOtherParty,
        @SuppressWarnings("rawtypes") Collection associationTypes) throws JAXRException {
        // TODO: implement findQualifiers support later
        if ((confirmedByCaller == null) && (confirmedByOtherParty == null) &&
                (associationTypes == null)) {
            return new BulkResponseImpl();
        }

        // TODO: xxx pa 111228 findConceptByPath() dependency
        
        //Find all Associations owned by caller's user ($currentUser is resolved by registry automatically)
        String qs =
            "SELECT DISTINCT a.* FROM Association a, AuditableEvent e, AffectedObject o, Slot s1, Slot s2 WHERE " +
            "e.user_ = $currentUser AND ( e.eventType = '" +
            BindingUtility.CANONICAL_EVENT_TYPE_ID_Created +
            "' OR e.eventType = '" +
            BindingUtility.CANONICAL_EVENT_TYPE_ID_Versioned +
            "' OR e.eventType = '" +
            BindingUtility.CANONICAL_EVENT_TYPE_ID_Relocated +
            "') AND o.eventId = e.id AND (o.id = a.sourceObject OR o.id = a.targetObject)";

        if (associationTypes != null) {
            //Add predicate for associationType filter
            Iterator<?> iter = associationTypes.iterator();

            while (iter.hasNext()) {
                Object obj = iter.next();

                String assocTypeId = null;

                if (obj instanceof Concept) {
                    assocTypeId = ((Concept) obj).getKey().getId();
                } else if (obj instanceof String) {
                    String str = (String) obj;

                    if (str.startsWith("urn:uuid")) {
                        //str is already the assocTypeId
                        assocTypeId = str;
                    } else {
                        //Assume str is the code
                        Concept c = findConceptByPath("/" +
                                BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType +
                                "%/" + str);
                        assocTypeId = (c).getKey().getId();
                    }
                }

                if (assocTypeId != null) {
                    qs += (" AND a.associationType = '" + assocTypeId + "'");
                }
            }
        }

        //Do further filtering based upon confirmedByCaller and confirmedByOtherParty if needed.
        if (confirmedByCaller != null) {
            if (confirmedByCaller.booleanValue()) {
                //ass is confirmed by caller
                qs += (" AND (s1.parent = a.id AND s1.sequenceId = 1 AND ((s1.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
                "' AND s1.value = $currentUser)" + " OR (s1.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
                "' AND s1.value = $currentUser)))");
            } else {
                //ass is NOT confirmed by caller
                qs += (" AND NOT (s1.parent = a.id AND s1.sequenceId = 1 AND ((s1.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
                "' AND s1.value = $currentUser)" + " OR (s1.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
                "' AND s1.value = $currentUser)))");
            }
        }

        if (confirmedByOtherParty != null) {
            if (confirmedByOtherParty.booleanValue()) {
                //ass is confirmed by other party
                qs += (" AND (s2.parent = a.id AND s2.sequenceId = 1 AND ((s2.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
                "' AND s2.value != $currentUser)" + " OR (s2.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
                "' AND s2.value != $currentUser)))");
            } else {
                //ass is NOT confirmed by other party
                qs += (" AND NOT (s2.parent = a.id AND s2.sequenceId = 1 AND ((s2.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_SRC_OWNER +
                "' AND s2.value != $currentUser)" + " OR (s2.name_ = '" +
                BindingUtility.IMPL_SLOT_ASSOCIATION_IS_CONFIRMED_BY_TARGET_OWNER +
                "' AND s2.value != $currentUser)))");
            }
        }

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
        BulkResponse br = dqm.executeQuery(query);

        return br;
    }

    //??JAXR 2.0
    @SuppressWarnings("rawtypes")
	public BulkResponse findObjects(String objectType,
        Collection findQualifiers, Collection namePatterns,
        Collection classifications, Collection specifications,
        Collection externalIdentifiers, Collection externalLinks)
        throws JAXRException {

        @SuppressWarnings("unchecked")
		Query query = createQueryByName(findQualifiers, objectType, namePatterns);
        query = addClassifications(query, classifications, findQualifiers);
        query = addExternalIdentifiers(query, externalIdentifiers, findQualifiers);
        query = addExternalLinks(query, externalLinks, findQualifiers);
        //TODO: Add specifications.
        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

    /**
     * Finds all Organizations that match ALL of the criteria specified by
     * the parameters of this call.  This is a Logical AND operation
     * between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing Collection of Organizations
     *
     */
    @SuppressWarnings("rawtypes")
	public BulkResponse findOrganizations(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection specifications, Collection externalIdentifiers,
        Collection externalLinks) throws JAXRException {
        @SuppressWarnings("unchecked")
		Query query = createQueryByName(findQualifiers, "Organization",
                namePatterns);
        query = addClassifications(query, classifications, findQualifiers);
        query = addSpecifications(query, specifications, findQualifiers);
        query = addExternalIdentifiers(query, externalIdentifiers, findQualifiers);
        query = addExternalLinks(query, externalLinks, findQualifiers);
        
        //TODO: Add specifications.

        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

    /**
     * Finds all Services that match ALL of the criteria specified by the
     * parameters of this call.  This is a Logical AND operation between
     * all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     *
     * @param orgKey Key identifying an Organization. Required for UDDI
     * providers.
     */
    @SuppressWarnings("rawtypes")
	public BulkResponse findServices(Key orgKey, Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection specifications) throws JAXRException {
    	
        @SuppressWarnings("unchecked")
		Query query = createQueryByName(findQualifiers, "Service", namePatterns);
        query = addClassifications(query, classifications, findQualifiers);
        query = addServiceSpecifications(query, specifications, findQualifiers);
        //TODO: Add specifications
        
        query = addOrgKeyPredicate(query, orgKey);
        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

    /**
     * Adds a predicate restricting query to service associated with specified
     * organization.
     *
     * @param query Existing query
     * @param orgKey Key of associated organization to which to restrict query
     *        result
     * @throws JAXRException if an error occurs
     * @return Updated query
     */
    private Query addOrgKeyPredicate(Query query, Key orgKey)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);

        if ((orgKey != null) && !orgKey.getId().equals("")) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(PRIMARY_TABLE_NAME +
                ".id in (select s.id from service s, association a, organization o where " +
                "s.id = a.targetobject and a.associationtype = '" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_OffersService +
                "' and a.sourceobject = '" + orgKey.getId() + "') ");
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }

    /**
     * Finds all ServiceBindings that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param serviceKey Key identifying a Service. Required for UDDI providers.
     *
     *
     * @return BulkResponse containing Collection of ServiceBindings
     */
    @SuppressWarnings("rawtypes")
	public BulkResponse findServiceBindings(Key serviceKey,
        Collection findQualifiers, Collection classifications,
        Collection specifications) throws JAXRException {
    	
        Query query = createQueryByName(findQualifiers, "ServiceBinding", null);
        
        query = addServiceKeyPredicate(query, serviceKey);
        query = addClassifications(query, classifications, findQualifiers);

        // specifications are specification RegistryObjects 
        query = addSpecifications(query, specifications, findQualifiers);
        
        //TODO: Add specifications
        
        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

    /**
     * Adds a predicate restricting query to service associated with specified
     * service.
     *
     * @param query Existing query
     * @param serviceKey Key of associated Service to which to restrict result
     * @throws JAXRException if an error occurs
     * @return Updated query
     */
    private Query addServiceKeyPredicate(Query query, Key serviceKey)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);

        if (serviceKey != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append("service = '" + serviceKey.getId() + "' ");
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }

    /**
     * Finds all ClassificationSchemes that match ALL of the criteria
     * specified by the parameters of this call.  This is a Logical AND
     * operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @return BulkResponse containing Collection of ClassificationSchemes
     */
    @SuppressWarnings("rawtypes")
	public BulkResponse findClassificationSchemes(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection externalLinks) throws JAXRException {
        @SuppressWarnings("unchecked")
		Query query = createQueryByName(findQualifiers, "ClassificationScheme",
                namePatterns);
        query = addClassifications(query, classifications, findQualifiers);
        query = addExternalLinks(query, externalLinks, findQualifiers);
        
        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

    /**
     * Find a ClassificationScheme by name based on the specified name pattern.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param namePattern Is a String that is a partial or full
     * name pattern with wildcard searching as specified by the SQL-92 LIKE
     * specification.
     *
     * @return The ClassificationScheme matching the namePattern. If none match
     * return null. If multiple match then throw an InvalidRequestException.
     *
     */
    public ClassificationScheme findClassificationSchemeByName(
        @SuppressWarnings("rawtypes") Collection findQualifiers, String namePattern)
        throws JAXRException {
        Collection<String> namePatterns = new ArrayList<String>();
        namePatterns.add(namePattern);

        Query query = createQueryByName(findQualifiers, "ClassificationScheme",
                namePatterns);
        BulkResponse br = dqm.executeQuery(query);
        query = addOrderBy(query, findQualifiers);

        Iterator<?> i = br.getCollection().iterator();
        ClassificationScheme cs = null;

        if (i.hasNext()) {
            cs = (ClassificationScheme) i.next();
        }

        // needs to check if more then 1 return and raise InvalidRequestException
        if (i.hasNext()) {
            throw new InvalidRequestException(
                JAXRResourceBundle.getInstance().getString("message.error.call.not.match.ClassificationScheme"));
        }

        return cs;
    }

    /**
     * Finds all Concepts that match ALL of the criteria specified by the
     * parameters of this call.  This is a Logical AND operation between
     * all non-null parameters.
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param findQualifier specifies qualifiers that effect string
     * matching, sorting etc.
     *
     *
     * @return BulkResponse containing Collection of Concepts
     */
    @SuppressWarnings("rawtypes")
	public BulkResponse findConcepts(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection externalIdentifiers, Collection externalLinks)
        throws JAXRException {
        @SuppressWarnings("unchecked")
		Query query = createQueryByName(findQualifiers, "ClassificationNode",
                namePatterns);
        query = addClassifications(query, classifications, findQualifiers);
        query = addExternalIdentifiers(query, externalIdentifiers, findQualifiers);
        query = addExternalLinks(query, externalLinks, findQualifiers);
        
        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

    /**
     * Find a Concept based on the path specified.
     * If specified path matches more than one ClassificationScheme then
     * the one that is most general (higher in the concept hierarchy) is returned.
     *
     *
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param path Is a canonical path expression as defined in the JAXR specification that identifies the Concept.
     *
     */
    public Concept findConceptByPath(String path) throws JAXRException {

    	//Kludge to work around JAXR 1.0 spec weirdness
        path = fixConceptPathForEbXML(path);

        HashMap<String, String> queryParams = new HashMap<String, String>();
        String queryId = CanonicalConstants.CANONICAL_QUERY_GetClassificationNodeByPath;
        queryParams.put(CanonicalConstants.CANONICAL_SLOT_QUERY_ID, queryId);
        queryParams.put("$path", path);
        Query query = null;
        try {
            query = dqm.createQuery(Query.QUERY_TYPE_SQL);
        } catch (JAXRException ex) { 
            throw ex;
        } catch (Throwable t) {
            throw new JAXRException(t);
        }
        BulkResponse bResponse = dqm.executeQuery(query, queryParams);
        return (Concept) (((BulkResponseImpl) bResponse).getRegistryObject());
    }

    /**
     * Handles a quirk of the JAXR spec. Fix in JAXR 2.0 spec??
     * Replace schemeName with schemeId.
     * Prefix value with wild card to account for fact that it may be in different place in ebXML Registry
     *
     */
    private String fixConceptPathForEbXML(String path) {
        String newPath = path;

        //Get the first element of the path.
        StringTokenizer st = new StringTokenizer(path, "/");
        int cnt = st.countTokens();

        //JAXR 1.0 assumes only a single level below root scheme
        if (cnt == 2) {
            String firstElem = st.nextToken();

            //Replace firstElem with schemeId if firstElem is a pre-defined concept
            //name as defined in Appendix A of the JAXR specification.
            //Prefix value with wild card to account for fact that it may be in different place in ebXML Registry
            if (!(firstElem.startsWith("urn:"))) {
                String schemeId = (schemeNameToIdMap.get(firstElem));

                if (schemeId != null) {
                    String value = st.nextToken();
                    newPath = "/" + schemeId + "%/" + value;
                }
            }
        }

        return newPath;
    }

    /**
     * Find all Concept that match the path specified. For JAXR 2.0??
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     * @param path Is a canonical path expression as defined in the JAXR specification that identifies the Concept.
     *
     */
    @SuppressWarnings("rawtypes")
	public Collection findConceptsByPath(String path) throws JAXRException {
//        String likeOrEqual = "=";
//
//        if (path.indexOf('%') != -1) {
//            likeOrEqual = LIKE_KEYWORD;
//        }
//
//        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL,
//                "SELECT cn.* from ClassificationNode cn WHERE cn.path " +
//                likeOrEqual + " '" + path + "' ORDER BY cn.path ASC");
//        BulkResponse resp = dqm.executeQuery(query);

    	Query query = SQLQueryProvider.findConceptsByPath(dqm, path);
    	BulkResponse resp = dqm.executeQuery(query);
    	    	
        return resp.getCollection();
    }

    /**
     * Finds all RegistryPackages that match ALL of the criteria specified by the parameters of this call.
     * This is a Logical AND operation between all non-null parameters.
     *
     * <p><DL><DT><B>Capability Level: 1 </B></DL>
     *
     *
     * @param findQualifier specifies qualifiers that effect string matching, sorting etc.
     *
     * @return BulkResponse containing Collection of RegistryPackages
     */
    @SuppressWarnings("rawtypes")
	public BulkResponse findRegistryPackages(Collection findQualifiers,
        Collection namePatterns, Collection classifications,
        Collection externalLinks) throws JAXRException {
        @SuppressWarnings("unchecked")
		Query query = createQueryByName(findQualifiers, "RegistryPackage",
                namePatterns);
        query = addClassifications(query, classifications, findQualifiers);
        query = addExternalLinks(query, externalLinks, findQualifiers);
        
        query = addOrderBy(query, findQualifiers);

        return dqm.executeQuery(query);
    }

//    static private String namePatternsToLikeExpr(Collection namePatterns,
//        String term, boolean caseSensitive, boolean exactNameMatch,
//        int sortByName) {
//        String likeOrEqual = LIKE_KEYWORD;
//
//        if (exactNameMatch == true) {
//            likeOrEqual = "=";
//        }
//
//        if (sortByName == SORT_NONE) {
//            if ((namePatterns == null) || (namePatterns.size() == 0)) {
//                return null;
//            } else if (namePatterns.size() == 1) {
//                Object[] namesArray = namePatterns.toArray();
//
//                if ((exactNameMatch == false) && (getNamePattern(namesArray[0]).equals("%"))) {
//                    return null;
//                }
//            }
//        } else {
//            // Need to have "LIKE '%'" as namePattern if none specified but name
//            // sorting is specified.
//            if ((namePatterns == null) || (namePatterns.size() == 0)) {
//                likeOrEqual = LIKE_KEYWORD;
//
//                namePatterns = new ArrayList();
//                namePatterns.add("%");
//            }
//        }
//
//        Iterator i = namePatterns.iterator();
//        StringBuffer result = new StringBuffer("(" +
//                caseSensitise(term, caseSensitive) + " " + likeOrEqual + " " +
//                caseSensitise("'" + getNamePattern(i.next()) + "'", caseSensitive));
//
//        while (i.hasNext()) {
//            result.append(" OR " + caseSensitise(term, caseSensitive) + " " +
//                likeOrEqual + " " +
//                caseSensitise("'" + getNamePattern(i.next()) + "'", caseSensitive));
//        }
//
//        return result.append(")").toString();
//    }
    
//    private static String getNamePattern(Object o) {
//        String namePattern = o.toString();
//        if (o instanceof LocalizedString) {
//            try {
//                namePattern = ((LocalizedString)o).getValue();
//            } catch (JAXRException e) {
//                //Cant happen
//                log.error(e);
//            }
//        }
//        
//        return namePattern;
//    } 

    // TODO: xxx pa 111228 moved copy to SQLQueryProvider
    public static String caseSensitise(String term, boolean caseSensitive) {
        String newTerm = term;

        if (!caseSensitive) {
            newTerm = "UPPER(" + term + ")";
        }

        return newTerm;
    }

    @SuppressWarnings("unused")
	static private String classificationToConceptId(Object obj)
        throws JAXRException {
        if (!(obj instanceof Classification)) {
            throw new UnexpectedObjectException(
                JAXRResourceBundle.getInstance().getString("message.error.expected.collection.objectType.Classification"));
        }

        Classification cl = (Classification) obj;

        if (cl.isExternal()) {
            throw new JAXRException(
                JAXRResourceBundle.getInstance().getString("message.error.no.support.external.classification.qaulifier"));
        }

        Concept concept = cl.getConcept();

        if (concept == null) {
            throw new JAXRException(
                JAXRResourceBundle.getInstance().getString("message.error.internal.classification.concept.null"));
        }

        return concept.getKey().getId();
    }

    /**
	 * Creates a Query based on specified parameters
	 * 
	 * @param findQualifiers
	 *            UDDI find qualifiers to apply
	 * @param tableName
	 *            Database table from which to select objects
	 * @param namePatterns
	 *            Collection of Strings that are patterns of names to match
	 * @throws JAXRException
	 *             if an error occurs
	 * @return New query
	 */
	private Query createQueryByName(Collection<?> findQualifiers, String tableName, Collection<String> namePatterns)
			throws JAXRException {
//		boolean caseSensitive = false;
//		boolean exactNameMatch = false;
//		int sortByName = SORT_NONE;
//
//		tableName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(tableName);
//		if (findQualifiers != null) {
//			if (findQualifiers.contains(FindQualifier.CASE_SENSITIVE_MATCH)) {
//				caseSensitive = true;
//			}
//
//			if (findQualifiers.contains(FindQualifier.EXACT_NAME_MATCH)) {
//				exactNameMatch = true;
//			}
//
//			if (findQualifiers.contains(FindQualifier.SORT_BY_NAME_ASC)) {
//				sortByName = SORT_ASC;
//			} else if (findQualifiers.contains(FindQualifier.SORT_BY_NAME_DESC)) {
//				sortByName = SORT_DESC;
//			}
//		}
//
//		StringBuffer qs = new StringBuffer("SELECT DISTINCT " + PRIMARY_TABLE_NAME + ".* FROM " + tableName + " "
//				+ PRIMARY_TABLE_NAME);
//		String likeExpr = namePatternsToLikeExpr(namePatterns, "n.value", caseSensitive, exactNameMatch, sortByName);
//
//		if (likeExpr != null) {
//			qs.append(", Name_ n " + WHERE_KEYWORD + " " + likeExpr + " AND n.parent = " + PRIMARY_TABLE_NAME + ".id");
//		}
//
//		return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
		
		return SQLQueryProvider.createQueryByName(dqm, findQualifiers, tableName, namePatterns);
		
	}

    /**
     * Adds an "ORDER BY" clause based on specified parameters
     *
     * @param query Query to which to append clause
     * @param findQualifiers UDDI find qualifiers to apply
     * @throws JAXRException if an error occurs
     * @return Possibly-modified query
     */
    private Query addOrderBy(Query query, Collection<?> findQualifiers)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);

        if (findQualifiers != null && checkOrderBy) {
            boolean caseInsensitiveSort = false;

            if (findQualifiers.contains(CASE_INSENSITIVE_SORT)) {
                caseInsensitiveSort = true;
            }

            int sortByName = SORT_NONE;

            if (findQualifiers.contains(FindQualifier.SORT_BY_NAME_ASC)) {
                sortByName = SORT_ASC;
            } else if (findQualifiers.contains(FindQualifier.SORT_BY_NAME_DESC)) {
                sortByName = SORT_DESC;
            }

            StringBuffer orderBy = new StringBuffer("");

            if (sortByName != SORT_NONE) {
                if (caseInsensitiveSort) {
                    orderBy.append(" ORDER BY UPPER('n.value')");
                } else {
                    orderBy.append(" ORDER BY n.value");
                }

                if (sortByName == SORT_ASC) {
                    orderBy.append(" ASC");
                } else {
                    orderBy.append(" DESC");
                }
            }

            int sortByDate = SORT_NONE;

            if (findQualifiers.contains(FindQualifier.SORT_BY_DATE_ASC)) {
                sortByDate = SORT_ASC;
            } else if (findQualifiers.contains(FindQualifier.SORT_BY_DATE_DESC)) {
                sortByDate = SORT_DESC;
            }

            if (sortByDate != SORT_NONE) {
                //TODO: Need to handle sort by date which is harder as it involves getting AuditTrail.
                if (orderBy.length() > 0) {
                    //orderBy += ", ";
                }
            }

            qs.append(orderBy);
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }

    private Query addClassifications(Query query, Collection<?> classifications, Collection<?> findQualifiers)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String expr = qu.getClassificationsPredicate(classifications,
                PRIMARY_TABLE_NAME + ".id", findQualifiers);

        if (expr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(expr);
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
    
    private Query addExternalIdentifiers(Query query, Collection<?> extIds, Collection<?> findQualifiers)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String expr = qu.getExternalIdentifiersPredicate((ArrayList<?>) extIds,
                PRIMARY_TABLE_NAME + ".id", findQualifiers);

        if (expr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(expr);
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
    
    private Query addExternalLinks(Query query, Collection<?> extLinks, Collection<?> findQualifiers)
        throws JAXRException {
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String expr = qu.getExternalLinksPredicate(extLinks,
                PRIMARY_TABLE_NAME + ".id", findQualifiers);

        if (expr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(expr);
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
    
    private Query addServiceSpecifications(Query query, Collection<?> specificationObjects, Collection<?> findQualifiers)
        throws JAXRException {
        // Don't bother changing query if no specifications specified.
        if (specificationObjects == null) {
            return query;
        }
        
        // ptn.id in (select sb.service from servicebinding sb where
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String expr = qu.getSpecificationLinksPredicate(specificationObjects,
                "sb.id", findQualifiers);

        if (expr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(PRIMARY_TABLE_NAME + ".id IN (SELECT sb.service FROM ServiceBinding sb WHERE ");
            qs.append(expr);
            qs.append(')');
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
    
    private Query addSpecifications(Query query, Collection<?> specificationObjects, Collection<?> findQualifiers)
        throws JAXRException {
        // Don't bother changing query if no specifications specified.
        if (specificationObjects == null) {
            return query;
        }
        
        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String expr = qu.getSpecificationLinksPredicate(specificationObjects,
                PRIMARY_TABLE_NAME + ".id", findQualifiers);

        if (expr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            } else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }

            qs.append(expr);
        } else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
    
}

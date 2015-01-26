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

import javax.xml.registry.infomodel.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.registry.FindQualifier;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.LocalizedString;


/**
 * Helper routines used to build ad hoc queries.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class QueryUtil {
    private static final QueryUtil INSTANCE = new QueryUtil();
    HashSet<String> unsupportedObjectTypes = new HashSet<String>();

    private QueryUtil() {
        //Not supoorted as a type
        unsupportedObjectTypes.add("ObjectType");
    }

    /**
     * Gets the single instance of QueryUtil.
     *
     * @return the QueryUtil instance.
     */
    public static QueryUtil getInstance() {
        return INSTANCE;
    }

    /*
    public StringBuffer addNamePredicate(StringBuffer selectStr, String namePattern) throws JAXRException {
        return addNameOrDescriptionPredicate(selectStr, namePattern, "Name", "nm");
    }

    public StringBuffer addDescriptionPredicate(StringBuffer selectStr, String descPattern) throws JAXRException {
        return addNameOrDescriptionPredicate(selectStr, descPattern, "Descripton", "des");
    }

    /*
     * Warning assumes that registry object aliasName is 'obj'.
     */
    /*
    private StringBuffer addNameOrDescriptionPredicate(StringBuffer selectStr, String pattern, String tblName, String aliasName) throws JAXRException {
        StringBuffer predStr = new StringBuffer();;

        if ((pattern != null) && (pattern.length() > 0)) {
            //Add name to tables in join
            selectStr.append(", " + tblName + " " + aliasName + " ");

            if ((pattern != null) && (pattern.length() != 0)) {
                predStr.append("((" + aliasName + ".parent = obj.id) AND (" +
                aliasName + ".value LIKE '" + pattern + "')) ");
            }
        }

        return predStr;
    }
    */
    /*
    public StringBuffer addClassificationsPredicate(StringBuffer selectSt, Collection classifications) throws JAXRException {

        String q = query.toString();
        StringBuffer qs = new StringBuffer(q);
        String clExpr = classificationsToExpr(classifications, PRIMARY_TABLE_NAME + ".id");

        if (clExpr != null) {
            if (q.indexOf(WHERE_KEYWORD) != -1) {
                // where clause already created
                qs.append(" AND ");
            }
            else {
                qs.append(" " + WHERE_KEYWORD + " ");
            }
            qs.append(clExpr);
        }
        else {
            // No qualifiers are specified
        }

        return dqm.createQuery(Query.QUERY_TYPE_SQL, qs.toString());
    }
     */
    
    /**
     * Sorts provided Classifications by their ClassificationScheme id.
     *
     * @param classifications Classifications to sort.
     * @throws JAXRException if an error occurs
     * @return sorted Classifications
     */
    public ArrayList<Object> sortClassificationsByClassificationScheme(
        @SuppressWarnings("rawtypes") Collection classifications) throws JAXRException {
        @SuppressWarnings("unchecked")
		ArrayList<Object> list = new ArrayList<Object>(classifications);

        Collections.sort(list,
            new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    int val = -1;

                    try {
                        if ((o1 instanceof Classification) &&
                                (o2 instanceof Classification)) {
                            String id1 = ((Classification) o1).getClassificationScheme()
                                          .getKey().getId();
                            String id2 = ((Classification) o2).getClassificationScheme()
                                          .getKey().getId();
                            val = id1.compareTo(id2);
                        }
                    } catch (javax.xml.registry.JAXRException e) {
                        e.printStackTrace();
                    }

                    return val;
                }

                public boolean equals(Object obj) {
                    return super.equals(obj);
                }
            });

        return list;
    }

    /**
     * Sorts provided ExternalIdentifiers by their ClassificationScheme id.
     *
     * @param extIds ExternalIdentifiers to sort.
     * @throws JAXRException if an error occurs
     * @return sorted ExternalIdentifiers
     */
    public ArrayList<?> sortExternalIdentifiersByClassificationScheme(ArrayList<?> extIds)
        throws JAXRException {
        ArrayList<Object> list = new ArrayList<Object>(extIds);

        Collections.sort(list,
            new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    int val = -1;

                    try {
                        if ((o1 instanceof ExternalIdentifier) &&
                                (o2 instanceof ExternalIdentifier)) {
                            String id1 = ((ExternalIdentifier) o1).getIdentificationScheme()
                                          .getKey().getId();
                            String id2 = ((ExternalIdentifier) o2).getIdentificationScheme()
                                          .getKey().getId();
                            val = id1.compareTo(id2);
                        }
                    } catch (javax.xml.registry.JAXRException e) {
                        e.printStackTrace();
                    }

                    return val;
                }

                public boolean equals(Object obj) {
                    return super.equals(obj);
                }
            });

        return list;
    }

    /**
     * Generates an SQL predicate restricting SQL query result to objects
     * with Classifications matching provided
     * Classifications.  Classifications are ANDed, unless
     * FindQualifier.OR_LIKE_KEYS is provided, in which case they are ORed.
     *
     * @param classifications Classifications to match.
     * @param term Name of primary table.
     * @param findQualifiers FindQualifiers that may modify generated predicate.
     * @throws JAXRException if an error occurs.
     * @return Predicate to be used as part of an SQL query
     */
    public String getClassificationsPredicate(@SuppressWarnings("rawtypes") Collection classifications,
        String term, Collection<?> findQualifiers)
        throws JAXRException {
        if ((classifications == null) || (classifications.size() == 0)) {
            return null;
        }

        if ((findQualifiers != null) &&
                (findQualifiers.contains(FindQualifier.OR_LIKE_KEYS))) {
            //Need to sort by scheme for OR_LIKE_KEYS
            classifications = sortClassificationsByClassificationScheme(classifications);
        }

        String result = "";
        Iterator<?> i = classifications.iterator();
        boolean first = true;
        String lastSchemeId = "";
        javax.xml.registry.infomodel.Classification cl = null;

        while (i.hasNext()) {
            cl = (Classification) i.next();

            String schemeId = cl.getClassificationScheme().getKey().getId();
            String classifiedObjSelect = " IN (SELECT cls.classifiedObject FROM Classification cls WHERE (";

            if (cl.isExternal()) {
                classifiedObjSelect += ("cls.classificationScheme='" +
                schemeId + "' AND " + "cls.nodeRepresentation='" +
                cl.getValue() + "'))");
            } else {
                classifiedObjSelect += ("cls.classificationNode IN ( SELECT node.id FROM ClassificationNode node WHERE (node.path LIKE '" +
                cl.getConcept().getPath() + "' OR node.path LIKE '" +
                cl.getConcept().getPath() + "/%'))))");
            }

            String predicate = "";

            if (first) {
                predicate = "(" + term + classifiedObjSelect + ")";
                first = false;
            } else {
                predicate = " AND " + "(" + term + classifiedObjSelect + ")";

                if ((findQualifiers != null) &&
                        ((findQualifiers.contains(FindQualifier.OR_ALL_KEYS)) ||
                        ((findQualifiers.contains(FindQualifier.OR_LIKE_KEYS)) &&
                        (schemeId.equals(lastSchemeId))))) {
                    predicate = " OR " + "(" + term + classifiedObjSelect +
                        ")";
                }
            }

            result += predicate;

            lastSchemeId = schemeId;
        }

        return result;
    }

    /**
     * Generates an SQL predicate restricting SQL query result to objects
     * with ExternalIdentifiers matching provided
     * ExternalIdentifiers.  ExternalIdentifiers are ANDed, unless
     * FindQualifier.OR_LIKE_KEYS is provided, in which case they are ORed.
     *
     * @param extIds ExternalIdentifiers to match.
     * @param term Name of primary table.
     * @param findQualifiers FindQualifiers that may modify generated predicate.
     * @throws JAXRException if an error occurs.
     * @return Predicate to be used as part of an SQL query
     */
    public String getExternalIdentifiersPredicate(ArrayList<?> extIds,
        String term, Collection<?> findQualifiers) throws JAXRException {
        if ((extIds == null) || (extIds.size() == 0)) {
            return null;
        }

        if ((findQualifiers != null) &&
                (findQualifiers.contains(FindQualifier.OR_LIKE_KEYS))) {
            //Need to sort by scheme for OR_LIKE_KEYS
            extIds = sortExternalIdentifiersByClassificationScheme(extIds);
        }

        String result = "";
        Iterator<?> i = extIds.iterator();
        boolean first = true;
        String lastSchemeId = "";
        javax.xml.registry.infomodel.ExternalIdentifier extId = null;

        while (i.hasNext()) {
            extId = (javax.xml.registry.infomodel.ExternalIdentifier) i.next();

            String schemeId = extId.getIdentificationScheme().getKey().getId();
            String registryObjectSelect = " IN (SELECT extId.registryObject FROM ExternalIdentifier extId WHERE (";

            registryObjectSelect += ("extId.identificationScheme='" + schemeId +
            "' AND " + "extId.value='" + extId.getValue() + "'))");

            String predicate = "";

            if (first) {
                predicate = "(" + term + registryObjectSelect + ")";
                first = false;
            } else {
                predicate = " AND " + "(" + term + registryObjectSelect + ")";

                if ((findQualifiers != null) &&
                        ((findQualifiers.contains(FindQualifier.OR_ALL_KEYS)) ||
                        ((findQualifiers.contains(FindQualifier.OR_LIKE_KEYS)) &&
                        (schemeId.equals(lastSchemeId))))) {
                    predicate = " OR " + "(" + term + registryObjectSelect +
                        ")";
                }
            }

            result += predicate;

            lastSchemeId = schemeId;
        }

        return result;
    }

    /**
     * Generates an SQL predicate restricting SQL query result to objects
     * with ExternalLinks matching provided
     * ExternalLinks.  ExternalLinks are ANDed, unless
     * <code>FindQualifier.OR_LIKE_KEYS</code> is provided, in which case they are ORed.
     *
     * @param extLinks ExternalLinks to match.
     * @param term Name of primary table.
     * @param findQualifiers FindQualifiers that may modify generated predicate.
     * @throws JAXRException if an error occurs.
     * @return Predicate to be used as part of an SQL query
     */
    @SuppressWarnings("unused")
	public String getExternalLinksPredicate(Collection<?> extLinks, String term,
        Collection<?> findQualifiers) throws JAXRException {
        if ((extLinks == null) || (extLinks.size() == 0)) {
            return null;
        }

        boolean caseSensitive = false;
        boolean exactNameMatch = false;
        boolean orLinks = false;
        
        if (findQualifiers != null) {
            if (findQualifiers.contains(FindQualifier.CASE_SENSITIVE_MATCH)) {
                caseSensitive = true;
            }

            if (findQualifiers.contains(FindQualifier.OR_LIKE_KEYS)) {
                orLinks = true;
            }

            if (findQualifiers.contains(FindQualifier.EXACT_NAME_MATCH)) {
                exactNameMatch = true;
            }
        }
        
        String andORor = " AND ";
        if (orLinks) {
            andORor = " OR ";
        }

        //SELECT * from RegistryObject obj WHERE obj.id IN 
        //(SELECT ass.targetObject from Association ass WHERE 
        //ass.associationType = 'ExternallyLinks' AND ass.sourceObject IN 
        //(SELECT id from ExternalLink extLink 
        //WHERE extLink.externalURI LIKE '%'))
        StringBuffer pred = new StringBuffer();
        boolean first = true;
        Iterator<?> iter = extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();

            if (!first) {
                pred.append(andORor);
            } else {
                first = false;
            }

            pred.append("(" + term +
                " IN (SELECT ass.targetObject FROM Association ass WHERE ass.associationType = '" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks +
                "' AND ass.sourceObject IN (SELECT extLink.id from ExternalLink extLink WHERE extLink.externalURI = '" +
                extLink.getExternalURI() + "'");
            
            InternationalString extLinkName = extLink.getName();
            if (extLinkName != null) {
                Collection<?> localizedStrings = extLinkName.getLocalizedStrings();
                
                int nameCount = 0;
                StringBuffer names = new StringBuffer();
                
                Iterator<?> lsIter = localizedStrings.iterator();
                
                while (lsIter.hasNext()) {
                    String name = ((LocalizedString) lsIter.next()).getValue();
                    
                    if ((name != null) && !name.equals("")) {
                        nameCount++;
                        
                        names.append(" '" + name + "' ");
                    }
                }

                if (nameCount > 0) {
                    pred.append(" AND extLink.id IN (SELECT n.parent FROM Name_ n WHERE n.value in (" + names + ") ");
                }
            }
            pred.append(")))");
        }

        return pred.toString();
    }

    /**
     * Generates an SQL predicate restricting SQL query result to objects
     * with Specifications matching provided
     * Specifications.  Specifications are ANDed, unless
     * FindQualifier.OR_LIKE_KEYS is provided, in which case they are ORed.
     *
     * The query being formed is something like this:
     *
     * SELECT * FROM ServiceBinding binding WHERE
     * binding.id IN (
     *   SELECT specLink.serviceBinding FROM SpecificationLink specLink WHERE
     *   specLink.specificationObject = specObject.id
     *   )
     *
     * @param specificationObjects Specifications to match.
     * @param term Name of primary table (e.g. binding.id or service.id)
     * @param findQualifiers FindQualifiers that may modify generated predicate.
     * @throws JAXRException if an error occurs.
     * @return Predicate to be used as part of an SQL query
     */
	public String getSpecificationLinksPredicate(Collection<?> specificationObjects, String term, Collection<?> findQualifiers)
			throws JAXRException {

		if ((specificationObjects == null) || (specificationObjects.size() == 0)) {
			return null;
		}

		String result = "";
		Iterator<?> i = specificationObjects.iterator();
        boolean first = true;

        String lastSpecObjectId = "";
        RegistryObject specificationObject = null;

        while (i.hasNext()) {
            specificationObject = (RegistryObject) i.next();

            // If the specObject is temporary, it might not have an id.
            String specObjectId = "";
            if ((specificationObject.getKey() != null) && (specificationObject.getKey().getId() != null)) {
            	specObjectId = specificationObject.getKey().getId();
            }
                        
            String registryObjectSelect = " IN (SELECT specLink.serviceBinding FROM SpecificationLink specLink WHERE (";

            registryObjectSelect += ("specLink.specificationObject = '" + specObjectId + "'))");

            String predicate = "";

            if (first) {
                predicate = "(" + term + registryObjectSelect + ")";
                first = false;
            } else {
                predicate = " AND " + "(" + term + registryObjectSelect + ")";

                if ((findQualifiers != null) &&
                        ((findQualifiers.contains(FindQualifier.OR_ALL_KEYS)) ||
                        ((findQualifiers.contains(FindQualifier.OR_LIKE_KEYS)) &&
                        (specObjectId.equals(lastSpecObjectId))))) {
                    predicate = " OR " + "(" + term + registryObjectSelect +
                        ")";
                }
            }

            result += predicate;

            lastSpecObjectId = specObjectId;
        }
        
        return result;
    }

    /**
     * Gets the set of registry object types that are not supported in queries.
     *
     * @return Set of object types.
     */
    public Set<String> getUnsupportedObjectTypes() {
        return unsupportedObjectTypes;
    }
}

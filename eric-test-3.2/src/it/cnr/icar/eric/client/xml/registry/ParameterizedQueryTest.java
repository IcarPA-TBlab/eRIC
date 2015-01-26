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

import it.cnr.icar.eric.client.common.ClientTest;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Concept;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for QueryManager
 *
 * @author Farrukh Najmi
 */
public class ParameterizedQueryTest extends ClientTest {
    
    public ParameterizedQueryTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ParameterizedQueryTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new ParameterizedQueryTest("testArbitraryQuery"));
        return suite;
    }
    

    /**
     * Test arbitrary query.
     */
    public void testArbitraryQuery() throws Exception {
        String queryId = CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery;
        Map<String, String> queryParams = new HashMap<String, String>();
        @SuppressWarnings("unused")
		String id = it.cnr.icar.eric.common.Utility.getInstance().createId();
        queryParams.put("$query", "SELECT * FROM ClassificationScheme");
        Collection<?> registryObjects = executeQuery(queryId, queryParams);
        assertTrue(registryObjects.size() > 0);
    }    
    
    public void testParameterizedQuery() throws Exception {
        /* TODO: This test getting OutOfMemory error as it fetches all objects for 
         each ObjectType which does not currently scale.
         * Currently, object types scaled down to a sample: Service, Service Binding
         * and Organization
         */
        Collection<?> objectTypes = getObjectTypes();
        Map<String, String> parameters = new HashMap<String, String>();
        @SuppressWarnings("static-access")
		String paramQueryURN = BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID;
        parameters.put(paramQueryURN, "urn:freebxml:registry:query:BusinessQuery");
        testObjectTypes(parameters, objectTypes);
    }
    
    private void testObjectTypes(Map<String, String> parameters, Collection<?> objectTypes) 
        throws InvalidRequestException, JAXRException {
        Iterator<?> itr = objectTypes.iterator();
        while (itr.hasNext()) {
            String objectType = (String)itr.next();
            parameters.put("$objectTypePath", objectType);
            Query query = (dqm)
                .createQuery(Query.QUERY_TYPE_SQL);
            BulkResponse bResponse = (dqm)
                .executeQuery(query, parameters);
            Collection<?> registryObjects = bResponse.getCollection();
            Iterator<?> roItr = registryObjects.iterator();
            Object registryObject = null;
            if (roItr.hasNext()) {
                registryObject = roItr.next();
            }
            assertNotNull("Query returned no objects of type "+objectType, registryObject);
        }
    }
    
    /* This method will return a Collection of objectTypes that are found in
     * the minDB installation
     */
    private Collection<String> getObjectTypes() {
        ArrayList<String> list = new ArrayList<String>();
//        list.add("RegistryObject");
        list.add("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/Service");
        list.add("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/ServiceBinding");
//        list.add("Organization");
//        list.add("User");
//        list.add("ClassificationScheme");
//        list.add("ClassificationNode");
//        list.add("ExternalLink");
//        list.add("Association");
//        list.add("ExtrinsicObject");
//        list.add("AdhocQuery");
        return list;
    }

    @SuppressWarnings("unused")
	private Collection<String> getAllObjectTypes() throws JAXRException {   
        Collection<?> concepts = bqm.findConceptsByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject%");
        int size = concepts.size();
        ArrayList<String> list = new ArrayList<String>(size);
        Iterator<?> iter = concepts.iterator();
        while (iter.hasNext()) {
            Concept concept = (Concept) iter.next();
            String objectType = concept.getValue();
            list.add(objectType);
        }
        return list;
    }
    
    public static void main(String[] args) {
	System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}

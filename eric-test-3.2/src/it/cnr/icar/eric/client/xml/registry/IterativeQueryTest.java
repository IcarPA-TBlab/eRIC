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
import it.cnr.icar.eric.common.IterativeQueryParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Iterator;
import java.math.BigInteger;
import java.util.Map;
import java.util.HashMap;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
//import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 * jUnit Test for Iterative Queries
 *
 * @author Paul Sterk
 */
public class IterativeQueryTest extends ClientTest {

    public IterativeQueryTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(IterativeQueryTest.class);
        return suite;
    }

    @SuppressWarnings("static-access")
	public void testIterativeQueryAllRegistryObjects() throws Exception {
        String paramQueryURN = BindingUtility.getInstance().CANONICAL_SLOT_QUERY_ID;
        String paramQuery = "urn:freebxml:registry:query:BusinessQuery";
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(paramQueryURN, paramQuery);
        String parameter = "$objectTypePath";
        String parameterValue = "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType +
            "/RegistryObject";
        parameters.put(parameter, parameterValue);
        Query query = (dqm).createQuery(Query.QUERY_TYPE_SQL);
        int startIndex = 0;
        int maxResults = 25;
        IterativeQueryParams iterativeQueryParams =
            new IterativeQueryParams(startIndex, maxResults);
        BulkResponse bResponse = (dqm)
            .executeQuery(query, parameters, iterativeQueryParams);
        assertResponseSuccess("Error executing parameterized query: " + paramQuery +
            " parameter:" + parameter + " param value:" +parameterValue, bResponse);

    }

    //Test for potential bug if iterating more time than there is windows of data available
    //Test contributed by Kim Haase.
    public void testIteratingMoreThanNecessary() throws Exception {

        String pattern = "%Packa%";
	// TODO: Determine if repeated selection of objects with multiple
	// names leads to occasional failure of this test case.  Is
	// something (for example) performing an implicit "distinct" on the
	// result set?  Also, do those occasional failures occur when ORDER
	// BY claus is removed?
        String queryStr = "SELECT p.* FROM RegistryObject p, Name n WHERE " +
            "n.value LIKE '" + pattern + "' AND " +
            "n.parent = p.id ORDER BY n.value ASC";

        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse br = dqm.executeQuery(query);
        Collection<?> objects = br.getCollection();

        int maxResults = 10;
        int expectedIterations = (objects.size() / maxResults);
        int remainderObjects = objects.size() % maxResults;
        if (remainderObjects > 0) {
            expectedIterations++;
        }

        //Do the iterative queries, getting 10 results
        //at a time, and verify results
        int startIndex = 0;
        IterativeQueryParams iterativeQueryParams = new IterativeQueryParams(startIndex, maxResults);
        for (int i = 0; i < expectedIterations+3; i++) {
            // Execute query with iterative query 'in' params
            iterativeQueryParams.startIndex = startIndex;
            br = dqm.executeQuery(query, null, iterativeQueryParams);
            startIndex += maxResults;
            objects = br.getCollection();
            @SuppressWarnings("unused")
			Iterator<?> objIter = objects.iterator();

            if (i < expectedIterations-1) {
                assertEquals("Unexpected object count", maxResults, objects.size());
            } else if (i == expectedIterations-1) {
                assertEquals("Unexpected object count", remainderObjects, objects.size());
            } else {
                //??There is a server bug here in iterative query. Paul please assign this to your self. Thanks.
                assertEquals("This is a known failure that needs to be investigated and fixed by Paul. " + "Unexpected object count", 0, objects.size());
            }
        }
    }

    /**
     * Returns the name value for a registry object.
     *
     * @param ro        a RegistryObject
     * @return                the String value
     */
    @SuppressWarnings("unused")
	private String getName(RegistryObject ro) throws JAXRException {
        try {
            return ro.getName()
                     .getValue();
        } catch (NullPointerException npe) {
            return "No Name";
        }
    }

    /**
     * Returns the description value for a registry object.
     *
     * @param ro        a RegistryObject
     * @return                the String value
     */
    @SuppressWarnings("unused")
	private String getDescription(RegistryObject ro) throws JAXRException {
        try {
            return ro.getDescription()
                     .getValue();
        } catch (NullPointerException npe) {
            return "No Description";
        }
    }

    /**
     * Returns the key id value for a registry object.
     *
     * @param ro        a RegistryObject
     * @return                the String value
     */
    @SuppressWarnings("unused")
	private String getKey(RegistryObject ro) throws JAXRException {
        try {
            return ro.getKey()
                     .getId();
        } catch (NullPointerException npe) {
            return "No Key";
        }
    }

    public void testIterativeQuery() throws Exception {
        String queryStr = "SELECT p.* FROM RegistryPackage p, Name_ n WHERE " +
            "n.value LIKE 'IterativeQueryTestClientObject_%' AND n.parent = p.id ORDER BY n.value ASC";

        // Remove any old test packages
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse br = dqm.executeQuery(query);
        assertTrue("RegistryPackage query failed",
                    br.getStatus() == BulkResponse.STATUS_SUCCESS);
        Collection<?> ros = br.getCollection();
        if (ros.size() > 0) {
            List<Key> keys = new ArrayList<Key>();
            Iterator<?> itr = ros.iterator();
            while (itr.hasNext()) {
                Key key = ((RegistryObject)itr.next()).getKey();
                keys.add(key);
            }
            br = lcm.deleteObjects(keys);
            assertTrue("RegistryPackage delete failed",
                    br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }

        // Create test packages
        List<RegistryPackage> testPkgs = new ArrayList<RegistryPackage>();
        for (int i=0; i<100; i++) {
            String name = getTestObjectName(i);
            RegistryPackage pkg = lcm.createRegistryPackage(name);
            testPkgs.add(pkg);
        }
        br = lcm.saveObjects(testPkgs);
        assertTrue("RegistryPackage creation failed",
                    br.getStatus() == BulkResponse.STATUS_SUCCESS);

        //Now do the iterative queries and verify results
        int maxResults = 10;
        int startIndex = 0;
        int totalResultCount = 0;
        IterativeQueryParams iterativeQueryParams =
            new IterativeQueryParams(startIndex, maxResults);
        for (int i = 0; i < 100; i += 10) {

            // Execute query with iterative query 'in' params
            query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            iterativeQueryParams.startIndex = i;
            br = dqm.executeQuery(query, null, iterativeQueryParams);
            assertTrue("Iterative query execution failed",
                br.getStatus() == BulkResponse.STATUS_SUCCESS);

            // Get iterative query 'out' params from AdhocQueryResponseType
            RegistryResponseType response = ((BulkResponseImpl)br).getRegistryResponse();
            int numResultsInResponse = 0;
            if (response instanceof AdhocQueryResponse) {
                AdhocQueryResponse aqr = (AdhocQueryResponse) response;
                RegistryObjectListType list = aqr.getRegistryObjectList();
                numResultsInResponse = list.getIdentifiable().size();
                BigInteger count = aqr.getTotalResultCount();
                totalResultCount = Integer.parseInt(count.toString());
                BigInteger index = aqr.getStartIndex();
                startIndex = Integer.parseInt(index.toString());
            }

            // Check startIndex and totalResultCount
            assertTrue("Incorrect startIndex", i == startIndex);
            assertTrue("Incorrect totalResultCount", 100 == totalResultCount);



            // Check maxResults
            @SuppressWarnings("rawtypes")
			List<?> results = (List)br.getCollection();
            int resultSize = results.size();
            assertTrue("Result size " + resultSize + " not equal to maxResults "
                        + maxResults, resultSize == maxResults);

            // Check number of results returned in AdhocQueryResponseType with
            // maxResults
            assertTrue("Results size in response " + numResultsInResponse +
                       " not equal to maxResults "
                        + maxResults, numResultsInResponse == maxResults);

            // Check RegistryPackage names
            for (int j = 0; j < resultSize; j++) {
                RegistryPackage pkg = (RegistryPackage)results.get(j);
                String expectedName = getTestObjectName(i+j);
                String name = pkg.getName().getValue();
                assertEquals("Name did not match expectation", expectedName, name);
            }
        }

    }

    public void testIterativeQueryRegistryObject() throws Exception {
        String queryStr = " SELECT p.* FROM RegistryObject p, Name_ n WHERE "+
            "( p.objecttype IN (  SELECT id   FROM ClassificationNode "+
            "WHERE path LIKE '/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject/RegistryPackage' "+
            "OR path LIKE '/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject/RegistryPackage/%')) "+
            "AND n.value LIKE 'IterativeQueryTestClientObject_%' AND n.parent " +
            "= p.id ORDER BY n.value ASC";

        // Remove any old test packages
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        BulkResponse br = dqm.executeQuery(query);
        assertTrue("RegistryObject query failed",
                    br.getStatus() == BulkResponse.STATUS_SUCCESS);
        Collection<?> ros = br.getCollection();
        if (ros.size() > 0) {
            List<Key> keys = new ArrayList<Key>();
            Iterator<?> itr = ros.iterator();
            while (itr.hasNext()) {
                Key key = ((RegistryObject)itr.next()).getKey();
                keys.add(key);
            }
            br = lcm.deleteObjects(keys);
            assertTrue("RegistryObject delete failed",
                    br.getStatus() == BulkResponse.STATUS_SUCCESS);
        }

        // Create test packages
        List<RegistryPackage> testPkgs = new ArrayList<RegistryPackage>();
        for (int i=0; i<100; i++) {
            String name = getTestObjectName(i);
            RegistryPackage pkg = lcm.createRegistryPackage(name);
            testPkgs.add(pkg);
        }
        br = lcm.saveObjects(testPkgs);
        assertTrue("RegistryObject creation failed",
                    br.getStatus() == BulkResponse.STATUS_SUCCESS);

        //Now do the iterative queries and verify results
        int maxResults = 10;
        int startIndex = 0;
        int totalResultCount = 0;
        IterativeQueryParams iterativeQueryParams =
            new IterativeQueryParams(startIndex, maxResults);
        for (int i = 0; i < 100; i += 10) {

            // Execute query with iterative query 'in' params
            query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            iterativeQueryParams.startIndex = i;
            br = dqm.executeQuery(query, null, iterativeQueryParams);
            assertTrue("Iterative query execution failed",
                br.getStatus() == BulkResponse.STATUS_SUCCESS);

            // Get iterative query 'out' params from AdhocQueryResponseType
            RegistryResponseType response = ((BulkResponseImpl)br).getRegistryResponse();
            int numResultsInResponse = 0;
            if (response instanceof AdhocQueryResponse) {
                AdhocQueryResponse aqr = (AdhocQueryResponse) response;
                RegistryObjectListType list = aqr.getRegistryObjectList();
                numResultsInResponse = list.getIdentifiable().size();
                BigInteger count = aqr.getTotalResultCount();
                totalResultCount = Integer.parseInt(count.toString());
                BigInteger index = aqr.getStartIndex();
                startIndex = Integer.parseInt(index.toString());
            }

            // Check startIndex and totalResultCount
            assertTrue("Incorrect startIndex", i == startIndex);
            assertTrue("Incorrect totalResultCount", 100 == totalResultCount);



            // Check maxResults
            List<?> results = (List<?>)br.getCollection();
            int resultSize = results.size();
            assertTrue("Result size " + resultSize + " not equal to maxResults "
                        + maxResults, resultSize == maxResults);

            // Check number of results returned in AdhocQueryResponseType with
            // maxResults
            assertTrue("Results size in response " + numResultsInResponse +
                       " not equal to maxResults "
                        + maxResults, numResultsInResponse == maxResults);

        }

    }

    public void testIterativeQueryTotalResultsReturned() throws Exception {
        String queryStr = " SELECT r.* FROM RegistryPackage r, Name n where "+
            "n.value like '%IterativeQueryTestClientObject%' and n.parent = r.id";

        //Now do the iterative queries and verify results
        int maxResults = 10;
        int startIndex = 0;
        int totalResultCount = 0;
        int sumOfResults = 0;
        int numResultsInResponse = 0;
        IterativeQueryParams iterativeQueryParams =
            new IterativeQueryParams(startIndex, maxResults);
        do {
            // Execute query with iterative query 'in' params
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            iterativeQueryParams.startIndex = startIndex;
            BulkResponse br = dqm.executeQuery(query, null, iterativeQueryParams);
            assertTrue("Iterative query execution failed",
                br.getStatus() == BulkResponse.STATUS_SUCCESS);

            // Get iterative query 'out' params from AdhocQueryResponseType
            RegistryResponseType response = ((BulkResponseImpl)br).getRegistryResponse();
            if (response instanceof AdhocQueryResponse) {
                AdhocQueryResponse aqr = (AdhocQueryResponse) response;
                RegistryObjectListType list = aqr.getRegistryObjectList();
                numResultsInResponse = list.getIdentifiable().size();
                sumOfResults += numResultsInResponse;
                BigInteger count = aqr.getTotalResultCount();
                totalResultCount = Integer.parseInt(count.toString());
            }
            startIndex += maxResults;

        } while (numResultsInResponse > 0);

        assertTrue("Total result count " + totalResultCount +
                       " not equal to sum of all results " + sumOfResults,
                        totalResultCount == sumOfResults);

    }

    private String getTestObjectName(int index) {
        /* Following works for JDK 5.0
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.getDefault());
        formatter.format("%3d", index);
        String name = "IterativeQueryTestObject_" + sb;
        */

        //JDK 1.4 compatible
        String name = "IterativeQueryTestClientObject_";
        if (index < 10) {
            name += "00";
        } else if (index < 100) {
            name += "0";
        }
        name += index;
        return name;
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

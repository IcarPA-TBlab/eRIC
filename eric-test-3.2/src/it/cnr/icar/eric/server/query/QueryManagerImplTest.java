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
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
//import org.oasis.ebxml.registry.bindings.rs.RegistryResponse;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 *
 * @author najmi
 */
public class QueryManagerImplTest extends ServerTest {
    
    
    public QueryManagerImplTest(java.lang.String testName) {
        super(testName);        
    }
    
    /**
     * Test of submitAdhocQuery method, of class it.cnr.icar.eric.server.query.QueryManagerImpl.
     * Submits whatever query is in eric/misc/samples/SQLQuery_1.
     * This is useful in testing bugs in specific queries since the specific
     * query can be placed in the file eric/misc/samples/SQLQuery_1 and test
     * can be run standalone in debugger.
     *
     */
    public void testSubmitAdhocQuery() throws Exception {
        Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();		
        AdhocQueryRequest req = (AdhocQueryRequest)unmarshaller.unmarshal(new File("SQLQuery_1.xml"));
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testSubmitAdhocQuery", req);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        AdhocQueryResponse resp = qm.submitAdhocQuery(context);
        @SuppressWarnings({ "unused" })
		List<JAXBElement<? extends IdentifiableType>> res = resp.getRegistryObjectList().getIdentifiable();        
    }
    
    /**
     * Test of submitAdhocQuery method to get child objects of ClassificationScheme/Node and RegistryPackages
     */
    public void testSubmitAdhocQueryWithChildObjs() throws Exception {
        Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
        InputStream is = getClass().getResourceAsStream("/resources/AdhocQuery_getChildObjects.xml");
        AdhocQueryRequest req = (AdhocQueryRequest)unmarshaller.unmarshal(is);
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testSubmitAdhocQuery", req);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        AdhocQueryResponse resp = qm.submitAdhocQuery(context);
        ClassificationSchemeType cs = (ClassificationSchemeType)resp.getRegistryObjectList().getIdentifiable().get(0).getValue();
        assertTrue(cs.getClassificationNode().size() > 0);
    }
    
    /**
     * Tests that a QueryPlugin that returns no matched objects does not create an error
     * as was seen by Sun bug: 6422859
     *
     * Test invokes FindById QueryPlugin with a non-existent Id
     * 
     */
    public void testEmptyResultByQueryPlugin() throws Exception {  
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$idPattern", "urn:freebxml:registry:some:non:existent:url");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testEmptyResultByQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetClassificationSchemesById, queryParamsMap);
        assertEquals("FindByIdQueryPlugin failed.", 0, res.size());
    }    
    
    /**
     * Tests invoking the FindByIdQueryPlugin.
     * 
     */
    public void testFindByIdQueryPlugin() throws Exception {        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$id", CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType);
        queryParamsMap.put("$tableName", "AdhocQuery");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testFindByIdQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType, queryParamsMap);
        assertEquals("FindByIdQueryPlugin failed.", 1, res.size());
    }
    
    /**
     * Tests invoking the GetSchemesByIdQueryPlugin.
     * 
     */
    public void testGetSchemesByIdQueryPlugin() throws Exception {        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$idPattern", CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType);
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetSchemesByIdQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetClassificationSchemesById, queryParamsMap);
        assertEquals("GetSchemesByIdQueryPlugin failed.", 1, res.size());
    }
    
    /**
     * Tests invoking the GetClassificationNodeByPathQueryPlugin
     * 
     */
    public void testGetClassificationNodeByPathQueryPlugin() throws Exception {        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();      
        String path = "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject";
        queryParamsMap.put("$path", path);
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetClassificationNodeByPathQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetClassificationNodeByPath, queryParamsMap);
        assertEquals("GetClassificationNodeByPathQueryPlugin failed.", 1, res.size());
    }
    
    /**
     * Tests invoking the GetClassificationNodeByPathQueryPlugin with an invalid 
     * path to check the API handles this condition gracefully.
     * 
     */
    public void testGetClassificationNodeByPathQueryPluginWithInvalidPath() throws Exception {        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();      
        String path = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject";
        queryParamsMap.put("$path", path);
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetClassificationNodeByPathQueryPluginWithInvalidPath", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_GetClassificationNodeByPath, queryParamsMap);
        // Since the path contains an invalid value, the res List should be empty.
        assertEquals("GetClassificationNodeByPathQueryPlugin failed.", 0, res.size());
    }
    
    /**
     * Tests invoking the ArbitraryQueryQueryPlugin.
     * 
     */
    public void testArbitraryQueryQueryPlugin() throws Exception {        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$query", "SELECT * FROM ClassificationScheme WHERE id LIKE '%ObjectType%'");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testArbitraryQueryQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_ArbitraryQuery, queryParamsMap);
        assertTrue("ArbitraryQueryQueryPlugin failed.", (res.size() > 0));
    }
    
    /**
     * Tests invoking of a stored parameterized query.
     * 
     */
    public void testStoredParameterizedQuery() throws Exception {        
        //queryStr does not really matter
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$objectTypePath", "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject/Organization");
        queryParamsMap.put("$name", "%sun%");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testStoredParameterizedQuery", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_BasicQuery, queryParamsMap);
        assertTrue("StoredQuery failed.", (res.size() > 0));
    }
    
    /**
     * Test of getContent method, of class it.cnr.icar.eric.server.query.QueryManagerImpl.
     */
    public void testGetContent() throws Exception {
        System.out.println("testGetContent");
        
        // Add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
    }
    
    /**
     * Test of getRegistryObject method, of class it.cnr.icar.eric.server.query.QueryManagerImpl.
     */
    public void testGetRegistryObject() throws Exception {
        System.out.println("testGetRegistryObject");
        
        //Get the default ACP that is expected in any minDB        
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetRegistryObject", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        RegistryObjectType ro = qm.getRegistryObject(context, "urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        if (ro == null) {
            fail("Failed getRegistryObject for default Access Control Policy using id: urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        }
    }
    
    /**
     * Tests that the getRegistryObject() method performs access control
     */
    public void testGetRegistryObjectAccessControl() throws Exception {
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetRegistryObjectAccessControl", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        RegistryObjectType ro = qm.getRegistryObject(context, "urn:freebxml:registry:demoDB:ExtrinsicObject:zeusDescription");
        assertTrue("This failure is expected when permitAllReads=true (default) in eric.properties. Failed to enforce access control on getRegistryObject()", (ro == null));
    }
    
    /**
     * Tests that the getRepositoryItem() method performs access control
     */
    public void testGetRepositoryItemAccessControl() throws Exception {
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetRepositoryItemAccessControl", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        RepositoryItem ri = qm.getRepositoryItem(context, "urn:freebxml:registry:demoDB:ExtrinsicObject:zeusDescription");
        assertTrue("Failed to enforce access control on getRepositoryItem()", (ri == null));
    }
    
    /**
     * Test of getRepositoryItem method, of class it.cnr.icar.eric.server.query.QueryManagerImpl.
     */
    public void testGetRepositoryItem() throws Exception {
        System.out.println("testGetRepositoryItem");
        
        //Get the default ACP that is expected in any minDB        
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetRepositoryItem", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        RepositoryItem ri = qm.getRepositoryItem(context, "urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        if (ri == null) {
            fail("This failure is expected when permitAllReads=true (default) in eric.properties. Failed getRepositoryItemr default Access Control Policy using id: urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        }
    }
    
    /**
     * Test the iterative query feature of QUeryManager.
     *
     * Create and save 100 Test objects whose names are IterativeQueryTestObject_nn.
     * Iterate 10 times and each time send iterative query to get next 10 objects.
     * Verify that corect objects were received.
     *
     */
    public void testIterativeQuery() throws Exception {
        int maxResults = 10;
        String queryStr = "SELECT p.* FROM RegistryPackage p, Name_ n WHERE n.value LIKE 'IterativeQueryTestObject_%' AND n.parent = p.id ORDER BY n.value ASC";
        //Remove any old objects
        RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(queryStr);
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testIterativeQuery", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        RegistryResponseType resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
        //ArrayList<RegistryPackageType> objects = new ArrayList<RegistryPackageType>();
        ArrayList<JAXBElement<RegistryPackageType>> objects = new ArrayList<JAXBElement<RegistryPackageType>>();

        for (int i=0; i<100; i++) {
            String name = getTestObjectName(i);
            RegistryPackageType pkg = createTestPackage(name);
            //objects.add(pkg);
            objects.add(bu.rimFac.createRegistryPackage(pkg));
        }
        
        //Create submit request 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
                
        //Now do the submit 
        context = new ServerRequestContext("QueryManagerImplTest:testIterativeQuery", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);

        //Now do the iterative queries and verify results
        for (int i=0; i<100;) {
            
            AdhocQueryRequest req = createAdhocQueryRequest(queryStr, null, i, maxResults);
            context = new ServerRequestContext("QueryManagerImplTest:testIterativeQuery", req);
            context.setUser(ac.farrukh);
            AdhocQueryResponse ahqr = qm.submitAdhocQuery(context);
            int startIndex = ahqr.getStartIndex().intValue();
            int totalResultCount = ahqr.getTotalResultCount().intValue();
            
            assertTrue("Incorrect startIndex", i == startIndex);
            assertTrue("Incorrect totalResultCount", 100 == totalResultCount);
            
            List<JAXBElement<? extends IdentifiableType>> res = ahqr.getRegistryObjectList().getIdentifiable();
            assertTrue("Result size greater than maxLentgh", res.size() <= maxResults);
            
            for (int j=0; j<res.size(); j++) {
                RegistryPackageType pkg = (RegistryPackageType)res.get(j).getValue();
                
                String expectedName = getTestObjectName(i+j);

                
                String name = bu.getInternationalStringAsString(pkg.getName());
                assertEquals("Name did not match expectation", expectedName, name);
            }
            
            i += 10;
        }
        
    }
    
    /**
     * Tests invoking the FindAllMyObjects.
     * 
     */
    public void testFindAllMyObjects() throws Exception {
        String id = "urn:freebxml:eric:server:query:QueryManagerImplTest:pkg1";
        // Test using this user:
        UserType user = AuthenticationServiceImpl.getInstance().farrukh;
        try {
            // Add a new test object
            RegistryPackageType pkg1 = bu.rimFac.createRegistryPackageType();
            pkg1.setId(id);
            //ArrayList<RegistryPackageType> objects = new ArrayList<RegistryPackageType>();
            //objects.add(pkg1);
            ArrayList<JAXBElement<RegistryPackageType>> objects = new ArrayList<JAXBElement<RegistryPackageType>>();
            objects.add(bu.rimFac.createRegistryPackage(pkg1));
            
            SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
            roList.getIdentifiable().addAll(objects);
            submitRequest.setRegistryObjectList(roList);
            HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
            ServerRequestContext serverContext = new ServerRequestContext("QueryManagerImplTest:testFindAllMyObjects", submitRequest);
            serverContext.setUser(user);
            serverContext.setRepositoryItemsMap(idToRepositoryItemMap);
            RegistryResponseType resp = lcm.submitObjects(serverContext);
            bu.checkRegistryResponse(resp);
            
            // Execute this query to get the total number of objects owned by the
            // user object above.
            HashMap<String, String> queryParamsMap = new HashMap<String, String>();
            String queryId = CanonicalConstants.CANONICAL_QUERY_FindAllMyObjects;
            queryParamsMap.put("$id", queryId);
            ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testFindAllMyObjects", null);
            context.setUser(user);
            List<?> res = executeQuery(context, queryId, queryParamsMap);        
            int numObjectsBeforeStatusUpdate = res.size();
            
            // Update the status of the newly added object to test if this 
            // query will return the correct total number of objects owned by the user
            setStatus(serverContext, id, BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved);
            
            // Execute this query again after setting the status
            res = executeQuery(context, queryId, queryParamsMap);
            int numObjectsAfterStatusUpdate = res.size();
            
            // Since we have only done a status update, the numObjectsBeforeStatusUpdate 
            // should be equal to numObjectsAfterStatusUpdate.  
            // If not, there is a bug in the query.
            assertEquals("testFindAllMyObjects failed. The number of objects " +
                         "found before setting status on new object does not "+
                         "match number after setting status. They should be equal", 
                          numObjectsBeforeStatusUpdate, 
                          numObjectsAfterStatusUpdate);
        } finally {
            String cleanupQueryString = "SELECT * FROM RegistryPackage WHERE id = "+
                                    "('"+ id +"')";
            RemoveObjectsRequest removeRequest = createRemoveObjectsRequest(cleanupQueryString);
            ServerRequestContext context = new ServerRequestContext("RepositoryTest:testDelete", removeRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            @SuppressWarnings("unused")
			RegistryResponseType response = lcm.removeObjects(context);
        }
    }
    
    private String getTestObjectName(int index) {
        /* Following works for JDK 5.0
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.getDefault());
        formatter.format("%3d", index);
        String name = "IterativeQueryTestObject_" + sb;
        */
        
        //JDK 1.4 compatible
        String name = "IterativeQueryTestObject_";
        if (index < 10) {
            name += "00";
        } else if (index < 100) {
            name += "0";
        } else {
            
        }
        
        name += index;
        
        return name;
    }
    
    private RegistryPackageType createTestPackage(String name) throws Exception {
        RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
        String pkgId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        pkg.setId(pkgId);
        
        //Add name to pkg
        InternationalStringType nameIS = bu.createInternationalStringType(name);
        pkg.setName(nameIS);
        
        return pkg;
    }
    
    private AdhocQueryRequest createAdhocQueryRequest(String sqlString, HashMap<String, String> queryParams, int startIndex, int maxResults) throws Exception {
        AdhocQueryRequest req = BindingUtility.getInstance().createAdhocQueryRequest(sqlString);        
        req.setStartIndex(BigInteger.valueOf(startIndex));
        req.setMaxResults(BigInteger.valueOf(maxResults));
        
        if ((queryParams != null) && (queryParams.size() > 0)) {
            BindingUtility.getInstance().addSlotsToRequest(req, queryParams);
        }
        BindingUtility.getInstance().printObject(req);
        
        return req;
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(QueryManagerImplTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new QueryManagerImplTest("testGetRegistryObjectAccessControl"));
        //suite.addTest(new QueryManagerImplTest("testGetRepositoryItemAccessControl"));
        //suite.addTest(new QueryManagerImplTest("testArbitraryQueryQueryPlugin"));
        //suite.addTest(new QueryManagerImplTest("testStoredParameterizedQuery"));
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    
}

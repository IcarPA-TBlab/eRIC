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
import it.cnr.icar.eric.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.activation.DataHandler;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.User;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;


/**
 * jUnit Test for QueryManager
 *
 * @author Farrukh Najmi
 */
public class QueryManagerTest extends ClientTest {

    public QueryManagerTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(QueryManagerTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new QueryManagerTest("testResponseOption"));
        return suite;
    }

    /**
     * Tests new features in DeclarativeQueryManagerImpl where transient slots that can be used to specify ResponseOption.
     */
    @SuppressWarnings("static-access")
	public void testResponseOption_LeafClass() throws Exception {

        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$id", "urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        queryParams.put("$tableName", LifeCycleManager.EXTRINSIC_OBJECT);

        //First fetch defaultACP without RepositoryItem
        String returnType = ReturnType.LEAF_CLASS.toString();
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);

        Collection<?> registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof ExtrinsicObject));

        ExtrinsicObject eo = (ExtrinsicObject)obj;
        DataHandler repositoryItem = ((ExtrinsicObjectImpl)eo).getRepositoryItemInternal();
        assertTrue("Fetched repositoryItem when should not have done so", (repositoryItem == null));
    }

    @SuppressWarnings("static-access")
	public void testResponseOption_LeafClassWithRI() throws Exception {

        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$id", "urn:oasis:names:tc:ebxml-regrep:acp:defaultACP");
        queryParams.put("$tableName", LifeCycleManager.EXTRINSIC_OBJECT);

        //Now fetch defaultACP with RepositoryItem
        String returnType = ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM.toString();
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_TYPE, returnType);

        Collection<?> registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof ExtrinsicObject));

        ExtrinsicObject eo = (ExtrinsicObject)obj;
        DataHandler repositoryItem = ((ExtrinsicObjectImpl)eo).getRepositoryItemInternal();
        assertTrue("Did not fetch repositoryItem when should have done so", (repositoryItem != null));

    }

    @SuppressWarnings("static-access")
	public void testResponseOption_NoComposedObjects() throws Exception {

        // Use CanonicalXMLCatalogingService to check for slots
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$id", CanonicalConstants.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_ID);
        queryParams.put("$tableName", LifeCycleManager.SERVICE);
        // set the return composed objects to true
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS, "false");

        Collection<?> registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof Service));

        Service s = (Service)obj;
        assertTrue("No composed objects (slots) expected. Maybe object was loaded from server cache.", s.getSlots().size() == 0);
    }

    @SuppressWarnings("static-access")
	public void testResponseOption_ComposedObjects() throws Exception {

        // Use CanonicalXMLCatalogingService to check for slots
        String queryId = CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType;
        Map<String, String> queryParams = new HashMap<String, String>();
        queryParams.put("$id", CanonicalConstants.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_ID);
        queryParams.put("$tableName", LifeCycleManager.SERVICE);
        // set the return composed objects to true
        queryParams.put(dqm.CANONICAL_SLOT_RESPONSEOPTION_RETURN_COMPOSED_OBJECTS, "true");

        Collection<?> registryObjects = executeQuery(queryId, queryParams);
        assertTrue("executeQuery failed", (registryObjects.size() == 1));

        Object obj = registryObjects.toArray()[0];
        assertTrue("Invalid object", (obj instanceof Service));

        Service s = (Service)obj;
        assertTrue("No composed objects (slots)", s.getSlots().size() > 0);
    }

    public void testGetRegistryObjectsByType() throws Exception {

        User user = dqm.getCallersUser();
        BulkResponse br = dqm.getRegistryObjects(LifeCycleManager.USER);
        assertResponseSuccess("dqm.getRegistryObjects failed.", br);

        assertTrue("callers user not in dqm.getRegistryObjects()", br.getCollection().contains(user));
    }

    public void testGetRegistryObjects() throws Exception {

        User user = dqm.getCallersUser();
        BulkResponse br = dqm.getRegistryObjects();
        assertResponseSuccess("dqm.getRegistryObjects failed.", br);

        assertTrue("callers user not in dqm.getRegistryObjects()", br.getCollection().contains(user));
    }

    public void testGetCallersUser() throws Exception {
        User user = (dqm).getCallersUser();
        assertNotNull("Callers user not found.", user);
    }

    /**
     * Tests bug fix where AuthorizationServiceImpl was throwing exception
     * when a non RegistryAdmin user tried to read the default ACP
     */
    public void testReadDefaultACPAsNonRegistryAdmin() throws Exception {
        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, "SELECT * FROM ExtrinsicObject WHERE objectType = '" +
            BindingUtility.CANONICAL_OBJECT_TYPE_ID_XACML + "'");
        BulkResponse br = dqm.executeQuery(query);
        assertTrue("Query matching all XACML Policies failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
    }

}

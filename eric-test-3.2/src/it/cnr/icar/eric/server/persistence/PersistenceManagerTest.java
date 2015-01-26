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

package it.cnr.icar.eric.server.persistence;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.rdb.SQLPersistenceManagerImpl;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;


import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;


/**
 * @author Tony Graham
 */
public class PersistenceManagerTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected UserType registryOperator;
    protected ServerRequestContext context;

    /**
     * Constructor for PersistenceManagerTest
     *
     * @param name name of the test
     */
    @SuppressWarnings("static-access")
	public PersistenceManagerTest(String name) {
        super(name);

        try {
            context = new ServerRequestContext("PersistenceManagerTest:PersistenceManagerTest", null);
            registryOperator = (UserType) qm.getRegistryObject(context, ac.ALIAS_REGISTRY_OPERATOR);
        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            fail("Couldn't initialize objects:\n" + baos);
        }
    }
    
    /**
     * Tests that getRegistryObjectMatchingQuery method 
     * returns first matched object when multiple objects match.
     */
    public void testGetRegistryObjectMatchingQuery() throws Exception {
        final String contextId = "testGetRegistryObjectMatchingQuery";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
                
        RegistryPackageType pkg = (RegistryPackageType)SQLPersistenceManagerImpl.getInstance().getRegistryObjectMatchingQuery(context, "SELECT p.* FROM RegistryPackage p", null, "RegistryPackage");
        assertTrue("Incorrectly returned null", pkg != null);
}        

    /**
     * Tests getOwnersMap() after submission of an CPPA ExtrinsicObject to PersistenceManager and RepositoryManager
     *
     * @throws Exception if an error occurs
     */
    @SuppressWarnings("static-access")
	public void testGetOwnersMap_PMInsertCPPA() throws Exception {
        String eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        ExtrinsicObjectType eo = createExtrinsicObject(eoId,
                "testGetOwnersMap_PMInsertCPPA", bu.CPP_CLASSIFICATION_NODE_ID);
        RepositoryItem ri = createCPPRepositoryItem(eoId);

        context = new ServerRequestContext("PersistenceManagerTest.testGetOwnersMap_PMInsertCPPA", null);
        context.setUser(registryOperator);

        try {
            ArrayList<IdentifiableType> eoList = new ArrayList<IdentifiableType>();
            eoList.add(eo);
            context.getRepositoryItemsMap().put(eoId, ri);
            pm.insert(context, eoList);
            rm.insert(context, ri);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }

        context.commit();

        List<String> ids = new ArrayList<String>();
        ids.add(eoId);

        HashMap<?, ?> ownersMap = pm.getOwnersMap(context, ids);
        String ownerId = (String) ownersMap.get(eoId);

        assertNotNull("ownerId of submitted RegistryObject should not be null.",
            ownerId);
        assertEquals("Owner should be RegistryOperator",
            ac.ALIAS_REGISTRY_OPERATOR, ownerId);
    }

    /**
     * Tests getOwnersMap() after submission of an CPPA ExtrinsicObject to LifeCycleManager
     *
     * @throws Exception if an error occurs
     */
    @SuppressWarnings({ "static-access" })
	public void testGetOwnersMap_LCMSubmitCPPA() throws Exception {
        String eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();

        ExtrinsicObjectType eo = createExtrinsicObject(eoId,
                "testGetOwnersMap_LCMSubmitCPPA", bu.CPP_CLASSIFICATION_NODE_ID);
        RepositoryItem ri = createCPPRepositoryItem(eoId);

        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createExtrinsicObject(eo));

        //Now do the submit 
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);

        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
        idToRepositoryItemMap.put(eoId, ri);

        ServerRequestContext context = new ServerRequestContext("PersistenceManagerTest:testGetOwnersMap_LCMSubmitCPPA", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        BindingUtility.getInstance().checkRegistryResponse(resp);

        context = new ServerRequestContext("PersistenceManagerTest.testGetOwnersMap_LCMSubmitCPPA",
                null);
        context.setUser(registryOperator);

        List<String> ids = new ArrayList<String>();
        ids.add(eoId);

        HashMap<?, ?> ownersMap = pm.getOwnersMap(context, ids);
        String ownerId = (String) ownersMap.get(eoId);

        assertNotNull("ownerId of submitted RegistryObject should not be null.",
            ownerId);
        assertEquals("Owner should be RegistryOperator",
            ac.ALIAS_REGISTRY_OPERATOR, ownerId);
    }

    /**
     * Creates an ExtrinsicObject of specific type.
     *
     * @return an <code>ExtrinsicObjectType</code> value
     * @param id id of created ExtrinsicObject
     * @param desc description to add to generated ExtrinsicObject
     * @param objectType id of classification type of created ExtrinsicObject
     * @exception Exception if an error occurs
     */
    ExtrinsicObjectType createExtrinsicObject(String id, String desc,
        String objectType) throws Exception {
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();

        if (desc != null) {
            eo.setDescription(bu.createInternationalStringType(desc));
        }

        eo.setId(id);
        eo.setObjectType(objectType);
        eo.setContentVersionInfo(bu.rimFac.createVersionInfoType());

        return eo;
    }

    /**
     *
     * @return
     */
    public static Test suite() {
        return new TestSuite(PersistenceManagerTest.class);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

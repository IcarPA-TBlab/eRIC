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

package it.cnr.icar.eric.server.cms;

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.net.URL;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * JUnit TestCase for CMSManagerImpl.  Uses the Canonical XML Content
 * Cataloging Service as the service that the CMSManager invokes.
 */
public class CMSManagerImplTest extends ServerTest {
    protected static final String VALIDATION_TEST_ALWAYS_SUCCEED_CLASSIFICATION_NODE_ID =
        "urn:uuid:b0b80d53-331e-4c96-b4ba-a24236462337";
    protected static final String VALIDATION_TEST_THROW_INVALID_CONTENT_EXCEPTION_CLASSIFICATION_NODE_ID =
        "urn:uuid:e2953fd7-5537-4298-aa22-be8cae70dbcc";
    protected static final String VALIDATION_TEST_CPPA_VALIDATION_CLASSIFICATION_NODE_ID =
        "urn:uuid:7e2a7ba4-61c4-4f9d-9262-c2a7a7860d69";
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected static CMSTestUtility cmsTestUtility = CMSTestUtility.getInstance();
    protected ServerRequestContext context;
    protected CMSManagerImpl manager = new CMSManagerImpl();
    protected URL riURL = getClass().getResource("/resources/CPP1.xml");
    protected UserType registryOperator;

    /**
     * Constructor for CMSManagerImplTest
     *
     * @param name
     */
    public CMSManagerImplTest(String name) {
        super(name);

        try {
            ServerRequestContext context = new ServerRequestContext("CMSManagerImplTest:CMSManagerImplTest", null);
            registryOperator = (UserType) qm.getRegistryObject(context, AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);
        } catch (Exception e) {
            fail("Could not get RegistryObject for 'RegistryOperator'.");
        }
    }

    /**
     * Creates a new <code>RequestContext</code> for each test.
     */
    protected void setUp() {
        try {
            context = new ServerRequestContext("CMSManagerImplTest.setUp", null);
            context.setUser(registryOperator);
        } catch (Exception e) {
            fail("Couldn't initialise RequestContext.");
        }
    }
    
    /**
     * Invokes the manager on an empty context.  Should succeed.
     *
     * @exception Exception if an error occurs
     */
    public void testInvokeServices_EmptyContext() throws Exception {
        try {
            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    /**
     * Invokes the manager on a context with one ExtrinsicObject
     * without a corresponding RepositoryItem.  The absence of the
     * RepositoryItem is an error for the Canonical XML Content
     * Cataloging Service.
     *
     * @exception Exception if an error occurs
     */
    public void testInvokeServices_EO_NoRI() throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_EO_NoRI()";

            // Add an ExtrinsicObject to context.
            @SuppressWarnings("static-access")
			ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelRegistryObjectTypeMap().put(eo.getId(), eo);

            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    public void testInvokeServices_EO_RI() throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_EO_RI()";

            // Add an ExtrinsicObject + RepositoryItem pair to context.
            @SuppressWarnings("static-access")
			ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelRegistryObjectTypeMap().put(eo.getId(), eo);
            context.getRepositoryItemsMap().put(eo.getId(),
                cmsTestUtility.createCPPRepositoryItem(eo.getId()));

            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    /**
     * Invokes the manager on a context containing multiple
     * ExtrinsicObjects each with a RepositoryItem.  A CMS system that
     * can handle one ExtrinsicObject+RepositoryItem pair won't
     * necessarily be able to handle more than one in a context.
     *
     * @exception Exception if an error occurs
     */
    @SuppressWarnings("static-access")
	public void testInvokeServices_2EO_2RI() throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_2EO_2RI()";

            ArrayList<IdentifiableType> eoList = new ArrayList<IdentifiableType>();

            // Add an ExtrinsicObject + RepositoryItem pair to context.
            ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelRegistryObjectTypeMap().put(eo.getId(), eo);
            eoList.add(eo);

            RepositoryItem ri1 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri1);
            eo.setMimeType(ri1.getDataHandler().getContentType());

            // Add another ExtrinsicObject + RepositoryItem pair to context.
            eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            eoList.add(eo);
            context.getTopLevelRegistryObjectTypeMap().put(eo.getId(), eo);

            RepositoryItem ri2 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri2);
            eo.setMimeType(ri2.getDataHandler().getContentType());

            pm.insert(context, eoList);
            rm.insert(context, ri1);
            rm.insert(context, ri2);
            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    /**
     * Invokes the manager on a context containing multiple
     * ExtrinsicObjects each with a RepositoryItem.  A CMS system that
     * can handle one ExtrinsicObject+RepositoryItem pair won't
     * necessarily be able to handle more than one in a context.
     *
     * @exception Exception if an error occurs
     */
    @SuppressWarnings("static-access")
	public void testInvokeServices_validationTestThrowInvalidContentException()
        throws Exception {
        try {
            String desc = "CMSManagerImplTest.testInvokeServices_validationTestThrowInvalidContentException()";

            ArrayList<IdentifiableType> eoList = new ArrayList<IdentifiableType>();

            // Add an ExtrinsicObject + RepositoryItem pair to context.
            ExtrinsicObjectType eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            context.getTopLevelRegistryObjectTypeMap().put(eo.getId(), eo);
            eoList.add(eo);

            RepositoryItem ri1 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri1);
            eo.setMimeType(ri1.getDataHandler().getContentType());

            // Add another ExtrinsicObject + RepositoryItem pair to context.
            eo = cmsTestUtility.createExtrinsicObject(desc,
                    bu.CPP_CLASSIFICATION_NODE_ID);
            eoList.add(eo);
            context.getTopLevelRegistryObjectTypeMap().put(eo.getId(), eo);

            RepositoryItem ri2 = cmsTestUtility.createCPPRepositoryItem(eo.getId());
            context.getRepositoryItemsMap().put(eo.getId(), ri2);
            eo.setMimeType(ri2.getDataHandler().getContentType());

            pm.insert(context, eoList);
            rm.insert(context, ri1);
            rm.insert(context, ri2);
            manager.invokeServices(context);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    public static Test suite() {
        return new TestSuite(CMSManagerImplTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

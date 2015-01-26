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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * JUnit TestCase for ContentCatalogingServiceImpl.  Uses the test Cataloging
 * service loaded with DemoDB as the service that the CMSManager invokes.
 */
public class ContentCatalogingServiceImplTest extends ServerTest {
    protected static final String CPPA_CATALOGING_SERVICE_ID = "urn:freebxml:registry:demoDB:test:cms:ContentCatalogingService:cppaCataloging";

    protected static final String CPPA_CATALOGING_OBJECT_TYPE = "urn:uuid:eb11b777-eb16-455e-8837-8c98aae3c0db";
    protected static final String CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_ASSOC_ID =
        "urn:uuid:92418a05-eb1b-4bb5-8579-dc9accb1469a";
    protected static final String CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_EO_ID =
        "urn:uuid:50ea1df2-5bb8-44c0-8d70-a1e18d84001e";
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected ContentManagementService catalogingServiceImpl = new ContentCatalogingServiceImpl();
    protected static CMSTestUtility cmsTestUtility = CMSTestUtility.getInstance();
    protected UserType registryOperator;
    protected ServiceType cppaCatalogingService;
    protected InvocationController cppaCatalogingIC;
    protected ServerRequestContext context;

    /**
     * Constructor for ContentCatalogingServiceImplTest
     *
     * @param name
     */
    public ContentCatalogingServiceImplTest(String name) {
        super(name);

        try {
            context = new ServerRequestContext("ContentCatalogingServiceImplTest:ContentCatalogingServiceImplTest", null);
            registryOperator = (UserType) qm.getRegistryObject(context, AuthenticationServiceImpl.ALIAS_REGISTRY_OPERATOR);

            cppaCatalogingService = (ServiceType) qm.getRegistryObject(context, CPPA_CATALOGING_SERVICE_ID);
            cppaCatalogingIC = new InvocationController(CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_ASSOC_ID,
                    CATALOGING_CONTROL_FILE_FOR_CPPA_CATALOGING_EO_ID);

        } catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            fail("Couldn't initialize objects:\n" + baos);
        }
    }

    public void testInvoke_cppaCataloging() throws Exception {
        @SuppressWarnings("static-access")
		RegistryObjectType cppaCatalogingValidEO = cmsTestUtility.createExtrinsicObject("CPPACataloging EO",
                CPPA_CATALOGING_OBJECT_TYPE);
        RepositoryItem cppaCatalogingValidRI = cmsTestUtility.createCPPRepositoryItem(cppaCatalogingValidEO.getId());
        
        try {
            context = new ServerRequestContext("ContentCatalogingServiceImplTest.testInvoke_cppaCataloging", null);
            context.setUser(registryOperator);

            ArrayList<IdentifiableType> eoList = new ArrayList<IdentifiableType>();
            eoList.add(cppaCatalogingValidEO);
            context.getRepositoryItemsMap().put(cppaCatalogingValidEO.getId(),
                cppaCatalogingValidRI);
            pm.insert(context, eoList);
            rm.insert(context, cppaCatalogingValidRI);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }

        cppaCatalogingValidEO = qm.getRegistryObject(context, cppaCatalogingValidEO.getId());
        context.commit();
        
        assertNotNull("ExtrinsicObject from registry should not be null", cppaCatalogingValidEO);

        ServiceOutput output = catalogingServiceImpl.invoke(context, new ServiceInput(
                    cppaCatalogingValidEO, cppaCatalogingValidRI),
                    cppaCatalogingService, cppaCatalogingIC, registryOperator);

        assertTrue("ServiceOutput value should be a RequestContext.",
            output.getOutput() instanceof ServerRequestContext);
    }

    public static Test suite() {
        return new TestSuite(ContentCatalogingServiceImplTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

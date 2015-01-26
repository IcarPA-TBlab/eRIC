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

package it.cnr.icar.eric.server.lcm;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.query.federation.FederatedQueryManager;
import junit.framework.Test;


import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;

/**
 * @author Farrukh Najmi
 */
public class FederationSystemTest extends ServerTest {
    
    private static FederationType federation = null;
    @SuppressWarnings("unused")
	private static RegistryType registry1 = null;
    @SuppressWarnings("unused")
	private static RegistryType registry2 = null;
    
    
    /** Creates a new instance of FederationTest */
    public FederationSystemTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        // These tests need to be ordered for purposes of read/write/delete.
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        // Test creating and retrieving 2 internal schemes and 1 external scheme
        suite.addTest(new FederationSystemTest("testGetRegistries"));
        suite.addTest(new FederationSystemTest("testCreateFederation"));
        suite.addTest(new FederationSystemTest("testJoinFederation"));
        suite.addTest(new FederationSystemTest("testFederatedQuery"));
        suite.addTest(new FederationSystemTest("testLeaveFederation"));
        suite.addTest(new FederationSystemTest("testDissolveFederation"));
                        
        return suite;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    /**
     * Tests getting of Registry instance for the two test registries.
     */
    public void testGetRegistries() throws Exception {
        
    }
        
    /**
     * Tests creating a new Federation.
     */
    public void testCreateFederation() throws Exception {
        federation = bu.rimFac.createFederationType();
        String fedId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        federation.setId(fedId);
        
        //Add name to federation
        InternationalStringType nameIS = bu.createInternationalStringType("FederationSystemTest-TestFederation");
        federation.setName(nameIS);
    }
        
    /**
     * Tests the joining of registries with a Federation.
     */
    public void testJoinFederation() throws Exception {
    }
        
    /**
     * Tests executing a federated query across 2 registries in a Federation.
     */
    public void testFederatedQuery() throws Exception {
        FederatedQueryManager fqm = FederatedQueryManager.getInstance();
        AdhocQueryRequest req = bu.createAdhocQueryRequest("SELECT * FROM RegistryPackage WHERE lid LIKE '" + 
            BindingUtility.FEDERATION_TEST_DATA_LID_PREFIX + "%'");
        ServerRequestContext context = new ServerRequestContext("FederationSystemTest:testFederatedQuery", req);
        context.setUser(ac.registryOperator);
        @SuppressWarnings("unused")
		AdhocQueryResponse resp = fqm.submitAdhocQuery(context);
    }
    
    
    /**
     * Tests the leaving of registries from a Federation.
     */
    public void testLeaveFederation() throws Exception {
    }
    
    /**
     * Tests the dissolving of a Federation.
     */
    public void testDissolveFederation() throws Exception {
    }
    
}

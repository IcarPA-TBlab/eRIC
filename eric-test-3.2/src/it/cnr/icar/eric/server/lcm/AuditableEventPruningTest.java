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

import it.cnr.icar.eric.common.exceptions.UnauthorizedRequestException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.JAXRException;

import junit.framework.Test;

import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * @author Farrukh Najmi
 */
public class AuditableEventPruningTest extends ServerTest {
            
    static RegistryPackageType pkg1 = null;
    static String pkg1Id = "urn:freebxml:eric:server:lcm:AudtibleEventPruningTest:pkg1";
    static String getAuditableEventsQueryStr = "SELECT ae.* FROM AuditableEvent ae, AffectedObject ao WHERE ae.id = ao.eventId and ao.id = '" + pkg1Id + "' ORDER BY ae.timestamp_ DESC" ; 
    static String getPkg1QueryStr = "SELECT pkg.* FROM RegistryPackage pkg WHERE pkg.id = '" + pkg1Id + "'" ; 
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public AuditableEventPruningTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        // These tests need to be ordered
        // Do not change order of tests, erroneous errors will result.
        junit.framework.TestSuite suite= new junit.framework.TestSuite();
        
        suite.addTest(new AuditableEventPruningTest("testSetup"));
        suite.addTest(new AuditableEventPruningTest("testCreateAuditTrail"));
        suite.addTest(new AuditableEventPruningTest("testPruneEventsAsOwner"));
        suite.addTest(new AuditableEventPruningTest("testPruneEventsAsRegistryAdmin"));
        suite.addTest(new AuditableEventPruningTest("testPruneEventsAsRegistryAdmin"));
	// Clean up because object may be left in an invalid state after
	// successfully pruning associated events
        suite.addTest(new AuditableEventPruningTest("testSetup"));
        
        return suite;
    }
    
    /**
     * Remove test objects from previous test iterations before starting test.
     */
    public void testSetup() throws Exception {
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        @SuppressWarnings("unused")
		RegistryResponseType resp = null;
        
        //Remove pkg1
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getPkg1QueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testSetup", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        try {
            resp = lcm.removeObjects(context);
            //bu.checkRegistryResponse(resp);
        } catch (JAXRException e) {
            //Ignore
        }
        
        req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        context = new ServerRequestContext("AuditableEventPruningTest:testSetup", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);        
        
        try {
            resp = lcm.removeObjects(context);
            //bu.checkRegistryResponse(resp);
        } catch (JAXRException e) {
            //Ignore
        }
        
    }
    
    /**
     * Creates an audit trail by publishing and editing an object.
     */ 
    public void testCreateAuditTrail() throws Exception {
        //Create the Created event
        pkg1 = bu.rimFac.createRegistryPackageType();
        pkg1.setId(pkg1Id);
        
        ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage(pkg1));
        
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
                
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testCreateAuditTrail", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Now create the Updated event by just changing the name
        pkg1.setName(bu.getName(pkg1Id));
        
        objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
        objects.add(bu.rimFac.createRegistryPackage(pkg1));
        
        //Now do the submit 
        submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        roList = bu.rimFac.createRegistryObjectListType();

        roList.getIdentifiable().addAll(objects);
        submitRequest.setRegistryObjectList(roList);
        idToRepositoryItemMap = new HashMap<String, Object>();
                
        context = new ServerRequestContext("AuditableEventPruningTest:testCreateAuditTrail", submitRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Fetch AuditableEvents for pkg1 and assert that there is at least 2 (Created and Updated)
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        context = new ServerRequestContext("AuditableEventPruningTest:testCreateAuditTrail", req);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        AdhocQueryResponse queryResp = qm.submitAdhocQuery(context);
        List<JAXBElement<? extends IdentifiableType>> res = queryResp.getRegistryObjectList().getIdentifiable();
        assertTrue("Must have at least 2 Auditable events. It is normal for this test to fail when versioning is turned on.", (res.size() >= 2));
    }    
    
    /**
     * Attempt to prune AuditableEvents for object using identity of objects owner.
     * It is expected that the registry will not allow this to happen as only 
     * RegistryAdministrator roles are allowed to prune AuditableEvents
     */ 
    @SuppressWarnings("static-access")
	public void testPruneEventsAsOwner() throws Exception {
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        
        //Now remove the AuditableEvents for pkg1         
        //Set the AdhocQuery param
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testPruneEventsAsOwner", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            @SuppressWarnings("unused")
			RegistryResponseType resp = lcm.removeObjects(context);
            this.fail("Owner should not be allowed to delete AuditableEvents.");
        } catch (UnauthorizedRequestException e) {
            //All is well. This was expected.
        }
    }    
    
    /**
     * Attempt to prune AuditableEvents for object using identity RegistryOperator.
     * It is expected that the registry will allow this to happen as RegistryOperator user has 
     * RegistryAdministrator role.
     */ 
    public void testPruneEventsAsRegistryAdmin() throws Exception {
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        ServerRequestContext context = new ServerRequestContext("AuditableEventPruningTest:testPruneEventsAsRegistryAdmin", removeRequest);
        context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        //Now remove the AuditableEvents for pkg1         
        //Set the AdhocQuery param
        AdhocQueryRequest req = bu.createAdhocQueryRequest(getAuditableEventsQueryStr);
        removeRequest.setAdhocQuery(req.getAdhocQuery());
        
        RegistryResponseType resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
    }    
    
    /**
     * Attempt to prune Created or Relocated AuditableEvents for object using identity RegistryOperator.
     * It is expected that the registry will not allow this to happen if there are objects effected
     * by this event as this would make it no longer possible to determine owner of object.
     */ 
    public void testPruneProvenanceEventsAsRegistryAdmin() throws Exception {
        //TBD. This functionality is being thought thru from a spec perspective
    }    
    
}

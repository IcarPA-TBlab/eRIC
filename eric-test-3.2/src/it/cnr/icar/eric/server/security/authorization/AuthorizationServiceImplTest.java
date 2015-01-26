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

package it.cnr.icar.eric.server.security.authorization;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.AuthorizationException;
import it.cnr.icar.eric.common.exceptions.UnauthorizedRequestException;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.rdb.SQLPersistenceManagerImpl;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.JAXBElement;

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * Tests the AuthorizationServiceImpl class in server code.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 *
 */
public class AuthorizationServiceImplTest extends ServerTest {
    
    static String assId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:assId";
    static String eoId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:eoId";
    
    public AuthorizationServiceImplTest(java.lang.String testName) {
        super(testName);
        
    }
        
    public static Test suite() {
        //Warning: this test requires its methods to be executed in sequence.
        junit.framework.TestSuite suite = new TestSuite(AuthorizationServiceImplTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new AuthorizationServiceImplTest("testReferenceByNonOwner"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonOwnerACPNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testOwnerACPAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testMultipleACPNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testAddMemberAction"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonAdminDefaultDefineFederationNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonAdminDefaultDefineRegistryNotAllowed"));
        //suite.addTest(new AuthorizationServiceImplTest("testNonAdminReferenceRegistryAllowed"));
        return suite;
    }
        
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    /*
     * Tests that defaultACP allows reference from an object A to object B
     * when object A is owned by User A and object B is owner by User B and
     * submitter of reference action is User A.
     *
     * Test for: 6430938 Unable to associate Service and Service Binding
     */
    public void testReferenceByNonOwner() throws Exception {
        
        final String contextId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:testReferenceByNonOwner";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        
        final String pkgId = contextId + ":pkg1";
        final String assId = contextId + ":ass1";
        
        try {            
            // initial clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);

            // Create package
            RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
            pkg.setId(pkgId);
            submit(context, pkg);

            //Now create and association between pkg and a service binding owned by someone else
            //This should work
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            ass.setId(assId);
            ass.setSourceObject(pkgId);
            ass.setTargetObject("urn:freebxml:registry:demoDB:ebXMLRegistryServiceBinding");
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            submit(context, ass);                        
        } finally {
            // final clean-up
            removeIfExist(context, pkgId);
            removeIfExist(context, assId);            
        }                
        
    }
    
    /*
     * Test that server rejects the action when defaultACP
     * is submitted by a non-admin.
     */
    public void testNonAdminDefaultACPNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefaultACPNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        @SuppressWarnings("unused")
		final String defaultACP = contextId + ":pkg1";
        
        // Create package
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        eo.setId(az.idForDefaultACP);
        
        try {
            submit(context, eo);
            fail("Non admin user should not have been able to submit defaultACP");
        } catch (UnauthorizedRequestException e) {
        	return;
            //Expected.
        }

    }    
    
    /**
     * Test that server rejects the action when Federation
     * is submitted by a non-admin.
     */
    public void testNonAdminDefineFederationNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefineFederationNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String federationId = contextId + ":federation";
                
        try {
            removeIfExist(context, federationId);
            
            // Create Federation
            FederationType federation = bu.rimFac.createFederationType();
            federation.setId(federationId);
            
            try {
                submit(context, federation);
                fail("Non admin user should not have been able to submit Federation");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
            removeIfExist(context, federationId);
        }                
    }    
    
    /**
     * Test that server rejects the action when Registry
     * is submitted by a non-admin.
     */
    public void testNonAdminDefineRegistryNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefineRegistryNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String registryId = contextId + ":registry";
                
        try {
            removeIfExist(context, registryId);
            
            // Create Federation
            RegistryType registry = bu.rimFac.createRegistryType();
            registry.setId(registryId);
            
            try {
                submit(context, registry);
                fail("Non admin user should not have been able to submit Federation");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
            removeIfExist(context, registryId);
        }                
    }    
    
    
    /**
     * Test that server accepts the action when Registry or Federation
     * is referenced by a non-admin as long its not defining a HasFederationMember association.
     *
     */
    @SuppressWarnings("static-access")
	public void testNonAdminReferenceRegistryAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:testNonAdminReferenceRegistryAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String assId = contextId + ":notHasFedarationAss";
                
        try {
            removeIfExist(context, assId);
            
            // Create Association of some type other than HasFederationMember between Federation and Registry
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            ass.setId(assId);
            ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);

            RegistryType registry = ServerCache.getInstance().getRegistry(context);            
            FederationType federation = (FederationType)SQLPersistenceManagerImpl.getInstance().getRegistryObjectMatchingQuery(context, "SELECT f.* FROM Federation f", null, "Federation");
            
            //The following should point to Federation and Registry instance but in this
            //context it matters not. Any Federation with this associationType is not 
            //allowed unless published by a RegistryAdmin.
            ass.setSourceObject(federation.getId());
            ass.setTargetObject(registry.getId());
            
            submit(context, ass);
        } finally {
            // final clean-up
            removeIfExist(context, assId);
        }                
    }    
    
    /**
     * Test that server rejects the action when an Association of AssociationType HasFederationMember
     * is submitted by a non-admin.
     */
    @SuppressWarnings("static-access")
	public void testNonAdminHasFederationMemberAssociationNotAllowed() throws Exception {
        final String contextId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:testNonAdminDefaultDefineRegistryNotAllowed";
        ServerRequestContext context = new ServerRequestContext(contextId, null);
        context.setUser(AuthenticationServiceImpl.getInstance().nikola);
        final String assId = contextId + ":hasFedarationAss";
                
        try {
            removeIfExist(context, assId);
            
            // Create Federation
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            ass.setId(assId);
            ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_HasFederationMember);

            //The following should point to Federation and Registry instance but in this
            //context it matters not. Any Federation with this associationType is not 
            //allowed unless published by a RegistryAdmin.
            ass.setSourceObject(bu.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor);
            ass.setTargetObject(bu.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            
            try {
                submit(context, ass);
                fail("Non admin user should not have been able to submit HasFederationAssociation.");
            } catch (UnauthorizedRequestException e) {
                //Expected.
            }

        } finally {
            // final clean-up
            removeIfExist(context, assId);
        }                
    }    
    
    /*
     * Test that server rejects the action when an AccessControlPolicyFor Association
     * is submitted by a non-owner of the targetObject.
     */
    @SuppressWarnings("static-access")
	public void testNonOwnerACPNotAllowed() throws Exception {
        ArrayList<RegistryObjectType> ros = new ArrayList<RegistryObjectType>();
        
        eoId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        ExtrinsicObjectType eo = bu.rimFac.createExtrinsicObjectType();
        eo.setId(eoId);
        ros.add(eo);
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, ros);
        ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testNonOwnerACPNotAllowed", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);                
        
        ros.clear();
        assId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        AssociationType1 ass = bu.rimFac.createAssociationType1();
        ass.setId(assId);
        ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor);
        ass.setTargetObject(eoId);
        ass.setSourceObject(az.idForDefaultACP);
        ros.add(ass);
        
        submitRequest = bu.createSubmitRequest(false, false, ros);
        context = new ServerRequestContext("AuthenticationServiceImpl:testNonOwnerACPNotAllowed", submitRequest);
        context.setUser(ac.nikola);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        try {
            resp = lcm.submitObjects(context);        
            fail("Server did not throw RegistryException when AccessControlPolicyFor Association submitted by non-owner.");
        } catch (UnauthorizedRequestException e) {
            //Expected
        }
     }    
    
    /*
     * Test that server accepts the action when an AccessControlPolicyFor Association
     * is submitted by the owner of the targetObject when none existed before.
     */
    @SuppressWarnings("static-access")
	public void testOwnerACPAllowed() throws Exception {
        ArrayList<RegistryObjectType> ros = new ArrayList<RegistryObjectType>();
                
        AssociationType1 ass = bu.rimFac.createAssociationType1();
        ass.setId(assId);
        ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor);
        ass.setTargetObject(eoId);
        ass.setSourceObject(az.idForDefaultACP);
        ros.add(ass);
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, ros);
        ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testOwnerACPAllowed", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        RegistryResponseType resp = lcm.submitObjects(context);
        bu.checkRegistryResponse(resp);                
     }    
    
    /*
     * Test that server throws Exception when an AccessControlPolicyFor Association
     * is submitted when an one exists already.
     */
    public void testMultipleACPNotAllowed() throws Exception {
        ArrayList<RegistryObjectType> ros = new ArrayList<RegistryObjectType>();
        
        ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testMultipleACPNotAllowed", null);
        context.setUser(ac.registryGuest);
        AssociationType1 ass = (AssociationType1)qm.getRegistryObject(context, assId);
        assertNotNull("AccessControlPolicyFor Association not found", assId);
        
        //Save a a new association
        String newAssId = it.cnr.icar.eric.common.Utility.getInstance().createId();
        ass.setId(newAssId);
        ros.add(ass);
        
        SubmitObjectsRequest submitRequest = bu.createSubmitRequest(false, false, ros);
        context = new ServerRequestContext("AuthenticationServiceImpl:testMultipleACPNotAllowed", submitRequest);
        context.setUser(ac.farrukh);
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        try {
            @SuppressWarnings("unused")
			RegistryResponseType resp = lcm.submitObjects(context);
            fail("Server did not throw RegistryException on duplicate ACP submission.");
        } catch (AuthorizationException e) {
            //Expected
        }
    }
    
            
    @SuppressWarnings({ "static-access" })
	public void testAddMemberAction() throws Exception {
        //Make sure customACP are enabled for this test
        String customAccessControlPoliciesEnabled = RegistryProperties.getInstance().getProperty("eric.security.authorization.customAccessControlPoliciesEnabled", "false");
        RegistryProperties.getInstance().put("eric.security.authorization.customAccessControlPoliciesEnabled", "true");
        
        try {
            //The following id must be kept synced with misc/samples/demoDB/SUbmitObjectsRequst_Role.xml
            String parentFolderId = "urn:freebxml:registry:demoDB:folder1";
            ObjectRefType parentFolderRef = bu.rimFac.createObjectRefType();
            parentFolderRef.setId(parentFolderId);

            RegistryPackageType childFolder = bu.rimFac.createRegistryPackageType();
            String childFolderId = it.cnr.icar.eric.common.Utility.getInstance().createId();
            childFolder.setId(childFolderId);

            //Attempt to add childFolder to parentFolder as Developer and expect to fail
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            String assId = it.cnr.icar.eric.common.Utility.getInstance().createId();
            ass.setId(assId);
            ass.setSourceObject(parentFolderId);
            ass.setTargetObject(childFolderId);
            ass.setAssociationType(bu.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);

            ArrayList<JAXBElement<? extends IdentifiableType>> objects = new ArrayList<JAXBElement<? extends IdentifiableType>>();
            objects.add(bu.rimFac.createAssociation(ass));
            objects.add(bu.rimFac.createRegistryPackage(childFolder));
            objects.add(bu.rimFac.createObjectRef(parentFolderRef));

            //Now do the submit
            SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();

            roList.getIdentifiable().addAll(objects);
            submitRequest.setRegistryObjectList(roList);
            HashMap<String, Object> idToRepositoryItemMap = new HashMap<String, Object>();
            ServerRequestContext context = new ServerRequestContext("AuthenticationServiceImpl:testAddMemberAction", submitRequest);
            context.setUser(ac.nikola);
            context.setRepositoryItemsMap(idToRepositoryItemMap);

            RegistryResponseType resp = null;
            try {
                //Assert that a developer role cannot add members to folder
                resp = lcm.submitObjects(context);
                assertEquals("Developer should not have been able to add member.", BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure, resp.getStatus());
            } catch (UnauthorizedRequestException e) {
                //Expected once we fix spec to throw this Exception instead of IllegalStateException
            } catch (IllegalStateException e) {
                //Expected until we fix spec to throw UnauthorizedRequestException instead of IllegalStateException
            }

            //Assert that a ProjectLead role can add members to folder
            context = new ServerRequestContext("AuthenticationServiceImpl:testAddMemberAction", submitRequest);
            context.setUser(ac.farrukh);
            context.setRepositoryItemsMap(idToRepositoryItemMap);
            resp = lcm.submitObjects(context);
            String respStr = bu.marshalObject(bu.rsFac.createRegistryResponse(resp));
            assertEquals(respStr, BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success, resp.getStatus());

            //Now do the remove of what was submitted
            ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
            ObjectRefType childFolderRef = bu.rimFac.createObjectRefType();
            childFolderRef.setId(childFolderId);
            objectRefs.add(childFolderRef);

            ObjectRefType assRef = bu.rimFac.createObjectRefType();
            assRef.setId(assId);
            objectRefs.add(assRef);

            RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
            org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
            orList.getObjectRef().addAll(objectRefs);
            removeRequest.setObjectRefList(orList);

            context = new ServerRequestContext("AuthenticationServiceImpl:testAddMemberAction", removeRequest);
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            resp = lcm.removeObjects(context);
            bu.checkRegistryResponse(resp);
        } finally {
            RegistryProperties.getInstance().put("eric.security.authorization.customAccessControlPoliciesEnabled", customAccessControlPoliciesEnabled);
        }
    }
    
    /**
     * Tests that LCM does not throw IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor owns the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectSameUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObjectSameUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextFarrukh, "testNoNewRefsToDeprecatedObjectSameUser");
        } catch (IllegalStateException e) {
            fail("Threw IllegalStateException for new reference to a Deprecated object even when object is my own.");
        }
    }
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object when requestor does not own the refreence target.
     *
     */
    public void testNoNewRefsToDeprecatedObjectDifferentUser() throws Exception {
        ServerRequestContext contextFarrukh = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:farrukh", null);
        contextFarrukh.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        ServerRequestContext contextNikola = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObjectDifferentUser:nikola", null);
        contextNikola.setUser(AuthenticationServiceImpl.getInstance().nikola);
        
        try {
            internalTestNoNewRefsToDeprecatedObject(contextFarrukh, contextNikola, "testNoNewRefsToDeprecatedObjectDifferentUser");
            fail("Did not throw IllegalStateException for new reference to a Deprecated object.");
        } catch (IllegalStateException e) {
            //expected. All is well
        }
    }
    
    /**
     * Tests that LCM throws IllegalStateException if an object is submitted with a 
     * reference to a deprecated object.
     *
     */
    private void internalTestNoNewRefsToDeprecatedObject(ServerRequestContext context1, ServerRequestContext context2, String contextId) throws Exception {
        final String pkgId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:" + contextId + ":pkg1";
        final String assId = "urn:org:freebxml:eric:server:security:authorization:AuthorizationServiceImplTest:" + contextId + ":ass1";

        ServerRequestContext contextRegAdmin = new ServerRequestContext("AuthorizationServiceImplTest:testNoNewRefsToDeprecatedObject:registryOperator", null);
        contextRegAdmin.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
        
        try {            
            // initial clean-up
            removeIfExist(contextRegAdmin, pkgId);
            removeIfExist(contextRegAdmin, assId);

            // Create package
            RegistryPackageType pkg = bu.rimFac.createRegistryPackageType();
            pkg.setId(pkgId);
            submit(context1, pkg);

            //Now create and association between ass and pkg
            //This should work as object being referenced is not deprecated
            AssociationType1 ass = bu.rimFac.createAssociationType1();
            ass.setId(assId);
            ass.setSourceObject(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved); //id of some random existing object
            ass.setTargetObject(pkgId);
            ass.setAssociationType(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith);
            submit(context2, ass);
            
            //Now deprecate the pkg
            setStatus(context1, pkgId, BindingUtility.CANONICAL_STATUS_TYPE_ID_Deprecated);
            
            //Now submit ass again
            //This should fail as object being referenced is deprecated
            //as long as user is not superuser or owner of reference target
            submit(context2, ass);
        } finally {
            // final clean-up
            removeIfExist(contextRegAdmin, pkgId);
            removeIfExist(contextRegAdmin, assId);            
        }                
    }    
    
}

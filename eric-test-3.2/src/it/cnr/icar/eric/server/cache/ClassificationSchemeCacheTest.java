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

package it.cnr.icar.eric.server.cache;

import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBElement;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 * @author Farrukh Najmi
 */
public class ClassificationSchemeCacheTest extends ServerTest {

    String node1Id = "urn:org:freebxml:eric:server:cache:ClassificationSchemeCacheTest:node1";

    
    public ClassificationSchemeCacheTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        return new TestSuite(ClassificationSchemeCacheTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testGetAllSchemes() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ClassificationSchemeCacheTest:testGetAllSchemes", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        List<?> schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
    }
    
    /*
     * Publishes a single node to AssociationType scheme and then callss getAllClassificationSchemes
     * and verifies all schemes are there and that new node is present.
     */
    @SuppressWarnings({ "static-access" })
	public void testUpdateCacheWhenNodeInserted() throws Exception {
        ServerRequestContext context = new ServerRequestContext("ClassificationSchemeCacheTest:testUpdateCacheWhenNodeInserted", null);
        context.setUser(ac.farrukh);
        List<?> schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
        
        int oldSchemeSize = schemes.size();
        
        ClassificationNodeType node = bu.rimFac.createClassificationNodeType();
        node.setId(node1Id);
        node.setCode("testUpdateCacheWhenNodeInserted");
        node.setParent(bu.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType);
        
        //ArrayList<ClassificationNodeType> objects = new ArrayList<ClassificationNodeType>();
        //objects.add(node);
        
        SubmitObjectsRequest submitRequest = bu.lcmFac.createSubmitObjectsRequest();
        RegistryObjectListType roList = bu.rimFac.createRegistryObjectListType();
        
        bu.addSlotsToRequest(submitRequest, dontVersionSlotsMap);
        
        //roList.getIdentifiable().add(objects);
        
		JAXBElement<ClassificationNodeType> ebNode = bu.rimFac.createClassificationNode(node);
		//ArrayList<JAXBElement<ClassificationNodeType>> objects = new ArrayList<JAXBElement<ClassificationNodeType>>();
		//objects.add(ebNode);
		
        roList.getIdentifiable().add(ebNode);
        
        submitRequest.setRegistryObjectList(roList);
        HashMap<String, Object> idToRepositoryItemMap = new HashMap<String,Object>();
                
        //Now do the submit 
        context.setRepositoryItemsMap(idToRepositoryItemMap);
        
        try {
            context.pushRegistryRequest(submitRequest);
            RegistryResponseType resp = lcm.submitObjects(context);
            bu.checkRegistryResponse(resp);
        } finally {
            context.popRegistryRequest();
        }
        
        schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
        
        assertEquals("Cache update made cache lose some schemes", oldSchemeSize, schemes.size());
        assertTrue("Unable to readback node after inserting it to scheme.", schemesContainsNode(schemes, bu.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType, node1Id));
        
    }
    
    /*
     * Removes a single node to AssociationType scheme and then calls getAllClassificationSchemes
     * and verifies all schemes are there and that removed node is not present.
     */
    public void testUpdateCacheWhenNodeRemoved() throws Exception {
        ArrayList<ObjectRefType> objectRefs = new ArrayList<ObjectRefType>();
        ObjectRefType nodeRef = bu.rimFac.createObjectRefType();
        nodeRef.setId(node1Id);        
        objectRefs.add(nodeRef);
        
        RemoveObjectsRequest removeRequest = bu.lcmFac.createRemoveObjectsRequest();
        org.oasis.ebxml.registry.bindings.rim.ObjectRefListType orList = bu.rimFac.createObjectRefListType();
        orList.getObjectRef().addAll(objectRefs);
        removeRequest.setObjectRefList(orList);
        
        ServerRequestContext context = new ServerRequestContext("ClassificationSchemeCacheTest:testUpdateCacheWhenNodeRemoved", removeRequest);
        context.setUser(ac.registryOperator);

        //Remember old scheme size
        List<?> schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        int oldSchemeSize = schemes.size();        
        
        
        RegistryResponseType resp = lcm.removeObjects(context);
        bu.checkRegistryResponse(resp);
        
        //Make sure # of schemes is same as before.
        schemes = ServerCache.getInstance().getAllClassificationSchemes(context);
        assertNotNull(schemes);
        assertTrue((schemes.size() > 0));
        
        assertEquals("Cache update made cache lose some schemes", oldSchemeSize, schemes.size());
        
    }
    
    private ClassificationSchemeType getClassificationSchemeFromSchemeList(List<?> schemes, String schemeId) {
        ClassificationSchemeType scheme = null;
        
        Iterator<?> iter = schemes.iterator();
        while (iter.hasNext()) {
            ClassificationSchemeType currentScheme = (ClassificationSchemeType)iter.next();
            if (currentScheme.getId().equalsIgnoreCase(schemeId)) {
                scheme = currentScheme;
                break;
            }
        }
        return scheme;
    }
    
    private boolean schemesContainsNode(List<?> schemes, String schemeId, String nodeId) {
        boolean containsNode = false;
        
        ClassificationSchemeType scheme = getClassificationSchemeFromSchemeList(schemes, schemeId);
        List<ClassificationNodeType> children = scheme.getClassificationNode();
        
        Iterator<ClassificationNodeType> iter = children.iterator();
        while (iter.hasNext()) {
            ClassificationNodeType node = iter.next();
            System.err.println("node id:" + node.getId());
            if (node.getId().equalsIgnoreCase(nodeId)) {
                containsNode = true;
                break;
            }
        }
        
        return containsNode;
    }
    
}

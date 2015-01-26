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

package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.common.ClientTest;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.CanonicalConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for ClassificationSchemes
 *
 * @author Based on test contributed by Steve Allman
 */
public class ClassificationSchemeTest extends ClientTest {
    
    
    public ClassificationSchemeTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ClassificationSchemeTest.class);
        return suite;
    }
    
    /**
     * Tests: Server emits warnings when finding user-created ClassificationScheme
     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6444687
     *
     * 
     */
    @SuppressWarnings("static-access")
	public void testExternalSchemeStatus() throws Exception {
      String extSchemeId = "urn:freebxml:registry:test:client:ClassificationSchemeTest:testExternalScheme:externalScheme";
            
      try {
          deleteIfExist(extSchemeId);
          ClassificationScheme extScheme = lcm.createClassificationScheme("NASDAQ", 
              "OTC Stock Exchange");
          Key extSchemeKey = lcm.createKey(extSchemeId);
          extScheme.setKey(extSchemeKey);
          BulkResponse response = lcm.saveObjects(Collections.singletonList(extScheme));
          assertResponseSuccess("External Scheme save failed.", response);      

          extScheme = (ClassificationScheme)dqm.getRegistryObject(extSchemeId, lcm.CLASSIFICATION_SCHEME);
          assertNotNull("Could not read back scheme.", extScheme);
      } finally {
        deleteIfExist(extSchemeId);
      }
    }
    
    /**
     * Finds and displays a classification scheme and a hierarchy of
     * concepts. First it displays all the descendant concepts of the 
     * classification scheme. Then it displays the concept hierarchy of 
     * the classification scheme.
     *
     * @param searchString  the classification scheme name
     */
    public void testGetDescendantConcepts6316600() throws Exception {
        
        ClassificationScheme scheme = lcm.createClassificationScheme("GeographyTestScheme", null);

        // create a children of scheme
        Concept usConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        usConcept.setName(lcm.createInternationalString("United States"));
        usConcept.setValue("US");

        Concept canConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        canConcept.setName(lcm.createInternationalString("Canada"));
        canConcept.setValue("CAN");

        ArrayList<Concept> childConcepts = new ArrayList<Concept>();        
        childConcepts.add(usConcept);
        childConcepts.add(canConcept);
        scheme.addChildConcepts(childConcepts);
        
        // create grand children via US child concept
        Concept akConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        akConcept.setName(lcm.createInternationalString("Alaska"));
        akConcept.setValue("US-AK");

        Concept caConcept = (Concept)lcm.createObject(LifeCycleManager.CONCEPT);
        caConcept.setName(lcm.createInternationalString("California"));
        caConcept.setValue("US-CA");

        // add children to US Concept so we will have descendents
        childConcepts = new ArrayList<Concept>();
        childConcepts.add(caConcept);
        childConcepts.add(akConcept);
        usConcept.addChildConcepts(childConcepts);

        assertEquals(2, scheme.getChildConceptCount());           
        Collection<?> concepts = scheme.getDescendantConcepts();
        //System.err.println("scheme.getDescendantConcepts() = " + concepts);
        assertNotNull(concepts);
        assertEquals(4, concepts.size());
        
        concepts = usConcept.getDescendantConcepts();
        //System.err.println("usConcept.getDescendantConcepts() = " + concepts);
        assertNotNull(concepts);
        assertEquals(2, concepts.size());
    }
    
    
    /** Test submit of a Service */
    @SuppressWarnings("static-access")
	public void testSubmit() throws Exception {
        
        ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
        
        // create Class scheme
        ClassificationScheme testScheme = lcm.createClassificationScheme("Test Scheme", "DC");
        String schemeId = testScheme.getKey().getId();
        objects.add(testScheme);
        
        // create first child concept
        Concept node1 = lcm.createConcept(testScheme, "1", "1");
        String node1Id = node1.getKey().getId();
        testScheme.addChildConcept(node1);
        //objects.add(node1);
        
        // create second child concept
        Concept node1_2 = lcm.createConcept(testScheme, "2", "2");
        String node1_2Id = node1_2.getKey().getId();
        node1.addChildConcept(node1_2);        
        //objects.add(node1_2);
                
        // create second third concept
        // This is where the error arises: 2 levels of Concepts are fine, 3 causes problems
        Concept node1_2_3 = lcm.createConcept(testScheme, "3", "3");
        String node1_2_3Id = node1_2_3.getKey().getId();
        node1_2.addChildConcept(node1_2_3);
        objects.add(node1_2_3);
        
        BulkResponse resp = lcm.saveObjects(objects);        
        JAXRUtility.checkBulkResponse(resp);

        testScheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, lcm.CLASSIFICATION_SCHEME);
        assertNotNull("Unable to read back testScheme", testScheme);
        
        node1 = (Concept)dqm.getRegistryObject(node1Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1", node1);

        node1_2 = (Concept)dqm.getRegistryObject(node1_2Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2", node1_2);

        node1_2_3 = (Concept)dqm.getRegistryObject(node1_2_3Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2_3", node1_2_3);
    }
    
    /** Test submit of a Service */
    @SuppressWarnings("static-access")
	public void testConceptAdding() throws Exception {
        
        ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
        
        // create Class scheme
        ClassificationScheme testScheme = lcm.createClassificationScheme("Test Scheme", "DC1");
        String schemeId = testScheme.getKey().getId();
        
        // create first child concept
        Concept node1 = lcm.createConcept(testScheme, "1", "1");
        String node1Id = node1.getKey().getId();
        testScheme.addChildConcept(node1);
        
        // create second child concept
        Concept node1_2 = lcm.createConcept(testScheme, "2", "2");
        String node1_2Id = node1_2.getKey().getId();
        testScheme.addChildConcept(node1);     
                
        objects.add(testScheme);
        BulkResponse resp = lcm.saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);

        testScheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, lcm.CLASSIFICATION_SCHEME);
        assertNotNull("Unable to read back testScheme", testScheme);
        
        node1 = (Concept)dqm.getRegistryObject(node1Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1", node1);

        node1_2 = (Concept)dqm.getRegistryObject(node1_2Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2", node1_2);
        
        int childCount = testScheme.getChildConceptCount();
        assertEquals(2, childCount);
        
        Collection<?> concepts = testScheme.getChildrenConcepts();
        assertEquals(2, concepts.size());
        
        // create second child concept
        Concept node1_3 = lcm.createConcept(testScheme, "3", "3");
        String node1_3Id = node1_3.getKey().getId();
        testScheme.addChildConcept(node1_3);     

        objects = new ArrayList<RegistryObject>();
        objects.add(testScheme);
        resp = lcm.saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);

        testScheme = (ClassificationScheme)dqm.getRegistryObject(schemeId, lcm.CLASSIFICATION_SCHEME);
        assertNotNull("Unable to read back testScheme", testScheme);
        
        node1 = (Concept)dqm.getRegistryObject(node1Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1", node1);

        node1_2 = (Concept)dqm.getRegistryObject(node1_2Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_2", node1_2);
        
        node1_3 = (Concept)dqm.getRegistryObject(node1_3Id, lcm.CONCEPT);
        assertNotNull("Unable to read back node1_3", node1_3);

        childCount = testScheme.getChildConceptCount();
        assertEquals(3, childCount);
        
        concepts = testScheme.getChildrenConcepts();
        assertEquals(3, concepts.size());
        
    }

    public void testGetDescendantConcepts() throws JAXRException {
        String qString = "%Asso%";
        ClassificationScheme scheme = 
            bqm.findClassificationSchemeByName(null, qString);       
        assertNotNull(scheme);
        
        Collection<?> concepts = scheme.getDescendantConcepts();
        assertNotNull(concepts);
        
        int numDescendants = concepts.size();
        
        int numChildren = scheme.getChildrenConcepts().size();       
        
        assertTrue("There are "+numChildren+" children, but the number of "+
            "descendents is "+numDescendants, 
            numChildren > 0 && numDescendants > 0);
        
    }
            
    /* 
     * This test checks that after updating a class scheme, any child concepts
     * are not dropped. 
     */
    public void testUpdateClassScheme() throws Exception {
        System.out.println("\ntestUpdateClassScheme");
        ArrayList<Object> objects = new ArrayList<Object>();
        ClassificationScheme scheme = null;
        Concept node = null;
        try {           
            scheme = lcm.createClassificationScheme("LifeCycleManagerTest.updateTest1", "LifeCycleManagerTest.updateTest1");            
            String schemeId = scheme.getKey().getId();
            objects.add(scheme);
            //Add a child Concept as pseudo-composed object
            node = lcm.createConcept(scheme, "LifeCycleManagerTest.testNode1", "LifeCycleManagerTest.testNode1");
            String nodeId = node.getKey().getId();
            scheme.addChildConcept(node);
            HashMap<String, String> slotsMap = new HashMap<String, String>();
            slotsMap.put(CanonicalConstants.CANONICAL_SLOT_LCM_DONT_VERSION, "true");
            BulkResponse br = lcm.saveObjects(objects, slotsMap);
            assertResponseSuccess(br);
            // check that the Concept has been saved
            node = (Concept)bqm.getRegistryObject(nodeId, LifeCycleManager.CONCEPT);
            assertNotNull(node);
            // get Scheme from database
            ClassificationScheme scheme2 = (ClassificationScheme)bqm.getRegistryObject(schemeId, LifeCycleManager.CLASSIFICATION_SCHEME);
            assertNotNull(scheme2);
            
            // update the ClassificationScheme
            InternationalString is = lcm.createInternationalString("LifeCycleManagerTest.testNode1.new");
            scheme2.setName(is);
            objects.clear();
            objects.add(scheme2);
            br = lcm.saveObjects(objects, slotsMap);
            assertResponseSuccess(br);
            // retrieve Class Scheme
            node = (Concept)bqm.getRegistryObject(nodeId, LifeCycleManager.CONCEPT);
            // check that the node is still there
            assertNotNull(node);
        } finally {
            objects.clear();
            if (scheme != null) {
                objects.add(scheme.getKey());
            }
            if (node != null) {
                objects.add(node.getKey());
            }
            @SuppressWarnings("unused")
			BulkResponse br = lcm.deleteObjects(objects);
        }
    }
    
    public static void main(String[] args) {
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
            + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}

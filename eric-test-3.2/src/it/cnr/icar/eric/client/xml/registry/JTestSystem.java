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
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 * @author  mzaremba
 */
public class JTestSystem extends ClientTest {

  
  /** Creates a new instance of JTestSystem */
  public JTestSystem(String testMethod) 
  {
    super(testMethod);
  }
  
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(JTestSystem.class);
        return suite;
    }
  
    public static void main(String[] args) {
        System.out.println("Get into the program...\n");
        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
  /*
   * Test browsing for Classification Schemes (AssociationType used in this test) 
   * and for Classification Concepts
   */
  @SuppressWarnings("unused")
public void testClassificationSchemes() throws Exception {
    ArrayList<String> al = new ArrayList<String>();
    al.add("%Asso%");
    BulkResponse br = bqm.findClassificationSchemes(null, al, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("AssociationType classification schemes could not be found");
    }
    Collection<?> collection = br.getCollection();
    Iterator<?> i = collection.iterator();
    ClassificationScheme cs = (ClassificationScheme)i.next();
    assertEquals("Did not find expected ClassificationScheme on name match.", "AssociationType", cs.getName().getValue());
    
    String[] children = new String[] {"AffiliatedWith", "EmployeeOf", "MemberOf", 
                                      "RelatedTo", "HasFederationMember", "HasMember", 
                                      "ExternallyLinks", "Contains", "EquivalentTo", 
                                      "Extends", "Implements", "InstanceOf", 
                                      "Supersedes", "Uses", "Replaces", "SubmitterOf",
                                      "ResponsibleFor", "OwnerOf", "OffersService",
                                      "ContentManagementServiceFor", "InvocationControlFileFor", 
                                      "AccessControlPolicyFor"};
    ArrayList<String> childrenList = new ArrayList<String>();
    for (int index =0; index < children.length; index++) {
        childrenList.add(children[index]);
    }
    
    for (Iterator<String> it = childrenList.iterator(); it.hasNext(); ) {
        String conceptCode = it.next();
        Concept con = bqm.findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_ID_AssociationType + "/" + conceptCode);
        assertNotNull("Cannot find concept: " + con);
    }
  }
  
  @SuppressWarnings("unused")
public void testFindConcept() throws Exception {
    String[] objectTypes = new String[] {
                                         "RegistryObject", "AdhocQuery", "Association", 
                                         "AuditableEvent", "Classification", "ClassificationNode",
                                         "ExternalIdentifier", "ExternalLink", "Organization",
                                         "ServiceBinding", "SpecificationLink", "Subscription", "User",
                                         "ClassificationScheme", "Federation", "Registry", "RegistryPackage",
                                         "Service", "ExtrinsicObject", "XACML", "Policy", "PolicySet" };
    ArrayList<String> objectTypesList = new ArrayList<String>();
    for (int index =0; index < objectTypes.length; index++) {
        objectTypesList.add(objectTypes[index]);
    }
    
    ArrayList<String> cNamePats = new ArrayList<String>();
    cNamePats.add("%");
    BulkResponse br = bqm.findConcepts(null, cNamePats, null, null, null);
    assertNull(br.getExceptions());
    if (br == null) {
        fail("No Concept found that match patern %");
    }
    
    ArrayList<String> conceptsList = new ArrayList<String>();
    Collection<?> collection = br.getCollection();
    for (Iterator<?> it = collection.iterator(); it.hasNext(); ) {
        conceptsList.add(((Concept)it.next()).getValue());        
    }   
    assertTrue("Not all Object Types available in registry", conceptsList.containsAll(objectTypesList));
  }
} 


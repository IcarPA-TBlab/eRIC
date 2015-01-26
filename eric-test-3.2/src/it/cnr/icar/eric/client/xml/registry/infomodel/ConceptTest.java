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

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for ClassificationSchemes
 *
 * @author Based on test contributed by Steve Allman
 */
public class ConceptTest extends ClientTest {
    
    
    public ConceptTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ConceptTest.class);
        return suite;
    }
    
    /**
     * Tests publishing a new Concep with no parent and then
     * retrieving it successfully.
     *
     */
    public void testAddConceptWithoutParent() throws JAXRException {
    }
    
    public void testGetDescendantConcepts() throws JAXRException {
        String path = 
           "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject";
        Concept concept = bqm.findConceptByPath(path);
       
        int numDescendants = concept.getDescendantConcepts().size();
        
        int numChildren = concept.getChildrenConcepts().size();       
        
        assertTrue("There are "+numChildren+" children, but the number of "+
            "descendents is "+numDescendants, 
            numChildren > 0 && numDescendants > 0);
        
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

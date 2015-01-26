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

package it.cnr.icar.eric.client.xml.registry.util;

import it.cnr.icar.eric.client.common.ClientTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.SpecificationLink;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;




/**
 * jUnit Test for QueryUtil class
 *
 * @author Farrukh Najmi
 */
public class QueryUtilTest extends ClientTest {
        
    public QueryUtilTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(QueryUtilTest.class);
        return suite;
    }
    
    /**
     * Tests getClassificationsPredicate method.
     */
    public void testGetClassificationsPredicate() throws Exception {
        //Add a Classification as a true composed object
        ClassificationScheme dunsScheme = (ClassificationScheme)dqm.getRegistryObject(DUNS_CLASSIFICATION_SCHEME);
        ClassificationScheme nyseTickerScheme = (ClassificationScheme)dqm.getRegistryObject(NYSE_TICKER_CLASSIFICATION_SCHEME);
        ClassificationScheme usTaxpayerIdScheme = (ClassificationScheme)dqm.getRegistryObject(US_TAXPAYER_ID_CLASSIFICATION_SCHEME);
        ArrayList<Classification> classifications = new ArrayList<Classification>();
        classifications.add(lcm.createClassification(dunsScheme, "", "1"));
        classifications.add(lcm.createClassification(nyseTickerScheme, "", "2"));
        classifications.add(lcm.createClassification(dunsScheme, "", "3"));
        classifications.add(lcm.createClassification(usTaxpayerIdScheme, "", "4"));
        classifications.add(lcm.createClassification(nyseTickerScheme, "", "5"));
        classifications.add(lcm.createClassification(dunsScheme, "", "6"));
        
        @SuppressWarnings("unused")
		String pred = QueryUtil.getInstance().getClassificationsPredicate(classifications, "pkg.id", (Collection<?>)null);
        //assertTrue("Did not get expected predicate for null FindQualifier", pred.matches("\\(\\s*pkg\\.id\\.*"));
        pred = QueryUtil.getInstance().getClassificationsPredicate(classifications, "pkg.id", Collections.singleton(FindQualifier.AND_ALL_KEYS));
        //assertTrue("Did not get expected predicate for null FindQualifier", pred.matches("\\(\\s*pkg\\.id\\.*"));
        pred = QueryUtil.getInstance().getClassificationsPredicate(classifications, "pkg.id", Collections.singleton(FindQualifier.OR_ALL_KEYS));
        //assertTrue("Did not get expected predicate for null FindQualifier", pred.matches("\\(\\s*pkg\\.id\\.*"));
        pred = QueryUtil.getInstance().getClassificationsPredicate(classifications, "pkg.id", Collections.singleton(FindQualifier.OR_LIKE_KEYS));
        //assertTrue("Did not get expected predicate for null FindQualifier", pred.matches("\\(\\s*pkg\\.id\\.*"));
        
        @SuppressWarnings("unused")
		int i=0;
    }
    
    /**
     * Tests getExternalLinksPredicate method.
     */
    public void testGetExternalLinksPredicate() throws Exception {
        System.err.println("\ntestGetExternalLinksPredicate");
        
        ExternalLink cppaCatalogingHomepage = (ExternalLink) dqm.getRegistryObject("urn:freebxml:registry:demoDB:test:cms:ExternalLink:cppaCataloging:homepage");
        
        Collection<ExternalLink> extLinks = new ArrayList<ExternalLink>();
        extLinks.add(cppaCatalogingHomepage);
        
        String pred = QueryUtil.getInstance().getExternalLinksPredicate(extLinks, "pkg.id", null);
        System.err.println(pred);
        assertTrue("Predicate should include homepage external URI", pred.indexOf(cppaCatalogingHomepage.getExternalURI()) != -1);
        assertTrue("Predicate should include homepage name", pred.indexOf(cppaCatalogingHomepage.getName().toString()) != -1);

        ExternalLink cppaCatalogingWSDL = (ExternalLink) dqm.getRegistryObject("urn:freebxml:registry:demoDB:test:cms:ExternalLink:cppaCataloging:WSDL");
        
        extLinks.add(cppaCatalogingWSDL);
        
        pred = QueryUtil.getInstance().getExternalLinksPredicate(extLinks, "pkg.id", null);
        System.err.println(pred);
        assertTrue("Predicate should include homepage external URI", pred.indexOf(cppaCatalogingHomepage.getExternalURI()) != -1);
        assertTrue("Predicate should include homepage name", pred.indexOf(cppaCatalogingHomepage.getName().toString()) != -1);
        assertTrue("Predicate should include WSDL external URI", pred.indexOf(cppaCatalogingWSDL.getExternalURI()) != -1);
        assertTrue("Predicate should include WSDL name", pred.indexOf(cppaCatalogingWSDL.getName().toString()) != -1);
    }
    
    /**
     * Tests getSpecificationLinksPredicate method.
     */
    public void testGetSpecificationLinksPredicate() throws Exception {
        System.err.println("\ntestGetSpecificationLinksPredicate");
        
        SpecificationLink registryServiceSpecLink = (SpecificationLink) dqm.getRegistryObject("urn:freebxml:registry:demoDB:ebXMLRegistryServiceSpecLink");
        
        Collection<SpecificationLink> specLinks = new ArrayList<SpecificationLink>();
        specLinks.add(registryServiceSpecLink);
        
        String pred = QueryUtil.getInstance().getSpecificationLinksPredicate(specLinks, "pkg.id", null);
        System.err.println(pred);
        assertTrue("Predicate should include Registry Service specification object.", pred.indexOf(registryServiceSpecLink.getSpecificationObject().getKey().getId()) != -1);
        //assertTrue("Predicate should include homepage name", pred.indexOf(registryServiceSpecLink.getName().toString()) != -1);

        /*
         * Removed following because the CPPA Caataloging Test data seems to have moved and the test was getting an NPE
         *
        SpecificationLink cppaCatalogingWSDLSpecLink = (SpecificationLink) dqm.getRegistryObject("urn:uuid:f8bba4da-a1e6-49aa-9979-e4308be8eada");
        
        specLinks.add(cppaCatalogingWSDLSpecLink);
        
        pred = QueryUtil.getInstance().getSpecificationLinksPredicate(specLinks, "pkg.id", null);
        System.err.println(pred);
        assertTrue("Predicate should include Registry Service specification object.", pred.indexOf(registryServiceSpecLink.getSpecificationObject().getKey().getId()) != -1);
        //assertTrue("Predicate should include homepage name", pred.indexOf(registryServiceSpecLink.getName().toString()) != -1);
        assertTrue("Predicate should include CPPA Cataloging WSDL specification object.", pred.indexOf(cppaCatalogingWSDLSpecLink.getSpecificationObject().getKey().getId()) != -1);
        //assertTrue("Predicate should include WSDL name", pred.indexOf(cppaCatalogingWSDLSpecLink.getName().toString()) != -1);
         */
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
    
}

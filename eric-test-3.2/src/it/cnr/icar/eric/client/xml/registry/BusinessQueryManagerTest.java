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
import it.cnr.icar.eric.common.UUIDFactory;

import java.util.Locale;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Service;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;



/**
 * jUnit Test for BusinessQueryManager
 *
 * @author Farrukh Najmi
 */
public class BusinessQueryManagerTest extends ClientTest {
    /*
     * Pre-define FindQualifier collections used in tests.
     */

    /** Empty find qualifiers collection. */
    @SuppressWarnings("unchecked")
	protected static final Collection<String> emptyFQ = Collections.EMPTY_LIST;

    /** SORT_BT_NAME_ASC find qualifiers collection. */
    protected static final Collection<String> sortByNameAscFQ = Collections.singleton(FindQualifier.SORT_BY_NAME_ASC);

    /** SORT_BT_NAME_DESC find qualifiers collection. */
    protected static final Collection<String> sortByNameDescFQ = Collections.singleton(FindQualifier.SORT_BY_NAME_DESC);

    /** OR_ALL_KEYS find qualifiers collection. */
    protected static final Collection<String> orAllKeysFQ = Collections.singleton(FindQualifier.OR_ALL_KEYS);

    /** OR_LIKE_KEYS find qualifiers collection. */
    protected static final Collection<String> orLikeKeysFQ = Collections.singleton(FindQualifier.OR_LIKE_KEYS);

    /** EXACT_NAME_MATCH find qualifiers collection. */
    protected static final Collection<String> exactNameMatchFQ = Collections.singleton(FindQualifier.EXACT_NAME_MATCH);

    /** CASE_SENSITIVE_MATCH find qualifiers collection. */
    protected static final Collection<String> caseSensitiveMatchFQ = Collections.singleton(FindQualifier.CASE_SENSITIVE_MATCH);

    /** EXACT_NAME_MATCH and CASE_SENSITIVE_MATCH find qualifiers collection. */
    protected static final Collection<String> exactCaseSensitiveMatchFQ;

    static {
        Collection<String> tmpColl = new ArrayList<String>(2);
        tmpColl.add(FindQualifier.EXACT_NAME_MATCH);
        tmpColl.add(FindQualifier.CASE_SENSITIVE_MATCH);
        exactCaseSensitiveMatchFQ = Collections.unmodifiableCollection(tmpColl);
    }

    public BusinessQueryManagerTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(BusinessQueryManagerTest.class);
        //TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new BusinessQueryManagerTest("testLocalizedStringInNamePattern"));

        return suite;
    }
    
    /**
     * Tests that LocalizedString can be used for namePattern (bug 6378209).
     *
     */
    public void testLocalizedStringInNamePattern() throws Exception {
        ArrayList<Object> namePatterns = new ArrayList<Object>();
        LocalizedString ls = lcm.createLocalizedString(Locale.getDefault(), "ObjectType");
        namePatterns.add(ls);

        BulkResponse br = bqm.findClassificationSchemes(null,
                namePatterns, (Collection<?>) null, (Collection<?>) null);
        assertResponseSuccess("Error during findClassificationSchemes", br);        
    }

    /*
     * Test for 6325549: ClassCastException in QueryUtil.getSpecificationLinksPredicate()
     * Creates a ServiceBinding with SpecificfationLink with specificationObject
     * that is a Concept with no parent (UDDI model) and then tries findServiceBinding
     * by specificationObject.
     */
    @SuppressWarnings("static-access")
	public void testFindServicesBySpecificationObject() throws Exception {
        Service service = lcm.createService("TestService");
        ServiceBinding binding = lcm.createServiceBinding();
        service.addServiceBinding(binding);
        SpecificationLink specLink = lcm.createSpecificationLink();
        binding.addSpecificationLink(specLink);
        Concept concept = lcm.createConcept(null, "dummyConcept", "dummyValue");
        specLink.setSpecificationObject(concept);
        
        ArrayList<Service> services = new ArrayList<Service>();
        services.add(service);
        lcm.saveServices(services);
        
        //Readback to ensure objects were saved
        RegistryObject ro = bqm.getRegistryObject(service.getKey().getId(), lcm.SERVICE);
        assertEquals(service, ro);
        ro = bqm.getRegistryObject(binding.getKey().getId(), lcm.SERVICE_BINDING);
        assertEquals(binding, ro);
        ro = bqm.getRegistryObject(specLink.getKey().getId(), lcm.SPECIFICATION_LINK);
        assertEquals(specLink, ro);
        ro = bqm.getRegistryObject(concept.getKey().getId(), lcm.CONCEPT);
        assertEquals(concept, ro);
        
        try {
            ArrayList<RegistryObject> specifications = new ArrayList<RegistryObject>();
            specifications.add(concept);
            
            BulkResponse br = bqm.findServices(null, null, null, null, specifications);
            assertResponseSuccess("Error during findServiceBindings", br);
            Collection<?> services1 = br.getCollection();
            assertTrue(services1.contains(service));
            
            br = bqm.findServiceBindings(null, null, null, specifications);
            assertResponseSuccess("Error during findServiceBindings", br);
            Collection<?> bindings = br.getCollection();
            assertTrue(bindings.contains(binding));
        } finally {
            //Cleanup
            try {
                ArrayList<Key> keys = new ArrayList<Key>();
                keys.add(binding.getKey());
                keys.add(specLink.getKey());
                keys.add(concept.getKey());
                lcm.deleteObjects(keys);
            } catch (Exception e) {
                //Do nothing
                e.printStackTrace();
            }
        }
    }
        
    /**
     * Tests findCallerAssociations method.
     *
     * Note that ebXML Registry does not support parameters
     * confirmedByCaller, confirmedByOtherParty
     * as version 3.0 drops Association confirmation after
     * realizing it is a bogus idea and instead using 3.0 access control mechanisms
     * to control who is allowed to create an Association with one's objects
     * and under what constraints. The custom access control policies can do much more than
     * what association confirmation allowed us to do in the past.
     *
     */
    public void testFindCallerAssociations() throws Exception {
        ArrayList<String> associationTypes = new ArrayList<String>();
        associationTypes.add(BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_AffiliatedWith);

        BulkResponse br = bqm.findCallerAssociations(null, null, null,
                associationTypes);
        assertResponseSuccess("Error during findCallerAssociations", br);

        @SuppressWarnings("unused")
		Collection<?> associations = br.getCollection();
    }

    /**
     * Tests various findQualifiers are handled correctly.
     * Currently only tests for a subset.
     */
    public void testFindQualifiers() throws Exception {
        ArrayList<String> findQualifiers = new ArrayList<String>();
        findQualifiers.add(FindQualifier.SORT_BY_NAME_ASC);

        Collection<?> schemes = getSchemes(findQualifiers,
                "ClassificationScheme sort by name ASC.");
        String lastName = null;
        Iterator<?> iter = schemes.iterator();

        while (iter.hasNext()) {
            ClassificationScheme scheme = (ClassificationScheme) iter.next();
            String name = scheme.getName().getValue();

            if (lastName != null) {
                boolean result = (name.compareTo(lastName) >= 0);

                if (!result) {
                    System.err.println("\nName:" + name + " lastName:" +
                        lastName);
                    fail("This is a known failure that needs to be investigated and fixed by Tony. " + "ORDER BY NAME ASC failed");
                }
            }

            lastName = name;
        }

        findQualifiers.clear();
        findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
        schemes = getSchemes(findQualifiers,
                "ClassificationScheme sort by name DESC.");
        lastName = null;
        iter = schemes.iterator();

        while (iter.hasNext()) {
            ClassificationScheme scheme = (ClassificationScheme) iter.next();
            String name = scheme.getName().getValue();

            if (lastName != null) {
                boolean result = (name.compareTo(lastName) <= 0);

                if (!result) {
                    System.err.println("\nName:" + name + " lastName:" +
                        lastName);
                    fail("ORDER BY NAME DESC failed");
                }
            }

            lastName = name;
        }
    }

    public Collection<?> getSchemes(Collection<String> findQualifiers, String desc)
        throws Exception {
        ArrayList<Object> namePatterns = new ArrayList<Object>();
        namePatterns.add("%");

        BulkResponse br = bqm.findClassificationSchemes(findQualifiers,
                namePatterns, (Collection<?>) null, (Collection<?>) null);
        assertResponseSuccess("Error during findClassificationSchemes", br);

        System.err.println(desc);

        Collection<?> schemes = br.getCollection();
        Iterator<?> iter = schemes.iterator();

        while (iter.hasNext()) {
            ClassificationScheme scheme = (ClassificationScheme) iter.next();
            String name = scheme.getName().getValue();
            System.err.println("\tName: " + name);
        }

        return schemes;
    }

    /**
     * Tests bug where findAssociations was not implementing the associationTypes predicate
     * and was generating NPE if all parameters were null.
     */
    public void testfindAssociations() throws Exception {
        BulkResponse br = bqm.findAssociations((Collection<?>) null,
                (String) null, (String) null, (Collection<String>) null);
        assertTrue("findAssociations failed when all params were null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        br = bqm.findAssociations((Collection<?>) null,
                BindingUtility.CANONICAL_ROOT_FOLDER_ID, (String) null,
                (Collection<String>) null);
        assertTrue("findAssociations failed when sourceObject is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        br = bqm.findAssociations((Collection<?>) null, (String) null,
                BindingUtility.CANONICAL_ROOT_FOLDER_ID, (Collection<String>) null);
        assertTrue("findAssociations failed when targetObject is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        br = bqm.findAssociations((Collection<?>) null,
                BindingUtility.CANONICAL_ROOT_FOLDER_ID,
                BindingUtility.CANONICAL_USERDATA_FOLDER_ID, (Collection<String>) null);
        assertTrue("findAssociations failed when sourceObject and targetObject is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        ArrayList<String> assTypes = new ArrayList<String>();
        assTypes.add(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_HasMember);
        br = bqm.findAssociations((Collection<?>) null,
                BindingUtility.CANONICAL_ROOT_FOLDER_ID,
                BindingUtility.CANONICAL_USERDATA_FOLDER_ID, assTypes);
        assertTrue("findAssociations failed when sourceObject, targetObject and assocType is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        assTypes.add(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo);
        br = bqm.findAssociations((Collection<?>) null, null,
                BindingUtility.CANONICAL_USERDATA_FOLDER_ID, assTypes);
        assertTrue("findAssociations failed when targetObject and assocType is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        br = bqm.findAssociations((Collection<?>) null,
                BindingUtility.CANONICAL_ROOT_FOLDER_ID, null, assTypes);
        assertTrue("findAssociations failed when sourceObject, and assocType is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);

        br = bqm.findAssociations((Collection<?>) null, null, null, assTypes);
        assertTrue("findAssociations failed when assocType is not null.",
            br.getStatus() == BulkResponse.STATUS_SUCCESS);
    }

    /**
     * Tests findServices method.
     *
     * <p>This test depends on the presence in the registry of the "freebXML"
     * organization and the "Canonical XML Cataloging Service" service.
     */
    public void testFindServices() throws Exception {
        /*
         * Pre-define organization keys used in test.
         */

        // Key for Organization that is known to exist in Registry.
        final Key orgKey = lcm.createKey(BindingUtility.FREEBXML_REGISTRY_ORGANIZATION_ID);

        /*
         * Pre-define name patterns used in test.
         */

        // Exact name
        final Collection<String> exactNP = Collections.singleton(BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME);

        // Lower-case name
        final Collection<String> lowerCaseNP = Collections.singleton(BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME.toLowerCase());

        // Name with '%' wildcard that matches any string
        final Collection<String> percentNP = Collections.singleton("%" +
                BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME.substring(
                    2));

        // Name with two '_' wildcards that each match any character
        final Collection<String> underscoreNP = Collections.singleton("__" +
                BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME.substring(
                    2));

        // Lower-case name with '%' wildcard
        final Collection<String> lcPercentNP = Collections.singleton("%" +
                BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME.substring(
                    2).toLowerCase());

        // Lower-case name with two '_' wildcards
        final Collection<String> lcUnderscoreNP = Collections.singleton("__" +
                BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME.substring(
                    2).toLowerCase());

        /*
         * Now do the tests...
         */

        // Test with all parameters null.
        BulkResponse br = bqm.findServices(null, null, null, null, null);
        assertResponseSuccess("findServices with null parameters should succeed.",
            br);

        Collection<?> services = br.getCollection();
        assertTrue("findServices with null parameters should return more than zero services.",
            services.size() > 0);

        int allServicesSize = services.size();

        // Test with known organization.
        br = bqm.findServices(orgKey, null, null, null, null);
        assertResponseSuccess("findServices with known organization should succeed.",
            br);
        services = br.getCollection();
        assertTrue("findServices with known organization should return at least one service.",
            services.size() >= 1);
        assertTrue("findServices with known organization should return fewer services than with null organization key.",
            services.size() < allServicesSize);

        // Test with find qualifiers but no names.
        br = bqm.findServices(null, emptyFQ, null, null, null);
        assertResponseSuccess("findServices with find qualifiers but no names should succeed.",
            br);
        services = br.getCollection();
        assertTrue("findServices with find qualifiers but no names should return at least one service.",
            services.size() >= 1);
        assertTrue("findServices with find qualifiers but no names should return same services with no find qualifiers and no names.",
            services.size() == allServicesSize);

        // Test with known organization and find qualifiers but no names.
        br = bqm.findServices(orgKey, emptyFQ, null, null, null);
        assertResponseSuccess("findServices with find qualifiers but no names should succeed.",
            br);
        services = br.getCollection();
        assertTrue("findServices with known key and find qualifiers but no names should return at least one service.",
            services.size() >= 1);

        // Test with name but no find qualifiers
        br = bqm.findServices(null, null, exactNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name");

        // Test with known name and known organization but no find qualifiers
        br = bqm.findServices(orgKey, null, exactNP, null, null);
        assertResponseSuccess("findServices with known name and known organization should succeed.",
            br);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name and known key");

        // Test with lower-case name but no find qualifiers
        br = bqm.findServices(null, null, lowerCaseNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with lower-case XML Cataloging Service name");

        // Test with lower-case name and case-sensitive match find qualifier
        br = bqm.findServices(null, caseSensitiveMatchFQ, lowerCaseNP, null,
                null);
        assertResponseSuccess("findServices with known name should succeed.", br);
        assertResultIsZeroObjects(br,
            "findServices with lower-case XML Cataloging Service name and case-sensitive-match");

        // Test with name with "%" wildcard
        br = bqm.findServices(null, null, percentNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name with \"%\" wildcard");

        // Test with name with "_" wildcard
        br = bqm.findServices(null, null, underscoreNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name with \"_\" wildcard");

        // Test with name with "%" wildcard and case-sensitive-match find qualifier
        br = bqm.findServices(null, caseSensitiveMatchFQ, percentNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name with \"%\" wildcard and case-sensitive-match find qualifier");

        // Test with name with "_" wildcard and case-sensitive-match find qualifier
        br = bqm.findServices(null, caseSensitiveMatchFQ, underscoreNP, null,
                null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name with \"_\" wildcard and case-sensitive-match find qualifier");

        // Test with name with exact-name-match find qualifier
        br = bqm.findServices(null, exactNameMatchFQ, exactNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name with exact name match");

        // Test with lower-case name with exact-name-match find qualifier
        br = bqm.findServices(null, exactNameMatchFQ, lowerCaseNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with lower-case XML Cataloging Service name with exact name match");

        // Test with name with "%" wildcard and exact-name-match find qualifier
        br = bqm.findServices(null, exactNameMatchFQ, percentNP, null, null);
        assertResultIsZeroObjects(br,
            "findServices with XML Cataloging Service name with \"%\" wildcard and exact-name match");

        // Test with name with "_" wildcard and exact-name-match find qualifier
        br = bqm.findServices(null, exactNameMatchFQ, underscoreNP, null, null);
        assertResultIsZeroObjects(br,
            "findServices with XML Cataloging Service name with \"_\" wildcard and exact-name match");

        // Test with name with exact-name-match and case-sensitive-match find qualifiers
        br = bqm.findServices(null, exactCaseSensitiveMatchFQ, exactNP, null,
                null);
        assertResultIsXMLCatalogingService(br,
            "findServices with XML Cataloging Service name with exact, case-sensitive name match");

        // Test with lower-case name with exact-name-match and case-sensitive-match find qualifiers
        br = bqm.findServices(null, exactCaseSensitiveMatchFQ, lowerCaseNP,
                null, null);
        assertResultIsZeroObjects(br,
            "findServices with lower-case XML Cataloging Service name with exact, case-sensitive name match");

        // Test with lower-case name with "%" wildcard
        br = bqm.findServices(null, null, lcPercentNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with lower-case XML Cataloging Service name with \"%\" wildcard");

        // Test with lower-case name with "_" wildcard
        br = bqm.findServices(null, null, lcUnderscoreNP, null, null);
        assertResultIsXMLCatalogingService(br,
            "findServices with lower-case XML Cataloging Service name with \"_\" wildcard");

        // Test with lower-case name with "%" wildcard and case-sensitive-match find qualifier
        br = bqm.findServices(null, caseSensitiveMatchFQ, lcPercentNP, null,
                null);
        assertResultIsZeroObjects(br,
            "findServices with lower-case XML Cataloging Service name with \"%\" wildcard and case-sensitive-match find qualifier");

        // Test with lower-case name with "_" wildcard and case-sensitive-match find qualifier
        br = bqm.findServices(null, caseSensitiveMatchFQ, lcUnderscoreNP, null,
                null);
        assertResultIsZeroObjects(br,
            "findServices with lower-case XML Cataloging Service name with \"_\" wildcard and case-sensitive-match find qualifier");
    }

    /**
     * Tests External Links argument to findOrganizations method.
     */
    public void nonWorking_testFindOrganizationsExternalLinks()
        throws Exception {
        /* Currently doesn't work as expected because external links are not being
         * added to the correct organizations.  At present, all external links are
         * being added to the first organization defined.
         */

        // Strings used for names and URIs
        final String orgName = "testOrganizationsExternalLinks:: Org";
        final String URI1 = "testOrganizationsExternalLinks/URI1";
        final String URI2 = "testOrganizationsExternalLinks/URI2";
        final String linkName = "testOrganizationsExternalLinks";

        /*
         * Predefine objects used in test.
         */

        // Organization 1.
        Organization org1 = createOrganization(orgName + "1");
        org1.getKey().setId(UUIDFactory.getInstance().newUUID().toString());

        // Organization 2.
        Organization org2 = createOrganization(orgName + "2");
        org1.getKey().setId(UUIDFactory.getInstance().newUUID().toString());

        // External link A.
        ExternalLink extLinkA = lcm.createExternalLink(URI1,
                (InternationalString) null);
        extLinkA.getKey().setId(UUIDFactory.getInstance().newUUID().toString());

        // External link B.
        ExternalLink extLinkB = lcm.createExternalLink(URI2,
                (InternationalString) null);
        extLinkB.getKey().setId(UUIDFactory.getInstance().newUUID().toString());

        // External link C is just like ExternalLink B with an added name.
        ExternalLink extLinkC = lcm.createExternalLink(URI2,
                (InternationalString) null);
        extLinkC.setName(lcm.createInternationalString(linkName));
        extLinkC.getKey().setId(UUIDFactory.getInstance().newUUID().toString());

        // External link that won't match anything
        ExternalLink noMatchExtLink = lcm.createExternalLink("blah",
                (InternationalString) null);

        /*
         * Pre-define External Link collections used in tests.
         */

        // Just extLinkA
        final Collection<ExternalLink> aColl = Collections.singleton(extLinkA);

        // Just extLinkB
        final Collection<ExternalLink> bColl = Collections.singleton(extLinkB);

        // Just extLinkC
        @SuppressWarnings("unused")
		final Collection<ExternalLink> cColl = Collections.singleton(extLinkC);

        // extLinkA + extLinkB
        final Collection<ExternalLink> abColl = new ArrayList<ExternalLink>(2);
        abColl.add(extLinkA);
        abColl.add(extLinkB);

        // extLinkA + extLinkC
        final Collection<ExternalLink> acColl = new ArrayList<ExternalLink>(2);
        abColl.add(extLinkA);
        abColl.add(extLinkC);

        // extLinkA + noMatchExtLink
        final Collection<ExternalLink> aNoMatchColl = new ArrayList<ExternalLink>(2);
        abColl.add(extLinkA);
        abColl.add(noMatchExtLink);

        /*
                Collection objs = new ArrayList();
                objs.add(extLinkA);
                objs.add(extLinkB);
                objs.add(extLinkC);
                objs.add(noMatchExtLink);
                BulkResponse br1 = lcm.saveObjects(objs);
                assertResponseSuccess("Saving objects should succeed.", br1);
                // Save the saved objects' keys for deletion later.
                Collection savedObjs1 = new ArrayList(br1.getCollection());
                /*
                 * Add selected External Links to Organizations
                 */
        org1.addExternalLinks(abColl);
        org2.addExternalLinks(acColl);

        Collection<Organization> objs2 = new ArrayList<Organization>();
        objs2.add(org1);
        objs2.add(org2);

        BulkResponse br2 = lcm.saveOrganizations(objs2);
        assertResponseSuccess("Saving objects should succeed.", br2);

        // Save the saved objects' keys for deletion later.
        @SuppressWarnings({ "unused", "unchecked" })
		Collection<?> savedObjs2 = new ArrayList<Object>(br2.getCollection());

        /*
         * Now do the tests...
         */

        // Test with all parameters null.
        BulkResponse br = bqm.findOrganizations(null, null, null, null,
                null, null);
        assertResponseSuccess("findOrganizations with null parameters should succeed.",
            br);

        Collection<?> orgs = br.getCollection();
        assertTrue("findOrganizations with null parameters should return more than zero organizations.",
            orgs.size() > 0);

        @SuppressWarnings("unused")
		int allOrgsSize = orgs.size();

        // Test with one, common External Link.
        br = bqm.findOrganizations(null, null, null, null, null, aColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with common ExternalLink collection should return two organizations.",
            2, orgs.size());

        // Test with common, no-name External Link.
        br = bqm.findOrganizations(null, null, null, null, null, bColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with common, no-name ExternalLink collection should return two organizations.",
            2, orgs.size());

        // Test with named External Link.
        br = bqm.findOrganizations(null, null, null, null, null, bColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with named ExternalLink collection should return one organization.",
            1, orgs.size());

        // Test with two common External Links.
        br = bqm.findOrganizations(null, null, null, null, null, abColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with two common ExternalLinks collection should return two organizations.",
            2, orgs.size());

        // Test with common and named External Links.
        br = bqm.findOrganizations(null, null, null, null, null, acColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with common and named ExternalLinks collection should return one organization.",
            1, orgs.size());

        // Test with common and named External Links and OR_LIKE_KEYS FindQualifier.
        br = bqm.findOrganizations(orLikeKeysFQ, null, null, null, null,
                acColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with common and named ExternalLinks collection and OR_LIKE_KEYS FindQualifier should return two organizations.",
            2, orgs.size());

        // Test with common and no-match External Links.
        br = bqm.findOrganizations(null, null, null, null, null,
                aNoMatchColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with common and no-match ExternalLinks collection should return no organizations.",
            0, orgs.size());

        // Test with common and no-match External Links and OR_LIKE_KEYS FindQualifier.
        br = bqm.findOrganizations(orLikeKeysFQ, null, null, null, null,
                acColl);
        assertResponseSuccess("findOrganizations should succeed.", br);
        orgs = br.getCollection();
        assertEquals("findOrganizations with common and no-match ExternalLinks collection and OR_LIKE_KEYS FindQualifier should return two organizations.",
            2, orgs.size());return;
    }

    /**
     * Tests 'specifications' argument to findOrganizations method.
     */
    public void testFindOrganizationsClassifications()
        throws Exception {
        // Strings used for names and URIs

        /*
         * Predefine objects used in test.
         */

        //final ClassificationScheme iso3166Scheme = (ClassificationScheme)dqm.getRegistryObject(ISO_3166_CLASSIFICATION_SCHEME);
        final Concept usConcept = (Concept) dqm.getRegistryObject(
                "urn:uuid:1152986f-f5fd-4e69-963b-9088678a2e73");

        /*
         * Pre-define External Link collections used in tests.
         */
        final Collection<Classification> dunsColl = Collections.singleton(lcm.createClassification(
                    usConcept));

	        /*
	         * Now do the tests...
	         */
	
	        // Test with all parameters null.
	        BulkResponse br = bqm.findOrganizations(null, null, null, null,
	                null, null);
	        assertResponseSuccess("findOrganizations with null parameters should succeed.",
	            br);
	
	        Collection<?> orgs = br.getCollection();
	        assertTrue("findOrganizations with null parameters should return more than zero organizations.",
	            orgs.size() > 0);
	
	        @SuppressWarnings("unused")
			int allOrgsSize = orgs.size();
	
	        // Test with DUNS classification scheme.
	        br = bqm.findOrganizations(null, null, dunsColl, null, null, null);
	        assertResponseSuccess("findOrganizations should succeed.", br);
	        orgs = br.getCollection();
	
	        Iterator<?> orgsIter = orgs.iterator();
	
	        while (orgsIter.hasNext()) {
	            Organization org = (Organization) orgsIter.next();
	
	            System.err.println("Organization:: id: " +
	                org.getKey().getId() + "; name: " +
	                org.getName().getValue());
	        }
	
	        assertTrue("findOrganizations with ISO 3166 classification scheme should return more than zero organizations.",
	            orgs.size() > 0);

    }

    /**
     * Tests findServices method.
     *
     * <p>This test depends on the presence in the registry of the "freebXML"
     * organization and the "Canonical XML Cataloging Service" service.
     */
    @SuppressWarnings("unchecked")
	public void testFindServiceBindings() throws Exception {
        /*
         * Pre-define organization keys used in test.
         */

        // Key for ServiceBinding that is known to exist in Registry.
        @SuppressWarnings("unused")
		final Key serviceBindingKey = lcm.createKey(REGISTRY_SERVICE_SERVICE_BINDING);

        // Key for Service that is known to exist in Registry.
        final Key registryServiceKey = lcm.createKey(REGISTRY_SERVICE_ID);
        
        final SpecificationLink registrySpecLink = lcm.createSpecificationLink();
        final RegistryObject registrySpecObj = dqm.getRegistryObject(
                REGISTRY_SERVICE_SPECIFICATION_OBJECT_ID);
        registrySpecLink.setSpecificationObject(registrySpecObj);
        
        final Collection<SpecificationLink> registrySpecLinkColl = Collections.singleton(registrySpecLink);
        
        final SpecificationLink cppaSpecLink = lcm.createSpecificationLink();
        final RegistryObject cppaSpecObj = dqm.getRegistryObject(
                TEST_CPPA_CATALOGING_SERVICE_WSDL_EXTERNAL_LINK_ID);
        cppaSpecLink.setSpecificationObject(cppaSpecObj);
        
        @SuppressWarnings("rawtypes")
		Collection tmpColl = new ArrayList(2);
        tmpColl.add(registrySpecLink);
        tmpColl.add(cppaSpecLink);
        final Collection<?> registryAndCatalogingSpecLinkColl = Collections.unmodifiableCollection(tmpColl);

        /*
         * Now do the tests...
         */

        // Test with all parameters null.
        BulkResponse br = bqm.findServiceBindings(null, null, null, null);
        assertResponseSuccess("findServiceBindings with null parameters should succeed.",
            br);

        Collection<?> bindings = br.getCollection();
        assertTrue("findServiceBindings with null parameters should return more than zero service bindings.",
            bindings.size() > 0);

        int allBindingsSize = bindings.size();

        // Test with known ServiceBinding key.
        br = bqm.findServiceBindings(registryServiceKey, null, null, null);
        assertResponseSuccess("findServiceBindings should succeed.", br);
        bindings = br.getCollection();
        assertTrue("findServiceBindings with known Service should return at least one service binding.",
            bindings.size() >= 1);
        assertTrue("findServiceBindings with known Service should return fewer bindings than with all parameters null.",
            bindings.size() < allBindingsSize);

        // Test with find qualifiers but no names.
        br = bqm.findServiceBindings(null, emptyFQ, null, null);
        assertResponseSuccess("findServiceBindings with find qualifiers but no names should succeed.",
            br);
        bindings = br.getCollection();
        assertTrue("findServiceBindings with find qualifiers but no names should return at least one binding.",
            bindings.size() >= 1);
        assertTrue("findServiceBindings with find qualifiers but no names should return same bindings as with no find qualifiers and no names.",
            bindings.size() == allBindingsSize);

        // Test with specifications collection but no names.
        br = bqm.findServiceBindings(null, null, null, registrySpecLinkColl);
        assertResponseSuccess("findServiceBindings should succeed.",
            br);
        bindings = br.getCollection();
        assertTrue("findServiceBindings with Specification id collection should return at least one binding.",
            bindings.size() >= 1);
        assertTrue("findServiceBindings with Specification id collection should return fewer bindings than with all parameters null.",
            bindings.size() < allBindingsSize);
        
        assertBindingForService(bindings, REGISTRY_SERVICE_ID);
        
        int registrySpecBindingCount = bindings.size();
        
        // Test with multiple-id specifications collection but no names.
        br = bqm.findServiceBindings(null, null, null, registryAndCatalogingSpecLinkColl);
        assertResponseSuccess("findServiceBindings should succeed.",
            br);
        bindings = br.getCollection();
        assertTrue("findServiceBindings with multiple-obj Specification collection should not return more than single-obj Specification collection.",
            bindings.size() <= registrySpecBindingCount);
        assertTrue("findServiceBindings with multiple-obj Specification collection should return fewer bindings than with all parameters null.",
            bindings.size() < allBindingsSize);
        
        // Test with multiple-obj specifications collection and OR_ALL_KEYS FindQualifier.
        br = bqm.findServiceBindings(null, orAllKeysFQ, null, registryAndCatalogingSpecLinkColl);
        assertResponseSuccess("findServiceBindings should succeed.",
            br);
        bindings = br.getCollection();
        assertTrue("findServiceBindings with multiple-obj Specification collection with OR_ALL_KEYS should return at least two bindings.",
            bindings.size() >= 2);
        assertTrue("findServiceBindings with multiple-obj Specification collection with OR_ALL_KEYS should not return more bindings than with all parameters null.",
            bindings.size() <= allBindingsSize);
        assertTrue("findServiceBindings with multiple-obj Specification collection with OR_ALL_KEYS should not return more than single Specification id collection.",
            bindings.size() > registrySpecBindingCount);
        
        assertBindingForService(bindings, REGISTRY_SERVICE_ID);
        assertBindingForService(bindings, TEST_CPPA_CATALOGING_SERVICE_ID);        
    }
    
    /**
     * Tests the bqm.findConceptByPath(String path) method.
     */
    @SuppressWarnings("static-access")
	public void testfindConceptByPath() throws Exception {
        String path = "/urn:oasis:names:tc:ebxml-regrep:classificationScheme:ObjectType/RegistryObject";
        Concept concept = bqm.findConceptByPath(path);
        this.assertNotNull("Failed to find concept with path: " + path, concept);
    }
    
    private void assertBindingForService(Collection<?> bindings, String serviceId) throws Exception {
        boolean bindingIsBindingForService = false;
        
        Iterator<?> bindingsIter = bindings.iterator();
        while (bindingsIter.hasNext()) {
            ServiceBinding binding = (ServiceBinding) bindingsIter.next();
            
            if (binding.getService().getKey().getId().equals(serviceId)) {
                bindingIsBindingForService = true;
            }
        }
        
        assertTrue("At least one ServiceBinding should be for this service: " + serviceId, bindingIsBindingForService);
    }

    private void assertResultIsXMLCatalogingService(BulkResponse br, String desc)
        throws Exception {
        assertResultIsRegistryObject(br,
            BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_ID, desc,
            "Canonical XML Cataloging Service");
    }

    private void assertResultIsRegistryObject(BulkResponse br, String objectId,
        String testDesc, String resultDesc) throws Exception {
        assertResponseSuccess("findServices with known name should succeed.", br);

        Collection<?> resultObjects = br.getCollection();
        assertEquals(testDesc + " should return one registry object.", 1,
            resultObjects.size());

        String resultId = ((RegistryObject) getFirst(resultObjects)).getKey()
                           .getId();
        assertEquals("Result registry object should be " + resultDesc + ".",
            objectId, resultId);
    }

    private void assertResultIsZeroObjects(BulkResponse br, String desc)
        throws Exception {
        assertResponseSuccess(desc + " should succeed.", br);

        Collection<?> resultObjects = br.getCollection();
        assertEquals(desc + " should return zero objects.", 0,
            resultObjects.size());
    }

    private Object getFirst(Collection<?> collection) {
        Object object = null;

        Iterator<?> iter = collection.iterator();

        if (iter.hasNext()) {
            object = iter.next();
        }

        return object;
    }

    public static void main(String[] args) {
        System.out.println("Get into the program...\n");

        try {
            TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName() +
                " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

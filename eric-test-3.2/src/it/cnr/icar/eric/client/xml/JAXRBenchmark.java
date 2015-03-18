package it.cnr.icar.eric.client.xml;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import junit.framework.Test;
import junit.framework.TestSuite;
import it.cnr.icar.eric.client.common.ClientTest;
import it.cnr.icar.eric.client.xml.registry.infomodel.AuditableEventImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ConceptImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PersonImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectRef;
import it.cnr.icar.eric.client.xml.registry.infomodel.UserImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;

public class JAXRBenchmark extends ClientTest {

	static int loops = 10;
    static String serviceId = "urn:freebxml:registry:test:client:ServiceTest:service1";
    static String org1Id = "urn:freebxml:registry:test:client:ServiceTest:org1";
    static String org2Id = "urn:freebxml:registry:test:client:ServiceTest:org2";
	
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JAXRBenchmark.class);
        return suite;
    }
    
    public JAXRBenchmark(String testName) {
        super(testName);        
	}

    public void testBusinessQueryManagerl() throws Exception {
        System.out.print("JAXR BusinessQueryManager Test Overall ");    	

        long time = 0;

        for (int i=0; i<loops; i++) {
        	time += doFindServices();
        	time += doFindServicesBySpecificationObject();
        	time += doFindCallerAssociations();
    	}
    	
        System.out.println(" " + (time/loops) + " ms");    	
    }
    
    public void testLifeCycleManagerl() throws Exception {
        System.out.print("JAXR LifeCycleManager Test Overall ");    	

        long time = 0;

        for (int i=0; i<loops; i++) {
        	time += doSetStatusOnObject();
        	time += doNullParameters();
        	time += doImplicitSaveTrueComposedObjects();
        	time += doImplicitSaveAssociationsAndAssociatedObjects();
    	}
    	
        System.out.println(" " + (time/loops) + " ms");    	
    }

    public void testEbxml() throws Exception {
        System.out.print("JAXR ebXML Test Overall ");    	

        long time = 0;

        for (int i=0; i<loops; i++) {
        	time += doDeleteAssociation();
        	time += doSaveObjectsSavesAssociations();
        	time += doUpdateObjectsRequest();
        	time += doSubmitDeleteExtrinsicObject();
        	time += doSimpleQuery();
    	}
    	
        System.out.println(" " + (time/loops) + " ms");    	
    }
    
    public void testRIM() throws Exception {
        System.out.print("JAXR Infomodel Overall ");    	

    	long time = 0;
    	
        for (int i=0; i<loops; i++) {
    		time += doClassificationSchemeTests();
    		time += doClassificationTests();
    		time += doPersonTests();
    		time += doServiceTests();
    	}
    	
        System.out.println(" " + (time/loops) + " ms");    	
    }

    /**
     * Tests findServices method.
     *
     * <p>This test depends on the presence in the registry of the "freebXML"
     * organization and the "Canonical XML Cataloging Service" service.
     */
    public long doFindServices() throws Exception {
        long startTime = System.currentTimeMillis();
        /*
         * Pre-define organization keys used in test.
         */

        // Key for Organization that is known to exist in Registry.
        final Key orgKey = lcm.createKey(BindingUtility.FREEBXML_REGISTRY_ORGANIZATION_ID);
        final Collection<String> exactNP = Collections.singleton(BindingUtility.CANONICAL_DEFAULT_XML_CATALOGING_SERVICE_NAME);

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

        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    /*
     * Test for 6325549: ClassCastException in QueryUtil.getSpecificationLinksPredicate()
     * Creates a ServiceBinding with SpecificfationLink with specificationObject
     * that is a Concept with no parent (UDDI model) and then tries findServiceBinding
     * by specificationObject.
     */
    @SuppressWarnings("static-access")
	public long doFindServicesBySpecificationObject() throws Exception {
        long startTime = System.currentTimeMillis();
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
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
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
    public long doFindCallerAssociations() throws Exception {
        long startTime = System.currentTimeMillis();
        ArrayList<String> associationTypes = new ArrayList<String>();
        associationTypes.add(BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_AffiliatedWith);

        BulkResponse br = bqm.findCallerAssociations(null, null, null,
                associationTypes);
        assertResponseSuccess("Error during findCallerAssociations", br);

        @SuppressWarnings("unused")
		Collection<?> associations = br.getCollection();
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
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
    
    private Object getFirst(Collection<?> collection) {
        Object object = null;

        Iterator<?> iter = collection.iterator();

        if (iter.hasNext()) {
            object = iter.next();
        }

        return object;
    }
    
    /*
     * Test SetStatusOnObject extension protocol.
     * This protocol allows setting status of an Object to any
     * ClassificationNode within canonical StatysType ClassificationScheme.
     *
     * This test sets the status of the callers user to be Withdrawn which
     * is otherwise not possible and then changes it back to Submitted.
     */
    @SuppressWarnings("static-access")
	public long doSetStatusOnObject() throws Exception {
        long startTime = System.currentTimeMillis();
        User user = bqm.getCallersUser();
        List<Key> keys = new ArrayList<Key>();
        keys.add(user.getKey());
        BulkResponse br = lcm.setStatusOnObjects(keys, bu.CANONICAL_STATUS_TYPE_ID_Withdrawn);
        assertResponseSuccess("Error during testSetStatusOnObject", br);
        
        //Dont use getCallersUser as it is cache and does not have status update.
        //This is a known issue that JAXR client objects are not updated if changed in server.
        user = (User)bqm.getRegistryObject(user.getKey().getId(), lcm.USER);
        RegistryObjectRef statusRef = ((UserImpl)user).getStatusRef();
        assertEquals(bu.CANONICAL_STATUS_TYPE_ID_Withdrawn, statusRef.getId());
        
        //Now check that audit trail was generated for setStatus action
        Collection<?> auditTrail = user.getAuditTrail();
	Object events[] = auditTrail.toArray();
        AuditableEvent event = (AuditableEvent)events[auditTrail.size() - 1];
        String latestEventType = ((AuditableEventImpl)event).getEventType1();
	// Timestamps are not unique enough to be sure latest event is last
	for (int i = auditTrail.size() - 2;
	     ( 0 <= i &&
	       !latestEventType.equals(bu.CANONICAL_STATUS_TYPE_ID_Withdrawn) &&
	       event.getTimestamp().equals(((AuditableEvent)events[i])
					   .getTimestamp()) );
	     i--) {
	    // Try another 'event' -- also with latest known timestamp
	    event = (AuditableEvent)events[i];
	    latestEventType = ((AuditableEventImpl)event).getEventType1();
	}
        assertEquals("EventType did not match", bu.CANONICAL_STATUS_TYPE_ID_Withdrawn, latestEventType);        
        
        br = lcm.setStatusOnObjects(keys, bu.CANONICAL_STATUS_TYPE_ID_Submitted);
        assertResponseSuccess("Error during testSetStatusOnObject", br);
        
        user = (User)bqm.getRegistryObject(user.getKey().getId(), lcm.USER);
        statusRef = ((UserImpl)user).getStatusRef();
        assertEquals("Error during testSetStatusOnObject", bu.CANONICAL_STATUS_TYPE_ID_Submitted, statusRef.getId());
        
        //Now check that audit trail was generated for setStatus action
        auditTrail = user.getAuditTrail();
	events = auditTrail.toArray();
	event = (AuditableEvent)events[auditTrail.size() - 1];
        latestEventType = ((AuditableEventImpl)event).getEventType1();
	// Timestamps are not unique enough to be sure latest event is last
	for (int i = auditTrail.size() - 2;
	     ( 0 <= i &&
	       !latestEventType.equals(bu.CANONICAL_STATUS_TYPE_ID_Submitted) &&
	       event.getTimestamp().equals(((AuditableEvent)events[i]).
					   getTimestamp()) );
	     i--) {
	    // Try another 'event' -- also with latest known timestamp
	    event = (AuditableEvent)events[i];
	    latestEventType = ((AuditableEventImpl)event).getEventType1();
	}
        assertEquals("EventType did not match", bu.CANONICAL_STATUS_TYPE_ID_Submitted, latestEventType);
        
        try {
            br = lcm.setStatusOnObjects(keys, "some-text-that-is-not-status-id");
            assertResponseFailure("setStatus accepted any text", br);
        } catch (RegistryException e) {
            //Expected.
        }
        
        try {
            br = lcm.setStatusOnObjects(keys, bu.CANONICAL_ASSOCIATION_TYPE_ID_Contains);
            assertResponseFailure("setStatus accepted any concept", br);
        } catch (RegistryException e) {
            //Expected.
        }
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    public long doNullParameters() throws Exception {
        long startTime = System.currentTimeMillis();
		//Call various API methods with null parameter and make sure no NPE happens
		  BulkResponse br = lcm.saveObjects(null);        
		  assertTrue("save failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
		  
		  br = lcm.deleteObjects(null);        
		  assertTrue("Delete failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
		
		  br = lcm.deprecateObjects(null);        
		  assertTrue("Deprecate failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
		
		  br = lcm.unDeprecateObjects(null);        
		  assertTrue("unDeprecate failed with no keys specified.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }
    
	 /**
	 * Tests that true-composed objects are implicitly saved.
	 *
	 * A true-composed object is a composed object within RIM
	 *
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public long doImplicitSaveTrueComposedObjects() throws Exception {
        long startTime = System.currentTimeMillis();
	    //Create the pkg that is the main object to save explicitly
	    RegistryPackage pkg1 = lcm.createRegistryPackage("LifeCycleManagerTest.pkg1");
	    String pkg1Id = pkg1.getKey().getId();
	    
	    //Add a Classification as a true composed object
	    Concept xacmlConcept = (Concept)bqm.getRegistryObject(BindingUtility.CANONICAL_OBJECT_TYPE_ID_XACML, LifeCycleManager.CONCEPT);
	    assertNotNull("Unable to read xacmlConcept", xacmlConcept);
	    
	    Classification classification = (Classification)lcm.createClassification(xacmlConcept);
	    pkg1.addClassification(classification);
	    
	    ArrayList saveObjects = new ArrayList();        
	    saveObjects.add(pkg1);
	    BulkResponse br = lcm.saveObjects(saveObjects);        
	    assertTrue("pkg1 creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
	    
	    //Assert that scheme and node were both saved.
	    pkg1 = (RegistryPackage)bqm.getRegistryObject(pkg1Id, LifeCycleManager.REGISTRY_PACKAGE);
	    assertNotNull("Unable to read back pkg1", pkg1);
	    
	    assertEquals("pkg1 has incorrect classification count.", 1, pkg1.getClassifications().size());
	    
	    ArrayList deleteObjects = new ArrayList();
	    deleteObjects.add(pkg1.getKey());
	    br = lcm.deleteObjects(deleteObjects);
	    assertTrue("pkg1 deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
	    
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
	}
   
	 /**
	 * Tests that association and associated objects are implicitly saved.
	 *
	 *
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public long doImplicitSaveAssociationsAndAssociatedObjects() throws Exception {
        long startTime = System.currentTimeMillis();
	    //Create org11 that is the main object to save explicitly
	    Organization org1 = lcm.createOrganization("LifeCycleManagerTest.org1");
	    String org1Id = org1.getKey().getId();
	    
	    PostalAddress addr = lcm.createPostalAddress("streetNumber",
	                                     "street",
	                                     "city",
	                                     "stateOrProvince",
	                                     "country",
	                                     "postalCode",
	                                     "type");
	    TelephoneNumber tel = lcm.createTelephoneNumber();
	    ArrayList tels = new ArrayList();
	    tels.add(tel);
	    org1.setPostalAddress(addr);
	    org1.setTelephoneNumbers(tels);
	//
	    //Create org2 and associate it with org1 to be implicitly saved
	    Organization org2 = lcm.createOrganization("LifeCycleManagerTest.org2");
	    String org2Id = org2.getKey().getId();
	    org2.setPostalAddress(addr);
	    org2.setTelephoneNumbers(tels);
	//
	    Concept relatedToType = (Concept)bqm.getRegistryObject(
	    		"urn:oasis:names:tc:ebxml-regrep:AssociationType:RelatedTo", LifeCycleManager.CONCEPT);
	    Association ass1 = lcm.createAssociation(org2, relatedToType);
	    String ass1Id = ass1.getKey().getId();
	    
	    org1.addAssociation(ass1);
	    
	    ArrayList saveObjects = new ArrayList();        
	    saveObjects.add(org1);
	    BulkResponse br = lcm.saveObjects(saveObjects);        
	    assertTrue("org11 creation failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
	    
	    //Assert that all explicit and implicit objects were saved.
	    org1 = (Organization)bqm.getRegistryObject(org1Id, LifeCycleManager.ORGANIZATION);
	    assertNotNull("Unable to read back org1", org1);
	    
	    org2 = (Organization)bqm.getRegistryObject(org2Id, LifeCycleManager.ORGANIZATION);
	    assertNotNull("Unable to read back org2", org2);
	    
	    ass1 = (Association)bqm.getRegistryObject(ass1Id, LifeCycleManager.ASSOCIATION);
	    assertNotNull("Unable to read back ass1", ass1);
	            
	    ArrayList deleteObjects = new ArrayList();
	    deleteObjects.add(org1.getKey());
	    deleteObjects.add(org2.getKey());
	    deleteObjects.add(ass1.getKey());
	    br = lcm.deleteObjects(deleteObjects);
	    assertTrue("service1 deletion failed.", br.getStatus() == BulkResponse.STATUS_SUCCESS);
	    
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
	}
	
    
    public long doSimpleQuery() throws Exception {
        long startTime = System.currentTimeMillis();

        String qs       = "SELECT * FROM ClassScheme";
        Query query     =
            dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
        BulkResponse br = dqm.executeQuery(query);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    /**
     * Test submit of ExtrinsicObject and remove it
     *
     * @throws Exception DOCUMENT ME!
     */
    public long doSubmitDeleteExtrinsicObject() throws Exception {
        long startTime = System.currentTimeMillis();

        DataHandler dh     = getSampleDataHandler("duke-wave.gif");
        ExtrinsicObject eo = lcm.createExtrinsicObject(dh);
        ArrayList<Object> al       = new ArrayList<Object>();
        al.add(eo);

        BulkResponse br = lcm.saveObjects(al, dontVersionSlotsMap);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        String eoId = JAXRUtility.toId(eo);

        // Query the object
        RegistryObject ro = bqm.getRegistryObject(eoId);
        assertEquals("Ids should be equal", eoId,
                     JAXRUtility.toId(ro));

        assertEquals(true, ro instanceof ExtrinsicObject);

        ExtrinsicObject eo2 = (ExtrinsicObject)ro;
        @SuppressWarnings("unused")
		DataHandler ri      = eo2.getRepositoryItem();

        // Now delete it
        al.clear();
        al.add(ro.getKey());
        br = lcm.deleteObjects(al);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        // Check results of deleteObjects. ??eeg not working
        //         u.outResponse(br);
        //         Collection col = br.getCollection();
        //         Iterator it = col.iterator();
        //         assertEquals("Should have one item", true, it.hasNext());
        //         assertEquals("Ids should be equal#2", eoId, u.toId(it.next()));
        // Query the object again and check it no longer exists
        RegistryObject ro2 = null;
        try {
            ro2 = bqm.getRegistryObject(eoId);
        } catch (ObjectNotFoundException e) {
            //Expected: do nothing
        }
        
        assertNull("RegistryObject should not have been found", ro2);
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    private DataHandler getSampleDataHandler(String sampleId)
    	      throws Exception {

    	        URL url = getClass().getResource("/resources/" + sampleId);

    	        if (url == null) {
    	            throw new Exception("Resource not found");
    	        }

    	        DataHandler dh = new DataHandler(url);

    	        return dh;
    	    }
    
    /**
     * Test that LCM.saveObjects can send both a SubmitObjectsRequest
     * or an UpdateObjectsRequest.
     *
     * @throws Exception DOCUMENT ME!
     */
    public long doUpdateObjectsRequest() throws Exception {
        long startTime = System.currentTimeMillis();

        // Save a RegistryPackage named "Package1"
        RegistryPackage pkg1 =
            lcm.createRegistryPackage("Package1");
        String id            = pkg1.getKey().getId();
        ArrayList<Object> al         = new ArrayList<Object>();
        al.add(pkg1);

        BulkResponse br = lcm.saveObjects(al, dontVersionSlotsMap); // => SubmitObjectsRequest
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        // Update the name by adding a "NewName" suffix
        String pkg1Name          = pkg1.getName().getValue();
        String newName           = pkg1Name + "NewName";
        InternationalString istr =
            lcm.createInternationalString(newName);
        pkg1.setName(istr);
        al.clear();
        al.add(pkg1);
        br = lcm.saveObjects(al, dontVersionSlotsMap); // => UpdateObjectsRequest
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        // Query the object
        RegistryObject ro = bqm.getRegistryObject(id);
        assertEquals("Ids should be equal", id,
                     JAXRUtility.toId(ro));

        // Now delete it
        al.clear();
        al.add(ro.getKey());
        br = lcm.deleteObjects(al);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        // Names should be equal
        assertEquals("Names should be equal", newName,
                     ro.getName().getValue());
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    /**
     * Test that LCM.saveObjects() saves an objects associations as
     * well as the object itself.
     *
     * @throws Exception DOCUMENT ME!
     */
    public long doSaveObjectsSavesAssociations() throws Exception {
        long startTime = System.currentTimeMillis();

        // Create Association from ParentPackage to ChildPackage
        RegistryPackage parentPkg =
            lcm.createRegistryPackage("ParentPackage");
        Concept assType           =
            bqm.findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/HasMember");

        RegistryPackage childPkg =
            lcm.createRegistryPackage("ChildPackage");
        Association ass          =
            lcm.createAssociation(childPkg, assType);
        parentPkg.addAssociation(ass);

        String assId      = ass.getKey().getId();
        String childPkgId = childPkg.getKey().getId();

        // Save "ParentPackage" which should save all its Association-s too
        ArrayList<Object> al = new ArrayList<Object>();
        al.add(parentPkg);

        BulkResponse br = lcm.saveObjects(al, dontVersionSlotsMap); // => SubmitObjectsRequest
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        // Verify that the Association object exists and is of the proper type
        RegistryObject ro = bqm.getRegistryObject(assId);
        assertEquals(true, ro instanceof Association);

        // Verify that the target object exists and is of the proper type
        RegistryObject ro2 = bqm.getRegistryObject(childPkgId);
        assertEquals(true, ro2 instanceof RegistryPackage);

        // Now delete all created objects
        al.clear();
        al.add(ass.getKey());
        br = lcm.deleteObjects(al);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        al.clear();
        al.add(childPkg.getKey());
        al.add(parentPkg.getKey());
        br = lcm.deleteObjects(al);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    /**
     * Create an Association between two objects and remove it and the
     * objects
     *
     * @throws Exception DOCUMENT ME!
     */
    public long doDeleteAssociation() throws Exception {
        long startTime = System.currentTimeMillis();

        // Create Association from ParentPackage to ChildPackage
        RegistryPackage parentPkg =
            lcm.createRegistryPackage("ParentPackage");
        Concept assType           =
            bqm.findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/HasMember");
        RegistryPackage childPkg  =
            lcm.createRegistryPackage("ChildPackage");
        Association ass           =
            lcm.createAssociation(childPkg, assType);
        parentPkg.addAssociation(ass);

        // Save "ParentPackage" which should save all its Association-s too
        ArrayList<Object> al = new ArrayList<Object>();
        al.add(parentPkg);

        BulkResponse br = lcm.saveObjects(al, dontVersionSlotsMap); // => SubmitObjectsRequest
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());

        // Now delete all created objects
        al.clear();
        al.add(ass.getKey());
        br = lcm.deleteObjects(al);
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        assertNull(br.getExceptions());
        al.clear();
        al.add(parentPkg.getKey());
        al.add(childPkg.getKey());
        br = lcm.deleteObjects(al);
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
        assertNull(br.getExceptions());

        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    public long doPersonTests() throws Exception {
        long startTime = System.currentTimeMillis();

        doAddTelephoneNumbers();
        doUrl();
        
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    public long doClassificationSchemeTests() throws Exception {
        long startTime = System.currentTimeMillis();

    	doExternalSchemeStatus();
    	doGetDescendantConcepts6316600();
    	doSubmit();
    	doConceptAdding();
    	doGetDescendantConcepts();
    	doUpdateClassScheme();

        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
   }

    public long doClassificationTests() throws Exception {
        long startTime = System.currentTimeMillis();

        doGetClassifiedObject();
        doClassificationOnly();
        doExternalClassification();
        doNestedClassificationUpdate();

        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
   }

    public long doServiceTests() throws Exception {
        long startTime = System.currentTimeMillis();

        doSetProvidingOrganization();
        doDeleteService();
        
        System.out.print(".");    	    	
        return System.currentTimeMillis()-startTime;
    }

    public void doSetProvidingOrganization() throws Exception {
        Service service = getLCM().createService("ServiceTest_Service1");
        service.setKey(getLCM().createKey(serviceId));
        
        //Set to expire on my 100th birthday. Test expiration truncation bug reported by Rajesh.
        service.setExpiration(new java.util.Date((new java.util.GregorianCalendar(2061, 0, 10)).getTimeInMillis()));    

        service.setStability(Service.STABILITY_STATIC);
        
        Collection<RegistryObject> objects = new ArrayList<RegistryObject>();
        objects.add(service);
        
        BulkResponse resp = getLCM().saveObjects(objects);
        JAXRUtility.checkBulkResponse(resp);

        Organization org1 = createOrganization("ServiceTest_Org1");
        org1.setKey(getLCM().createKey(org1Id));
        
        Organization org2 = createOrganization("ServiceTest_Org2");
        org2.setKey(getLCM().createKey(org2Id));
        
        objects = new ArrayList<RegistryObject>();
        objects.add(org1);
        objects.add(org2);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);
        
        service = (Service) getBQM().getRegistryObject(serviceId);
        
        // Set the providing organization to 'org1'.
        service.setProvidingOrganization(org1);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org1Id);

        // Set the providing organization to 'org2'.
        service.setProvidingOrganization(org2);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org2Id);

        // Set providing organization to null.
        service.setProvidingOrganization(null);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, null);

        // Set the providing organization back to 'org1'.
        service.setProvidingOrganization(org1);
        
        objects = new ArrayList<RegistryObject>();
        objects.add(service);
        resp = lcm.saveObjects(objects, dontVersionSlotsMap);
        JAXRUtility.checkBulkResponse(resp);

        assertOrgIsProvidingOrg(serviceId, org1Id);
    }
    
    /** Test delete of a Service */
    public void doDeleteService() throws Exception {
        //Delete the service that was created in testSubmit
        ArrayList<Key> keys = new ArrayList<Key>();
        keys.add(getLCM().createKey(serviceId));
        BulkResponse resp = getLCM().deleteObjects(keys, LifeCycleManager.SERVICE);
        JAXRUtility.checkBulkResponse(resp);
        
        // delete associations of service.
        resp = getBQM().findAssociations(null, null, serviceId, null);
        JAXRUtility.checkBulkResponse(resp);
        resp = lcm.deleteObjects(JAXRUtility.getKeysFromObjects(resp.getCollection()));
        JAXRUtility.checkBulkResponse(resp);
        
        //Delete the organization that was created in testSetProvidingOrganization().
        keys = new ArrayList<Key>();
        keys.add(getLCM().createKey(org1Id));
        keys.add(getLCM().createKey(org2Id));
        resp = getLCM().deleteObjects(keys, LifeCycleManager.ORGANIZATION);
        JAXRUtility.checkBulkResponse(resp);
    }

    protected void assertOrgIsProvidingOrg(String serviceId, String orgId) throws Exception {
        Service service = (Service) getBQM().getRegistryObject(serviceId);
        
        if (orgId == null) {
            assertNull("Service should have null providing Organization.", service.getProvidingOrganization());
        } else {
            try {
                Organization org = service.getProvidingOrganization();
                assertNotNull("Service's providing Organization should not be null", org);
                assertEquals("Service's providing Organization id should match.", orgId, org.getKey().getId());
            } catch (ClassCastException e) {
                fail("Service's providing Organization should be an Organization.");
            }
	}
    }

    @SuppressWarnings({ "unused", "unchecked" })
	public void doAddTelephoneNumbers() throws Exception {
        TelephoneNumber tn1, tn2, tn3 = null;
        BulkResponse br= null;
        ArrayList<User> personsList = new ArrayList<User>();
        
        PersonImpl person = createPerson("TestPersonWithTN");

        tn1 = lcm.createTelephoneNumber();
        tn1.setType("Office Phone");
        
        tn2 = lcm.createTelephoneNumber();
        tn2.setType("Mobile Phone");

        tn3 = lcm.createTelephoneNumber();
        tn3.setType("Fax");
        
        List<TelephoneNumber> tels = new ArrayList<TelephoneNumber>();
        tels.add(tn1);
        tels.add(tn2);
        tels.add(tn3);
        
        person.setTelephoneNumbers(tels);
        personsList.add(person);
        lcm.saveObjects(personsList);

        PersonImpl retrievePerson = (PersonImpl)dqm.getRegistryObject(person.getKey().getId());
        assertNotNull("person was not saved", retrievePerson);
        
        Collection<TelephoneNumber> retList = retrievePerson.getTelephoneNumbers("Fax");
        assertEquals("Count of Telephone Numbers returned from Person should be 1.", 1, retList.size());
    }
             
    public void doUrl() throws JAXRException,Exception{
        
        //Creating a new User
        User user =lcm.createUser();
        
        //Adding URL to User
        URL url =new URL("http://TheCoffeeBreak.com/JaneMDoe.html");
        user.setUrl(url);
             
        PersonName personName = lcm.createPersonName("Fazuluddin","","Mohammed");
        user.setPersonName(personName);

        //Adding PostalAddress to User
        Collection<PostalAddress> postalAddr = new ArrayList<PostalAddress>();
        PostalAddress postalAddress = lcm.createPostalAddress("1112","Longford","Bangalore","Karnataka","India","600292","String");
        postalAddr.add(postalAddress);
        user.setPostalAddresses(postalAddr);

        //Adding User Informaion to Collection Object and Saving to Registry
        Collection<User> userObject =new ArrayList<User>();
        userObject.add(user);
        lcm.saveObjects(userObject);
        
        User user1  = (User)bqm.getRegistryObject(user.getKey().getId(), LifeCycleManager.USER);
        
        //Testing with expected result
        assertEquals(url,user1.getUrl());
                    
    }

    public void doNestedClassificationUpdate() throws Exception {

        RegistryPackage pkg1 = lcm.createRegistryPackage("ClassificationTest.pkg1");
        String pkg1Id = pkg1.getKey().getId();        
        
        //Add a Classification as a true composed object
        //ConceptImpl xacmlConcept = (ConceptImpl)dqm.getRegistryObject(BindingUtility.CANONICAL_OBJECT_TYPE_ID_XACML, LifeCycleManager.CONCEPT);
        //assertNotNull("Unable to read xacmlConcept", xacmlConcept);

        ConceptImpl xacmlConcept = (ConceptImpl)bqm.getRegistryObject(
                CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_Implements,
                LifeCycleManager.CONCEPT);
        assertNotNull("Unable to read xacmlConcept", xacmlConcept);
        
        Classification parentClassification = lcm.createClassification(xacmlConcept);
        Classification childClassification = lcm.createClassification(xacmlConcept);
        parentClassification.addClassification(childClassification);

        pkg1.addClassification(parentClassification);
                
        //Now save pkg1 and its nested Classification
        Collection<RegistryPackage> saveObjects = new ArrayList<RegistryPackage>();
        saveObjects.add(pkg1);
        @SuppressWarnings("unused")
		BulkResponse response = lcm.saveObjects(saveObjects);
        
        //Now read back pkg1 to verify that it was saved
        pkg1 = (RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNotNull("pkg1 was not saved", pkg1);
        assertEquals("pkg1 does not have correct number of Classifications", 1, pkg1.getClassifications().size());

        parentClassification = (Classification)(pkg1.getClassifications().toArray())[0];
        assertEquals("parentClassification does not have correct number of child Classifications", 1, parentClassification.getClassifications().size());
        
                
        //Now delete pkg1
        ArrayList<Key> deleteObjects = new ArrayList<Key>();
        deleteObjects.add(pkg1.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg1=(RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNull("pkg1 was not deleted", pkg1);

    }

    /**
     * Tests creating an object with an external classification and then
     * finding the object classified by that external Classification.
     * External Classifications use an external ClassificationScheme (a scheme
     * with no ClassificationNode) and a value.
     *
     */
    public void doExternalClassification() throws Exception {
        String pkg1Id = "urn:freebxml:registry:test:ClassificationTest.testExternalClassification:pkg1";

        RegistryPackage pkg1 = lcm.createRegistryPackage(pkg1Id);
        pkg1.getKey().setId(pkg1Id);
        
        //Add a Classification as a true composed object
        ClassificationScheme dunsScheme = (ClassificationScheme)dqm.getRegistryObject(DUNS_CLASSIFICATION_SCHEME);
        Classification dunsClassification = lcm.createClassification(dunsScheme, "", pkg1Id);

        pkg1.addClassification(dunsClassification);
                
        //Now save pkg1 and its DUNS Classification
        Collection<RegistryPackage> saveObjects = new ArrayList<RegistryPackage>();
        saveObjects.add(pkg1);
        BulkResponse br = lcm.saveObjects(saveObjects);
        assertResponseSuccess("Saving pkg1 failed.", br);                
        
        //Now find pkg1 by DUNS classification
        ArrayList<Classification> classifications = new ArrayList<Classification>();
        classifications.add(dunsClassification);
        br = bqm.findRegistryPackages(null, null, classifications, null);
        assertResponseSuccess("Find for pkg1 failed.", br);

        assertTrue("pkg1 was not saved", br.getCollection().contains(pkg1));
        pkg1 = (RegistryPackage)br.getCollection().toArray()[0];
        assertEquals("pkg1 does not have correct number of Classifications", 1, pkg1.getClassifications().size());
                        
        //Now delete pkg1
        ArrayList<Key> deleteObjects = new ArrayList<Key>();
        deleteObjects.add(pkg1.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg1=(RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNull("pkg1 was not deleted", pkg1);

    }

    /**
     * Tests saving a classification directly and not through its clasified object.
     */
    public void doClassificationOnly() throws Exception {
        String pkg0Id = "urn:freebxml:registry:test:ClassificationTest.testClassificationOnly:pkg0";
        String clf0Id = "urn:freebxml:registry:test:ClassificationTest.testClassificationOnly:clf0";
                
        // Create a package w/o classification
        RegistryPackage pkg0 = lcm.createRegistryPackage(pkg0Id+":name");
        pkg0.getKey().setId(pkg0Id);
        ArrayList<RegistryObject> saveObjects = new ArrayList<RegistryObject>();
        saveObjects.add(pkg0);
        BulkResponse br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving pkg0 failed.", br);                

        //Create a classification for that pkg
        ClassificationScheme dunsScheme = (ClassificationScheme)dqm.getRegistryObject(DUNS_CLASSIFICATION_SCHEME);
        Classification clf0 = lcm.createClassification(dunsScheme, "", clf0Id+":value");
        clf0.getKey().setId(clf0Id);
        //Manually set the classified obj
        clf0.setClassifiedObject(pkg0);
        
        //Save the classification separately (Should also save the package)
        saveObjects.clear();
        saveObjects.add(clf0);
        br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving clf0 failed.", br);                

        //Now find pkg0 by DUNS classification
        ArrayList<Classification> classifications = new ArrayList<Classification>();
        classifications.add(clf0);
        br = bqm.findRegistryPackages(null, null, classifications, null);
        assertResponseSuccess("Find for pkg0 failed.", br);
        assertTrue("pkg0 was not saved", br.getCollection().contains(pkg0));
        pkg0 = (RegistryPackage)br.getCollection().toArray()[0];
        assertEquals("pkg0 does not have correct number of Classifications", 1, pkg0.getClassifications().size());
        
        //Modify the classification only, save it separately
        clf0.setName(lcm.createInternationalString(clf0+":name"));
        saveObjects.clear();
        saveObjects.add(clf0);
        br = lcm.saveObjects(saveObjects, dontVersionSlotsMap);
        assertResponseSuccess("Saving clf0 failed.", br);                
        
        //Now find pkg0 by DUNS classification, again
        classifications.clear();
        classifications.add(clf0);
        br = bqm.findRegistryPackages(null, null, classifications, null);
        assertResponseSuccess("Find for pkg0 failed.", br);
        assertTrue("pkg0 was not saved", br.getCollection().contains(pkg0));
        pkg0 = (RegistryPackage)br.getCollection().toArray()[0];
        assertEquals("pkg0 does not have correct number of Classifications", 1, pkg0.getClassifications().size());
        assertEquals("clf0 has not been saved", clf0+":name",
                ((Classification)pkg0.getClassifications().toArray()[0]).getName().getValue());
                        
        //Now delete pkg0 and clf0
        ArrayList<Key> deleteObjects = new ArrayList<Key>();
        deleteObjects.add(pkg0.getKey());
        deleteObjects.add(clf0.getKey());
        lcm.deleteObjects(deleteObjects, null, forceRemoveRequestSlotsMap, null);
        
        //Now read back bto verify that they were deleted
        pkg0=(RegistryPackage)dqm.getRegistryObject(pkg0Id);
        assertNull("pkg0 was not deleted", pkg0);
    }

    /**
     * Test to get Classified Object of a classification
     * @throws Exception
     */
    public void doGetClassifiedObject() throws Exception {
        String classificationId = "urn:uuid:cc7278a3-acbf-46e2-9001-c65ad02a0a39";  //id for classification with name Developer
        Classification classi = (Classification)dqm.getRegistryObject(classificationId);
        assertNotNull("Could not find classification. Was Demo DB created?", classi);
        assertNotNull("Could not find classified object", classi.getClassifiedObject());
    }

	@SuppressWarnings("static-access")
	public void doExternalSchemeStatus() throws Exception {
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
	
    public void doGetDescendantConcepts6316600() throws Exception {      
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
	public void doSubmit() throws Exception {       
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
	public void doConceptAdding() throws Exception {
        
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

    public void doGetDescendantConcepts() throws JAXRException {
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
    public void doUpdateClassScheme() throws Exception {
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

}

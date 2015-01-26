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
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;

import java.net.URL;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


import junit.framework.Test;
import junit.framework.TestSuite;


/*
 * First set of JAXR ebXML provider unit tests
 *
 * Use following properties to configure parameters:
 *
 *    jaxr-ebxml.soap.url = URL used to connect to registry server
 *
 */
public class EbxmlTest1 extends ClientTest {
    
    /**
     * Creates a new EbxmlTest1 object.
     *
     * @param testMethod DOCUMENT ME!
     */
    public EbxmlTest1(String testMethod) {
        super(testMethod);
    }

    public static Test suite() throws Exception {
        return new TestSuite(EbxmlTest1.class);
    }
    
    /*
     * Test the capability level
     */
    public void testCapabilityLevel() throws Exception {

        CapabilityProfile profile =
            service.getCapabilityProfile();
        int capabilityLevel       = profile.getCapabilityLevel();
        assertEquals(capabilityLevel, 1);
    }

    /**
     * Create an Association between two objects and remove it and the
     * objects
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testDeleteAssociation() throws Exception {

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
    }

    /**
     * Test that LCM.saveObjects() saves an objects associations as
     * well as the object itself.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testSaveObjectsSavesAssociations() throws Exception {

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
    }

    /**
     * Test that LCM.saveObjects can send both a SubmitObjectsRequest
     * or an UpdateObjectsRequest.
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testUpdateObjectsRequest() throws Exception {

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
    }

    /**
     * Test createAssociation and removeAssociation w/o saving to
     * server
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testCreateRemoveAssociationOnClient() throws Exception {

        RegistryPackage parentPkg =
            lcm.createRegistryPackage("ParentPackage");
        Concept assType           =
            bqm.findConceptByPath("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/HasMember");
        RegistryPackage childPkg  =
            lcm.createRegistryPackage("ChildPackage");
        Association ass           =
            lcm.createAssociation(childPkg, assType);
        parentPkg.addAssociation(ass);

        Association ass2 =
            (Association)JAXRUtility.getFirstObject(parentPkg
                                                           .getAssociations());
        assertEquals("Ids should be equal",
                     JAXRUtility.toId(ass),
                     JAXRUtility.toId(ass2));
        parentPkg.removeAssociation(ass);
        ass2 =
            (Association)JAXRUtility.getFirstObject(parentPkg
                                                           .getAssociations());
        assertNull("No Associations should have been found", ass2);
    }

    /**
     * Test submit of ExtrinsicObject and remove it
     *
     * @throws Exception DOCUMENT ME!
     */
    public void testSubmitDeleteExtrinsicObject() throws Exception {

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
    }

    /**
     *
     */
    public void testSimpleQuery() throws Exception {

        String qs       = "SELECT * FROM ClassScheme";
        Query query     =
            dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
        BulkResponse br = dqm.executeQuery(query);
        assertNull(br.getExceptions());
        assertEquals(BulkResponse.STATUS_SUCCESS, br.getStatus());
    }

    /**
     * DOCUMENT ME!
     *
     * @param sampleId a relative path to a sample file. Sample files
     *        are stored as resources.
     *
     * @return DataHandler representing sample file
     *
     * @throws Exception DOCUMENT ME!
     */
    private DataHandler getSampleDataHandler(String sampleId)
      throws Exception {

        URL url = getClass().getResource("/resources/" + sampleId);

        if (url == null) {
            throw new Exception("Resource not found");
        }

        DataHandler dh = new DataHandler(url);

        return dh;
    }
        
    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
                + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
}

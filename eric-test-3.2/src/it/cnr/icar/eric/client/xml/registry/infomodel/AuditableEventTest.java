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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * jUnit Test for AuditableEvent
 *
 * @author Farrukh S. Najmi
 */
public class AuditableEventTest extends ClientTest {

    public AuditableEventTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(AuditableEventTest.class);
        return suite;
    }
    
    /** Test creation of an Extrinsic Object */
    @SuppressWarnings("static-access")
	public void testGetAssociatedObjects() throws Exception {
        RegistryPackage pkg1 = lcm.createRegistryPackage("AuditableTest.pkg1");
        String pkg1Id = pkg1.getKey().getId();        
	deleteIdToTypeMap.put(pkg1Id, lcm.REGISTRY_PACKAGE);
                        
        //Now save pkg1
        Collection<RegistryPackage> saveObjects = new ArrayList<RegistryPackage>();
        saveObjects.add(pkg1);
        lcm.saveObjects(saveObjects);
        
        //Now read back pkg1 to verify that it was saved
        pkg1 = (RegistryPackage)dqm.getRegistryObject(pkg1Id);
        assertNotNull("pkg1 was not saved", pkg1);
        
        //Now getAuditTrail andmake sure it is not empty
        Collection<?> aeList = pkg1.getAuditTrail();
        assertEquals("Invalid AuditableEvent count", 1, aeList.size());
        
        AuditableEventImpl ae = (AuditableEventImpl)aeList.iterator().next();
        // deprecated: RegistryObject ro = ae.getRegistryObject();
        // assertNotNull("AuditableEvent.getRegistryObject() returned null", ro);
	List<?> affected = ae.getAffectedObjects();
	assertNotNull("AuditableEvent.getAffectedObjects() returned null",
		      affected);
	assertEquals("AuditableEvent.getAffectedObjects() unexpected list size",
		     1, affected.size());

	RegistryObject ro = (RegistryObject)affected.get(0);
	assertNotNull("AuditableEvent.getAffectedObjects() included null", ro);

        assertEquals("Unexpected object in affectedObjects",
		     pkg1Id, ro.getKey().getId());
    }
}

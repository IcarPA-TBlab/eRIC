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
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * jUnit Test for BusinessLifeCycleManager
 *
 * @author Farrukh Najmi
 */
public class BusinessLifeCycleManagerTest extends ClientTest {
        
    public BusinessLifeCycleManagerTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(BusinessLifeCycleManagerTest.class);
        return suite;
    }
    
    /**
     * Duplicates a very tough TCK test.
     * This test has been removed because it takes over 2 hours to run.
     */
    @SuppressWarnings("unchecked")
	public void xxxtestIsConfirmedExtramuralPubl() throws Exception {
        BulkResponse br = null;
        Key savekey = null;
        Collection<Association> sourceKeys = null;
        Collection<Association> targetKeys = null;

        //Create 2 sets of connections etc. for 2 different identities
        Connection farrukhConnection = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH);
        DeclarativeQueryManagerImpl farrukhDQM = (DeclarativeQueryManagerImpl)farrukhConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager farrukhBQM = farrukhConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        @SuppressWarnings("unused")
		DeclarativeQueryManagerImpl nikolaDQM = (DeclarativeQueryManagerImpl)nikolaConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager nikolaBQM = nikolaConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
                
        try {
            Organization target = nikolaLCM.createOrganization(nikolaLCM.createInternationalString("Org Target"));
            Organization source = farrukhLCM.createOrganization(farrukhLCM.createInternationalString("Org Source"));
            
            // publish the organizations
            Collection<Organization> orgs = new ArrayList<Organization>();
            orgs.add(source);
            br = farrukhLCM.saveOrganizations(orgs); // publish to registry
            assertResponseSuccess("Error during saveOrganizations", br);

            sourceKeys = br.getCollection();
            Iterator<Association> iter = sourceKeys.iterator();
            while ( iter.hasNext() ) {
                savekey = (Key) iter.next();
            }
            String sourceId = savekey.getId();
            Organization pubSource =  (Organization)farrukhBQM.getRegistryObject(sourceId, LifeCycleManager.ORGANIZATION);

            orgs.clear();
            orgs.add(target);
            br = nikolaLCM.saveOrganizations(orgs); // publish to registry
            assertResponseSuccess("Error during saveOrganizations", br);
            
            targetKeys = br.getCollection();
            iter = targetKeys.iterator();
            while ( iter.hasNext() ) {
                savekey = (Key) iter.next();
            }
            String targetId = savekey.getId();

            Organization pubTarget =  (Organization)nikolaBQM.getRegistryObject(targetId, LifeCycleManager.ORGANIZATION);
            
            Concept associationType = (Concept)farrukhDQM.getRegistryObject(
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_Implements,
                LifeCycleManager.CONCEPT);
            assertNotNull("assocType is null", associationType);
            ArrayList<Concept> assocTypes = new ArrayList<Concept>();
            assocTypes.add(associationType);
            
            Association a = nikolaLCM.createAssociation(pubTarget, associationType);
            a.setSourceObject(pubSource);
            //System.err.println("Association: " + a.toString());
            
            nikolaLCM.confirmAssociation(a);
            //System.err.println("Association: " + a.toString());

            // publish the Association
            Collection<Association> associations = new ArrayList<Association>();
            associations.add(a);
            // user 2 saves the association.
            br = nikolaLCM.saveAssociations(associations, false);
            assertResponseSuccess("Error during saveAssociations", br);
            
            // get back the association
            Collection<Association> associationKeys = br.getCollection();
            iter = associationKeys.iterator();
            Key assocKey = (Key)iter.next();
            assertEquals("assocKey not matched.", a.getKey().getId(), assocKey.getId());
            
            ArrayList<Concept> associationTypes = new ArrayList<Concept>();
            associationTypes.add(associationType);
            //confirmedByCaller = false, confirmedByOtherParty = true.
            br = farrukhBQM.findCallerAssociations( null, Boolean.FALSE, Boolean.TRUE, associationTypes);
            assertResponseSuccess("Error during findCallerAssociations", br);
            
            associations = br.getCollection();
            assertFalse("findCallerAssociations did not return an association as expected", ( associations.size() == 0 ));
            
            assertTrue("Did not get expected association with findCallerAssociations", associations.contains(a));            
            iter = associations.iterator();
            while ( iter.hasNext() ) {
                Association ass = iter.next();
                if (ass.getKey().getId().equals(assocKey.getId())) {
                    a = ass;
                    break;
                }
            }            
            //System.err.println("Association: " + a.toString());
            
            assertFalse("isConfirmed incorrectly returned true", a.isConfirmed());
            assertFalse("isConfirmedBySourceOwner incorrectly returned true", a.isConfirmedBySourceOwner());
            
            // now confirm the association
            farrukhLCM.confirmAssociation(a);
            assertResponseSuccess("Error during confirmAssociation", br);
            
            associations.clear(); //*****************
            associations.add(a);
            br = farrukhLCM.saveAssociations(associations, false);
            assertResponseSuccess("Error during saveAssociation", br);

            br = farrukhBQM.findCallerAssociations( null, Boolean.TRUE, Boolean.TRUE, associationTypes);
            assertResponseSuccess("Error during findCallerAssociations", br);

            associations = br.getCollection();
            iter = associations.iterator();
            while ( iter.hasNext() ) {
                Association ass = iter.next();
                if (ass.getKey().getId().equals(assocKey.getId())) {
                    a = ass;
                    break;
                }
            }            

            assertTrue("isConfirmed incorrectly returned false", a.isConfirmed());
            assertTrue("isConfirmedBySourceOwner incorrectly returned false", a.isConfirmedBySourceOwner());        
         } finally {
               // clean up - get rid of published orgs
             try {
               farrukhLCM.deleteOrganizations(sourceKeys);
               nikolaLCM.deleteOrganizations(targetKeys);

             } catch (JAXRException je) { 
             }
         }

    } // end of method
    
    @SuppressWarnings("unchecked")
	public void testConfirmAssociations() throws Exception {
        //farrukh own ass and srcObject. Nikola owns targetObjects
        
        ArrayList<RegistryObject> objects = new ArrayList<RegistryObject>();
        
        Connection nikolaConnection = getConnection(AuthenticationServiceImpl.ALIAS_NIKOLA);
        @SuppressWarnings("unused")
		DeclarativeQueryManagerImpl nikolaDQM = (DeclarativeQueryManagerImpl)nikolaConnection.getRegistryService().getDeclarativeQueryManager();
        @SuppressWarnings("unused")
		BusinessQueryManager nikolaBQM = nikolaConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager nikolaLCM = nikolaConnection.getRegistryService().getBusinessLifeCycleManager();
        
        Connection farrukhConnection = getConnection(AuthenticationServiceImpl.ALIAS_FARRUKH);
        DeclarativeQueryManagerImpl farrukhDQM = (DeclarativeQueryManagerImpl)farrukhConnection.getRegistryService().getDeclarativeQueryManager();
        BusinessQueryManager farrukhBQM = farrukhConnection.getRegistryService().getBusinessQueryManager();
        BusinessLifeCycleManager farrukhLCM = farrukhConnection.getRegistryService().getBusinessLifeCycleManager();
        
        RegistryObject nikolaObject = nikolaLCM.createRegistryPackage("NikolaPackage");
        objects.clear();
        objects.add(nikolaObject);
        nikolaLCM.saveObjects(objects);
        nikolaObject = dqm.getRegistryObject(nikolaObject.getKey().getId(), LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to save nikolaObject", nikolaObject);
        
        RegistryObject farrukhObject = farrukhLCM.createRegistryPackage("FarrukhPackage");
        objects.clear();
        objects.add(farrukhObject);
        farrukhLCM.saveObjects(objects);
        farrukhObject = dqm.getRegistryObject(nikolaObject.getKey().getId(), LifeCycleManager.REGISTRY_PACKAGE);
        assertNotNull("Unable to save farrukhObject", farrukhObject);
                
        Concept assocType = (Concept)farrukhDQM.getRegistryObject(
            BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_RelatedTo,
            LifeCycleManager.CONCEPT);
        ArrayList<Concept> assocTypes = new ArrayList<Concept>();
        assocTypes.add(assocType);
        
        //Create an extramural assoc 
        Association extramuralAss = farrukhLCM.createAssociation(nikolaObject, assocType);
        farrukhObject.addAssociation(extramuralAss);
        
        nikolaLCM.confirmAssociation(extramuralAss);
        
        objects.clear();
        objects.add(extramuralAss);
        nikolaLCM.saveObjects(objects);
                        
        //Get asses confirmed by other party 
        BulkResponse br = farrukhBQM.findCallerAssociations((Collection<?>)null,
            Boolean.FALSE, Boolean.TRUE,
            assocTypes);
        assertResponseSuccess("Error during findCallerAssociations", br);
        
        Collection<Association> asses = br.getCollection();
        assertTrue("This is a known failure that needs to be investigated and fixed by Farrukh. " + "isConfirmedByOtherParty extramuralAssociation not found by findCallersAssociations", asses.contains(extramuralAss));

        assertFalse("extramuralAss.isConfirmedBySourceOwner() should not return true.", (extramuralAss.isConfirmedBySourceOwner()));
        assertTrue("extramuralAss.isConfirmedByTargetOwner() should not return false.", (extramuralAss.isConfirmedByTargetOwner()));
        assertFalse("extramuralAss.isConfirmed() should not return true.", (extramuralAss.isConfirmed()));

        
        
        farrukhLCM.confirmAssociation(extramuralAss);
        objects.clear();
        objects.add(extramuralAss);
        farrukhLCM.saveObjects(objects);
        
        
        //Switch callers and find same asses by getting asses confirmed by caller 
        br = farrukhBQM.findCallerAssociations((Collection<?>)null,
            Boolean.TRUE, Boolean.TRUE, 
            assocTypes);
        assertResponseSuccess("Error during findCallerAssociations", br);
        
        asses = br.getCollection();
        assertTrue("isConfirmedByOtherParty extramuralAssociation not found by findCallersAssociations", asses.contains(extramuralAss));
        
        extramuralAss = (Association)farrukhBQM.getRegistryObject(extramuralAss.getKey().getId(), LifeCycleManager.ASSOCIATION);
        assertTrue("extramuralAss.isConfirmedBySourceOwner() should not return false.", (extramuralAss.isConfirmedBySourceOwner()));
        assertTrue("extramuralAss.isConfirmedByTargetOwner() should not return false.", (extramuralAss.isConfirmedByTargetOwner()));
        assertTrue("extramuralAss.isConfirmed() should not return false.", (extramuralAss.isConfirmed()));
        
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

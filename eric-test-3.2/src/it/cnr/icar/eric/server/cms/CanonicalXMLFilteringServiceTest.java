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

package it.cnr.icar.eric.server.cms;

import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * JUnit TestCase for CanonicalXMLFilteringServiceTest.
 */
public class CanonicalXMLFilteringServiceTest extends ServerTest {
    protected static PersistenceManager pm = PersistenceManagerFactory.getInstance()
                                                                      .getPersistenceManager();

    /**
     * Constructor for CanonicalXMLFilteringServiceTest
     *
     * @param name
     */
    public CanonicalXMLFilteringServiceTest(String name) {
        super(name);

    }

    public static Test suite() {
        return new TestSuite(CanonicalXMLFilteringServiceTest.class);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
        
    /*
     * This test verifies that the CanonicalXMLFilteringService 
     * filters results of CPP query when user is not authorized to
     * see some content.
     *
     * Strategy: simply query the CPP1.xml demo CPP as authorized user
     * and verify that no filtering is done.
     */
    public void testNoFilteringForAuthorizedUser() throws Exception {      
        String id = "urn:freebxml:registry:sample:profile:cpp:instance:cpp1";
        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$id", id);
        queryParamsMap.put("$tableName", "ExtrinsicObject");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetSchemesByIdQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType, queryParamsMap);
        assertEquals("Did not find CPP1.xml", 1, res.size());
        
        ExtrinsicObjectType ro = (ExtrinsicObjectType)((JAXBElement<?>)res.get(0)).getValue();
        assertNotNull("RegistryObject should not be filtered for authothorized user", ro.getName());
        
        RepositoryItem ri = (RepositoryItem) context.getRepositoryItemsMap().get(id);
        String content = Utility.getInstance().unmarshalInputStreamToString(ri.getDataHandler().getInputStream());
        assertTrue("RepositoryItem should not be filtered for authorized user", (content.indexOf("tp:Comment") != -1));
    }
    
    /*
     * This test verifies that the CanonicalXMLFilteringService 
     * filters results of CPP query when user is not authorized to
     * see some content.
     *
     * Strategy: simply query the CPP1.xml demo CPP as RegistryGuest
     * and verify that name is filtered, decsription is masked and
     * tp:Comment in CPP is filtered.
     */
    public void testFilteringForUnauthorizedUser() throws Exception {      
        String id = "urn:freebxml:registry:sample:profile:cpp:instance:cpp1";
        
        HashMap<String, String> queryParamsMap = new HashMap<String, String>();        
        queryParamsMap.put("$id", id);
        queryParamsMap.put("$tableName", "ExtrinsicObject");
                
        ServerRequestContext context = new ServerRequestContext("QueryManagerImplTest:testGetSchemesByIdQueryPlugin", null);
        context.setUser(AuthenticationServiceImpl.getInstance().registryGuest);
        List<?> res = executeQuery(context, CanonicalConstants.CANONICAL_QUERY_FindObjectByIdAndType, queryParamsMap);
        assertEquals("Did not find CPP1.xml", 1, res.size());
        
        ExtrinsicObjectType ro = (ExtrinsicObjectType)((JAXBElement<?>)res.get(0)).getValue();
        assertNull("RegistryObject should be filtered for unauthothorized user", ro.getName());
        
        RepositoryItem ri = (RepositoryItem) context.getRepositoryItemsMap().get(id);
        String content = Utility.getInstance().unmarshalInputStreamToString(ri.getDataHandler().getInputStream());
        assertFalse("RepositoryItem should be filtered for unauthorized user", (content.indexOf("tp:Comment") == -1));        
    }
    

}

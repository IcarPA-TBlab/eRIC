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

package it.cnr.icar.eric.server.query.filter;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.io.File;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 * Test the Filter QUery feature.
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
public class FilterQueryProcessorTest extends ServerTest {
    
    RRFilterQueryProcessor qp = RRFilterQueryProcessor.getInstance();
    ResponseOptionType responseOption = null;
    org.oasis.ebxml.registry.bindings.query.ObjectFactory qof = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
    org.oasis.ebxml.registry.bindings.rim.ObjectFactory rof = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
    
    public FilterQueryProcessorTest(java.lang.String testName) {
        super(testName);
        
        responseOption =
		BindingUtility.getInstance().queryFac.createResponseOptionType();
		responseOption.setReturnComposedObjects(true);
		responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType.LEAF_CLASS);
    }
    
    //Test a RegistryObjectQuery.
    public void testRegistryObjectQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryObjectQuery",
                "filter/RegistryObjectQuery.xml");
    }

    //Test a RegistryObjectQuery -- with AND.
    public void testRegistryObjectQueryAND() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryObjectQueryAND",
                "filter/RegistryObjectQueryAND.xml");
    }
    
    //Test a RegistryObjectQuery -- with OR.
    public void testRegistryObjectQueryOR() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryObjectQueryOR",
                "filter/RegistryObjectQueryOR.xml");
    }
    
    // Test a ClassificationNodeQuery.
    public void testClassificationNodeQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testClassificationNodeQuery", 
                "filter/ClassificationNodeQuery.xml");
    }
    
     // Test a ClassificationSchemeQuery.
    public void testClassificationSchemeQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testClassificationSchemeQuery", 
                "filter/ClassificationSchemeQuery.xml");
    }
    
    // Test a AssociationQuery.
    public void testAssociationQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testAssociationQuery", 
                "filter/AssociationQuery.xml");
    }
    
    // Test a ClassificationQuery.
    public void testClassificationQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testClassificationQuery", 
            "filter/ClassificationQuery.xml");
    }
    
    // Test a ExternalIdentifierQuery.
    public void testExternalIdentifierQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testExternalIdentifierQuery", 
            "filter/ExternalIdentifierQuery.xml");
    }
    
    // Test a AuditableEventQuery.
    public void testAuditableEventQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testAuditableEventQuery", 
            "filter/AuditableEventQuery.xml");
    }
    
    // Test a PersonQuery.
    public void testPersonQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testPersonQuery", 
            "filter/PersonQuery.xml");
    }
    
    // Test a UserQuery.
    public void testUserQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testUserQuery", 
            "filter/UserQuery.xml");
    }
    
    // Test a ServiceQuery.
    public void testServiceQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testServiceQuery", 
            "filter/ServiceQuery.xml");
    }
    
    // Test a ServiceBindingQuery.
    public void testServiceBindingQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testServiceBindingQuery", 
            "filter/ServiceBindingQuery.xml");
    }    
    // Test a SpecificationLinkQuery.
    public void testSpecificationLinkQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testSpecificationLinkQuery", 
            "filter/SpecificationLinkQuery.xml");
    }
    
    // Test a RegistryQuery.
    public void testRegistryQuery() throws Exception {
        doQuery("FilterQueryProcessorTest.testRegistryQuery", 
            "filter/RegistryQuery.xml");
    }
    
    /**
     * Submits a FilterQuery from specified file.
     */
    private void doQuery(String contextId, String file) throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext(contextId, null);
            IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
            Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
            FilterQueryType query = (FilterQueryType)((JAXBElement<?>)unmarshaller.unmarshal(new File(file))).getValue();
            
            Marshaller marshaller = bu.getJAXBContext().createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
            marshaller.marshal(qof.createRegistryObjectQuery((RegistryObjectQueryType) query), System.err);
                        
            RegistryObjectListType rolt = qp.executeQuery(context, AuthenticationServiceImpl.getInstance().farrukh, query, responseOption, paramHolder);
            marshaller.marshal(rof.createRegistryObjectList(rolt), System.err);            
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
    }

    public static Test suite() {
        return new TestSuite(FilterQueryProcessorTest.class);
    }
}

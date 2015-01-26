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

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Query;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;


/**
 * jUnit Test for testing Filter Queries using JAXR API.
 *
 * @author Farrukh Najmi
 */
public class FilterQueryTest extends ClientTest {
    
    public FilterQueryTest(String testName) {
        super(testName);
    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(FilterQueryTest.class);
        return suite;
    }
    
    /*
     * Tests a RegistryObjectQuery.
     */
    public void testRegistryObjectQuery() throws Exception {
        //Read RegistryObjectQuery from a file and write it to a String
        Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
        //FilterQueryType fq = (FilterQueryType)unmarshaller.unmarshal(new File("RegistryObjectQuery.xml"));
        FilterQueryType fq = (FilterQueryType)JAXBIntrospector.getValue(unmarshaller.unmarshal(new File("RegistryObjectQuery.xml")));

        Marshaller marshaller = bu.getJAXBContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
        
        StringWriter sw = new StringWriter();
        org.oasis.ebxml.registry.bindings.query.ObjectFactory oF = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
        marshaller.marshal(oF.createRegistryObjectQuery((RegistryObjectQueryType) fq), sw);
        String queryStr = sw.toString();
        
        Query query = dqm.createQuery(Query.QUERY_TYPE_EBXML_FILTER_QUERY, queryStr);
        @SuppressWarnings("unused")
		BulkResponse resp = dqm.executeQuery(query);
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

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

package it.cnr.icar.eric.server.query;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.query.sql.SQLQueryProcessor;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;

/**
 *
 * @author najmi
 */
public class SQLQueryProcessorTest extends ServerTest {
    
    SQLQueryProcessor qp = SQLQueryProcessor.getInstance();
    ResponseOptionType responseOption = null;
    
    public SQLQueryProcessorTest(java.lang.String testName) {
        super(testName);
        
        responseOption =
		BindingUtility.getInstance().queryFac.createResponseOptionType();
		responseOption.setReturnComposedObjects(true);
		responseOption.setReturnType(org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType.LEAF_CLASS);
    }

    /*
     * Tests that SQLParser supports function calls
     * in SQLSelectCols
     */
    public void testFunctionInSelectCols() throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("SQLQueryProcessorTest.testFunctionInSelectCols", null);
            String query=" SELECT o1.* FROM ExtrinsicObject o1 where o1.lid = 'urn:freebxml:registry:VersioningTest:TestExtrinsicObject' AND o1.versionname IN ( SELECT MAX ( o2.versionname ) FROM ExtrinsicObject o2 WHERE o2.lid = o1.lid )";
            IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
            @SuppressWarnings("unused")
			RegistryObjectListType rolt = qp.executeQuery(context, AuthenticationServiceImpl.getInstance().farrukh, query, responseOption, paramHolder);
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
       
    }    
    
    /**
     * Test of submitAdhocQuery method, of class it.cnr.icar.eric.server.query.SQLQueryProcessorImpl.
     */
    public void testNullNameAndDesc() throws Exception {
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("SQLQueryProcessorTest.testNullNameAndDesc", null);
            String query=" SELECT ro.* from RegistryObject ro, Name_ nm, Description d WHERE (1=1)  AND (objecttype IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/RegistryPackage%\'))  AND (nm.parent = ro.id AND nm.value LIKE \'$name\' )  AND (d.parent = ro.id AND d.value LIKE \'$description\' )  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'$classificationPath1%\' ) ))  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'$classificationPath2%\' ) ))  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'$classificationPath3%\' ) ))  AND (ro.id IN ( SELECT classifiedObject FROM Classification WHERE classificationNode IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'classificationPath4%\' ) ))";
            //String query=" SELECT ro.* from RegistryObject ro, Name_ nm, Description d WHERE (1=1)  AND (objecttype IN (  SELECT id  FROM ClassificationNode WHERE path LIKE \'/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/RegistryPackage%\'))  AND (nm.parent = ro.id AND nm.value LIKE \'$name\' )";
            IterativeQueryParams paramHolder = new IterativeQueryParams(0, -1);
            @SuppressWarnings("unused")
			RegistryObjectListType rolt = qp.executeQuery(context, AuthenticationServiceImpl.getInstance().farrukh, query, responseOption, paramHolder);
            System.err.println();
        } catch (Exception e) {
            context.rollback();
            throw e;
        }
        context.commit();
       
    }
    
    
    
    public static Test suite() {
        return new TestSuite(SQLQueryProcessorTest.class);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    
}

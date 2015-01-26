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

package it.cnr.icar.eric.server.security.authorization;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.ctx.Status;

import it.cnr.icar.eric.server.common.ServerTest;

import java.util.ArrayList;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Diego Ballve / Digital Artefacts
 *
 */
public class HasSlotFunctionTest extends ServerTest {
    
    static String assId = null;
    static String eoId = null;
    
    public HasSlotFunctionTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return new TestSuite(HasSlotFunctionTest.class);
    }
        
    public void testEvaluate() throws Exception {
        HasSlotFunction function = new HasSlotFunction();
        
        //Must be kept in sync with misc/samples/demoDB/SubmitObjectsRequest_Organization.xml
        ArrayList<AnyURIAttribute> inputs = new ArrayList<AnyURIAttribute>();
        String parentId = "urn:freebxml:registry:demoDB:Sun";
        String name = "urn:freebxml:registry:demo:NASDAQ-Symbol";
        String value = "SUNW";
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance(name));
        inputs.add(AnyURIAttribute.getInstance(value));
        EvaluationCtx context = null;
        EvaluationResult result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("true"));
        
        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance(name));
        inputs.add(AnyURIAttribute.getInstance("junk"));
        result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("false"));

        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        inputs.add(AnyURIAttribute.getInstance("urn:junk"));
        result = function.evaluate(inputs, context);
        assertTrue(result.getAttributeValue().encode().equalsIgnoreCase("false"));
        
        inputs.clear();
        result = function.evaluate(inputs, context);
        assertTrue(result.getStatus().getCode().contains(Status.STATUS_MISSING_ATTRIBUTE));

        inputs.clear();
        inputs.add(AnyURIAttribute.getInstance(parentId));
        result = function.evaluate(inputs, context);
        assertTrue(result.getStatus().getCode().contains(Status.STATUS_MISSING_ATTRIBUTE));        
    }
}
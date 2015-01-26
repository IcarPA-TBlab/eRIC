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

package it.cnr.icar.eric.client.admin.function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import it.cnr.icar.eric.client.admin.AdminFunctionContext;
import it.cnr.icar.eric.client.admin.JAXRService;
import it.cnr.icar.eric.client.common.ClientTest;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


/**
 * Common code for JUnit tests for admin tool functions.
 *
 * @author Tony Graham
 */
public abstract class AbstractFunctionTest extends ClientTest {
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(AbstractFunctionTest.class.getName());
    protected AdminFunctionContext context;
    protected ByteArrayOutputStream testOut;

    public AbstractFunctionTest(String testName) {
        super(testName);
    }

    /** Creates and sets up an AdminFunctionContext for use by a test. */
    protected void setUp() throws Exception {
	super.setUp();
        context = AdminFunctionContext.getInstance();

        testOut = new ByteArrayOutputStream();
        context.setOutStream(new PrintStream(testOut));

        JAXRService service = new JAXRService();
        service.setAlias(getTestUserAlias());
        service.setKeyPass(getTestUserKeypass());
        service.connect();
        context.setService(service);
    }

    /** Returns the platform-specific line separator. */
    protected String newLine() {
        /* Be careful of the LineSeparator with println */
        String newLine = System.getProperty("line.separator");

        if (newLine == null) {
            newLine = "\n";
        }

        return newLine;
    }
}

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

package it.cnr.icar.eric.client.rest;

import it.cnr.icar.eric.client.common.ClientTest;

import java.net.URL;
//import java.net.URLConnection;
import java.net.HttpURLConnection;


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * jUnit Test to test RestServlet interface
 *
 * @author Paul Sterk
 */
public class RestTest extends ClientTest {
    public RestTest(String testName) {
	// Do not create a JAXR connection, unecessary for this test
        super(testName, false);
    }

    public static Test suite() throws Exception {
	TestSuite suite = null;
	if (localCallMode) {
	    // No equivalent for localCall in REST interface; do nothing if
	    // in that mode
	    suite = new TestSuite();
	} else {
	    suite = new TestSuite(RestTest.class);
	}
        return suite;
    }

    public void testQueryManagerGetRegistryObject() throws Exception {
        String restURL = regHttpUrl +
            "?interface=QueryManager" +
            "&method=getRegistryObject" +
            "&param-id=urn:oasis:names:tc:ebxml-regrep:acp:defaultACP";

        URL restUrl = new URL(restURL);
        HttpURLConnection urlConn =
	    (HttpURLConnection)restUrl.openConnection();
        urlConn.connect();

        int responseCode = urlConn.getResponseCode();
        assertTrue("Rest request failed.",
		   responseCode == HttpURLConnection.HTTP_OK);
    }

    public void testQueryManagerGetRepositoryItem() throws Exception {
        String restURL = regHttpUrl +
            "?interface=QueryManager" +
            "&method=getRepositoryItem" +
            "&param-id=urn:oasis:names:tc:ebxml-regrep:acp:defaultACP" +
            "&param-lid=urn:oasis:names:tc:ebxml-regrep:acp:defaultACP" +
            "&param-versionName=1.1";

        URL restUrl = new URL(restURL);
        HttpURLConnection urlConn =
	    (HttpURLConnection)restUrl.openConnection();
        urlConn.connect();

        int responseCode = urlConn.getResponseCode();
        assertTrue("Rest request failed.",
		   responseCode == HttpURLConnection.HTTP_OK);
    }
}

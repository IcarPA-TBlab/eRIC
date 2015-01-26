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

import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import java.io.StringWriter;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;

/**
 * @author Farrukh Najmi
 * @author Doug Bunting, Sun Microsystems
 * @version $Revision: 1.17 $
 */
public class QueryTest extends ServerTest {
    private static String xmlText = null;

    /**
     * Very simple Handler instance which fails immediately when any syntax
     * error or warning is encountered during validation of an XML instance
     * against our expected schemas.
     */
    protected static class Handler extends DefaultHandler {
        /**
	 * {@inheritDoc}
         * @param sAXParseException
         * @throws SAXException
         */
        public void error(SAXParseException sAXParseException)
	    throws SAXException {
	    System.err.println("Invalid response:\n" + xmlText);
            fail(sAXParseException.toString());
        }

        /**
	 * {@inheritDoc}
         * @param sAXParseException
         * @throws SAXException
         */
        public void fatalError(SAXParseException sAXParseException)
	    throws SAXException {
	    System.err.println("Invalid response:\n" + xmlText);
            fail(sAXParseException.toString());
        }

        /**
	 * {@inheritDoc}
         * @param sAXParseException
         * @throws SAXException
         */
        public void warning(SAXParseException sAXParseException)
	    throws SAXException {
	    System.err.println("Invalid response:\n" + xmlText);
            fail(sAXParseException.toString());
        }
    }

    /** Handler instance for use when performing XML Schema validation. */
    @SuppressWarnings("unused")
	private static Handler handler = new Handler();
    /** Marshaller used to extract XML content from returned Java trees. */
    private static Marshaller marshaller = null;
    /** JAXR query manager */
    private static QueryManager qm =
	QueryManagerFactory.getInstance().getQueryManager();
    /** JAXB Validator for response Java tree. */
 	//private static javax.xml.bind.Validator bindValidator = null;
    /** XML Schema validator */
    //private static javax.xml.validation.Validator xmlValidator = null;

    /**
     * Can we double-check validation?  Such validation works only if
     * ebxmlrr-spec tree is available.
     */
    @SuppressWarnings("unused")
	private boolean doXMLValidation = canUseEbxmlrrSpecHome;

    /**
     * What is the directory containing the XML Schema instances we need?
     * This directory and all derived sources are used only when
     * doXMLValidation is true.
     *
     * http://www.oasis-open.org/committees/regrep/documents/3.0/schema/
     * may be somewhat more reliable than ../ebxmlrr-spec/.. (because that
     * workspace may not have been downloaded) but does not contain schema
     * instances with the correct target namespace.
     */
    private String schemaLoc = ebxmlrrSpecHome + "/misc/3.0/schema/";

    /** schema for urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0 namespace */
    private StreamSource rimSource = new StreamSource(schemaLoc + "rim.xsd");
    /** schema for urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0 namespace */
    private StreamSource rsSource = new StreamSource(schemaLoc + "rs.xsd");
    /** schema for urn:oasis:names:tc:ebxml-regrep:xsd:query:3.0 namespace */
    private StreamSource querySource = new StreamSource(schemaLoc+"query.xsd");
    /** all of the schema instances we need */
    @SuppressWarnings("unused")
	private StreamSource sources[] = {rimSource, rsSource, querySource};

    /**
     * Constructor for XalanVersionTest.
     *
     * @param name
     */
    public QueryTest(String name) {
        super(name);
    }

    public void testSelectAdhocQuery() throws Exception {
        testSelectQuery("AdhocQuery");
    }

    public void testSelectAssociation() throws Exception {
        testSelectQuery("Association");
    }

    public void testSelectAuditableEvent() throws Exception {
        testSelectQuery("AuditableEvent");
    }

    public void testSelectClassification() throws Exception {
        testSelectQuery("Classification");
    }

    public void testSelectClassificationNode() throws Exception {
        testSelectQuery("ClassificationNode");
    }

    public void testSelectClassificationScheme() throws Exception {
        testSelectQuery("ClassificationScheme");
    }

    public void testSelectExternalIdentifier() throws Exception {
        testSelectQuery("ExternalIdentifier");
    }

    public void testSelectExternalLink() throws Exception {
        testSelectQuery("ExternalLink");
    }

    public void testSelectExtrinsicObject() throws Exception {
        testSelectQuery("ExtrinsicObject");
    }

    public void testSelectFederation() throws Exception {
        testSelectQuery("Federation");
    }

    public void testSelectOrganization() throws Exception {
        testSelectQuery("Organization");
    }

    public void testSelectRegistry() throws Exception {
        testSelectQuery("Registry");
    }

    // ?? SHould we comment this out as it takes an excessive amount of time to run
    public void testSelectRegistryObject() throws Exception {
        testSelectQuery("RegistryObject");
    }

    public void testSelectRegistryPackage() throws Exception {
        testSelectQuery("RegistryPackage");
    }

    public void testSelectService() throws Exception {
        testSelectQuery("Service");
    }

    public void testSelectServiceBinding() throws Exception {
        testSelectQuery("ServiceBinding");
    }

    public void testSelectSpecificationLink() throws Exception {
        testSelectQuery("SpecificationLink");
    }

    public void testSelectSubscription() throws Exception {
        testSelectQuery("Subscription");
    }

    public void testSelectUser() throws Exception {
        testSelectQuery("User");
    }

    private void testSelectQuery(String rimClass) throws Exception {
	// Get a Validator (and other private fields) if not already available
    	/*
	if (null == bindValidator) {
	    marshaller = bu.getJAXBContext().createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
				   Boolean.TRUE);

	    bindValidator = bu.getJAXBContext().createValidator();
	} */
	if (null == marshaller) {
	    marshaller = bu.getJAXBContext().createMarshaller();
	    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
				   Boolean.TRUE);
	}

        //System.err.println("Querying class: " + rimClass);
	rimClass = Utility.getInstance().mapTableName(rimClass);
        String sqlString = "SELECT * FROM " + rimClass;
        AdhocQueryRequest req = bu.createAdhocQueryRequest(sqlString);
        ServerRequestContext context =
	    new ServerRequestContext("QueryTest:testSelectQuery", req);
        context.setUser(ac.registryGuest);

        AdhocQueryResponse resp = qm.submitAdhocQuery(context);

        // Make sure that there is at least one object that matched the query
	int cnt = resp.getRegistryObjectList().getIdentifiable().size();
        assertTrue("Found 0 " + rimClass +
		   " objects to match the query. Expected at least one.",
		   0 < cnt);

	// Display count
	System.out.println(rimClass + ":\t" + cnt);

	// Get response content as a String
	StringWriter strWriter = new StringWriter();
	marshaller.marshal(resp, strWriter);
	xmlText = strWriter.toString();

	// Validate the response
	/*
	if (!bindValidator.validateRoot(resp)) {
	    System.err.println("Invalid response:\n" + xmlText);
	    fail("Validation failed for response");
	}
	*/
        /* Removed as it depends upon JDK 1.5
	if (doXMLValidation) {
	    // Get an XML Validator if not already available
	    if (null == xmlValidator) {
		SchemaFactory sf = SchemaFactory.
		    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		sf.setErrorHandler(handler);

		Schema respSchema = sf.newSchema(sources);
		xmlValidator = respSchema.newValidator();
		xmlValidator.setErrorHandler(handler);
	    }

	    // Confirm the response was valid (paranoia)
	    xmlValidator.validate(new StreamSource(new StringReader(xmlText)));
	}
         **/
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(QueryTest.class);
        //junit.framework.TestSuite suite= new junit.framework.TestSuite();
        //suite.addTest(new QueryTest("testSelectAssociation"));
        return suite;
        
    }
}

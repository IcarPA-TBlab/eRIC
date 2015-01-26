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

package it.cnr.icar.eric.common;

import java.net.URISyntaxException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author Farrukh S. Najmi
 */
public class URNTest extends ericTest {

    public URNTest(String testName) {
        super(testName);
    }

    protected void setUp() throws java.lang.Exception {
    }

    protected void tearDown() throws java.lang.Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(URNTest.class);
        //TestSuite suite = new TestSuite();
	//suite.addTest(new URNTest("testValidateValidURN"));
	//suite.addTest(new URNTest("testValidateInvalidURN"));
	//suite.addTest(new URNTest("testMakeValidURNIsURL"));
        return suite;
    }

    /**
     * Test a valid a URN that is > 256 char limit on ids. Starts with a letter, has '-' and numbers
     */
    public void testValidateValidURNLongerThanIdSize() throws java.lang.Exception {
        String spec = "urn:Org-1:acme:someresource111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN that starts with a number
     */
    public void testValidateValidURNStartsWithNumber() throws java.lang.Exception {
        String spec = "urn:1-Org-1:acme:someresource";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN where Namespace is max of 32 chars
     */
    public void testValidateValidURNNamespaceIsMaxed() throws java.lang.Exception {
        String spec = "urn:12345678901234567890123456789012:acme:someresource";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN where Namespace is min of 2 chars
     */
    public void testValidateValidURNNamespaceIsMin()
	throws java.lang.Exception {
        String spec = "urn:12:acme:someresource";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN where Namespace has '-' char
     */
    public void testValidateValidURNNamespaceHasDash() throws java.lang.Exception {
        String spec = "urn:www-acme-org:someresource";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN where Suffix has min of 1 char
     */
    public void testValidateValidURNSuffixIsMin() throws java.lang.Exception {
        String spec = "urn:12345678901234567890123456789012:1";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN where Suffix has all other allowed chars and "%" <hex> <hex>
     */
    public void testValidateValidURNSuffixHasOtherChars() throws java.lang.Exception {
        //Suffix is not swear words - honest.
        String spec = "urn:12345678901234567890123456789012:()+,-.:=@;$_!*'%01%23%45%67%89%aB%cD%eF";
        validateValidURN(spec);
    }

    /**
     * Test a valid a URN where Suffix has "'" char which is legal but giving some trouble being seen as legal.
     */
    public void testValidateValidURNSuffixHasSingleQuote() throws java.lang.Exception {
        String spec = "urn:12345678901234567890123456789012:asdf'";
        validateValidURN(spec);
    }

    /**
     * Test that invalid URNs is found to be invalid when a valid portion if found between invalid portions.
     */
    public void testValidateInvalidURNNamespaceHasValidWithinInvalid() throws java.lang.Exception {
        validateInvalidURN("__A__", "Allowed valid portion within invalid namespace: '__A__'");
    }

    /**
     * Test that invalid URNs is found to be invalid when suffix end with '\n'.
     */
    public void testValidateInvalidURNSuffixEndInNewline() throws java.lang.Exception {
        String urn = "urn:12345678901234567890123456789012:asdf\n";
        validateInvalidURN(urn, "Allowed suffix '" + urn + "' end w/ newline");
    }

    /**
     * Test that invalid URN is found to be invalid when suffix has an
     * embedded '\n'
     */
    public void testValidateInvalidURNSuffixEmbedsNewline() throws java.lang.Exception {
        String urn = "urn:acme:foo\nbar";
        validateInvalidURN(urn,
			   "Allowed suffix '" + urn + "' w/ embedded newline");
    }

    /**
     * Test that invalid URNs is found to be invalid when Namespace is null.
     */
    public void testValidateInvalidURNNamespaceIsNull() throws java.lang.Exception {
        validateInvalidURN("urn::acme:someresource", "Allowed null namespace");
    }

    /**
     * Test that invalid URNs is found to be invalid when Namespace > max of 32.
     */
    public void testValidateInvalidURNNamespaceTooBig() throws java.lang.Exception {
        validateInvalidURN("urn:123456789012345678901234567890123:acme:someresource", "Allowed namespace > 32");
    }

    /**
     * Test that invalid URN is found to be invalid when Namespace < min of
     * 2 characters.
     */
    public void testValidateInvalidURNNamespaceTooSmall()
	throws java.lang.Exception {
        validateInvalidURN("urn:1:acme:someresource", "Allowed namespace < 2");
    }

    /**
     * Test that invalid URNs is found to be invalid when Suffix is missing (null suffix).
     */
    public void testValidateInvalidURNSuffixIsNull() throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012", "Allowed null suffix");
    }

    /**
     * Test that invalid URNs is found to be invalid when Suffix of zero length
     */
    public void testValidateInvalidURNSuffixIsEmpty() throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:", "Allowed empty suffix");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has some
     * weird char not in other chars ( '{' ) at the start
     */
    public void testValidateInvalidURNSuffixStartIsInvalid()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:{b",
			   "Allowed invalid char at suffix start");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has some
     * weird char not in other chars ( '{' ) in its middle
     */
    public void testValidateInvalidURNSuffixMiddleIsInvalid()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a{b",
			   "Allowed invalid char in suffix");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has some
     * weird char not in other chars ( '{' ) at its end
     */
    public void testValidateInvalidURNSuffixEndIsInvalid()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a{",
			   "Allowed invalid char at suffix end");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has some
     * non <hex> char after '%' ( %G1 ) at the start
     */
    public void testValidateInvalidURNSuffixStartHasNonHexChar()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:%G1b",
			   "Allowed non-hex char after '%' at suffix start");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has some
     * non <hex> char after '%' ( %G1 ) in its middle
     */
    public void testValidateInvalidURNSuffixMiddleHasNonHexChar()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a%G1b",
			   "Allowed non-hex char after '%' in suffix");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has some
     * non <hex> char after '%' ( %G1 ) at its end
     */
    public void testValidateInvalidURNSuffixEndHasNonHexChar()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a%G1",
			   "Allowed non-hex char after '%' at suffix end");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has only
     * one char after '%' ( %1 ) at its end
     */
    public void testValidateInvalidURNSuffixHexEndsEarly()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a%1",
			   "Allowed just one char after '%' at suffix end");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has no
     * chars after '%' at its end
     */
    public void testValidateInvalidURNSuffixHexEmpty()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a%",
			   "Allowed zero chars after '%' at suffix end");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix contains
     * null char ( '\00' )
     */
    public void testValidateInvalidURNSuffixHasNullChar()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a\00b",
			   "Allowed null char in suffix");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix contains
     * encoded null char ( %00 )
     */
    public void testValidateInvalidURNSuffixHasNullHexChar()
	throws java.lang.Exception {
        validateInvalidURN("urn:12345678901234567890123456789012:a%00b",
			   "Temporarily expected failure: Allowed encoded " +
			   "null char in suffix");
    }

    /**
     * Test that invalid URN is found to be invalid when Suffix has
     * reserved chars, which are not intended to be used.
     */
    public void testValidateInvalidURNSuffixHasReservedChars()
	throws java.lang.Exception {
        validateInvalidURN("urn:acme:a" +
			   // 3 reserved characters ('%' handled elsewhere)
			   "/?#" +
			   "b",
			   "Allowed reserved chars in suffix");
    }

    /**
     * Test that invalid URN is made valid when Namespace is null.
     */
    public void testMakeValidInvalidURNNamespaceIsNull() throws java.lang.Exception {
        makeValidInvalidURNFixable("urn::acme:someresource", defaultNamespacePrefix+":acme:someresource");
    }

    /**
     * Test that invalid URN cannot be made valid when Namespace > max of 32. This should not be fixable
     */
    public void testMakeValidInvalidURNNamespaceTooBig() throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:123456789012345678901234567890123:acme:someresource", "Made valid namespace > 32");
    }

    /**
     * Test that invalid URN cannot be made valid when Namespace < min of
     * 2. This should not be fixable.
     */
    public void testMakeValidInvalidURNNamespaceTooSmall() throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:1:acme:someresource",
				      "Made valid namespace < 2");
    }

    /**
     * Test that invalid URL can be made valid after removing leading '-'
     * chars.
     */
    public void testMakeValidInvalidURLNamespaceDashes()
	throws java.lang.Exception {
	makeValidInvalidURNFixable("urn:---acme:someresource",
				   "urn:acme:someresource");
    }

    /**
     * Test that invalid URL cannot be made valid if removing leading '-'
     * chars leaves too few characters.
     */
    public void testMakeValidInvalidURLNamespaceDashesTooSmall()
	throws java.lang.Exception {
	makeValidInvalidURNNotFixable("urn:---a:someresource",
				      "Made valid namespace < 2 w/o dashes");
    }

    /**
     * Test that invalid URN is made valid when Suffix is missing (null suffix). This is treated as namespace missing.
     */
    public void testMakeValidInvalidURNSuffixIsNull() throws java.lang.Exception {
        makeValidInvalidURNFixable("urn:12345678901234567890123456789012", defaultNamespacePrefix+":12345678901234567890123456789012");
    }

    /**
     * Test that invalid URN cannot be made valid when Suffix does not have min of 1 char (0 chars). This is not fixable.
     */
    public void testMakeValidInvalidURNSuffixEmpty() throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:12345678901234567890123456789012:", "Made valid suffix of length 0");
    }

    /**
     * Test that invalid URN is made valid when Suffix has some weird char not in other chars ( '{' )
     */
    public void testMakeValidInvalidURNSuffixHasInvalidChar() throws java.lang.Exception {
        makeValidInvalidURNFixable("urn:12345678901234567890123456789012:a{", "urn:12345678901234567890123456789012:a_");
    }

    /**
     * Test that invalid URN is made valid when Suffix has some weird chars
     * not in other chars ( '{' and '}' ) as well as all other chars (which
     * should not be affected)
     */
    public void testMakeValidInvalidURNSuffixHasOtherChars()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("urn:acme:a{" +
				   // the other characters
				   "()+,.:=@;$_!*'-" +
				   "}b",
				   "urn:acme:a_" +
				   "()+,.:=@;$_!*'-" +
				   "_b");
    }

    /**
     * Test that invalid URN is made valid when Suffix has numerous weird
     * chars not in other chars (the excluded set)
     */
    public void testMakeValidInvalidURNSuffixHasExcludedChars()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("urn:acme:a" +
				   // 20 excluded characters, backslash a
				   // special case (mapped to colon)
				   "\00\01\02\20\40\"&<>[]^`{|}~\177\377\\" +
				   "b",
				   "urn:acme:a___________________:b");
    }

    /**
     * Test that invalid URN is made valid when Suffix has reserved
     * chars, which are not intended to be used.
     */
    public void testMakeValidInvalidURNSuffixHasReservedChars()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("urn:acme:a" +
				   // 3 reserved characters ('%' handled
				   // elsewhere), slash a special case
				   // (mapped to colon)
				   "/?#" +
				   "b",
				   "urn:acme:a:__b");
    }

    /**
     * Test that invalid URN cannot be made valid when Suffix has some non
     * <hex> char after '%' ( %G1 ). This is not currently fixable.
     * TODO: Suggest replacement of '%' character with '_'
     */
    public void testMakeValidInvalidURNSuffixHasNonHexChar() throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:12345678901234567890123456789012:a%G1", "Made valid suffix that has some non <hex> char after '%'  ( %G1 )");
    }

    /**
     * Test that invalid URN cannot be made valid when Suffix has only one
     * char after '%' ( %1 ) at its end. This is not currently fixable.
     * TODO: Suggest replacement of '%' character with '_'
     */
    public void testMakeValidInvalidURNSuffixHexEndsEarly()
	throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:acme:a%1",
				      "Made valid suffix that has just one " +
				      "char after '%' at suffix end");
    }

    /**
     * Test that invalid URN cannot be made valid when Suffix has no chars
     * after '%' at its end. This is not currently fixable.
     * TODO: Suggest replacement of '%' character with '_'
     */
    public void testMakeValidInvalidURNSuffixHexEmpty()
	throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:acme:a%",
				      "Made valid suffix that has zero " +
				      "chars after '%' at suffix end");
    }

    /**
     * Test that invalid URN cannot be made valid when Suffix has encoded
     * null char ( %00 ). This is not currently fixable.
     * TODO: Suggest replacement of '%' character with '_'
     */
    public void testMakeValidInvalidURNSuffixHasNullHexChar()
	throws java.lang.Exception {
        makeValidInvalidURNNotFixable("urn:acme:a%00b",
				      "Temporarily expected failure: Made " +
				      "\"valid\" suffix w/ null <hex> char");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical URL
     */
    public void testMakeValidInvalidURNTypicalHttpURL() throws java.lang.Exception {
        makeValidInvalidURNFixable("http://www.w3.org/2001/xml.xsd", "urn:www-w3-org:2001:xml.xsd");
    }


    /**
     * Test that invalid URN is made valid when URN is a typical file:/// URL
     */
    public void testMakeValidInvalidURNTypicalFileURL() throws java.lang.Exception {
        makeValidInvalidURNFixable("file:///tmp/xml.xsd", defaultNamespacePrefix+":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical absolute
     * path (with '/' prefix).
     * TODO: Should be distinguishable from relative path case.
     * suggest defaultNamespacePrefix + ":tmp:xml.xsd" is correct
     */
    public void testMakeValidInvalidURNAbsolutePath()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("/tmp/xml.xsd",
				   defaultNamespacePrefix+":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical absolute
     * path (with "c:\" prefix).
     * TODO: Should be distinguishable from relative path case.
     * suggest defaultNamespacePrefix + "c:tmp:xml.xsd" is correct
     */
    public void testMakeValidInvalidURNWindowsAbsolutePath()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("c:\\tmp\\xml.xsd",
				   defaultNamespacePrefix+":c:tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical relative
     * path.
     * TODO: Should be distinguishable from absolute path case.
     * suggest defaultNamespacePrefix + ".:tmp:xml.xsd"
     */
    public void testMakeValidInvalidURNRelativePathWithSlash()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("tmp/xml.xsd",
				   defaultNamespacePrefix + ":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical relative
     * path.
     * TODO: Should be distinguishable from absolute path case.
     * suggest defaultNamespacePrefix + ".:tmp:xml.xsd"
     */
    public void testMakeValidInvalidURNRelativePathWithBackSlash()
	throws java.lang.Exception {
        makeValidInvalidURNFixable("tmp/xml.xsd",
				   defaultNamespacePrefix + ":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical relative
     * path with './' prefix.
     * TODO: Should be distinguishable from absolute path case.
     * suggest defaultNamespacePrefix + ".:tmp:xml.xsd"
     */
    public void testMakeValidInvalidURNRelativePathWithDotSlash() throws java.lang.Exception {
        makeValidInvalidURNFixable("./tmp/xml.xsd", defaultNamespacePrefix+":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical relative
     * path with '.\' prefix
     * TODO: Should be distinguishable from absolute path case.
     * suggest defaultNamespacePrefix + ".:tmp:xml.xsd"
     */
    public void testMakeValidInvalidURNRelativePathWithDotBackSlash() throws java.lang.Exception {
        makeValidInvalidURNFixable(".\\tmp/xml.xsd", defaultNamespacePrefix+":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical relative
     * path with '../' prefix
     * TODO: Should be distinguishable from absolute path case.
     * suggest defaultNamespacePrefix + "..:tmp:xml.xsd"
     */
    public void testMakeValidInvalidURNRelativePathWithDotDotSlash() throws java.lang.Exception {
        makeValidInvalidURNFixable("../tmp/xml.xsd", defaultNamespacePrefix+":tmp:xml.xsd");
    }

    /**
     * Test that invalid URN is made valid when URN is a typical relative
     * path with '..\' prefix
     * TODO: Should be distinguishable from absolute path case.
     * suggest defaultNamespacePrefix + "..:tmp:xml.xsd"
     */
    public void testMakeValidInvalidURNRelativePathWithDotDotBackSlash() throws java.lang.Exception {
        makeValidInvalidURNFixable("..\\tmp/xml.xsd", defaultNamespacePrefix+":tmp:xml.xsd");
    }

    /**
     * Test that URN.makeValid uses a defaultNamespacePrefix when input contains no namespace prefix.
     */
    public void testMakeValidNoDefaultNamespace() throws java.lang.Exception {
        //Remember defaultNamespacePrefix, remove it for first part of test and later add it back or set default
        String defaultNamespacePrefix = CommonProperties.getInstance().getProperty("eric.common.URN.defaultNamespacePrefix");
        try {
            if (defaultNamespacePrefix != null) {
                CommonProperties.getInstance().remove("eric.common.URN.defaultNamespacePrefix");
            }
            URN urn = new URN("IRS Sector Schemas/CASE\\IRS-CaseCAC-1.0.xsd");
            try {
                urn.makeValid();
                fail("Did not generate exception when eric.common.URN.defaultNamespacePrefix=urn:org:acme was not set.");
            } catch (URISyntaxException e) {
                //expected
            }

            String tempPrefix = defaultNamespacePrefix;
            if (tempPrefix == null) {
                tempPrefix = "urn:org:acme";
            }

            //Set the defaultNamespacePrefix prop
            CommonProperties.getInstance().put("eric.common.URN.defaultNamespacePrefix", tempPrefix);
            urn.makeValid();
            String urnStr = urn.getURN();
            assertTrue(urnStr.endsWith("IRS_Sector_Schemas:CASE:IRS-CaseCAC-1.0.xsd"));
        } finally {
            //Reset defaultNamespacePrefix tooriginal value.
            CommonProperties.getInstance().remove("eric.common.URN.defaultNamespacePrefix");
            if (defaultNamespacePrefix != null) {
                CommonProperties.getInstance().put("eric.common.URN.defaultNamespacePrefix", defaultNamespacePrefix);
            }
        }
    }

    /**
     * Validates the URN specified by spec, calls makeValid() redundantlyand then
     * compares if makeValid altered a valid URL or not (it should not).
     */
    private void validateValidURN(String spec) throws java.lang.Exception {
        URN urn = new URN(spec);
        urn.validate();
        urn.makeValid();
        assertEquals(spec, urn.getURN());
    }

    /**
     * Test that various invalid URNs are found to be invalid
     */
    private void validateInvalidURN(String spec, String errorMessage) throws java.lang.Exception {
        try {
            URN urn = new URN(spec);
            urn.validate();
            fail(errorMessage);
        } catch (URISyntaxException e) {
            //Expected
        }
    }

    /**
     * Test that various invalid URNs are made valid and yield expected valid values.
     * Note that all URNs passed MUST be fixable by makeValid and that not all invalid URNs
     * can be fixed by makeValid.
     */
    private void makeValidInvalidURNFixable(String spec, String expectedFixedValue) throws java.lang.Exception {
        URN urn = new URN(spec);
        urn.makeValid();
        String validURN = urn.getURN();
        assertEquals(expectedFixedValue, validURN);
    }

    /**
     * Test that various invalid URNs are not (currently) made valid and
     * yield expected URISyntaxException.  Note that all URNs passed MUST
     * be un-fixable by makeValid() and therefore are expected to result in
     * a URISyntaxException.
     */
    private void makeValidInvalidURNNotFixable(String spec, String errorMessage) throws java.lang.Exception {
        try {
            URN urn = new URN(spec);
            urn.makeValid();
            fail(errorMessage);
        } catch (URISyntaxException e) {
            //Expected
        }
    }


}

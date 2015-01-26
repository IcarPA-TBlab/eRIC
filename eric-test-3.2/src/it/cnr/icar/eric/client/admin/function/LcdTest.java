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

import java.io.File;

import javax.swing.filechooser.FileSystemView;


import junit.framework.*;


/**
 * A JUnit TestCase to test the admin tool 'lcd' command
 *
 * @author Tony Graham
 */
public class LcdTest extends AbstractFunctionTest {
    
    public LcdTest(String testName) {
        super(testName);
    }
    
    public static Test suite() throws Exception {
        return new TestSuite(LcdTest.class);
    }

    // test methods 
    
    //
    public void testExecute_NoArg() throws Exception {
	String testString = null;

	Lcd lcd = new Lcd();

	lcd.execute(context,
		    testString);

        String expectedLocalDir =
	    FileSystemView.getFileSystemView().getDefaultDirectory().getCanonicalPath();

        assertEquals("Expected user's home directory",
		     expectedLocalDir,
		     context.getLocalDir().getCanonicalPath());
    }

    //
    public void testExecute_DirArg() throws Exception {
	String testString =
	    FileSystemView.getFileSystemView().getDefaultDirectory().getCanonicalPath();

	Lcd lcd = new Lcd();

	lcd.execute(context,
		    testString);

        String expectedLocalDir = testString;

        assertEquals("Expected user's home directory",
		     expectedLocalDir,
		     context.getLocalDir().getCanonicalPath());
    }

    //
    public void testExecute_FileArg() throws Exception {
	File tmpFile = File.createTempFile("LcdTest", null);

	tmpFile.deleteOnExit();

	String testString =
	    tmpFile.getCanonicalPath();

	Lcd lcd = new Lcd();

	boolean success;
	try {
	    lcd.execute(context,
			testString);
	    success = true;
	} catch (Exception e) {
	    success = false;
	    e.printStackTrace();
	}

        @SuppressWarnings("unused")
		String expectedLocalDir = testString;

	assertFalse("Lcd to file should fail.",
		    success);
    }

    public static void main(String[] args) {
        try {
            junit.textui.TestRunner.run(suite());
        } catch (Throwable t) {
            System.out.println("Throwable: " + t.getClass().getName()
			       + " Message: " + t.getMessage());
            t.printStackTrace();
        }
    }
}

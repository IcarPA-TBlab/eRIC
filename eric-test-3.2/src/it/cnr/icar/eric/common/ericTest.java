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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;


import junit.framework.TestCase;


/**
 * @author Farrukh Najmi
 *
 * Common base class for all tests in freebXML Registry.
 *
 */
public abstract class ericTest extends TestCase implements DemoDBConstants {
    
    protected BindingUtility bu = BindingUtility.getInstance();
    protected HashMap<String, String> dontVersionSlotsMap = new HashMap<String, String>();
    protected HashMap<String, String> dontVersionContentSlotsMap = new HashMap<String, String>();
    protected HashMap<String, String> dontCommitSlotsMap = new HashMap<String, String>();
    
    protected HashMap<String, String> forceRemoveRequestSlotsMap = new HashMap<String, String>();    

    protected static final boolean canUseEbxmlrrSpecHome =
	Boolean.valueOf(CommonProperties.getInstance().
			getProperty("can.use.ebxmlrr-spec",
				    "false")).booleanValue();
    protected static final String ebxmlrrSpecHome =
	CommonProperties.getInstance().
	getProperty("ebxmlrr-spec.home", "../ebxmlrr-spec");
    protected static final int testRepetitionsInner =
	Integer.parseInt(CommonProperties.getInstance().
			 getProperty("test.repetitions.inner", "1"));
    protected static final int testRepetitionsOuter =
	Integer.parseInt(CommonProperties.getInstance().
			 getProperty("test.repetitions.outer", "1"));
                
    protected static final String TMP_DIR = System.getProperty(
            "java.io.tmpdir");
    
    protected static final String OMAR_DEFAULT_NAMESPACE = "urn:freebxml:registry";
    protected static String defaultNamespacePrefix = CommonProperties.getInstance().getProperty("eric.common.URN.defaultNamespacePrefix");

    static {
        if (defaultNamespacePrefix == null) {
            defaultNamespacePrefix = OMAR_DEFAULT_NAMESPACE;
            CommonProperties.getInstance().put("eric.common.URN.defaultNamespacePrefix", defaultNamespacePrefix);
        }
    }

    /** Creates a new instance of OMARTest */
    @SuppressWarnings("static-access")
	public ericTest(String name) {
        super(name);
        dontVersionSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");        
        dontVersionContentSlotsMap.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");
        dontCommitSlotsMap.put(BindingUtility.getInstance().CANONICAL_SLOT_LCM_DO_NOT_COMMIT, "true");
        forceRemoveRequestSlotsMap.put(bu.CANONICAL_SLOT_DELETE_MODE_FORCE, "true");
    }
    
    public static File createTempFile(boolean deleteOnExit) throws IOException {
        return createTempFile(deleteOnExit, "aString");
    }
    
    public static File createTempFile(boolean deleteOnExit, String content) throws IOException {
        // Create temp file.
        File temp = File.createTempFile("eric", ".txt");
        
        // Delete temp file when program exits.
        if (deleteOnExit) {
            temp.deleteOnExit();
        }
        
        // Write to temp file
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(content);
        out.close();
        
        return temp;
    }
    
    /**
     * Reads bytes from InputStream until the end of the stream.
     *
     * @param in The InputStream to be read.
     * @return the read bytes
     * @thows Exception (IOEXception...)
     */
    public byte[] readBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        @SuppressWarnings("unused")
		InputStreamReader inr = new InputStreamReader(in);
        byte bbuf[] = new byte[1024];
        int read;
        while ((read = in.read(bbuf)) > 0) {
            baos.write(bbuf, 0, read);
        }
        return baos.toByteArray();
    }
}

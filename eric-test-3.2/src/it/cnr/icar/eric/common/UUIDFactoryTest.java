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

import junit.framework.TestCase;

/**
 * @author Tony Graham
 */
public class UUIDFactoryTest extends TestCase {

    /**
     * Constructor for UUIDFactoryTest.
     * @param name
     */
    public UUIDFactoryTest(String name) {
        super(name);
    }

    public void testIsValidUUID() {
		UUIDFactory uf = UUIDFactory.getInstance();

		try {
			UUIDFactory.getInstance().isValidUUID(null);
			fail("isValidUUID is not expected to handle null values.");
		} catch (Exception e) {}

        assertTrue(!uf.isValidUUID(""));
        assertTrue(!uf.isValidUUID("a"));
        assertTrue(uf.isValidUUID("57d925e0-7ad2-4dc3-ace1-b8a4064abcc7"));
        assertTrue(!uf.isValidUUID("57d925e0-7ad2-4dc3-ace1-b8a4064abcc7"
								   + "a"));
    }
    
}

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

import junit.framework.Test;
import junit.framework.TestSuite;


import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import java.util.Comparator;

/**
 * Tests <code>IdentifiableComparator</code>.
 */
public class IdentifiableComparatorTest extends ericTest {
    protected Comparator<Object> comparator;
    protected IdentifiableType object1;
    protected IdentifiableType object2;
    protected IdentifiableType object3;
    protected BindingUtility bu;
                
    /**
     * Constructor for IdentifiableComparatorTest.
     *
     * @param name
     */
	public IdentifiableComparatorTest(String name) {
        super(name);

	bu = BindingUtility.getInstance();

	comparator = new IdentifiableComparator();

	try {
	    object1 = bu.rimFac.createIdentifiableType();
	    object2 = bu.rimFac.createIdentifiableType();
	    object3 = bu.rimFac.createIdentifiableType();
	} catch (Exception e) {
	    fail("Couldn't create IdentifiableType objects.");
	}

        object1.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
        object2.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
        object3.setId(it.cnr.icar.eric.common.Utility.getInstance().createId());
    }
        
    public static Test suite() {
        return new TestSuite(IdentifiableComparatorTest.class);
    }

    public void testNonIdentifiable1() {
	try {
	    comparator.compare(this, object1);
	    fail("Comparator should throw exception on non-Identifiable input.");
	} catch (Exception e) {
	}
    }

    public void testNonIdentifiable2() {
	try {
	    comparator.compare(object1, this);
	    fail("Comparator should throw exception on non-Identifiable input.");
	} catch (Exception e) {
	}
    }

    public void testNullIdentifiable1() {
	try {
	    comparator.compare(null, object1);
	    fail("Comparator should throw exception on null input.");
	} catch (Exception e) {
	}
    }

    public void testNullIdentifiable2() {
	try {
	    comparator.compare(object1, null);
	    fail("Comparator should throw exception on null input.");
	} catch (Exception e) {
	}
    }

    public void testEqual() {
	assertEquals("Comparing the same service should return 0.",
		     0,
		     comparator.compare(object1,
					object1));
    }

    public void testNonEqual() {
	assertFalse("Comparing two different services should not return 0.",
		    comparator.compare(object1,
				       object2) == 0);
    }

    public void testOrdering() {
	boolean signOneWay = comparator.compare(object1,
						object2) < 0;
	boolean signOtherWay = comparator.compare(object2,
						  object1) < 0;

	assertTrue("Reversing order should give opposite sign.",
		   signOneWay == !signOtherWay);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }    
    
}

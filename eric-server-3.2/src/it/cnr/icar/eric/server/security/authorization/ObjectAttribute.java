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

import com.sun.xacml.attr.AttributeValue;

import org.w3c.dom.Node;

import java.net.URI;


/**
 * Representation of an java.lang.Object value.
 * All objects of this class are immutable and
 * all methods of the class are thread-safe.
 *
 * @author Farrukh Najmi
 */
public class ObjectAttribute extends AttributeValue {
    /**
     * Official name of this type
     */
    public static final String identifier = "http://www.w3.org/2001/XMLSchema#object"; //What should this be??

    /**
     * URI version of name for this type
     * <p>
     * This field is initialized by a static initializer so that
     * we can catch any exceptions thrown by URI(String) and
     * transform them into a RuntimeException, since this should
     * never happen but should be reported properly if it ever does.
     */
    private static URI identifierURI;

    /**
     * RuntimeException that wraps an Exception thrown during the
     * creation of identifierURI, null if none.
     */
    private static RuntimeException earlyException;

    /**
     * Static initializer that initializes the identifierURI
     * class field so that we can catch any exceptions thrown
     * by URI(String) and transform them into a RuntimeException.
     * Such exceptions should never happen but should be reported
     * properly if they ever do.
     */
    static {
        try {
            identifierURI = new URI(identifier);
        } catch (Exception e) {
            earlyException = new IllegalArgumentException();
            earlyException.initCause(e);
        }
    }

    /**
     * The actual Object value that this object represents.
     */
    private Object value;

    /**
     * Creates a new <code>ObjectAttribute</code> that represents
     * the Object value supplied.
     *
     * @param value the <code>Object</code> value to be represented
     */
    public ObjectAttribute(Object value) {
        super(identifierURI);

        // Shouldn't happen, but just in case...
        if (earlyException != null) {
            throw earlyException;
        }

        this.value = value;
    }

    /**
     * Returns a new <code>ObjectAttribute</code> that represents
     * a java.lang.Object.
     *
     * @param root the <code>Node</code> that contains the desired value
     * @return a new <code>ObjectAttribute</code> representing the
     *         appropriate value (null if there is a parsing error)
     */
    public static ObjectAttribute getInstance(Node root) {
        return getInstance(root.getFirstChild().getNodeValue());
    }

    /**
     * Returns a new <code>ObjectAttribute</code> that represents
     * the Object value indicated by the <code>Object</code> provided.
     *
     * @param value a Object representing the desired value
     * @return a new <code>ObjectAttribute</code> representing the
     *         appropriate value
     */
    public static ObjectAttribute getInstance(Object value) {
        return new ObjectAttribute(value);
    }

    /**
     * Returns the <code>Object</code> value represented by this object.
     *
     * @return the <code>Object</code> value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns true if the input is an instance of this class and if its
     * value equals the value contained in this class.
     *
     * @param o the object to compare
     *
     * @return true if this object and the input represent the same value
     */
    public boolean equals(Object o) {
        if (!(o instanceof ObjectAttribute)) {
            return false;
        }

        ObjectAttribute other = (ObjectAttribute) o;

        return value.equals(other.value);
    }

    /**
     * Returns the hashcode value used to index and compare this object with
     * others of the same type. Typically this is the hashcode of the backing
     * data object.
     *
     * @return the object's hashcode value
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Converts to a String representation.
     *
     * @return the String representation
     */
    public String toString() {
        return "ObjectAttribute: \"" + value + "\"";
    }

    /**
     *
     */
    public String encode() {
        return value.toString();
    }
}

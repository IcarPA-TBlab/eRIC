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

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 *
 * Example use case:
 * <pre>
 *  public int hashCode(){
 *    int result = HashCodeUtil.SEED;
 *    //collect the contributions of various fields
 *    result = HashCodeUtil.hash(result, fPrimitive);
 *    result = HashCodeUtil.hash(result, fObject);
 *    result = HashCodeUtil.hash(result, fArray);
 *    return result;
 *  }
 * </pre>
 *
 * Copied from: http://www.javapractices.com/Topic28.cjp
 *
 * @author Josh Bloch
 *
 */
public final class HashCodeUtil {
    
    /**
     * An initial value for a <code>hashCode</code>, to which is added contributions
     * from fields. Using a non-zero value decreases collisons of <code>hashCode</code>
     * values.
     */
    public static final int SEED = 23;
    
    /**
     * booleans.
     */
    public static int hash( int aSeed, boolean aBoolean ) {
        return firstTerm( aSeed ) + ( aBoolean ? 1 : 0 );
    }
    
    /**
     * chars.
     */
    public static int hash( int aSeed, char aChar ) {
        return firstTerm( aSeed ) + (int)aChar;
    }
    
    /**
     * ints.
     */
    public static int hash( int aSeed , int aInt ) {
    /*
     * Implementation Note
     * Note that byte and short are handled by this method, through
     * implicit conversion.
     */
        return firstTerm( aSeed ) + aInt;
    }
    
    /**
     * longs.
     */
    public static int hash( int aSeed , long aLong ) {
        return firstTerm(aSeed)  + (int)( aLong ^ (aLong >>> 32) );
    }
    
    /**
     * floats.
     */
    public static int hash( int aSeed , float aFloat ) {
        return hash( aSeed, Float.floatToIntBits(aFloat) );
    }
    
    /**
     * doubles.
     */
    public static int hash( int aSeed , double aDouble ) {
        return hash( aSeed, Double.doubleToLongBits(aDouble) );
    }
    
    /**
     * <code>aObject</code> is a possibly-null object field, and possibly an array.
     *
     * If <code>aObject</code> is an array, then each element may be a primitive
     * or a possibly-null object.
     */
    public static int hash( int aSeed , Object aObject ) {
        int result = aSeed;
        if ( aObject == null) {
            result = hash(result, 0);
        }
        else if ( ! isArray(aObject) ) {
            result = hash(result, aObject.hashCode());
        }
        else {
            int length = Array.getLength(aObject);
            for ( int idx = 0; idx < length; ++idx ) {
                Object item = Array.get(aObject, idx);
                //recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }
    
    
    /// PRIVATE ///
    private static final int fODD_PRIME_NUMBER = 37;
    
    private static int firstTerm( int aSeed ){
        return fODD_PRIME_NUMBER * aSeed;
    }
    
    private static boolean isArray(Object aObject){
        return aObject.getClass().isArray();
    }
}

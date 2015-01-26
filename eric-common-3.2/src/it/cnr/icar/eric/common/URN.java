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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a URN as defined by RFC 2141.
 * Based upon http://www.ietf.org/rfc/rfc2141.txt
 *
 * @author Farrukh S. Najmi
 */
public class URN {
    
    /** Regular expression to validate RFC 2141 URN syntax. */
    public final static String REGEXP_URN =
        "^urn:[a-zA-Z0-9][a-zA-Z0-9-]{1,31}:([a-zA-Z0-9()+,.:=@;$_!*'-]|%[0-9A-Fa-f]{2})+$";
    public final static Pattern PATTERN_URN = Pattern.compile(URN.REGEXP_URN);
    
    private String urn;
    private String namespace;
    private String suffix;
    
    /** Creates a new instance of URN */
    public URN(String spec)  {
	urn = spec;
    }
    
    /**
     * Performs validation of the URN according to RFC 2141.
     */
    public void validate() throws URISyntaxException {
        try {
            Matcher urnMatcher = PATTERN_URN.matcher(urn);
            if (!urnMatcher.matches()) {
                throw new URISyntaxException(urn, 
                        CommonResourceBundle.getInstance().getString("message.error.URISyntaxException"));                
            }
                        
        } catch (URISyntaxException e) {
            throw e;
        } catch(Exception  nse) {
            URISyntaxException e = new URISyntaxException(urn, 
                    CommonResourceBundle.getInstance().getString("message.error.exceptionDuringUrnValidation"));
            e.initCause(nse);
            throw e;
        }        
    }
    
    /**
     * Modifies a potentially invalid URN by replacing any invalid characters with '_' to make it a valid URN.
     */
    public void makeValid() throws URISyntaxException {
        try {
            validate();
            //No need to validate as it is already valid.
            return;
        } catch (Exception ex) {
            //Need to continue to make valid.

            try {            
                //Get namespace
                parseURN();
                if ((namespace == null) || (namespace.length() == 0)) {
                    String defaultNamespacePrefix = CommonProperties.getInstance().getProperty("eric.common.URN.defaultNamespacePrefix");
                    if (defaultNamespacePrefix == null) {
                        throw new URISyntaxException(urn,                                
                            CommonResourceBundle.getInstance().getString("message.error.noDefaultUrnPrefix"));
                    }
                    urn = defaultNamespacePrefix + ":" + suffix;
                    parseURN();
                }
                
                //Replace invalid chars with '-'
                namespace = namespace.replaceAll("[^A-Za-z0-9-]", "-");
                
                while ((namespace.length() >= 1) && (namespace.charAt(0) == '-')) {
                    //Skip first char if it is '-'
                    namespace = namespace.substring(1);
                }
                
                //Replace '/' and '\' chars with ':'
                suffix = suffix.replace('/', ':');
                suffix = suffix.replace('\\', ':');
                
                //Replace invalid chars with '_'
                suffix = suffix.replaceAll("[^a-zA-Z0-9()+,.:=@;$_!*'%-]", "_");
                
                urn = "urn:" + namespace + ":" + suffix;
                
                //Remove any repeated ':' chars
                urn = urn.replaceAll("[:]{2,}", ":");
                            
                //Now validate to double check makeValid's output
                validate();
            } catch(NoSuchElementException  nse) {
                throw new URISyntaxException(urn,
                    CommonResourceBundle.getInstance().getString("message.error.incompleteURN"));
            } catch (Exception e) {
                URISyntaxException e1 = new
    		URISyntaxException(urn,
    				   // is reason necessary in this case?
    				   // alternatively, should we special-case
    				   // a thrown URISyntaxException?
    				   CommonResourceBundle.getInstance().
    				   getString("message.error." +
    					     "exceptionDuringUrnValidation"));
                e1.initCause(e);
                throw e1;
            }         
        }
    }
    
    private void parseURN() throws URISyntaxException {
        @SuppressWarnings("unused")
		int namespace_index = 0;
        String delimeter = "[:]";
        
        String nameSpaceAndSuffix = new String(urn);
        
        if (urn.startsWith("urn:")) {
            nameSpaceAndSuffix = urn.substring(4);
        } else if (urn.startsWith("http://")) {
            nameSpaceAndSuffix = urn.substring(7);
            delimeter = "[/\\\\]";
        } else if (urn.startsWith("./")) {
            nameSpaceAndSuffix = urn.substring(1);
            delimeter = "[/\\\\]";
        } else if (urn.startsWith(".\\")) {
            nameSpaceAndSuffix = urn.substring(1);
            delimeter = "[/\\\\]";
        } else if (urn.startsWith("../")) {
            nameSpaceAndSuffix = urn.substring(2);
            delimeter = "[/\\\\]";
        } else if (urn.startsWith("..\\")) {
            nameSpaceAndSuffix = urn.substring(2);
            delimeter = "[/\\\\]";
        } else {
            try {
                URL url = new URL(urn);
                namespace = null;
                suffix = url.getPath();
                return;
            } catch (MalformedURLException e) {
                namespace = null;
                suffix = new String(urn);
                return;
            }
        }
        
        String[] components = nameSpaceAndSuffix.split(delimeter, 2);
        if (components.length == 0) {
            namespace = null;
            suffix = null;            
        } else if (components.length == 1) {
            namespace = null;
            suffix = nameSpaceAndSuffix;
        } else if (components.length == 2) {
            namespace = components[0];
            suffix = components[1];
        }
    }
        
    public String getURN() {
        return urn;
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public String getSuffix() {
        return suffix;
    }
    
    public String toExternalForm() {
        return urn;
    }
    
    public boolean equals(Object o) {
        if (o instanceof URN) {
        	URN otherURN = (URN)o;
            return (otherURN.namespace.equals(namespace) &&
                    otherURN.suffix.equals(suffix));
        }
        else
            return false;
    }
    
    public String toString() {
        return toExternalForm();
    }
    
    
}

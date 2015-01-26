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
package it.cnr.icar.eric.client.xml.registry.util;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.User;



/**
 * Wrapper around {@link javax.xml.registry.infomodel.User}. This allows
 * us to check the validity of the input. Usually one would do that in the
 * model itself (i.e. class User), but an extra layer inbetween gives us
 * more flexibility.
 *
 * @author Fabian Ritzmann
 */
public class UserRegistrationInfo {
    private final User user;
    private String alias = null;
    private char[] keyPassword = null;
    private char[] storePassword = null;
    private String p12File = null; //File in PKCS12 format to import from / export to the cert
    private boolean caIssuedCert = false;
    private String organization = null;
    private String organizationUnit = null;

    
    /**
     * @param u Underlying User implementation
     */
    public UserRegistrationInfo(User u) throws JAXRException {
        this.user = u;

        // hard coded for now:
        this.storePassword = ProviderProperties.getInstance()
                                               .getProperty("jaxr-ebxml.security.storepass")
                                               .toCharArray();
    }

    public User getUser() {
        return this.user;
    }

    /**
     * Method setAlias.
     * @param text
     */
    public void setAlias(String text) {
        this.alias = text.trim();
        if (this.p12File == null) {
            this.p12File = System.getProperty("java.io.tmpdir", ".") + "/" + alias + ".p12";
        }
    }

    /**
     * Method setKeyPassword.
     * @param text
     */
    public void setKeyPassword(char[] text) {
        // Don't trim text here. It's not a good idea to use whitespace
        // in a password, but if somebody does, it shouldn't be changed.
        this.keyPassword = text;
    }

    public void setStorePassword(char[] text) {
        this.storePassword = text;
    }
    
    /**
     * Sets the PKCS12 file to import / export a cert from.
     */
    public void setP12File(String text) {
        this.p12File = text.trim();
    }

    /**
     * Sets the Organization the user belongs to
     */
    public void setOrganization(String organization) {
        this.organization = organization.trim();
    }

    /**
     * Sets the Organization Unit the user belongs to
     */
    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit.trim();
    }

    /**
     * Returns tru if this is a CA issued cert, false if registry issued cert.
     */
    public void setCAIssuedCert(boolean caIssuedCert) {
        this.caIssuedCert = caIssuedCert;
    }        

    /**
     * Method getAlias.
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * Method getKeyPassword.
     */
    public char[] getKeyPassword() {
        return this.keyPassword;
    }

    /**
     * Method getStorePassword.
     */
    public char[] getStorePassword() {
        return this.storePassword;
    }

    /**
     * Gets the PKCS12 file to import / export a cert from.
     */
    public String getP12File() {
        return this.p12File;
    }    

    /**
     * Gets the Organization user belongs to
     */
    public String getOrganization() {
        return this.organization;
    }    

    /**
     * Gets the Organization Unit user belongs to
     */
    public String getOrganizationUnit() {
        return this.organizationUnit;
    }    

    /**
     * Returns tru if this is a CA issued cert, false if registry issued cert.
     */
    public boolean isCAIssuedCert() {
        return this.caIssuedCert;
    }        
}

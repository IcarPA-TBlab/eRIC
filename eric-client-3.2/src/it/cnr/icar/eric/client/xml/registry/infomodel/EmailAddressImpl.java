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
package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.common.BindingUtility;


import org.oasis.ebxml.registry.bindings.rim.EmailAddressType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.EmailAddress;


/**
 * Implements JAXR API interface named EmailAddress.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class EmailAddressImpl implements EmailAddress {
    @SuppressWarnings("unused")
	private BindingUtility bu = BindingUtility.getInstance();
    
    private String address = null;
    private String type = null;

    @SuppressWarnings("unused")
	private EmailAddressImpl() {
    }

    public EmailAddressImpl(LifeCycleManagerImpl lcm) {
    }

    public EmailAddressImpl(LifeCycleManagerImpl lcm, EmailAddressType ebEmail)
        throws JAXRException {
        address = ebEmail.getAddress();
        type = ebEmail.getType();
    }

    public String getAddress() throws JAXRException {
        return address;
    }

    public void setAddress(String par1) throws JAXRException {
        address = par1;
    }

    public String getType() throws JAXRException {
        return type;
    }

    public void setType(String par1) throws JAXRException {
        type = par1;
    }

    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        EmailAddressType ebEmailAddressType = factory.createEmailAddressType(); 
		setBindingObject(ebEmailAddressType);
		
//		JAXBElement<EmailAddressType> ebEmailAddress = factory.createEmailAddress(ebEmailAddressType);
		
		return ebEmailAddressType;
    }

    protected void setBindingObject(EmailAddressType ebEmailAddressType)
        throws JAXRException {
//        super.setBindingObject(ebEmailAddressType);

        ebEmailAddressType.setType(getType());
        ebEmailAddressType.setAddress(getAddress());
    }
}

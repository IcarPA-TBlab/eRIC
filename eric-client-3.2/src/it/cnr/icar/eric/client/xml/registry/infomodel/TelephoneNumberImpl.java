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


import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.TelephoneNumber;


/**
 * Implements JAXR API interface named TelephoneNumber.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class TelephoneNumberImpl implements TelephoneNumber {
    private String countryCode = null;
    private String areaCode = null;
    private String number = null;
    private String extension = null;
    private String url = null;
    private String type = null;
    @SuppressWarnings("unused")
	private LifeCycleManagerImpl lcm = null;

    //not accessable
    @SuppressWarnings("unused")
	private TelephoneNumberImpl() {
    }

    public TelephoneNumberImpl(LifeCycleManagerImpl lcm) {
        this.lcm = lcm;
    }

    public TelephoneNumberImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType tel) {
        this.lcm = lcm;
        areaCode = tel.getAreaCode();
        countryCode = tel.getCountryCode();
        extension = tel.getExtension();
        number = tel.getNumber();
        type = tel.getPhoneType();
    }

    public String getCountryCode() throws JAXRException {
        return countryCode;
    }

    public String getAreaCode() throws JAXRException {
        return areaCode;
    }

    public String getNumber() throws JAXRException {
        return number;
    }

    public String getExtension() throws JAXRException {
        return extension;
    }

    //TODO: Remove from JAXR 2.0
    public String getUrl() throws JAXRException {
        return url;
    }

    public String getType() throws JAXRException {
        return type;
    }

    public void setCountryCode(String par1) throws JAXRException {
        countryCode = par1;
    }

    public void setAreaCode(String par1) throws JAXRException {
        areaCode = par1;
    }

    public void setNumber(String par1) throws JAXRException {
        number = par1;
    }

    public void setExtension(String par1) throws JAXRException {
        extension = par1;
    }

    //TODO: Remove from JAXR 2.0
    public void setUrl(String par1) throws JAXRException {
        url = par1;
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

        TelephoneNumberType ebTelephoneNumberType = factory.createTelephoneNumberType(); 
		setBindingObject(ebTelephoneNumberType);
		
//		JAXBElement<TelephoneNumberType> ebTelephoneNumber = factory.createTelephoneNumber(ebTelephoneNumberType);
		
		return ebTelephoneNumberType;

    }

    protected void setBindingObject(TelephoneNumberType ebTelephoneNumberType)
        throws JAXRException {
        ebTelephoneNumberType.setCountryCode(getCountryCode());
        ebTelephoneNumberType.setAreaCode(getAreaCode());
        ebTelephoneNumberType.setNumber(getNumber());
        ebTelephoneNumberType.setExtension(getExtension());
        ebTelephoneNumberType.setPhoneType(getType());
    }

    public String toString() {
        String str = "";

        try {
            int registryLevel = 1; //??Get from RegistryService later

            if (registryLevel == 0) {
                str = getNumber();
            } else {
                if (getCountryCode() != null) {
                    str += ("(" + getCountryCode() + ") ");
                }

                if (getAreaCode() != null) {
                    str += (getAreaCode() + "-");
                }

                if (getNumber() != null) {
                    str += getNumber();
                }

                if (getType() != null) {
                    str += (" (" + getType() + ")");
                }
            }
        } catch (JAXRException e) {
            e.printStackTrace();
        }

        return str;
    }
}

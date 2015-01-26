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

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.PersonName;

import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;


/**
 * Implements JAXR API interface named PersonName.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class PersonNameImpl implements PersonName {
    private String lastName = null;
    private String middleName = null;
    private String firstName = null;

    // This required attribute is used to communicate to the parent PersonImpl
    // that a change to firstName, middleName or lastName has occurred. This
    // ensures that the InternationalString returned by this class is the same
    // as the parent RegistryObject.Name value.
    private PersonImpl personImpl = null;

    public PersonNameImpl(LifeCycleManagerImpl lcm) {
    }

    // 	private String fullName = null;     // ?? Spec issue w/ format conversion
    public PersonNameImpl(LifeCycleManagerImpl lcm,
                          PersonImpl personImpl) {
        this(lcm);
        this.personImpl = personImpl;
    }

    public PersonNameImpl(LifeCycleManagerImpl lcm,
                          PersonImpl personImpl,
                          PersonNameType ebPersonName) {
        this(lcm, personImpl);
        firstName = ebPersonName.getFirstName();
        middleName = ebPersonName.getMiddleName();
        lastName = ebPersonName.getLastName();
    }

    /**
     * This method is called by the PersonImpl class in its setPersonName method
     * There should be no need to call this method directly.
     */
    protected void setPersonImpl(PersonImpl personImpl) throws InvalidRequestException{
        this.personImpl = personImpl;
    }

    public PersonImpl getPersonImpl() {
        return personImpl;
    }

    public String getLastName() throws JAXRException {
        if (lastName == null) {
            lastName = "";
        }

        return lastName;
    }

    public void setLastName(String par1) throws JAXRException {
        if (lastName == null || !lastName.equals(par1)) {
            lastName = par1;
            if (personImpl != null) {
                personImpl.setNameInternal();
            }
        }
    }

    public String getFirstName() throws JAXRException {
        if (firstName == null) {
            firstName = "";
        }

        return firstName;
    }

    public void setFirstName(String par1) throws JAXRException {
        if (firstName == null || !firstName.equals(par1)) {
            firstName = par1;
            if (personImpl != null) {
                personImpl.setNameInternal();
            }
        }
    }

    public String getMiddleName() throws JAXRException {
        if (middleName == null) {
            middleName = "";
        }

        return middleName;
    }

    public void setMiddleName(String par1) throws JAXRException {
        if (middleName == null || !middleName.equals(par1)) {
            middleName = par1;
            if (personImpl != null) {
                personImpl.setNameInternal();
            }
        }
    }

    /*
     * This method returns a formatted Person's name:
     * LastName, FirstName MiddleName.  Other variants include:
     * LastName, FirstName
     * LastName, MiddleName
     * FirstName MiddleName
     * LastName | FirstName | MiddleName
     *
     * This form should also be acceptable in Asian locales where just the 
     * first and last names are typically needed.
     */
    public String getFormattedName() throws JAXRException {
        StringBuffer fullName = new StringBuffer();

        if (getLastName() != null && getLastName().length() > 0) {
            fullName.append(lastName);
            if ((getFirstName() != null && getFirstName().length() > 0) ||
                (getMiddleName() != null && getMiddleName().length() > 0)) {
                fullName.append(", ");
            }
        }
        if (getFirstName() != null && getFirstName().length() > 0) {
            fullName.append(firstName);
            if (getMiddleName() != null && getMiddleName().length() > 0) {
                fullName.append(' ');
            }
        }
        if (getMiddleName() != null && getMiddleName().length() > 0) {
            fullName.append(middleName);
        }

        return fullName.toString().trim();
    }

    public String getFullName() throws JAXRException {
        //Do not attempt formatting by combining first, middle, lastName as that has
        //isues with getFullName not matching due to extra spaces etc.
        //and causes failures in JAXR TCK
        return getLastName();
    }
    
    public void setFullName(String fullName) throws JAXRException {
        //Do not attempt formatting by parsing fullName as that has
        //isues with getFullName not matching due to extra spaces etc.
        //and causes failures in JAXR TCK
        setLastName(fullName);
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

        PersonNameType ebPersonNameType = factory.createPersonNameType(); 
		setBindingObject(ebPersonNameType);
		
//		JAXBElement<PersonNameType> ebPersonName = factory.createPersonName(ebPersonNameType);
		
		return ebPersonNameType;
    }

    protected void setBindingObject(PersonNameType ebPersonNameType)
        throws JAXRException {
//        super.setBindingObject(ebPersonNameType);

        ebPersonNameType.setFirstName(getFirstName());
        ebPersonNameType.setMiddleName(getMiddleName());
        ebPersonNameType.setLastName(getLastName());
    }
    
    public String toString() {
        String str = super.toString();

        try {
            str += " lastName:" + getLastName() + " formattedName: " + getFormattedName();
        } catch (JAXRException e) {
        }

        return str;
    }
}

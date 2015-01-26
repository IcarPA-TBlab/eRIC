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

import java.util.Date;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.Versionable;

import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 * Implements JAXR API interface named RegistryEntry.
 * The RegistryEntry interface will likely be removed from JAXR 2.0??
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class RegistryEntryImpl extends RegistryObjectImpl
    implements RegistryEntry, Versionable {
    private int stability = RegistryEntry.STABILITY_DYNAMIC;
    private Date expiration = null;

    public RegistryEntryImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public RegistryEntryImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.RegistryObjectType ebObject)
        throws JAXRException {
        super(lcm, ebObject);

        /*
        Calendar cal = ebObject.getExpiration();

        if (cal != null) {
            expiration = new Date(cal.getTimeInMillis());
        }
         */
    }

    public int getStability() throws JAXRException {
        return stability;
    }

    public void setStability(int stability) throws JAXRException {
        this.stability = stability;
        setModified(true);
    }

    public Date getExpiration() throws JAXRException {
        return expiration;
    }

    public void setExpiration(Date par1) throws JAXRException {
        expiration = par1;
        setModified(true);
    }

    public int getMajorVersion() throws JAXRException {
        return 1;
    }

    public void setMajorVersion(int par1) throws JAXRException {
    }

    public int getMinorVersion() throws JAXRException {
        return 1;
    }

    public void setMinorVersion(int par1) throws JAXRException {
    }

    public String getUserVersion() throws JAXRException {
        //userInfo is obsolete but can be mapped to RegistryObject.comment
        String comment=null;
        VersionInfoType versionInfo = getVersionInfo();
        if (versionInfo != null) {
            comment = versionInfo.getComment();
        }
        return comment;
    }

    public void setUserVersion(String comment) throws JAXRException {
        VersionInfoType versionInfo = getVersionInfo();
		if (versionInfo == null) {
		    versionInfo = BindingUtility.getInstance().rimFac.createVersionInfoType();
		}

		versionInfo.setComment(comment);
		setVersionInfo(versionInfo);

    }
}

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
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.HashCodeUtil;

import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;

/**
 * Base class for all classes that have id and home attributes. TODO: Add to
 * JAXR 2.0 as a new interface
 * 
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class IdentifiableImpl extends ExtensibleObjectImpl implements Comparable<Object> {
	private static final Log log = LogFactory.getLog(IdentifiableImpl.class);

	protected Key key = null;
	protected String home = null;

	IdentifiableImpl(LifeCycleManagerImpl lcm) throws JAXRException {
		super(lcm);

		// Assign default key
		key = lcm.createKey();
	}

	IdentifiableImpl(LifeCycleManagerImpl lcm, IdentifiableType ebObject) throws JAXRException {
		// Pass ebObject to superclass so slot-s can be initialized
		super(lcm, ebObject);

		key = new KeyImpl(lcm);
		key.setId(ebObject.getId());
		home = ebObject.getHome();
	}

	public Key getKey() throws JAXRException {
		return key;
	}

	/**
	 * Add to JAXR 2.0??
	 */
	public String getId() throws JAXRException {
		return key.getId();
	}

	public void setKey(Key key) throws JAXRException {
		this.key = key;
		setModified(true);
	}

	/**
	 * Add to JAXR 2.0??
	 */
	public String getHome() throws JAXRException {
		return home;
	}

	/**
	 * Add to JAXR 2.0??
	 */
	public void setHome(String home) throws JAXRException {
		this.home = home;
	}

	public String toXML() throws JAXRException {
		try {
			StringWriter sw = new StringWriter();
			Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
			marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			JAXBElement<IdentifiableType> ebIdentifiable = bu.rimFac
					.createIdentifiable((IdentifiableType) toBindingObject());
			marshaller.marshal(ebIdentifiable, sw);

			return sw.toString();
		} catch (javax.xml.bind.JAXBException e) {
			throw new JAXRException(e);
		}
	}

	/**
	 * This method takes this JAXR infomodel object and returns an equivalent
	 * binding object for it. Note it does the reverse of one of the
	 * constructors above.
	 */
	abstract public Object toBindingObject() throws JAXRException;

	protected void setBindingObject(IdentifiableType ebIdentifiableType) throws JAXRException {
		// Pass ebObject to superclass so slot-s can be initialized
		super.setBindingObject(ebIdentifiableType);

		ebIdentifiableType.setId(key.getId());

		if (home != null) {
			ebIdentifiableType.setHome(home);
		}
	}

	public String toString() {
		String str = super.toString();

		try {
			str = getId() + "," + str;
		} catch (JAXRException e) {
			log.warn(JAXRResourceBundle.getInstance().getString("ErrorGettingId"), e);
		}

		return str;
	}

	/**
	 * Returns true if the object specified is a RegistryObjectImpl with the
	 * same id.
	 * 
	 * @param o
	 *            The object to compare to.
	 * @return <code>true</code> if the objects are equal.
	 * @todo Do we need to ensure the object is the same type as this instance?
	 *       For example, this instance could be a ServiceImpl and the object
	 *       could be an ExternalLinkImpl. Could these have the same id?
	 */
	public boolean equals(Object o) {
		return (compareTo(o) == 0);
		/*
		if (compareTo(o) == 0) {
			return true;
		} else {
			return false;
		}
		*/
	}

	public int hashCode() {
		int result = HashCodeUtil.SEED;
		try {
			if (key != null) {
				result = HashCodeUtil.hash(result, key.getId());
			}
			result = HashCodeUtil.hash(result, this.getClass());
		} catch (JAXRException e) {
			throw new UndeclaredThrowableException(e);
		}
		return result;
	}

	/**
	 * Compares two registries objects. Consider adding Comparable to
	 * RegistryObject in JAXR 2.0??
	 * 
	 * @return 0 (equal) is the id of the objects matches this objects id.
	 *         Otherwise return -1 (this object is less than arg o).
	 */
	public int compareTo(Object o) {
		int result = -1;

		if (o instanceof IdentifiableImpl) {
			try {
				// Need class match otherwise RegistryObjectRef and
				// IdentifiableImpl
				// with same id will match when they should not.
				if (o.getClass() == this.getClass()) {
					String myId = getId();
					String otherId = ((IdentifiableImpl) o).getKey().getId();
					result = myId.compareTo(otherId);
				}
			} catch (JAXRException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
}

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
import it.cnr.icar.eric.client.xml.registry.RegistryServiceImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.lang.ref.Reference;

import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.RegistryObject;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;


/**
 * Holds a direct or indirect reference to a RegistryObject
 * TODO: Add to JAXR 2.0 as new interface
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class RegistryObjectRef extends IdentifiableImpl {
    private Object ref = null;

    public RegistryObjectRef(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }

    public RegistryObjectRef(LifeCycleManagerImpl lcm, ObjectRefType ebObject) throws JAXRException {
        super(lcm, ebObject);
    }

    public RegistryObjectRef(LifeCycleManagerImpl lcm, Object obj) throws JAXRException {
        super(lcm);
        String _id = null;
        String _home = null;
        if (obj instanceof RegistryObjectImpl) {
            RegistryObjectImpl ro = ((RegistryObjectImpl) obj);
            _id = ro.getKey().getId();
            _home = ro.getHome();
            if (ro.isNew() || ro.isModified()) {
                //System.err.println("RegistryObjectRef: for object id:" + _id + " new:" + ro.isNew() + " modified:" + ro.isModified());
                this.ref = ro;
            } else {
                ((RegistryServiceImpl) lcm.getRegistryService()).getObjectCache()
                 .putRegistryObject(ro);
            }
        } else if (obj instanceof IdentifiableType) {
            _id = ((IdentifiableType) obj).getId();
            _home = ((IdentifiableType) obj).getHome();
        } else if (obj instanceof String) {
            _id = ((String) obj);
            /* Following code is useful in debugging Unresolved reference problems and should not be removed. */
            /*
            try {
                String queryStr = "SELECT * FROM RegistryObject WHERE id = '" +
                    (String)obj + "'";
                Query query = lcm.getRegistryService().getDeclarativeQueryManager().createQuery(Query.QUERY_TYPE_SQL, queryStr);
                AdhocQueryRequest req = ((QueryImpl)query).toBindingObject();
                @SuppressWarnings("unused")
				UserType user = null;

                QueryManagerSOAPProxy serverQMProxy = new QueryManagerSOAPProxy(
                    ((ConnectionImpl)((RegistryServiceImpl)lcm.getRegistryService()).getConnection()).getQueryManagerURL(),
                    null);
                AdhocQueryResponse resp = serverQMProxy.submitAdhocQuery((RequestContext) req);
                RegistryObjectListType sqlResult = resp.getRegistryObjectList();
                //.getSQLQueryResult();
                List<?> items = sqlResult.getIdentifiable();
                Iterator<?> iter = items.iterator();
                if (iter.hasNext()) {
                    IdentifiableType it = (IdentifiableType)((JAXBElement<?>)iter.next()).getValue();
                    if (it == null) {
                        throw new JAXRException("Got a null from query.");
                    } else {
                        System.err.println("Adding String for object. id:" + it.getId() + " class:" + it.getClass());
                    }
                } else {
                    throw new JAXRException("Cannot add string: '" + _id + "' to cache if it is not resolvable.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            /**/
        } else {
            JAXRException e = new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unexpected.object", new Object[] {obj}));
            e.printStackTrace();
            throw e;
        }

        if (_id == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.reference.id.null"));
        }

        KeyImpl key = new KeyImpl(lcm);
        key.setId(_id);
        this.setKey(key);

        setHome(_home);
        //System.err.println("Created RegistryObjectRef for object of type: " + obj.getClass() + " id:" + _id + " object:" + obj.toString());


    }

    /*
     * Must be called only if ref is null or a Reference.
     *
     */
    private Reference<?> getReference(String objectType) throws JAXRException {
        Reference<?> _ref = null;

        //Need to also check if ref.get() == null as referrant could have been GCed
        if ((ref == null) || (ref instanceof Reference)) {
            if ((ref != null) && ((Reference<?>)ref).get() != null) {
                _ref = (Reference<?>)ref;
            } else {
                _ref = ((RegistryServiceImpl) lcm.getRegistryService()).getObjectCache()
                   .getReference(getId(), objectType);
            }

            ref = _ref;

        } else {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.expected.ref.null.or.reference",new Object[] {ref.getClass()}));
        }
        return _ref;
    }

    public RegistryObject getRegistryObject(String objectType)
        throws JAXRException {
        RegistryObject ro = null;

        if ((ref == null) || (ref instanceof Reference)) {
            Reference<?> _ref = getReference(objectType);
            ro = (RegistryObject) (_ref.get());
        } else if (ref instanceof RegistryObject) {
            ro = (RegistryObject)ref;
        } else {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.expected.ref.null.reference.registryobject",new Object[] {ref.getClass()}));
        }

        return ro;
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

        ObjectRefType ebObjectRefType = factory.createObjectRefType(); 
		setBindingObject(ebObjectRefType);
		
//		JAXBElement<ObjectRefType> ebObjectRef = factory.createObjectRef(ebObjectRefType);
		
		return ebObjectRefType;
    }
}

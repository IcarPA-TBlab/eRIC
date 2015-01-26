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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

/**
 * Implements JAXR API interface named ClassificationScheme.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ClassificationSchemeImpl extends RegistryEntryImpl
    implements ClassificationScheme {

    private static final Log log = LogFactory.getLog(ClassificationSchemeImpl.class);

    private boolean external = false;
    private int valueType = ClassificationScheme.VALUE_TYPE_UNIQUE; //??No default defined by spec
    private ArrayList<Concept> children = new ArrayList<Concept>();
    private boolean childrenLoaded = false;

    public ClassificationSchemeImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
        childrenLoaded = true;
    }

    /**
     * This constructor is to do a type-safe cast from a Concept with no parent to a scheme.
     * This has history in UDDI and we are not sure what value it has in ebXML.
     * We are implementing it as it is required by the JAXR API.
     * Needs evaluation for relevance in JAXR 2.0??
     **/
    public ClassificationSchemeImpl(LifeCycleManagerImpl lcm, Concept concept)
        throws JAXRException {
        super(lcm);

        if (concept.getParent() != null) {
            throw new InvalidRequestException(
                 JAXRResourceBundle.getInstance().getString("message.error.cannot.create.concept.parent.classScheme"));
        }

        setName(concept.getName());
        setDescription(concept.getDescription());
        addClassifications(concept.getClassifications());
        addExternalIdentifiers(concept.getExternalIdentifiers());

        //??incomplete
    }

    public ClassificationSchemeImpl(LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType ebScheme)
        throws JAXRException {
        super(lcm, ebScheme);
        external = !(ebScheme.isIsInternal());
        
        //See if ebScheme includes children
        List<ClassificationNodeType> ebChildren = ebScheme.getClassificationNode();
        
        if (ebChildren.size() > 0) {
            Iterator<ClassificationNodeType> iter = ebChildren.iterator();
            while (iter.hasNext()) {
                ClassificationNodeType ebChild = iter.next();
                ConceptImpl child = new ConceptImpl(lcm, ebChild);
                addChildConcept(child);
            }        
            childrenLoaded = true;
        } else {
            try {
                //Check if optmization flag present indicating there are no children.
                HashMap<String, Serializable> slotsMap = bu.getSlotsFromRegistryObject(ebScheme);
                @SuppressWarnings("static-access")
				String childCount = (String)slotsMap.get(bu.CANONICAL_SLOT_NODE_PARENT_CHILD_NODE_COUNT);
                if ((childCount != null) && (childCount.equals("0"))) {
                    childrenLoaded = true;
                }
            } catch (JAXBException e) {
                //No big harm done as this is a optmization flag only.
                log.error(e);
            }
        }        
    }

    //??This shoudl be added to JAXR 2.0 API.
    public void setExternal(boolean external) throws JAXRException {
        if (this.external != external) {
            this.external = external;
            setModified(true);  
        }
    }

    public void addChildConcept(Concept c) throws JAXRException {
        if (!(children.contains(c))) {
            children.add(c);
            ((ConceptImpl) c).setClassificationScheme(this);
            setExternal(false);
            //No need to call setModified(true) since RIM does not require parent to remember children
        }
    }

    public void addChildConcepts(@SuppressWarnings("rawtypes") Collection par1) throws JAXRException {
        Iterator<?> iter = par1.iterator();

        while (iter.hasNext()) {
            ConceptImpl concept = (ConceptImpl) iter.next();
            addChildConcept(concept);
        }

        //No need to call setModified(true) since RIM does not require parent to remember children
    }
    

    public void removeChildConcept(Concept par1) throws JAXRException {
        children.remove(par1);

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public void removeChildConcepts(@SuppressWarnings("rawtypes") Collection par1) throws JAXRException {
        children.removeAll(par1);

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public int getChildConceptCount() throws JAXRException {
        return getChildrenConcepts().size();
    }

    @SuppressWarnings("unchecked")
	public Collection<Concept> getChildrenConcepts() throws JAXRException {
        if (!childrenLoaded) {
            DeclarativeQueryManager dqm = lcm.getRegistryService()
                                             .getDeclarativeQueryManager();
            String qs = "SELECT * FROM ClassificationNode WHERE parent = '" +
                getKey().getId() + "' ORDER BY CODE";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
            children.addAll(dqm.executeQuery(query).getCollection());
            childrenLoaded = true;
        }

        return children;
    }

    /**
     * Gets child concepts in their original order.
     * This should really be behavior of getChildrenConcepts()
     * method and a separate method should allow ORBER BY 
     * to be specified. Keeping it safe and simple for now.
     * 
     * Not yet planned for JAXR 2.0.
     *
     * @return the Set of child concepts in the default order. 
     *   Current implementation returns then in order of creation.
     */
    @SuppressWarnings("unchecked")
	public Collection<Concept> getChildrenConceptsUnordered() throws JAXRException {
        if (!childrenLoaded) {
            DeclarativeQueryManager dqm = lcm.getRegistryService()
                                             .getDeclarativeQueryManager();
            String qs = "SELECT * FROM ClassificationNode WHERE parent = '" +
                getKey().getId() + "' ";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
            children.addAll(dqm.executeQuery(query).getCollection());
            childrenLoaded = true;
        }

        return children;
    }
    
    @SuppressWarnings("unchecked")
	public Collection<Concept> getDescendantConcepts() throws JAXRException {
        getChildrenConcepts();
        ArrayList<Concept> descendants = new ArrayList<Concept>(children);
        Iterator<Concept> iter = children.iterator();

        while (iter.hasNext()) {
            Concept child = iter.next();

            if (child.getChildConceptCount() > 0) {
                descendants.addAll(child.getDescendantConcepts());
            }
        }

        return descendants;        
    }

    public boolean isExternal() throws JAXRException {
        Collection<Concept> _children = getChildrenConcepts();
        boolean _external = external;
        if (_children.size() == 0) {
            _external = true;
        } else {
            _external = false;
        }
        setExternal(_external);
        return external;
    }

    public void setValueType(int param) throws javax.xml.registry.JAXRException {
        this.valueType = param;
        setModified(true);
    }

    public int getValueType() throws javax.xml.registry.JAXRException {
        return valueType;
    }

    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        ClassificationSchemeType ebClassificationSchemeType = factory.createClassificationSchemeType();
        setBindingObject(ebClassificationSchemeType);

//		JAXBElement<ClassificationSchemeType> ebClassificationScheme = factory.createClassificationScheme(ebClassificationSchemeType);
        
        return ebClassificationSchemeType;
    }

    protected void setBindingObject(ClassificationSchemeType ebClassificationSchemeType)
        throws JAXRException {
        super.setBindingObject(ebClassificationSchemeType);

        ebClassificationSchemeType.setIsInternal(!isExternal());

        switch (this.getValueType()) {
        case VALUE_TYPE_EMBEDDED_PATH:
            ebClassificationSchemeType.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_EmbeddedPath);
            break;
        case VALUE_TYPE_NON_UNIQUE:
            ebClassificationSchemeType.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_NonUniqueCode);
            break;
        case VALUE_TYPE_UNIQUE:
            ebClassificationSchemeType.setNodeType(BindingUtility.CANONICAL_NODE_TYPE_ID_UniqueCode);
            break;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();        
        composedObjects.addAll(getChildrenConcepts());
        
        return composedObjects;
    }
}

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
import it.cnr.icar.eric.common.exceptions.MissingParentReferenceException;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.RegistryObject;

import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

/**
 * Implements JAXR API interface named Classification.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ClassificationImpl extends RegistryObjectImpl
    implements Classification {
    private RegistryObjectRef conceptRef = null;
    private RegistryObjectRef schemeRef = null;
    private String value = null;
    private RegistryObjectRef classifiedObjectRef = null;

    public ClassificationImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public ClassificationImpl(LifeCycleManagerImpl lcm,
        ClassificationType ebClass, RegistryObject classifiedObject)
        throws JAXRException {
        super(lcm, ebClass);

        Object schemeObj = ebClass.getClassificationScheme();

        if (schemeObj != null) {
            schemeRef = new RegistryObjectRef(lcm, schemeObj);
        }

        if (classifiedObject != null) {
            classifiedObjectRef = new RegistryObjectRef(lcm, classifiedObject);
        }

        Object cnodeObj = ebClass.getClassificationNode();

        if (cnodeObj != null) {
            conceptRef = new RegistryObjectRef(lcm, cnodeObj);
        }

        value = ebClass.getNodeRepresentation();

        // clean modified flag since object freshly loaded from registry
        setModified(false);
    }

    public Concept getConcept() throws JAXRException {
        Concept concept = null;

        if (conceptRef != null) {
            concept = (Concept) (conceptRef.getRegistryObject(
                    "ClassificationNode"));
        }

        return concept;
    }

    public void setConcept(Concept concept) throws JAXRException {
        //If Class scheme is  External type in that case the concept will be null
        //so the conceptRef should be set to the null also. 
        if (concept == null) {
            conceptRef = null;
        } else {
            ClassificationScheme scheme = concept.getClassificationScheme();
            if (scheme == null) {
                throw new InvalidRequestException(
                 JAXRResourceBundle.getInstance().getString("message.error.no.classScheme.ancestore"));
            }
            conceptRef = new RegistryObjectRef(lcm, concept);
        }          
        setModified(true);   
    }

    public ClassificationScheme getClassificationScheme()
        throws JAXRException {
        ClassificationScheme scheme = null;

        if (schemeRef == null) {
            Concept concept = getConcept();

            if (concept != null) {
                scheme = concept.getClassificationScheme();
            }
        } else {
            scheme = (ClassificationScheme) (schemeRef.getRegistryObject(
                    "ClassificationScheme"));
        }

        return scheme;
    }

    public void setClassificationScheme(ClassificationScheme scheme)
        throws JAXRException {
        schemeRef = new RegistryObjectRef(lcm, scheme);
        setModified(true);
    }

    public InternationalString getName() throws JAXRException {
        InternationalString name = super.getName();

        //Clone name from Concept if none defined
        if (name.getLocalizedStrings().size() == 0) {
            if (!isExternal()) {
                Concept concept = getConcept();

                if (concept != null) {
                    name = (InternationalStringImpl) ((InternationalStringImpl) (concept.getName())).clone();
                }
            }
        }

        return name;
    }

    public String getValue() throws JAXRException {
        String val = null;

        if (isExternal()) {
            val = value;
        } else {
            Concept concept = getConcept();

            if (concept != null) {
                val = concept.getValue();
            }
        }

        return val;
    }

    public void setValue(String par1) throws JAXRException {
        value = par1;
        setModified(true);
    }

    public RegistryObject getClassifiedObject() throws JAXRException {
        RegistryObject classifiedObject = null;

        if (classifiedObjectRef != null) {
            classifiedObject = (classifiedObjectRef.getRegistryObject(
                    "RegistryObject"));
        }

        return classifiedObject;
    }

    public void setClassifiedObject(RegistryObject ro)
        throws JAXRException {
        if (ro == null) {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.classified.object.null"));
        }

        classifiedObjectRef = new RegistryObjectRef(lcm, ro);
        setModified(true);

        //In case this was called directly by client, make sure that this classification is added to classified Object
        Collection<?> classifications = ro.getClassifications();

        if (!classifications.contains(this)) {
            ro.addClassification(this);
        }
    }

    public boolean isExternal() throws JAXRException {
        boolean external = false;

        if (conceptRef == null) {
            external = true;
        }

        return external;
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

        ClassificationType ebClassificationType = factory.createClassificationType();
        setBindingObject(ebClassificationType);

//		JAXBElement<ClassificationType> ebClassification = factory.createClassification(ebClassificationType);
        
        return ebClassificationType;
    }

    protected void setBindingObject(ClassificationType ebClassificationType)
        throws JAXRException {
        super.setBindingObject(ebClassificationType);

        if (schemeRef != null) {
            ebClassificationType.setClassificationScheme(schemeRef.getId());
        }

        if (conceptRef != null) {
            ebClassificationType.setClassificationNode(conceptRef.getId());
        }

        if (classifiedObjectRef != null) {
            ebClassificationType.setClassifiedObject(classifiedObjectRef.getId());
        } else {
            throw new MissingParentReferenceException(
                JAXRResourceBundle.getInstance().getString("message.error.missing.classified.object.id",new Object[] {getId()}));
        }

        if (value != null) {
            ebClassificationType.setNodeRepresentation(value);
        }
    }

    /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<RegistryObjectRef> getRegistryObjectRefs() {
        HashSet<RegistryObjectRef> refs = new HashSet<RegistryObjectRef>();

        //refs.addAll(super.getRegistryObjectRefs());
        if (conceptRef != null) {
            refs.add(conceptRef);
        }

        if (schemeRef != null) {
            refs.add(schemeRef);
        }

        if (classifiedObjectRef != null) {
            refs.add(classifiedObjectRef);
        }

        return refs;
    }
}

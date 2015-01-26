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

import java.util.*;

import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.RegistryObject;

import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;


/**
 * Implements JAXR API interface named ExternalLink.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class ExternalLinkImpl extends RegistryObjectImpl implements ExternalLink {
    private String externalURI = null;
    private boolean validateURI = true;
    
    public ExternalLinkImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);
    }
    
    public ExternalLinkImpl(LifeCycleManagerImpl lcm, ExternalLinkType ebExtLink)
    throws JAXRException {
        super(lcm, ebExtLink);
        
        externalURI = ebExtLink.getExternalURI();
    }
    
    public Collection<RegistryObject> getLinkedObjects() throws JAXRException {
        HashSet<RegistryObject> linkedObjects = new HashSet<RegistryObject>();

        Iterator<?> iter = getAssociations().iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getAssociationTypeRef().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks)) {
                linkedObjects.add(ass.getTargetObject());
            }
        }

        return linkedObjects;
    }
    
    @SuppressWarnings("unchecked")
	public HashSet<?> getComposedObjects() throws JAXRException {
        @SuppressWarnings("rawtypes")
		HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getLinkedObjects());
        return composedObjects;
    }
    
    public String getExternalURI() throws JAXRException {
        return externalURI;
    }
    
    public void setExternalURI(String uri) throws JAXRException {
        validateURI(uri);
        externalURI = uri;
        setModified(true);
    }
    
    public boolean getValidateURI() throws JAXRException {
        return validateURI;
    }
    
    public void setValidateURI(boolean validateURI) throws JAXRException {
        this.validateURI = validateURI;
    }
    
    public Concept getObjectType() throws JAXRException {
        Concept objectType = super.getObjectType();
        
        if (objectType == null) {
            if (objectType == null) {
                objectType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType + "/RegistryObject/ExternalLink");
                
                if (objectType != null) {
                    setObjectType(objectType);
                }
            }
        }

        return objectType;
    }

    public void setObjectType(Concept objectType) throws JAXRException {
        setObjectTypeInternal(objectType);
    }

    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        ExternalLinkType ebExternalLinkType = factory.createExternalLinkType(); 
		setBindingObject(ebExternalLinkType);
		
//		JAXBElement<ExternalLinkType> ebExternalLink = factory.createExternalLink(ebExternalLinkType);
		
		return ebExternalLinkType;
    }
    
    protected void setBindingObject(ExternalLinkType ebExternalLinkType)
    throws JAXRException {
        super.setBindingObject(ebExternalLinkType);
        
        ebExternalLinkType.setExternalURI(externalURI);
        if (objectTypeRef != null) {
            ebExternalLinkType.setObjectType(objectTypeRef.getId());
        }
        
    }
    
    private void validateURI(String uri) throws InvalidRequestException {
        
        if (validateURI) {
            // check the http url
            boolean isValid = it.cnr.icar.eric.common.Utility.getInstance().isValidURI(uri);
            
            if (!isValid) {
                throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.error.url.not.resolvable",new Object[] {uri}));
            }
        }
    }

    public boolean isExternalURIPresent() throws JAXRException {
        return (externalURI != null && externalURI.length() > 0);
    }

}

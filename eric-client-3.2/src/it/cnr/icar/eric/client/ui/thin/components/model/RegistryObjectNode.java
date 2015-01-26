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

package it.cnr.icar.eric.client.ui.thin.components.model;


import javax.faces.context.FacesContext;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.JAXRException;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.ui.thin.WebUIResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Node is a JavaBean representing a node in a tree control or menu.</p>
 */

public class RegistryObjectNode extends Node {

    private static Log log = LogFactory.getLog(RegistryObjectNode.class);
    @SuppressWarnings("unused")
	private String id = null;
    @SuppressWarnings("unused")
	private boolean hasChild = false;
    private RegistryObject ro = null;
    private Concept registryObjectType = null;
    // ----------------------------------------------------------- Constructors

    public RegistryObjectNode(RegistryObject ro) {
        super();
        if (ro == null) {
            throw new IllegalArgumentException("RegistryObject is null");
        }
        setEnabled(true);     
        this.ro = ro;
    }
    
    public RegistryObjectNode(RegistryObject ro, Concept registryObjectType) {
        this(ro);
        this.registryObjectType = registryObjectType;
    }
    
    public Concept getRegistryObjectType() {
        return registryObjectType;
    }
    
    public RegistryObject getRegistryObject()  {
        return ro;
    }
    
    public void setRegistryObject(RegistryObject ro)  {
        if (ro == null) {
            throw new IllegalArgumentException("RegistryObject is null");
        }
        this.ro = ro;
    }
    
    public String getId() throws JAXRException {
        return ro.getKey().getId();
    }
    
    public String getRegistryObjectPath() throws JAXRException {
        String path = null;
        if (ro instanceof ClassificationScheme) {
            path = "/" + getId();
        } else if (ro instanceof Concept) {
            path = ((Concept)ro).getPath();
        }
        return path;
    }
    
    public String getLabel() {
        String label = null;
        try {
            label = ((InternationalStringImpl)ro.getName()).getClosestValue(FacesContext.getCurrentInstance().getViewRoot().getLocale());
            if (label == null && ro instanceof Concept) {
                label = ((Concept)ro).getValue();
            }
            if (label == null) {
                label = WebUIResourceBundle.getInstance().getString("message.noName", "No Name");
                StringBuffer sb = new StringBuffer("(");
                sb.append(label).append(')');
                label = sb.toString();
            }
            if (ro instanceof ClassificationScheme || ro instanceof Concept) {           
                StringBuffer sb = new StringBuffer(label);
                if (ro instanceof ClassificationScheme) {
                    ClassificationScheme scheme = (ClassificationScheme)ro;
                    sb.append(" (").append(scheme.getChildConceptCount()).append(')');
                } else {
                    Concept concept = (Concept)ro;
                    sb.append(" (").append(concept.getChildConceptCount()).append(')');
                }       
                label = sb.toString();
            }
        }  catch (JAXRException ex) {
            log.error(ex);
        }
        return label;
    }
}

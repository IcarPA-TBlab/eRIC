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
package it.cnr.icar.eric.client.ui.swing.graph;




import it.cnr.icar.eric.client.ui.swing.AssociationPanel;
import it.cnr.icar.eric.client.ui.swing.CardManagerPanel;
import it.cnr.icar.eric.client.ui.swing.JAXRClient;
import it.cnr.icar.eric.client.ui.swing.RegistryBrowser;
import it.cnr.icar.eric.common.BindingUtility;

import java.awt.GridBagConstraints;

import java.util.ArrayList;

import javax.swing.JPanel;

import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;


/**
 * A panel that allows setting different types of relationships between two RegistryObjects.
 *
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a>
 */
public class RelationshipPanel extends CardManagerPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6973824559366717364L;
	public static final String RELATIONSHIP_TYPE_ASSOCIATION = "Association";
    public static final String RELATIONSHIP_TYPE_REFERENCE = "Reference";
    
    //Do not I18N the following constants.
    private static final String REL_ASSOCIATION_SRC = "sourceObject";
    private static final String REL_ASSOCIATION_TARGET = "targetObject";
    private static final String REL_EXTERNALINK_RO = "externalLink";
    private static final String REL_EXTERNALID_RO = "registryObject";
    private static final String REL_REGISTRY_PACKAGE_RO = "registryObject";
    private static final String REL_CLASSIFICATION_RO = "classifiedObject";
    private static final String REL_CLASSIFICATION_SCHEME = "classificationScheme";
    private static final String REL_CLASSIFICATION_CONCEPT = "concept";
    @SuppressWarnings("unused")
	private static final String REL_CONCEPT_PARENT = "parent";
    private static final String REL_EXTERNALID_IDSCHEME = "identificationScheme";
    private static final String REL_ORGANIZATION_CHILDORGS = "childOrganizations";
    private static final String REL_ORGANIZATION_CONTACT = "primaryContact";
    private static final String REL_BINDING_SPECLINKS = "specificationLinks";
    private static final String REL_SERVICE_BINDINGS = "serviceBindings";
    private static final String REL_SPECLINK_SPEC = "specificationObject";
    private static final String REL_FEDERATION_ORG = "operator";
    
    
    @SuppressWarnings("unused")
	private GridBagConstraints c = new GridBagConstraints();
    private RegistryObject src = null;
    private RegistryObject target = null;
    private String relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;
    private AssociationPanel assPanel;
    private ReferencePanel refPanel;
    private ArrayList<String> map = new ArrayList<String>();
    
    /*
     * Each row (first dimension in multi-dimensional array) in the refMatrix 
     * represents a JAXR class which is the source of a relationship.
     *
     * Each column (second dimension in multi-dimensional array) in the refMatrix 
     * represents a JAXR class which is the source of a relationship.
     *
     * Each cell (second dimension in multi-dimensional array) in the refMatrix represents a collection 
     * of referenceAttributes from source JAXR class to target JAXR class.
     *
     * Any changes to refMatrix MUST be synced with changes to map initialization in class constructor.
     */    
    private String[][][] refMatrix = {
        
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //AdhocQuery
        {
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //AdhocQuery (extension)
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Association
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //AuditableEvent
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Classification
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //ClassificationScheme
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Concept
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //ExternalIdentifier
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //ExternalLink
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //ExtrinsicObject
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Federation (extension)
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Organization
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Person (extension)
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Registry (extension)
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //RegistryPackage
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //ServiceBinding
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //Service
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }, //SpecificationLink
            {REL_ASSOCIATION_SRC, REL_ASSOCIATION_TARGET  }  //User
        }, //Association
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //AuditableEvent
        {
            {REL_CLASSIFICATION_RO  }, //AdhocQuery (extension)
            {REL_CLASSIFICATION_RO  }, //Association
            {REL_CLASSIFICATION_RO  }, //AuditableEvent
            {REL_CLASSIFICATION_RO  }, //Classification
            {REL_CLASSIFICATION_RO, REL_CLASSIFICATION_SCHEME  }, //ClassificationScheme
            {REL_CLASSIFICATION_RO, REL_CLASSIFICATION_CONCEPT  }, //Concept
            {REL_CLASSIFICATION_RO  }, //ExternalIdentifier
            {REL_CLASSIFICATION_RO  }, //ExternalLink
            {REL_CLASSIFICATION_RO  }, //ExtrinsicObject
            {REL_CLASSIFICATION_RO  }, //Federation (extension)
            {REL_CLASSIFICATION_RO  }, //Organization
            {REL_CLASSIFICATION_RO  }, //Person (extension)
            {REL_CLASSIFICATION_RO  }, //Registry (extension)
            {REL_CLASSIFICATION_RO  }, //RegistryPackage
            {REL_CLASSIFICATION_RO  }, //ServiceBinding
            {REL_CLASSIFICATION_RO  }, //Service
            {REL_CLASSIFICATION_RO  }, //SpecificationLink
            {REL_CLASSIFICATION_RO  }  //User            
        }, //Classification
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //ClassificationScheme
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme ?
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //Concept
        {
            {REL_EXTERNALID_RO  }, //AdhocQuery (extension)
            {REL_EXTERNALID_RO  }, //Association
            {REL_EXTERNALID_RO  }, //AuditableEvent
            {REL_EXTERNALID_RO  }, //Classification
            {REL_EXTERNALID_RO, REL_EXTERNALID_IDSCHEME  }, //ClassificationScheme
            {REL_EXTERNALID_RO  }, //Concept
            {REL_EXTERNALID_RO  }, //ExternalIdentifier
            {REL_EXTERNALID_RO  }, //ExternalLink
            {REL_EXTERNALID_RO  }, //ExtrinsicObject
            {REL_EXTERNALID_RO  }, //Federation (extension)
            {REL_EXTERNALID_RO  }, //Organization
            {REL_EXTERNALID_RO  }, //Person (extension)
            {REL_EXTERNALID_RO  }, //Registry (extension)
            {REL_EXTERNALID_RO  }, //RegistryPackage
            {REL_EXTERNALID_RO  }, //ServiceBinding
            {REL_EXTERNALID_RO  }, //Service
            {REL_EXTERNALID_RO  }, //SpecificationLink
            {REL_EXTERNALID_RO  }  //User
        }, //ExternalIdentifier
        {
            {REL_EXTERNALINK_RO  }, //AdhocQuery (extension)
            {REL_EXTERNALINK_RO  }, //Association
            {REL_EXTERNALINK_RO  }, //AuditableEvent
            {REL_EXTERNALINK_RO  }, //Classification
            {REL_EXTERNALINK_RO  }, //ClassificationScheme
            {REL_EXTERNALINK_RO  }, //Concept
            {REL_EXTERNALINK_RO  }, //ExternalIdentifier
            {REL_EXTERNALINK_RO  }, //ExternalLink
            {REL_EXTERNALINK_RO  }, //ExtrinsicObject
            {REL_EXTERNALINK_RO  }, //Federation (extension)
            {REL_EXTERNALINK_RO  }, //Organization
            {REL_EXTERNALINK_RO  }, //Person (extension)
            {REL_EXTERNALINK_RO  }, //Registry (extension)
            {REL_EXTERNALINK_RO  }, //RegistryPackage
            {REL_EXTERNALINK_RO  }, //ServiceBinding
            {REL_EXTERNALINK_RO  }, //Service
            {REL_EXTERNALINK_RO  }, //SpecificationLink
            {REL_EXTERNALINK_RO  }  //User
        }, //ExternalLink
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //ExtrinsicObject
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {REL_FEDERATION_ORG  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //Federation
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {REL_ORGANIZATION_CHILDORGS  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {REL_ORGANIZATION_CONTACT  }  //User
        }, //Organization
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //Person
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //Registry
        {
            {REL_REGISTRY_PACKAGE_RO  }, //AdhocQuery (extension)
            {REL_REGISTRY_PACKAGE_RO  }, //Association
            {REL_REGISTRY_PACKAGE_RO  }, //AuditableEvent
            {REL_REGISTRY_PACKAGE_RO  }, //Classification
            {REL_REGISTRY_PACKAGE_RO  }, //ClassificationScheme
            {REL_REGISTRY_PACKAGE_RO  }, //Concept
            {REL_REGISTRY_PACKAGE_RO  }, //ExternalIdentifier
            {REL_REGISTRY_PACKAGE_RO  }, //ExternalLink
            {REL_REGISTRY_PACKAGE_RO  }, //ExtrinsicObject
            {REL_REGISTRY_PACKAGE_RO  }, //Federation (extension)
            {REL_REGISTRY_PACKAGE_RO  }, //Organization
            {REL_REGISTRY_PACKAGE_RO  }, //Person (extension)
            {REL_REGISTRY_PACKAGE_RO  }, //Registry (extension)
            {REL_REGISTRY_PACKAGE_RO  }, //RegistryPackage
            {REL_REGISTRY_PACKAGE_RO  }, //ServiceBinding
            {REL_REGISTRY_PACKAGE_RO  }, //Service
            {REL_REGISTRY_PACKAGE_RO  }, //SpecificationLink
            {REL_REGISTRY_PACKAGE_RO  }  //User
        }, //RegistryPackage
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {REL_BINDING_SPECLINKS  }, //SpecificationLink
            {  }  //User
        }, //ServiceBinding
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {REL_SERVICE_BINDINGS  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //Service
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {REL_SPECLINK_SPEC  }, //ExternalLink
            {REL_SPECLINK_SPEC  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //SpecificationLink
        {
            {  }, //AdhocQuery (extension)
            {  }, //Association
            {  }, //AuditableEvent
            {  }, //Classification
            {  }, //ClassificationScheme
            {  }, //Concept
            {  }, //ExternalIdentifier
            {  }, //ExternalLink
            {  }, //ExtrinsicObject
            {  }, //Federation (extension)
            {  }, //Organization
            {  }, //Person (extension)
            {  }, //Registry (extension)
            {  }, //RegistryPackage
            {  }, //ServiceBinding
            {  }, //Service
            {  }, //SpecificationLink
            {  }  //User
        }, //User
    };

    /**
     * Class Constructor.
     */
    @SuppressWarnings("unused")
	public RelationshipPanel(RegistryObject src, RegistryObject target) {
        super(new String[] {
                RELATIONSHIP_TYPE_ASSOCIATION, RELATIONSHIP_TYPE_REFERENCE
            },
            new JPanel[] { new AssociationPanel(), new ReferencePanel(src,
                    target) });

        assPanel = (AssociationPanel) cardPanels[0];
        refPanel = (ReferencePanel) cardPanels[1];

        this.src = src;
        this.target = target;

        //Any changes to below MUST be synced with changes to refMatrix initializer above
        map.add("AdhocQuery"); // (extension)
        map.add("Association");
        map.add("AuditableEvent");
        map.add("Classification");
        map.add("ClassificationScheme");
        map.add("Concept");
        map.add("ExternalIdentifier");
        map.add("ExternalLink");
        map.add("ExtrinsicObject");
        map.add("Federation"); // (extension)
        map.add("Organization");
        map.add("Person (extension)");
        map.add("Registry"); // (extension)
        map.add("RegistryPackage");
        map.add("ServiceBinding");
        map.add("Service");
        map.add("SpecificationLink");
        map.add("User");
        
        int row = map.indexOf(getJAXRName(src));
        int col = map.indexOf(getJAXRName(target));

        String[] refs = refMatrix[row][col];

        try {
            JAXRClient client = RegistryBrowser.getInstance().getClient();
            BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
            it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl bqm = (it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl) (client.getBusinessQueryManager());

            if (refs.length == 0) {
                remove(selectorPanel);
                initAssociationPanel();
            } else {
                relationshipType = RELATIONSHIP_TYPE_REFERENCE;
                refPanel.setReferenceAttributes(refs);
            }

            showCard(relationshipType);
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }
    }

    private void initAssociationPanel() throws JAXRException {
        JAXRClient client = RegistryBrowser.getInstance().getClient();
        BusinessLifeCycleManager lcm = client.getBusinessLifeCycleManager();
        it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl bqm = (it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl) (client.getBusinessQueryManager());

        relationshipType = RELATIONSHIP_TYPE_ASSOCIATION;

        Concept assType = null;

        //Set associationType for any pre-defined associations
        if (src instanceof RegistryPackage) {
            assType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_HasMember);
        } else if (src instanceof ExternalLink) {
            assType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_ExternallyLinks);
        } else if ((src instanceof Organization) &&
                (target instanceof Service)) {
            assType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_OffersService);
        }

        //this.remove(selectorPanel);
        Association ass = lcm.createAssociation(target, assType);
        src.addAssociation(ass);
        assPanel.setModel(ass);
    }

    protected void showCardAction(String card) {
        if (card.equals(RELATIONSHIP_TYPE_ASSOCIATION)) {
            try {
                if (assPanel.getModel() == null) {
                    initAssociationPanel();
                }
            } catch (JAXRException e) {
                RegistryBrowser.displayError(e);
            }
        }

        super.showCardAction(card);
    }

    public void setReferenceAttributeOnSourceObject() throws JAXRException {
        if (relationshipType.equals(RELATIONSHIP_TYPE_REFERENCE)) {
            refPanel.setReferenceAttributeOnSourceObject();
        }
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public String getRelationshipName() {
        String relName = "";

        try {
            if (relationshipType.equals(RELATIONSHIP_TYPE_ASSOCIATION)) {
                Concept assType = ((Association) (assPanel.getModel())).getAssociationType();

                if (assType != null) {
                    relName = assType.getValue();
                }
            } else if (relationshipType.equals(RELATIONSHIP_TYPE_REFERENCE)) {
                relName = refPanel.getReferenceAttribute();
            }
        } catch (JAXRException e) {
            RegistryBrowser.displayError(e);
        }

        return relName;
    }

    public Association getAssociation() throws JAXRException {
        return (Association) (assPanel.getModel());
    }

    String getJAXRName(RegistryObject ro) {
        String newClassName = ro.getClass().getName();
        newClassName = newClassName.substring(newClassName.lastIndexOf(".") +
                1);

        if (newClassName.endsWith("Impl")) {
            //Remove Impl suffix for JAXR provider Impl classes
            newClassName = newClassName.substring(0, newClassName.length() - 4);
        }

        return newClassName;
    }
}

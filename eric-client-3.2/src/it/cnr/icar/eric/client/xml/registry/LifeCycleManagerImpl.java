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
package it.cnr.icar.eric.client.xml.registry;

import it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AssociationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ClassificationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ClassificationSchemeImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ConceptImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.EmailAddressImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExternalIdentifierImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExternalLinkImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.FederationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.IdentifiableImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.KeyImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.LocalizedStringImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.Notification;
import it.cnr.icar.eric.client.xml.registry.infomodel.NotificationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.OrganizationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PersonNameImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PostalAddressImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectRef;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryPackageImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceBindingImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SlotImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SpecificationLinkImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SubscriptionImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.TelephoneNumberImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.UserImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.client.xml.registry.util.SQLQueryProvider;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CredentialInfo;
import it.cnr.icar.eric.common.LifeCycleManagerLocalProxy;
import it.cnr.icar.eric.common.LifeCycleManagerSOAPProxy;
import it.cnr.icar.eric.common.RegistryResponseHolder;
import it.cnr.icar.eric.common.exceptions.UnresolvedReferenceException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.activation.DataHandler;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryService;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.oasis.ebxml.registry.bindings.lcm.SetStatusOnObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.NotificationType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 * Implements JAXR API interface named LifeCycleManager.
 * 
 * 
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class LifeCycleManagerImpl implements LifeCycleManager {
	private static Log log = LogFactory.getLog(LifeCycleManagerImpl.class.getName());
	/** Factory class for infomodel objects and extension. */
	private static InfomodelFactory imFactory = null;
	RegistryServiceImpl regService;
	@SuppressWarnings("rawtypes")
	HashSet modifiedObjects;
	private HashMap<String, Concept> objectTypesMap;
	private org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
	private org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
	@SuppressWarnings("unused")
	private org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac;
	private it.cnr.icar.eric.common.spi.LifeCycleManager serverLCM;
	private BindingUtility bu = BindingUtility.getInstance();
	private CredentialInfo credentialInfo = null;

	LifeCycleManagerImpl(RegistryServiceImpl regService) {
		this.regService = regService;
		imFactory = InfomodelFactory.getInstance();
		modifiedObjects = new HashSet<Object>();

		lcmFac = bu.lcmFac;
		rsFac = bu.rsFac;
		rimFac = bu.rimFac;

		try {
			setCredentialInfo((regService.getConnection()).getCredentialInfo());
		} catch (JAXRException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	public static RegistryFacade createRegistryFacade() {
		RegistryFacade facade = new RegistryFacadeImpl();
		return facade;
	}

	public Notification createNotification(NotificationType ebNotification) throws JAXRException {
		Notification notification = new NotificationImpl(this, ebNotification);
		return notification;
	}

	/**
	 * Create an object for the specified objectType Add to JAXR 2.0
	 */
	public Object createObject(Concept objectTypeConcept) throws JAXRException, InvalidRequestException,
			UnsupportedCapabilityException {

		Object obj = null;
		String path = objectTypeConcept.getPath();
		if (path == null) {
			throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.Concept.path.null",
					new Object[] { objectTypeConcept.getKey().getId() }));
		}
		if (path.startsWith("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
				+ "/RegistryObject/ExtrinsicObject")) {
			obj = createExtrinsicObject(objectTypeConcept);
		} else if (path.startsWith("" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/")
				|| path.startsWith("/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
						+ "/RegistryObject/Association")) {
			obj = createAssociation(objectTypeConcept);
		} else {
			String className = getJAXRClassNameFromObjectType(objectTypeConcept);
			obj = createObject(className);
		}

		return obj;
	}

	public boolean isIntrinsicObjectType(Concept objectTypeConcept) throws JAXRException {
		boolean isIntrinsic = true;

		// This is risky as we may allow ExtrinsicObjects to be sub-classes of
		// other classes in future.
		if (objectTypeConcept.getPath().startsWith(
				"/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
						+ "/RegistryObject/ExtrinsicObject/")) {
			isIntrinsic = false;
		}

		return isIntrinsic;
	}

	public String getJAXRClassNameFromObjectType(Concept objectTypeConcept) throws JAXRException {
		String jaxrClassName = null;

		String conceptValue = objectTypeConcept.getValue();
		if (isIntrinsicObjectType(objectTypeConcept)) {
			jaxrClassName = BindingUtility.mapEbXMLNameToJAXRName(conceptValue);
		} else {
			jaxrClassName = "ExtrinsicObject";
		}

		return jaxrClassName;
	}

	public Concept getObjectTypeFromJAXRClassName(String jaxrClassName) throws JAXRException {
		return (Concept) (getObjectTypeRefFromJAXRClassName(jaxrClassName).getRegistryObject("ClassificationNode"));
	}

	public RegistryObjectRef getObjectTypeRefFromJAXRClassName(String jaxrClassName) throws JAXRException {
		RegistryObjectRef objectTypeRef = null;

		// Make sure jaxrClassName is not package qualified and does not end in
		// Impl
		int index = jaxrClassName.lastIndexOf('.');
		if (index >= 0) {
			jaxrClassName = jaxrClassName.substring(index + 1, jaxrClassName.length() - 4);
		}

		String objectTypeId = BindingUtility.getInstance().getObjectTypeId(
				BindingUtility.mapJAXRNameToEbXMLName(jaxrClassName));

		objectTypeRef = new RegistryObjectRef(this, objectTypeId);

		return objectTypeRef;
	}

	/**
	 * Creates instances of information model interfaces (factory method). To
	 * create an Organization, use this method as follows:
	 * 
	 * <pre>
	 * Organization org = (Organization) lifeCycleMgr.createObject(LifeCycleManager.ORGANIZATION);
	 * </pre>
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param interfaceName
	 *            the unqualified name of an interface in the
	 *            javax.xml.registry.infomodel package
	 * 
	 * @return an Object that can then be cast to an instance of the interface
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * 
	 * @throws InvalidRequestException
	 *             if the interface is not an interface in the
	 *             javax.xml.registry.infomodel package
	 * 
	 * @throws UnsupportedCapabilityException
	 *             if the client attempts to create an instance of an infomodel
	 *             interface that is not supported by the capability level of
	 *             the JAXR provider
	 */
	public Object createObject(String className) throws JAXRException, InvalidRequestException,
			UnsupportedCapabilityException {
		Object obj = null;

		try {
			// Try to find extended constructor by nickname
			Constructor<?> cons = imFactory.getConstructor1Arg(className);
			if (cons != null) {
				// use extended constructor
				Object[] args = { this };
				obj = cons.newInstance(args);

				// set extended type
				String typeId = imFactory.getTypeName(className);
				BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) regService.getBusinessQueryManager();
				Concept typeConcept = (Concept) bqm.getRegistryObject(typeId, LifeCycleManager.CONCEPT);
				if (obj instanceof Association) {
					((Association) obj).setAssociationType(typeConcept);
				} else if (obj instanceof ExtrinsicObject) {
					((ExtrinsicObjectImpl) obj).setObjectType(typeConcept);
				}
			} else {
				// proceed the default way: infomodel class
				className = "it.cnr.icar.eric.client.xml.registry.infomodel."
						+ BindingUtility.mapEbXMLNameToJAXRName(className) + "Impl";

				Class<?> cls = this.getClass().getClassLoader().loadClass(className);
				Class<?> lcmCls = this.getClass().getClassLoader()
						.loadClass("it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl");
				@SuppressWarnings("rawtypes")
				Class[] parmTypes = { lcmCls };
				cons = cls.getDeclaredConstructor(parmTypes);

				Object[] args = { this };
				obj = cons.newInstance(args);
			}

		} catch (ClassNotFoundException e) {
			throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString(
					"message.error.invalid.classname", new Object[] { className }));
		} catch (NoSuchMethodException e) {
			throw new JAXRException(e);
		} catch (InvocationTargetException e) {
			throw new JAXRException(e.getCause());
		} catch (IllegalAccessException e) {
			throw new JAXRException(e);
		} catch (InstantiationException e) {
			throw new JAXRException(e);
		} catch (ExceptionInInitializerError e) {
			throw new JAXRException(e);
		} catch (SecurityException e) {
			throw new JAXRException(e);
		}

		return obj;
	}

	/**
	 * Create an Association instance of the given type. The sourceObject and
	 * target object must be set later.
	 * <p>
	 * The implementation class returned depends on whether an extension class
	 * has been defined for the given associationType or not. Default is
	 * <code>.infomodel.AssociationImpl</code>.
	 * 
	 * @param associationType
	 *            The type for the Association (Concept)
	 * @return a new instance of an Association
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public Association createAssociation(Concept associationType) throws JAXRException {
		String type = null;
		if (associationType != null) {
			type = associationType.getKey().getId();
		}
		AssociationImpl ass = imFactory.createAssociation(this, type);
		ass.setAssociationType(associationType);

		return ass;
	}

	/**
	 * Create an Association instance using the specified parameters. The
	 * sourceObject is left null and will be set when the Association is added
	 * to a RegistryObject.
	 * <p>
	 * Note that for a UDDI provider an Association may only be created between
	 * Organizations.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Association createAssociation(RegistryObject targetObject, Concept associationType) throws JAXRException {
		Association ass = createAssociation(associationType);
		ass.setTargetObject(targetObject);

		return ass;
	}

	/**
	 * Create a Classification instance for an external Classification using the
	 * specified name and value that identifies a taxonomy element within
	 * specified ClassificationScheme.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Classification createClassification(ClassificationScheme scheme, String name, String value)
			throws JAXRException {
		InternationalString is = createInternationalString(name);
		Classification cl = createClassification(scheme, is, value);

		return cl;
	}

	/**
	 * Create a Classification instance for an external Classification using the
	 * specified name and value that identifies a taxonomy element within
	 * specified ClassificationScheme.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Classification createClassification(ClassificationScheme scheme, InternationalString name, String value)
			throws JAXRException {
		ClassificationImpl cl = new ClassificationImpl(this);
		cl.setClassificationScheme(scheme);
		cl.setName(name);
		cl.setValue(value);

		return cl;
	}

	/**
	 * Create a Classification instance for an internal Classification using the
	 * specified Concept which identifies a taxonomy element within an internal
	 * ClassificationScheme.
	 * <p>
	 * Throws InvalidRequestException if the Concept is not under a
	 * ClassificationScheme.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Classification createClassification(Concept concept) throws JAXRException, InvalidRequestException {
		ClassificationImpl cl = new ClassificationImpl(this);
		cl.setConcept(concept);

		return cl;
	}

	/**
	 * Create a scheme given specified parameters. This is the method to use to
	 * create a scheme in most situations.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public ClassificationScheme createClassificationScheme(String name, String description) throws JAXRException,
			InvalidRequestException {
		InternationalString isName = createInternationalString(name);
		InternationalString isDesc = createInternationalString(description);

		ClassificationScheme scheme = createClassificationScheme(isName, isDesc);

		return scheme;
	}

	/**
	 * Create a scheme given specified parameters. This is the method to use to
	 * create a scheme in most situations.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public ClassificationScheme createClassificationScheme(InternationalString name, InternationalString description)
			throws JAXRException, InvalidRequestException {
		ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(this);
		scheme.setName(name);
		scheme.setDescription(description);

		return scheme;
	}

	/**
	 * Creates a ClassificationScheme from a Concept that has no
	 * ClassificationScheme or parent Concept.
	 * <p>
	 * This method is a special case method to do a type safe conversion from
	 * Concept to ClassificationScheme.
	 * <p>
	 * This method is provided to allow for Concepts returned by the
	 * BusinessQueryManager findConcepts call to be safely cast to
	 * ClassificationScheme. It is up to the programer to make sure that the
	 * Concept is indeed semantically a ClassificationScheme.
	 * <p>
	 * This method is necessary because in UDDI a tModel may serve multiple
	 * purposes and there is no way to know when a tModel maps to a Concept and
	 * when it maps to a ClassificationScheme. UDDI leaves the determination to
	 * the programmer and consequently so does this method.
	 * <p>
	 * Throws InvalidRequestException if the Concept has a parent Concept or is
	 * under a ClassificationScheme.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * 
	 */
	public ClassificationScheme createClassificationScheme(Concept concept) throws JAXRException,
			InvalidRequestException {
		ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(this, concept);

		return scheme;
	}

	/**
	 * Create a Concept instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param parent
	 *            Is either a reference to a parent ClassificationScheme or
	 *            Concept
	 */
	public Concept createConcept(RegistryObject parent, String name, String value) throws JAXRException {
		InternationalString isName = createInternationalString(name);

		return createConcept(parent, isName, value);
	}

	/**
	 * Create a Concept instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param parent
	 *            Is either a reference to a parent ClassificationScheme or
	 *            Concept
	 */
	public Concept createConcept(RegistryObject parent, InternationalString name, String value) throws JAXRException {
		ConceptImpl concept = new ConceptImpl(this);

		if (parent instanceof ClassificationScheme) {
			concept.setClassificationScheme((ClassificationScheme) parent);
		} else if (parent instanceof Concept) {
			concept.setParentConcept((Concept) parent);
		}

		concept.setName(name);
		concept.setValue(value);

		return concept;
	}

	/**
	 * Creates an EmailAddress instance using an address as the parameter.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param address
	 *            the email address
	 * 
	 * @return the EmailAddress instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public EmailAddress createEmailAddress(String address) throws JAXRException {
		EmailAddressImpl email = new EmailAddressImpl(this);
		email.setAddress(address);

		return email;
	}

	/**
	 * Creates an EmailAddress instance using both an address and a type as
	 * parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param address
	 *            the email address
	 * @param type
	 *            the type of the address
	 * 
	 * @return the EmailAddress instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public EmailAddress createEmailAddress(String address, String type) throws JAXRException {
		EmailAddressImpl email = new EmailAddressImpl(this);
		email.setAddress(address);
		email.setType(type);

		return email;
	}

	/**
	 * Create an ExternalIdentifier instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public ExternalIdentifier createExternalIdentifier(ClassificationScheme scheme, String name, String value)
			throws JAXRException {
		InternationalString is = createInternationalString(name);
		ExternalIdentifier extId = createExternalIdentifier(scheme, is, value);

		return extId;
	}

	/**
	 * Create an ExternalIdentifier instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public ExternalIdentifier createExternalIdentifier(ClassificationScheme scheme, InternationalString name,
			String value) throws JAXRException {
		ExternalIdentifierImpl extId = new ExternalIdentifierImpl(this);
		if (scheme == null) {
			throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString(
					"message.error.IdentificationScheme.null"));
		}
		extId.setIdentificationScheme(scheme);
		extId.setName(name);
		extId.setValue(value);

		return extId;
	}

	/**
	 * Create an ExternalLink instance using the specified parameters.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * 
	 */
	public ExternalLink createExternalLink(String externalURI, String description) throws JAXRException {
		InternationalString isDesc = createInternationalString(description);
		ExternalLink link = createExternalLink(externalURI, isDesc);

		return link;
	}

	/**
	 * Create an ExternalLink instance using the specified parameters.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * 
	 */
	public ExternalLink createExternalLink(String externalURI, InternationalString description) throws JAXRException {
		ExternalLink link = new ExternalLinkImpl(this);
		link.setDescription(description);

		boolean compatibilityMode = Boolean.valueOf(
				it.cnr.icar.eric.client.xml.registry.util.ProviderProperties.getInstance().getProperty(
						"jaxr-ebxml.tck.compatibilityMode", "false")).booleanValue();

		// In compatibilityMode we MUST validate URI here but that is
		// a usability issue. See spec bug: 6381331
		// SO unless spec mode is set dont do URI validation here.
		if (!compatibilityMode) {
			link.setValidateURI(false);
		}
		link.setExternalURI(externalURI);
		link.setValidateURI(true);

		return link;
	}

	/**
	 * Create an ExtrinsicObject instance of the given type.
	 * <p>
	 * The implementation class returned depends on whether an extension class
	 * has been defined for the given objectType or not. Default is
	 * <code>.infomodel.ExtrinsicObjectImpl</code>.
	 * 
	 * @param objectType
	 *            The type for the ExtrinsicObject (Concept)
	 * @return a new instance of an ExtrinsicObject
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public ExtrinsicObject createExtrinsicObject(Concept objectType) throws javax.xml.registry.JAXRException {
		String type = objectType.getKey().getId();
		ExtrinsicObjectImpl eo = imFactory.createExtrinsicObject(this, type);
		eo.setObjectType(objectType);

		return eo;
	}

	/**
	 * Creates an ExtrinsicObject instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @param repositoryItem
	 *            the DataHandler for the repository item. Must not be null.
	 * 
	 * @return the ExtrinsicObject instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * 
	 */
	public ExtrinsicObject createExtrinsicObject(javax.activation.DataHandler repositoryItem)
			throws javax.xml.registry.JAXRException {
		ExtrinsicObject eo = imFactory.createExtrinsicObject(this, (String) null);

		if (repositoryItem != null) {
			eo.setRepositoryItem(repositoryItem);
		}

		return eo;
	}

	/**
	 * Creates an ExtrinsicObject instance using the specified parameters. JAXR
	 * 2.0??
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * 
	 * @return the ExtrinsicObject instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * 
	 */
	public ExtrinsicObject createExtrinsicObject() throws javax.xml.registry.JAXRException {
		return createExtrinsicObject((javax.activation.DataHandler) null);
	}

	/**
	 * Create a InternationalString instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public javax.xml.registry.infomodel.InternationalString createInternationalString()
			throws javax.xml.registry.JAXRException {
		return createInternationalString(Locale.getDefault(), null);
	}

	/**
	 * Create a InternationalString instance using the specified parameters and
	 * the default Locale.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public InternationalString createInternationalString(String s) throws JAXRException {
		return createInternationalString(Locale.getDefault(), s);
	}

	/**
	 * Create a InternationalString instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public InternationalString createInternationalString(Locale l, String s) throws JAXRException {
		InternationalString is = new InternationalStringImpl(this);
		if (s != null) {
			LocalizedString ls = new LocalizedStringImpl(this);
			ls.setLocale(l);
			ls.setValue(s);
			is.addLocalizedString(ls);
		}

		return is;
	}

	/**
	 * Creates a Key instance from an ID.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param id
	 *            the ID string from which to create the Key
	 * 
	 * @return the Key instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public Key createKey(String id) throws JAXRException {
		KeyImpl key = new KeyImpl(this);
		key.setId(id);

		return key;
	}

	// ??Add to level 1 API for JAXR 2.0
	public Key createKey() throws JAXRException {
		KeyImpl key = new KeyImpl(this);

		return key;
	}

	/**
	 * Creates a LocalizedString instance using the specified Locale and String
	 * parameters.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param l
	 *            the Locale in which to create the LocalizedString
	 * @param s
	 *            the String from which to create the LocalizedString
	 * 
	 * @return the LocalizedString instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public LocalizedString createLocalizedString(Locale l, String s) throws JAXRException {
		LocalizedStringImpl lString = new LocalizedStringImpl(this);
		lString.setLocale(l);
		lString.setValue(s);

		return lString;
	}

	/**
	 * Creates a LocalizedString instance using the specified Locale, String,
	 * and character set parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param l
	 *            the Locale in which to create the LocalizedString
	 * @param s
	 *            the String from which to create the LocalizedString
	 * @param charSetName
	 *            the name of the character set to use
	 * 
	 * @return the LocalizedString instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public LocalizedString createLocalizedString(Locale l, String s, String charSetName) throws JAXRException {
		LocalizedString lString = createLocalizedString(l, s);
		lString.setCharsetName(charSetName);

		return lString;
	}

	/**
	 * Create an Organization instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Organization createOrganization(String name) throws JAXRException {
		InternationalString is = createInternationalString(name);
		Organization org = createOrganization(is);

		return org;
	}

	/**
	 * Create an Organization instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Organization createOrganization(InternationalString name) throws JAXRException {
		Organization org = new OrganizationImpl(this);
		org.setName(name);

		return org;
	}

	/**
	 * Create a PersonName instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public PersonName createPersonName(String firstName, String middleName, String lastName) throws JAXRException {
		PersonNameImpl personName = new PersonNameImpl(this);
		personName.setFirstName(firstName);
		personName.setMiddleName(middleName);
		personName.setLastName(lastName);

		return personName;
	}

	/**
	 * Create a PersonName instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public PersonName createPersonName(String fullName) throws JAXRException {
		PersonNameImpl personName = new PersonNameImpl(this);
		personName.setFullName(fullName);

		return personName;
	}

	/**
	 * Create a PostalAddress instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public PostalAddress createPostalAddress(String streetNumber, String street, String city, String stateOrProvince,
			String country, String postalCode, String type) throws JAXRException {
		PostalAddress addr = new PostalAddressImpl(this);
		addr.setStreetNumber(streetNumber);
		addr.setStreet(street);
		addr.setCity(city);
		addr.setStateOrProvince(stateOrProvince);
		addr.setCountry(country);
		addr.setPostalCode(postalCode);
		addr.setType(type);

		return addr;
	}

	/**
	 * Create a RegistryPackage instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public RegistryPackage createRegistryPackage(String name) throws JAXRException {
		InternationalString is = createInternationalString(name);

		return createRegistryPackage(is);
	}

	/**
	 * Create a RegistryPackage instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public RegistryPackage createRegistryPackage(InternationalString name) throws JAXRException {
		RegistryPackageImpl pkg = new RegistryPackageImpl(this);
		pkg.setName(name);

		return pkg;
	}

	/**
	 * Create a Registry instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public RegistryImpl createRegistry(String name) throws JAXRException {
		InternationalString is = createInternationalString(name);

		return createRegistry(is);
	}

	/**
	 * Create a Registry instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public RegistryImpl createRegistry(InternationalString name) throws JAXRException {
		RegistryImpl registry = new RegistryImpl(this);
		registry.setName(name);

		return registry;
	}

	/**
	 * Create a Federation instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public FederationImpl createFederation(String name) throws JAXRException {
		InternationalString is = createInternationalString(name);

		return createFederation(is);
	}

	/**
	 * Create a Registry instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 */
	public FederationImpl createFederation(InternationalString name) throws JAXRException {
		FederationImpl federation = new FederationImpl(this);
		federation.setName(name);

		return federation;
	}

	/**
	 * Create an Service instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Service createService(String name) throws JAXRException {
		InternationalString is = createInternationalString(name);

		return createService(is);
	}

	/**
	 * Create an Service instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public Service createService(InternationalString name) throws JAXRException {
		Service service = new ServiceImpl(this);
		service.setName(name);

		return service;
	}

	/**
	 * Create an ServiceBinding instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public javax.xml.registry.infomodel.ServiceBinding createServiceBinding() throws javax.xml.registry.JAXRException {
		ServiceBinding serviceBinding = new ServiceBindingImpl(this);

		return serviceBinding;
	}

	/**
	 * Creates a Slot instance using the specified parameters, where the value
	 * is a String.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param name
	 *            the name of the Slot
	 * @param value
	 *            the value (a String)
	 * @param slotType
	 *            the slot type
	 * 
	 * @return the Slot instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public Slot createSlot(String name, String value, String slotType) throws JAXRException {
		ArrayList<String> al = new ArrayList<String>();
		al.add(value);

		return createSlot(name, al, slotType);
	}

	/**
	 * Creates a Slot instance using the specified parameters, where the value
	 * is a Collection of Strings.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param name
	 *            the name of the Slot
	 * @param value
	 *            the value (a Collection of Strings)
	 * @param slotType
	 *            the slot type
	 * 
	 * @return the Slot instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public Slot createSlot(String name, @SuppressWarnings("rawtypes") Collection values, String slotType) throws JAXRException {
		SlotImpl slot = new SlotImpl(this);
		slot.setName(name);
		slot.setValues(values);
		slot.setSlotType(slotType);

		return slot;
	}

	/**
	 * Creates an empty SpecificationLink instance.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @return the SpecificationLink instance created
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public SpecificationLink createSpecificationLink() throws JAXRException {
		SpecificationLinkImpl specLink = new SpecificationLinkImpl(this);

		return specLink;
	}

	/**
	 * Creates a Subscription instance with the specified selector.
	 * 
	 * @param selector
	 *            the id of an <code>AdhocQuery</code> object that defines the
	 *            selection criteria
	 */
	public SubscriptionImpl createSubscription(String selector) throws JAXRException {
		SubscriptionImpl subscription = new SubscriptionImpl(this);
		RegistryObjectRef objRef = new RegistryObjectRef(this, selector);
		subscription.setSelector(objRef);

		return subscription;
	}

	/**
	 * Create a TelephoneNumber instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public javax.xml.registry.infomodel.TelephoneNumber createTelephoneNumber() throws javax.xml.registry.JAXRException {
		TelephoneNumber ph = new TelephoneNumberImpl(this);

		return ph;
	}

	/**
	 * Create a User instance using the specified parameters.
	 * 
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public javax.xml.registry.infomodel.User createUser() throws javax.xml.registry.JAXRException {
		UserImpl user = new UserImpl(this);

		return user;
	}

	// ??Add to JAXRAPI 2.0
	public BulkResponse saveAllObjects() throws JAXRException {
		HashSet<?> _modifiedObjects = null;

		synchronized (modifiedObjects) {
			_modifiedObjects = (HashSet<?>) (modifiedObjects.clone());
		}

		return saveObjects(_modifiedObjects);
	}

	/**
	 * Add an object to list set of modified objects
	 */
	@SuppressWarnings("unchecked")
	public void addModifiedObject(RegistryObject ro) {
		synchronized (modifiedObjects) {
			modifiedObjects.add(ro);
		}
	}

	/**
	 * Remove an object from list set of modified objects
	 */
	public void removeModifiedObject(RegistryObject ro) {
		synchronized (modifiedObjects) {
			modifiedObjects.remove(ro);
		}
	}

	/**
	 * Saves one or more Objects to the registry. An object may be a
	 * RegistryObject. If an object is not in the registry, then it is created
	 * in the registry. If it already exists in the registry and has been
	 * modified, then its state is updated (replaced) in the registry.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @return BulkResponse containing the Collection of keys for those objects
	 *         that were saved successfully and any SaveException that was
	 *         encountered in case of partial commit.
	 */
	public BulkResponse saveObjects(@SuppressWarnings("rawtypes") Collection objects) throws JAXRException {

		return this.saveObjects(objects, null);
	}

	/**
	 * Saves one or more Objects to the registry. An object may be a
	 * RegistryObject. If an object is not in the registry, then it is created
	 * in the registry. If it already exists in the registry and has been
	 * modified, then its state is updated (replaced) in the registry.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @return BulkResponse containing the Collection of keys for those objects
	 *         that were saved successfully and any SaveException that was
	 *         encountered in case of partial commit.
	 */
	@SuppressWarnings("unchecked")
	public BulkResponse saveObjects(@SuppressWarnings("rawtypes") Collection objects, HashMap<String, String> slotsMap) throws JAXRException {
		if ((objects == null) || (objects.size() == 0)) {
			return new BulkResponseImpl();
		}

		/*
		if ((JAXRUtility.getFirstObject(objects) instanceof IdentifiableType) || (JAXRUtility.getFirstObject(objects) instanceof JAXBElement)) {
			ArrayList<Object> objectList = new ArrayList<Object>();
			objectList.addAll(objects);
			objects = JAXRUtility.getJAXRObjectsFromJAXBObjects(this, objectList, null);
		}
		*/
		
		ClientRequestContext context = new ClientRequestContext(
				"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:saveObjects", null);
		context.setSlotsMap(slotsMap);
		context.getOriginalObjects().addAll(objects);

		String pad = "";

		for (Iterator<?> it = objects.iterator(); it.hasNext();) {
			Object obj = it.next();
			processObject(context, obj, pad);
		}

		if (log.isDebugEnabled()) {
			log.debug("candidateSubmitObjects = " + context.getCandidateSubmitObjects());
			log.debug("processedObjects = " + context.getProcessedObjects());
		}

		// Return value for this method
		BulkResponse response = null;

		// Do any final pruning / processing
		finalizeSubmitObjectList(context);

		if (context.getSubmitObjectsMap() != null) {
			response = doSubmitObjectsRequestInternal(context);

			if (response.getStatus() == BulkResponse.STATUS_SUCCESS) {
				// ROs are no longer new
				markRegistryObjectsClean(context);
			}
		}

		return response;
	}

	/**
	 * Process an Object that may be a RegistryObject, RegistryObjectRef or
	 * something else (e.g. Slot).
	 * 
	 * Rules: -Do nothing for objects that are neither RegistryObjectImpl nor
	 * RegistryObjectRef (e.g. Slots) -Don't process the same object more than
	 * once
	 */
	@SuppressWarnings("unchecked")
	public void processObject(ClientRequestContext context, Object obj, String pad) throws JAXRException {
		if (log.isDebugEnabled()) {
			log.debug(pad + "processObject entered: obj = " + obj);
		}

		// Rule: Do nothing for objects that are neither RegistryObjectImpl nor
		// RegistryObjectRef (e.g. Slots)
		if (!((obj instanceof RegistryObjectImpl) || (obj instanceof RegistryObjectRef))) {
			if (log.isDebugEnabled()) {
				log.debug(pad + "processObject: skipping obj = " + obj);
			}
			return;
		}

		// Rule: Don't process the same object more than once
		if (context.getProcessedObjects().contains(obj)) {
			if (log.isDebugEnabled()) {
				log.debug(pad + "processObject: returning on already processed obj = " + obj);
			}
			return;
		} else {
			if (log.isDebugEnabled()) {
				log.debug(pad + "processObject: processedObject.add on obj = " + obj + " processedObjects.contains = "
						+ context.getProcessedObjects().contains(obj));
			}
			context.getProcessedObjects().add(obj);

			if (obj instanceof RegistryObjectRef) {
				processRegistryObjectRef(context, (RegistryObjectRef) obj, pad);
			} else if (obj instanceof RegistryObjectImpl) {
				processRegistryObject(context, (RegistryObjectImpl) obj, pad);
			}
		}

	}

	/**
	 * Process a RegistryObjectRef.
	 * 
	 * Rules: -Submit ObjectRef for references to clean objects only -Call
	 * processObject for references to modified objects
	 * 
	 * The ReferencedObject may be: a) A new or modified object b) An ObjectRef
	 * to an object in the registry
	 */
	@SuppressWarnings("unchecked")
	private void processRegistryObjectRef(ClientRequestContext context, RegistryObjectRef ref, String pad)
			throws JAXRException {
		log.debug(pad + "processRegistryObjectRef entered: ref = " + ref);

		// Get RegistryObject from the ref
		try {
			RegistryObjectImpl ro = (RegistryObjectImpl) ref.getRegistryObject("RegistryObject");
			log.debug(pad + "processRegistryObjectRef: ro = " + ro);

			if (ro != null) {
				if (!(ro.isNew() || ro.isModified())) {
					// Rule: Submit ObjectRef for references to clean objects
					log.debug(pad + "processRegistryObjectRef: candidateSubmitObjects.add for clean ref  = " + ro);
					context.getCandidateSubmitObjects().add(ref);
				} else {
					// Rule: Call processRegistryObject for references to dirty
					// objects
					// log.debug(pad +
					// "processRegistryObjectRef: recursing for dirty ref  = " +
					// ro);
					processObject(context, ro, pad);
				}
			}
		} catch (UnresolvedReferenceException e) {
			// This can happen when the RegistryObjectRef is to a remote
			// ObjectRef
			// In such cases simply submit the ObjectRef
			context.getCandidateSubmitObjects().add(ref);
		}
	}

	/**
	 * Process a RegistryObject.
	 * 
	 * Potential cases: 1) Composed object is submitted as a composed object 2)
	 * Composed object is submitted as a top level object (e.g. create
	 * Classification, call setClassifiedObject on it and save it.)
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void processRegistryObject(ClientRequestContext context, RegistryObjectImpl ro, String pad)
			throws JAXRException {
		// log.debug(pad + "processRegistryObject entered: ro = " + ro);
		log.debug(pad + "processRegistryObject: entered candidateSubmitObjects.add on ro  = " + ro);
		context.getCandidateSubmitObjects().add(ro);

		// Get and process composed objects implicitly for RegistryObjects being
		// saved
		@SuppressWarnings("rawtypes")
		HashSet _composedObjects = ro.getComposedObjects();
		context.getComposedObjects().addAll(_composedObjects);

		for (Iterator<?> composedIter = _composedObjects.iterator(); composedIter.hasNext();) {
			Object obj = composedIter.next();

			if (obj instanceof RegistryObjectImpl) {
				RegistryObjectImpl composedObj = (RegistryObjectImpl) obj;
				if (composedObj.isNew() || composedObj.isModified()) {
					// log.debug(pad + "processRegistryObject: recursing on composedObj = " + composedObj);
					processObject(context, composedObj, pad.concat(" "));
				}
			} else if (obj instanceof RegistryObjectRef) {
				// This could be the case where a remote RegistryObjectRef is
				// being saved.
				// Not sure yet that we need to do anything as
				// FederationSystemTest seems to work as is.
				@SuppressWarnings("unused")
				int i = 0;// ??
			}
		}

		// Get and process objects referenced by RegistryObject
		HashSet<?> refObjects = ro.getRegistryObjectRefs();

		if (refObjects != null) {
			for (Iterator<?> refIter = refObjects.iterator(); refIter.hasNext();) {
				Object refObj = refIter.next();

				// log.debug(pad +
				// "processRegistryObject: recursing on refObj = " + refObj);
				processObject(context, refObj, pad.concat(" "));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void finalizeSubmitObjectList(ClientRequestContext context) throws JAXRException {
		HashSet<String> roIds = new HashSet<String>();
		HashSet<String> refIds = new HashSet<String>();

		if ((context.getCandidateSubmitObjects() == null) || (context.getCandidateSubmitObjects().size() == 0)) {
			return;
		}

		for (Iterator<Object> it = context.getCandidateSubmitObjects().iterator(); it.hasNext();) {
			Object obj = it.next();
			RegistryObjectImpl ro = null;
			IdentifiableType ebIdentifiableType = null;
			if (obj instanceof RegistryObjectImpl) {
				ro = (RegistryObjectImpl) obj;

				String id = ro.getId();

				log.debug("finalizeSubmitObjectList: ro=" + ro + " modified=" + ro.isModified() + " new=" + ro.isNew());

				if ((ro.isModified()) || (ro.isNew())) {
					log.debug("finalizeSubmitObjectList: submitting " + obj);

					if (obj instanceof ExtrinsicObject) {
						ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl) obj;

						// Use getRepositoryItemInternal instead of
						// getRepositoryItem otherwise
						// we fetch RI from registry in the case where it was
						// being remove from EO
						// in this request
						DataHandler ri = eo.getRepositoryItemInternal();

						// ebXML RS spec 2.0 allows ExtrinsicObject to exist
						// w/o RepositoryItem, but not JAXR 1.0
						if (ri != null) {
							context.getRepositoryItemsMap().put(id, ri);
						}
					}

					if (obj instanceof Classification && context.getComposedObjects().contains(obj)) {
						// Special case: Classification: it should go as a
						// composed
						// object, inside its classified object. NO-OP.
					} else if (!(roIds.contains(id))) {
						roIds.add(id);
						
						ebIdentifiableType = (IdentifiableType) ro.toBindingObject();
						context.getSubmitObjectsMap().put(ebIdentifiableType, ro);
					}
				} else {
					// Not modified or new. Create an ObjectRef if not already
					// being marshalled
					if (!(refIds.contains(id))) {
						refIds.add(id);
						
						ebIdentifiableType = rimFac.createObjectRefType();
						ebIdentifiableType.setId(id);
						log.debug("finalizeSubmitObjectList: submitting ref to unmodified ro=" + ro);
						context.getSubmitObjectsMap().put(ebIdentifiableType, null);
					}
				}
			} else if (obj instanceof RegistryObjectRef) {
				RegistryObjectRef ref = (RegistryObjectRef) obj;
				// There may be multiple refs to same object.
				// Only add ObjectRef if it has not been added already
				String id = ref.getId();

				if (!(refIds.contains(id))) {
					refIds.add(id);

					ebIdentifiableType = (IdentifiableType)ref.toBindingObject();

					log.debug("finalizeSubmitObjectList: submitting ref=" + obj);
					context.getSubmitObjectsMap().put(ebIdentifiableType, null);
				}
			} else {
				throw new JAXRException(JAXRResourceBundle.getInstance().getString(
						"message.error.unexpected.ebxml.object.save", new Object[] { obj }));
			}
		}

		log.debug("finalizeSubmitObjectList: returning item count = " + context.getSubmitObjectsMap().size());
	}

	private BulkResponse doSubmitObjectsRequestInternal(ClientRequestContext context) throws JAXRException {
		BulkResponseImpl response = null;

		try {
			
			SubmitObjectsRequest ebSubmitObjectsRequest = lcmFac.createSubmitObjectsRequest();
			context.pushRegistryRequest(ebSubmitObjectsRequest);
			if (context.getSlotsMap() != null) {
				bu.addSlotsToRequest(ebSubmitObjectsRequest, context.getSlotsMap());
			}

			//RegistryObjectListType ebRegistryObjectListType = bu.getRegistryObjectListType((RegistryObjectType) context.getSubmitObjectsMap().keySet());
			@SuppressWarnings("unchecked")
			RegistryObjectListType ebRegistryObjectListType = bu.getRegistryObjectListType(context.getSubmitObjectsMap().keySet());
					
			ebSubmitObjectsRequest.setRegistryObjectList(ebRegistryObjectListType);

			response = (BulkResponseImpl) doSubmitObjectsRequest(context);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);

		} catch (javax.xml.bind.JAXBException e) {
			e.printStackTrace();
			log.debug(e);
			throw new JAXRException(e);
		} finally {
			context.popRegistryRequest();
		}

		return response;
	}

	public BulkResponse doSubmitObjectsRequest(ClientRequestContext context) throws JAXRException {
		BulkResponseImpl response = null;
		try {
			JAXRUtility.addCreateSessionSlot(context.getCurrentRegistryRequest(), regService.getConnection());
			checkCredentialInfo();

			RegistryResponseType ebResp = serverLCM.submitObjects(context);

			response = new BulkResponseImpl(this, ebResp, null);

		} catch (javax.xml.bind.JAXBException e) {
			e.printStackTrace();
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	@SuppressWarnings("unused")
	private BulkResponse doUpdateObjectsRequest(ClientRequestContext context) throws JAXRException {
		BulkResponseImpl response = null;

		try {
			org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest req = lcmFac.createUpdateObjectsRequest();
			context.pushRegistryRequest(req);
			if (context.getSlotsMap() != null) {
				bu.addSlotsToRequest(req, context.getSlotsMap());
			}

			RegistryObjectListType ebRegistryObjectListType = bu.getRegistryObjectListType((RegistryObjectType) context
					.getSubmitObjectsMap().keySet());
			req.setRegistryObjectList(ebRegistryObjectListType);

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();
			RegistryResponseType ebResp = serverLCM.updateObjects(context);
			checkCredentialInfo();
			response = new BulkResponseImpl(this, ebResp, null);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		} finally {
			context.popRegistryRequest();
		}

		return response;
	}

	private void setKeysOnBulkResponse(ClientRequestContext context, BulkResponseImpl response) throws JAXRException {
		ArrayList<Key> keyList = new ArrayList<Key>();

		if ((context.getCurrentRegistryRequest() instanceof SubmitObjectsRequest)
				|| (context.getCurrentRegistryRequest() instanceof UpdateObjectsRequest)) {
			// For submits and updates only consider the context.originalObjects
			// System.err.println("originalObkjects=" +
			// context.originalObjects);
			
			Iterator<?> iter = context.getOriginalObjects().iterator();
			/*
			Collection<?> objects = context.getOriginalObjects();
			if ((JAXRUtility.getFirstObject(objects) instanceof IdentifiableType) || (JAXRUtility.getFirstObject(objects) instanceof JAXBElement)) {
				ArrayList<Object> objectList = new ArrayList<Object>();
				objectList.addAll(objects);
				objects = JAXRUtility.getJAXRObjectsFromJAXBObjects(this, objectList, null);
			}
			Iterator<?> iter = objects.iterator();
			*/

			while (iter.hasNext()) {
				IdentifiableImpl identifiable = (IdentifiableImpl) iter.next();		
				Key key = identifiable.getKey();
				keyList.add(key);
			}
		} else {
			List<String> idList = BindingUtility.getInstance().getIdsFromRequest(context.getCurrentRegistryRequest());
			Iterator<String> iter = idList.iterator();

			while (iter.hasNext()) {
				String id = iter.next();
				Key key = createKey(id);
				keyList.add(key);
			}
		}

		response.setCollection(keyList);
	}

	/**
	 * Marks all RegistryObjectImpl-s as clean, ie. not new nor modified.
	 * 
	 * @param objects
	 *            Collection of RegistryObjectImpl-s
	 */
	private void markRegistryObjectsClean(ClientRequestContext context) {
		for (@SuppressWarnings("unchecked")
		Iterator<Object> it = context.getSubmitObjectsMap().keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			Object value = context.getSubmitObjectsMap().get(key);
			if (value instanceof RegistryObjectImpl) {
				RegistryObjectImpl ro = (RegistryObjectImpl) value;
				ro.setNew(false);
				ro.setModified(false);
			}
		}
	}

	/**
	 * Sets the specified status on specified objects.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to apply status change
	 *            to.
	 * @param statusId
	 *            The id of the Concept within the canonical StatusType
	 *            ClassificationScheme.
	 * 
	 * @return a BulkResponse containing the Collection of keys for those
	 *         objects that were deprecated successfully and any JAXRException
	 *         that was encountered in case of partial commit
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse setStatusOnObjects(Collection<?> keys, String statusId) throws JAXRException {
		return setStatusOnObjects(keys, statusId, null, null);
	}

	/**
	 * Sets the status of specified objects. This is an extension request that
	 * will be adde to ebRR 3.1??
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to apply status change
	 *            to.
	 * @param statusId
	 *            The id of the Concept within the canonical StatusType
	 *            ClassificationScheme.
	 * 
	 * @return a BulkResponse containing the Collection of keys for those
	 *         objects that were deprecated successfully and any JAXRException
	 *         that was encountered in case of partial commit
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse setStatusOnObjects(Collection<?> keys, String statusId, AdhocQueryImpl query, HashMap<String, String> slotsMap)
			throws JAXRException {
		BulkResponseImpl response = null;
		List<ObjectRefType> orl = createObjectRefList(keys);

		try {
			SetStatusOnObjectsRequest req = lcmFac.createSetStatusOnObjectsRequest();
			req.setStatus(statusId);
			if (slotsMap != null) {
				bu.addSlotsToRequest(req, slotsMap);
			}

			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:setStatusOnObjects", req);
			ObjectRefListType ebObjectRefListType = rimFac.createObjectRefListType();
			ebObjectRefListType.getObjectRef().addAll(orl);
			req.setObjectRefList(ebObjectRefListType);

			if (query != null) {
				AdhocQueryType ebAdhocQueryType = (AdhocQueryType) (query).toBindingObject();
				req.setAdhocQuery(ebAdhocQueryType);
			}

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();
			RegistryResponseType ebResp = serverLCM.setStatusOnObjects(context);
			response = new BulkResponseImpl(this, ebResp, null);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	public BulkResponse deprecateObjects(@SuppressWarnings("rawtypes") Collection keys) throws JAXRException {
		return deprecateObjects(keys, null, null);
	}

	/**
	 * Deprecates one or more previously submitted objects. Deprecation marks an
	 * object as "soon to be deleted". Once an object is deprecated, the JAXR
	 * provider must not allow any new references (e.g. new Associations,
	 * Classifications and ExternalLinks) to that object to be submitted. If a
	 * client makes an API call that results in a new reference to a deprecated
	 * object, the JAXR provider must throw a java.lang.IllegalStateException
	 * within a JAXRException. However, existing references to a deprecated
	 * object continue to function normally.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be deprecated
	 * 
	 * @return a BulkResponse containing the Collection of keys for those
	 *         objects that were deprecated successfully and any JAXRException
	 *         that was encountered in case of partial commit
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse deprecateObjects(Collection<?> keys, AdhocQueryImpl query, HashMap<String, String> slotsMap) throws JAXRException {
		BulkResponseImpl response = null;
		List<ObjectRefType> orl = createObjectRefList(keys);

		try {
			org.oasis.ebxml.registry.bindings.lcm.DeprecateObjectsRequest req = lcmFac.createDeprecateObjectsRequest();
			if (slotsMap != null) {
				bu.addSlotsToRequest(req, slotsMap);
			}

			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:deprecateObjects", req);
			ObjectRefListType ebObjectRefListType = rimFac.createObjectRefListType();
			ebObjectRefListType.getObjectRef().addAll(orl);
			req.setObjectRefList(ebObjectRefListType);

			if (query != null) {
				AdhocQueryType ebAdhocQueryType = (AdhocQueryType) (query).toBindingObject();
				req.setAdhocQuery(ebAdhocQueryType);
			}

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();
			RegistryResponseType ebResp = serverLCM.deprecateObjects(context);
			response = new BulkResponseImpl(this, ebResp, null);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	public BulkResponse unDeprecateObjects(@SuppressWarnings("rawtypes") Collection keys) throws JAXRException {
		return unDeprecateObjects(keys, null, null);
	}

	/**
	 * Undeprecates one or more previously deprecated objects. If an object was
	 * not previously deprecated, it is not an error, and no exception is
	 * thrown. Once an object is undeprecated, the JAXR provider must again
	 * allow new references (e.g. new Associations, Classifications and
	 * ExternalLinks) to that object to be submitted.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be undeprecated
	 * 
	 * @return a BulkResponse containing the Collection of keys for those
	 *         objects that were deprecated successfully and any JAXRException
	 *         that was encountered in case of partial commit
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse unDeprecateObjects(Collection<?> keys, AdhocQueryImpl query, HashMap<String, String> slotsMap)
			throws JAXRException {
		BulkResponseImpl response = null;
		List<ObjectRefType> orl = createObjectRefList(keys);

		try {
			org.oasis.ebxml.registry.bindings.lcm.UndeprecateObjectsRequest req = lcmFac
					.createUndeprecateObjectsRequest();
			if (slotsMap != null) {
				bu.addSlotsToRequest(req, slotsMap);
			}

			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:unDeprecateObjects", req);

			ObjectRefListType ebObjectRefListType = rimFac.createObjectRefListType();
			ebObjectRefListType.getObjectRef().addAll(orl);
			req.setObjectRefList(ebObjectRefListType);

			if (query != null) {
				AdhocQueryType ebAdhocQueryType = (AdhocQueryType) (query).toBindingObject();
				req.setAdhocQuery(ebAdhocQueryType);
			}

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();
			RegistryResponseType ebResp = serverLCM.unDeprecateObjects(context);
			response = new BulkResponseImpl(this, ebResp, null);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	/**
	 * Deletes one or more previously submitted objects from the registry.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be deleted
	 * 
	 * @return BulkResponse with success status or list of exceptions.
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * @param keys
	 */
	public BulkResponse deleteObjects(@SuppressWarnings("rawtypes") Collection keys) throws JAXRException {
		return deleteObjects(keys, null, null, BindingUtility.CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll);
	}

	public BulkResponse deleteObjects(Collection<?> keys, HashMap<String, String> slotsMap) throws JAXRException {
		return deleteObjects(keys, null, slotsMap, null);
	}

	/**
	 * Deletes one or more previously submitted objects from the registry,
	 * according to the specified deletion scope. Add to JAXR 2.0 as
	 * deleteObjects(Collection keys, String deletionScope)??
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be deleted
	 * @param deletionScope
	 *            the canonical deletion scope code
	 * 
	 * @return BulkResponse with success status or list of exceptions.
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * @param keys
	 */
	public BulkResponse deleteObjects(Collection<?> keys, HashMap<String, String> slotsMap, String deletionScope) throws JAXRException {
		return deleteObjects(keys, null, slotsMap, deletionScope);
	}

	/**
	 * Deletes one or more previously submitted objects from the registry,
	 * according to the specified deletion scope. Add to JAXR 2.0 as
	 * deleteObjects(Collection keys, String deletionScope)??
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be deleted
	 * @param deletionScope
	 *            the canonical deletion scope code
	 * 
	 * @return BulkResponse with success status or list of exceptions.
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * @param keys
	 */
	public BulkResponse deleteObjects(Collection<?> keys, AdhocQueryImpl query, HashMap<String, String> slotsMap, String deletionScope)
			throws JAXRException {
		BulkResponseImpl response = null;
		List<ObjectRefType> orl = createObjectRefList(keys);
		boolean isCallersUserBeingDeleted = isCallersUserBeingDeleted(keys);
		try {
			org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest req = lcmFac.createRemoveObjectsRequest();

			if (slotsMap != null) {
				bu.addSlotsToRequest(req, slotsMap);
			}

			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:deleteObjects", req);
			req.setDeletionScope(deletionScope);
			ObjectRefListType ebObjectRefListType = rimFac.createObjectRefListType();

			ebObjectRefListType.getObjectRef().addAll(orl);
			req.setObjectRefList(ebObjectRefListType);

			if (query != null) {
				
				AdhocQueryType ebAdhocQueryType = (AdhocQueryType) (query).toBindingObject();
				req.setAdhocQuery(ebAdhocQueryType);
			}

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();
			RegistryResponseType ebResp = serverLCM.removeObjects(context);
			response = new BulkResponseImpl(this, ebResp, null);
			// If there are no exceptions and isCallersUserToBeDeleted is
			// 'true',
			// then clear the credentials on the Connection object. Failure to
			// do
			// so will result in the Connection continuing to use the deleted
			// credentials.
			if (response.getExceptions() == null && isCallersUserBeingDeleted) {
				regService.getConnection().logoff();
			}
			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	private boolean isCallersUserBeingDeleted(Collection<?> keys) throws JAXRException {
		boolean isCallersUserToBeDeleted = false;
		User user = ((DeclarativeQueryManagerImpl) regService.getDeclarativeQueryManager()).getCallersUser();
		String userId = user.getKey().getId();
		if ((user != null) && (keys != null)) {
			Iterator<?> keyItr = keys.iterator();
			while (keyItr.hasNext()) {
				Key key = (Key) keyItr.next();
				String objectId = key.getId();
				if (objectId.equalsIgnoreCase(userId)) {
					isCallersUserToBeDeleted = true;
					break;
				}
			}
		}
		return isCallersUserToBeDeleted;
	}

	/**
	 * Deletes one or more previously submitted objects from the registry using
	 * the object keys and a specified objectType attribute.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be deleted
	 * @param objectType
	 *            the objectType attribute for the objects to be deleted
	 * 
	 * @return BulkResponse with success status or list of exceptions.
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse deleteObjects(@SuppressWarnings("rawtypes") Collection keys, String objectType) throws JAXRException {
		return deleteObjects(keys);
	}

	public BulkResponse approveObjects(Collection<?> keys) throws JAXRException {
		return approveObjects(keys, null, null);
	}

	/**
	 * Approve one or more previously submitted objects from the registry.
	 * //TODO: Add to JAXR 2.0 API
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 1 </B>
	 * </DL>
	 * 
	 * @return BulkResponse containing the Collection of keys for those objects
	 *         that were approved successfully and any RegistryException that
	 *         was encountered in case of partial commit.
	 */
	public BulkResponse approveObjects(Collection<?> keys, AdhocQueryImpl query, HashMap<String, String> slotsMap) throws JAXRException {
		BulkResponseImpl response = null;
		List<ObjectRefType> orl = createObjectRefList(keys);

		try {
			org.oasis.ebxml.registry.bindings.lcm.ApproveObjectsRequest req = lcmFac.createApproveObjectsRequest();
			if (slotsMap != null) {
				bu.addSlotsToRequest(req, slotsMap);
			}

			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:approveObjects", req);

			ObjectRefListType ebObjectRefListType = rimFac.createObjectRefListType();
			ebObjectRefListType.getObjectRef().addAll(orl);
			req.setObjectRefList(ebObjectRefListType);

			if (query != null) {
				AdhocQueryType ebAdhocQueryType = (AdhocQueryType) (query).toBindingObject();
				req.setAdhocQuery(ebAdhocQueryType);
			}

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();
			RegistryResponseType ebResp = serverLCM.approveObjects(context);
			response = new BulkResponseImpl(this, ebResp, null);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	/*
	 * Extension request.
	 */
	public RegistryResponseHolder extensionRequest(RegistryRequestType req, HashMap<String, Object> idToRepositoryItemsMap)
			throws JAXRException {
		RegistryResponseHolder respHolder = null;
		try {
			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:extensionRequest", req);
			context.setRepositoryItemsMap(idToRepositoryItemsMap);
			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			checkCredentialInfo();

			respHolder = serverLCM.extensionRequest(context);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return respHolder;
	}

	/**
	 * Create a semantic equivalence between the two specified Concepts. This is
	 * a convenience method to create an Association with sourceObject as
	 * concept1 and targetObject as concept2 and associationType as
	 * EquivalentTo.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public void createConceptEquivalence(Concept concept1, Concept concept2) throws JAXRException {
		BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) regService.getBusinessQueryManager();
		Concept eqConcept = bqm.findConceptByPath("/"
				+ BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" + "EquivalentTo");
		Association assoc = createAssociation(concept2, eqConcept);
		concept1.addAssociation(assoc);

		// ??eeg save assoc to Registry or is an attribute of the Connection??
	}

	/**
	 * Removes the semantic equivalence, if any, between the specified two
	 * Concepts. This is a convenience method to to delete any Association
	 * sourceObject as concept1 and targetObject as concept2 and associationType
	 * as EquivalentTo.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 */
	public void deleteConceptEquivalence(Concept concept1, Concept concept2) throws JAXRException {
		// Write your code here
	}

	/**
	 * Returns the parent RegistryService that created this object.
	 * 
	 * <p>
	 * <DL>
	 * <DT><B>Capability Level: 0 </B>
	 * </DL>
	 * 
	 * @return the parent RegistryService
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 * 
	 * @associates <{javax.xml.registry.RegistryService}>
	 */
	public RegistryService getRegistryService() throws JAXRException {
		return regService;
	}

	// ??Add to level 1 API for JAXR 2.0
	/**
	 * Changes the owner of one or more objects.
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be
	 * @param newOwner
	 *            a Collection of keys for the objects to be deprecated
	 * 
	 * @return a BulkResponse containing the Collection of keys for those
	 *         objects that had their owner changed successfully and any
	 *         JAXRException that was encountered in case of partial commit
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse changeObjectsOwner(Collection<?> keys, String newOwner) throws JAXRException {
		return changeObjectsOwner(keys, null, newOwner);
	}

	// ??Add to level 1 API for JAXR 2.0
	/**
	 * Changes the owner of one or more objects.
	 * 
	 * @param keys
	 *            a Collection of keys for the objects to be
	 * @param newOwner
	 *            a Collection of keys for the objects to be deprecated
	 * 
	 * @return a BulkResponse containing the Collection of keys for those
	 *         objects that had their owner changed successfully and any
	 *         JAXRException that was encountered in case of partial commit
	 * 
	 * @throws JAXRException
	 *             if the JAXR provider encounters an internal error
	 */
	public BulkResponse changeObjectsOwner(Collection<?> keys, AdhocQueryImpl query1, String newOwner)
			throws JAXRException {
		BulkResponseImpl response = null;
		@SuppressWarnings("unused")
		List<ObjectRefType> orl = createObjectRefList(keys);

		try {
			DeclarativeQueryManager dqm = getRegistryService().getDeclarativeQueryManager();

			org.oasis.ebxml.registry.bindings.lcm.RelocateObjectsRequest req = lcmFac.createRelocateObjectsRequest();
			ClientRequestContext context = new ClientRequestContext(
					"it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl:changeObjectsOwner", req);

			// Create and set query
			String queryStr = "SELECT ro.* from RegistryObject ro WHERE ";

			Iterator<?> iter = keys.iterator();

			while (iter.hasNext()) {
				String roID = ((Key) iter.next()).getId();
				queryStr += "ro.id = '" + roID + "'";
				if (iter.hasNext()) {
					queryStr += " OR ";
				}
			}

			AdhocQueryType ebAdhocQueryType = bu.createAdhocQueryType(queryStr);
			req.setAdhocQuery(ebAdhocQueryType);

			// Set owner at source
			ObjectRefType sourceOwnerRef = rimFac.createObjectRefType();
			sourceOwnerRef.setId(it.cnr.icar.eric.common.Utility.getInstance().createId()); // Just
																								// a
																								// dummy
																								// id
																								// for
																								// now
																								// will
																								// do.
			req.setOwnerAtSource(sourceOwnerRef);

			// Set owner at destination
			ObjectRefType newOwnerObj = rimFac.createObjectRefType();
			newOwnerObj.setId(newOwner);
			req.setOwnerAtDestination(newOwnerObj);

			// Get registry
			String registryQueryStr = "SELECT r.* from Registry r ";

			Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, registryQueryStr);

			// make JAXR request
			javax.xml.registry.BulkResponse registryResponse = dqm.executeQuery(query);

			JAXRUtility.checkBulkResponse(registryResponse);

			// TODOD: Following is dangerous assumption as there may be replica
			// Registry instances
			// representing other registries. SHould iterate over all and find
			// the first one
			// where home attribute is undefined.
			RegistryObject registry = (RegistryObject) JAXRUtility.getFirstObject(registryResponse.getCollection());

			ObjectRefType registryRef = rimFac.createObjectRefType();
			registryRef.setId(registry.getKey().getId());
			req.setSourceRegistry(registryRef);
			req.setDestinationRegistry(registryRef);

			JAXRUtility.addCreateSessionSlot(req, regService.getConnection());
			// Submit the request
			RegistryResponseType ebResp = serverLCM.relocateObjects(context);
			response = new BulkResponseImpl(this, ebResp, null);

			// Now setCollection with ids of objects saved
			setKeysOnBulkResponse(context, response);
		} catch (javax.xml.bind.JAXBException e) {
			log.debug(e);
			throw new JAXRException(e);
		}

		return response;
	}

	/**
	 * @param keys
	 *            Collection of objects which are typically Key-s. Non Key
	 *            objects are ignored.
	 * 
	 * @return an ObjectRefList binding object representing the list of unique
	 *         Keys
	 */
	private List<ObjectRefType> createObjectRefList(Collection<?> keys) throws JAXRException {
		ArrayList<ObjectRefType> orl = new ArrayList<ObjectRefType>();

		// Used to prevent duplicate keys from being sent
		HashSet<String> processedIds = new HashSet<String>();
		processedIds.add(null);

		if (keys != null) {
			for (Iterator<?> it = keys.iterator(); it.hasNext();) {
				Object obj = it.next();

				if (obj instanceof KeyImpl) {
					KeyImpl key = (KeyImpl) obj;
					String id = key.getId();

					if (!processedIds.contains(id)) {
						processedIds.add(id);

						ObjectRefType ebObjectRefType = rimFac.createObjectRefType();
						ebObjectRefType.setId(id);
						orl.add(ebObjectRefType);
					}
				}
			}
		}

		return orl;
	}

	private void initializeObjectTypesMap() {
		try {
			DeclarativeQueryManager dqm = getRegistryService().getDeclarativeQueryManager();
//			String queryStr = "SELECT children.* FROM ClassificationNode children, ClassificationNode parent where (parent.path LIKE '/"
//					+ BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
//					+ "/RegistryObject%') AND (parent.path NOT LIKE '/"
//					+ BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_ObjectType
//					+ "/RegistryObject/ExtrinsicObject/%') AND parent.id = children.parent";
//			Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
			
			Query query = SQLQueryProvider.initializeObjectTypesMap(dqm);
			
			BulkResponse resp = dqm.executeQuery(query);

			if ((resp != null) && (!(resp.getStatus() == JAXRResponse.STATUS_SUCCESS))) {
				Collection<?> exceptions = resp.getExceptions();

				if (exceptions != null) {
					Iterator<?> iter = exceptions.iterator();

					while (iter.hasNext()) {
						Exception e = (Exception) iter.next();
						e.printStackTrace();
					}
				}

				return;
			}

			objectTypesMap = new HashMap<String, Concept>();

			Collection<?> concepts = resp.getCollection();
			Iterator<?> iter = concepts.iterator();

			while (iter.hasNext()) {
				Concept concept = (Concept) iter.next();
				String value = concept.getValue();

				if (value.equals("ClassificationNode")) {
					value = "Concept";
				}

				objectTypesMap.put(value, concept);
			}
		} catch (JAXRException e) {
			e.printStackTrace();
		}
	}

	public HashMap<String, Concept> getObjectTypesMap() {
		if (objectTypesMap == null) {
			initializeObjectTypesMap();
		}

		return objectTypesMap;
	}

	void checkCredentialInfo() {
		try {
			CredentialInfo currentCredentialInfo = (regService.getConnection()).getCredentialInfo();
			if (currentCredentialInfo != credentialInfo) {
				setCredentialInfo(currentCredentialInfo);
			}
		} catch (JAXRException ex) {
			log.warn(JAXRResourceBundle.getInstance().getString("message.CouldNotCheckCredentials"), ex);
		}
	}

	void setCredentialInfo(CredentialInfo credentialInfo) throws JAXRException {
		boolean localCall = ((((RegistryServiceImpl) this.getRegistryService()).getConnection()))
				.isLocalCallMode();
		if (localCall) {
			serverLCM = new LifeCycleManagerLocalProxy(
					(regService.getConnection()).getQueryManagerURL(), credentialInfo);
		} else {
			serverLCM = new LifeCycleManagerSOAPProxy(
					(regService.getConnection()).getQueryManagerURL(), credentialInfo);
		}
		this.credentialInfo = credentialInfo;
	}

}

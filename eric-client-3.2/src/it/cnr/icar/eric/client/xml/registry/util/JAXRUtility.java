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
package it.cnr.icar.eric.client.xml.registry.util;

import it.cnr.icar.eric.client.xml.registry.ConnectionFactoryImpl;
import it.cnr.icar.eric.client.xml.registry.InfomodelFactory;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.ObjectCache;
import it.cnr.icar.eric.client.xml.registry.RegistryServiceImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AssociationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.AuditableEventImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ClassificationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ClassificationSchemeImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ConceptImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExternalIdentifierImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExternalLinkImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.FederationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.OrganizationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.PersonImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryPackageImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceBindingImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ServiceImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SpecificationLinkImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.SubscriptionImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.UserImpl;
import it.cnr.icar.eric.common.BindingUtility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.PersonType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;

/**
 * Miscellaneous utility methods useful to JAXR client programs.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class JAXRUtility {

    private static Properties bundledProperties = null;
    private static final Log log = LogFactory.getLog(JAXRUtility.class);
    private static InfomodelFactory imFactory = InfomodelFactory.getInstance();


    /**
     * Retrieves id from RegistryObject.
     *
     * @param obj
     * @return
     * @throws JAXRException
     */
    public static String toId(Object obj) throws JAXRException {
        if (obj instanceof RegistryObject) {
            RegistryObject ro = (RegistryObject) obj;
            return ro.getKey().getId();
        } else {
            return "[toId: not a RegistryObject, obj=" + obj + "]";
        }
    }

    /**
     * Retrieves first object from a Collection.
     *
     * @param col
     * @return
     */
    public static Object getFirstObject(Collection<?> col) {
        if (col == null) {
            return null;
        }
        Iterator<?> it = col.iterator();
        if (!it.hasNext()) {
            return null;
        }
        return it.next();
    }

    /**
     * Convert JAXB generated java binding objects for RIM classes to equivalent JAXR Objects.
     * This version does not set repositoryItemMap and is therefor deprecated.
     *
     * @deprecated As of release 3.0-final, replaced by {@link #getJAXRObjectsFromJAXBObjects(LifeCycleManagerImpl, List, Map)}
     */
    public static List<RegistryObjectImpl> getJAXRObjectsFromJAXBObjects(LifeCycleManagerImpl lcm, List<?> jaxbObjects) throws JAXRException {
        return getJAXRObjectsFromJAXBObjects(lcm, jaxbObjects, null);
    }

    /**
     * Convert JAXB generated java binding objects for RIM classes to equivalent JAXR Objects.
     * Also sets the repositoryItemMap.
     *
     */
    @SuppressWarnings("unchecked")
	public static List<RegistryObjectImpl> getJAXRObjectsFromJAXBObjects(LifeCycleManagerImpl lcm, List<?> jaxbObjects, Map<?, ?> repositoryItemsMap) throws JAXRException {
        ArrayList<RegistryObjectImpl> jaxrObjects = new ArrayList<RegistryObjectImpl>();
        ObjectCache objCache = ((RegistryServiceImpl) (lcm.getRegistryService())).getObjectCache();


        Iterator<?> iter = jaxbObjects.iterator();
        while (iter.hasNext()) {
        	// take ComplexType from Element
        	
        	// switch on each object in list to access ComplexType
        	Object ebElementOrComplexType = iter.next();
        	IdentifiableType ebIdentifiableType = null;
        	if (ebElementOrComplexType instanceof JAXBElement<?>) {
              ebIdentifiableType = ((JAXBElement<IdentifiableType>) ebElementOrComplexType).getValue();

			} else if (ebElementOrComplexType instanceof IdentifiableType) {
	              ebIdentifiableType = (IdentifiableType) ebElementOrComplexType;
			}
        	
//            IdentifiableType ebIdentifiableType = (IdentifiableType) o;
            
            if (ebIdentifiableType instanceof ClassificationSchemeType) {
                ClassificationSchemeImpl scheme = new ClassificationSchemeImpl(lcm, (ClassificationSchemeType) ebIdentifiableType);
                objCache.putRegistryObject(scheme);
                jaxrObjects.add(scheme);
                continue;
            } else if (ebIdentifiableType instanceof ClassificationType) {
                ClassificationType classType = (ClassificationType)ebIdentifiableType;
                // Get classified RegistryObject
                String classObjId = classType.getClassifiedObject();
                RegistryObject classifiedRO = lcm.getRegistryService()
                                       .getBusinessQueryManager()
                                       .getRegistryObject(classObjId);

                ClassificationImpl cls = new ClassificationImpl(lcm, (ClassificationType) ebIdentifiableType, classifiedRO);
                objCache.putRegistryObject(cls);
                jaxrObjects.add(cls);

                continue;
            } else if (ebIdentifiableType instanceof OrganizationType) {
                OrganizationImpl org = new OrganizationImpl(lcm, (OrganizationType) ebIdentifiableType);
                objCache.putRegistryObject(org);
                jaxrObjects.add(org);

                continue;
            } else if (ebIdentifiableType instanceof AssociationType1) {
                AssociationImpl ass =
                    imFactory.createAssociation(lcm, (AssociationType1)ebIdentifiableType);
                objCache.putRegistryObject(ass);
                jaxrObjects.add(ass);

                continue;
            } else if (ebIdentifiableType instanceof RegistryPackageType) {
                RegistryPackageImpl pkg = new RegistryPackageImpl(lcm,
                        (RegistryPackageType) ebIdentifiableType);
                objCache.putRegistryObject(pkg);
                jaxrObjects.add(pkg);

                continue;
            } else if (ebIdentifiableType instanceof ExternalLinkType) {
                ExternalLinkImpl extLink = new ExternalLinkImpl(lcm,
                        (ExternalLinkType) ebIdentifiableType);
                objCache.putRegistryObject(extLink);
                jaxrObjects.add(extLink);

                continue;
            } else if (ebIdentifiableType instanceof ExternalIdentifierType) {
                //Pass parent object and not null
                ExternalIdentifierType ebExtIdentifier = (ExternalIdentifierType) ebIdentifiableType;
                @SuppressWarnings("unused")
				String parentId = ebExtIdentifier.getRegistryObject();
                ExternalIdentifierImpl extIdentifier = new ExternalIdentifierImpl(lcm,
                        (ExternalIdentifierType) ebIdentifiableType);
                objCache.putRegistryObject(extIdentifier);
                jaxrObjects.add(extIdentifier);

                continue;
            } else if (ebIdentifiableType instanceof ExtrinsicObjectType) {
                ExtrinsicObjectImpl extrinsicObj =
                    imFactory.createExtrinsicObject(lcm, (ExtrinsicObjectType)ebIdentifiableType);
                objCache.putRegistryObject(extrinsicObj);
                jaxrObjects.add(extrinsicObj);

                if ((repositoryItemsMap != null) && (repositoryItemsMap.containsKey(extrinsicObj.getKey().getId()))) {
                   DataHandler repositoryItem = (DataHandler)repositoryItemsMap.get(extrinsicObj.getKey().getId());
                   extrinsicObj.setRepositoryItemInternal(repositoryItem);
                }

                continue;
            } else if (ebIdentifiableType instanceof AdhocQueryType) {
                    AdhocQueryImpl adhocQueryObj = new AdhocQueryImpl(lcm,
                        (AdhocQueryType) ebIdentifiableType);
                objCache.putRegistryObject(adhocQueryObj);
                jaxrObjects.add(adhocQueryObj);

                continue;
            } else if (ebIdentifiableType instanceof ServiceType) {
                ServiceImpl service = new ServiceImpl(lcm, (ServiceType) ebIdentifiableType);
                objCache.putRegistryObject(service);
                jaxrObjects.add(service);

                continue;
            } else if (ebIdentifiableType instanceof ServiceBindingType) {
                ServiceBindingImpl binding = new ServiceBindingImpl(lcm,
                        (ServiceBindingType) ebIdentifiableType);
                objCache.putRegistryObject(binding);
                jaxrObjects.add(binding);

                continue;
            } else if (ebIdentifiableType instanceof SubscriptionType) {
                SubscriptionImpl subscription = new SubscriptionImpl(lcm,
                        (SubscriptionType) ebIdentifiableType);
                objCache.putRegistryObject(subscription);
                jaxrObjects.add(subscription);

                continue;
            } else if (ebIdentifiableType instanceof SpecificationLinkType) {
                SpecificationLinkImpl specLink = new SpecificationLinkImpl(lcm,
                        (SpecificationLinkType) ebIdentifiableType);
                objCache.putRegistryObject(specLink);
                jaxrObjects.add(specLink);

                continue;
            } else if (ebIdentifiableType instanceof ClassificationNodeType) {
                ConceptImpl concept = new ConceptImpl(lcm,
                        (ClassificationNodeType) ebIdentifiableType);
                objCache.putRegistryObject(concept);
                jaxrObjects.add(concept);

                continue;
            } else if (ebIdentifiableType instanceof ObjectRefType) {
                // ObjectRef-s are processed by leaf components
                continue;
            } else if (ebIdentifiableType instanceof AuditableEventType) {
                AuditableEventImpl ae = new AuditableEventImpl(lcm,
                        (AuditableEventType) ebIdentifiableType);
                objCache.putRegistryObject(ae);
                jaxrObjects.add(ae);

                continue;
            } else if (ebIdentifiableType instanceof UserType) {
                UserImpl user = new UserImpl(lcm, (UserType) ebIdentifiableType);
                objCache.putRegistryObject(user);
                jaxrObjects.add(user);

                continue;
            } else if (ebIdentifiableType instanceof PersonType) {
                PersonImpl person = new PersonImpl(lcm, (PersonType) ebIdentifiableType);
                objCache.putRegistryObject(person);
                jaxrObjects.add(person);

                continue;
            } else if (ebIdentifiableType instanceof RegistryType) {
                RegistryImpl registry = new RegistryImpl(lcm,
                        (RegistryType) ebIdentifiableType);
                objCache.putRegistryObject(registry);
                jaxrObjects.add(registry);

                continue;
            } else if (ebIdentifiableType instanceof FederationType) {
                FederationImpl federation = new FederationImpl(lcm,
                        (FederationType) ebIdentifiableType);
                objCache.putRegistryObject(federation);
                jaxrObjects.add(federation);

                continue;
            }
            log.error(JAXRResourceBundle.getInstance().getString("message.NotImplemented", new Object[]{ebIdentifiableType.getClass().getName()}));
        }

        return jaxrObjects;
    }

    /**
     * Throws exception if BulkResponse contains any exceptions.
     *
     * @param response
     * @throws JAXRException
     */
    public static void checkBulkResponse(BulkResponse response)
    throws JAXRException {
        Collection<?> exes = response.getExceptions();
        if (exes == null) {
            return;
        }
        throw new JAXRException((JAXRException) getFirstObject(exes));
    }

    /**
     * Retrieves owner of RegistryObject.
     *
     * @param ro RegistryObject to get the owner of
     * @return owner, ie. creator or null if this is a new RegistryObject
     */
    public static User getOwner(RegistryObject ro) throws JAXRException {
        return ((it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl)ro).getOwner();
    }


    public static Collection<Key> getKeysFromObjects(Collection<?> registryObjects) throws JAXRException {
        ArrayList<Key> keys = new ArrayList<Key>();

        Iterator<?> iter = registryObjects.iterator();

        while (iter.hasNext()) {
            RegistryObject obj = (RegistryObject) iter.next();
            Key key = obj.getKey();
            keys.add(key);
        }
        return keys;
    }

    public static Properties getBundledClientProperties() {
        if (bundledProperties == null) {
            bundledProperties = new Properties();

            BufferedReader in = null;

            try {
                InputStream propInputStream = JAXRUtility.class.getClassLoader()
                                                  .getResourceAsStream("jaxr-ebxml.properties");

                if (propInputStream != null) {
                    bundledProperties.load(propInputStream);
                }
            } catch (Throwable t) {
                log.error(JAXRResourceBundle.getInstance().getString("message.ProblemReadingBundledEricpropertiesFile"));
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ex) {
                    	in = null;
                    }
                }
            }
        }
        return bundledProperties;
    }

    /**
     * Adds slots specified by slotsMap to RegistryObject ro.
     *
     */
    public static void addSlotsToRegistryObject(RegistryObject ro, Map<?, ?> slotsMap) throws JAXRException {
        ArrayList<Slot> slots = new ArrayList<Slot>();

        Iterator<?> iter = slotsMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object slotValue = slotsMap.get(key);

            Slot slot = null;

            //slotValue must either be a String or a Collection of Strings
            if (slotValue instanceof String) {
                slot = ro.getLifeCycleManager().createSlot((String)key, (String)slotValue, (String)null);
            } else if (slotValue instanceof Collection) {
                Collection<?> c = (Collection<?>)slotValue;
                slot = ro.getLifeCycleManager().createSlot((String)key, c, (String)null);
            } else {
                //??throw new IllegalArgumentException(resourceBundle.getString("message.addingParameter",
                //        new String[]{slotValue.getClass().getName()}));
            }
            if (slot != null) {
                slots.add(slot);
            }
        }

        ro.addSlots(slots);
    }

    /**
     * This method is used to add special rim:Slot to the RegistryRequestType
     * object to indicate that the server should create a secure session.
     *
     * @param req
     *   The RegistryRequestType to which the rim:Slot will be added
     * @param connection
     *   A javax.registry.Connection implementation.  This method will use this
     *   class to determine if a secure session should be created:
     *   If the connection's credential set is empty, do not create session.
     *   Empty credentials means the user has not authenticated. Thus, the
     *   user defaults to RegistryGuest and secure session is not needed.
     */
    public static void addCreateSessionSlot(RegistryRequestType req,
        Connection connection)
        throws JAXBException {

        // check if there are credentials.  If not, there is no need for a
        // secure session as the UserType is RegistryGuest
        boolean isEmpty = true;
        try {
            isEmpty = connection.getCredentials().isEmpty();
        } catch (JAXRException ex) {
            log.error(JAXRResourceBundle.getInstance().getString("message.CouldNotGetCredentialsFromConnectionObjectPresumeCorruptedCredentials"));
        }

        // This property allows the user to control whether or not to use
        // secure sessions.  Default is 'true'.
        String createSecureSession =
            ProviderProperties.getInstance()
                              .getProperty("jaxr-ebxml.security.createSecureSession",
                                           "true");
        if ((!isEmpty) && createSecureSession.equalsIgnoreCase("true")) {
            HashMap<String, String> slotMap = new HashMap<String, String>();
            slotMap.put("urn:javax:xml:registry:connection:createHttpSession", "true");
            BindingUtility.getInstance().addSlotsToRequest(req, slotMap);
        }
    }

    /**
     * This method is used to create an instance of the
     * javax.xml.registry.ConnectionFactory interface. By default, it will create
     * an instance of it.cnr.icar.eric.client.xml.registry.ConnectionFactoryImpl
     *
     * @return
     *   A it.cnr.icar.eric.client.xml.registry.ConnectionFactoryImpl instance
     * @see
     *   it.cnr.icar.eric.client.xml.registry.ConnectionFactoryImpl
     */
    public static ConnectionFactory getConnectionFactory() throws JAXRException {
        return new ConnectionFactoryImpl();
    }

}

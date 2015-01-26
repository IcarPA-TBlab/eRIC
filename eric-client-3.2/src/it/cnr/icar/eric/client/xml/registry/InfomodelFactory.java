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

import it.cnr.icar.eric.client.xml.registry.infomodel.AssociationImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ExtrinsicObjectImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;

/**
 * A factory class to create infomodel objects and handle class level JAXR
 * extensions. This should load the configured extensions during its
 * initialization and later be used to instantiate the extensible objects.
 * <p>
 * //TODO: Make it generic, load all infomodel constructors
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class InfomodelFactory {

    private static final Class<LifeCycleManagerImpl> LCMIMPL_CLASS = 
        LifeCycleManagerImpl.class;
    private static final Class<ExtrinsicObjectType> EXTRINSIC_OBJECT_BINDING_CLASS = 
        ExtrinsicObjectType.class;
    private static final Class<AssociationType1> ASSOCIATION_BINDING_CLASS = 
        AssociationType1.class;
    
    // Log
    private static final Log log = LogFactory.getLog(InfomodelFactory.class);
    
    // Singleton instance
    private static InfomodelFactory instance;

    // I18n util (ResourceBundle)
    protected static JAXRResourceBundle i18nUtil = null;

    // RegistryObject constructors w/ 1 arg (lcm), keyed by uuid
    @SuppressWarnings("rawtypes")
	protected Map<String, Constructor> extConstructors1Arg;
    // RegistryObject constructors w/ 1 arg (lcm, bind), keyed by uuid
    @SuppressWarnings("rawtypes")
	protected Map<String, Constructor> extConstructors2Args;
    // Type nickname -> type concept uid
    protected Map<String, String> extNicknames;

    /** Creates a new instance of InfomodelFactory */
    protected InfomodelFactory() {
        init();
    }

    /**
     * Implement Singleton class.
     */
    public synchronized static InfomodelFactory getInstance() {
        if (instance == null) {
            instance = new InfomodelFactory();
        }
        return instance;
    }

    /* Load class mappings from properties. */
    @SuppressWarnings("rawtypes")
	private void init() {
        i18nUtil = JAXRResourceBundle.getInstance();
        // initialize maps
        extConstructors1Arg = new TreeMap<String, Constructor>();
        extConstructors2Args = new TreeMap<String, Constructor>();
        extNicknames = new TreeMap<String, String>();
        
        // get mapped constructors. log.warn on error and ignore.
        ProviderProperties props = ProviderProperties.getInstance();
        Enumeration<?> keys = props.getProperties().propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.startsWith("jaxr-ebxml.extensionclass.")) {
                if (key.startsWith("jaxr-ebxml.extensionclass.association.")) {
                    addAssConstructors(
                        key.substring("jaxr-ebxml.extensionclass.association.".length()),
                        props.getProperty(key)
                    );
                } else if (key.startsWith("jaxr-ebxml.extensionclass.extrinsicobject.")) {
                    addEOConstructors(
                        key.substring("jaxr-ebxml.extensionclass.extrinsicobject.".length()),
                        props.getProperty(key)
                    );
                }
            }
        }
    }
    
    /** 
     * Process className, first token before ',' is actual className, other tokens
     * are treated as nicknames and added to nicknames map.
     */
    private String getRealClassName(String typeName, String className) {
        String classNameSplit[] = className.split(",");
        for (int i = 1; i < classNameSplit.length; i++) {
            extNicknames.put(classNameSplit[i], typeName);
        }
        
        return classNameSplit[0];
    }
    
    /** init method for ExtrinsicObject extensions */
    private void addAssConstructors(String typeName, String className) {
        try {
            className = getRealClassName(typeName, className);
            
            Class<?> clazz = Class.forName(className);
            Constructor<?> c1 = clazz.getConstructor(new Class[] {
                LCMIMPL_CLASS,
            });
            Constructor<?> c2 = clazz.getConstructor(new Class[] {
                LCMIMPL_CLASS,
                ASSOCIATION_BINDING_CLASS
            });
            extConstructors1Arg.put(typeName, c1);
            extConstructors2Args.put(typeName, c2);
            if (log.isDebugEnabled()) {
                log.debug(JAXRResourceBundle.getInstance().
			  getString("message.RegisteredAssociationExtensionClassForType",
				    new Object[]{className, typeName}));
            }
        } catch (Exception e) {
            Object[] objs = { className, typeName };
            String msg = i18nUtil.getString("extension.load.failure", objs);
            log.warn(msg, e);
        }
    }

    /** init method for Association extensions */
    private void addEOConstructors(String typeName, String className) {
        try {
            className = getRealClassName(typeName, className);

            Class<?> clazz = Class.forName(className);
            Constructor<?> c1 = clazz.getConstructor(new Class[] {
                LCMIMPL_CLASS,
            });
            Constructor<?> c2 = clazz.getConstructor(new Class[] {
                LCMIMPL_CLASS,
                EXTRINSIC_OBJECT_BINDING_CLASS
            });
            extConstructors1Arg.put(typeName, c1);
            extConstructors2Args.put(typeName, c2);
            if (log.isDebugEnabled()) {
                log.debug(JAXRResourceBundle.getInstance().
			  getString("message.RegisteredExtrinsicObjectExtensionClassForType",
				    new Object[]{className, typeName}));
            }
        } catch (Exception e) {
            Object[] objs = { className, typeName };
            String msg = i18nUtil.getString("extension.load.failure", objs);
            log.warn(msg, e);
        }
    }

    /**
     * Getter for extension constructor w/ 1 argument (LifeCycleManagerImpl).
     *
     * @param type the value/uuid that identifies the extension
     * @return Constructor for type, if defined. Null otherwise.
     */
    public Constructor<?> getConstructor1Arg(String type) {
        Object c = extConstructors1Arg.get(type);
        if (c == null) { 
            String altType = extNicknames.get(type);
            if (altType != null) {
                c = extConstructors1Arg.get(altType);
            }
        }
        return (Constructor<?>)c;
    }
    
    /**
     * Getter for extension constructor w/ 2 arguments (LifeCycleManagerImpl,
     * binding object).
     *
     * @param type the value/uuid that identifies the extension
     * @return Constructor for type, if defined. Null otherwise.
     */
    public Constructor<?> getConstructor2Args(String type) {
        Object c = extConstructors2Args.get(type);
        if (c == null) { 
            String altType = extNicknames.get(type);
            if (altType != null) {
                c = extConstructors2Args.get(altType);
            }
        }
        return (Constructor<?>)c;
    }    

    /**
     * Creates an instance of AssociationImpl or of one of its extension, if any
     * defined for 'associationType'. Uses the constructor w/ 1 argument
     * (LifeCycleManagerImpl). In case of failure to instantiate the extension,
     * returns an instance of the default class (AssociationImpl).
     *
     * @param lcm The LifeCycleManagerImpl
     * @param type the value/uuid that identifies the extension
     * @return AssociationImpl instantiated.
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public AssociationImpl createAssociation(LifeCycleManagerImpl lcm, String associationType)
    throws JAXRException {
        if (associationType != null) {
            Constructor<?> c = extConstructors1Arg.get(associationType);
            if (c != null) {
                AssociationImpl ass = (AssociationImpl)instantiate(
                    associationType, c, new Object [] {lcm});
                if (ass != null) {
                    return ass;
                }
            }
        }
        
        // Default
        return new AssociationImpl(lcm);
    }
    
    /**
     * Creates an instance of AssociationImpl or of one of its extension, if any
     * defined for 'bind.associationType()'. Uses the constructor w/ 2 arguments
     * (LifeCycleManagerImpl, binding object). In case of failure to instantiate
     * the extension, returns an instance of the default class (AssociationImpl).
     *
     * @param lcm The LifeCycleManagerImpl
     * @param bind the binding object
     * @return AssociationImpl instantiated.
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public AssociationImpl createAssociation(LifeCycleManagerImpl lcm, AssociationType1 bind)
    throws JAXRException {
        if (bind != null) {
            String objectType = bind.getAssociationType();
            Constructor<?> c = extConstructors2Args.get(objectType);
            if (c != null) {
                AssociationImpl ass = (AssociationImpl)instantiate(
                    objectType, c, new Object [] {lcm, bind});
                if (ass != null) {
                    return ass;
                }
            }
        }
        
        // Default
        return new AssociationImpl(lcm, bind);
    }

    /**
     * Creates an instance of ExtrinsicObjectImpl or of one of its extension, if
     * any defined for 'ExtrinsicObjectType'. Uses the constructor w/ 1 argument
     * (LifeCycleManagerImpl). In case of failure to instantiate the extension,
     * returns an instance of the default class (ExtrinsicObjectImpl).
     *
     * @param lcm The LifeCycleManagerImpl
     * @param type the value/uuid that identifies the extension
     * @return ExtrinsicObjectImpl instantiated.
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ExtrinsicObjectImpl createExtrinsicObject(LifeCycleManagerImpl lcm, String objectType)
    throws JAXRException {
        if (objectType != null) {
            Constructor<?> c = extConstructors1Arg.get(objectType);
            if (c != null) {
                ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl)instantiate(
                    objectType, c, new Object [] {lcm});
                if (eo != null) {
                    return eo;
                }
            }
        }
        
        // Default
        return new ExtrinsicObjectImpl(lcm);
    }
    
    /**
     * Creates an instance of ExtrinsicObjectImpl or of one of its extension, if
     * any defined for 'ExtrinsicObjectType'. Uses the constructor w/ 2 arguments
     * (LifeCycleManagerImpl, binding object). In case of failure to instantiate
     * the extension, returns an instance of the default class (ExtrinsicObjectImpl).
     *
     * @param lcm The LifeCycleManagerImpl
     * @param bind the binding object
     * @return ExtrinsicObjectImpl instantiated.
     * @throws JAXRException if the JAXR provider encounters an internal error
     */
    public ExtrinsicObjectImpl createExtrinsicObject(LifeCycleManagerImpl lcm, ExtrinsicObjectType bind)
    throws JAXRException {
        String objectType = bind.getObjectType();
        Constructor<?> c = extConstructors2Args.get(objectType);
        if (c != null) {
            ExtrinsicObjectImpl eo = (ExtrinsicObjectImpl)instantiate(
                objectType, c, new Object [] {lcm, bind});
            if (eo != null) {
                return eo;
            }
        }

        // Default
        return new ExtrinsicObjectImpl(lcm, bind);
    }
    
    /**
     * Calls newInstance() on the constructor w/ the given initArgs. Returns the
     * new instance, or, in case of exception, log.warn and return null.
     *
     * @param objectType String, used for log only.
     * @param initArgs Objects to be used when calling newInstance().
     * @return new instance; or null in case of exception.
     */
    private Object instantiate(String objectType, Constructor<?> c, Object initArgs[]) {
        try {
            return c.newInstance(initArgs);
        } catch (Exception e) {
            Object[] objs = { c.getDeclaringClass().getName(), objectType };
            String msg = i18nUtil.getString("extension.instantiation.failure", objs);
            log.warn(msg, e);
        }
        return null;
    }
    
    /**
     * Returns the type name configured for a given nickname, if available. Nicknames
     * are additional tokens that map to a UID/Concept and could represent class or
     * interface name, for instance.
     *
     * @param nickname Any name
     * @return String type name (configured UID) for nickname or NULL if not defined.
     */
    public String getTypeName(String nickname) {
        return extNicknames.get(nickname);
    }
    
}

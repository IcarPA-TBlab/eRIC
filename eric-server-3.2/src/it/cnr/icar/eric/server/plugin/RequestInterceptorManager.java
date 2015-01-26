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
package it.cnr.icar.eric.server.plugin;

import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.spi.RequestContext;
import it.cnr.icar.eric.common.spi.RequestInterceptor;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;

/**
 * The class manages and invokes RequestInterceptor plugins.
 * 
 * @author Farrukh Najmi
 * @author Diego Ballve
 *
 */
public class RequestInterceptorManager extends AbstractPluginManager {
    private static final Log log = LogFactory.getLog(RequestInterceptorManager.class);
    private List<RequestInterceptor> interceptors = null;
    
    private static RequestInterceptorManager instance = null;            
    
    protected RequestInterceptorManager() throws RegistryException {
    }
            
    public synchronized static RequestInterceptorManager getInstance() throws RegistryException {
        if (instance == null) {
            instance = new it.cnr.icar.eric.server.plugin.RequestInterceptorManager();
        }

        return instance;
    }

    public void postProcessRequest(RequestContext context) throws RegistryException {
        List<RequestInterceptor> applicablePlugins = getApplicablePlugins((ServerRequestContext)context);
        
        Iterator<RequestInterceptor> iter = applicablePlugins.iterator();
        while (iter.hasNext()) {
            RequestInterceptor interceptor = iter.next();
            interceptor.postProcessRequest(context);
        }
    }

    public void preProcessRequest(RequestContext context) throws RegistryException {
        List<RequestInterceptor> applicablePlugins = getApplicablePlugins((ServerRequestContext)context);
        
        Iterator<RequestInterceptor> iter = applicablePlugins.iterator();
        while (iter.hasNext()) {
            RequestInterceptor interceptor = iter.next();
            interceptor.preProcessRequest(context);
        }
    }
    
    private List<RequestInterceptor> getApplicablePlugins(ServerRequestContext context) throws RegistryException {
        List<RequestInterceptor> applicablePlugins = new ArrayList<RequestInterceptor>();
        
        try {
            if (getInterceptors().size() > 0) {
                @SuppressWarnings({ "static-access", "unused" })
				String requestAction = bu.getActionFromRequest(context.getCurrentRegistryRequest());

                //Get roles associated with user associated with this RequestContext
                Set<?> subjectRoles = ServerCache.getInstance().getRoles(context);

                //Now get those RequestInterceptors whose roles are a proper subset of subjectRoles
                Iterator<RequestInterceptor> iter = getInterceptors().iterator();
                while (iter.hasNext()) {
                    RequestInterceptor interceptor = iter.next();
                    Set<?> interceptorRoles = interceptor.getRoles();
                    @SuppressWarnings("unused")
					Set<?> interceptorActions = interceptor.getActions();
                    if ((subjectRoles.containsAll(interceptorRoles))) {
                        applicablePlugins.add(interceptor);
                    }
                }
            }
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
        
        return applicablePlugins;
    }

    @SuppressWarnings("unchecked")
	public List<RequestInterceptor> getInterceptors() throws RegistryException {
	ServerRequestContext context = null;
        try {
            if (interceptors == null) {
                ArrayList<RequestInterceptor> _interceptors = new ArrayList<RequestInterceptor>();
                context =
		    new ServerRequestContext("RequestInterceptorManager:RequestInterceptorManager",
					     null);
                RegistryType registry = null;
                try {
                    registry = ServerCache.getInstance().getRegistry(context);
                } catch (ObjectNotFoundException e) {
                    //Registry instance not loaded yet
                    //Must be in db build sequence.
                    //Return empty list but dont set the interceptors var until registry instance is read
                    return new ArrayList<RequestInterceptor>();
                }

                HashMap<String, Serializable> slotsMap = bu.getSlotsFromRegistryObject(registry);
                //??Need to fix getSlotsFromRegistryObject and getSlotsFromRegistryRequest to always return slot values as List
                //Otherwise a List slot with single value gets returned as String and we get ClassCastException.
                @SuppressWarnings("static-access")
				Object o = slotsMap.get(bu.FREEBXML_REGISTRY_REGISTRY_INTERCEPTORS);
                List<String> interceptorClasses = null;
                if (o != null) {
                    if (o instanceof List) {
                        interceptorClasses = (List<String>)o;
                    } else {
                        interceptorClasses = new ArrayList<String>();
                        interceptorClasses.add((String)o);
                    }

                    //Instantiate and cache Interceptor classes
                    if (interceptorClasses != null) {
                        Iterator<String> iter = interceptorClasses.iterator();
                        while (iter.hasNext()) {
                            String className = iter.next();
                            RequestInterceptor interceptor = (RequestInterceptor)createPluginInstance(className);
                            _interceptors.add(interceptor);
                        }
                    }
                    interceptors = _interceptors;
                } else {
                    //NO interceptors configured.
                    interceptors = new ArrayList<RequestInterceptor>();
                }
            }
        } catch (Throwable e) {
            log.error(e);
            throw new RegistryException(e);
        } finally {
	    if (null != context) {
		context.rollback();
	    }
	}
        return interceptors;
    }
    
}

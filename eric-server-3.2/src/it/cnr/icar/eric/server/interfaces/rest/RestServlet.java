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
package it.cnr.icar.eric.server.interfaces.rest;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.UnimplementedException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;

/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class RestServlet extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 3204285271866257552L;
	private Log log = LogFactory.getLog(this.getClass());
    @SuppressWarnings("unused")
	private BindingUtility bu = BindingUtility.getInstance();
    private SortedMap<String, String> extendedInterfaces = new TreeMap<String, String>();
    
    /** Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            initExtendedInterfaces();
        } catch (Exception e) {
            extendedInterfaces.clear();
            log.error(ServerResourceBundle.getInstance().getString("message.FailedToInitExtendedRESTInterfaces"), e);
        }
    }

    /** Destroys the servlet.
     */
    public void destroy() {
    }

    /**
     * Initializes the 'extendedInterfaces' Map with (handler name, class name)
     * pair values read from RegistryProperties.
     */
    protected void initExtendedInterfaces() {
        // Initialize extended URLHandlers from properties
        RegistryProperties props = RegistryProperties.getInstance();
        String keyStart = "eric.server.interfaces.rest.extend.";
        Iterator<String> names = props.getPropertyNamesStartingWith(keyStart);
        while (names.hasNext()) {
            String name = names.next();
            String className = props.getProperty(name).trim();
            name = name.substring(keyStart.length());
            extendedInterfaces.put(name, className);
            log.info(ServerResourceBundle.getInstance().getString("message.AddedRESTExtensionInterface", new Object[]{name, className}));
        }
    }
    
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @SuppressWarnings("static-access")
	protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        String interfase = request.getParameter("interface");
        
        try {            
            if ((interfase == null) || interfase.equals("")) {
                //First check if it is a user defined URL since that is less expensive to match        
                UserDefinedURLHandler udefURLHandler = new UserDefinedURLHandler(request, response);
                try {
                    udefURLHandler.processGetRequest();
                    response.setStatus(response.SC_OK);
                } catch (ObjectNotFoundException e) {
                    FilePathURLHandler filePathURLHandler = new FilePathURLHandler(request, response);
                    filePathURLHandler.processGetRequest();
                    response.setStatus(response.SC_OK);
                }
            } else if (interfase.equalsIgnoreCase("QueryManager")) {
                QueryManagerURLHandler qmURLHandler = new QueryManagerURLHandler(request, response);
                qmURLHandler.processGetRequest();
                response.setStatus(response.SC_OK);
            } else if (interfase.equalsIgnoreCase("LifeCycleManager")) {
                LifeCycleManagerURLHandler lcmURLHandler = new LifeCycleManagerURLHandler(request, response);
                lcmURLHandler.processGetRequest();
                response.setStatus(response.SC_OK);
            } else if (extendedInterfaces.containsKey(interfase)) {
                String className = extendedInterfaces.get(interfase).toString();
                URLHandler handler = createURLHandlerInstance(className, request, response);
                if (handler != null) {
                    handler.processGetRequest();
                    response.setStatus(response.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid request. Unknown interface: " + interfase);
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid request. Unknown interface: " + interfase);
            }
        }
        catch (ObjectNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                e.getMessage());
        }
        catch (InvalidRequestException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                e.getMessage());
        }
        catch (UnimplementedException e) {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
                e.getMessage());
        }
        catch (RegistryException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                e.getMessage());
        }
    }

    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @SuppressWarnings("static-access")
	protected void doPost(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {
            
        String interfase = request.getParameter("interface");
        
        try {
            if (interfase.equalsIgnoreCase("QueryManager")) {
                QueryManagerURLHandler qmURLHandler = new QueryManagerURLHandler(request, response);
                qmURLHandler.processPostRequest();
            } else if (interfase.equalsIgnoreCase("LifecycleManager")) {
                LifeCycleManagerURLHandler lcmURLHandler = new LifeCycleManagerURLHandler(request, response);
                lcmURLHandler.processPostRequest();
            } else if (extendedInterfaces.containsKey(interfase)) {
                String className = extendedInterfaces.get(interfase).toString();
                URLHandler handler = createURLHandlerInstance(className, request, response);
                if (handler != null) {
                    handler.processPostRequest();
                    response.setStatus(response.SC_OK);
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid request. Unknown interface: " + interfase);
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Post methods only supported for LifeCycleManager or QueryManager!");        
            }
        }
        catch (JAXRException e) {
            //TODO:
        	return;
        }        
    }

    /** Returns the description of the servlet.
     *  @param response servlet description
     */
    public String getServletInfo() {
        return "HTTP interface implementation for freebXML Registry";
    }

    /**
     * Uses reflection to instantiate an instance of 'className' URLHandler.
     * - 'className' must be an URLHandler
     * - 'className' must have a constructor with params (HttpServletRequest, HttpServletResponse)
     * In case of any exception, logs it and returns null.
     *
     * @param className String name of the URLHandler implementation to be instantiated.
     * @param request the HttpServletRequest
     * @param response the HttpServletResponse
     * @returm an instance of 'className' or null if any Exception occurs.
     */
    private URLHandler createURLHandlerInstance(String className,
        HttpServletRequest request, HttpServletResponse response) {
        try {
            Class<?> clazz = Class.forName(className);
            Class<?> parameterTypes[] = new Class[2];
            parameterTypes[0] = HttpServletRequest.class;
            parameterTypes[1] = HttpServletResponse.class;
            Constructor<?> constr = clazz.getConstructor(parameterTypes);
            Object parameters[] = new Object[2];
            parameters[0] = request; 
            parameters[1] = response; 
            Object handler = constr.newInstance(parameters);
            return (URLHandler)handler;
        } catch (Exception e) {
            log.error(ServerResourceBundle.getInstance().getString("message.FailedToCreateExtendedURLHandlerForMessage",
                                                                    new Object[]{className, e.getClass().getName(), e.getMessage()}), e);
            return null;
        }
    }
    
}

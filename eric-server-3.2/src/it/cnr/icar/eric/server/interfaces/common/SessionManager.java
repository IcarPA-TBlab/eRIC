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
package it.cnr.icar.eric.server.interfaces.common;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Manages user sessions for ebXML Registry 
 *
 * @author Paul Sterk
 */
public class SessionManager {
    
    private static final Log log = LogFactory.getLog(SessionManager.class);
    public static final String HTTP_SESSION_USER = "it.cnr.icar.eric.server.interfaces.common.HTTPSessionUser";   
    private static SessionManager instance = null;
    
    private BindingUtility bu = BindingUtility.getInstance();
    
    /** Creates a new instance of SessionManager */
    private SessionManager() {
        
    }
    
    /**
     * This class is a Singleton.  This method is used to a reference to an 
     * instance of this class
     *
     * @return
     *   A SessionManager instance
     */
    public synchronized static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * This method is used to determine if an existing HttpSession exists
     *
     * @param request
     *   A reference to HttpServletRequest instance. A session is obtained from 
     *   this object
     * @return
     *   Returns a boolean value: 'true' means a session exists, 'false' means
     *   a session does not exist
     */
    public boolean isSessionEstablished(HttpServletRequest request) 
        throws RegistryException {
        boolean sessionEstablished = false;
        
        if (request != null) {  //request may be null in localCall mode
            HttpSession session = request.getSession(false);
            sessionEstablished = (session == null ? false : true);
        }
        return sessionEstablished;
    }
   
    /**
     * This method is used to establish an HttpSession and associate a UserType 
     * instance with it.  If a session already exists, it will be used instead
     * of creating a new one.
     *
     * @param request
     *  An HttpServletRequest instance. A session is created using this object.
     * @param user
     *  A UserType instance. This object will be set as an attribute on the 
     *  session.  Once this is done, the UserType will obtained from the session
     *  as long as the session is active. This means subsequent SOAP messages
     *  do not have to be signed as the identify of the UserType is obtained 
     *  from the session.
     * @param message
     *   A RegistryRequestType instance. This object is used to determine if a
     *   session should be established.  If this object has a slot with key:
     *   SessionManager.HTTP_SESSION_USE, then a session will be established.
     *
     */
    public void establishSession(HttpServletRequest request,
                                 UserType user,
                                 RegistryRequestType message)
                                 throws RegistryException {
                                     
        // Since caller has not indicated whether or not to create an 
        // HttpSession, use the method below 
        boolean createHttpSession = createHttpSession(message);
        establishSession(request, user, createHttpSession);                       
    }
   
    /**
     * This method is used to establish an HttpSession and associate a UserType 
     * instance with it.  If a session already exists, it will be used instead
     * of creating a new one.
     *
     * @param request
     *  An HttpServletRequest instance. A session is created using this object.
     * @param user
     *  A UserType instance. This object will be set as an attribute on the 
     *  session.  Once this is done, the UserType will obtained from the session
     *  as long as the session is active. This means subsequent SOAP messages
     *  do not have to be signed as the identify of the UserType is obtained 
     *  from the session.
     * @param createHttpSession
     *   A boolean primitive that indicates whether or not to create a secure 
     *   session.
     */
    public void establishSession(HttpServletRequest request,
                                 UserType user,
                                 boolean createHttpSession) {
        
        if (request != null) {  //request may be null in localCall mode
            HttpSession session = request.getSession(false);

            // if user is null, default to registry guest
            if (user == null) {
                user = AuthenticationServiceImpl.getInstance().registryGuest;
                log.warn(ServerResourceBundle.getInstance().getString("message.userReferenceIsNullDefaultingToRegistryGuest"));
            }

            if (session == null) {
                //If so specified by request, create an authenticated HttpSession 
                //if none exists and set user context                
                if (createHttpSession) {
                    session = request.getSession(true);
                    if (session != null) {
                        session.setAttribute(SessionManager.HTTP_SESSION_USER, user);
                    }
                }
                //TODO: Cache HttpSession in RequestContext
            } else {
                session.setAttribute(SessionManager.HTTP_SESSION_USER, user);
            }
        }
    }

    /**
     * This method is used to obtain the UserType from the session.
     *
     * @return
     *   A reference to a UserType instance obtained from the session.  If the
     *   session does not exists or the SessionManager.HTTP_SESSION_USER is not
     *   found in the session, 'null' is returned.
     */ 
    public UserType getUserFromSession(HttpServletRequest request) {
        UserType user = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            user = (UserType)session.getAttribute(SessionManager.HTTP_SESSION_USER);
        }
        return user;
    }
   
    /*
     * This method is used to determine if an HttpSession should be created.
     * If a rim:Slot has a key of BindingUtility.IMPL_SLOT_CREATE_HTTP_SESSION,
     * create the sessions.  Otherwise, do not.
     */
    @SuppressWarnings("static-access")
	private boolean createHttpSession(Object message) throws RegistryException {
        boolean createSession = false;
        
        try {
            if (message instanceof RegistryRequestType) {
                RegistryRequestType req = (RegistryRequestType)message;
                HashMap<String, Object> slotsMap = bu.getSlotsFromRequest(req);
                if (slotsMap.containsKey(bu.IMPL_SLOT_CREATE_HTTP_SESSION)) {
                    String val = (String)slotsMap.get(bu.IMPL_SLOT_CREATE_HTTP_SESSION);
                    if (val.trim().equalsIgnoreCase("true")) {
                        createSession = true;
                    }
                }            
            }
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        return createSession;
    }
     
}

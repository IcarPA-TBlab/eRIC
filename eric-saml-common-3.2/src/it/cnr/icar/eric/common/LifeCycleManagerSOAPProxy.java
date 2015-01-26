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
package it.cnr.icar.eric.common;

import it.cnr.icar.eric.common.spi.LifeCycleManager;
import it.cnr.icar.eric.common.spi.RequestContext;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

/**
 *
 * @author  najmi
 */
public class LifeCycleManagerSOAPProxy implements LifeCycleManager {
    
    @SuppressWarnings("unused")
	private String registryURL = null;
    @SuppressWarnings("unused")
	private CredentialInfo credentialInfo = null;
    private SOAPMessenger msgr = null;
        
    
    /** Creates a new instance of LifeCycleManagerLocalImpl */
    public LifeCycleManagerSOAPProxy(String registryURL, CredentialInfo credentialInfo) {
        msgr = new SOAPMessenger(registryURL, credentialInfo);
    }
    
    public RegistryResponseType approveObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    /** Sets the status of specified objects. This is an extension request that will be adde to ebRR 3.1?? */
    public RegistryResponseType setStatusOnObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponseType deprecateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponseType unDeprecateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponseType removeObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
       
    public RegistryResponseType submitObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        Map<String, Object> idToRepositoryItemMap = context.getRepositoryItemsMap();
                
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(req, sw);
            
            String requestString = sw.toString();
            logRequest(requestString);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), idToRepositoryItemMap);
            
            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    public RegistryResponseType updateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        Map<String, Object> idToRepositoryItemMap = context.getRepositoryItemsMap();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), idToRepositoryItemMap);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }

    }
    
    public RegistryResponseType relocateObjects(RequestContext context) throws RegistryException {
        RegistryRequestType req = context.getCurrentRegistryRequest();
        @SuppressWarnings("unused")
		Map<String, Object> idToRepositoryItemMap = context.getRepositoryItemsMap();
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(req, sw);
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), null);

            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    /** Sends an impl specific protocol extension request. */
    public RegistryResponseHolder extensionRequest(RequestContext context) throws RegistryException {
        RegistryRequestType ebRegistryRequestType = context.getCurrentRegistryRequest();
        Map<String, Object> idToRepositoryItemMap = context.getRepositoryItemsMap();
        
        JAXBElement<RegistryRequestType> ebRegistryRequest = BindingUtility.getInstance().rsFac.createRegistryRequest(ebRegistryRequestType);
        
        
        try {
            StringWriter sw = new StringWriter();
//            Marshaller marshaller = BindingUtility.getInstance().lcmFac.createMarshaller();
            Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
//            marshaller.marshal(ebRegistryRequestType, sw);
            marshaller.marshal(ebRegistryRequest, sw);
            
            RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString(), idToRepositoryItemMap);

            return resp;
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }
    
    
    private void logRequest(String requestString) {
		boolean logRequests = Boolean
				.valueOf(
						CommonProperties.getInstance().getProperty(
								"eric.common.soapMessenger.logSubmitRequests",
								"false")).booleanValue();

        if (logRequests) {
            PrintStream requestLogPS = null;
            try {
                requestLogPS = new PrintStream(new FileOutputStream(java.io.File.createTempFile(
                    "LifeCycleManagerSOAPProxy_submitLog", ".xml")));
                requestLogPS.println(requestString);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (requestLogPS != null) {
                    requestLogPS.close();
                }
            }            
        }
    }
    
}

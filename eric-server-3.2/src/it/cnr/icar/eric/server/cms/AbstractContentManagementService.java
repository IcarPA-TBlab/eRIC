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
package it.cnr.icar.eric.server.cms;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.exceptions.InvalidConfigurationException;
import it.cnr.icar.eric.common.security.wss4j.WSS4JSecurityUtilBST;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.List;
import javax.activation.DataHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.registry.RegistryException;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;


/**
 * Abstract superclass of cataloging and validation (and other)
 * services.
 * @see
 * @author Tony Graham
 *
 */
public abstract class AbstractContentManagementService
    implements ContentManagementService {
    private static final Log log = LogFactory.getLog(AbstractContentManagementService.class.getName());
    protected static BindingUtility bu = BindingUtility.getInstance();
    protected static RepositoryManager rm = RepositoryManagerFactory.getInstance()
                                                                    .getRepositoryManager();
    protected static QueryManager qm = QueryManagerFactory.getInstance()
                                                          .getQueryManager();
    protected static MessageFactory mf;

    static {
        try {
            mf = MessageFactory.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the <code>RegistryObjectType</code> as a stream of XML markup.
     *
     * @param ebRegistryObjectType an <code>RegistryObjectType</code> value
     * @return a <code>StreamSource</code> value
     * @exception RegistryException if an error occurs
     */
    protected StreamSource getAsStreamSource(RegistryObjectType ebRegistryObjectType)
        throws RegistryException {
        log.debug("getAsStreamSource(RegistryObjectType) entered");

        StreamSource src = null;

        try {
            StringWriter sw = new StringWriter();

            Marshaller marshaller = bu.getJAXBContext().createMarshaller();

            /*
             * marshal JAXBElement Identifiable
             */
			JAXBElement<IdentifiableType> ebIdentifiable = bu.rimFac
					.createIdentifiable(ebRegistryObjectType);
            marshaller.marshal(ebIdentifiable, sw);
            
            StringReader reader = new StringReader(sw.toString());
            src = new StreamSource(reader);
        }
        // these Exceptions should already be caught by Binding
        catch (JAXBException e) {
            throw new RegistryException(e);
        }

        return src;
    }

    protected AttachmentPart getRepositoryItemAsAttachmentPart(String id)
        throws RegistryException {
        RepositoryItem ri = rm.getRepositoryItem(id);

        AttachmentPart ap = null;

        try {
            SOAPMessage m = mf.createMessage();
            DataHandler dh = ri.getDataHandler();
            String cid = WSS4JSecurityUtilBST.convertUUIDToContentId(id);
            ap = m.createAttachmentPart(dh);
            ap.setContentId(cid);
            
        } catch (Exception e) {
            throw new RegistryException(e);
        }

        return ap;
    }
    
    /**
     * Select one accessURI from the possibly multiple ServiceBindings of
     * <code>service</code>, where a ServiceBinding may have either an
     * accessURI or a targetBinding that refers to another ServiceBinding.
     *
     * @param service The service from which to select an accessURI
     * @throws RegistryException if an error occurs
     * @return String for URI of remote service
     */
    protected String selectAccessURI(ServiceType service) throws RegistryException {
        List<ServiceBindingType> serviceBindings = service.getServiceBinding();

        if (log.isDebugEnabled()) {
            if (serviceBindings == null) {
                log.debug("ServiceBindings is null");
            } else {
                Iterator<ServiceBindingType> debugIter = serviceBindings.iterator();
                while (debugIter.hasNext()) {
                    ServiceBindingType binding = debugIter.next();
                    log.debug("URL: " + binding.getAccessURI());
                }
            }
        }

        String accessURI = null;

        // Use the first ServiceBinding having an accessURI.
        if (serviceBindings.size() > 0) {
            Iterator<ServiceBindingType> sbIter = serviceBindings.iterator();
            while (sbIter.hasNext()) {
                accessURI = (sbIter.next()).getAccessURI();
                
                if ((accessURI != null) && (!accessURI.equals(""))) {
                    break;
                }
            }
            
            if ((accessURI == null) || accessURI.equals("")) {
                throw new InvalidConfigurationException(
                    ServerResourceBundle.getInstance()
                                        .getString("message.NoAccessURIForService", 
                                            new Object[] {service.getId()}));
            }
        } else {
            throw new InvalidConfigurationException(
                ServerResourceBundle.getInstance()
                                        .getString("message.NoServiceBindingsForService", 
                                            new Object[] {service.getId()}));
        }

        return accessURI;
    }
}

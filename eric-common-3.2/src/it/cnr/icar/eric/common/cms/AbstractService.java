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
package it.cnr.icar.eric.common.cms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;

import org.w3c.dom.Node;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.security.wss4j.WSS4JSecurityUtilBST;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.activation.DataHandler;

import javax.servlet.ServletContext;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.rpc.handler.MessageContext;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;


/**
 * @author Tony Graham
 *
 */
public abstract class AbstractService implements ServiceLifecycle {
    private static final Log log = LogFactory.getLog(AbstractService.class);

    //Canonical ResponseStatusType ids
    protected static final String CANONICAL_RESPONSE_STATUS_TYPE_ID_Success =
	BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success;
    protected static final String CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure =
	BindingUtility.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure;
    protected static final ObjectFactory rimFac = new ObjectFactory();
    protected JAXBContext jaxbContext;
    protected ServletEndpointContext servletEndpointContext = null;
    protected ServletContext servletContext = null;

    public void init(Object context) {
        servletEndpointContext = (ServletEndpointContext) context;
        servletContext = servletEndpointContext.getServletContext();
    }

    public void destroy() {
        servletEndpointContext = null;
        servletContext = null;
    }

    protected JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(
                    "org.oasis.ebxml.registry.bindings.rim:" +
                    "org.oasis.ebxml.registry.bindings.rs:" +
                    "org.oasis.ebxml.registry.bindings.lcm:" +
                    "org.oasis.ebxml.registry.bindings.query:" +
                    "org.oasis.ebxml.registry.bindings.cms");
                    // "org.oasis.saml.bindings.protocol:" +
                    // "org.oasis.saml.bindings.assertion:" +
        }

        return jaxbContext;
    }

    protected Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        //unmarshaller.setValidating(true);
        unmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    boolean keepOn = false;

                    return keepOn;
                }
            });

        return unmarshaller;
    }

    protected Object getBindingObjectFromNode(Node node)
        throws Exception {
        Object obj = null;

        Unmarshaller unmarshaller = getJAXBContext().createUnmarshaller();
        obj = unmarshaller.unmarshal(node);

        return obj;
    }

    protected SOAPElement getSOAPElementFromBindingObject(Object obj)
        throws Exception {
        SOAPElement soapElem = null;

        SOAPElement parent = SOAPFactory.newInstance().createElement("dummy");

        Marshaller marshaller = getJAXBContext().createMarshaller();
        marshaller.marshal(obj, System.err);
        marshaller.marshal(obj, new DOMResult(parent));
        soapElem = (SOAPElement) parent.getChildElements().next();

        return soapElem;
    }

    protected static void printNodeToConsole(Node n) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();

            // also print to console
            transformer.transform(new DOMSource(n), new StreamResult(System.err));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    protected HashMap<String, DataHandler> getRepositoryItemDHMap() throws Exception {
        HashMap<String, DataHandler> repositoryItemDHMap = new HashMap<String, DataHandler>();
        MessageContext mc = servletEndpointContext.getMessageContext();
        Collection<?> attachments = (Collection<?>) mc.getProperty(com.sun.xml.rpc.server.ServerPropertyConstants.GET_ATTACHMENT_PROPERTY);

        Iterator<?> attachmentsIter = attachments.iterator();

        while (attachmentsIter.hasNext()) {
            Object obj = attachmentsIter.next();

            System.err.println("getRepositoryItems:: Attachment: " +
                obj.getClass().getName());

            if (obj instanceof AttachmentPart) {
                AttachmentPart ap = (AttachmentPart) obj;

                String contentId = WSS4JSecurityUtilBST.convertContentIdToUUID(ap.getContentId());
                String contentType = ap.getContentType();

                System.err.println("getRepositoryItems:: contentId: " +
                    contentId + "; contentType: " + contentType);
                if (log.isDebugEnabled()) {
                    log.debug(
                        "Processing attachment (RepositoryItem):\n" +
                        ap.getContent().toString());
                }

                
                DataHandler dh = ap.getDataHandler();

                repositoryItemDHMap.put(contentId, dh);
            }
        }

        return repositoryItemDHMap;
    }
}

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.xml.registry.RegistryException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;


/**
* Repository item wrapper.
*
* @author Adrian Chong
* @author Peter Burgess
*
* @version $Version: $ [$Date: 2005/02/23 22:56:47 $]
*/
public class RepositoryItemImpl implements RepositoryItem, Cloneable {
    @SuppressWarnings("unused")
	private Log log = LogFactory.getLog(RepositoryItemImpl.class.getName());

    private String id;
    private Element sigElement;
    private DataHandler handler;
    
    private RepositoryItemImpl() {}
    
    /**
    * Constructor.
    *
    * @deprecated sigElement/multipart attachment is not used anymore.
    */
    public RepositoryItemImpl(String id, Element sigElement, DataHandler handler) {
        this.id = id;
        this.sigElement = sigElement;
        this.handler = handler;
    }

    /**
    * Constructor.
    */
    public RepositoryItemImpl(String id, DataHandler handler) {
        this.id = id;
        this.handler = handler;
    }
    
    /**
    * Return the size of of the repository item in bytes.
    */
    public int getSize() throws IOException {
        InputStream is = handler.getInputStream();
        int size = 0;

        while (is.read() != -1) {
            size++;
        }

        return size;
    }

    //-------------------------------
    //  ACCESSORS
    //-------------------------------
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Element getSignatureElement() {
        return sigElement;
    }
    
    public DataHandler getDataHandler() {
        return handler;
    }
    
    /**
     * Packages the RepositoryItem as a MimeMultiPart and returns it
     *
     * @deprecated sigElement/multipart attachment is not used anymore.
     */
    public MimeMultipart getMimeMultipart() throws RegistryException {
        MimeMultipart mp = null;
        try {
            //Create a multipart with two bodyparts
            //First bodypart is an XMLDSIG and second is the attached file
            mp = new MimeMultipart();

            //The signature part
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.transform(new DOMSource(sigElement), new StreamResult(os));
            
            
            MimeBodyPart bp1 = new MimeBodyPart();
            bp1.addHeader("Content-ID", "payload1");
            bp1.setText(os.toString(), "utf-8");
            mp.addBodyPart(bp1);
            
            //The payload part
            MimeBodyPart bp2 = new MimeBodyPart();
            bp2.setDataHandler(handler);
            bp2.addHeader("Content-Type", handler.getContentType());
            bp2.addHeader("Content-ID", "payload2");
            mp.addBodyPart(bp2);
        } catch (MessagingException e) {
            throw new RegistryException(e);
        } catch (TransformerException e) {
            throw new RegistryException(e);
        }
        return mp;
    }
    
    public Object clone() throws CloneNotSupportedException {
        RepositoryItemImpl theClone = new RepositoryItemImpl();
        // Initialize theClone.
        theClone.id = id;
        theClone.sigElement = sigElement;
        theClone.handler = handler;
        
        return theClone;
    }
}

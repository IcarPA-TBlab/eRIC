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
package it.cnr.icar.eric.common.soap;

import it.cnr.icar.eric.common.CommonResourceBundle;

import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.JAXRException;
import org.w3c.dom.Document;

/**
 * Enables sending of SOAP requests to a SOAP endpoint over HTTP
 *
 * @author Farrukh S. Najmi
 */
public class SOAPSender {
    protected static CommonResourceBundle resourceBundle = CommonResourceBundle.getInstance();
        
    private Log log = LogFactory.getLog(SOAPSender.class);
    
    /** Creates a new instance of SOAPSender */
    public SOAPSender() {
    }
    
    /**
     *
     * Send specified SOAPMessage to specified SOAP endpoint.
     */
    public SOAPMessage send(SOAPMessage msg, String endpoint) throws SOAPException {
        
        SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
        SOAPConnection connection = scf.createConnection();

        long t1 = System.currentTimeMillis();

        msg.saveChanges();
        
        dumpMessage("Request:", msg);
        SOAPMessage reply = connection.call(msg, endpoint);
        
        long t2 = System.currentTimeMillis();

        dumpMessage("Response:", reply);

        double secs = ((double) t2 - t1) / 1000;
        log.debug("Call elapsed time in seconds: " + secs);

        return reply;
    }
    
    /**
     *
     * Creates a SOAPMessage with bodyDoc as only child.
     */
    public SOAPMessage createSOAPMessage(Document bodyDoc) throws JAXRException {
        SOAPMessage msg = null;
        
        try {
            MessageFactory factory = MessageFactory.newInstance();
            msg = factory.createMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();
            //SOAPHeader sh = se.getHeader(); 
            SOAPBody sb = se.getBody();

            sb.addDocument(bodyDoc);
            msg.saveChanges();
        }
        catch (SOAPException e) {
            e.printStackTrace();
            throw new JAXRException(resourceBundle.getString("message.URLNotFound"), e);
        } 
        return msg;
    }        

    void dumpMessage(String info, SOAPMessage msg) throws SOAPException {
        //if (log.isTraceEnabled()) {
            if (info != null) {
                System.err.print(info);
            }

            try {
                msg.writeTo(System.err);
                System.err.println();
            } 
            catch (IOException x) {
            	return;
            }
        //}
    }
    
}

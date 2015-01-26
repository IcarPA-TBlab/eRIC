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

import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;


/**
 *
 * The superclass for components that
 * live in a servlet container that receives SOAP messages.
 *
 * Based upon JAXMServlet from JAXM 1.1 API
 */
public abstract class SOAPServlet
        extends HttpServlet {
    
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2291572167403318883L;
	/**
     * The <code>MessageFactory</code> object that will be used internally
     * to create the <code>SOAPMessage</code> object to be passed to the
     * method <code>onMessage</code>. This new message will contain the data
     * from the message that was posted to the servlet.  Using the
     * <code>MessageFactory</code> object that is the value for this field
     * to create the new message ensures that the correct profile is used.
     */
    protected MessageFactory msgFactory = null;
    
    /**
     * Initializes this <code>SOAPServlet</code> object using the given
     * <code>ServletConfig</code> object and initializing the
     * <code>msgFactory</code> field with a default
     * <code>MessageFactory</code> object.
     *
     * @param servletConfig the <code>ServletConfig</code> object to be
     *        used in initializing this <code>SOAPServlet</code> object
     */
    public void init(ServletConfig servletConfig)
    throws ServletException {
        super.init(servletConfig);
        try {
            // Initialize it to the default.
            msgFactory = MessageFactory.newInstance();
        } catch (SOAPException ex) {
            throw new ServletException(ServerResourceBundle.getInstance().getString("message.unableToCreateMessageFactory")+ex.getMessage());
        }
    }
    
    
    /**
     * Sets this <code>SOAPServlet</code> object's <code>msgFactory</code>
     * field with the given <code>MessageFactory</code> object.
     * A <code>MessageFactory</code> object for a particular profile needs to
     * be set before a message is received in order for the message to be
     * successfully internalized.
     *
     * @param msgFactory the <code>MessageFactory</code> object that will
     *        be used to create the <code>SOAPMessage</code> object that
     *        will be used to internalize the message that was posted to
     *        the servlet
     */
    public void setMessageFactory(MessageFactory msgFactory) {
        this.msgFactory = msgFactory;
    }
    
    /**
     * Returns a <code>MimeHeaders</code> object that contains the headers
     * in the given <code>HttpServletRequest</code> object.
     *
     * @param req the <code>HttpServletRequest</code> object that a
     *        messaging provider sent to the servlet
     * @return a new <code>MimeHeaders</code> object containing the headers
     *         in the message sent to the servlet
     */
    protected static
            MimeHeaders getHeaders(HttpServletRequest req) {
        Enumeration<String> _unum = req.getHeaderNames();
        MimeHeaders headers = new MimeHeaders();
        
        while (_unum.hasMoreElements()) {
            String headerName = _unum.nextElement();
            String headerValue = req.getHeader(headerName);
            
            StringTokenizer values = new StringTokenizer(headerValue, ",");
            while (values.hasMoreTokens())
                headers.addHeader(headerName, values.nextToken().trim());
        }
        
        return headers;
    }
    
    /**
     * Sets the given <code>HttpServletResponse</code> object with the
     * headers in the given <code>MimeHeaders</code> object.
     *
     * @param headers the <code>MimeHeaders</code> object containing the
     *        the headers in the message sent to the servlet
     * @param res the <code>HttpServletResponse</code> object to which the
     *        headers are to be written
     * @see #getHeaders
     */
    protected static
            void putHeaders(MimeHeaders headers, HttpServletResponse res) {
        Iterator<?> it = headers.getAllHeaders();
        while (it.hasNext()) {
            MimeHeader header = (MimeHeader)it.next();
            
            String[] values = headers.getHeader(header.getName());
            if (values.length == 1) {
                res.setHeader(header.getName(), header.getValue());
            } else {
                StringBuffer concat = new StringBuffer();
                int i = 0;
                while (i < values.length) {
                    if (i != 0)
                        concat.append(',');
                    concat.append(values[i++]);
                }
                
                res.setHeader(header.getName(),
                        concat.toString());
            }
        }
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        resp.setContentType("text/plain");
        
        PrintWriter wt = resp.getWriter();
        wt.print(
                "Are you visiting this URL with Web browser? You should send requests only via SOAP/HTTP for this servlet.");
        wt.flush();
        wt.close();
    }
    
    /**
     * Internalizes the given <code>HttpServletRequest</code> object
     * and writes the reply to the given <code>HttpServletResponse</code>
     * object.
     * <P>
     * Note that the value for the <code>msgFactory</code> field will be used to
     * internalize the message. This ensures that the message
     * factory for the correct profile is used.
     *
     * @param req the <code>HttpServletRequest</code> object containing the
     *        message that was sent to the servlet
     * @param resp the <code>HttpServletResponse</code> object to which the
     *        response to the message will be written
     * @throws ServletException if there is a servlet error
     * @throws IOException if there is an input or output error
     */
    public void doPost(HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Get all the headers from the HTTP request.
            MimeHeaders headers = getHeaders(req);
            
            
            
            // Get the body of the HTTP request.
            InputStream is = req.getInputStream();
            
            // Now internalize the contents of a HTTP request and
            // create a SOAPMessage
            SOAPMessage msg = msgFactory.createMessage(headers, is);
            
            SOAPMessage reply = onMessage(msg, req, resp);
            
            if (reply != null) {
                
                // Need to saveChanges 'cos we're going to use the
                // MimeHeaders to set HTTP response information. These
                // MimeHeaders are generated as part of the save.
                
                if (reply.saveRequired()) {
                    reply.saveChanges();
                }
                
                resp.setStatus(HttpServletResponse.SC_OK);
                
                putHeaders(reply.getMimeHeaders(), resp);
                
                // Write out the message on the response stream.
                OutputStream os = resp.getOutputStream();
                reply.writeTo(os);
                
                os.flush();
                
            } else
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch(Exception ex) {
            throw new ServletException(ServerResourceBundle.getInstance().getString("message.SOAPPOSTFailed")+ex.getMessage());
        }
    }
    
    public abstract SOAPMessage onMessage(SOAPMessage message, HttpServletRequest req, HttpServletResponse resp);
}

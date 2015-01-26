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
import it.cnr.icar.eric.common.UUIDFactory;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.UnimplementedException;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.RegistryException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


/**
 *
 * @author  Uday Subbarayan(mailto:uday.s@sun.com)
 * @version
 */
public class QueryManagerURLHandler extends URLHandler {
    
    
    private Log log = LogFactory.getLog(this.getClass());
    private BindingUtility bu = BindingUtility.getInstance();


    protected QueryManagerURLHandler() {
    }
    
    QueryManagerURLHandler(HttpServletRequest request,
        HttpServletResponse response) throws RegistryException {
            
        super(request, response);
    }
    
    /** Process the QueryManager request and sends the response back to the client
     * @param
     *
     */
    void processGetRequest() 
        throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException 
    {
        String method = request.getParameter("method");
        String id = request.getParameter("param-id");
        String lid = request.getParameter("param-lid");
        String versionName = request.getParameter("param-versionName");
        @SuppressWarnings("unused")
		String flavor = request.getParameter("flavor");
        
        if ((method == null) || method.equals("")) {
            throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.methodCannotBeNull"));
        } 
        else if (method.equalsIgnoreCase("getRegistryObject")) {
            try {
                response.setContentType("text/xml; charset=UTF-8");
                
                String queryString = getQueryStringForFindByIdLidVersion(id, lid, versionName);
                List<RegistryObjectType> results = submitQueryAs(queryString, currentUser);
                if (results.isEmpty()) {
                    throw new ObjectNotFoundException(getNotFoundExceptionMsg(id, lid, versionName));
                }
                else {
                    if (results.size() > 1) {
                        writeDirectoryListing(results);
                    } else {
                        RegistryObjectType ebRegistryObjectType = results.get(0);
                        writeRegistryObject(ebRegistryObjectType);
                    }
                }   
            } catch (NullPointerException e) {
                log.error(e.toString(), e);
                throw new RegistryException(it.cnr.icar.eric.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
            }
        } else if (method.equalsIgnoreCase("getRepositoryItem")) {
            try {                
                String queryString = getQueryStringForFindByIdLidVersion(id, lid, versionName);
                List<RegistryObjectType> results = submitQueryAs(queryString, currentUser);
                if (results.isEmpty()) {
                    throw new ObjectNotFoundException(getNotFoundExceptionMsg(id, lid, versionName));
                }
                else {
//                    RegistryObjectType ro = (RegistryObjectType)results.get(0);
                	// take ComplexType from Element
                    //RegistryObjectType ebRegistryObjectType = ((JAXBElement<RegistryObjectType>)(results.get(0))).getValue();
                    RegistryObjectType ebRegistryObjectType = results.get(0);
                    
                    // check the first ComplexType in result
                    if (!(ebRegistryObjectType instanceof ExtrinsicObjectType)) {
                        //return the error code
                        throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.expectedExtrinsicObjectNotFound",
                                new Object[]{ebRegistryObjectType.getClass()}));
                    }
                    
                    if (results.size() > 1) {
                        writeDirectoryListing(results);
                    } else {
                        ExtrinsicObjectType ebExtrinsicObjectType = (ExtrinsicObjectType)ebRegistryObjectType;
                        writeRepositoryItem(ebExtrinsicObjectType);
                    }
                }   
            } catch (NullPointerException e) {
                log.error(e.toString(), e);
                throw new RegistryException(it.cnr.icar.eric.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
            }            
        } else if (method.equalsIgnoreCase("submitAdhocQuery")) {
            try {
                String startIndex = request.getParameter("startIndex");
                if (startIndex == null) {
                    startIndex = "0";
                }

                String maxResults = request.getParameter("maxResults");
                if (maxResults == null) {
                    maxResults = "-1";
                }
            
                String queryId = request.getParameter("queryId");
                if (queryId == null) {
                    queryId = "urn:freebxml:registry:query:BusinessQuery";
                }
                
                //Create and populate queryParamsMap from request params map
                Map<String, String> queryParams = new HashMap<String, String>();
                
                Iterator<String> iter = request.getParameterMap().keySet().iterator();
                while (iter.hasNext()) {
                    Object obj = iter.next();
                    if (obj instanceof String) {
                        String paramName = (String)obj;
                        
                        //Only use params whose names begin with '$' char
                        if (paramName.charAt(0) == '$') {
                            String paramValue = request.getParameter(paramName);
                            queryParams.put(paramName, paramValue);
                        }
                    }
                }
                                
                ServerRequestContext context = new ServerRequestContext("QueryManagerURLHandler.processGetRequest.submitAdhocQuery", null);
                List<IdentifiableType> ebIdentifiableTypeResultList = invokeParameterizedQuery(context,
                        queryId,
                        queryParams,
                        currentUser,
                        Integer.parseInt(startIndex),
                        Integer.parseInt(maxResults));
                String getRepositoryItem = request.getParameter("getRepositoryItem");
                if ((getRepositoryItem != null) &&  
                    ((getRepositoryItem.equalsIgnoreCase("true")) ||
                    (getRepositoryItem.equals("1")))) {
                    writeRepositoryItemList(ebIdentifiableTypeResultList);                    
                } else {
                    writeRegistryObjectList(ebIdentifiableTypeResultList);                        
                }
            } catch (NullPointerException e) {
                log.error(e.toString(), e);
                throw new RegistryException(it.cnr.icar.eric.server.common.Utility.getInstance().getStackTraceFromThrowable(e));
            }
        } else if (method.equalsIgnoreCase("newUUID")) {
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            UUIDFactory uuidFac =
                UUIDFactory.getInstance();
            out.print(uuidFac.newUUID().toString());
            out.close();
        } else {
            //return the error code
            throw new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.unknownMethod",
                    new Object[]{method}));
        }
    }
    
    /** 
     * Process the QueryManager POST request and sends the response back to the client
     *
     */
    void processPostRequest() throws IOException, RegistryException, InvalidRequestException, UnimplementedException, ObjectNotFoundException {
        try {
            PrintWriter out = response.getWriter();
            String xmlAdhocQueryRequest = request.getParameter("xmldoc");

            String message = xmlAdhocQueryRequest;
            //TODO: consider using unmarshall directly from BindingUtility
            StreamSource messageSS = new StreamSource(new StringReader(message));
            Unmarshaller unmarshaller = bu.getJAXBContext().createUnmarshaller();
            AdhocQueryRequest req = (AdhocQueryRequest)unmarshaller.unmarshal(messageSS);
            
            ServerRequestContext context = new ServerRequestContext("QueryManagerURLHandler.processPostRequest", req);
            context.setUser(currentUser);
            RegistryResponseType regResponse = QueryManagerFactory.getInstance()
                                                                  .getQueryManager()
                                                                  .submitAdhocQuery(context);
//            Marshaller marshaller = bu.rsFac.createMarshaller();
            Marshaller marshaller = bu.getJAXBContext().createMarshaller();
            marshaller.marshal(regResponse, out);
            out.close();

        } catch (JAXBException ex) {
            log.error(ex.toString(), ex);
            throw new RegistryException(ex);
        }
    }

    private String getQueryStringForFindByIdLidVersion(String id, String lid, String versionName) {
        String queryString = null;
        if (id != null) {
            queryString = 
                "SELECT * FROM RegistryObject WHERE id LIKE '" + id + "'";
        } else if (lid != null) {
            queryString = 
                "SELECT * FROM RegistryObject WHERE lid LIKE '" + lid + "'";

            if (versionName != null) {
                queryString += " AND versionName LIKE '" + versionName + "'";
            }

            queryString += " ORDER BY versionName DESC";
        }
        
        return queryString;
    }
    
    private String getNotFoundExceptionMsg(String id, String lid, String versionName) {
        String msg = null;
        if (id != null) {
            msg = "RegistryObject with id = '" + id + "' not found.";
        } else if (lid != null) {
            msg = "Repository item with ExtrinsicObject with lid = '" + lid + "' ";

            if (versionName != null) {
                msg += " AND versionName = '" + versionName + "'";
            }

            msg += " not found.";
        }
        
        return msg;
    }
    
    /** Write the result set as a RegistryObjectList */
    private void writeRegistryObjectList(List<? extends IdentifiableType> ebRegistryObjectTypeList) throws IOException, RegistryException {
        ServletOutputStream sout = null;
        try {
            sout = response.getOutputStream();

            @SuppressWarnings("unchecked")
			RegistryObjectListType ebRegistryObjectListType = bu.getRegistryObjectListType((List<RegistryObjectType>)ebRegistryObjectTypeList);
            
//            javax.xml.bind.Marshaller marshaller = bu.rimFac.createMarshaller();
            javax.xml.bind.Marshaller marshaller = bu.getJAXBContext().createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
            marshaller.marshal(ebRegistryObjectListType, sout);

        } catch (JAXBException e) {
          throw new RegistryException(e);  
        } finally {
            if ( sout != null ) {
                sout.close();
            }
        }
    }
    
    /** Write the result set as a RepositoryList */
    private void writeRepositoryItemList(List<IdentifiableType> roList) throws IOException, RegistryException {
        ServletOutputStream sout = null;
        @SuppressWarnings("unused")
		String requestURL = request.getRequestURL().toString() + "?" + request.getQueryString();        
        try {
            sout = response.getOutputStream();

            ArrayList<ExtrinsicObjectType> eos = new ArrayList<ExtrinsicObjectType>();
            Iterator<IdentifiableType> iter = roList.iterator();
            while(iter.hasNext()) {
                RegistryObjectType ro = (RegistryObjectType)iter.next();
                if (ro instanceof ExtrinsicObjectType) {
                    ExtrinsicObjectType eo = (ExtrinsicObjectType)ro;
                    if (eo.getContentVersionInfo() != null) {
                        //This eo has an ri
                        eos.add(eo);
                    }
                }
            }
            writeRepositoryItems(eos);
        } finally {
            if ( sout != null ) {
                sout.close();
            }
        }
    }
        
    /** Write the directory listing showing specified objects */
    private void writeDirectoryListing(List<?> roList) throws IOException, RegistryException {
        ServletOutputStream sout = null;
        String requestURL = request.getRequestURL().toString() + "?" + request.getQueryString();        
        response.setContentType("text/html; charset=UTF-8");
        try {
            sout = response.getOutputStream();

            sout.println("<html>");
            sout.println("<head>");
            sout.println("<title>Index of " + requestURL + "</title>");
            sout.println("</head>");
            sout.println("<body>");
            sout.println("<h1>Index of " + requestURL + "</h1>");
            sout.println("<pre>ObjectType\t\tName\t\tVersion\t\tContent Version\t\tLogical ID");
            sout.println("<hr/>");
            
            Iterator<?> iter = roList.iterator();
            while(iter.hasNext()) {
//                RegistryObjectType ro = (RegistryObjectType)iter.next();
            	// take ComplexType from Element
                @SuppressWarnings("unchecked")
				RegistryObjectType ebRegistryObjectType = ((JAXBElement<RegistryObjectType>)iter.next()).getValue();
                writeDirectoryListingItem(sout, ebRegistryObjectType);
            }
            
            sout.println("</pre>");
            sout.println("<hr/>");
            sout.println("<address>freebXML Regisry Server version 3.0</address>");
            sout.println("</body>");
            sout.println("</html>");
        }
        finally {
            if ( sout != null ) {
                sout.close();
            }
        }
    }
    
    @SuppressWarnings("unused")
	private void writeDirectoryListingItem(ServletOutputStream os, RegistryObjectType ebRegistryObjectType) throws IOException, RegistryException {
        String baseURL = getBaseUrl();
        String reqId = request.getParameter("param-id");
        String reqLid = request.getParameter("param-lid");
        String reqVersionName = request.getParameter("param-versionName");
        String method = request.getParameter("method");
        
        String id = ebRegistryObjectType.getId();
        String name = getClosestValue(ebRegistryObjectType.getName());
        String desc = getClosestValue(ebRegistryObjectType.getDescription());
        String lid = ebRegistryObjectType.getLid();
        ServerRequestContext context = new ServerRequestContext("QueryManagerURLHandler.writeDirectoryListingItem", null);
        ClassificationNodeType node = (ClassificationNodeType)qm.getRegistryObject(context, ebRegistryObjectType.getObjectType());
        String objectType = node.getCode();
        VersionInfoType versionInfo = ebRegistryObjectType.getVersionInfo();
        VersionInfoType contentVersionInfo = null;
        
        String versionName = versionInfo.getVersionName();
        String contentVersionName = "";
        if (ebRegistryObjectType instanceof ExtrinsicObjectType) {
            ExtrinsicObjectType eo = (ExtrinsicObjectType)ebRegistryObjectType;
            contentVersionInfo = eo.getContentVersionInfo();
            if (contentVersionInfo != null) {
                contentVersionName = contentVersionInfo.getVersionName();
            }
        }
        
        String roURL = baseURL + "?interface=QueryManager&method=getRegistryObject&";        
        if (reqId != null) {
            roURL += "param-id=" + id;
        } else if (reqLid != null) {
            roURL += "param-lid=" + lid + "&param-versionName=" + versionName;            
        }
        
        String riURL = baseURL + "?interface=QueryManager&method=getRepositoryItem&";        
        if (reqId != null) {
            riURL += "param-id=" + id;
        } else if (reqLid != null) {
            riURL += "param-lid=" + lid + "&param-versionName=" + versionName;            
        }
        
        os.println(objectType + "\t\t" + name + 
            "\t\t<a href=\"" + roURL + "\">" + versionName + 
            "</a>\t\t<a href=\"" + riURL + "\">" + contentVersionName +
            "</a>\t\t" + lid + "<br/>");
    }
    
}

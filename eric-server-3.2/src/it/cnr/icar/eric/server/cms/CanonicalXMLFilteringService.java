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

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.RegistryException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.URIResolver;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.UserType;

/**
 * Canonical XML Content Filtering Service
 *
 * @author Farrukh S. Najmi
 *
 */
public class CanonicalXMLFilteringService extends AbstractContentManagementService {
    private static final Log log = LogFactory.getLog(CanonicalXMLFilteringService.class.getName());
    
    @SuppressWarnings("unchecked")
	public ServiceOutput invoke(ServerRequestContext context, ServiceInput si, ServiceType service,
            InvocationController invocationController, UserType user)
            throws RegistryException {
        if (log.isTraceEnabled()) {
            log.trace("CanonicalXMLFilteringService.invoke()");
        }
        ServiceOutput so = new ServiceOutput();
        so.setOutput(context);
        RepositoryItem repositoryItem = si.getRepositoryItem();
        
        String roId = si.getRegistryObject().getId();
        
        ServerRequestContext outputContext = context; //new RequestContext(null);
        
        try {
            
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("subjectId", context.getUser().getId());
            
            //First filter the RegistryObject metadata
            StreamSource input = getAsStreamSource(si.getRegistryObject());
            StreamSource xslt = rm.getAsStreamSource(invocationController.getEoId());
                        
            //Specify empty value to indicate that only metadata should be transformed
            //in this first XSLT invocation.
            params.put("repositoryItem", "");
            File output = runXSLT(input, xslt, getURIResolver(context), params);
            
            JAXBElement<RegistryObjectType> ebRegistryObject = (JAXBElement<RegistryObjectType>) bu.getJAXBContext()
                    .createUnmarshaller().unmarshal(output);

            // take ComplexType from Element
            RegistryObjectType ebRegistryObjectType = ebRegistryObject.getValue();
            
            //Replace input RegistryObject with output RegistryObject.
            outputContext.getQueryResults().remove(si.getRegistryObject());
            outputContext.getQueryResults().add(ebRegistryObjectType);
                        
            //Now filter the RepositoryItem content if any
            if (repositoryItem != null) {
                input = new StreamSource(repositoryItem.getDataHandler().getInputStream());
                xslt = rm.getAsStreamSource(invocationController.getEoId());
                
                params.put("repositoryItem", roId);
                output = runXSLT(input, xslt, getURIResolver(context), params);

                RepositoryItem outputRI = new RepositoryItemImpl(roId, new DataHandler(new FileDataSource(output)));
                

                //Replace input RepositoryItem with output RepositoryItem.
                outputContext.getRepositoryItemsMap().put(roId, outputRI);                
            }
            
            // TODO: User should refer to "Service object for the
            // Content Management Service that generated the
            // Cataloged Content."
            outputContext.setUser(user);
            
        } catch (Exception e) {
            if (outputContext != context) {
                outputContext.rollback();
            }
            throw new RegistryException(e);
        }
        
        
        so.setOutput(outputContext);
        
        // Setting this error list is redundant, but Content Validation Services
        // currently output a Boolean and a RegistryErrorList, so using
        // same mechanism to report errors from Content Filtering Services.
        so.setErrorList(outputContext.getErrorList());
        
        if (outputContext != context) {
            outputContext.commit();
        }
        
        return so;
    }
    
    /**
     * Runs XSLT based upon specified inputs and returns the outputfile.
     *
     * TODO: Need some refactoring to make this reusable throughout OMAR
     * particularly in CanonicalXMLCatalogingService.
     */
    protected static File runXSLT(
            StreamSource input,
            StreamSource xslt,
            URIResolver resolver,
            HashMap<String, String> params) throws RegistryException {
        
        File outputFile = null;
        
        try {
            //dumpStream(xslt);
            
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = initTransformer(tFactory, xslt);
            // Use FilteringService URIResolver to resolve RIs submitted in the
            // ServiceInput object
            transformer.setURIResolver(resolver);
            //Set respository item as parameter
            
            //Create the output file with the filtered RegistryObject Metadata
            outputFile = File.createTempFile("CanonicalXMLFilteringService_Output",
                    ".xml");
            outputFile.deleteOnExit();
            
            log.debug("outputFile= " + outputFile.getAbsolutePath());
            
            Iterator<String> iter = params.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                Object value = params.get(key);
                transformer.setParameter(key, value);
            }
            
            StreamResult sr = new StreamResult(outputFile);
            transformer.transform(input, sr);
            
        } catch (Exception e)  {
            throw new RegistryException(e);
        }
        
        return outputFile;
        
    }
    
    /*
     * This method is used to create and initialize a Transformer instance
     */
    private static Transformer initTransformer(TransformerFactory tFactory,
            StreamSource xslt)
            throws TransformerConfigurationException {
        Transformer transformer = tFactory.newTransformer(xslt);
        transformer.setErrorListener(new ErrorListener() {
            public void error(TransformerException exception)
            throws TransformerException {
                log.info(exception);
            }
            
            public void fatalError(TransformerException exception)
            throws TransformerException {
                log.error(exception);
                throw exception;
            }
            
            public void warning(TransformerException exception)
            throws TransformerException {
                log.info(exception);
            }
        });
        return transformer;
    }
    
    /**
     * A URIResolver for RepositoryItem objects
     *
     */
    class FilteringServiceURIResolver implements URIResolver {
        private ServerRequestContext context;
        protected FilteringServiceURIResolver(ServerRequestContext context) {
            this.context = context;
        }
        @SuppressWarnings("unused")
		public Source resolve(String href, String base) throws TransformerException {
            Source source = null;
            try {
                // Should this check that href is UUID URN first?
                RepositoryItem ri = (RepositoryItem)context.getRepositoryItemsMap().get(href);
                if (ri == null) {
                    // if RI is not submitted in the ServiceInput class, use
                    // the StreamSource to create a Source instance using the
                    // href as the URI string.
                    try {
                        source = new StreamSource(href);
                        if (source == null) {
                            source = getSourceFromRepositoryManager(href);
                        }
                    } catch (TransformerException te) {
                        throw te;
                    } catch (Throwable ex) {
                        // if RI not found in the Map and a StreamSource could
                        // not resolve the URI, use the RepositoryManager
                        source = getSourceFromRepositoryManager(href);
                    }
                } else {
                    source = new StreamSource(ri.getDataHandler().getInputStream());
                }
            } catch (TransformerException ex) {
                throw ex;
            } catch (Throwable t) {
                throw new TransformerException(t);
            }
            return source;
        }
    }
    
    private Source getSourceFromRepositoryManager(String href)
    throws TransformerException {
        Source source = null;
        try {
            source = RepositoryManagerFactory.getInstance()
            .getRepositoryManager()
            .getAsStreamSource(href);
            if (source == null) {
                // RM could not resolve the URI throw exception
                throw new TransformerException(ServerResourceBundle.getInstance()
                .getString("message.error.couldNotResolveURI",
                        new Object[] {href}));
            }
        } catch (Throwable t) {
            throw new TransformerException(t);
        }
        return source;
    }
    
    /**
     * Gets a <code>URIResolver</code> that handles locating repository items
     *
     * @return an <code>URIResolver</code> value
     */
    public URIResolver getURIResolver(ServerRequestContext context) {
        return new FilteringServiceURIResolver(context);
    }
    
}

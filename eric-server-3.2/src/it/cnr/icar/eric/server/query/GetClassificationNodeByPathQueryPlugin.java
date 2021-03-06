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
package it.cnr.icar.eric.server.query;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.spi.RequestContext;
import it.cnr.icar.eric.server.cache.ServerCache;
import it.cnr.icar.eric.server.common.ServerRequestContext;

import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;

/**
 * The QueryPlugin for GetClassificationNodeByPath special parameterized query.
 * 
 * @author Paul Sterk
 *
 */
public class GetClassificationNodeByPathQueryPlugin extends AbstractQueryPlugin {
    
    private Log log = LogFactory.getLog(GetClassificationNodeByPathQueryPlugin.class);

    
    public void processRequest(RequestContext context) throws RegistryException {
        ServerRequestContext serverContext = ServerRequestContext.convert(context);
        
        Map<?, ?> queryParamsMap = serverContext.getQueryParamsMap();
        String path = (String)queryParamsMap.get("$path");
        
        ArrayList<JAXBElement<ClassificationNodeType>> results = new ArrayList<JAXBElement<ClassificationNodeType>>();
        ClassificationNodeType ebClassificationNodeType = ServerCache.getInstance().getClassificationNodeByPath(path);

        
        if (ebClassificationNodeType != null) {
            // wrap as element
            JAXBElement<ClassificationNodeType> ebClassificationNode = BindingUtility.getInstance().rimFac.createClassificationNode(ebClassificationNodeType);
            results.add(ebClassificationNode);
        }
        // Need to set serverContext.specialQueryResults so that query is 
        // detected as special query, but don't add null as result or server will 
        // try to check access to null.
        serverContext.setSpecialQueryResults(results);        
    }

    public String getId() {
        return CanonicalConstants.CANONICAL_QUERY_GetClassificationNodeByPath;
    }

    public InternationalStringType getName() {
        InternationalStringType is = null;
        try {
            //??I18N: Need a ResourceBundle class for minDB
            is = bu.createInternationalStringType("GetClassificationNodeByPath");
        } catch (JAXRException e) {
            //can't happen
            log.error(e);
        }
        
        return is;
    }

    public InternationalStringType getDescription() {
        InternationalStringType is = null;
        try {
            //??I18N: Need a ResourceBundle class for minDB
            is = bu.createInternationalStringType("GetClassificationNodeByPath");
        } catch (JAXRException e) {
            //can't happen
            log.error(e);
        }
        
        return is;
    }

}

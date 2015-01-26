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
package it.cnr.icar.eric.client.ui.thin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.registry.JAXRException;

import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.client.xml.registry.infomodel.AdhocQueryImpl;


import javax.xml.registry.infomodel.InternationalString;
import it.cnr.icar.eric.client.xml.registry.infomodel.InternationalStringImpl;
import it.cnr.icar.eric.client.ui.common.conf.bindings.QueryType;
import it.cnr.icar.eric.client.ui.common.conf.bindings.ParameterType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.context.FacesContext;

/**
  *
  * @author  dhilder
  */
public class QueryBean extends java.lang.Object implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 2915009827164850420L;
	private String name;
    private String description;
    private InternationalString i18nName;
    private InternationalString i18nDescription;
    private String queryId;
    private String queryString;
    private Map<?, ?> parameters;
    private Log log = LogFactory.getLog(QueryBean.class);
    
   
    public QueryBean(QueryType query, AdhocQueryImpl adhocQuery) 
        throws JAXRException {
        this.queryId = query.getAdhocQueryRef().getId();
        this.i18nName = adhocQuery.getName();
        this.name = getLocalizedValue(i18nName);
        this.i18nDescription = adhocQuery.getDescription();
        this.description = getLocalizedValue(i18nDescription);
        this.queryString = adhocQuery.toString();
        this.parameters = getParameterBeans(query);
    }
    
    public QueryBean(String name, String description, String queryId, String queryString) {
        this.name = name;
        this.description = description;
        this.queryId = queryId;
        this.queryString = queryString;
        parameters = parseParameters(queryString);
    }
    
    private Map<String, ParameterBean> getParameterBeans(QueryType query) {
        TreeMap<String, ParameterBean> beans = new TreeMap<String, ParameterBean>();
        Iterator<ParameterType> queryItr = query.getParameter().iterator();
        while (queryItr.hasNext()) {
            ParameterType param = queryItr.next();
            String value = param.getParameterName();
            // Replace the dot with an underscore to match the method binding
            // created by the QueryPanelComponent. Reason is that JSF will
            // misinterpret the '.' as a method dereference in the method binding.
            // The replacement must only happen in the key. The value must 
            // retain the '.' so it matches the value of the query parameter
            // in the stored query.
            String paramBeanKey = value.replace('.', '_');
            beans.put(paramBeanKey, new ParameterBean(value));
        }
        return beans;
    }
    
    private String getLocalizedValue(InternationalString i18n) throws JAXRException {
        String value = ((InternationalStringImpl)i18n).getClosestValue(FacesContext.getCurrentInstance().getViewRoot().getLocale());
        
        return value;
    }
   
    private Map<String, ParameterBean> parseParameters(String s) {
        TreeMap<String, ParameterBean> p = new TreeMap<String, ParameterBean>();
        s = s.trim();
        int index = s.lastIndexOf(';');
        if (index != -1) {
            s = s.substring(0, index);
        }
        String[] tokens = s.split("[ ,'()=%;]");
        for (int i=0; i<tokens.length; i++) {
            if (tokens[i].charAt(0) == '$') {
                // Assume trailing numbers indicate a multi value. Trim
                // them off and just create a single parameter.
                String[] arrayToken = tokens[i].split("[0-9]$");
                tokens[i] = arrayToken[0];
                p.put(tokens[i], new ParameterBean(tokens[i]));
            }
        }
        return p;
    }
    
    public String getName() {
        String value = name;
        try {
            if (i18nName != null) {
                value = getLocalizedValue(i18nName);
            }
	} catch (JAXRException e) {
            log.error(e, e);
        }
        return value;
    }

    public void setName(String name) {
        log.debug("Setting current query name to:" + name);
    }
    
    public String getDescription() {
        String value = description;
        try {
            if (i18nDescription != null) {
                value = getLocalizedValue(i18nDescription);
            }
	} catch (JAXRException e) {
            log.error(e, e);
        }
        return value;
    }
    
    public String getQueryId() {
        return queryId;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public Map<?, ?> getParameters() {
        return parameters;
    }
    
    public Map<String, String> getQueryParameters() {
        HashMap<String, String> queryParameters = new HashMap<String, String>();
        String queryIdURN = CanonicalConstants.CANONICAL_SLOT_QUERY_ID;
        queryParameters.put(queryIdURN, queryId);
        Iterator<?> parameterBeans = parameters.values().iterator();
        while (parameterBeans.hasNext()) {
            ParameterBean parameterBean = (ParameterBean)parameterBeans.next();
            log.debug("Param Bean text value: " + parameterBean.getTextValue());
            parameterBean.addQueryParameters(queryParameters);
        }
        return queryParameters;
    }
    
}

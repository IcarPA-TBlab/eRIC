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
package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;

import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;


/**
 * Implementation of the AdhocQueryType.
 * @author <a href="mailto:Paul.Sterk@Sun.COM">Paul Sterk</a>
 */
public class AdhocQueryImpl extends RegistryObjectImpl implements Query {

    private int queryType;
    private String queryString="";
    
    public AdhocQueryImpl(LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);
    }

    public AdhocQueryImpl(LifeCycleManagerImpl lcm,
        AdhocQueryType adhocQueryObj) throws JAXRException {
        super(lcm, adhocQueryObj);

        QueryExpressionType queryExp = adhocQueryObj.getQueryExpression();
        String queryLang = queryExp.getQueryLanguage();
        if (queryLang.equals(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92)) {
            queryString = (String)queryExp.getContent().get(0);
        } else {
            throw new JAXRException(JAXRResourceBundle.getInstance().getString("message.error.unsupported.querylanguage", new Object[] {queryLang}));
        }
    }
    
    public int getType() {
        return queryType;
    }
    
    public void setType(int queryType) {
        this.queryType = queryType;
    }
    
    public String toString() {
        return queryString ;
    }
    
    public void setString(String queryString) {
        this.queryString = queryString;
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;
        
		AdhocQueryType ebAdhocQueryType = factory.createAdhocQueryType();
		setBindingObject(ebAdhocQueryType);

//		JAXBElement<AdhocQueryType> ebAdhocQuery = factory.createAdhocQuery(ebAdhocQueryType);

		return ebAdhocQueryType;
    }
    
    protected void setBindingObject(AdhocQueryType ebAdhocQueryType)
    throws JAXRException {
        super.setBindingObject(ebAdhocQueryType);

        ObjectFactory factory = BindingUtility.getInstance().rimFac;

		QueryExpressionType qeType = factory.createQueryExpressionType();
		qeType.setQueryLanguage(getQueryTypeId());
		qeType.getContent().add(queryString);
		ebAdhocQueryType.setQueryExpression(qeType);
    }

    
    private String getQueryTypeId() {
        String queryTypeBinding = null;
        
        switch (queryType) {
            case Query.QUERY_TYPE_SQL:
                queryTypeBinding = BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92;
                break;
            
            case Query.QUERY_TYPE_EBXML_FILTER_QUERY:
                queryTypeBinding = BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_ebRSFilterQuery;
                break;
            
            case Query.QUERY_TYPE_XQUERY:
                queryTypeBinding = BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_XQuery;
                break;
        }
        return queryTypeBinding;
    }
    
}

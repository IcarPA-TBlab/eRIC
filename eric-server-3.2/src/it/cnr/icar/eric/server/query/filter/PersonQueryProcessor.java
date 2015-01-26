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
package it.cnr.icar.eric.server.query.filter;

import java.util.Iterator;
import java.util.List;
import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.FilterType;
import org.oasis.ebxml.registry.bindings.query.PersonQueryType;


/**
 * Processor for ExternalIdentifierQuerys.
 * Processes a ExternalIdentifierQueryType and converts it to an SQL Query string
 * using the process() method.
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
class PersonQueryProcessor extends RegistryObjectQueryProcessor {
    
    public PersonQueryProcessor(FilterQueryProcessor parentQueryProcessor, FilterQueryType filterQuery) throws RegistryException {
        super(parentQueryProcessor, filterQuery);
    }
    
    
    /**
     * Builds the SQL predicates for the primary and additional filters for this FilterQuery
     *
     */
    protected String processFilters() throws RegistryException {
        //Processing of primary filter if any are done by super class
        String filterPredicates = super.processFilters();        
                
        //AddressFilter
        //SELECT p.* FROM Person p, PostalAddress a WHERE ((a.parent = p.id) AND (a ...));         
        List<FilterType> addressFilters = ((PersonQueryType)filterQuery).getAddressFilter();
        if (addressFilters != null) {
            Iterator<FilterType> iter = addressFilters.iterator();
            while (iter.hasNext()) {
                FilterType filter = iter.next();
                String addressAlias = getAliasForTable("PostalAddress");
                FilterProcessor filterProcessor = FilterProcessor.newInstance(this, filter);
                filterProcessor.setAlias(addressAlias);
                String filterPred = filterProcessor.process() + " ";
                String relationshipPred = "(" + alias + ".id = " + addressAlias + ".parent) ";
                filterPredicates = appendPredicate(filterPredicates, filterPred, relationshipPred);
            }            
        }        
        
        //PersonNameFilter is special case since PersoName table is flattened into Person table
        //Treat like PrimaryFilter
        //SELECT p.* FROM Person p, PersonName n WHERE ((n.parent = p.id) AND (n ...));         
        FilterType pnameFilter = ((PersonQueryType)filterQuery).getPersonNameFilter();
        if (pnameFilter != null) {
            FilterProcessor filterProcessor = FilterProcessor.newInstance(this, pnameFilter);
            String filterPred = filterProcessor.process() + " ";
            filterPredicates = appendPredicate(filterPredicates, filterPred);                        
        }        
        
        //TelephoneNumberFilter
        //SELECT p.* FROM Person p, TelephoneNumber t WHERE ((t.parent = p.id) AND (t ...));         
        List<FilterType> phoneFilters = ((PersonQueryType)filterQuery).getTelephoneNumberFilter();
        if (phoneFilters != null) {
            Iterator<FilterType> iter = phoneFilters.iterator();
            while (iter.hasNext()) {
                FilterType filter = iter.next();
                String phoneAlias = getAliasForTable("TelephoneNumber");
                FilterProcessor filterProcessor = FilterProcessor.newInstance(this, filter);
                filterProcessor.setAlias(phoneAlias);
                String filterPred = filterProcessor.process() + " ";
                String relationshipPred = "(" + alias + ".id = " + phoneAlias + ".parent) ";
                filterPredicates = appendPredicate(filterPredicates, filterPred, relationshipPred);
            }            
        }        
        
        //EmailAddressFilter
        //SELECT p.* FROM Person p, EmailAddress a WHERE ((a.parent = p.id) AND (a ...));         
        List<FilterType> emailFilters = ((PersonQueryType)filterQuery).getEmailAddressFilter();
        if (emailFilters != null) {
            Iterator<FilterType> iter = emailFilters.iterator();
            while (iter.hasNext()) {
                FilterType filter = iter.next();
                String emailAlias = getAliasForTable("EmailAddress");
                FilterProcessor filterProcessor = FilterProcessor.newInstance(this, filter);
                filterProcessor.setAlias(emailAlias);
                String filterPred = filterProcessor.process() + " ";
                String relationshipPred = "(" + alias + ".id = " + emailAlias + ".parent) ";
                filterPredicates = appendPredicate(filterPredicates, filterPred, relationshipPred);
            }            
        }        
        
        return filterPredicates;
    }  
    
}

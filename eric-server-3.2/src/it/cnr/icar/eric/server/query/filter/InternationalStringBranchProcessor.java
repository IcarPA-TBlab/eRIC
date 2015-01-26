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
import org.oasis.ebxml.registry.bindings.query.InternationalStringBranchType;


/**
 * BranchProcessor for processing InternationStringBranchTypes.
 * Processes a InternationStringBranchType and converts it to an SQL predicate
 * using the process() method.
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
class InternationalStringBranchProcessor extends BranchProcessor {
    
    public InternationalStringBranchProcessor(FilterQueryProcessor parentQueryProcessor, FilterQueryType filterQuery) throws RegistryException {
        super(parentQueryProcessor, filterQuery);
    }
            
    /**
     * Builds the SQL predicates for the primary and additional filters for this Branch
     *
     * SELECT ro.* FROM RegistryObject ro, VersionInfo v WHERE (ro.status == "<status>") AND (v.parent = ro.id AND v.versionName = "1.2");
     *
     */
    protected String processFilters() throws RegistryException {
        //Processing of primary filter if any are done by super class
        String filterPredicates = super.processFilters();
        
        //Process secondary filters if any
        List<FilterType> localizedStringFilters = ((InternationalStringBranchType)filterQuery).getLocalizedStringFilter();
        Iterator<FilterType> iter = localizedStringFilters.iterator();
        while (iter.hasNext()) {
            FilterType localizedStringFilter = iter.next();
            if (localizedStringFilter != null) {            
                FilterProcessor localizedStringFilterProcessor = FilterProcessor.newInstance(this, localizedStringFilter);
                                
                filterPredicates = appendPredicate(filterPredicates, localizedStringFilterProcessor.process() + " ");
            }     
        }
                        
        return filterPredicates;
    }
    
    /**
     * Gets the primary key column name for a RegistryObject
     */
    String getPrimaryKeyColumn() {
        //TODO: Primary key can consist of multiple columns (parent, lang). How to handle this?
        return "parent";        
    }
}

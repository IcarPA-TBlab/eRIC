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

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.CompoundFilterType;
import org.oasis.ebxml.registry.bindings.query.FilterType;

/**
 * FilterProcessor subtype for processing CompoundFilters
 * A CompoundFilter contains two sub-filters that are
 * combined using a logicalOPerator like "AND" or "OR".
 * 
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
class CompoundFilterProcessor extends FilterProcessor {
    
    public CompoundFilterProcessor(FilterQueryProcessor parentQueryProcessor, FilterType filter) throws RegistryException {
        super(parentQueryProcessor, filter);
    }
    
    
    /**
     * Builds the SQL predicates for the CompoundFilter.
     * Processes left and right filter and combine their predicates using logicalOperator.
     */
    public String processInternal() throws RegistryException {        
        String filterPredicate = null;
        
        FilterType leftFilter = ((CompoundFilterType)filter).getLeftFilter();
        FilterProcessor leftFilterProcessor = FilterProcessor.newInstance(parentQueryProcessor, leftFilter);
        
        FilterType rightFilter = ((CompoundFilterType)filter).getRightFilter();
        FilterProcessor rightFilterProcessor = FilterProcessor.newInstance(parentQueryProcessor, rightFilter);
        String logicalOp = ((CompoundFilterType)filter).getLogicalOperator().toString();
        
        filterPredicate = " (" + leftFilterProcessor.process() + " " + logicalOp + " " + rightFilterProcessor.process() + ") ";
        
        return filterPredicate;
   }            
}

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

import org.oasis.ebxml.registry.bindings.query.ExternalIdentifierQueryType;
import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;


/**
 * Processor for ExternalIdentifierQuerys.
 * Processes a ExternalIdentifierQueryType and converts it to an SQL Query string
 * using the process() method.
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
class ExternalIdentifierQueryProcessor extends RegistryObjectQueryProcessor {
    
    public ExternalIdentifierQueryProcessor(FilterQueryProcessor parentQueryProcessor, FilterQueryType filterQuery) throws RegistryException {
        super(parentQueryProcessor, filterQuery);
    }
        
    /**
     * Builds the SQL clauses for the sub-queries of this FilterQuery.
     *
     *
     */
    protected String processSubQueries() throws RegistryException {
        String subQueryPredicate = super.processSubQueries();
                
        //RegistryObjectQuery      
        RegistryObjectQueryType roQuery = ((ExternalIdentifierQueryType)filterQuery).getRegistryObjectQuery();
         
        if (roQuery != null) {
                  
            RegistryObjectQueryProcessor subQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, roQuery);
            String roAlias = getAliasForTable("RegistryObject"); 
            subQueryProcessor.setAlias(roAlias);

            String roQueryPred = subQueryProcessor.process() + " ";

            String relationShipPred = "(" + alias + ".registryObject = " + roAlias + ".id) ";
            subQueryPredicate = appendPredicate(subQueryPredicate, roQueryPred, relationShipPred);            
        }
        
        //IdentificationSchemeQuery        
        //SELECT ro.* FROM ExternalIdentifier e, ClassificationScheme s WHERE ((e.identificationScheme = s.id) AND (s ...))
        roQuery = ((ExternalIdentifierQueryType)filterQuery).getIdentificationSchemeQuery();
         
        if (roQuery != null) {         
            RegistryObjectQueryProcessor roQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, roQuery);

            String schemeAlias = getAliasForTable("ClassScheme"); 
            roQueryProcessor.setAlias(schemeAlias);
            
            String schemeQueryPred = roQueryProcessor.process() + " ";

            String relationShipPred = "(" + alias + ".identificationScheme = " + schemeAlias + ".id) ";
            subQueryPredicate = appendPredicate(subQueryPredicate, schemeQueryPred, relationShipPred);            
        }
                
        
        
        return subQueryPredicate;
    }
    
}

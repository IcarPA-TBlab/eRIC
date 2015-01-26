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

import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.InternationalStringBranchType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.SpecificationLinkQueryType;


/**
 * Processor for ExternalIdentifierQuerys.
 * Processes a ExternalIdentifierQueryType and converts it to an SQL Query string
 * using the process() method.
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
class SpecificationLinkQueryProcessor extends RegistryObjectQueryProcessor {
    
    public SpecificationLinkQueryProcessor(FilterQueryProcessor parentQueryProcessor, FilterQueryType filterQuery) throws RegistryException {
        super(parentQueryProcessor, filterQuery);
    }
            
    /**
     * Processes branches, if any, of this RegistryObjectQuery and
     * builds the SQL clauses for those branches.
     *
     *
     */
    protected String processBranches() throws RegistryException {
        String branchPredicate = super.processBranches();
                
        //UsageDescription Table in SQL schema is handled special because InternationalString 
        //and LocalizedString tables have been flattened into a Name table
        //As such, they can be processed in a manner similar to secondary filters
        InternationalStringBranchType usageDescBranch = ((SpecificationLinkQueryType)filterQuery).getUsageDescriptionBranch();
        if (usageDescBranch != null) {            
            FilterQueryProcessor usageDescBranchProcessor = FilterQueryProcessor.newInstance(this, usageDescBranch);
            usageDescBranchProcessor.setForeignKeyColumn("parent");
            usageDescBranchProcessor.setAlias(getAliasForTable("UsageDescription"));
            
            branchPredicate = appendPredicate(branchPredicate, usageDescBranchProcessor.process() + " ");            
        }        
        
        return branchPredicate;
    }
    
    /**
     * Builds the SQL clauses for the sub-queries of this FilterQuery.
     *
     * SELECT ro.* FROM RegistryObject ro WHERE ro.id IN (SELECT c.id FROM Classification c WHERE c.registryObject = ro.id AND c.value = "somevalue");
     *
     */
    protected String processSubQueries() throws RegistryException {
        String subQueryPredicate = super.processSubQueries();
                
        //ServiceBindingQuery
        RegistryObjectQueryType roQuery = ((SpecificationLinkQueryType)filterQuery).getServiceBindingQuery();
         
        if (roQuery != null) {         
            RegistryObjectQueryProcessor roQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, roQuery);

            String roAlias = getAliasForTable("ServiceBinding"); 
            roQueryProcessor.setAlias(roAlias);
            
            String roQueryPred = roQueryProcessor.process() + " ";

            String relationShipPred = "(" + alias + ".serviceBinding = " + roAlias + ".id) ";
            subQueryPredicate = appendPredicate(subQueryPredicate, roQueryPred, relationShipPred);            
        }
        
        //SpecificationObjectQuery     
        RegistryObjectQueryType specQuery = ((SpecificationLinkQueryType)filterQuery).getSpecificationObjectQuery();
         
        if (specQuery != null) {
                  
            RegistryObjectQueryProcessor subQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, specQuery);
            String specAlias = getAliasForTable("RegistryObject"); 
            subQueryProcessor.setAlias(specAlias);

            String specQueryPred = subQueryProcessor.process() + " ";

            String relationShipPred = "(" + alias + ".specificationObject = " + specAlias + ".id) ";
            subQueryPredicate = appendPredicate(subQueryPredicate, specQueryPred, relationShipPred);            
        }
        return subQueryPredicate;
    }
    
}

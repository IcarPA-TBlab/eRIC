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

import org.oasis.ebxml.registry.bindings.query.AuditableEventQueryType;
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
class AuditableEventQueryProcessor extends RegistryObjectQueryProcessor {
    
    public AuditableEventQueryProcessor(FilterQueryProcessor parentQueryProcessor, FilterQueryType filterQuery) throws RegistryException {
        super(parentQueryProcessor, filterQuery);
    }
    
    
    /**
     * Builds the SQL clauses for the sub-queries of this FilterQuery.
     *
     */
    protected String processSubQueries() throws RegistryException {
        String subQueryPredicate = super.processSubQueries();

        /*
        //RegistryObjectQuerys
        List roQueries = ((AuditableEventQueryType)filterQuery).getAffectedObjectQuery();
         
        if (roQueries != null) {
         
            Iterator iter = roQueries.iterator();
            while (iter.hasNext()) {
                FilterQueryType subQuery = (FilterQueryType)iter.next();
         
                RegistryObjectQueryProcessor roQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, subQuery);
                String roAlias = getAliasForTable("RegistryObject"); 
                roQueryProcessor.setAlias(roAlias);
                
                String roPred = roQueryProcessor.process() + " ";
                
                String relationShipPred = "(" + alias + ".id = " + nodeAlias + ".id) ";
                subQueryPredicate = appendPredicate(subQueryPredicate, roPred, relationShipPred);            
            }
        }
         */
        
        //eventTypeQuery
        //SELECT ro.* FROM AuditableEvent e, ClassificationNode n WHERE ((e.eventTypeType = n.id) AND (n ...))
        RegistryObjectQueryType roQuery = ((AuditableEventQueryType)filterQuery).getEventTypeQuery();
         
        if (roQuery != null) {         
            RegistryObjectQueryProcessor roQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, roQuery);

            String nodeAlias = getAliasForTable("ClassificationNode"); 
            roQueryProcessor.setAlias(nodeAlias);
            
            String nodeQueryPred = roQueryProcessor.process() + " ";

            String relationShipPred = "(" + alias + ".eventType = " + nodeAlias + ".id) ";
            subQueryPredicate = appendPredicate(subQueryPredicate, nodeQueryPred, relationShipPred);            
        }
        
        //UserQuery
        //SELECT ro.* FROM AuditableEvent e, User_ u WHERE ((e.user_ = u.id) AND (u ...))
        roQuery = ((AuditableEventQueryType)filterQuery).getUserQuery();
         
        if (roQuery != null) {         
            RegistryObjectQueryProcessor roQueryProcessor = (RegistryObjectQueryProcessor)FilterQueryProcessor.newInstance(this, roQuery);

            String userAlias = getAliasForTable("User_"); 
            roQueryProcessor.setAlias(userAlias);
            
            String userQueryPred = roQueryProcessor.process() + " ";

            String relationShipPred = "(" + alias + ".user_ = " + userAlias + ".id) ";
            subQueryPredicate = appendPredicate(subQueryPredicate, userQueryPred, relationShipPred);            
        }
        
        return subQueryPredicate;
    }
    
}

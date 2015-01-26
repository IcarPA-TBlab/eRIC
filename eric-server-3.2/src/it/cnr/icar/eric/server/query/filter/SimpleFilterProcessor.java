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

import it.cnr.icar.eric.server.util.ServerResourceBundle;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.SimpleFilterType.Comparator;
import org.oasis.ebxml.registry.bindings.query.FilterType;
import org.oasis.ebxml.registry.bindings.query.SimpleFilterType;

/**
 * FilterProcessor subtype for processing SimpleFilters
 *
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
abstract class SimpleFilterProcessor extends FilterProcessor {
    private String filterColumn =  null;
    //private String tableName = null;
    
    SimpleFilterProcessor(FilterQueryProcessor parentQueryProcessor, FilterType filter) throws RegistryException {
        super(parentQueryProcessor, filter);
        filterColumn = it.cnr.icar.eric.common.Utility.getInstance().mapColumnName(((SimpleFilterType)filter).getDomainAttribute());
        
        //tableName = it.cnr.icar.eric.common.Utility.getInstance().getClassNameNoPackage(getParentFilterQuery());
    }
    
    /*
     * Must be called by the parentQueryProcessor after creating this object.
     *
    public void setFilterDomainClass(String filterDomainClass) {
        tableName = it.cnr.icar.eric.common.Utility.getInstance().mapTableName(filterDomainClass);
    }*/
            
    /**
     * Builds the SQL predicates for the SimpleFilter.
     * Join predicate is added by the parentQueryProcessor
     * and not this Filter.
     *
     * Sample query (1) below exemplifies filterPredicate where join is not required.
     * Sample query (2) below exemplifies filterPredicate where join is required.
     *
     * 1. SELECT ro.* FROM RegistryObject ro WHERE
     *     (ro.status == "<status>")
     *
     * 2. SELECT ro.* FROM RegistryObject ro, VersionInfo v WHERE
     *     (v.parent = ro.id AND v.versionName = "1.2");
     *
     * ro = parentAlias
     * v = alias
     * status = filterColumn
     * versionName = filterColumn
     * parent = foreignKeyColumn
     * id = parent.primaryKeyColumn
     *
     */
    protected String processInternal() throws RegistryException {
        String filterPredicate = null;
        String alias = getAlias();
        
        filterPredicate = "(" + alias + "." + filterColumn +
                mapComparatorToSQL(((SimpleFilterType)filter).getComparator()) +
                getValue() + ") ";        
        
        return filterPredicate;
    }
    
    abstract protected String getValue() throws RegistryException;
    
    private String mapComparatorToSQL(Comparator comparator) throws RegistryException {
        String sqlComparator = null;
        
        if (comparator == Comparator.EQ) {
            sqlComparator = " = ";
        } else if (comparator == Comparator.GE) {
            sqlComparator = " >= ";
        } else if (comparator == Comparator.GT) {
            sqlComparator = " > ";
        } else if (comparator == Comparator.LE) {
            sqlComparator = " <= ";
        } else if (comparator == Comparator.LIKE) {
            sqlComparator = " LIKE ";
        } else if (comparator == Comparator.LT) {
            sqlComparator = " < ";
        } else if (comparator == Comparator.NE) {
            sqlComparator = " != ";
        } else if (comparator == Comparator.NOT_LIKE) {
            sqlComparator = " NOT LIKE ";
        } else {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unsuportedComparatorAttribute"));
        }
        
        return sqlComparator;
    }
    
}

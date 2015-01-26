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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.FilterQueryType;
import org.oasis.ebxml.registry.bindings.query.FilterType;


/**
 * Based class for types of FilterProcessor clases.
 * 
 * @author Nikola Stojanovic
 * @author Farrukh Najmi
 *
 */
abstract class FilterProcessor {
    protected FilterType filter = null;
    protected FilterQueryProcessor parentQueryProcessor = null;
    protected String alias = null;

    @SuppressWarnings("unused")
	private FilterProcessor() {        
    }
    
    public FilterProcessor(FilterQueryProcessor parentQueryProcessor, FilterType filter) throws RegistryException {
        this.parentQueryProcessor = parentQueryProcessor;
        this.filter = filter;
    }
    
    FilterQueryType getParentFilterQuery() {
        return parentQueryProcessor.getFilterQuery();
    }

    void setAlias(String alias) {
        this.alias = alias;
    }
    
    String getAlias() {
        if (alias == null) {
            //Handles case where filter is a secondary filter
            alias = parentQueryProcessor.getAlias();
        }
        
        return alias;
    }
    
    protected boolean isPrimaryFilter() {
        return ((getParentFilterQuery().getPrimaryFilter()) == filter);
    }
    
    /*
     * Returns whether the predicate for this Filter requires a join between the parent table and the 
     * filter domain class table.
     *
     * Note that primayFilters never require joins while secondary filter typically do.
     * The exception is when a RIM class has been flattened into parent class in SQL schema
     * (e.g. VersionInfo in RegistryObject). The way to distinguish teh special case is that
     * the
     *
     */
    protected boolean requiresJoin() {
        return !isPrimaryFilter();
    }
    
    static FilterProcessor newInstance(FilterQueryProcessor parentQueryProcessor, FilterType filter) throws RegistryException {
        FilterProcessor filterProcessor = null;
        
        String className = it.cnr.icar.eric.common.Utility.getInstance().getClassNameNoPackage(filter);
        if (className.endsWith("Impl")) {
            className = className.substring(0, className.length()-4);   //Remove "Impl" suffix.
        }
        if (className.endsWith("Type")) {
            className = className.substring(0, className.length()-4);   //Remove "Type" suffix.
        }
        
        className = "it.cnr.icar.eric.server.query.filter." + className + "Processor";
        
        try {            
            Class<?> filterProcessorClass = Class.forName(className);
            
            Class<?>[] parameterTypes = new Class[2];
            parameterTypes[0] = FilterQueryProcessor.class;
            parameterTypes[1] = FilterType.class;
            Constructor<?> constructor = filterProcessorClass.getConstructor(parameterTypes);
            
            Object[] parameterValues = new Object[2];
            parameterValues[0] = parentQueryProcessor;                
            parameterValues[1] = filter;                
            filterProcessor = (FilterProcessor) constructor.newInstance(parameterValues);    
            
            //filterProcessor = new CompoundFilterProcessor(parentQueryProcessor, filter);
        } catch (ClassNotFoundException e) {
            throw new RegistryException(e);
        } catch (NoSuchMethodException e) {
            throw new RegistryException(e);
        } catch (IllegalArgumentException e) {
            throw new RegistryException(e);
        } catch (IllegalAccessException e) {
            throw new RegistryException(e);
        } catch (InvocationTargetException e) {
            throw new RegistryException(e);
        } catch (ExceptionInInitializerError e) {
            throw new RegistryException(e);
        } catch (InstantiationException e) {
            throw new RegistryException(e);
        }            
        
        return filterProcessor;
    }
    
    /*
     * Converts filter to an SQL predicate
     */
    public String process() throws RegistryException {
        String filterPredicate = processInternal();
        
        if (filter.isNegate()) {
            filterPredicate = " ( NOT (" + filterPredicate + ")) ";
        }
        
        return filterPredicate;
    }
    
    protected abstract String processInternal() throws RegistryException;
    
}

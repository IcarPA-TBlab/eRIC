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
package it.cnr.icar.eric.server.query.sql;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.IterativeQueryParams;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


/**
 * Processor for SQL queries. Used by the QueryManagerImpl.
 *
 * @see
 * @author Farrukh S. Najmi
 */
public class SQLQueryProcessor {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private SQLQueryProcessor _sqlQueryProcessor; */
    private static SQLQueryProcessor instance = null;
    private Log log = LogFactory.getLog(this.getClass());
    boolean bypassSQLParser = false;

    protected SQLQueryProcessor() {
        bypassSQLParser = Boolean.valueOf(RegistryProperties.getInstance()
            .getProperty("it.cnr.icar.eric.server.query.sql.SQLQueryProcessor.bypassSQLParser", "false")).booleanValue();
    }

    public RegistryObjectListType executeQuery(ServerRequestContext context, UserType user, String sqlQuery,
        ResponseOptionType responseOption, IterativeQueryParams paramHolder) throws RegistryException {
        RegistryObjectListType ebRegistryObjectListType = null;
        log.debug("unparsed query: " + sqlQuery + ";");
        try {
            ebRegistryObjectListType = BindingUtility.getInstance().rimFac.createRegistryObjectListType();

            //Fix the query according to the responseOption to return the right type of objects
            String fixedQuery = sqlQuery;
            String tableName=null;
            
            if (!bypassSQLParser) {
                //parse the queryString to get at certain info like the select column and table name etc.
                InputStream stream = new ByteArrayInputStream(sqlQuery.getBytes("utf-8"));
                SQLParser parser = new SQLParser(new InputStreamReader(stream, "utf-8"));
                fixedQuery = parser.processQuery(user, responseOption);
                log.debug("Fixed query: " + fixedQuery + ";");
                tableName = parser.firstTableName;
            } else {
                String[] strs = sqlQuery.toUpperCase().split(" FROM ");
                if (strs.length > 1) {
                    tableName = (strs[1].split(" "))[0];
                }
                //tableName = sqlQuery.substring(sqlQuery.indexOf("FROM"));
            }
            if (log.isTraceEnabled()) {
                log.trace(ServerResourceBundle.getInstance().getString("message.executingQuery",
				                                                        new Object[]{fixedQuery}));
            }
            //Get the List of objects (ObjectRef, RegistryObject, leaf class) as
            //specified by the responeOption
            ArrayList<Object> objectRefs = new ArrayList<Object>();
            List<String> queryParams = context.getStoredQueryParams();
            if (queryParams.size() == 0) {
                queryParams = null;
            }


            log.debug("queryParams = " + queryParams);
            List<IdentifiableType> objs = PersistenceManagerFactory.getInstance()
                                                 .getPersistenceManager()
                                                 .executeSQLQuery(context, fixedQuery, queryParams,
                    responseOption, tableName, objectRefs, paramHolder);

            if (queryParams != null) {
                queryParams.clear();
            }

            List<JAXBElement<? extends IdentifiableType>> ebIdentifiableTypeList = ebRegistryObjectListType.getIdentifiable();
            
            
            if ((ebIdentifiableTypeList != null) && (objs != null))  {
            	
            	Iterator<?> iter = objs.iterator();
            	while (iter.hasNext()) {

            		// iter over ComplexTypes
            		// add as Identifiable elements into ebRegistryObjectListType
            		ebIdentifiableTypeList.add(BindingUtility.getInstance().rimFac.createIdentifiable((IdentifiableType) iter.next()));
            		
            	}
            }
           

            //BindingUtility.getInstance().getJAXBContext().createMarshaller().marshal(objs.get(0), System.err);
            //BindingUtility.getInstance().getJAXBContext().createMarshaller().marshal(sqlResult, System.err);
            //TODO: Not sure what this code was about but leaving it commented for now.

            /*
                // Attaching the ObjectRef to the response. objectsRefs contains duplicates!
                Iterator objectRefsIter = objectRefs.iterator();
                // It is to store the ObjectRef 's id after removing duplicated ObjectRef. It is a dirty fix, change it later!!!!
                List finalObjectRefsIds = new java.util.ArrayList();
                List finalObjectRefs = new java.util.ArrayList();
                while(objectRefsIter.hasNext()) {
                    Object obj = objectRefsIter.next();
                    if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRef) {
                        ObjectRef objectRef = (ObjectRef) obj;
                        String id = objectRef.getId();
                        if (!finalObjectRefsIds.contains(id)) {
                            finalObjectRefsIds.add(id);
                            ObjectRef or = new ObjectRef();
                            or.setId(id);
                            finalObjectRefs.add(or);
                        }
                    }
                    else {
                        throw new RegistryException("Unexpected object" + obj);
                    }
                }

                Iterator finalObjectRefsIter = finalObjectRefs.iterator();
                while (finalObjectRefsIter.hasNext()) {
                    Object obj = finalObjectRefsIter.next();
                    if (obj instanceof org.oasis.ebxml.registry.bindings.rim.ObjectRef) {
                        RegistryObjectListTypeTypeItem li = new RegistryObjectListTypeTypeItem();
                        li.setObjectRef((ObjectRef)obj);
                        sqlResult.addRegistryObjectListTypeTypeItem(li);
                    }
                    else {
                        throw new RegistryException("Unexpected object" + obj);
                    }
                }
            */
        } catch (UnsupportedEncodingException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } catch (ParseException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        }

        return ebRegistryObjectListType;
    }

    public synchronized static SQLQueryProcessor getInstance() {
        if (instance == null) {
            instance = new it.cnr.icar.eric.server.query.sql.SQLQueryProcessor();
        }

        return instance;
    }
}

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
package it.cnr.icar.eric.server.persistence.rdb;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.UserType;


class AuditableEventDAO extends RegistryObjectDAO {
    private static final Log log = LogFactory.getLog(AuditableEventDAO.class);

    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */


    /**
     * Use this constructor only.
     */
    AuditableEventDAO(ServerRequestContext context) {
        super(context);
    }
    
    public static String getTableNameStatic() {
        return "AuditableEvent";
    }

    public String getTableName() {
        return getTableNameStatic();
    }

    /**
     * Delete composed objects that have the specified registryObject
     * as parent.
     */
    protected void deleteComposedObjects(Object object) throws RegistryException {
        super.deleteComposedObjects(object);
        @SuppressWarnings("unused")
		AuditableEventType ae = (AuditableEventType)object;
        
        AffectedObjectDAO affectedObjectDAO = new AffectedObjectDAO(context);
        affectedObjectDAO.setParent(object);        
        
        //Delete affectedObjects
        affectedObjectDAO.deleteByParent();
        
    }
    
    /**
     * Insert the composed objects for the specified registryObject
     */
    protected void insertComposedObjects(Object object) throws RegistryException {
        super.insertComposedObjects(object);
        AuditableEventType ae = (AuditableEventType)object;
        @SuppressWarnings("unused")
		String id = ae.getId();
        
        
        AffectedObjectDAO affectedObjectDAO = new AffectedObjectDAO(context);
        affectedObjectDAO.setParent(object);        
        List<ObjectRefType> affectedObjects = ae.getAffectedObjects().getObjectRef();
        
        //Insert affectedObjects
        affectedObjectDAO.insert(affectedObjects);        
    }
    
    /**
     * Returns the SQL fragment string needed by insert or update statements 
     * within insert or update method of sub-classes. This is done to avoid code
     * duplication.
     */
	protected String getSQLStatementFragment(Object ro)
			throws RegistryException {

		AuditableEventType auditableEvent = (AuditableEventType) ro;

		String stmtFragment = null;

		try {

			String requestId = auditableEvent.getRequestId();
			String eventType = auditableEvent.getEventType();

			if (auditableEvent.getTimestamp() == null) {
				XMLGregorianCalendar timeNow;
				timeNow = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(new GregorianCalendar());

				auditableEvent.setTimestamp(timeNow);
			}
			
			Timestamp timestamp = new Timestamp(auditableEvent.getTimestamp()
					.toGregorianCalendar().getTimeInMillis());

			// ??The timestamp is being truncated to work around a bug in
			// PostgreSQL 7.2.2 JDBC driver
			String timestampStr = timestamp.toString().substring(0, 19);

			String aeUser = auditableEvent.getUser();
			if (aeUser == null) {
				UserType user = context.getUser();
				if (user != null) {
					aeUser = user.getId();
				}
			}

			if (action == DAO_ACTION_INSERT) {
				stmtFragment = "INSERT INTO AuditableEvent "
						+ super.getSQLStatementFragment(ro) + ", '" + requestId
						+ "', '" + eventType + "', '" + timestampStr + "', '"
						+ aeUser + "' ) ";
			} else if (action == DAO_ACTION_UPDATE) {
				stmtFragment = "UPDATE AuditableEvent SET "
						+ super.getSQLStatementFragment(ro) + ", requestId='"
						+ requestId + "', eventType='" + eventType
						+ "', timestamp_='" + timestampStr + "', user_='"
						+ aeUser + "' WHERE id = '"
						+ ((RegistryObjectType) ro).getId() + "' ";
			} else if (action == DAO_ACTION_DELETE) {
				stmtFragment = super.getSQLStatementFragment(ro);
			}
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return stmtFragment;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	protected void loadObject( Object obj, ResultSet rs) throws RegistryException {
        try {
            if (!(obj instanceof AuditableEventType)) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.AuditableEventExpected",
                        new Object[]{obj}));
            }

            AuditableEventType ebAuditableEventType = (AuditableEventType) obj;
            super.loadObject( obj, rs);

            //TODO: Fix so requestId is properly supported
            String requestId = rs.getString("requestId");
            if (requestId == null) {
                requestId = "Unknown";
            }
            ebAuditableEventType.setRequestId(requestId);
            
            String eventType = rs.getString("eventType");
            ebAuditableEventType.setEventType(eventType);
            
            //Workaround for bug in PostgreSQL 7.2.2 JDBC driver
            //java.sql.Timestamp timeStamp = rs.getTimestamp("timeStamp_"); --old
            String timestampStr = rs.getString("timeStamp_").substring(0,19);
            Timestamp timeStamp = Timestamp.valueOf(timestampStr);

            GregorianCalendar calendar = new GregorianCalendar();
        	calendar.setTime(timeStamp);
            XMLGregorianCalendar cal = DatatypeFactory.newInstance()
            		.newXMLGregorianCalendar(calendar);

            ebAuditableEventType.setTimestamp(cal);
            
            String userId = rs.getString("user_");
            ObjectRefType ebObjectRefType = bu.rimFac.createObjectRefType();
            ebObjectRefType.setId(userId);
            context.getObjectRefs().add(ebObjectRefType);
            ebAuditableEventType.setUser(userId);
            
            AffectedObjectDAO affectedDAO = new AffectedObjectDAO(context);
            affectedDAO.setParent(ebAuditableEventType);
            List affectedObjects = affectedDAO.getByParent();
            ObjectRefListType orefList = BindingUtility.getInstance().rimFac.createObjectRefListType();
            orefList.getObjectRef().addAll(affectedObjects);
            ebAuditableEventType.setAffectedObjects(orefList);
            
        } catch (SQLException e) {
            log.error(ServerResourceBundle.getInstance().getString("message.CaughtException1"), e);
            throw new RegistryException(e);
        } catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Creates an unitialized binding object for the type supported by this DAO.
     */
    Object createObject() throws JAXBException {
        AuditableEventType ebAuditableEventType = null;
        
		try {
	        ebAuditableEventType = bu.rimFac.createAuditableEventType();

	        XMLGregorianCalendar timeNow = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar());

			ebAuditableEventType.setTimestamp(timeNow);
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return ebAuditableEventType;
    }
    
}

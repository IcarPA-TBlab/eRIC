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
package it.cnr.icar.eric.client.ui.swing;


import it.cnr.icar.eric.client.xml.registry.infomodel.AuditableEventImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.ConceptImpl;

import java.util.ArrayList;
import java.util.Collection;

import java.sql.Timestamp;


import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.User;


/**
 * @author <a href="mailto:nikola.stojanovic@acm.org">Nikola Stojanovic</a>
 */
public class AuditableEventsTableModel extends AbstractTableModel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -9004443466546949985L;

	protected JavaUIResourceBundle resourceBundle = JavaUIResourceBundle.getInstance();
    
    String[] columnNames = { resourceBundle.getString("columnName.eventType"),
            resourceBundle.getString("columnName.timestamp"),
            resourceBundle.getString("columnName.user") };
            @SuppressWarnings("rawtypes")
			ArrayList<?> auditableEvents = new ArrayList();
            RegistryBrowser registryBrowser;
            
            public AuditableEventsTableModel() {
                registryBrowser = RegistryBrowser.getInstance();
            }
            
            public int getColumnCount() {
                return columnNames.length;
            }
            
            public int getRowCount() {
                return auditableEvents.size();
            }
            
            public AuditableEvent getObjectAt(int row) {
                AuditableEvent ae = (AuditableEvent) auditableEvents.get(row);
                
                return ae;
            }
            
            public Object getValueAt(int row, int col) {
                AuditableEventImpl auditableEvent = (AuditableEventImpl) auditableEvents.get(row);
                Object value = null;
                @SuppressWarnings("unused")
				InternationalString iString = null;
                
                try {
                    switch (col) {
                        case 0:
                            value = RegistryBrowser.getEventTypeAsString(auditableEvent.getEventType());
                            if (value == null) {
                                JAXRClient client = RegistryBrowser.getInstance().getClient();
                                BusinessQueryManager bqm = client.getBusinessQueryManager();                              
                                //MUST be the id of a Concept in StatusType scheme
                                String statusTypeConceptId = auditableEvent.getEventType1();
                                ConceptImpl statusTypeConcept = (ConceptImpl)bqm.getRegistryObject(statusTypeConceptId, 
                                        LifeCycleManager.CONCEPT);
                                value = statusTypeConcept.getDisplayName();
                            }
                            
                            break;
                            
                        case 1:
                            Timestamp timestamp = auditableEvent.getTimestamp();
                            if (timestamp!= null) {
                                value = timestamp.toString();
                            } else {
                                value = resourceBundle.getString("text.unknownTime");
                            }
                            break;
                            
                        case 2:
                            
                            User user = null;
                            
                            try {
                                user = auditableEvent.getUser();
                            } catch (JAXRException e) {
                                //User may have been deleted. Handle gracefully
                            	user = null;
                            }
                            
                            if (user != null) {
                                value = RegistryBrowser.getUserName(auditableEvent.getUser(),
                                        1);
                            } else {
                                it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectRef userRef =
                                        (auditableEvent).getUserRef();
                                value = userRef.getId();
                            }
                            
                            break;
                    }
                } catch (JAXRException e) {
                    RegistryBrowser.displayError(e);
                }
                
                return value;
            }
            
            public String getColumnName(int col) {
                return columnNames[col].toString();
            }
            
            @SuppressWarnings("rawtypes")
			void update(Collection auditableEvents) {
                if (auditableEvents.isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            resourceBundle.getString("message.noAuditTrail"),
                            resourceBundle.getString("title.registryBrowser.java"),
                            JOptionPane.INFORMATION_MESSAGE);
                    auditableEvents = new ArrayList();
                }
                
                setAuditableEvents(auditableEvents);
            }
            
            @SuppressWarnings("rawtypes")
			ArrayList getAuditableEvents() {
                return auditableEvents;
            }
            
            @SuppressWarnings({ "rawtypes", "unchecked" })
			void setAuditableEvents(Collection objects) {
                auditableEvents.clear();
                auditableEvents.addAll(objects);
                fireTableDataChanged();
            }
}

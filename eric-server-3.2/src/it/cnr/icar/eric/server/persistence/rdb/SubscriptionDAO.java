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
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;

/**
 * 
 * @author Farrukh S. Najmi
 * @author Nikola Stojanovic
 */
class SubscriptionDAO extends RegistryObjectDAO {
	private static final Log log = LogFactory.getLog(SubscriptionDAO.class);

	/**
	 * Use this constructor only.
	 */
	SubscriptionDAO(ServerRequestContext context) {
		super(context);
	}

	public static String getTableNameStatic() {
		return "Subscription";
	}

	public String getTableName() {
		return getTableNameStatic();
	}

	protected void deleteComposedObjects(Object object) throws RegistryException {
		SubscriptionType subscription = (SubscriptionType) object;

		super.deleteComposedObjects(subscription);

		NotifyActionDAO notifyActionDAO = new NotifyActionDAO(context);
		notifyActionDAO.setParent(subscription);
		notifyActionDAO.deleteByParent();
	}


	protected void insertComposedObjects(Object object) throws RegistryException {
		SubscriptionType subscription = (SubscriptionType) object;

		super.insertComposedObjects(subscription);

		NotifyActionDAO notifyActionDAO = new NotifyActionDAO(context);
		notifyActionDAO.setParent(subscription);

		// xxx pa 120203 handle getAction() return of JAXBElement list
		notifyActionDAO.insert(bu.getActionTypeListFromElements(subscription.getAction()));
	}

	/**
	 * Returns the SQL fragment string needed by insert or update statements
	 * within insert or update method of sub-classes. This is done to avoid code
	 * duplication.
	 */
	protected String getSQLStatementFragment(Object ro) throws RegistryException {

		SubscriptionType subscription = (SubscriptionType) ro;

		String stmtFragment = null;
		String selector = subscription.getSelector();

		XMLGregorianCalendar endTime = subscription.getEndTime();
		String endTimeAsString = null;

		if (endTime != null) {
			endTimeAsString = " '" + (new Timestamp(endTime.toGregorianCalendar().getTimeInMillis())) + "'";
		}

		Duration notificationInterval = subscription.getNotificationInterval();
		String notificationIntervalString = "";

		if (notificationInterval != null) {
			notificationIntervalString = "'" + notificationInterval.toString() + "'";
		}
		// xxx 120203 pa fix for missing XJC/JAXB generated default value for
		// duration
		else {
			notificationIntervalString = "'P1D'";
		}

		XMLGregorianCalendar startTime = subscription.getStartTime();
		String startTimeAsString = null;

		if (startTime != null) {
			startTimeAsString = " '" + (new Timestamp(startTime.toGregorianCalendar().getTimeInMillis())) + "'";
		}

		if (action == DAO_ACTION_INSERT) {
			stmtFragment = "INSERT INTO Subscription " + super.getSQLStatementFragment(ro) + ", '" + selector + "', "
					+ endTimeAsString + ", " + notificationIntervalString + ", " + startTimeAsString + " ) ";
		} else if (action == DAO_ACTION_UPDATE) {
			stmtFragment = "UPDATE Subscription SET " + super.getSQLStatementFragment(ro) + ", selector='" + selector
					+ "', endTime=" + endTimeAsString + ", notificationInterval=" + notificationIntervalString
					+ ", startTime=" + startTimeAsString + " WHERE id = '" + ((RegistryObjectType) ro).getId() + "' ";
		} else if (action == DAO_ACTION_DELETE) {
			stmtFragment = super.getSQLStatementFragment(ro);
		}

		return stmtFragment;
	}

	@SuppressWarnings("unchecked")
	protected void loadObject(Object obj, ResultSet rs) throws RegistryException {
		try {
			if (!(obj instanceof SubscriptionType)) {
				throw new RegistryException(ServerResourceBundle.getInstance().getString(
						"message.SubscriptionTypeExpected", new Object[] { obj }));
			}

			SubscriptionType ebSubscriptionType = (SubscriptionType) obj;
			super.loadObject(obj, rs);

			String selector = rs.getString("selector");
			ebSubscriptionType.setSelector(selector);

			// Need to work around a bug in PostgreSQL and loading of
			// ClassificationScheme data from NIST tests
			try {
				Timestamp endTimestamp = rs.getTimestamp("endTime");

				if (endTimestamp != null) {
					// Calendar calendar = Calendar.getInstance();
					// calendar.setTimeInMillis(endTime.getTime());

					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTimeInMillis(endTimestamp.getTime());
					XMLGregorianCalendar endTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);

					ebSubscriptionType.setEndTime(endTime);
				}
			} catch (StringIndexOutOfBoundsException e) {
				String id = rs.getString("id");
				log.error(ServerResourceBundle.getInstance()
						.getString("message.SubscriptionDAOId", new Object[] { id }), e);
			}

			String notificationIntervalString = rs.getString("notificationInterval");

			if (notificationIntervalString != null) {
				Duration notificationInterval = DatatypeFactory.newInstance().newDuration(notificationIntervalString);
				ebSubscriptionType.setNotificationInterval(notificationInterval);
			}

			// Need to work around a bug in PostgreSQL and loading of
			// ClassificationScheme data from NIST tests
			try {
				Timestamp startTimestamp = rs.getTimestamp("startTime");

				if (startTimestamp != null) {
					// Calendar calendar = Calendar.getInstance();
					// calendar.setTimeInMillis(startTime.getTime());

					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTimeInMillis(startTimestamp.getTime());

					XMLGregorianCalendar startTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(
							new GregorianCalendar());

					ebSubscriptionType.setStartTime(startTime);
				}
			} catch (StringIndexOutOfBoundsException e) {
				String id = rs.getString("id");
				log.error(ServerResourceBundle.getInstance()
						.getString("message.SubscriptionDAOId", new Object[] { id }), e);
			}

			NotifyActionDAO notifyActionDAO = new NotifyActionDAO(context);
			notifyActionDAO.setParent(ebSubscriptionType);
			@SuppressWarnings("rawtypes")
			List notifyActions = notifyActionDAO.getByParent();
			if (notifyActions != null) {
				bu.getActionTypeListFromElements(ebSubscriptionType.getAction()).addAll(notifyActions);
			}

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
		SubscriptionType ebSubscriptionType = bu.rimFac.createSubscriptionType();

		return ebSubscriptionType;
	}
}

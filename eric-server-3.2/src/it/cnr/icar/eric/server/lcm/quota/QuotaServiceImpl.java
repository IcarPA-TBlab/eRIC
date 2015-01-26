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
package it.cnr.icar.eric.server.lcm.quota;

import it.cnr.icar.eric.common.exceptions.QuotaExceededException;
import it.cnr.icar.eric.server.common.RegistryProperties;

import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class QuotaServiceImpl {
    private static QuotaServiceImpl instance = null;
    private static long quotaLimit; // in MB
    @SuppressWarnings("unused")
	private static long quotaLimitInBytes;
    @SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(QuotaServiceImpl.class);

    public synchronized static QuotaServiceImpl getInstance() {
        if (instance == null) {
            instance = new QuotaServiceImpl();
            quotaLimit = Integer.parseInt(RegistryProperties.getInstance()
                                                            .getProperty("eric.repository.quota"));
            quotaLimitInBytes = quotaLimit * 1024 * 1024;
        }

        return instance;
    }

    /**
    *     @throws QuotaExceededException if the size of already submitted items exceeds
    *     the quota specified in ebxmlrr.properties. It simply counts the number of bytes
    *     of previously submitted items and throws the exception on *next* request. So
    *     a single request can submit an item whose size is bigger than the quota limit.
    *     @throws RegistryException if there is a IOException when getting the items sizes.
    */
    public void checkQuota(String userId)
        throws QuotaExceededException, RegistryException {
        //TODO: Fix after AuditableEvents have been fixed for V3
        /*
        try {            
            // Get the id of items already submitted by this user
            String sql = "SELECT eo.* FROM ExtrinsicObject eo WHERE id IN " +
                "(SELECT ae.registryObject FROM AuditableEvent ae WHERE ae.eventType='Created' AND " +
                "ae.user_='" + userId + "')";

            //System.err.println(sql);
            ResponseOption responseOption = BindingUtility.getInstance().queryFac.createResponseOption();
            responseOption.setReturnType(ReturnType.OBJECT_REF);

            List results = PersistenceManagerFactory.getInstance()
                                                    .getPersistenceManager()
                                                    .executeSQLQuery(sql,
                    responseOption, "ExtrinsicObject", new java.util.ArrayList());

            // Get the total size of the repository items already submitted
            Iterator resultsIter = results.iterator();
            List itemsIds = new java.util.ArrayList();

            while (resultsIter.hasNext()) {
                ObjectRef objectRef = (ObjectRef) resultsIter.next();
                itemsIds.add(objectRef.getId());

                //System.err.println(objectRef.getId());    
            }

            RepositoryManager repManager = RepositoryManagerFactory.getInstance()
                                                                   .getRepositoryManager();
            long totalSizeOfSubmittedItems = repManager.getItemsSize(itemsIds);

            //System.err.println("Size: " + totalSizeOfSubmittedItems);
            if (totalSizeOfSubmittedItems > quotaLimitInBytes) {
                throw new QuotaExceededException(userId, quotaLimit);
            }
        } catch (javax.xml.bind.JAXBException e) {
            log.error(e);
        }
         */
    }
}

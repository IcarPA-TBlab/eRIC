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
package it.cnr.icar.eric.server.repository.hibernate;

import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.RepositoryItemImpl;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.RepositoryItemNotFoundException;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.repository.AbstractRepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryItemKey;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;


/**
 *
 * @author  Diego Ballve / Digital Artefacts
 */
public class HibernateRepositoryManager extends AbstractRepositoryManager {    
    /** Log */
    private Log log = LogFactory.getLog(HibernateRepositoryManager.class.getName());
    
    /** Hibernate util, initialized by constructor. */
    private RepositoryHibernateUtil hu;

    /** Flag for BLOB supported, required for using Oracle. Postgres requires byte
     *  arrays (binary type). HSQLDB works with both. */
    private boolean isBlobSupported;

    /** Flag to signal need for Oracle hack. */
    private boolean needsOracleHack;
    
    private boolean reclaimUsedDBConnections = false;
    
    /** Singleton instance */
    protected static RepositoryManager instance;        
    
    // ---------------------------------------------------------------------- //
    // Constructor
    // ---------------------------------------------------------------------- //
    
    /** Creates a new instance of HibernateRepositoryManager */
    protected HibernateRepositoryManager() {
        hu = RepositoryHibernateUtil.getInstance();
        // initialize isBlobSupported property, default to true.
        isBlobSupported = "BLOB".equalsIgnoreCase(RegistryProperties.getInstance()
            .getProperty("eric.persistence.rdb.largeBinaryType", "BLOB"));
        // checks if Oracle hack will be needed.. does not consider version!
        needsOracleHack = "oracle.jdbc.driver.OracleDriver".equalsIgnoreCase(
            RegistryProperties.getInstance().getProperty(
            "eric.persistence.rdb.databaseDriver", ""));
        reclaimUsedDBConnections = "true".equalsIgnoreCase(RegistryProperties.getInstance()
            .getProperty("eric.repository.hibernate.reclaimUsedDBConnections", "false"));
    }
    
    // ---------------------------------------------------------------------- //
    // Singleton pattern
    // ---------------------------------------------------------------------- //
    
    /**
     * Singleton instance accessor.
     */
    public synchronized static RepositoryManager getInstance() {
        if (instance == null) {
            instance = new HibernateRepositoryManager();
        }
        
        return instance;
    }
    
    // ---------------------------------------------------------------------- //
    // RepositoryManager interface implementation
    // ---------------------------------------------------------------------- //
    
    /**
     * Insert the repository item.
     * @param item The repository item.
     */
    public void insert(ServerRequestContext context, RepositoryItem item) throws RegistryException {
        Transaction tx = null;
        
        ExtrinsicObjectType eo = (ExtrinsicObjectType)context.getRegistryObject(item.getId(), "ExtrinsicObject");
        String lid = eo.getLid();
        String versionName = null;
                    
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
                                    
            VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
            if (contentVersionInfo == null) {
                contentVersionInfo = bu.rimFac.createVersionInfoType();
                eo.setContentVersionInfo(contentVersionInfo);
            }
            versionName = eo.getContentVersionInfo().getVersionName();
        
            // if item already exists, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
            
            List<?> results = s.find(findByID, params, types);
            if (!results.isEmpty()) {
                String errmsg = ServerResourceBundle.getInstance().getString("message.RepositoryItemWithIdAndVersionAlreadyExist",
				                                                    new Object[]{lid, versionName});
                log.error(errmsg);
                throw new RegistryException(errmsg);
            }
            
            // Writing out the RepositoryItem itself
            byte contentBytes[] = readBytes(item.getDataHandler().getInputStream());
            
            RepositoryItemBean bean = new RepositoryItemBean();
            RepositoryItemKey key = new RepositoryItemKey(lid, versionName);
            bean.setKey(key);

            if (needsOracleHack) {
                doOracleHackForInsert(s, bean, contentBytes);
                // do not call save after this
            } else {
                if (isBlobSupported) {
                    bean.setBlobContent(Hibernate.createBlob(contentBytes));
                } else {
                    bean.setBinaryContent(contentBytes);
                }
            }
            
            if (log.isDebugEnabled()) {
                String message = "Inserting repository item:"
                + "lid='" + key.getLid() + "', "
                + "versionName='" + key.getVersionName() + "', ";
                if (isBlobSupported) {
                    message += "content size=" + bean.getBlobContent().length();
                } else {
                    message += "content size=" + bean.getBinaryContent().length;
                }
                log.debug(message);
            }

            if (!needsOracleHack) {
                s.save(bean);
            }
            
            tx.commit();
            s.refresh(bean);
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToInsertRepositoryItem",
			                                                new Object[]{item.getId()});
            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg}));
        } finally {
            tryClose();
        }
    }
    
    /**
    * Returns the RepositoryItem associated with the ExtrinsicObject specified by id.
    *
    * @param id Unique id for ExtrinsicObject whose repository item is desired.
    * @return RepositoryItem instance
    * @exception RegistryException
    */
    @SuppressWarnings("unused")
	public RepositoryItem getRepositoryItem(String id)
        throws RegistryException {
        RepositoryItem repositoryItem = null;

	/*
	** Following code must duplicate that in getRepositoryItemKey()
	** because we need the eo variable later.  Keep the two in sync
	** manually.
	*/
        ServerRequestContext context = null;
        try {
        	
        	
            context = new ServerRequestContext("HibernateRepositoryManager:getRepositoryItem", null);
            
            //Access control is check in qm.getRepositoryItem using actual request context
            //This internal request context has total access.
            context.setUser(AuthenticationServiceImpl.getInstance().registryOperator);
            RegistryObjectType ro = qm.getRegistryObject(context, id,  "ExtrinsicObject");

            if (!(ro instanceof ExtrinsicObjectType)) {
                throw new ObjectNotFoundException(id);
            }

            ExtrinsicObjectType ebExtrinsicObjectType = (ExtrinsicObjectType)ro;
            if (ebExtrinsicObjectType == null) {
                throw new ObjectNotFoundException(id);
            }
            

            VersionInfoType ebVersionInfoType = ebExtrinsicObjectType.getContentVersionInfo();

            
            if (ebVersionInfoType == null) {
                // no Repository Item to find for this EO
                throw new RepositoryItemNotFoundException(id,
                                             ebExtrinsicObjectType.getVersionInfo().getVersionName());
            }
            
            RepositoryItemKey key = new RepositoryItemKey(ebExtrinsicObjectType.getLid(), ebVersionInfoType.getVersionName());

            Transaction tx = null;
            String lid = key.getLid();
            String versionName = key.getVersionName();

            try {
                SessionContext sc = hu.getSessionContext();
                Session s = sc.getSession();
                
                //Need to call clear otherwise we get a cached ReposiytoryItemBean where the txn has committed 
                //and Blob cannot be read any more. This will be better fixed when we pass ServerRequestContext
                //to each rm interface method and leverage leave txn management to ServerRequestContext.
                //See patch submitted for Sun Bug 6444810 on 6/28/2006
                s.clear();
                tx = s.beginTransaction();

                // if item does not exist, error
                String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
                Object[] params = {
                    lid, versionName
                };

                Type[] types = {
                    Hibernate.STRING, Hibernate.STRING
                };

                List<?> results = s.find(findByID, params, types);
                if (results.isEmpty()) {
                    String errmsg = ServerResourceBundle.getInstance().getString("message.RepositoryItemWithIdAndVersionDoesNotExist",
                                                                                       new Object[]{lid, versionName});
                    log.error(errmsg);
                    throw new RepositoryItemNotFoundException(lid, versionName);
                }

                RepositoryItemBean bean = (RepositoryItemBean)results.get(0);

                if (log.isDebugEnabled()) {
                    String message = "Getting repository item:"
                    + "lid='" + lid + "', "
                    + "versionName='" + versionName + "', ";
                    if (isBlobSupported) {
                        message += "content size=" + bean.getBlobContent().length();
                    } else {
                        message += "content size=" + bean.getBinaryContent().length;
                    }
                    log.debug(message);
                }

                String contentType = ebExtrinsicObjectType.getMimeType();

                DataHandler contentDataHandler;
                if (isBlobSupported) {
                    contentDataHandler = new DataHandler(new ByteArrayDataSource(
                        readBytes(bean.getBlobContent().getBinaryStream()), contentType));
                } else {
                    contentDataHandler = new DataHandler(new ByteArrayDataSource(
                        bean.getBinaryContent(), contentType));
                }

                repositoryItem = new RepositoryItemImpl(id, contentDataHandler);

                tx.commit();
            } catch (RegistryException e) {
                tryRollback(tx);
                throw e;
            } catch (Exception e) {
                String msg = ServerResourceBundle.getInstance().getString("message.FailedToGetRepositoryItem",
                                                                             new Object[]{lid, versionName});
                log.error(e, e);
                tryRollback(tx);
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
            } finally {
                tryClose();
            }
        } finally {
            context.rollback();
        }
        
        return repositoryItem;
    }
    
    /**
     * Delete the repository item.
     * @param key Unique key for repository item
     * @throws RegistryException if the item does not exist
     */
    public void delete(RepositoryItemKey key) throws RegistryException {
        Transaction tx = null;
        String lid = key.getLid();
        String versionName = key.getVersionName();
        
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
            
            if (log.isDebugEnabled()) {
                String message = "Deleting repository item: lid='" + lid + "' versionName='" + versionName + "'";
                log.debug(message);
            }
            
                        
            // if item does not exist, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
                        
            int deleted = s.delete(findByID, params, types);
            if (deleted == 0) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.RepositoryItemDoesNotExist", new Object[]{lid,versionName}));
            }
            tx.commit();
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToDeleteRepositoryItem",
			                                                 new Object[]{lid, versionName});
            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
    }
    
    /**
     * Since ids could contain duplicates returns a Set.
     */
    private Set<RepositoryItemKey> getKeysFromIds(List<String> ids) throws RegistryException {
        HashSet<RepositoryItemKey> keys = new HashSet<RepositoryItemKey>();
        
        Iterator<?> iter = ids.iterator();
        while (iter.hasNext()) {
            String id = (String)iter.next();
            RepositoryItemKey key = getRepositoryItemKey(id);
            keys.add(key);
        }
        
        return keys;
    }
        
    
    /**
     * Delete multiple repository items.
     * @param ids List of ids of ExtrinsicObjects whose repositoryItems are desired to be deleted.
     * @throws RegistryException if any of the item do not exist
     */
    public void delete(@SuppressWarnings("rawtypes") List ids) throws RegistryException {
        Transaction tx = null;
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
            
            @SuppressWarnings("unchecked")
			Set<RepositoryItemKey> keys = getKeysFromIds(ids);
            @SuppressWarnings("unused")
			Iterator<RepositoryItemKey> iter = keys.iterator();
            
            if (log.isDebugEnabled()) {
                StringBuffer message = new StringBuffer("Deleting repository items: ");
                for (Iterator<RepositoryItemKey> it = keys.iterator(); it.hasNext(); ) {
                    message.append((it.next()).toString());
                    if (it.hasNext()) {
                        message.append(", \n");
                    } else {
                        message.append(".\n");
                    }
                }
                log.debug(message);
            }
            
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            
            for (Iterator<RepositoryItemKey> it = keys.iterator(); it.hasNext(); ) {
                RepositoryItemKey key = it.next();
                
                String lid = key.getLid();
                String versionName = key.getVersionName();
                
                Object[] params = {
                    lid, versionName
                };

                Type[] types = {
                    Hibernate.STRING, Hibernate.STRING
                };

                int deleted = s.delete(findByID, params, types);
                
                if (deleted == 0) {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString("message.RepositoryItemDoesNotExist", new Object[]{lid,versionName}));
                }
            }
            
            tx.commit();
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToDeleteRepositoryItems");
            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
    }
    
    /**
     * Updates a RepositoryItem.
     */
    public void update(ServerRequestContext context, RepositoryItem item) throws RegistryException {
        Transaction tx = null;
        
        ExtrinsicObjectType eo = (ExtrinsicObjectType)context.getRegistryObject(item.getId(), "ExtrinsicObject");
        String lid = eo.getLid();
        String versionName = null;
            
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            tx = s.beginTransaction();
                        
            VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
            if (contentVersionInfo == null) {
                contentVersionInfo = bu.rimFac.createVersionInfoType();
                eo.setContentVersionInfo(contentVersionInfo);
            }
            versionName = eo.getContentVersionInfo().getVersionName();
            
            // if item does not exists, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
                        
            List<?> results = s.find(findByID, params, types);
            if (results.isEmpty()) {
                throw new RepositoryItemNotFoundException(lid, versionName);
            }
            
            // Writing out the RepositoryItem itself
            byte contentBytes[] = readBytes(item.getDataHandler().getInputStream());
            
            RepositoryItemBean bean = (RepositoryItemBean)results.get(0);
            RepositoryItemKey key = new RepositoryItemKey(lid, versionName);
            bean.setKey(key);

            if (needsOracleHack) {
                doOracleHackForUpdate(s, bean, contentBytes);
                // do not call save after this
            } else {
                if (isBlobSupported) {
                    bean.setBlobContent(Hibernate.createBlob(contentBytes));
                } else {
                    bean.setBinaryContent(contentBytes);
                }
            }
            
            if (log.isDebugEnabled()) {
                String message = "Updating repository item:"
                + "lid='" + lid + "', "
                + "versionName='" + versionName + "', ";
                if (isBlobSupported) {
                    message += "content size=" + bean.getBlobContent().length();
                } else {
                    message += "content size=" + bean.getBinaryContent().length;
                }
                log.debug(message);
            }
            
            if (!needsOracleHack) {
                s.update(bean);
            }
            
            tx.commit();
            s.refresh(bean);
            
        } catch (RegistryException e) {
            tryRollback(tx);
            throw e;
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToUpdateRepositoryItem",
			                                                new Object[]{lid, versionName});

            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
    }
    
    /**
    * Determines if RepositoryItem exists for specified key. 
    *
    * @return true if a RepositoryItem exists for specified key, false otherwise 
    * @param key The RepositoryItemKey.
    **/
    public boolean itemExists(RepositoryItemKey key) throws RegistryException {
        boolean found = false;
        Transaction tx = null;
        
        String lid = key.getLid();
        String versionName = key.getVersionName();
            
        try {
            SessionContext sc = hu.getSessionContext();
            Session s = sc.getSession();
            
            //Need to call clear otherwise we get a cached ReposiytoryItemBean where the txn has committed 
            //and Blob cannot be read any more. This will be better fixed when we pass ServerRequestContext
            //to each rm interface method and leverage leave txn management to ServerRequestContext.
            //See patch submitted for Sun Bug 6444810 on 6/28/2006
            s.clear();
            tx = s.beginTransaction();
            // if item does not exists, error
            String findByID = "from RepositoryItemBean as rib where rib.key.lid = ? AND rib.key.versionName = ? ";
            Object[] params = {
                lid, versionName
            };
            
            Type[] types = {
                Hibernate.STRING, Hibernate.STRING
            };
                        
            List<?> results = s.find(findByID, params, types);
            if (!results.isEmpty()) {
                found = true;
            }
            
            tx.commit();                 
        } catch (Exception e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedToSearchRepositoryItem",
			                                                 new Object[] {lid, versionName});

            log.error(e, e);
            tryRollback(tx);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        } finally {
            tryClose();
        }
        
        return found;
    }
            
    // ---------------------------------------------------------------------- //
    // private/util methods
    // ---------------------------------------------------------------------- //
    
    /**
     * Reads bytes from InputStream untill the end of the stream.
     *
     * @param in The InputStream to be read.
     * @return the read bytes
     * @thows Exception (IOEXception...)
     */
    private byte[] readBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        @SuppressWarnings("unused")
		InputStreamReader inr = new InputStreamReader(in);
        byte bbuf[] = new byte[1024];
        int read;
        while ((read = in.read(bbuf)) > 0) {
            baos.write(bbuf, 0, read);
        }
        return baos.toByteArray();
    }
    
    protected void tryClose() throws RegistryException {
        try {
            SessionContext sc = hu.getSessionContext();
            if (reclaimUsedDBConnections) {
                try {
                    sc.getSession().disconnect();
                } catch (Throwable t) {
                    String msg = ServerResourceBundle.getInstance().getString("message.FailedCloseDatabaseSession");
                    log.error(msg);
                    log.error(t, t);
                }
            }
            sc.close();
        } catch (HibernateException e) {
            String msg = ServerResourceBundle.getInstance().getString("message.FailedCloseDatabaseSession");
            log.error(e, e);
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
        }
    }
    
    protected void tryRollback(Transaction tx) throws RegistryException {
        if (tx != null) {
            try {
                tx.rollback();
            } catch (Exception e) {
                String msg = ServerResourceBundle.getInstance().getString("message.failedToRollbackTransaction");
                log.error(e, e);
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.seeLogsForDetails", new Object[]{msg})); 
            }
        }
    }            

    private void doOracleHackForInsert(Session s, RepositoryItemBean bean, byte[] contentBytes)
    throws Exception {
        // the code below is used to for oracle only, since it requires 
        // special handling for BLOBs. First save dummy small cotent to BLOBs
        bean.setBlobContent(Hibernate.createBlob("dummy-content".getBytes("utf-8")));
        s.save(bean);
        s.flush();
        doOracleHackForUpdate(s, bean, contentBytes);
    }

    private void doOracleHackForUpdate(Session s, RepositoryItemBean bean, byte[] contentBytes)
    throws Exception {
        // grabs an Oracle LOB
        s.refresh(bean, net.sf.hibernate.LockMode.UPGRADE); 
        Object contentBlob = bean.getBlobContent();
        Class<?> clazz = Class.forName("oracle.sql.BLOB");
        java.lang.reflect.Method method = clazz.getMethod("getBinaryOutputStream", new Class[] {});
        java.io.OutputStream contentOS = (java.io.OutputStream)
            method.invoke(contentBlob, new Object[] {});
            
        // Write directly to the OutputStreams
        contentOS.write(contentBytes);
        contentOS.flush();
        contentOS.close();
    }
    
}

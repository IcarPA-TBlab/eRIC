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
package it.cnr.icar.eric.server.lcm.replication;

import it.cnr.icar.eric.client.xml.registry.DeclarativeQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.infomodel.RegistryObjectImpl;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ConnectionManager;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.registry.Connection;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.RegistryService;

import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

/**
 * Manages replication of objects from remote registris to local registry
 * 
 * @author Farrukh S. Najmi
 * 
 */
public class ReplicationManager {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /*# private ReplicationManager _objectManagerImpl; */
    private static ReplicationManager instance = null;
    @SuppressWarnings("unused")
	private static BindingUtility bu = BindingUtility.getInstance();
    @SuppressWarnings("unused")
	private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    
    /**
     *
     * @associates <{it.cnr.icar.eric.server.persistence.PersistenceManagerImpl}>
     */
    it.cnr.icar.eric.server.persistence.PersistenceManager pm = it.cnr.icar.eric.server.persistence.PersistenceManagerFactory.getInstance()
                                                                                                                               .getPersistenceManager();
    /** Creates a new instance of ReplicationManager */
    protected ReplicationManager() {
    }
    
    public synchronized static ReplicationManager getInstance() {
        if (instance == null) {
            instance = new ReplicationManager();
        }

        return instance;
    }
    
    public RegistryObjectType createReplica(ServerRequestContext context, ObjectRefType oref) throws RegistryException {
        RegistryObjectType replica = null;
        
        //Only create replica if one does not exists already
        RegistryObjectType ro = pm.getRegistryObject(context, oref.getId(), "RegistryObject");
        
        if (ro == null) {
            String home = oref.getHome();
            if (home == null) {
                throw new RegistryException(new InvalidRequestException(ServerResourceBundle.getInstance().getString("message.cannotCreateReplica")));
            }
            
            ro = getRemoteRegistryObjectUsingJAXR(oref);
            
            if (ro != null) {
                //Make sure ro has home set. TODO: Add to spec
                if (ro.getHome() == null) {
                    ro.setHome(oref.getHome());
                }
                
                //Store the remote object replica and its ObjectRef 
                //locally using user associated with requestContext
                List<IdentifiableType> roList = new ArrayList<IdentifiableType>();
                roList.add(ro);
                roList.add(oref);
                pm.insert(context, roList);                            
            } else {
                //What to do if remote ObjectRef unresolved
                @SuppressWarnings("unused")
				int i=0;
            }
            
        }
        
        return replica;
    }
        
    public boolean isRemoteObjectRef(ObjectRefType oref) throws RegistryException {
        boolean isRemoteRef = false;
        String refHome = oref.getHome();
        if (refHome != null) {
            //TODO: Need extra check in case home is to local registry
            isRemoteRef = true;
        }
        
        return isRemoteRef;
    }
    

    private RegistryObjectType getRemoteRegistryObjectUsingJAXR(ObjectRefType remoteRef) throws RegistryException {
        RegistryObjectType ebRO = null;
        
        String home = remoteRef.getHome();       
        try {
            Connection connection = ConnectionManager.getInstance().getConnection(home);        
            RegistryService service = connection.getRegistryService();
            DeclarativeQueryManagerImpl dqm = (DeclarativeQueryManagerImpl)service.getDeclarativeQueryManager();
            RegistryObjectImpl ro = (RegistryObjectImpl)dqm.getRegistryObject(remoteRef.getId());
            if (ro != null) {
                ebRO = (RegistryObjectType)ro.toBindingObject();
            } else {
                String msg = ServerResourceBundle.getInstance().getString("message.error.RemoteObjectNotFound", new Object[]{home, remoteRef.getId()});
                throw new RegistryException(msg);
            }
        } catch (JAXRException e) {
            String msg = ServerResourceBundle.getInstance().getString("message.error.ErrorGettingRemoteObject", new Object[]{home, remoteRef.getId()});
            throw new RegistryException(msg, e);
        }
        return ebRO;
    }
    
    @SuppressWarnings("unused")
	private RegistryObjectType getRemoteRegistryObjectUsingREST(ObjectRefType remoteObj) throws RegistryException {
        RegistryObjectType ebRegistryObjectType = null;
        String remoteURLString = remoteObj.getHome() +
        "/http?interface=QueryManager&method=getRegistryObject&param-id=" + remoteObj.getId();
                
        InputStream is = null;
        try {
            URL url = new URL(remoteURLString);
            
            HttpURLConnection conn =
            (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if ((responseCode < 200) || ((responseCode > 300) && (responseCode < 500))) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.HTTPError",
                        new Object[]{new Integer(responseCode)}));
            }
            
            conn.connect();
            is = conn.getInputStream();
//            ro = (RegistryObjectType)BindingUtility.getInstance().rimFac.createUnmarshaller().unmarshal(is);
			@SuppressWarnings("unchecked")
			JAXBElement<RegistryObjectType> ebRegistryObject = (JAXBElement<RegistryObjectType>) BindingUtility
					.getInstance().getJAXBContext().createUnmarshaller().unmarshal(is);

			// take ComplexType from Element
            ebRegistryObjectType = ebRegistryObject.getValue();
        }
        catch (MalformedURLException e) {
            throw new RegistryException(e);
        }
        catch (UnknownHostException e) {
            throw new RegistryException(e);
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (IOException e) {
            throw new RegistryException(e);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return ebRegistryObjectType;
    }
    
    @SuppressWarnings("unused")
	private RepositoryItem getRemoteRepositoryItem(ObjectRefType remoteObj) {
        RepositoryItem ri = null;
        
        return ri;
    }
    
    
    
}

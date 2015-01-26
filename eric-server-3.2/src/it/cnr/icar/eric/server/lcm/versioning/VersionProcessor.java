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
package it.cnr.icar.eric.server.lcm.versioning;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.common.Utility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.exceptions.RepositoryItemNotFoundException;
import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;

/**
 *
 * @author Farrukh S. Najmi
 *
 * This class handles version management for RegistryObjects and RepositoryItems.
 */
public class VersionProcessor {
    
    public static BindingUtility bu = BindingUtility.getInstance();
    private static Log log = LogFactory.getLog(VersionProcessor.class);
    private QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    private AuthenticationServiceImpl ac = AuthenticationServiceImpl.getInstance();
    private RepositoryManager rm = RepositoryManagerFactory.getInstance()
    .getRepositoryManager();
    private HashSet<Class<?>> versionableClassNameSet = new HashSet<Class<?>>();
    private List<IdentifiableType> versions;
    
    /**
     *
     * @associates <{it.cnr.icar.eric.server.persistence.PersistenceManagerImpl}>
     */
    PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();;
    
    ServerRequestContext context = null;
    
    /** Creates a new instance of VersionManagerImpl */
    public VersionProcessor(ServerRequestContext context) {
        this.context = context;
        loadVersionableClasses();
    }
    
    @SuppressWarnings("static-access")
	public boolean needToVersionRegistryObject(RegistryObjectType ro) throws RegistryException {
        boolean needToVersion = true;
        
        BindingUtility bu = BindingUtility.getInstance();
        boolean newObject = false;
        
        try {
            needToVersion = isVersionableClass(ro);
            
            if (needToVersion) {
                HashMap<?, ?> slotsMap;
                // Honour dontVersion flag if specified on request
                if (! context.getRegistryRequestStack().empty()) {
                    slotsMap = bu.getSlotsFromRequest(context.
                            getCurrentRegistryRequest());
                    if (slotsMap.containsKey(bu.CANONICAL_SLOT_LCM_DONT_VERSION)) {
                        String val = (String)slotsMap.
                                get(bu.CANONICAL_SLOT_LCM_DONT_VERSION);
                        if (val.trim().equalsIgnoreCase("true")) {
                            needToVersion = false;
                        }
                    }
                }
                
                //Honour dontVersion flag if specified on ro
                slotsMap = bu.getSlotsFromRegistryObject(ro);
                if (slotsMap.containsKey(bu.CANONICAL_SLOT_LCM_DONT_VERSION)) {
                    String val = (String)slotsMap.get(bu.CANONICAL_SLOT_LCM_DONT_VERSION);
                    if (val.trim().equalsIgnoreCase("true")) {
                        needToVersion = false;
                    }
                }
            }
            
            //TODO:
            //Need to investigate case where not versioning and it is a new object.
            //Need unit test for this case.
            if( needToVersion ) {
                versions = getAllRegistryObjectVersions( ro );
                
                if( versions.size() == 0 ) {     // If there are any existing versions (ie. ro's with same LID) then we need to version
                    //This is a new ro and therefore need not be versioned
                    needToVersion = false;
                    newObject = true;
                }
            }
            
            //Must set versionName to match latest versionName if existing object
            //or set to version 1.1 if new object.
            if (!needToVersion) {
                RegistryObjectType lastVersion = getLatestVersionOfRegistryObject(ro);
                String versionName = null;
                if (lastVersion == null) {
                    versionName = "1.1";
                } else {
                    versionName = lastVersion.getVersionInfo().getVersionName();
                    
                    //Must bump up versionName for new objects
                    if (newObject) {
                        versionName = nextVersion(versionName);
                    }
                }
                
                VersionInfoType versionInfo = ro.getVersionInfo();
                if (versionInfo == null) {
                    versionInfo = bu.rimFac.createVersionInfoType();
                    ro.setVersionInfo(versionInfo);
                }
                versionInfo.setVersionName(versionName);
                if (! context.getRegistryRequestStack().empty()) {
                    setVersionInfoComment(versionInfo);
                }
            }
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        
        return needToVersion;
    }
    
    private void setVersionInfoComment(VersionInfoType versionInfo) {
        String requestComment = context.getCurrentRegistryRequest().getComment();
        if (versionInfo.getComment() == null && requestComment != null) {
            versionInfo.setComment(requestComment);
        }
    }
    
    public void checkRegistryObjectLid(RegistryObjectType ro) throws RegistryException {
        String id = ro.getId();
        String lid = ro.getLid();
        
        String existingObjectLid = context.getIdToLidMap().get(id);
        
        //Assign lid if not specified, validate lid if specified
        if (existingObjectLid != null) {
            if (lid == null) {
                ro.setLid(existingObjectLid);
            } else {
                //Validate that lid matches existing objects lid
                if (!lid.equals(existingObjectLid)) {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString("message.idDoesNotMatch",
                            new Object[]{lid, existingObjectLid, id}));
                }
            }
        } else {
            checkRegistryObjectLidOnNewObject(ro);
        }
    }
    
    public void checkRegistryObjectLidOnNewObject(RegistryObjectType ro) throws RegistryException {
        @SuppressWarnings("unused")
		String id = ro.getId();
        String lid = ro.getLid();
        
        //Object does not exists already
        if (lid == null) {
            ro.setLid(ro.getId());
        }
    }
    
    @SuppressWarnings("static-access")
	public boolean needToVersionRepositoryItem(ExtrinsicObjectType eo, RepositoryItem riNew) throws RegistryException {
        boolean needToVersion = true;
        
        try {
            //dontVersion eo imples dontVersion ro
            needToVersion = needToVersionRegistryObject(eo);
            
            if (needToVersion) {
                //This is an existing object not a newly submitted object
                //See if repository item has changed or not.
                HashMap<?, ?> slotsMap;
                
                //Honour dontVersion flag if specified on request
                if (! context.getRegistryRequestStack().empty()) {
                    slotsMap = bu.getSlotsFromRequest(context.
                            getCurrentRegistryRequest());
                    if (slotsMap.
                            containsKey(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT)) {
                        String val = (String)slotsMap.
                                get(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT);
                        if (val.trim().equalsIgnoreCase("true")) {
                            needToVersion = false;
                        }
                    }
                }
                
                //Honour dontVersion flag if specified on ro
                slotsMap = bu.getSlotsFromRegistryObject(eo);
                if (slotsMap.containsKey(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT)) {
                    String val = (String)slotsMap.get(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT);
                    if (val.trim().equalsIgnoreCase("true")) {
                        needToVersion = false;
                    }
                }
            }
            
            if (needToVersion) {
                if (riNew == null) {
                    needToVersion = false;
                    return needToVersion;
                } else {
                    RepositoryItem riOld = null;
                    
                    try {
                        riOld = rm.getRepositoryItem(eo.getId());
                    } catch (RepositoryItemNotFoundException e) {
                        //It is possible that there is no RepositoryItem yet.
                        //Ignore the exception.
                    	riOld = null;
                    } catch (ObjectNotFoundException e) {
                        //It is possible that there is no RepositoryItem yet.
                        //Ignore the exception.
                    	riOld = null;
                    }
                    
                    if (riOld == null) {
                        needToVersion = false;
                    } else {
                        if (repositoryItemsAreIdentical(riOld, riNew)) {
                            needToVersion = false;
                        }
                    }
                }
            }
            
            //Must set contentVersionName to match latest versionName if existing object
            //or set to version 1.1 if new object.
            if (!needToVersion) {
                ExtrinsicObjectType lastVersion = (ExtrinsicObjectType)getLatestVersionOfRegistryObject(eo);
                
                VersionInfoType contentVersionInfo = eo.getContentVersionInfo();
                if (contentVersionInfo == null) {
                    contentVersionInfo = bu.rimFac.createVersionInfoType();
                }
                
                if (lastVersion == null) {
                    //This is the first ExtrinsicObject version.
                    if (riNew != null) {
                        //This is the first RepositoryItem version. Make sure versionName is "1.1"
                        contentVersionInfo.setVersionName("1.1");
                    } else {
                        //No repository item means that the contentVersionInfo MUST be set to null
                        contentVersionInfo = null;
                    }
                } else {
                    //This is not the first ExtrinsicObject version.
                    //Note that contentversionName is set even if no RI is submitted since
                    //it is OK to update just the EO and have new version use last version of RO
                    
                    VersionInfoType lastContentVersionInfo = lastVersion.getContentVersionInfo();
                    
                    if (lastContentVersionInfo == null) {
                        //Previous version had no repository item
                        String lastContentVersionName = rm.getLatestVersionName(context, eo.getLid());
                        if (lastContentVersionName != null) {
                            contentVersionInfo.setVersionName(lastContentVersionName);
                        } else {
                            contentVersionInfo.setVersionName("1.1");
                        }
                    } else {
                        //Previous version had a repository item
                        //Use the last contentVersionName
                        contentVersionInfo.setVersionName(lastContentVersionInfo.getVersionName());
                    }
                }
                eo.setContentVersionInfo(contentVersionInfo);
            }
            
        } catch (JAXBException e) {
            throw new RegistryException(e);
        }
        
        return needToVersion;
    }
    
    /**
     * Creates a new version of the RepositoryItem associated with specified ExtrinsicObject.
     * Note that when the RepositoryItem is versioned its ExtrinsicObject must also be versioned.
     *
     */
    public RepositoryItem createRepositoryItemVersion(ExtrinsicObjectType eo) throws RegistryException {
        RepositoryItem riNew = null;
        
        try {
            ExtrinsicObjectType eoNew = (ExtrinsicObjectType)createRegistryObjectVersion(eo);
            
            String latestContentVersionName = rm.getLatestVersionName(context, eo.getLid());
            String nextContentVersionName = nextVersion(latestContentVersionName);
            
            VersionInfoType nextContentVersionInfo = bu.rimFac.createVersionInfoType();
            nextContentVersionInfo.setVersionName(nextContentVersionName);
            
            //Set the contentComment from the submitted object's contentVersionInfo
            VersionInfoType submittedContentVersionInfo = eo.getContentVersionInfo();
            if (submittedContentVersionInfo != null) {
                nextContentVersionInfo.setComment(submittedContentVersionInfo.getComment());
            }
            
            //Update the eo contentVersionName to match newContentVersionName
            eoNew.setContentVersionInfo(nextContentVersionInfo);
            
            RepositoryItem riOld = (RepositoryItem)context.getRepositoryItemsMap().get(eo.getId());
            riNew = (RepositoryItem)riOld.clone();
            
            //A new version must have a unique id that matches its new ExtrinsicObject eoNew
            riNew.setId(eoNew.getId());
            
            //Now remeber in context.newRIVersionMap fro later replacement
            //Should we be using old or new eo.getId().
            //Maybe we dont need newRIVersionMap and newROVersionMap
            //Lets see how to just use existing idMap and other structures.
            context.getNewRIVersionMap().put((RepositoryItem) context.getRepositoryItemsMap().get(eo.getId()), riNew);
        } catch (CloneNotSupportedException e) {
            //This cannot happen
            throw new RegistryException(e);
        }
        
        return riNew;
    }
    
	private List<IdentifiableType> getAllRegistryObjectVersions(RegistryObjectType ebRegistryObjectType) throws RegistryException {
		if (versions == null) {
			ServerRequestContext queryContext = null;

			// Note: ORDER BY versionName DESC is not safe because String(1.10)
			// < String(1.9)
			String query = "SELECT ro.* FROM " + Utility.getInstance().mapTableName(ebRegistryObjectType) + " ro WHERE ro.lid = '"
					+ ebRegistryObjectType.getLid() + "'";

			try {
				AdhocQueryRequest queryRequest = bu.createAdhocQueryRequest(query);
				queryContext = new ServerRequestContext("VersionProcessor.getAllRegistryObjectVersions", queryRequest);

				queryContext.setUser(ac.registryOperator);

				AdhocQueryResponse ebAdhocQueryResponse = qm.submitAdhocQuery(queryContext);
				
				@SuppressWarnings("unchecked")
				List<IdentifiableType> ebIdentifiableTypeList = (List<IdentifiableType>) BindingUtility.getInstance()
						.getIdentifiableTypeList(ebAdhocQueryResponse.getRegistryObjectList());

				
				// set resulting list as versions
				versions = ebIdentifiableTypeList;
				
				queryContext.commit();
				queryContext = null;
			} catch (JAXBException e) {
				throw (new RegistryException(e));
			} catch (JAXRException e) {
				throw (new RegistryException(e));
			} finally {
				if (queryContext != null) {
					queryContext.rollback();
				}
			}
		}

		return (versions);
	}
    
    private RegistryObjectType getLatestVersionOfRegistryObject(RegistryObjectType ro) throws RegistryException {
        RegistryObjectType latestRO = null;
        
        //Call in case versions have not been initialized yet.
        getAllRegistryObjectVersions(ro);
        
        if (versions.size() == 0) {
            return null;
        }
        
        String latestVersion = null;
        for (Iterator<IdentifiableType> it = versions.iterator(); it.hasNext(); ) {
            if (latestRO == null) {
                latestRO = (RegistryObjectType)it.next();
                latestVersion = latestRO.getVersionInfo().getVersionName();
                continue;
            }
            
            RegistryObjectType next = (RegistryObjectType)it.next();
            String nextVersion = next.getVersionInfo().getVersionName();
            
            if (compareVersions(nextVersion, latestVersion) > 0) {
                latestRO = next;
                latestVersion = latestRO.getVersionInfo().getVersionName();
            }
        }
        
        return latestRO;
    }
    
    
    //TODO: Consider replacing versions with idToVersionsMap as a performance optimization in future.
    private boolean isIdInVersions( String id ) {
        RegistryObjectType      ro        = null;
        boolean                 foundId   = false;
        
        if( versions != null ) {
            Iterator<IdentifiableType>            iter      = versions.iterator();
            
            while( iter.hasNext() ) {
                ro = (RegistryObjectType)iter.next();
                
                if( id.equals( ro.getId() ) ) {
                    foundId = true;
                    break;
                }
            }
            
        }
        
        return( foundId );
    }
    
    
    /**
     * Compares 2 version Strings, with major/minor versions separated by '.'
     * Example: "1.10"
     *
     * @param v1 String for version 1.
     * @param v2 String for version 2.
     * @return int = 0 if params are equal; int > 0 if 1st is grater than 2nd;
     *         int < 0 if 2st is grater than 1nd.
     */
    public static int compareVersions(String v1, String v2) {
        String parts1 [] = v1.split("\\.", 2);
        String parts2 [] = v2.split("\\.", 2);
        
        int iCompare = Integer.parseInt(parts1[0]) - Integer.parseInt(parts2[0]);
        if (iCompare == 0) {
            // equal.. try subversions
            if (parts1.length == 1 && parts2.length == 1) {
                // really equal
                return 0;
            } else if (parts1.length == 1) {
                // other is bigger (v2)
                return -1;
            } else if (parts2.length == 1) {
                // other is bigger (v1)
                return +1;
            } else {
                // try subversions
                return compareVersions(parts1[1], parts2[1]);
            }
        } else {
            return iCompare;
        }
    }
    
    @SuppressWarnings("static-access")
	public RegistryObjectType createRegistryObjectVersion(RegistryObjectType ebRegistryObjectType) throws RegistryException {
        RegistryObjectType ebRegistryObjectTypeNew = null;
        
        try {
            Utility util = Utility.getInstance();
            
            RegistryObjectType lastVersion = getLatestVersionOfRegistryObject(ebRegistryObjectType);
            String nextVersion = null;
            if (lastVersion == null) {
                nextVersion = "1.1";
            } else {
                nextVersion = nextVersion(lastVersion.getVersionInfo().getVersionName());
            }
            
            ebRegistryObjectTypeNew = bu.cloneRegistryObject(ebRegistryObjectType);
            VersionInfoType nextVersionInfo = bu.rimFac.createVersionInfoType();
            nextVersionInfo.setVersionName(nextVersion);
            
            //Set the comment from the request comment (per the spec)
            if (! context.getRegistryRequestStack().empty()) {
                nextVersionInfo.setComment(context.
                        getCurrentRegistryRequest().
                        getComment());
            }
            
            ebRegistryObjectTypeNew.setVersionInfo(nextVersionInfo);
            
            //A new version must have a unique id
            String id = ebRegistryObjectType.getId();
            String lid = ebRegistryObjectType.getLid();
            String idNew    = id;
            
            //Only change id if it already exists in versions
            //Need to preserve client supplied id in the case where lid is an
            //existing lid but id is new
            if( isIdInVersions( id ) ) {
                //Make id of next version be lid with ":nextVersion" as suffix, if this id is already in use in a version
                idNew = lid + ":" + nextVersion; //Utility.getInstance().createId();
                ebRegistryObjectTypeNew.setId(idNew);
                
                //Add entry to idMap so old id and refs to it are mapped to idNew
                context.getIdMap().put(id, idNew);
            }
            
            //Add entry to context.newROVersionMap for later replacement
            context.getNewROVersionMap().put(ebRegistryObjectType, ebRegistryObjectTypeNew);
            
            //Assign new ids to all composed RegistryObjects within roNew
            Set<RegistryObjectType> composedObjects = bu.getComposedRegistryObjects(ebRegistryObjectTypeNew, -1);
            
            Iterator<RegistryObjectType> iter = composedObjects.iterator();
            while (iter.hasNext()) {
                RegistryObjectType composedObject = iter.next();
                
                //check for composed object if exist change the id and lid and 
                //also update the idMap.
                if(objectExists(composedObject.getId())){
                    String oldId = composedObject.getId();
                    String newId = oldId + ":" + nextVersion;
                    composedObject.setId(newId);
                    composedObject.setLid(newId);
                    context.getIdMap().put(oldId, newId);
                }               
                
                String composedId       = composedObject.getId();
                String composedLid      = composedObject.getLid();
                String composedIdNew    = composedId;
                
                if( !util.isValidRegistryId( composedId ) ) {  // Replace the id if it's not a valid ID already
                    composedIdNew    = util.createId();
                    
                    composedObject.setId( composedIdNew );
                    
                    // Add entry to idMap so old composedId and refs to it are mapped to composedIdNew
                    context.getIdMap().put( composedId, composedIdNew );
                }
                
                if( composedLid == null || composedLid.trim().length() == 0 ) {
                    composedObject.setLid( composedIdNew );
                }                
                // Set the parent id of this composed object to point to the new parent
                bu.setParentIdForComposedObject( composedObject, idNew );
            }
        } catch (JAXRException e) {
            throw new RegistryException(e);
        }
        return ebRegistryObjectTypeNew;
    }

    /**
     * Checks if object exist in registry.
     *
     */
    private boolean objectExists(String id) throws RegistryException{
        boolean exists = false;
        try{
            RegistryObjectType ro = context.getRegistryObject(id, "RegistryObject", true);
            if (ro != null) {
                exists = true;
            }
        } catch(ObjectNotFoundException e){
        	exists = false;
            //do nothing
        }
        return exists;
    }
    
    
    private String nextVersion(String lastVersion) {
        if ((lastVersion == null) || (lastVersion.length() == 0)) {
            lastVersion = "1.1";
        }
        
        String parts[] = lastVersion.split("\\.", 3);
        @SuppressWarnings("unused")
		//int majorVersion = (new Integer(parts[0])).intValue();
        //int minorVersion = (new Integer(parts[1])).intValue();
		int majorVersion = Integer.parseInt(parts[0]);
        int minorVersion = Integer.parseInt(parts[1]);
        
        //TODO: check this. What if version has more than major/minor?
        if (parts.length > 2 && log.isWarnEnabled()) {
            log.warn(ServerResourceBundle.getInstance().getString("message.IgnoringVersionInfromationAfterMajorMinorVersion",
                    new Object[]{parts[2]}));
        }
        
        //Increment version
        int newMinorVersion = minorVersion + 1;
        
        //String nextVersion = parts[0] + "." + (new Integer(newMinorVersion)).toString();
        String nextVersion = parts[0] + "." + Integer.toString(newMinorVersion);
        return nextVersion;
    }
    
    /*
     * Loads the list of versionable RIM classes from the property file during startup.
     */
    private void loadVersionableClasses() {
        String versionableClassList = RegistryProperties.getInstance().getProperty("eric.server.lcm.VersionManager.versionableClassList");
        //System.err.println("loadVersionableClasses: versionableClassList='" + versionableClassList + "'");
        if (versionableClassList != null) {
            StringTokenizer tokenizer = new StringTokenizer(versionableClassList,
                    "|");
            
            while (tokenizer.hasMoreTokens()) {
                try {
                    String versionableClassName = tokenizer.nextToken();
                    versionableClassName = "org.oasis.ebxml.registry.bindings.rim." + versionableClassName + "Type";
                    //System.err.println("    loadVersionableClasses: versionableClassName='" + versionableClassName + "'");
                    Class<?> versionableClass = Class.forName(versionableClassName);
                    versionableClassNameSet.add(versionableClass);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            //System.err.println(
            //"Registry has not defined the classes that will be versioned yet. This can be done by setting the eric.server.lcm.VersionManager.versionableClassList property in eric.properties file.");
        }
    }
    
    private boolean isVersionableClass(RegistryObjectType ro) {
        boolean isVersionable = false;
        
        //System.err.println("isVersionable entered. ro=" + ro.getClass().getName());
        Iterator<Class<?>> iter = versionableClassNameSet.iterator();
        while (iter.hasNext()) {
            Class<?> clazz = iter.next();
            //System.err.println("    isVersionable clazz=" + clazz.getName() + " isAssignableFrom = " +  clazz.isAssignableFrom(ro.getClass()));
            if (clazz.isAssignableFrom(ro.getClass())) {
                isVersionable = true;
                break;
            }
        }
        return isVersionable;
    }
    
    private boolean repositoryItemsAreIdentical(RepositoryItem ri1, RepositoryItem ri2) {
        DataHandler dh1 = ri1.getDataHandler();
        DataHandler dh2 = ri2.getDataHandler();
        
        return dh1.equals(dh2);
    }
}

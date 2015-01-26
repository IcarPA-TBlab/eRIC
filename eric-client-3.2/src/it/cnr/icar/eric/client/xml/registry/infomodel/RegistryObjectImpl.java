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
package it.cnr.icar.eric.client.xml.registry.infomodel;

import it.cnr.icar.eric.client.xml.registry.BusinessQueryManagerImpl;
import it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl;
import it.cnr.icar.eric.client.xml.registry.RegistryServiceImpl;
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.client.xml.registry.util.SQLQueryProvider;
import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.CanonicalConstants;
import it.cnr.icar.eric.common.exceptions.UnresolvedReferenceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.AuditableEvent;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.ExtensibleObject;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.RegistryEntry;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.User;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.VersionInfoType;

/**
 * Implements JAXR API interface named RegistryObject.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public abstract class RegistryObjectImpl extends IdentifiableImpl
    implements RegistryObject {

    private static final Log log = LogFactory.getLog(RegistryObjectImpl.class);

    protected InternationalString description = null;
    protected InternationalString name = null;
    protected String lid = null;
    User newObjOwner = null;

    /** The ObjectRef to the ObjectType Concept */
    protected RegistryObjectRef objectTypeRef = null;

    protected RegistryObjectRef statusRef = null;

    // lookup map for related object types
    protected Map<?, ?> relatedObjectTypesLookup = new HashMap<Object, Object>();

    /** Composed objects */
    protected ArrayList<RegistryObject> classifications = new ArrayList<RegistryObject>();
    protected ArrayList<ExternalIdentifier> externalIds = new ArrayList<ExternalIdentifier>();

    protected VersionInfoType versionInfo = null;

    /** Even though in JAXR Association-s are non-composed objects, their
    *         save behavior should be similar to composed objects. */
    protected Collection<Association> associations = null;

    //Following are collection of non-composed objects that are cached by this
    //implementation for performance efficiency. They are initialized on first access.
    protected HashSet<ExternalLink> externalLinks = null;
    protected Collection<?> packages = null;

    private Organization org = null;

    RegistryObjectImpl(LifeCycleManagerImpl lcm) throws JAXRException {
        super(lcm);

        @SuppressWarnings("unused")
		String str = getClass().getName();

        String clazzName = this.getClass().getName();
        if (clazzName.startsWith("it.cnr.icar.eric.client.xml.registry.infomodel")) {
            objectTypeRef = lcm.getObjectTypeRefFromJAXRClassName(clazzName);
        }
        lid = getKey().getId();
        newObjOwner = null;
        ((RegistryServiceImpl)(lcm.getRegistryService())).getObjectCache().putRegistryObject(this);
    }

    RegistryObjectImpl(LifeCycleManagerImpl lcm, RegistryObjectType ebObject)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super(lcm, ebObject);

        lid = ebObject.getLid();

        if (ebObject.getName() != null) {
            name = new InternationalStringImpl(lcm, ebObject.getName());
        }

        if (ebObject.getDescription() != null) {
            description = new InternationalStringImpl(lcm,
                    ebObject.getDescription());
        }

        String ebStatus = ebObject.getStatus();
        if (ebStatus == null) {
            ebStatus = BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted;
            System.err.println("Warning: Server sent object with null status. id: " + ebObject.getId());
        } else {
            statusRef = new RegistryObjectRef(lcm, ebObject.getStatus());
        }

        List<ClassificationType> ebClasses = ebObject.getClassification();
        Iterator<?> iter = ebClasses.iterator();

        while (iter.hasNext()) {
            ClassificationType ebClass = (ClassificationType) iter.next();
            internalAddClassification(new ClassificationImpl(lcm, ebClass, this));
        }

        List<ExternalIdentifierType> extIds = ebObject.getExternalIdentifier();
        iter = extIds.iterator();

        while (iter.hasNext()) {
            ExternalIdentifierType ebExtIdentifier = (ExternalIdentifierType) iter.next();
            internalAddExternalIdentifier(new ExternalIdentifierImpl(lcm,
                    ebExtIdentifier));
        }

        objectTypeRef = new RegistryObjectRef(lcm, ebObject.getObjectType());

        versionInfo = ebObject.getVersionInfo();

        newObjOwner = null;
    }

    public int getStatus() throws JAXRException {
        //TODO: Need to move status to RegistryObject in JAXR 2.0
        int status = RegistryEntry.STATUS_SUBMITTED;
        try {
            if (statusRef != null) {
                String ebStatus = statusRef.getId();

                if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Approved)) {
                    status = RegistryEntry.STATUS_APPROVED;
                } else if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Deprecated)) {
                    status = RegistryEntry.STATUS_DEPRECATED;
                } else if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Submitted)) {
                    status = RegistryEntry.STATUS_SUBMITTED;
                } else if (ebStatus.equals(BindingUtility.CANONICAL_STATUS_TYPE_ID_Withdrawn)) {
                    status = RegistryEntry.STATUS_WITHDRAWN;
                } else {
                    status = RegistryEntry.STATUS_WITHDRAWN + 1; //Unknown?
                }
            }
        } catch (JAXRException e) {
            //cannot happen
            log.error(e);
        }
        return status;
    }

    public String getStatusAsString() {
        String statusAsString = "";
        try {
            if (statusRef != null) {
                @SuppressWarnings("static-access")
				ConceptImpl statusConcept = (ConceptImpl)statusRef.getRegistryObject(lcm.CONCEPT);
                statusAsString = statusConcept.getDisplayName();
            }
        } catch (JAXRException e) {
            //cannot happen
            log.error(e);
        }

        return statusAsString;
    }

    public boolean isModified() {
        return super.isModified()
            || (name != null && ((InternationalStringImpl)name).isModified())
            || (description != null && ((InternationalStringImpl)description).isModified());
    }

    /**
     * Implementation private
     */
    public void setModified(boolean modified) {
        super.setModified(modified);

        // propagate clear flag
        if (!modified && name != null) {
            ((InternationalStringImpl)name).setModified(modified);
        }
        if (!modified && description != null) {
            ((InternationalStringImpl)description).setModified(modified);
        }

        if (modified) {
            lcm.addModifiedObject(this);
        } else {
            lcm.removeModifiedObject(this);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getStatusRef() throws JAXRException {
        return statusRef;
    }

    //??JAXR 2.0
    public void setStatusRef(RegistryObjectRef statusRef)
        throws JAXRException {

        //Only set if different
        if ((this.statusRef == null) || (!(this.statusRef.getId().equals(statusRef.getId())))) {
            this.statusRef = statusRef;
            setModified(true);
        }
    }

    //??JAXR 2.0
    public RegistryObjectRef getObjectTypeRef() throws JAXRException {
        return objectTypeRef;
    }

    public Concept getObjectType() throws JAXRException {
        Concept objectType = null;

        if (objectTypeRef != null) {
            objectType = (Concept)objectTypeRef.getRegistryObject("ClassificationNode");
        }

        return objectType;
    }

    /**
     * Internal method to set the objectType
     */
    @SuppressWarnings("static-access")
	void setObjectTypeInternal(Concept objectType)
        throws JAXRException {

        if (objectType == null) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.objectTypeConceptMustNotBeNull"));            
        }
        
        if (!objectType.getClassificationScheme().getKey().getId().
                equals(bu.CANONICAL_CLASSIFICATION_SCHEME_ID_ObjectType)) {
            throw new InvalidRequestException(JAXRResourceBundle.getInstance().getString("message.mustBeObjectTypeConcept"));
        }
        objectTypeRef = new RegistryObjectRef(lcm, objectType);
        setModified(true);
    }

    public InternationalString getDescription() throws JAXRException {
        if (description == null) {
            description = lcm.createInternationalString(null);
        }

        return description;
    }

    public void setDescription(InternationalString desc)
        throws JAXRException {
        description = desc;
        setModified(true);
    }

    /**
     * Add to JAXR 2.0??
     */
    public String getLid() throws JAXRException {
        return lid;
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setLid(String lid)
        throws JAXRException {
        this.lid = lid;
        setModified(true);
    }

    /**
     * Add to JAXR 2.0??
     */
    public VersionInfoType getVersionInfo() throws JAXRException {
        return versionInfo;
    }

    /**
     * Add to JAXR 2.0??
     */
    public void setVersionInfo(VersionInfoType versionInfo)
        throws JAXRException {
        this.versionInfo = versionInfo;
        setModified(true);
    }

    //Added for convenience in use in config.xml for RegistryBrowser. Should not be in JAXR API
    public String getVersionName() throws JAXRException {
        String versionName = "";
        if (versionInfo != null) {
            versionName = versionInfo.getVersionName();
        }
        return versionName;
    }

    //Added for convenience in use in config.xml for RegistryBrowser. Should not be in JAXR API
    public String getComment() throws JAXRException {
        String comment = "";
        if (versionInfo != null) {
            comment = versionInfo.getComment();
        }
        return comment;
    }

    /**
     * Gets the name of this object that is suitable for display in UIs.
     * This is typically the String in closest matching locale for the name of the object.
     * Add to JAXR2.0??
     *
     * @return the name suitable for display.
     */
    public String getDisplayName() throws JAXRException {
        String displayName = null;

        if (name != null) {
            displayName = ((InternationalStringImpl) name).getClosestValue();
        }

        if (displayName == null) {
            displayName = "";
        }

        return displayName;
    }

    public InternationalString getName() throws JAXRException {
        if (name == null) {
            name = lcm.createInternationalString(null);
        }

        return name;
    }

    public void setName(InternationalString name) throws JAXRException {
        this.name = name;
        setModified(true);
    }

    public void setKey(Key key) throws JAXRException {
        this.key = key;
        setModified(true);
    }

    /** Internal method, does not set modified flag. */
    private void internalAddClassification(Classification c)
        throws JAXRException {
        getClassifications().add(c);
        c.setClassifiedObject(this);
    }

    public void addClassification(Classification c) throws JAXRException {
        internalAddClassification(c);
        setModified(true);
    }

    public void addClassifications(@SuppressWarnings("rawtypes") Collection classifications)
        throws JAXRException {
        Iterator<?> iter = classifications.iterator();

        while (iter.hasNext()) {
            Classification cls = (Classification) iter.next();
            internalAddClassification(cls);
        }

        setModified(true);
    }

    public void removeClassification(Classification c)
        throws JAXRException {
        if (classifications != null) {
            getClassifications().remove(c);
            setModified(true);
        }
    }

    public void removeClassifications(@SuppressWarnings("rawtypes") Collection classifications)
        throws JAXRException {
        if (classifications != null) {
            getClassifications().removeAll(classifications);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllClassifications() throws JAXRException {
        if (classifications != null) {
            removeClassifications(classifications);
            setModified(true);
        }
    }

    public void setClassifications(@SuppressWarnings("rawtypes") Collection classifications)
        throws JAXRException {
        removeAllClassifications();

        addClassifications(classifications);
        setModified(true);
    }

    public ArrayList<RegistryObject> getClassifications() throws JAXRException {
        if (classifications == null) {
            classifications = new ArrayList<RegistryObject>();
        }

        return classifications;
    }

    /**
     * Gets all Concepts classifying this object that have specified path as prefix.
     * Used in RegistryObjectsTableModel.getValueAt via reflections API if so configured.
     */
    public Collection<Concept> getClassificationConceptsByPath(String pathPrefix)
        throws JAXRException {
        Collection<Concept> matchingClassificationConcepts = new ArrayList<Concept>();
        ArrayList<RegistryObject> _classifications = getClassifications();
        Iterator<RegistryObject> iter = _classifications.iterator();

        while (iter.hasNext()) {
            Classification cl = (Classification) iter.next();
            Concept concept = cl.getConcept();
            String conceptPath = concept.getPath();

            if (conceptPath.startsWith(pathPrefix)) {
                matchingClassificationConcepts.add(concept);
            }
        }

        return matchingClassificationConcepts;
    }

    @SuppressWarnings("unchecked")
	public Collection<RegistryObject> getAuditTrail() throws JAXRException {
        Collection<RegistryObject> auditTrail = null;
        if (!isNew()) {
//          String queryStr = "SELECT ae.* FROM AuditableEvent ae, AffectedObject ao, RegistryObject ro WHERE ro.lid='" + lid + "' AND ro.id = ao.id AND ao.eventId = ae.id ORDER BY ae.timeStamp_ ASC";
//          Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
        	
        	Query query = SQLQueryProvider.getAuditTrail(dqm, lid);
            
        	BulkResponse response = dqm.executeQuery(query);

            checkBulkResponseExceptions(response);
            auditTrail = response.getCollection();
        }

        if (auditTrail == null) {
            auditTrail = new ArrayList<RegistryObject>();
        }

        return auditTrail;
    }

    /**
     * Add this to the JAXR 2.0 API??
     *
     * @return owner, ie. creator or null if this is a new object
     */
    public User getOwner() throws JAXRException {
        User user = null;
        if (!isNew()) {
            // Ask server who our creator is
            Collection<RegistryObject> events = getAuditTrail();

            for (Iterator<RegistryObject> it = events.iterator(); it.hasNext();) {
                AuditableEventImpl ev = (AuditableEventImpl) it.next();

                if ((ev.getEventType() == AuditableEvent.EVENT_TYPE_CREATED) ||
                    (ev.getEventType() == AuditableEventImpl.EVENT_TYPE_RELOCATED)) {
                    user =  ev.getUser();
                }
            }
        } else {
            user = getNewObjectOwner();
        }

        return user;
    }

    public void addAssociation(Association ass) throws JAXRException {
        getAssociations();

        if (!(associations.contains(ass))) {
            associations.add(ass);
        }

        ((AssociationImpl) ass).setSourceObjectInternal(this);
    }

    public void addAssociations(@SuppressWarnings("rawtypes") Collection asses) throws JAXRException {
        @SuppressWarnings("unchecked")
		Iterator<RegistryObject> iterator = asses.iterator();
		for (Iterator<RegistryObject> it = iterator; it.hasNext();) {
            Association ass = (Association) it.next();
            addAssociation(ass);
        }
    }

    public void removeAssociation(Association ass) throws JAXRException {
        getAssociations();

        if (associations.contains(ass)) {
            associations.remove(ass);

            //Need to mark as deleted and only remove from server on Save in future.
            //For now leaving as is in order to minimize change.???
            // Remove from server only if Association exists there
            if (!((AssociationImpl) ass).isNew()) {
                // assert(Association must exist on server)
                ArrayList<Key> keys = new ArrayList<Key>();
                keys.add(ass.getKey());

                BulkResponse response = lcm.deleteObjects(keys);
                JAXRException ex = getBulkResponseException(response);

                if (ex != null) {
                    throw ex;
                }
            }

            //No need to call setModified(true) since RIM modified object is an Assoociation
            //setModified(true);
        }
    }

    public void removeAssociations(@SuppressWarnings("rawtypes") Collection asses) throws JAXRException {
        Collection<Association> savedAsses = getAssociations();

        if (associations.removeAll(asses)) {
            // Remove from server only if Association exists there
            ArrayList<Key> keys = new ArrayList<Key>();

            for (Iterator<?> it = asses.iterator(); it.hasNext();) {
                AssociationImpl ass = (AssociationImpl) it.next();

                if (!ass.isNew()) {
                    // assert(Association must exist on server)
                    keys.add(ass.getKey());
                }
            }

            //TODO: IN future only mark as deleted and delete on save.
            BulkResponse response = lcm.deleteObjects(keys);
            JAXRException ex = getBulkResponseException(response);

            if (ex != null) {
                // Undo remove
                // ??eeg Assumes all-or-nothing delete
                associations = savedAsses;
                throw ex;
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void setAssociations(Collection asses) throws JAXRException {
        // We make a copy of this.associations to avoid a
        // concurrent modification exception
        removeAssociations(new ArrayList(getAssociations()));
        addAssociations(asses);
    }

    public Collection<Association> getAssociations() throws JAXRException {
        if (associations == null) {
            associations = new HashSet<Association>();

            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {
                // Return Collection from server
                String id = getKey().getId();
                String queryStr =
                    "SELECT ass.* FROM Association ass WHERE sourceObject = '" + id +
                    "'";
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addAssociations(response.getCollection());
            }
        }

        return associations;
    }

    @SuppressWarnings("unchecked")
	public Collection<RegistryObject> getAssociatedObjects() throws JAXRException {
        Collection<RegistryObject> assObjects = null;
        if (isNew()) {
            assObjects = new ArrayList<RegistryObject>();
            if (associations != null) {
                Iterator<Association> iter = associations.iterator();
                while (iter.hasNext()) {
                    Association ass = iter.next();
                    assObjects.add(ass.getTargetObject());
                }
            }
        }
        else {
            String id = getKey().getId();
//            String queryStr = "SELECT ro.* FROM RegistryObject ro, Association ass WHERE ass.sourceObject = '" +
//                id + "' AND ass.targetObject = ro.id";
//            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);
            
            Query query = SQLQueryProvider.getAssociatedObjects(dqm, id);

            BulkResponse response = dqm.executeQuery(query);
            checkBulkResponseExceptions(response);
            assObjects = response.getCollection();
        }

        return assObjects;
    }

    @SuppressWarnings("unchecked")
	public Collection<RegistryObject> getAllAssociations() throws JAXRException {
        if (isNew()) {
            // ??eeg Still can have client side associated objects!
            // Return an empty Collection instead of null
            return new ArrayList<RegistryObject>();
        }

        String id = getKey().getId();
//        String queryStr = "SELECT ass.* FROM Association ass WHERE sourceObject = '" +
//            id + "' OR targetObject = '" + id + "'" + " ORDER BY " +
//            "sourceObject, targetObject, associationType";
//        Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

        Query query = SQLQueryProvider.getAllAssociations(dqm, id);
        		
        BulkResponse response = dqm.executeQuery(query);
        checkBulkResponseExceptions(response);

        return response.getCollection();
    }

    /** Internal method, does not set modified flag. */
    private void internalAddExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        getExternalIdentifiers().add(ei);
        ((ExternalIdentifierImpl)ei).setRegistryObject(this);
    }

    public void addExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        internalAddExternalIdentifier(ei);
        setModified(true);
    }

    public void addExternalIdentifiers(@SuppressWarnings("rawtypes") Collection extIds)
        throws JAXRException {
        Iterator<?> iter = extIds.iterator();

        while (iter.hasNext()) {
            ExternalIdentifier extId = (ExternalIdentifier) iter.next();
            internalAddExternalIdentifier(extId);
        }
        setModified(true);
    }

    public void removeExternalIdentifier(ExternalIdentifier ei)
        throws JAXRException {
        if (externalIds != null) {
            externalIds.remove(ei);
            setModified(true);
        }
    }

    public void removeExternalIdentifiers(@SuppressWarnings("rawtypes") Collection ei)
        throws JAXRException {
        if (externalIds != null) {
            externalIds.removeAll(ei);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllExternalIdentifiers() throws JAXRException {
        if (externalIds != null) {
            removeExternalIdentifiers(externalIds);
            setModified(true);
        }
    }

    public void setExternalIdentifiers(@SuppressWarnings("rawtypes") Collection extIds)
        throws JAXRException {
        removeAllExternalIdentifiers();

        addExternalIdentifiers(extIds);
        setModified(true);
    }

    public Collection<ExternalIdentifier> getExternalIdentifiers() throws JAXRException {
        if (externalIds == null) {
            externalIds = new ArrayList<ExternalIdentifier>();
        }

        return externalIds;
    }

    public void addExternalLink(ExternalLink extLink) throws JAXRException {
        getExternalLinks();

        // If the external link is not in this object's in-memory-cache of
        // external links, add it.
        if (!(externalLinks.contains(extLink))) {
            // Check that an ExternallyLinks association exists between this
            // object and its external link.
            boolean associationExists = false;
            BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                          .getBusinessQueryManager());
            Concept assocType = bqm.findConceptByPath(
                    "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_ExternallyLinks);
            @SuppressWarnings("unchecked")
			Collection<Association> linkAssociations = extLink.getAssociations();

            if (linkAssociations != null) {
                Iterator<Association> assIter = linkAssociations.iterator();

                while (assIter.hasNext()) {
                    Association ass = assIter.next();

                    if (ass.getSourceObject().equals(extLink) &&
                            ass.getTargetObject().equals(this) &&
                            ass.getAssociationType().equals(assocType)) {
                        associationExists = true;

                        break;
                    }
                }
            }

            // Create the association between the external link and this object,
            // if necessary.
            if (!associationExists) {
                Association ass = lcm.createAssociation(this, assocType);
                extLink.addAssociation(ass);
            }

            externalLinks.add(extLink);

            // Note: There is no need to call setModified(true) since
            // the RIM modified object is an Association
        }
    }

    public void addExternalLinks(@SuppressWarnings("rawtypes") Collection extLinks) throws JAXRException {
        Iterator<?> iter = extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();
            addExternalLink(extLink);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation
    }

    public void removeExternalLink(ExternalLink extLink)
        throws JAXRException {
        getExternalLinks();

        if (externalLinks.contains(extLink)) {
            externalLinks.remove(extLink);

            //Now remove the ExternallyLinks association that has extLink as src and this object as target
            // We make a copy of this.externalLinks to avoid a
            // concurrent modification exception in the removeExternalLinks
            @SuppressWarnings("unchecked")
			Collection<Association> linkAssociations = new ArrayList<Association>(extLink.getAssociations());

            if (linkAssociations != null) {
                Iterator<Association> iter = linkAssociations.iterator();

                while (iter.hasNext()) {
                    Association ass = iter.next();

                    if (ass.getTargetObject().equals(this)) {
                        if (ass.getAssociationType().getValue()
                                   .equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks)) {
                            extLink.removeAssociation(ass);
                        }
                    }
                }
            }

            //No need to call setModified(true) since RIM modified object is an Assoociation
            //setModified(true);
        }
    }

    public void removeExternalLinks(@SuppressWarnings("rawtypes") Collection extLinks)
        throws JAXRException {
        getExternalLinks();

        //Avoid ConcurrentModificationException
        @SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayList _extLinks = new ArrayList(extLinks);
        Iterator<?> iter = _extLinks.iterator();

        while (iter.hasNext()) {
            ExternalLink extLink = (ExternalLink) iter.next();
            removeExternalLink(extLink);
        }

        //No need to call setModified(true) since RIM modified object is an Assoociation    }
    }

    /** Set this object's list of external links to the list specified. If the
      * current list of external links contains links that are not in the specified
      * list, they will be removed and the association between them and this object
      * will be removed from the server. For any external links that are in the
      * list specified, an association will be created (in-memory, not on the
      * server) and they will be added to this object's list of external links.
      *
      * @param newExternalLinks
      *     A Collection of ExternalLink objects.
      * @throws JAXRException
      */
    public void setExternalLinks(@SuppressWarnings("rawtypes") Collection newExternalLinks)
        throws JAXRException {
        //Avoid ConcurrentModificationException by using a copy
        Collection<ExternalLink> currentExternalLinks = new ArrayList<ExternalLink>(getExternalLinks());

        // Add any external links that are not currently in this object's list.
        Iterator<?> newExtLinksIter = newExternalLinks.iterator();

        while (newExtLinksIter.hasNext()) {
            ExternalLink externalLink = (ExternalLink) newExtLinksIter.next();

            if (!currentExternalLinks.contains(externalLink)) {
                addExternalLink(externalLink);
            }
        }

        // Remove any external links that are currently in this object's list,
        // but are not in the new list.
        Iterator<ExternalLink> currentExternalIter = currentExternalLinks.iterator();

        while (currentExternalIter.hasNext()) {
            ExternalLink externalLink = currentExternalIter.next();

            if (!newExternalLinks.contains(externalLink)) {
                removeExternalLink(externalLink);
            }
        }
    }

    public Collection<ExternalLink> getExternalLinks() throws JAXRException {
        if (externalLinks == null) {
            externalLinks = new HashSet<ExternalLink>();

            //If existing object then now is the time to do lazy fetch from server
            if (!isNew()) {
                String id = getId();
//                String queryStr =
//                    "SELECT el.* FROM ExternalLink el, Association ass WHERE ass.targetObject = '" +
//                    id + "' AND ass.associationType = '" +
//                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_ExternallyLinks +
//                    "' AND ass.sourceObject = el.id ";
//                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, queryStr);

                Query query = SQLQueryProvider.getExternalLinks(dqm, id);
                		
                BulkResponse response = dqm.executeQuery(query);
                checkBulkResponseExceptions(response);
                addExternalLinks(response.getCollection());
            }
        }

        return externalLinks;
    }

    public Organization getSubmittingOrganization() throws JAXRException {
        return org;
    }

    public Collection<?> getRegistryPackages() throws JAXRException {
        if (packages == null) {
            if (!isNew()) {
                HashMap<String, String> parameters = new HashMap<String, String>();
                parameters.put(CanonicalConstants.CANONICAL_SLOT_QUERY_ID,
                        CanonicalConstants.CANONICAL_QUERY_GetRegistryPackagesByMemberId);
                parameters.put("$memberId", getId());
                Query query = dqm.createQuery(Query.QUERY_TYPE_SQL);
                BulkResponse response = dqm.executeQuery(query, parameters);
                checkBulkResponseExceptions(response);
                @SuppressWarnings("unused")
				Collection<?> registryObjects = response.getCollection();
                packages = response.getCollection();
            }

            if (packages == null) {
                packages = new ArrayList<Object>();
            }
        }

        return packages;
    }

    protected void setBindingObject(RegistryObjectType ebRegistryObjectType)
        throws JAXRException {
        // Pass ebObject to superclass so slot-s can be initialized
        super.setBindingObject(ebRegistryObjectType);

        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        //Set by registry, but we need to specify it as XACML ACPs may use objectType as resource attribute
        if (objectTypeRef != null) {
            ebRegistryObjectType.setObjectType(objectTypeRef.getId());
        }

        ebRegistryObjectType.setLid(lid);
        ebRegistryObjectType.setVersionInfo(versionInfo);
		
		/*
		 * Name
		 */
		InternationalStringType ebNameType = factory.createInternationalStringType(); 
		((InternationalStringImpl) getName()).setBindingObject(ebNameType);
		
		/*
		 * Add LocalizedStringType to Name
		 */
//		LocalizedStringType ebNameString = factory.createLocalizedStringType(); 
//		ebNameString.setValue(getName().getValue());
//		ebNameType.getLocalizedString().add(ebNameString);

		ebRegistryObjectType.setName(ebNameType);

		/*
		 * Description
		 */
		InternationalStringType ebDescriptionType = factory.createInternationalStringType(); 
		((InternationalStringImpl) getDescription()).setBindingObject(ebDescriptionType);

		/*
		 * Add LocalizedStringType to Name
		 */
//		LocalizedStringType ebDescriptionString = factory.createLocalizedStringType(); 
//		ebDescriptionString.setValue(getDescription().getValue());
//		ebDescriptionType.getLocalizedString().add(ebDescriptionString);
		
		ebRegistryObjectType.setDescription(ebDescriptionType);

		
		Iterator<?> iter = getClassifications().iterator();

		while (iter.hasNext()) {
		    ClassificationImpl cls = (ClassificationImpl) iter.next();
		    ClassificationType ebClassificationType = (ClassificationType) cls.toBindingObject();
		    ebRegistryObjectType.getClassification().add(ebClassificationType);
		}

		iter = getExternalIdentifiers().iterator();

		while (iter.hasNext()) {
		    ExternalIdentifierImpl extId = (ExternalIdentifierImpl) iter.next();
		    ExternalIdentifierType ebExternalIdentifierType = (ExternalIdentifierType) extId.toBindingObject();
		    ebRegistryObjectType.getExternalIdentifier().add(ebExternalIdentifierType);
		}
    }

    /**
     * RIM Composed objects are composed objects as defined by RIM.
     * Composed objects are composed objects as defined by JAXR.
     * JAXR defines more composed objects than RIM does.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getRIMComposedObjects() throws JAXRException {
        HashSet composedObjects = super.getRIMComposedObjects();

        Collection classifications = getClassifications();
        composedObjects.addAll(classifications);

        Collection extIds = getExternalIdentifiers();
        composedObjects.addAll(extIds);

        Collection slotIds = getSlots();
        composedObjects.addAll(slotIds);

        return composedObjects;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();

        Collection classifications = getClassifications();
        composedObjects.addAll(classifications);

        Collection extIds = getExternalIdentifiers();
        composedObjects.addAll(extIds);

        Collection extLinks = getExternalLinks();
        composedObjects.addAll(extLinks);

        composedObjects.addAll(getAssociationsAndAssociatedObjects());

        Collection slotIds = getSlots();
        composedObjects.addAll(slotIds);

        return composedObjects;
    }

    /**
     * @return First exception in BulkResponse if there is one else null
     */
    RegistryException getBulkResponseException(BulkResponse response)
        throws JAXRException {
        Collection<?> exceptions = response.getExceptions();

        if (exceptions != null) {
            return (RegistryException) exceptions.iterator().next();
        }

        return null;
    }

    /**
     * Throw first exception in BulkResponse if there is one else return
     */
    void checkBulkResponseExceptions(BulkResponse response)
        throws JAXRException {
        RegistryException ex = getBulkResponseException(response);

        if (ex != null) {
            throw ex;
        }

        return;
    }

    /**
     * Gest all Associations and their targets for which this object is a source.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<ExtensibleObject> getAssociationsAndAssociatedObjects()
        throws JAXRException {
        HashSet<ExtensibleObject> assObjects = new HashSet<ExtensibleObject>();

        // Automatically save any Association-s with an object in the save
        // list along with the target object of the Association per JAXR 1.0 spec.
        Collection<Association> asses = getAssociations();

        // Add the Association targets
        for (Iterator<Association> j = asses.iterator(); j.hasNext();) {
            AssociationImpl ass = (AssociationImpl) j.next();
            try {
                RegistryObject target = ass.getTargetObject();
                assObjects.add(target);
            } catch (UnresolvedReferenceException e) {
                //This happens when the targetObject is a remote ObjectRef
                //Handle this by adding the RegistryObjectRef instead
                RegistryObjectRef target = ass.getTargetObjectRef();
                assObjects.add(target);
            }
        }

        // Add also the Association-s themselves
        assObjects.addAll(asses);

        return assObjects;
    }

    /**
     * Gets all referenced objects for which this object is a referant.
     * Extended by base classes.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    public HashSet<?> getRegistryObjectRefs() {
        HashSet<Object> refs = new HashSet<Object>();

        return refs;
    }

    /*
     * Lazy fetches and returns newObjectOwner.
     * Fixes performance penalty where query was made to server when a
     * new RegistryObject was created.
     *
     */
    private User getNewObjectOwner() throws JAXRException {
        if (newObjOwner == null) {
            newObjOwner = dqm.getCallersUser();
        }

        return newObjOwner;
    }

}

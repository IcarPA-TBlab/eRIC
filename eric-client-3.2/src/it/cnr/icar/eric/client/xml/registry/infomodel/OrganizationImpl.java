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
import it.cnr.icar.eric.client.xml.registry.util.JAXRResourceBundle;
import it.cnr.icar.eric.common.BindingUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.registry.DeclarativeQueryManager;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import org.oasis.ebxml.registry.bindings.rim.EmailAddressType;
import org.oasis.ebxml.registry.bindings.rim.ObjectFactory;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.PostalAddressType;
import org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType;


/**
 * Implements JAXR API interface named Organization.
 *
 * @author <a href="mailto:farrukh@wellfleetsoftware.com">Farrukh S. Najmi</a>
 */
public class OrganizationImpl extends RegistryObjectImpl implements Organization {
    it.cnr.icar.eric.common.BindingUtility bu = it.cnr.icar.eric.common.BindingUtility.getInstance();
    private RegistryObjectRef parentRef = null;
    private List<PostalAddressImpl> addresses = new ArrayList<PostalAddressImpl>();
    private RegistryObjectRef primaryContactRef = null;
    private ArrayList<TelephoneNumber> phones = new ArrayList<TelephoneNumber>();    
    private ArrayList<EmailAddress> emailAddresses = new ArrayList<EmailAddress>();
    private HashSet<Organization> childOrgs = new HashSet<Organization>();
    private boolean childOrgsLoaded = false;
    private HashSet<RegistryObject> users = new HashSet<RegistryObject>();
    private boolean usersLoaded=false;

    public OrganizationImpl(
        it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl lcm)
        throws JAXRException {
        super(lcm);

        PostalAddressImpl address = new PostalAddressImpl(lcm);
        addresses.add(address);
    }

    public OrganizationImpl(
        it.cnr.icar.eric.client.xml.registry.LifeCycleManagerImpl lcm,
        org.oasis.ebxml.registry.bindings.rim.OrganizationType ebOrg)
        throws JAXRException {
        super(lcm, ebOrg);

        List<PostalAddressType> _addresses = ebOrg.getAddress();
        
        if (_addresses.size() > 0) {
            Iterator<PostalAddressType> itr = _addresses.iterator();
            while (itr.hasNext()) {
                PostalAddressType addressType = itr.next();
                addresses.add(new PostalAddressImpl(lcm, addressType));
            }
        }

        Iterator<TelephoneNumberType> tels = ebOrg.getTelephoneNumber().iterator();

        while (tels.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType tel = tels.next();
            phones.add(new TelephoneNumberImpl(lcm, tel));
        }
        
        Iterator<EmailAddressType> emails = ebOrg.getEmailAddress().iterator();

        while (emails.hasNext()) {
            org.oasis.ebxml.registry.bindings.rim.EmailAddressType email = emails.next();
            emailAddresses.add(new EmailAddressImpl(lcm, email));
        }

        Object ebParentRef = ebOrg.getParent();

        if (ebParentRef != null) {
            parentRef = new RegistryObjectRef(lcm, ebParentRef);
        }

        Object ebPrimaryContactRef = ebOrg.getPrimaryContact();

        if (ebPrimaryContactRef != null) {
            primaryContactRef = new RegistryObjectRef(lcm, ebPrimaryContactRef);
        } else {
            throw new JAXRException(
                JAXRResourceBundle.getInstance().getString("message.error.primary.contact.null"));
        }
    }

    public javax.xml.registry.infomodel.PostalAddress getPostalAddress()
        throws JAXRException {
        // We need to support this method for JAXR 1.x
        PostalAddress address = null;
        Collection<PostalAddressImpl> addresses = getPostalAddresses();
        if (addresses.iterator().hasNext()) {        
            // Return the first member of the Collection
            address = addresses.iterator().next();
        }
        return address;
    }

    public void setPostalAddress(
        javax.xml.registry.infomodel.PostalAddress par1)
        throws JAXRException {
        // XXX Code assumes that par1 will always be a PostalAddressImpl
        PostalAddressImpl address = (PostalAddressImpl) par1;
        // We need to support this method for JAXR 1.x
        // Add this address as first member of the Collection
        List<PostalAddressImpl> addresses = getPostalAddresses();
        addresses.add(0, address);
        setModified(true);
    }
    
    // Add to JAXR 2.0 API?
    public List<PostalAddressImpl> getPostalAddresses() {
        if (addresses == null) {
            addresses = new ArrayList<PostalAddressImpl>();
        }
        return addresses;
    }
    
    //Add to JAXR 2.0??
    public void setPostalAddresses(Collection<?> _addresses)
        throws JAXRException {
        removeAllPostalAddresses();
        addPostalAddresses(_addresses);
        
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void addPostalAddresses(Collection<?> addresses)
        throws JAXRException {
        Iterator<?> iter = addresses.iterator();

        while (iter.hasNext()) {
            PostalAddress address = (PostalAddress) iter.next();
            addPostalAddress((PostalAddressImpl) address);
        }

        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void addPostalAddress(PostalAddressImpl _address)
        throws JAXRException {
        getPostalAddresses().add(_address);
        setModified(true);
    }
    
    //Add to JAXR 2.0??
    public void removePostalAddress(PostalAddress _address)
        throws JAXRException {
        if (addresses != null) {
            getPostalAddresses().remove(_address);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    public void removePostalAddresses(List<PostalAddressImpl> addresses2)
        throws JAXRException {
        if (addresses != null) {
            getPostalAddresses().removeAll(addresses2);
            setModified(true);
        }
    }
    
    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllPostalAddresses() throws JAXRException {
        if (addresses != null) {
            removePostalAddresses(addresses);
            setModified(true);
        }
    }

    public User getPrimaryContact() throws JAXRException {
        javax.xml.registry.infomodel.RegistryObject primaryContact = null;

        if (primaryContactRef != null) {
            primaryContact = primaryContactRef.getRegistryObject("User");
        }

        return (User) primaryContact;
    }

    public void setPrimaryContact(User primaryContact)
        throws JAXRException {
        primaryContactRef = new RegistryObjectRef(lcm, primaryContact);
        addUser(primaryContact);
        setModified(true);
    }

    public void addUser(User user) throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_AffiliatedWith);
        Association ass = lcm.createAssociation(this, assocType);
        ass.setKey(lcm.createKey(bu.createAssociationId(user.getKey().getId(), this.getKey().getId(), BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith)));
        user.addAssociation(ass);
        
        //Need to setOrganization on user to placate JAXR spec.
        ((UserImpl)user).setOrganization(this);
        getUsers().add(user);
        
        //No need to call setModified(true) since RIM modified object is an Assoociation				
    }

    public void addUsers(@SuppressWarnings("rawtypes") Collection users) throws JAXRException {
        Iterator<?> iter = users.iterator();
        while (iter.hasNext()) {
            addUser((User)(iter.next()));
        }
    }

    public void removeUser(User user) throws JAXRException {
        //Remove Association from user
	@SuppressWarnings("unchecked")
	HashSet<Object> associations = new HashSet<Object>(user.getAssociations());
        Iterator<Object> iter = associations.iterator();

        while (iter.hasNext()) {
            Association ass = (Association) iter.next();

            if ((ass.getTargetObject().equals(this)) && (ass.getSourceObject().equals(user))) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith)) {
                    //The association itself is removed in RegistryObject.removeAssociation().
		    user.removeAssociation(ass);
		}
            }
        }
        users.remove(user);
    }

    public void removeUsers(@SuppressWarnings("rawtypes") Collection users) throws JAXRException {
        @SuppressWarnings({ "unchecked", "rawtypes" })
		ArrayList _users = new ArrayList(getUsers());
        Iterator<?> iter = _users.iterator();
        while (iter.hasNext()) {
            removeUser((User)(iter.next()));
        }
    }

    @SuppressWarnings("unchecked")
	public Collection<RegistryObject> getUsers() throws JAXRException {
        if (!usersLoaded) {
            DeclarativeQueryManager dqm = lcm.getRegistryService()
                                             .getDeclarativeQueryManager();
            String qs = "SELECT u.* FROM User_ u, Association a WHERE a.sourceObject = u.id AND a.associationType = '" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AffiliatedWith + 
                "' AND a.targetObject = '" + getId() + "'";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
            users.addAll(dqm.executeQuery(query).getCollection());
            usersLoaded = true;
        }

        return users;
    }

    //Add to JAXR 2.0??
    @SuppressWarnings("unchecked")
	public void addTelephoneNumber(TelephoneNumber _phone)
        throws JAXRException {
        getTelephoneNumbers().add(_phone);
        setModified(true);
    }

    //Add to JAXR 2.0??
    public void addTelephoneNumbers(@SuppressWarnings("rawtypes") Collection telephoneNumbers)
        throws JAXRException {
        @SuppressWarnings("rawtypes")
		java.util.Iterator iter = telephoneNumbers.iterator();

        while (iter.hasNext()) {
            TelephoneNumber phone = (TelephoneNumber) iter.next();
            addTelephoneNumber(phone);
        }

        setModified(true);
    }
    
    //Add to JAXR 2.0??
    @SuppressWarnings("unchecked")
	public void addEmailAddress(EmailAddress _email)
        throws JAXRException {
        getEmailAddresses().add(_email);
        setModified(true);
    }

    //Add to JAXR 2.0??
    public void addEmailAddresses(@SuppressWarnings("rawtypes") Collection emailAddresses)
        throws JAXRException {
        java.util.Iterator<?> iter = emailAddresses.iterator();

        while (iter.hasNext()) {
            EmailAddress email = (EmailAddress) iter.next();
            addEmailAddress(email);
        }

        setModified(true);
    }

    //Add to JAXR 2.0??
    public void removeTelephoneNumber(TelephoneNumber _phone)
        throws JAXRException {
        if (phones != null) {
            getTelephoneNumbers().remove(_phone);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    @SuppressWarnings("unchecked")
	public void removeTelephoneNumbers(@SuppressWarnings("rawtypes") Collection _phones)
        throws JAXRException {
        if (phones != null) {
            getTelephoneNumbers().removeAll(_phones);
            setModified(true);
        }
    }   

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllTelephoneNumbers() throws JAXRException {
        if (phones != null) {
            removeTelephoneNumbers(phones);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    public void setTelephoneNumbers(@SuppressWarnings("rawtypes") Collection _phones)
        throws JAXRException {
        removeAllTelephoneNumbers();

        addTelephoneNumbers(_phones);
        setModified(true);
    }

    //Add to JAXR 2.0??
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getTelephoneNumbers() throws JAXRException {
        if (phones == null) {
            phones = new ArrayList();
        }

        return phones;
    }
      
    //Deprecate in JAXR 2.0??
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getTelephoneNumbers(String phoneType)
        throws JAXRException {
        List selTypePhones = new ArrayList();
        Collection allPhones = getTelephoneNumbers();
        Iterator phoneIterator = allPhones.iterator();
        while (phoneIterator.hasNext()) {
            TelephoneNumber phone = (TelephoneNumber)phoneIterator.next();
            if ((null == phoneType) || (phoneType.equalsIgnoreCase(phone.getType()))) {
                selTypePhones.add(phone);
            }
        }
        return selTypePhones;
    }
    
    //Add to JAXR 2.0??
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getEmailAddresses() throws JAXRException {
        if (emailAddresses == null) {
            emailAddresses = new ArrayList();
        }

        return emailAddresses;
    }

     //Deprecate in JAXR 2.0??
    @SuppressWarnings("rawtypes")
	public Collection getEmailAddresses(String emailType)
        throws JAXRException {
        return getEmailAddresses();

        //return (Collection)(phones.clone());
    }
    
    //Add to JAXR 2.0??
    public void removeEmailAddress(EmailAddress _email)
        throws JAXRException {
        if (emailAddresses != null) {
            getEmailAddresses().remove(_email);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    @SuppressWarnings("unchecked")
	public void removeEmailAddresses(@SuppressWarnings("rawtypes") Collection _email)
        throws JAXRException {
        if (emailAddresses != null) {
            getEmailAddresses().removeAll(_email);
            setModified(true);
        }
    }

    //??Add to JAXR 2.0. Apply same pattern to all Collection attributes in RIM.
    public void removeAllEmailAddresses() throws JAXRException {
        if (emailAddresses != null) {
            removeEmailAddresses(emailAddresses);
            setModified(true);
        }
    }

    //Add to JAXR 2.0??
    public void setEmailAddresses(@SuppressWarnings("rawtypes") Collection _emails)
        throws JAXRException {
        removeAllEmailAddresses();
        addEmailAddresses(_emails);
        
        setModified(true);
    }

    public void addService(Service service)
        throws JAXRException {
        BusinessQueryManagerImpl bqm = (BusinessQueryManagerImpl) (lcm.getRegistryService()
                                                                      .getBusinessQueryManager());
        Concept assocType = bqm.findConceptByPath(
                "/" + BindingUtility.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                BindingUtility.CANONICAL_ASSOCIATION_TYPE_CODE_OffersService);
        Association ass = lcm.createAssociation(service, assocType);
        addAssociation(ass);

        // Need to setProvidingOrganization on service to placate JAXR spec.
        // Only do so if service doesn't already have a providing organization.
        Organization providingOrg = service.getProvidingOrganization();
        if (providingOrg == null) {
            ((ServiceImpl)service).setProvidingOrganization(this);
        }
        
        //No need to call setModified(true) since RIM modified object is an Association				
    }

    public void addServices(@SuppressWarnings("rawtypes") Collection services) throws JAXRException {
        Iterator<?> iter = services.iterator();
        while (iter.hasNext()) {
            addService((Service)(iter.next()));
        }
    }

    public void removeService(Service service)
        throws JAXRException {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	HashSet associations = new HashSet(getAssociations());
        Iterator<?> iter = associations.iterator();

        while (iter.hasNext()) {
            Association ass = (Association) iter.next();

            if (ass.getTargetObject().equals(service)) {
                if (ass.getAssociationType().getKey().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_OffersService)) {
		    removeAssociation(ass);
		}
            }
        }

        // Also remove as providing organization of service, if providing org.
        Organization providingOrg = service.getProvidingOrganization();
        if ((providingOrg != null) &&
                providingOrg.getKey().getId().equals(this.getKey().getId())) {
            ((ServiceImpl)service).setProvidingOrganization(null);
        }
    }

    public void removeServices(@SuppressWarnings("rawtypes") Collection services) throws JAXRException {
        Iterator<?> iter = services.iterator();
        while (iter.hasNext()) {
            removeService((Service)(iter.next()));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getServices() throws JAXRException {
        Set services = new HashSet();

        Iterator iter = getAssociations().iterator();

        while (iter.hasNext()) {
            AssociationImpl ass = (AssociationImpl) iter.next();

            if (ass.getAssociationTypeRef().getId().equalsIgnoreCase(BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_OffersService)) {
                RegistryObject ro = ass.getTargetObject();
                if (ro instanceof Service) {
                    services.add(ass.getTargetObject());
                }
            }
        }

        return services;
    }

    @SuppressWarnings("unchecked")
	public void addChildOrganization(Organization par1)
        throws JAXRException {
        ((OrganizationImpl) par1).setParentOrganization(this);
        getChildOrganizations().add(par1);

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public void addChildOrganizations(@SuppressWarnings("rawtypes") Collection par1)
        throws JAXRException {
        Iterator<?> iter = par1.iterator();

        while (iter.hasNext()) {
            OrganizationImpl org = (OrganizationImpl) iter.next();
            addChildOrganization(org);
        }

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public void removeChildOrganization(Organization org)
        throws JAXRException {
        getChildOrganizations().remove(org);

        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    @SuppressWarnings("unchecked")
	public void removeChildOrganizations(@SuppressWarnings("rawtypes") Collection par1)
        throws JAXRException {
        getChildOrganizations().removeAll(par1);
        //No need to call setModified(true) since RIM does not require parent to remember children
    }

    public int getChildOrganizationCount() throws JAXRException {
        return getChildOrganizations().size();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getChildOrganizations() throws JAXRException {
        if (!childOrgsLoaded) {
            DeclarativeQueryManager dqm = lcm.getRegistryService()
                                             .getDeclarativeQueryManager();
            String qs = "SELECT o.* FROM Organization o WHERE o.parent = '" +
                getId() + "'";
            Query query = dqm.createQuery(Query.QUERY_TYPE_SQL, qs);
            childOrgs.addAll(dqm.executeQuery(query).getCollection());
            childOrgsLoaded = true;
        }

        return childOrgs;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection getDescendantOrganizations() throws JAXRException {
        ArrayList descendants = new ArrayList(childOrgs);
        java.util.Iterator iter = childOrgs.iterator();

        while (iter.hasNext()) {
            Organization child = (Organization) iter.next();

            if (child.getChildOrganizationCount() > 0) {
                descendants.addAll(child.getDescendantOrganizations());
            }
        }

        return descendants;
    }

    public Organization getParentOrganization() throws JAXRException {
        Organization parent = null;

        if (parentRef != null) {
            javax.xml.registry.infomodel.RegistryObject parentObj = parentRef.getRegistryObject(
                    "Organization");

            if (parentObj instanceof Organization) {
                parent = (Organization) parentObj;
            }
        }

        return parent;
    }

    public void setParentOrganization(Organization org) throws JAXRException {
        parentRef = new RegistryObjectRef(lcm, org);
        setModified(true);
    }

    public Organization getRootOrganization() throws JAXRException {
        Organization root = this;

        while (root.getParentOrganization() != null) {
            root = root.getParentOrganization();
        }

        return root;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getRIMComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getChildOrganizations());   
        composedObjects.addAll(getTelephoneNumbers());
        composedObjects.addAll(getEmailAddresses());
        composedObjects.addAll(getPostalAddresses());
        return composedObjects;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getComposedObjects()
        throws JAXRException {
        HashSet composedObjects = super.getComposedObjects();
        composedObjects.addAll(getChildOrganizations());   
        composedObjects.addAll(getTelephoneNumbers());
        composedObjects.addAll(getEmailAddresses());
        composedObjects.addAll(getPostalAddresses());
        composedObjects.addAll(getServices());
        composedObjects.addAll(getUsers());
        if (getPrimaryContact() != null) {
            composedObjects.add(getPrimaryContact());
        }
        return composedObjects;
    }
    
    /**
     * This method takes this JAXR infomodel object and returns an
     * equivalent binding object for it.  Note it does the reverse of one
     * of the constructors above.
     */
    public Object toBindingObject() throws JAXRException {
        /*
		 * JAXB v1 to JAXB v2 migration
		 * (C) 2011 Dr. Krusche & Partner PartG
		 * team@dr-kruscheundpartner.de
		 */
        ObjectFactory factory = BindingUtility.getInstance().rimFac;

        OrganizationType ebOrganizationType = factory.createOrganizationType(); 
		setBindingObject(ebOrganizationType);
		
//		JAXBElement<OrganizationType> ebOrganization = factory.createOrganization(ebOrganizationType);
		
		return ebOrganizationType;
    }

    protected void setBindingObject(OrganizationType ebOrganizationType)
        throws JAXRException {
        super.setBindingObject(ebOrganizationType);

        if (parentRef != null) {
            ebOrganizationType.setParent(parentRef.getId());
        }

        Iterator<PostalAddressImpl> itr = addresses.iterator();
        while (itr.hasNext()) {

            PostalAddressImpl address = itr.next();
            
            PostalAddressType ebPostalAddressType = (PostalAddressType) address.toBindingObject();
            ebOrganizationType.getAddress().add(ebPostalAddressType);  
        }

        if (primaryContactRef == null) {
            //Automatically assign primary contact to be the caller
            User user = ((it.cnr.icar.eric.client.xml.registry.QueryManagerImpl) (lcm.getRegistryService()
                                                                                      .getBusinessQueryManager())).getCallersUser();
            setPrimaryContact(user);
        }
        ebOrganizationType.setPrimaryContact(primaryContactRef.getId());
        
        @SuppressWarnings("rawtypes")
		java.util.Iterator iter = getTelephoneNumbers().iterator();
        while (iter.hasNext()) {
            TelephoneNumberImpl phone = (TelephoneNumberImpl) iter.next();

            TelephoneNumberType ebTelephoneNumberType = (TelephoneNumberType) phone.toBindingObject();
            ebOrganizationType.getTelephoneNumber().add(ebTelephoneNumberType);
        }
        
        @SuppressWarnings("rawtypes")
		java.util.Iterator eIter = getEmailAddresses().iterator();
        while (eIter.hasNext()) {
            EmailAddressImpl email = (EmailAddressImpl) eIter.next();

            EmailAddressType ebEmailAddressType = (EmailAddressType) email.toBindingObject();
            ebOrganizationType.getEmailAddress().add(ebEmailAddressType);
        }

    }
   
    /**
     * Gest all Associations and their targets for which this object is a source.
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public HashSet getAssociationsAndAssociatedObjects()
        throws JAXRException {
        HashSet assObjects = super.getAssociationsAndAssociatedObjects();

        // Automatically save any Association-s with an object in the save
        // list along with the target object of the Association per JAXR 1.0 spec.
        Collection<RegistryObject> users = getUsers();

        // Add the Association sources (the users)
        for (Iterator<RegistryObject> j = users.iterator(); j.hasNext();) {
            User user = (User) j.next();
            assObjects.add(user);
        }

        // Add also the Association-s themselves
        assObjects.addAll(users);

        return assObjects;
    }
    
   /**
     * Used by LifeCycleManagerImpl.saveObjects
     *
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public HashSet getRegistryObjectRefs() {
        HashSet refs = new HashSet();

        //refs.addAll(super.getRegistryObjectRefs());
        if (parentRef != null) {
            refs.add(parentRef);
        }

        if (primaryContactRef != null) {
            refs.add(primaryContactRef);
        }

        return refs;
    }

    @SuppressWarnings("unused")
	public void setModified(boolean modified) {
        super.setModified(modified);

        if (modified) {
            int i = 0;
        } else {
            int j = 0;
        }
    }
}

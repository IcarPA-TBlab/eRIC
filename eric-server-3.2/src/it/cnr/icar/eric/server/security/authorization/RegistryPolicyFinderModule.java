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
package it.cnr.icar.eric.server.security.authorization;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.RepositoryItem;
import it.cnr.icar.eric.server.common.RegistryProperties;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManager;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.repository.RepositoryManager;
import it.cnr.icar.eric.server.repository.RepositoryManagerFactory;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.registry.RegistryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.MatchResult;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicySet;
import com.sun.xacml.PolicyTreeElement;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.finder.PolicyFinder;
import com.sun.xacml.finder.PolicyFinderModule;
import com.sun.xacml.finder.PolicyFinderResult;


/**
 * This module represents a collection of files containing polices,
 * each of which will be searched through when trying to find a
 * policy that is applicable to a specific request.
 *
 * @author Farrukh Najmi
 */
public class RegistryPolicyFinderModule extends PolicyFinderModule {
        
    private static Log log = LogFactory.getLog(RegistryPolicyFinderModule.class);
    
    PolicyFinder finder = null;

    // The default Access Control Policy for the registry
    private AbstractPolicy defaultPolicy;
    private String idForDefaultACP = null;
    
    RepositoryManager rm = RepositoryManagerFactory.getInstance().getRepositoryManager();
    PersistenceManager pm = PersistenceManagerFactory.getInstance().getPersistenceManager();
    BindingUtility bu = BindingUtility.getInstance();
    ServerRequestContext requestContext = null;

    public RegistryPolicyFinderModule() {
        try {
            requestContext = new ServerRequestContext("RegistryPolicyFinderModule:findPolicy", null);
            requestContext.setUser(AuthenticationServiceImpl.getInstance().registryOperator);            
        } catch (RegistryException e) {
            log.error(e, e);
            throw new UndeclaredThrowableException(e);
        }
    }

    // Load the default Access Control Policy for the registry
    private AbstractPolicy loadDefaultPolicy(ServerRequestContext requestContext) throws RegistryException {
        AbstractPolicy policy = null;
        try {
            idForDefaultACP = RegistryProperties.getInstance().getProperty("eric.security.authorization.defaultACP");
            policy = loadPolicy(requestContext, idForDefaultACP);
            if (policy == null) {
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.defaultAccessControlPolicy"));
            }
        } 
        catch (RegistryException e) {
            throw new RegistryException(ServerResourceBundle.getInstance().getString("message.defaultAccessControlPolicy"), e);
        }
        return policy;
    }

    // Load the Access Control Policy that has the specified id
    private AbstractPolicy loadPolicy(ServerRequestContext requestContext, String id) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("Loading policy for id=" + id);
        }

        AbstractPolicy policy = null;
        RepositoryItem ri = null;

        if (id == null) {
            return policy;
        }
        
        if ((id.equalsIgnoreCase(idForDefaultACP)) && (defaultPolicy != null)) {
            return defaultPolicy;
        }

        //First check if it is in the repository as a top level Policy
        try {
            ri = rm.getRepositoryItem(id);
        } catch (RegistryException e) {
            log.debug(e, e);
        }

        //Not found as top level repository item
        //See if it is a composed policy within a top level policy in repository 
        if (ri == null) {
            //No policy found. Next find an ACP with specified URI as
            //a value for a Slot named "ComposedPolicies".
            AbstractPolicy rootPolicy = loadRootPolicyFor(requestContext, id);

            try {
                if (rootPolicy != null) {
                    PolicyTreeElement policyElement = getDescendantPolicyTreeElement(rootPolicy,
                            new URI(id));

                    if (policyElement instanceof AbstractPolicy) {
                        policy = (AbstractPolicy) policyElement;
                    }
                }
            } catch (URISyntaxException e) {
                throw new RegistryException(e);
            }
        } else {
            DataHandler dh = ri.getDataHandler();
            policy = loadPolicy(dh, finder);
        }

        return policy;
    }

    /**
     * Gets the Collection of all POlicyElements that are Descendants of specified PolicyTreeElement
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection getDescendantPolicyTreeElements(PolicyTreeElement parent) {
        List<?> children = parent.getChildren();
        List descendants = new ArrayList(parent.getChildren());
        Iterator<?> iter = children.iterator();

        while (iter.hasNext()) {
            PolicyTreeElement child = (PolicyTreeElement) iter.next();

            descendants.addAll(getDescendantPolicyTreeElements(child));
        }

        return descendants;
    }

    /**
     * Get the descendent AbstractPolciy composed within specified PolicyTreeElement object that
     * matches the specified id.
     */
    public PolicyTreeElement getDescendantPolicyTreeElement(
        PolicyTreeElement parent, URI descendantId) {
        PolicyTreeElement descendent = null;

        //System.err.println("AbstractPolicy.getDescendantPolicyTreeElement: descendantId=" + descendantId);
        Collection<?> descendants = getDescendantPolicyTreeElements(parent);

        Iterator<?> iter = descendants.iterator();

        while (iter.hasNext()) {
            PolicyTreeElement policyElement = (PolicyTreeElement) iter.next();

            //System.err.println("AbstractPolicy.getDescendantPolicyTreeElement: currentId=" + policyElement.getId());
            if (policyElement.getId().toString().equals(descendantId.toString())) {
                descendent = policyElement;

                break;
            }
        }

        return descendent;
    }

    /**
     * Returns the root or top level policy that contains a descendant policy with specified
     * id.
     */
    @SuppressWarnings("rawtypes")
	private AbstractPolicy loadRootPolicyFor(ServerRequestContext context, String composedPolicyId)
        throws RegistryException {
        log.debug("Loading root policy for composed policy with id=" +
            composedPolicyId);

        AbstractPolicy rootPolicy = null;

        try {
            String id = null;

            ResponseOptionType ebResponseOptionType =
                BindingUtility.getInstance().queryFac.createResponseOptionType();
            ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
            ebResponseOptionType.setReturnComposedObjects(true);

            String query = "SELECT policy.* from ExtrinsicObject policy, Slot s WHERE " +
                "((policy.objectType = '" + BindingUtility.CANONICAL_OBJECT_TYPE_LID_Policy+ "') OR " +
                "(policy.objectType = '" + BindingUtility.CANONICAL_OBJECT_TYPE_LID_PolicySet + "')) AND s.name_ = 'ComposedPolicies' AND policy.id = " +
                "s.parent AND s.value = '" + composedPolicyId + "'";

            List objectRefs = new ArrayList();
            List<IdentifiableType> ebIdentifiableTypeResultList = pm.executeSQLQuery(context, query, ebResponseOptionType,
                    "ExtrinsicObject", objectRefs);
            List<String> ids = bu.getIdsFromRegistryObjectTypes(ebIdentifiableTypeResultList);

            int cnt = ids.size();

            if (cnt > 1) {
                log.warn(ServerResourceBundle.getInstance().getString("message.MoreThan1AccessControlPolicyFoundContainingComposedPolicy", new Object[]{composedPolicyId}));
            }

            if (cnt >= 1) {
                id = ids.get(0);

                rootPolicy = loadPolicy(context, id);
            }
        } catch (RegistryException e) {
            throw e;
        } catch (Exception e) {
            throw new RegistryException(e);
        }

        return rootPolicy;
    }

    /**
     * Loads a policy from the DataHandler, using the specified
     * <code>PolicyFinder</code> to help with instantiating PolicySets.
     *
     * @param DataHandler the DataHandler to load the policy from
     * @param finder a PolicyFinder used to help in instantiating PolicySets
     * @param handler an error handler used to print warnings and errors
     *                during parsing
     *
     * @return a policy associated with the specified DataHandler
     *
     * @throws RegistryException exception thrown if there is a problem reading the DataHandler's input stream
     */
    private static AbstractPolicy loadPolicy(DataHandler dh, PolicyFinder finder) throws RegistryException {
        AbstractPolicy policy = null;

        try {
            // create the factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);

            DocumentBuilder db = null;

            // set the factory to work the way the system requires
            // we're not doing any validation
            factory.setNamespaceAware(false);
            factory.setValidating(false);

            db = factory.newDocumentBuilder();

            // try to load the policy file
            Document doc = db.parse(dh.getInputStream());

            // handle the policy, if it's a known type
            Element root = doc.getDocumentElement();
            String name = root.getTagName();

            if (name.equals("Policy")) {
                policy = Policy.getInstance(root);
            } else if (name.equals("PolicySet")) {
                policy = PolicySet.getInstance(root, finder);
            } else {
                // this isn't a root type that we know how to handle
                throw new RegistryException(ServerResourceBundle.getInstance().getString("message.unknownRootDocumentType", new Object[]{name}));
            }
        } catch (Exception e) {
            log.error(ServerResourceBundle.getInstance().getString("message.FailedToLoadPolicy"), e);
            throw new RegistryException(e);
        }

        return policy;
    }

    /**
     * Returns true if the module supports finding policies based on a
     * request (ie, target matching). By default this method returns false.
     *
     * @return true if request retrieval is supported
     */
    public boolean isRequestSupported() {
        return true;
    }

    /**
     * Returns true if the module supports finding policies based on an
     * id reference (in a PolicySet). By default this method returns false.
     *
     * @return true if idReference retrieval is supported
     */
    public boolean isIdReferenceSupported() {
        return true;
    }

    public void init(PolicyFinder finder) {
        this.finder = finder;
    }
    
    /**
     * Gets the id for the PolicySet object for the objects matching specified resourceId.
     *
     */
    @SuppressWarnings("rawtypes")
	private String getRegistryObjectPolicyId(ServerRequestContext context, String resourceId)
        throws RegistryException {
        String id = null;

        boolean assumeCanonicalObjectsUseDefaultACP = Boolean.valueOf(RegistryProperties.getInstance()
            .getProperty("eric.security.authorization.assumeCanonicalObjectsUseDefaultACP", "true")).booleanValue();
        boolean checkForCustomACP = Boolean.
	    valueOf(RegistryProperties.getInstance().
		    getProperty("eric.security.authorization.customAccessControlPoliciesEnabled",
				"false")).booleanValue();

        if (!checkForCustomACP
	    || (assumeCanonicalObjectsUseDefaultACP &&
		it.cnr.icar.eric.common.Utility.
		isCanonicalObjectId(resourceId))) {
            getDefaultPolicy(context);
            return idForDefaultACP;            
        } else {                    
            try {            
                ResponseOptionType ebResponseOptionType =
                    BindingUtility.getInstance().queryFac.createResponseOptionType();
                ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
                ebResponseOptionType.setReturnComposedObjects(false);

                String query =
                    "SELECT policy.* from ExtrinsicObject policy, Association ass WHERE ass.targetObject = '" +
                    resourceId + "' AND ass.associationType = '" +
                    BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor +
                    "' AND ass.sourceObject = policy.id";

                List objectRefs = new ArrayList();
                List<IdentifiableType> ebIdentifiableTypeResultList = pm.executeSQLQuery(context, query, ebResponseOptionType,
                        "ExtrinsicObject", objectRefs);
                List<String> ids = bu.getIdsFromRegistryObjectTypes(ebIdentifiableTypeResultList);

                int cnt = ids.size();

                if (cnt == 0) {
                    //No policy defined for the RegistryObject
                    //See if a policy is defined for the ObjectType corresponding to this RegistryObject's objectType
                    query =
                        "SELECT policy.* from ExtrinsicObject policy, RegistryObject ro, ClassificationNode ot, Association ass WHERE ass.targetObject = ot.id AND ass.associationType = '" +
                        BindingUtility.CANONICAL_ASSOCIATION_TYPE_ID_AccessControlPolicyFor +
                        "' AND ass.sourceObject = policy.id AND ro.id = '" + resourceId + "' AND ro.objectType = ot.id";

                    objectRefs = new ArrayList();
                    ebIdentifiableTypeResultList = pm.executeSQLQuery(context, query, ebResponseOptionType,
                            "ExtrinsicObject", objectRefs);
                    ids = bu.getIdsFromRegistryObjectTypes(ebIdentifiableTypeResultList);

                    cnt = ids.size();                
                }

                if (cnt > 1) {
                    log.warn(ServerResourceBundle.getInstance().getString("message.MoreThan1AccessControlPolicyWithId", new Object[]{resourceId}));                
                }

                if (cnt >= 1) {
                    id = ids.get(0);
                }

            } catch (RegistryException e) {
                context.rollback();
                throw e;
            } catch (Exception e) {
                context.rollback();
                throw new RegistryException(e);
            }

            context.commit();
        }

        log.trace("RegistryAttributeFinderModule.getRegistryObjectPolicyId: resourceId=" + resourceId + " policyId=" + id);
        return id;
    }
        
    /**
     * Finds a policy based on a request's context. Returns the custom Policy associated with
     * resource if any, otherwise return the registry's default Policy.
     *
     * This will always do a Target match to make sure that the given policy applies.
     *
     *
     * @param context the representation of the request data
     *
     * @return the result of trying to find an applicable policy
     */
    @SuppressWarnings("unused")
	public PolicyFinderResult findPolicy(EvaluationCtx context) {
        
        AbstractPolicy roPolicy = null;
        AttributeValue resourceIdAttr = context.getResourceId();
        String resourceId = resourceIdAttr.encode(); //??Suggest marshal instead of encode
        String policyId = null;
        
        //Need to use an internal ServerRequestContext separate from request with registryOperator
        //to bypass infinite recursion in auth check in qm.getRegistryObject()
        //ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);                

        try {
            policyId = getRegistryObjectPolicyId(requestContext, resourceId);
            roPolicy = loadPolicy(requestContext, policyId);
        } 
        catch (RegistryException e) {
            log.error(e, e);
        }

        if (roPolicy != null) {
            MatchResult match = roPolicy.match(context);
            int result = match.getResult();
            if (result == MatchResult.INDETERMINATE) {
                log.warn(ServerResourceBundle.getInstance().getString("message.INDETERMINATEPolicyMatchForObjectId",
                                                            new Object[]{resourceIdAttr, policyId, match.getStatus().toString()}));
            } 
            else if (result == MatchResult.MATCH) {
                return new PolicyFinderResult(roPolicy);
            }
        }

        // roPolicy had no MATCH so use defaultPolicy
        // see if we match
        
        AbstractPolicy defaulACP = getDefaultPolicy(requestContext);
        if (defaulACP == null) {
            return new PolicyFinderResult();
        }
        
        MatchResult match = defaulACP.match(context);
        int result = match.getResult();
        if (result == MatchResult.INDETERMINATE) {
            // if there was an error, we stop right away
            return new PolicyFinderResult(match.getStatus());
        } 
        else if (result == MatchResult.MATCH) {
            // if we found a policy, return it, otherwise we're N/A
            if (defaulACP != null) {
                return new PolicyFinderResult(defaulACP);
            } 
            else {
                return new PolicyFinderResult();
            }
        } 
        else {
            // Return N/A
            return new PolicyFinderResult();
        }
    }

    /**
     * Tries to find one and only one matching policy given the idReference
     * First it tries to find a RegistryObject with specified URI as id.
     * If none is found, it tries to find an ACP with specified URI as
     * a value for a Slot named "ComposedPolicies".
     *
     *
     * @param idReference an identifier specifying some policy
     * @param type type of reference (policy or policySet) as identified by
     *             the fields in <code>PolicyReference</code>
     *
     * @return the result of looking for a matching policy
     */
    public PolicyFinderResult findPolicy(URI idReference, int type) {
        
        AbstractPolicy policy = null;

        //??XACML: WHy does this call from xacml code not provide EvaluationCtx where we could get ServerRequestContext
        //TODO: Figure out a solution to this 
        ServerRequestContext context = null;
        try {
            context = new ServerRequestContext("RegistryPolicyFinderModule.findPolicy", null);
            
            String id = idReference.toString();
            policy = loadPolicy(context, id);
        } 
        catch (RegistryException e) {
            log.error(e);
        } finally {
            try {
                context.rollback(); 
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        if (policy == null) {
            log.warn(ServerResourceBundle.getInstance().getString("message.findPolicyFailedToFindPolicyWithId", new Object[]{idReference}));
            return new PolicyFinderResult();
        } 
        else {
            return new PolicyFinderResult(policy);
        }
    }

    public AbstractPolicy getDefaultPolicy(ServerRequestContext requestContext) {
        try {
            if (defaultPolicy == null) {
                defaultPolicy = loadDefaultPolicy(requestContext);
            }
        } catch (RegistryException e) {
            log.error(e);
        }
        return defaultPolicy;
    }

}

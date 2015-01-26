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

import com.sun.xacml.ctx.Status;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.persistence.PersistenceManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.cond.EvaluationResult;

/** 
  * logical signature: boolean isAssociatedWith(String sourceObject, String targetObject, [String associationType])
  *
  * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
  */
public class AssociationExistsFunction extends AbstractRegistryFunction {
    
    private static final Log log = LogFactory.getLog(AssociationExistsFunction.class);

    private static BindingUtility bu = BindingUtility.getInstance();
    
    // the name of the function, which will be used publicly
    public static final String NAME = "association-exists";
    
    // the parameter types, in order, and whether or not they're bags
    private static final String[] params = {
        AnyURIAttribute.identifier, //sourceObject id - required
        AnyURIAttribute.identifier, //targetObject id - required
        AnyURIAttribute.identifier  //if of AssociationType node - optional
    };
    
    // Parameter names, for logging purposes
    private static final String[] paramsNames = {
        "sourceObjectId",
        "targetObjectId",
        "assocTypeId"
    };

    // Number of required parameters
    private final int minParams = 2;
    
    //Dont forget to make sure that bagParams.length MUST BE same as params.length
    private static final boolean[] bagParams = { false, false, false };
        
    public AssociationExistsFunction() {
        // use the constructor that handles mixed argument types
        super(AuthorizationServiceImpl.FUNCTION_NS + NAME, 0, params, bagParams, BooleanAttribute.identifier, false);
    }
    
    protected String[] getParameterNames() {
        return paramsNames;
    }

    public EvaluationResult evaluate(@SuppressWarnings("rawtypes") List inputs, EvaluationCtx context) {
        // Evaluate the arguments using the helper method...this will
        // catch any errors, and return values that can be compared
        AttributeValue[] argValues = new AttributeValue[inputs.size()];
        EvaluationResult result = evalArgs(inputs, context, argValues, minParams);
        
        if (result != null) {
            return result;
        }
        
        // cast the resolved values into specific types
        String sourceObjectId = (argValues[0]).encode().trim();
        String targetObjectId = (argValues[1]).encode().trim();
        String assocTypeId = (argValues.length > 2) ? (argValues[2]).encode().trim() : null;
        
        boolean evalResult = false;
                
        try {
            ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
            String assocTypeNodePath = null;
            if (assocTypeId != null) {
                ClassificationNodeType assocTypeNode = (ClassificationNodeType)qm.getRegistryObject(requestContext, assocTypeId);
                assocTypeNodePath = assocTypeNode.getPath();
            }
            
            evalResult = associationExistsInternal(requestContext, sourceObjectId, targetObjectId, assocTypeNodePath);
        } catch (ObjectNotFoundException e) {
            //Do nothing as evalResult = false; already
        	evalResult = false;
        } catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString(
                    "message.xacmlExtFunctionEvalError", new Object[]{getFunctionName()}), e);
            ArrayList<String> codes = new ArrayList<String>();
            codes.add(Status.STATUS_PROCESSING_ERROR);
            return new EvaluationResult(new Status(codes, e.getMessage()));
        }
        
        // boolean returns are common, so there's a getInstance() for that
        return EvaluationResult.getInstance(evalResult);
    }
    
    private boolean associationExistsInternal(ServerRequestContext requestContext, String sourceObject, String targetObject, String assocTypeNodePath) throws RegistryException {
        boolean assExists = false;
        
        String queryStr = "SELECT a.* from Association a, ClassificationNode assocTypeNode WHERE a.sourceObject='" + sourceObject + "' AND a.targetObject='" + targetObject + "' ";
        
        if (assocTypeNodePath != null) {            
            //The a.associationType may be a sub-class of assocTypeNodePath
            //Check exact match: a.associationType = assocTypeNodePath
            //Also check sub-class match: a.associationType LIKE assocTypeNodePath/%
            queryStr += " AND a.associationType = assocTypeNode.id AND (assocTypeNode.path = '" + assocTypeNodePath + "' OR assocTypeNode.path LIKE '" + assocTypeNodePath + "/%')";
        }
        
        ResponseOptionType ebResponseOptionType =
		    bu.queryFac.createResponseOptionType();
		ebResponseOptionType.setReturnType(ReturnType.LEAF_CLASS);
		ebResponseOptionType.setReturnComposedObjects(true);


		List<?> asses = PersistenceManagerFactory.getInstance()
		    .getPersistenceManager().executeSQLQuery(requestContext, queryStr, ebResponseOptionType, "Association", new ArrayList<Object>());        
		if (asses.size() > 0) {
		    assExists = true;
		}
    
        return assExists;
    }
    
}

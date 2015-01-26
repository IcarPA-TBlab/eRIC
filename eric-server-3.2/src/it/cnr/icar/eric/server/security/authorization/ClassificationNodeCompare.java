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

import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.cond.EvaluationResult;

/** Does a match on a ClassificationNode id taking into account the inheritence
  * semantics of ClassificationNode hierarchies.
  *
  * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
  */
public class ClassificationNodeCompare extends AbstractRegistryFunction {
    
    private static final Log log = LogFactory.getLog(ClassificationNodeCompare.class);

    // the name of the function, which will be used publicly
    public static final String NAME = "classification-node-compare";
    
    // the parameter types, in order
    private static final String[] params = {
        StringAttribute.identifier,
        StringAttribute.identifier
    };

    // Parameter names, for logging purposes
    private static final String[] paramsNames = {
        "cnode1Id",
        "cnode2Id"
    };

    // Number of required parameters
    private final int minParams = 2;
    
    // whether or not the parameters are bags
    private static final boolean[] bagParams = { false, false };
    
    public ClassificationNodeCompare() {
        // use the constructor that handles mixed argument types
//        super(NAME, 0, params, bagParams, BooleanAttribute.identifier, false);
        // DBH 1/22/04 - XACML 1.1 no longer supports addTargetFunction(Function, URI)
        // but rather uses addTargetFunction(Function) and gets the URI from 
        // the function's name. Because of this, we now include the namespace
        // in the name, as AuthorizationServiceImpl used to.
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
        String cnode1Id = (argValues[0]).encode().trim();
        String cnode2Id = (argValues[1]).encode().trim();
        
        boolean evalResult = false;
        
        //First see if we have an exact match on cnode id
        if (cnode1Id.equals(cnode2Id)) {
            evalResult = true;
        } else {
            // now see if the ClassificationNode identified by str1 ancestor
            //of ClassificationNode identified by str2
            try {
                ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);

                RegistryObjectType cnode1 = qm.getRegistryObject(requestContext, cnode1Id);
                if (!(cnode1 instanceof ClassificationNodeType)) {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString(
                            "message.ClassificationNodeExpected", new Object[] {cnode1.getClass()}));
                }
                
                RegistryObjectType cnode2 = qm.getRegistryObject(requestContext, cnode2Id);
                if (!(cnode2 instanceof ClassificationNodeType)) {
                    throw new RegistryException(ServerResourceBundle.getInstance().getString(
                            "message.ClassificationNodeExpected", new Object[] {cnode2.getClass()}));
                }
                
                String path1 = ((ClassificationNodeType) cnode1).getPath();
                String path2 = ((ClassificationNodeType) cnode2).getPath();
                
                if (path2.startsWith(path1)) {
                    evalResult = true;
                }
            } catch (RegistryException e) {
                log.error(ServerResourceBundle.getInstance().getString(
                        "message.xacmlExtFunctionEvalError", new Object[]{getFunctionName()}), e);
                ArrayList<String> codes = new ArrayList<String>();
                codes.add(Status.STATUS_PROCESSING_ERROR);
                return new EvaluationResult(new Status(codes, e.getMessage()));                
            }
        }
        
        // boolean returns are common, so there's a getInstance() for that
        return EvaluationResult.getInstance(evalResult);
    }
        
}

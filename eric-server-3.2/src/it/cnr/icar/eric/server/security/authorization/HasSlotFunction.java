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

import it.cnr.icar.eric.common.exceptions.ObjectNotFoundException;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.registry.RegistryException;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.BooleanAttribute;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.cond.EvaluationResult;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;

/** 
  * logical signature: boolean hasSlot(String parentId, String name, [String value])
  *
  * @author Diego Ballve / Digital Artefacts
  * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
  */
public class HasSlotFunction extends AbstractRegistryFunction {
    
    private static final Log log = LogFactory.getLog(HasSlotFunction.class);
    
    // the name of the function, which will be used publicly
    public static final String NAME = "has-slot";
    
    // the parameter types, in order, and whether or not they're bags
    private static final String[] params = {
        AnyURIAttribute.identifier, //parent object id - required
        AnyURIAttribute.identifier, //slot name - required
        StringAttribute.identifier  //slot value - optional
    };
    
    // Parameter names, for logging purposes
    private static final String[] paramsNames = {
        "parentId",
        "name",
        "value"
    };

    // Number of required parameters
    private final int minParams = 2;
    
    //Dont forget to make sure that bagParams.length MUST BE same as params.length
    private static final boolean[] bagParams = { false, false, false };
        
    public HasSlotFunction() {
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
        String parentId = (argValues[0]).encode().trim();
        String name = (argValues[1]).encode().trim();
        String value = (argValues.length > 2) ? (argValues[2]).encode().trim() : null;
        
        boolean evalResult = false;
                
        try {
            ServerRequestContext requestContext = AuthorizationServiceImpl.getRequestContext(context);
            @SuppressWarnings("unused")
			String assocTypeNodePath = null;
            
            evalResult = hasSlotInternal(requestContext, parentId, name, value);
        } catch (ObjectNotFoundException e) {
            //Do nothing as evalResult = false; already
        	evalResult = false;
        } catch (RegistryException e) {
            log.error(ServerResourceBundle.getInstance().getString(
                    "message.xacmlExtFunctionEvalError", new Object[]{getFunctionName()}), e);
            List<String> codes = new ArrayList<String>();
            codes.add(Status.STATUS_PROCESSING_ERROR);
            return new EvaluationResult(new Status(codes, e.getMessage()));
        }
        
        // boolean returns are common, so there's a getInstance() for that
        return EvaluationResult.getInstance(evalResult);
    }
    
    private boolean hasSlotInternal(ServerRequestContext requestContext, String parentId, String name, String value) throws RegistryException {
        boolean result = false;
        
        RegistryObjectType ebRegistryObjectType = QueryManagerFactory.getInstance().getQueryManager()
                .getRegistryObject(requestContext, parentId);
        
        if (ebRegistryObjectType != null) {
            List<SlotType1> slots = (ebRegistryObjectType).getSlot();
            for (Iterator<SlotType1> it = slots.iterator(); it.hasNext(); ) {
                SlotType1 s = it.next();
                if (name.equals(s.getName())) {
                    if (value != null) {
                        List<String> values = s.getValueList().getValue();
                        for (Iterator<String> it2 = values.iterator(); it2.hasNext(); ) {

                            if (value.equals(it2.next())) {
                                // name and value match
                                result = true;
                                break;
                            }
                        }
                    } else {
                        // name match, value not required
                        result = true;
                        break;
                    }
                }
            }
        }
        
        return result;
    }
    
}

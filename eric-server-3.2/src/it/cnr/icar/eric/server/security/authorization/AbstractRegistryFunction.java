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

import com.sun.xacml.EvaluationCtx;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.cond.EvaluationResult;
import com.sun.xacml.cond.FunctionBase;
import com.sun.xacml.ctx.Status;

import it.cnr.icar.eric.common.spi.QueryManager;
import it.cnr.icar.eric.common.spi.QueryManagerFactory;
import it.cnr.icar.eric.server.util.ServerResourceBundle;

import java.util.ArrayList;
import java.util.List;
import javax.xml.registry.RegistryException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for extension functions.
 *
 * @author Diego Ballve / Digital Artefacts
 * @author <a href="mailto:Farrukh.Najmi@Sun.COM">Farrukh S. Najmi</a> 
 */
public abstract class AbstractRegistryFunction extends FunctionBase {
    
    private static final Log log = LogFactory.getLog(AbstractRegistryFunction.class);

    protected QueryManager qm = QueryManagerFactory.getInstance().getQueryManager();
    
    /** Creates a new instance of AbstractRegistryFunction */
    public AbstractRegistryFunction(String functionName, int functionId,
            String[] paramTypes, boolean[] paramIsBag, String returnType,
            boolean returnsBag) {
        super(functionName, functionId, paramTypes, paramIsBag, returnType, returnsBag);
    }
    
    public abstract EvaluationResult evaluate(@SuppressWarnings("rawtypes") List inputs, EvaluationCtx context);
    
    /** Returns an array with parameter logical names, for logging purposes. */
    protected abstract String[] getParameterNames();

    /**
     * Extends 'evalArgs' to check for required parameters.
     *
     * @param params
     * @param context
     * @param args
     * @param minParams Minimal number of parameters in 'params'.
     *
     * @return EvaluationResult if something is wrong. Null otherwise.
     */
    protected EvaluationResult evalArgs(List<?> params, EvaluationCtx context, AttributeValue[] args, int minParams) {
        EvaluationResult result = super.evalArgs(params, context, args);
        
        if (result == null) {
            if (params.size() < minParams) {
                
                // Check which arguments are missing
                StringBuffer sb = new StringBuffer();
                for (int i = params.size(); i < minParams; i++) {
                    sb.append(ServerResourceBundle.getInstance().getString(
                        "message.xacmlExtFunctionParamMissing",
                        new Object[]{getFunctionName(), getParameterNames()[i], String.valueOf(i+1)}));
                    if (i+1 < minParams) {
                        sb.append(' ');
                    }
                }

                // Use an Exception to log
                RegistryException e = new RegistryException(sb.toString());
                log.error(ServerResourceBundle.getInstance().getString(
                        "message.xacmlExtFunctionEvalError", new Object[]{getFunctionName()}), e);
                
                // Return EvaluationResult with missing attribute code
                List<String> codes = new ArrayList<String>();
                codes.add(Status.STATUS_MISSING_ATTRIBUTE);
                result = new EvaluationResult(new Status(codes, e.getMessage()));
            }
        }
        
        return result;
    }
    
}

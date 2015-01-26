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
package it.cnr.icar.eric.client.admin.function;

import it.cnr.icar.eric.client.admin.AbstractAdminFunction;
import it.cnr.icar.eric.client.admin.AdminFunctionContext;
import it.cnr.icar.eric.client.xml.registry.util.JAXRUtility;
import it.cnr.icar.eric.common.Utility;

import java.lang.reflect.Method;

import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.Key;


public class Chown extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context,
    String[] args) throws Exception {
        if (context.getRegistryObjects() == null ||
        context.getRegistryObjects().size() == 0) {
            context.printMessage(format(rb,"noRegistryObjects"));
            return;
        }
        
        if (args == null ||
        args.length != 1) {
            context.printMessage(format(rb,"oneUserId"));
            return;
        }
        
        String userId = args[0];
        String userID = null;
        
        if (userId.matches("^%[0-9]+$")) {
            try {
                int numericUserId =
                Integer.valueOf(userId.substring(1)).intValue();
                userID = context.getUsers()[numericUserId];
            } catch (Exception e) {
                context.printMessage(format(rb,"invalidIdReference"));
                return;
            }
        } else if (Utility.getInstance().isValidURN(userId)) {
            userID = userId;
        } else {
            context.printMessage(format(rb,"invalidIdReference"));
            return;
        }
        
        if (context.getDebug()) {
            context.printMessage(context.getService().getLCM().getClass().toString());
        }
        javax.xml.registry.BusinessLifeCycleManager lcm =
        context.getService().getLCM();
        Method m = null;
        try {
            m = lcm.getClass().getMethod("changeObjectsOwner",
            new Class[] {Collection.class,
            String.class});
        } catch (Exception e) {
            context.printMessage(format(rb, "changeObjectOwner"));
            return;
        }
        
        
        Collection<Key> keys = JAXRUtility.getKeysFromObjects(context.getRegistryObjects());
        
		BulkResponse response =
            (BulkResponse) m.invoke(lcm,
									new Object[] {keys, userID});
		JAXRUtility.checkBulkResponse(response);
    }
    
    public String getUsage() {
        return format(rb, "usage.chown");
    }
}

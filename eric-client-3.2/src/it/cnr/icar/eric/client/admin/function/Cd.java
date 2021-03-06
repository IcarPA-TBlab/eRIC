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
import it.cnr.icar.eric.common.Utility;


import javax.xml.registry.infomodel.RegistryPackage;


public class Cd extends AbstractAdminFunction {
    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        if (args == null) {
            context.printMessage(format(rb,"argumentRequired"));

            return;
        }

        String useArgs = args;

        String[] useArgsArray = useArgs.split("(?<=^|[^\\\\])\"", -1);

        useArgs = useArgsArray[0];

        if (context.getDebug()) {
            context.printMessage(args);

            for (int i = 0; i < useArgsArray.length; i++) {
                context.printMessage(useArgsArray[i] + ":");
            }

            context.printMessage();
        }

        if ((useArgsArray.length > 1)) {
            // An even number of quotes results in an odd-number array length
            if ((useArgsArray.length % 2) == 0) {
                context.printMessage(format(rb, "unbalancedQuotes"));

                return;
            }

            for (int i = 1; i < useArgsArray.length; i += 2) {
                useArgsArray[i] = useArgsArray[i].replaceAll(" ", "\\\\ ");
            }

            for (int i = 1; i < useArgsArray.length; i++) {
                useArgs += useArgsArray[i];
            }
        }

        useArgs = useArgs.replaceAll("\\\\\"", "\"");
        useArgs = useArgs.replaceAll("\\\\ ", " ");

        if (context.getDebug()) {
            context.printMessage(":" + useArgs + ":");
        }

        RegistryPackage newRP;

        if (Utility.getInstance().isValidURN(useArgs)) {
            newRP = context.getService().getRegistryPackageByID(useArgs);
        } else {
            newRP = context.getService().getRegistryPackage(useArgs);
        }

        context.setCurrentRP(newRP);
    }

    public String getUsage() {
        return format(rb, "usage.cd");
    }
}

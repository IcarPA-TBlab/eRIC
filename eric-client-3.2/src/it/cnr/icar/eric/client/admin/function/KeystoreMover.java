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
import it.cnr.icar.eric.common.CommonResourceBundle;

import java.util.HashMap;


public class KeystoreMover extends AbstractAdminFunction {
    @SuppressWarnings("unused")
	private HashMap<Object, Object> attachments = new HashMap<Object, Object>();
    @SuppressWarnings("unused")
	private String request;
    
    public void execute(AdminFunctionContext context, String args)
    throws Exception {
        it.cnr.icar.eric.common.security.KeystoreMover ksm = new it.cnr.icar.eric.common.security.KeystoreMover();
        
        String sourceKeystoreType = "PKCS12";
        String sourceKeystorePath = null;
        String sourceKeystorePassword = null;
        String sourceAlias = null;
        String sourceKeyPassword = null;
        
        String destinationKeystoreType = "JKS";
        String destinationKeystorePath = null;
        String destinationKeystorePassword = null;
        String destinationAlias = null;
        String destinationKeyPassword = null;
        
	if (args != null) {
	    String[] tokens = args.split("\\s+");

	    int tIndex = 0;

	    for (tIndex = 0;
		 ((tIndex < tokens.length) && (tokens[tIndex].charAt(0) == '-'));
		 tIndex++) {
		String option = tokens[tIndex];

		if (option.equalsIgnoreCase("-sourceKeystoreType")) {
		    sourceKeystoreType = tokens[++tIndex];
		} else if (option.startsWith("-sourceKeystorePath")) {
		    sourceKeystorePath = tokens[++tIndex];
		} else if (option.startsWith("-sourceKeystorePassword")) {
		    sourceKeystorePassword = tokens[++tIndex];
		} else if (option.startsWith("-sourceAlias")) {
		    sourceAlias = tokens[++tIndex];
		} else if (option.startsWith("-sourceKeyPassword")) {
		    sourceKeyPassword = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeystoreType")) {
		    destinationKeystoreType = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeystorePath")) {
		    destinationKeystorePath = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeystorePassword")) {
		    destinationKeystorePassword = tokens[++tIndex];
		} else if (option.startsWith("-destinationAlias")) {
		    destinationAlias = tokens[++tIndex];
		} else if (option.startsWith("-destinationKeyPassword")) {
		    destinationKeyPassword = tokens[++tIndex];
		} else {
		    context.printMessage(format(rb,"invalidArgument",
						new Object[] { option }));
		    return;
		}
	    }
        
	    if (sourceKeystorePath == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingSourceKeystorePath"));
		context.printMessage(getUsage());
	    }
	    if (sourceKeystorePassword == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingSourceKeystorePassword"));
		context.printMessage(getUsage());
	    }
	    if (destinationKeystorePath == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingDestinationKeystorePath"));
		context.printMessage(getUsage());
	    }
	    if (destinationKeystorePassword == null) {
		context.printMessage(CommonResourceBundle.getInstance().getString("message.ErrorMissingDestinationKeystorePassword"));
		context.printMessage(getUsage());
	    }
        
	    ksm.move(sourceKeystoreType, sourceKeystorePath, sourceKeystorePassword, sourceAlias, sourceKeyPassword, 
		     destinationKeystoreType, destinationKeystorePath, destinationKeystorePassword, destinationAlias, destinationKeyPassword);
        } else {
            context.printMessage(format(rb,"argumentRequired"));
            
            return;
        }
    }
    
    public String getUsage() {
        return format(rb, "usage.keystoreMover");
    }

    public void help(AdminFunctionContext context,
		     String args) throws Exception {
	context.printMessage(getUsage());
	context.printMessage();
	context.printMessage(format(rb, "help.keystoreMover"));
    }
}

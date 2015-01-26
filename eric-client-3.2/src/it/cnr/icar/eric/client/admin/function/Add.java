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
import it.cnr.icar.eric.client.admin.AdminFunction;
import it.cnr.icar.eric.client.admin.AdminFunctionContext;
import it.cnr.icar.eric.client.admin.AdminShell;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Enumeration;
import java.util.ResourceBundle;


public class Add extends AbstractAdminFunction {
	@SuppressWarnings("rawtypes")
	static Class[] execute3ParameterTypes =
		new Class[] {AdminFunctionContext.class,
					 String.class};

	private static ResourceBundle addFunctions =
		ResourceBundle.getBundle(AdminShell.ADMIN_SHELL_RESOURCE_BASE + ".AddFunctions");

    public void execute(AdminFunctionContext context,
						String args) throws Exception {
		if (args == null) {
			context.printMessage(format(rb,"argumentRequired"));
			return;
		}

		String[] tokens = args.split("\\s+", 2);

		if (tokens == null) {
			context.printMessage(format(rb,"argumentRequired"));
			return;
		}

		String addFunctionClassName;
		try {
			addFunctionClassName = addFunctions.getString(tokens[0].toLowerCase());
		} catch (java.util.MissingResourceException e) {
			context.printMessage(format(rb,
						    "unrecognizedObjectType",
						    new Object[] {tokens[0]}));
			return;
		}

		doAddFunction(context,
					  addFunctionClassName,
					  tokens.length > 1 ? tokens[1] : null);
	}
    
	void doAddFunction(AdminFunctionContext currContext,
					   String functionClassName,
					   String args) throws Exception {
		Class<?> functionClass =
					Class.forName(functionClassName);

		Constructor<?> constructor = functionClass.getConstructor((java.lang.Class[])null);

		AdminFunction function = (AdminFunction) constructor.newInstance((java.lang.Object[])null);

		@SuppressWarnings("unused")
		Method execute3 = null;
		try {
			execute3 =
				functionClass.getDeclaredMethod("execute",
												execute3ParameterTypes);
		} catch (Exception e3) {
			//e3.printStackTrace();
			context.printMessage(format(rb, "cannotExecute",
						    new Object[] {
							    functionClassName
						    }));
		}
		function.execute(currContext, args);
	}

    public String getUsage() {
        return format(rb, "usage.add");
    }

	public void help(AdminFunctionContext context,
					 String args) throws Exception {
		if (args == null) {
			Enumeration<String> functionNames = addFunctions.getKeys();

			while (functionNames.hasMoreElements()) {
				String functionName = functionNames.nextElement();
				String functionClassName = addFunctions.getString(functionName);
				try {
					Class<?> functionClass = Class.forName(functionClassName);
					Constructor<?> constructor = functionClass.getConstructor((java.lang.Class[])null);
					AdminFunction function =
						(AdminFunction) constructor.newInstance((java.lang.Object[])null);
					context.printMessage(function.getUsage());
				} catch (Exception e) {
					e.printStackTrace();
					context.printMessage(
						format(rb, "noUsage",
						       new Object[] {
							       functionName}));
				}
			}
		} else {
			String[] tokens = args.split("\\s+", 2);

			try {
				String addFunctionClassName =
					addFunctions.getString(tokens[0].toLowerCase());

				try {
					Class<?> functionClass =
						Class.forName(addFunctionClassName);

					Constructor<?> constructor = functionClass.getConstructor((java.lang.Class[])null);
					AdminFunction function =
						(AdminFunction) constructor.newInstance((java.lang.Object[])null);

					function.help(context,
								  tokens.length > 1 ? tokens[1] : null);
				} catch (Exception e) {
					e.printStackTrace();
					context.printMessage(
						format(rb, "noHelp",
						       new Object[] {
							       tokens[0]}));
				}
			} catch (java.util.MissingResourceException e) {
				context.printMessage(
					format(rb, "unrecognizedFunction",
					       new Object[] {tokens[0]}));
			}
		}
	}
}

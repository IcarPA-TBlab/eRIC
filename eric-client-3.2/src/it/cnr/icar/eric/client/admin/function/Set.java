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

import java.text.Collator;

public class Set extends AbstractAdminFunction {
    private static final Collator collator;
    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

	public void execute(AdminFunctionContext context,
						String args) throws Exception {
		if (args == null) {
			context.printMessage(format(rb,"argumentRequired"));
			return;
		}

		String[] argsArray = args.split("\\s+", 2);

		if (collator.compare(argsArray[0], "debug") == 0) {
			if ((collator.compare(argsArray[1], "true") == 0) ||
				(collator.compare(argsArray[1], "on") == 0) ||
				(collator.compare(argsArray[1], "yes") == 0)) {
				context.setDebug(true);
				return;
			} else if ((collator.compare(argsArray[1], "false") == 0) ||
				(collator.compare(argsArray[1], "off") == 0) ||
				(collator.compare(argsArray[1], "no") == 0)) {
				context.setDebug(false);
				return;
			}
		} else if (collator.compare(argsArray[0], "editor") == 0) {
			context.setEditor(argsArray[1]);
			return;
		} else if (collator.compare(argsArray[0], "verbose") == 0) {
			if ((collator.compare(argsArray[1], "true") == 0) ||
				(collator.compare(argsArray[1], "on") == 0) ||
				(collator.compare(argsArray[1], "yes") == 0)) {
				context.setVerbose(true);
				return;
			} else if ((collator.compare(argsArray[1], "false") == 0) ||
				(collator.compare(argsArray[1], "off") == 0) ||
				(collator.compare(argsArray[1], "no") == 0)) {
				context.setVerbose(false);
				return;
			}
		}

		// If got to here, there was a problem.
		context.printMessage(format(rb,"invalidArgument",
					    new Object[] {args}));
	}

	public String getUsage() {
		return format(rb,"usage.set");
	}
}

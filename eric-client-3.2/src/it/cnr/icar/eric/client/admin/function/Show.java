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

import java.util.Locale;


public class Show extends AbstractAdminFunction {
    private static final Collator collator;

    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        if (context.getOutStream() == null) {
            throw new NullPointerException();
        }

        if (args == null) {
            showDebug(context);
            showEditor(context);
            showLastException(context);
            showLocalDir(context);
            showLocale(context);
            showVerbose(context);
        } else if (collator.compare(args, "debug") == 0) {
            showDebug(context);
        } else if (collator.compare(args, "editor") == 0) {
            showEditor(context);
        } else if (collator.compare(args, "exception") == 0) {
            showLastException(context);
        } else if (collator.compare(args, "localdir") == 0) {
            showLocalDir(context);
        } else if (collator.compare(args, "locale") == 0) {
            showLocale(context);
        } else if (collator.compare(args, "verbose") == 0) {
            showVerbose(context);
        } else {
            context.printMessage(format(rb,"invalidArgument",
					new Object[] { args }));
        }
    }

    private void showDebug(AdminFunctionContext context) {
        context.printMessage(format(rb,"debugStatus",
				    new Object[] {
					    new Boolean(context.getDebug())
				    }));
    }

    private void showEditor(AdminFunctionContext context) {
        context.printMessage(format(rb,"editorStatus",
				    new Object[] { context.getEditor() }));
    }

    private void showLastException(AdminFunctionContext context) {
        String lastException = context.getLastException();

        if (lastException != null) {
            context.printMessage(format(rb,"lastException"));
            context.printMessage(lastException);
        } else {
            context.printMessage(format(rb,"noLastException"));
        }
    }

    private void showLocalDir(AdminFunctionContext context) {
        context.printMessage(format(rb,"localDir",
				    new Object[] {
					    context.getLocalDir()
				    }));
    }

    private void showLocale(AdminFunctionContext context) {
        context.printMessage(format(rb,"localeStatus",
				    new Object[] {
					    Locale.getDefault().getDisplayName(),
					    Locale.getDefault()
				    }));
    }

    private void showVerbose(AdminFunctionContext context) {
        context.printMessage(format(rb,"verboseStatus",
				    new Object[] {
					    new Boolean(context.getVerbose())
				    }));
    }

    public String getUsage() {
        return "show [debug | editor | exception | localdir | locale | verbose]?";
    }
}

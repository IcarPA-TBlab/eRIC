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
package it.cnr.icar.eric.client.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class AdminShellUtility {
    private static final Log log = LogFactory.getLog(AdminShellUtility.class.getName());
    private static AdminShellUtility instance; //singleton instance

    protected AdminResourceBundle rb = AdminResourceBundle.getInstance();

    /** Creates a new instance of AdminShellFactory */
    protected AdminShellUtility() {
    }

    public synchronized static AdminShellUtility getInstance() {
        if (instance == null) {
            instance = new AdminShellUtility();
        }

        return instance;
    }

    public String normalizeArgs(String args) {
	/*
	 * Pattern is choice of:
	 *  - '"' (not preceded by '\') followed zero or more of:
	 *     - Not a '"' or
	 *     - '\"'
	 *  - '"' that is preceded by '\'
	 *  - Not a '"'
	 */
	Pattern p =
	    Pattern.compile("(?<!\\\\)\"(([^\"]|\\\\\")*)\"|(?<=\\\\)\"|[^\"]+");

	Matcher m = p.matcher(args);

	String matcherUseArgs = "";

	int prevEnd = 0;
	while (m.find()) {
	    prevEnd = m.end();

	    log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				   "debug.group",
				   new Object[] {m.group()}));
	    String quotedString = m.group(1);

	    if (quotedString != null) {
		matcherUseArgs += quotedString.replaceAll(" ", "\\\\ ");
		log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				       "debug.quoted",
				       new Object[] {quotedString.
						     replaceAll(" ", "\\\\ ")}));
	    } else {
		matcherUseArgs += m.group();
		log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				       "debug.unquoted",
				       new Object[] {m.group()}));
	    }
	}

	if (prevEnd < args.length()) {
	    String remainder = args.substring(prevEnd);
	    log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				   "debug.remainder",
				   new Object[] {remainder}));

	    if (remainder.matches(".*(?<!\\\\)\".*")) {
		log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                                       "unbalancedQuotes"));
	    } else {
		matcherUseArgs += remainder;
	    }
	}

	log.debug(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			       "debug.matcher",
			       new Object[] {matcherUseArgs}));

	String useArgs = args;

	String[] useArgsArray = useArgs.split("(?<=^|[^\\\\])\"", -1);

	useArgs = useArgsArray[0];

	if (log.isDebugEnabled()) {
	    log.debug(args);

	    for (int i = 0; i < useArgsArray.length; i++) {
		log.debug(useArgsArray[i] + ":");
	    }
	    log.debug("");
	}

	if ((useArgsArray.length > 1)) {
	    // An even number of quotes results in an odd-number array length
	    if (useArgsArray.length % 2 == 0) {
		log.error(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                                       "unbalancedQuotes"));
		return null;
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

	if (log.isDebugEnabled()) {
	    log.debug(":" + useArgs + ":");
	}

	return useArgs;
    }
}

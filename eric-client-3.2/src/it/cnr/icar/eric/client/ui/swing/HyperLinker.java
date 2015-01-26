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
package it.cnr.icar.eric.client.ui.swing;



import it.cnr.icar.eric.client.xml.registry.util.ProviderProperties;

import java.io.IOException;

/*
 * Based upon code from:
 * http://forum.java.sun.com/thread.jsp?thread=328882&forum=57&message=1337973
 *
 * Class is used to open a browser and show a defined URL/FILE
 * if not running in an Applet. (Running
 * in an Applet this will not work, use myApplet.getAppletContext
 * ().showDocument(URL);
 * If a browser window is opened, it will be reused to show the
 * document (no new one will be opened)
 */
public class HyperLinker {
    private static JavaUIResourceBundle rb =
	JavaUIResourceBundle.getInstance();
    private static ProviderProperties props = ProviderProperties.getInstance();

    // Used to identify the windows platform.
    private static final String WIN_ID = "windows";

    // The default system browser under windows.
    private static final String DEF_WIN_COMMAND =
	"rundll32 url.dll,FileProtocolHandler $url";

    // The default browser under unix.
    private static final String UNIX_PATH = "firefox ";
    private static final String DEF_UNIX_COMMAND =
	UNIX_PATH + "-remote openURL($url)";

    /**
     * Display a file in the system browser. If you want to
     * display a
     * file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with
     * either "http://" or * "file://").
     * @return is true, if displaying was possible (at leas no
     * error occures)
     */
    public static boolean displayURL(String url) {
        boolean result = true;
        boolean windows = isWindowsPlatform();
        String cmd =
	    props.getProperty("jaxr-ebxml.registryBrowser.webBrowser.launch");

        try {
            if (cmd == null) {
                // Set cmd from hardwired platform specific defaults
                if (windows) {
                    cmd = DEF_WIN_COMMAND;
                } else {
                    cmd = DEF_UNIX_COMMAND;
                }
            }

	    //Replace url parameter with actual URL
	    int index = cmd.indexOf("$url");
	    if (index != -1) {
		cmd = cmd.substring(0, index) + url + cmd.substring(index + 4);
	    }

	    Process p = Runtime.getRuntime().exec(cmd);
            if (!windows) {
                // Under Unix, Netscape/Mozilla has to be running for the "-
                //remote"
                // command to work. So, we try sending the command and
                // check for an exit value. If the exit command is 0,
                // it worked, otherwise we need to start the browser.
                // cmd = 'netscape -remote openURL
                //(http://www.javaworld.com)'
                try {
                    // wait for exit code -- if it's 0, command worked,
                    // otherwise we need to start the browser up.
                    int exitCode = p.waitFor();

                    if (exitCode != 0) {
                        // Command failed, start up the browser
                        // cmd2 = 'firefox http://www.javaworld.com'
			// ??? Should probably add property for fallback cmd
                        String cmd2 = UNIX_PATH + url;
			RegistryBrowser.displayInfo(rb.
			        getString("message.info.failedToLaunchBrowser",
					  new Object []{cmd, cmd2}));

                        p = Runtime.getRuntime().exec(cmd2);
                    }
                } catch (InterruptedException x) {
                    result = false;
		    RegistryBrowser.displayError(rb.
			       getString("message.error.failedToLaunchBrowser",
					 new Object []{cmd}), x);
                }
            }
        } catch (IOException ex) {
            // couldn't exec browser
            result = false;
            RegistryBrowser.displayError(rb.
			       getString("message.error.failedToLaunchBrowser",
					 new Object []{cmd}), ex);
        }

        return result;
    }

    /**
     * Try to determine whether this application is running under
     * Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows
     * OS
     */
    public static boolean isWindowsPlatform() {
        String os = props.getProperty("os.name");

        return ((os != null) && os.toLowerCase().startsWith(WIN_ID));
        /*
        if ((os != null) && os.toLowerCase().startsWith(WIN_ID)) {
            return true;
        } else {
            return false;
        }
        */
    }

    /**
     * Simple example. Opens the url to "http://ebxmlrr.sourceforge.net"
     */
    public static void main(String[] args) {
        displayURL("http://ebxmlrr.sourceforge.net");
    }
}

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
package it.cnr.icar.eric.server.container;

import java.util.ResourceBundle;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Class Declaration for TomcatListener.
 *
 * @author Doug Bunting, Sun Microsystems
 * @see 
 */
public class TomcatListener extends GenericListener
    implements org.apache.catalina.LifecycleListener {
    // execution arm
    private Helper helper;
    private boolean helperCreateAttempted = false;

    // did we successfully start a database server?
    private boolean startedServer = false;

    // for logging
    private static Log log = LogFactory.getLog(TomcatListener.class);
    private static String rbName =
	TomcatListener.class.getPackage().getName() + ".ResourceBundle";
    private static ResourceBundle rb = ResourceBundle.getBundle(rbName);

    /**
     * class constructor
     */
    public TomcatListener() {
	super();
    }

    /**
     * Main entry point for this class.  Called whenever an application or
     * container life cycle event occurs.
     * <p>Choice between application and container events is based on how
     * the controlling Tomcat {@code <Listener/>} element is placed.  If
     * {@code <Listener/>} appears in server.xml file (normally directly
     * beneath the {@code <Server>} element), container life cycle events
     * are tracked.  Otherwise (if it is within a {@code <Context>}
     * element), application life cycle events are tracked.</p>
     *
     * @see org.apache.catalina.LifecycleListener#lifecycleEvent(LifecycleEvent)
     */
    public void lifecycleEvent(LifecycleEvent event) {
	String dataStr = "<null>";
	String lifecycleStr = "<null>";

	if (null == helper) {
	    if (!createHelper()) {
		// avoid NPEs later
		return;
	    }
	}

	if (log.isDebugEnabled()) {
	    Object data = event.getData();
	    if (null != data) {
		dataStr = data.toString();
	    }

	    Lifecycle lifecycle = event.getLifecycle();
	    if (null != lifecycle) {
		lifecycleStr = lifecycle.toString();
	    }
	}

	String type = event.getType();
	if (type.equals(Lifecycle.START_EVENT)) {
	    log.debug("start: getData() returned " + dataStr);
	    log.debug("start: getLifecycle() returned " + lifecycleStr);

	    helper.initialize(System.getProperty("catalina.base") +
			      System.getProperty("file.separator") + "conf",
			      this);
	    if (startupServer) {
		startedServer = helper.startupServer();
	    }
	} else if (type.equals(Lifecycle.STOP_EVENT)) {
	    log.debug("stop: getData() returned " + dataStr);
	    log.debug("stop: getLifecycle() returned " + lifecycleStr);

	    if (shutdownDatabase) {
		helper.shutdownDatabase();
	    }
	    if (startedServer && shutdownServer) {
		helper.shutdownServer();
	    }
	}
    }

    private synchronized boolean createHelper() {
	boolean retVal = (null != helper);

	// are we done already or have we tried (and failed) before?
	if (!helperCreateAttempted) {
	    helperCreateAttempted = true;

	    // attempt creation of specified Helper class
	    if (null == helperClass || 0 == helperClass.length()) {
		log.error(rb.getString("message.incorrectHelperConfig"));
	    } else {
		try {
		    helper = (Helper)Class.forName(helperClass).
			newInstance();
		    retVal = true;
		} catch (ClassNotFoundException e) {
		    // this likely results from a configuration error:
		    // unable to instantiate the helper class at all; make
		    // it clear reconfiguration will improve the situation
		    // ??? Add message for this case
		    log.warn(e);
		} catch (NoClassDefFoundError e) {
		    // if using the default configuration, this is the
		    // expected exception when derbynet.jar is not in the
		    // classpath; fallback also part of the default
		    // configuration
		    // ??? Add message for this case
		    log.warn(e);
		} catch (Throwable t) {
		    // as in both the above cases, fallback should work
		    // ??? Add message for this case
		    log.warn(t);
		}
	    }

	    if (!retVal) {
		// attempt creation of specified fallback Helper class
		if (null == helperFallbackClass ||
		    0 == helperFallbackClass.length()) {
		    log.error(rb.getString("message.noHelperFallback"));
		} else {
		    try {
			helper = (Helper)Class.forName(helperFallbackClass).
			    newInstance();
			retVal = true;
		    } catch (ClassNotFoundException e) {
			// this likely results from a configuration error:
			// unable to instantiate the fallback helper class
			// ??? Add message for this case
			log.error(e);
		    } catch (NoClassDefFoundError e) {
			// a bit bizarre: configured fallback class is not
			// generic and depends on something not in our
			// environment
			// ??? Add message for this case
			log.error(e);
		    } catch (Throwable t) {
			// ??? Add message for this case
			log.error(t);
		    }
		}
	    }
	}

	return retVal;
    }
}

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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
//import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a variant of 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on and examples of using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 * http://www.javaworld.com/javaworld/jw-06-2003/jw-0606-swingworker.html
 *
 * This variant of works in conjunction with the GlassPane class.
 * While SwingWorker allows users to execute time-consuming task on a separate
 * thread, keeping the Swing Thread responsive and free for repaints, the
 * GlassPane addition will intercept mouse/keyboard events to the top level
 * component and prevent users from triggering more events. Also, it will set
 * cursors to "WAIT" mode and back to normal.
 *
 * Modifications for freebXML Registry include:
 * - finished() always executed after construct, with invokeLater
 * - RegistryBrowser ContextClassLoader set to worker
 * = Shared Object 'value' re-added, with doNonUILogic constructing and returning it
 * - if a GlassPane is activated n times (by different SwingWorkers for components
 *   with the same topLevelAncestor, for instance), it has to be deactivated n 
 *   times to be really deactivated (i.e, GlassPane set to invisible).
 *
 * Usage pattern:
 *
 *  ...
 *  public void actionPerformed(ActionEvent e) {
 *      ...
 *      final SwingWorker worker = new SwingWorker(this) {
 *          public Object doNonUILogic() {
 *              //...code that might take a while to execute is here...
 *              return something;
 *          }
 *          public void doUIUpdateLogic() {
 *              //...code that touches the GUI goes here - execute by Swing Thread
 *              // if needed, get object returned from doNonUILogic()
 *              //Object something = get();
 *          }
 *      };
 *      worker.start();
 *      // if needed, wait for doNonUILogic to complete/get object returned
 *      //Object something = get();
 *      ...
 *  }
 *
 * @author Yexin Chen
 * @author Diego Ballve / Digital Artefacts
 */
public abstract class SwingWorker {
    private static final Log log = LogFactory.getLog(SwingWorker.class);
    
    /**
     * Class to maintain reference to current worker thread
     * under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;
        ThreadVar(Thread t) {
            thread = t;
        }
        synchronized Thread get() {
            return thread;
        }
        synchronized void clear() {
            thread = null;
        }
    }
    
    private Object value;  // see getValue(), setValue(), get()
    @SuppressWarnings("unused")
	private Thread thread;    
    private ThreadVar threadVar;
    private GlassPane glassPane;
    private Component aComponent;
    
    private static Map<GlassPane, Integer> countPerGlassPane = new HashMap<GlassPane, Integer>();

    /**
     * Creates a SwingWorker that can be later started with <code>start</code>
     * method. <code>Component<c/ode> 'aComponent's top level ancestor will
     * receive a <code>GlassPane</code> that will intercept user events to that
     * ancestor until no GlassPane is set to that ancestor. Null 'aComponent' is
     * accepted and will not use GlassPane.
     *
     * @param aComponent Component whose top level ancestor will receive a GlassPane
     */
    public SwingWorker(Component aComponent) {
        setAComponent(aComponent);
        
        final Runnable doFinished = new Runnable() {
            public void run() {
                finished();
            }
        };
        
        Runnable doConstruct = new Runnable() {
            public void run() {
                try {
                    // Modification to SwingWorker, to use RB context
                    Thread.currentThread().setContextClassLoader(
                        RegistryBrowser.getInstance().classLoader);
                    construct();
                } finally {
                    threadVar.clear();

                    // Execute the doFinished runnable on the Swing dispatcher thread
                    SwingUtilities.invokeLater(doFinished);
                }                
            }
        };
        
        // Group the new worker thread in the same group as the "spawner" thread
        Thread t = new Thread(Thread.currentThread().getThreadGroup(), doConstruct);
        threadVar = new ThreadVar(t);
    }

    /**
     * Activate the capabilities of glasspane
     */
    private void activateGlassPane() {
        Component aComp = getAComponent();
        GlassPane aPane = activateGlassPaneSynch(aComp);
        
        // keep track of the glasspane as an instance variable
        setGlassPane(aPane);
    }

    /**
     * Activate the capabilities of glasspane, only if this is the 1st running 
     * call for 'aComp'
     *
     * @return GlassPane for aComponent's top level component.
     */
    private static synchronized GlassPane activateGlassPaneSynch(Component aComp) {
        // Mount the glasspane on the component window
        GlassPane aPane = GlassPane.mount(aComp, true);
        if (aPane != null) {
            Integer iCount = countPerGlassPane.get(aPane);
            if (iCount == null) {
                // no GlassPane active for it, create new, cache it and increase count
                countPerGlassPane.put(aPane, new Integer(1));
                
                // Start interception UI interactions
                aPane.setVisible(true);
            } else {
                // GlassPane already exist, just increase count
                int newCount = iCount.intValue() + 1;
                countPerGlassPane.put(aPane, new Integer(newCount));
            }

        }
        return aPane;
    }
    
    /**
     * Enable the glass pane (to disable unwanted UI manipulation), then spawn the non-UI logic on a separate thread.
     */
    private void construct() {
        activateGlassPane();
        try {
            setValue(doNonUILogic());
        } catch (RuntimeException e) {
	    // ??? why not use e.toString() instead?
            log.error("SwingWorker error: " + e.getClass().getName() + ": " +
		      e.getMessage(), e);
        }
    }

    /**
     * Deactivate the glasspane
     *
     */
    private void deactivateGlassPane() {
        deactivateGlassPaneSynch(getGlassPane());
    }

    /**
     * Activate the capabilities of glasspane, only if this is the 1st running 
     * call for 'aComp'
     *
     * @return GlassPane for aComponent's top level component.
     */
    private static synchronized void deactivateGlassPaneSynch(GlassPane aPane) {
        if (aPane != null) {
            Integer iCount = countPerGlassPane.get(aPane);
            if (iCount == null) {
                // no GlassPane active for it, error?, return
                return;
            } else {
                int count = iCount.intValue();
                if (count == 1) {
                    // only one activateGlassPane call for aPane, reset count
                    countPerGlassPane.remove(aPane);
                    // Stop UI interception
                    aPane.setVisible(false);
                } else {
                    // More than one call, just decrease count
                    int newCount = count - 1;
                    countPerGlassPane.put(aPane, new Integer(newCount));
                }
            }
        }
    }    

    /**
     * This method will be implemented by the inner class of SwingWorker
     * It should only consist of the logic that's unrelated to UI
     *
     * @throws java.lang.RuntimeException thrown if there are any errors in the non-ui logic
     */
    protected abstract Object doNonUILogic() throws RuntimeException;

    /**
     * This method will be implemented by the inner class of SwingWorker
     * It should only consist of the logic that's related to UI updating, after
     * the doNonUILogic() method is done.
     *
     * @throws java.lang.RuntimeException thrown if there are any problems executing the ui update logic
     */
    protected abstract void doUIUpdateLogic() throws RuntimeException;
    
    /**
     * Called on the event dispatching thread (not on the worker thread)
     * after the <code>construct</code> method has returned.
     */
    protected void finished() {
        try {
            deactivateGlassPane();
            doUIUpdateLogic();
        } catch (RuntimeException e) {
	    // ??? why not use e.toString() instead?
            log.error("SwingWorker error: " + e.getClass().getName() + ": " +
		      e.getMessage(), e);
        } finally {
            // Allow original component to get the focus
            if (getAComponent() != null) {
                getAComponent().requestFocus();
            }
        }
    }

    /**
     * Return the value created by the <code>construc/doNonUILogic</code> method.  
     * Returns null if either the constructing thread or the current
     * thread was interrupted before a value was produced.
     * 
     * @return the value created by the <code>construct</code> method
     */
    public Object get() {
        while (true) {  
            Thread t = threadVar.get();
            if (t == null) {
                return getValue();
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // propagate
                return null;
            }
        }
    }

    /**
     * Getter method
     *
     * @return java.awt.Component
     */
    protected Component getAComponent() {
        return aComponent;
    }

    /**
     * Getter method
     *
     * @return GlassPane
     */
    protected GlassPane getGlassPane() {
        return glassPane;
    }

    /** 
     * Get the value produced by the worker thread, or null if it 
     * hasn't been constructed yet.
     */
    protected synchronized Object getValue() { 
        return value; 
    }
    
    /**
     * A new method that interrupts the worker thread.  Call this method
     * to force the worker to stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();
        if (t != null) {
            t.interrupt();
        }
        threadVar.clear();
    }
    
    /**
     * Setter method
     *
     * @param newAComponent java.awt.Component
     */
    protected void setAComponent(Component newAComponent) {
        aComponent = newAComponent;
    }

    /**
     * Setter method
     *
     * @param newGlassPane GlassPane
     */
    protected void setGlassPane(GlassPane newGlassPane) {
        glassPane = newGlassPane;
    }
    
    /** 
     * Set the value produced by worker thread 
     */
    private synchronized void setValue(Object x) { 
        value = x; 
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();
        if (t != null) {
            t.start();
        }
    }
}

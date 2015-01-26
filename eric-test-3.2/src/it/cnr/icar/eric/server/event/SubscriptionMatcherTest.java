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

package it.cnr.icar.eric.server.event;

import it.cnr.icar.eric.common.BindingUtility;
import it.cnr.icar.eric.server.common.ServerRequestContext;
import it.cnr.icar.eric.server.common.ServerTest;
import it.cnr.icar.eric.server.security.authentication.AuthenticationServiceImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.SubscriptionType;

/**
 * @author Diego Ballve / Digital Artefacts
 */
public class SubscriptionMatcherTest extends ServerTest {

    public SubscriptionMatcherTest(String name) {
        super(name);
    }
        
    public static Test suite() {
        return new TestSuite(SubscriptionMatcherTest.class);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }

    /*
     * Gets the List of Subscriptions that definitely match the specified event.
     */
    public void testGetMatchedSubscriptionsMap() throws Exception {
        final String CONTEXT_ID = "SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:";
        
        final String pkg1Id = "urn:org:freebxml:eric:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:pkg1";
        final String pkg2Id = "urn:org:freebxml:eric:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:pkg2";
        final String subscripId = "urn:org:freebxml:eric:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:subscription";
        final String selQueryId = "urn:org:freebxml:eric:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:selectorQuery";

        // final clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg1Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg2Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), subscripId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), selQueryId);
        
        // Selector query without event id, limited to test objects
        String sqlQuerySel = "SELECT DISTINCT ro.* FROM RegistryObject ro, AuditableEvent e, AffectedObject ao"
            + " WHERE e.id = ''$currentEventId''"
            + " AND e.eventType = ''" + BindingUtility.CANONICAL_EVENT_TYPE_ID_Updated + "''"
            + " AND ao.id = ro.id AND ao.eventId = e.id"
            + " AND ro.id like ''urn:org:freebxml:eric:server:event:SubscriptionMatcherTest:testGetMatchedSubscriptionsMap:pkg%''";

        // Create subscription and selector query
        ServerRequestContext context2 = new ServerRequestContext(CONTEXT_ID + "2", null);
        context2.setUser(AuthenticationServiceImpl.getInstance().farrukh);

        AdhocQueryType selQuery = bu.rimFac.createAdhocQueryType();
        selQuery.setId(selQueryId);
        QueryExpressionType queryExp = bu.rimFac.createQueryExpressionType();
        queryExp.setQueryLanguage(BindingUtility.CANONICAL_QUERY_LANGUAGE_ID_SQL_92);
        queryExp.getContent().add(sqlQuerySel);
        selQuery.setQueryExpression(queryExp);

        SubscriptionType subscript = bu.rimFac.createSubscriptionType();
        subscript.setId(subscripId);
        subscript.setSelector(selQueryId);

        ArrayList<Object> objects = new ArrayList<Object>();
        objects.add(selQuery);
        objects.add(subscript);
        submit(context2, objects);

        // Create reg packs
        ServerRequestContext context3 = new ServerRequestContext(CONTEXT_ID + "3", null);
        context3.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        RegistryPackageType pkg1 = bu.rimFac.createRegistryPackageType();
        pkg1.setId(pkg1Id);
        RegistryPackageType pkg2 = bu.rimFac.createRegistryPackageType();
        pkg2.setId(pkg2Id);
        objects.clear();
        objects.add(pkg1);
        objects.add(pkg2);
        submit(context3, objects);

        // check creation event, expect no match
        SubscriptionMatcher matcher = new SubscriptionMatcher();
        Map<?, ?> subMap = matcher.getMatchedSubscriptionsMap(context3, context3.getUpdateEvent());
        assertTrue("Expecting no subscription match.", subMap.isEmpty());

        // modify pkg1
        ServerRequestContext context4 = new ServerRequestContext(CONTEXT_ID + "4", null);
        context4.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        submit(context4, pkg1);

        // check update event, expect 1 match for pkg1
        subMap = matcher.getMatchedSubscriptionsMap(context4, context4.getUpdateEvent());
        assertEquals("Wrong number of subscriptions matched.", 1, subMap.size());
        assertEquals("Wrong subscriptions matched.", subscripId, ((SubscriptionType)subMap.keySet().iterator().next()).getId());
        assertEquals("Wrong number of objects matched.", 1, ((Collection<?>)subMap.values().iterator().next()).size());
        assertEquals("Wrong object matched.", pkg1Id, ((RegistryPackageType)((Collection<?>)subMap.values().iterator().next()).iterator().next()).getId());

        // modify pkg2
        ServerRequestContext context5 = new ServerRequestContext(CONTEXT_ID + "5", null);
        context5.setUser(AuthenticationServiceImpl.getInstance().farrukh);
        submit(context5, pkg2);

        // check update event, expect only 1 match for pkg2
        subMap = matcher.getMatchedSubscriptionsMap(context5, context5.getUpdateEvent());
        assertEquals("Wrong number of subscriptions matched.", 1, subMap.size());
        assertEquals("Wrong subscriptions matched.", subscripId, ((SubscriptionType)subMap.keySet().iterator().next()).getId());
        assertEquals("Wrong number of objects matched.", 1, ((Collection<?>)subMap.values().iterator().next()).size());
        assertEquals("Wrong object matched.", pkg2Id, ((RegistryPackageType)((Collection<?>)subMap.values().iterator().next()).iterator().next()).getId());

        // final clean-up
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg1Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), pkg2Id);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), subscripId);
        removeIfExist(getContext(AuthenticationServiceImpl.getInstance().farrukh), selQueryId);
    }    
    
}

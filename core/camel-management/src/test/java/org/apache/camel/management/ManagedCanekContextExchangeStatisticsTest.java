/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.management;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.apache.camel.management.DefaultManagementObjectNameStrategy.TYPE_ROUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledOnOs(OS.AIX)
public class ManagedCanekContextExchangeStatisticsTest extends ManagementTestSupport {

    @Test
    public void testExchangesCompletedStatistics() throws Exception {
        MBeanServer mbeanServer = getMBeanServer();

        ObjectName on = getContextObjectName();
        Long completed = (Long) mbeanServer.getAttribute(on, "ExchangesCompleted");
        assertEquals(0, completed.longValue());

        ObjectName route1 = getCamelObjectName(TYPE_ROUTE, "route1");
        Long completed1 = (Long) mbeanServer.getAttribute(route1, "ExchangesCompleted");
        assertEquals(0, completed1.longValue());

        ObjectName route2 = getCamelObjectName(TYPE_ROUTE, "route2");
        Long completed2 = (Long) mbeanServer.getAttribute(route1, "ExchangesCompleted");
        assertEquals(0, completed2.longValue());

        getMockEndpoint("mock:result").expectedMessageCount(1);
        template.sendBody("direct:start", "Hello World");
        assertMockEndpointsSatisfied();

        completed = (Long) mbeanServer.getAttribute(route1, "ExchangesCompleted");
        assertEquals(1, completed.longValue());

        completed1 = (Long) mbeanServer.getAttribute(route1, "ExchangesCompleted");
        assertEquals(1, completed1.longValue());

        completed2 = (Long) mbeanServer.getAttribute(route2, "ExchangesCompleted");
        assertEquals(0, completed2.longValue());

        resetMocks();
        getMockEndpoint("mock:result").expectedMessageCount(2);
        template.sendBody("direct:start", "Hi World");
        template.sendBody("direct:bar", "Bye World");
        assertMockEndpointsSatisfied();

        completed = (Long) mbeanServer.getAttribute(on, "ExchangesCompleted");
        assertEquals(3, completed.longValue());

        completed1 = (Long) mbeanServer.getAttribute(route1, "ExchangesCompleted");
        assertEquals(2, completed1.longValue());

        completed2 = (Long) mbeanServer.getAttribute(route2, "ExchangesCompleted");
        assertEquals(1, completed2.longValue());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .routeId("route1")
                        .to("log:foo").to("mock:result");

                from("direct:bar")
                        .routeId("route2")
                        .to("log:bar").to("mock:result");
            }
        };
    }

}

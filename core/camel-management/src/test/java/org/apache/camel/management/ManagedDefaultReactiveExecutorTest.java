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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.apache.camel.management.DefaultManagementObjectNameStrategy.TYPE_SERVICE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledOnOs(OS.AIX)
public class ManagedDefaultReactiveExecutorTest extends ManagementTestSupport {

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext context = super.createCamelContext();
        context.getCamelContextExtension().getReactiveExecutor().setStatisticsEnabled(true);
        return context;
    }

    @Test
    public void testReactiveExecutor() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);

        template.sendBody("seda:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("seda:start")
                        .to("log:foo")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                // check mbeans
                                MBeanServer mbeanServer = getMBeanServer();

                                ObjectName on = getCamelObjectName(TYPE_SERVICE, "DefaultReactiveExecutor");
                                assertTrue(mbeanServer.isRegistered(on), "Should be registered");

                                // should be 1 running
                                Integer running = (Integer) mbeanServer.getAttribute(on, "RunningWorkers");
                                assertEquals(1, running.intValue());

                                // should be 0 pending
                                Integer pending = (Integer) mbeanServer.getAttribute(on, "PendingTasks");
                                assertEquals(0, pending.intValue());
                            }
                        })
                        .to("log:bar")
                        .to("mock:result");
            }
        };
    }

}

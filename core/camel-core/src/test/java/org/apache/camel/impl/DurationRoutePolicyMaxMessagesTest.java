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
package org.apache.camel.impl;

import java.util.concurrent.TimeUnit;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.engine.DurationRoutePolicy;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DurationRoutePolicyMaxMessagesTest extends ContextTestSupport {

    @Test
    public void testDurationRoutePolicy() throws Exception {
        assertTrue(context.getRouteController().getRouteStatus("foo").isStarted());
        assertFalse(context.getRouteController().getRouteStatus("foo").isStopped());

        // the policy should stop the route after 5 messages
        getMockEndpoint("mock:foo").expectedMinimumMessageCount(5);
        assertMockEndpointsSatisfied();

        // need a little time to stop async
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertFalse(context.getRouteController().getRouteStatus("foo").isStarted());
            assertTrue(context.getRouteController().getRouteStatus("foo").isStopped());
        });
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                DurationRoutePolicy policy = new DurationRoutePolicy();
                policy.setMaxMessages(5);

                from("timer:foo?period=100").routeId("foo").routePolicy(policy).to("mock:foo");
            }
        };
    }
}

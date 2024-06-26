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
package org.apache.camel.component.seda;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.FailedToStartRouteException;
import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SameSedaQueueMultipleConsumersDifferenceTest extends ContextTestSupport {

    @Test
    public void testSameOptions() throws Exception {
        getMockEndpoint("mock:foo").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:bar").expectedBodiesReceived("Hello World");

        template.sendBody("seda:foo?multipleConsumers=true", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testSameOptionsProducerStillOkay() throws Exception {
        getMockEndpoint("mock:foo").expectedBodiesReceived("Hello World");
        getMockEndpoint("mock:bar").expectedBodiesReceived("Hello World");

        template.sendBody("seda:foo", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testAddConsumer() {
        Exception e = assertThrows(Exception.class, () -> {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from("seda:foo").routeId("fail").to("mock:fail");
                }
            });
        }, "Should have thrown exception");

        FailedToStartRouteException failed = assertIsInstanceOf(FailedToStartRouteException.class, e);
        assertEquals("fail", failed.getRouteId());
        assertEquals(
                "Cannot use existing queue seda://foo as the existing queue multiple consumers true does not match given multiple consumers false",
                e.getCause().getMessage());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("seda:foo?multipleConsumers=true").routeId("foo").to("mock:foo");
                from("seda:foo?multipleConsumers=true").routeId("bar").to("mock:bar");
            }
        };
    }
}

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
package org.apache.camel.component.ref;

import org.apache.camel.CamelContext;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.NoSuchBeanException;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RefInvalidTest extends ContextTestSupport {

    @Test
    public void testOk() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(1);

        template.sendBody("ref:foo", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testInvalid() {
        Exception ex = assertThrows(ResolveEndpointFailedException.class, () -> template.sendBody("ref:xxx", "Hello World"),
                "Should have thrown an exception");

        assertEquals(
                "Failed to resolve endpoint: ref://xxx due to: No bean could be found in the registry for: xxx of type: org.apache.camel.Endpoint",
                ex.getMessage());
        NoSuchBeanException cause = assertIsInstanceOf(NoSuchBeanException.class, ex.getCause());
        assertEquals("xxx", cause.getName());
    }

    @Override
    protected CamelContext createCamelContext() {
        CamelContext context = new DefaultCamelContext();
        context.getRegistry().bind("foo", context.getEndpoint("seda:foo"));
        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("ref:foo").routeId("foo").to("mock:result");
            }
        };
    }
}

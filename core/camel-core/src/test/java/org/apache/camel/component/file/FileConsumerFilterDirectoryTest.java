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
package org.apache.camel.component.file;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

/**
 * Unit test for the filter file option
 */
public class FileConsumerFilterDirectoryTest extends ContextTestSupport {

    @Test
    public void testFilterFiles() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(0);

        template.sendBodyAndHeader(fileUri("foo"), "This is a file to be filtered", Exchange.FILE_NAME,
                "skipme.txt");

        mock.setResultWaitTime(100);
        mock.assertIsSatisfied();
    }

    @Test
    public void testFilterFilesWithARegularFile() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("Hello World");

        template.sendBodyAndHeader(fileUri("foo"), "This is a file to be filtered", Exchange.FILE_NAME,
                "skipme.txt");

        template.sendBodyAndHeader(fileUri("barbar"), "Hello World", Exchange.FILE_NAME, "hello.txt");

        mock.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(fileUri(
                        "?initialDelay=0&delay=10&recursive=true&filterDirectory=${header.CamelFileNameOnly.length()} > 4"))
                        .convertBodyTo(String.class).to("mock:result");
            }
        };
    }

}

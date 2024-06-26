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

import java.io.IOException;
import java.net.ConnectException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.ExchangeTestSupport;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.SafeCopyProperty;
import org.apache.camel.TypeConversionException;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.support.DefaultMessage;
import org.apache.camel.support.ExchangeHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultExchangeTest extends ExchangeTestSupport {

    private static final String SAFE_PROPERTY = "SAFE_PROPERTY";
    private static final String UNSAFE_PROPERTY = "UNSAFE_PROPERTY";

    @Test
    public void testBody() throws Exception {
        assertNotNull(exchange.getIn().getBody());

        assertEquals("<hello id='m123'>world!</hello>", exchange.getIn().getBody());
        assertEquals("<hello id='m123'>world!</hello>", exchange.getIn().getBody(String.class));

        assertEquals("<hello id='m123'>world!</hello>", exchange.getIn().getMandatoryBody());
        assertEquals("<hello id='m123'>world!</hello>", exchange.getIn().getMandatoryBody(String.class));
    }

    @Test
    public void testMandatoryBody() throws Exception {
        assertNotNull(exchange.getIn().getBody());

        assertEquals("<hello id='m123'>world!</hello>", exchange.getIn().getBody());

        assertThrows(TypeConversionException.class, () -> assertNull(exchange.getIn().getBody(Integer.class)),
                "Should have thrown a TypeConversionException");

        assertEquals("<hello id='m123'>world!</hello>", exchange.getIn().getMandatoryBody());

        assertThrows(InvalidPayloadException.class, () -> exchange.getIn().getMandatoryBody(Integer.class),
                "Should have thrown an InvalidPayloadException");
    }

    @Test
    public void testExceptionAsType() {
        exchange.setException(
                RuntimeCamelException.wrapRuntimeCamelException(new ConnectException("Cannot connect to remote server")));

        ConnectException ce = exchange.getException(ConnectException.class);
        assertNotNull(ce);
        assertEquals("Cannot connect to remote server", ce.getMessage());

        IOException ie = exchange.getException(IOException.class);
        assertNotNull(ie);
        assertEquals("Cannot connect to remote server", ie.getMessage());

        Exception e = exchange.getException(Exception.class);
        assertNotNull(e);
        assertEquals("Cannot connect to remote server", e.getMessage());

        RuntimeCamelException rce = exchange.getException(RuntimeCamelException.class);
        assertNotNull(rce);
        assertNotSame("Cannot connect to remote server", rce.getMessage());
        assertEquals("Cannot connect to remote server", rce.getCause().getMessage());
    }

    @Test
    public void testHeader() {
        assertNotNull(exchange.getIn().getHeaders());

        assertEquals(123, exchange.getIn().getHeader("bar"));
        assertEquals(Integer.valueOf(123), exchange.getIn().getHeader("bar", Integer.class));
        assertEquals("123", exchange.getIn().getHeader("bar", String.class));
        assertEquals(123, exchange.getIn().getHeader("bar", 234));
        assertEquals(123, exchange.getIn().getHeader("bar", () -> 456));
        assertEquals(456, exchange.getIn().getHeader("baz", () -> 456));

        assertEquals(123, exchange.getIn().getHeader("bar", 234));
        assertEquals(Integer.valueOf(123), exchange.getIn().getHeader("bar", 234, Integer.class));
        assertEquals("123", exchange.getIn().getHeader("bar", "234", String.class));
        assertEquals("123", exchange.getIn().getHeader("bar", () -> "456", String.class));
        assertEquals("456", exchange.getIn().getHeader("baz", () -> "456", String.class));

        assertEquals(234, exchange.getIn().getHeader("cheese", 234));
        assertEquals("234", exchange.getIn().getHeader("cheese", 234, String.class));
        assertEquals("456", exchange.getIn().getHeader("cheese", () -> 456, String.class));
    }

    @Test
    public void testProperty() {
        exchange.removeProperty("foobar");
        assertFalse(exchange.hasProperties());

        exchange.setProperty("fruit", "apple");
        assertTrue(exchange.hasProperties());

        assertEquals("apple", exchange.getProperty("fruit"));
        assertNull(exchange.getProperty("beer"));
        assertNull(exchange.getProperty("beer", String.class));

        // Current TypeConverter support to turn the null value to false of
        // boolean,
        // as assertEquals needs the Object as the parameter, we have to use
        // Boolean.FALSE value in this case
        assertEquals(Boolean.FALSE, exchange.getProperty("beer", boolean.class));
        assertNull(exchange.getProperty("beer", Boolean.class));

        assertEquals("apple", exchange.getProperty("fruit", String.class));
        assertEquals("apple", exchange.getProperty("fruit", "banana", String.class));
        assertEquals("banana", exchange.getProperty("beer", "banana", String.class));
    }

    @Test
    public void testVariable() {
        exchange.removeVariable("cheese");
        assertFalse(exchange.hasVariables());

        exchange.setVariable("fruit", "apple");
        assertTrue(exchange.hasVariables());

        assertEquals("apple", exchange.getVariable("fruit"));
        assertNull(exchange.getVariable("beer"));
        assertNull(exchange.getVariable("beer", String.class));

        // Current TypeConverter support to turn the null value to false of
        // boolean,
        // as assertEquals needs the Object as the parameter, we have to use
        // Boolean.FALSE value in this case
        assertEquals(Boolean.FALSE, exchange.getVariable("beer", boolean.class));
        assertNull(exchange.getVariable("beer", Boolean.class));

        assertEquals("apple", exchange.getVariable("fruit", String.class));
        assertEquals("apple", exchange.getVariable("fruit", "banana", String.class));
        assertEquals("banana", exchange.getVariable("beer", "banana", String.class));
    }

    @Test
    public void testRemoveProperties() {
        exchange.removeProperty("foobar");
        assertFalse(exchange.hasProperties());

        exchange.setProperty("fruit", "apple");
        exchange.setProperty("fruit1", "banana");
        exchange.setProperty("zone", "Africa");
        assertTrue(exchange.hasProperties());

        assertEquals("apple", exchange.getProperty("fruit"));
        assertEquals("banana", exchange.getProperty("fruit1"));
        assertEquals("Africa", exchange.getProperty("zone"));

        exchange.removeProperties("fr*");
        assertTrue(exchange.hasProperties());
        assertEquals(1, exchange.getProperties().size());
        assertNull(exchange.getProperty("fruit", String.class));
        assertNull(exchange.getProperty("fruit1", String.class));
        assertEquals("Africa", exchange.getProperty("zone", String.class));
    }

    @Test
    public void testRemoveVariables() {
        exchange.removeVariable("cheese");
        assertFalse(exchange.hasVariables());

        exchange.setVariable("fruit", "apple");
        exchange.setVariable("fruit1", "banana");
        exchange.setVariable("zone", "Africa");
        assertTrue(exchange.hasVariables());

        assertEquals("apple", exchange.getVariable("fruit"));
        assertEquals("banana", exchange.getVariable("fruit1"));
        assertEquals("Africa", exchange.getVariable("zone"));
    }

    @Test
    public void testRemoveAllProperties() {
        exchange.removeProperty("foobar");
        assertFalse(exchange.hasProperties());

        exchange.setProperty("fruit", "apple");
        exchange.setProperty("fruit1", "banana");
        exchange.setProperty("zone", "Africa");
        assertTrue(exchange.hasProperties());

        exchange.removeProperties("*");
        assertFalse(exchange.hasProperties());
        assertEquals(0, exchange.getProperties().size());
    }

    @Test
    public void testRemoveAllVariables() {
        exchange.removeVariable("cheese");
        assertFalse(exchange.hasVariables());

        exchange.setVariable("fruit", "apple");
        exchange.setVariable("fruit1", "banana");
        exchange.setVariable("zone", "Africa");
        assertTrue(exchange.hasVariables());

        exchange.removeVariable("*");
        assertFalse(exchange.hasVariables());
        assertEquals(0, exchange.getVariables().size());
    }

    @Test
    public void testRemovePropertiesWithExclusion() {
        exchange.removeProperty("foobar");
        assertFalse(exchange.hasProperties());

        exchange.setProperty("fruit", "apple");
        exchange.setProperty("fruit1", "banana");
        exchange.setProperty("fruit2", "peach");
        exchange.setProperty("zone", "Africa");
        assertTrue(exchange.hasProperties());

        assertEquals("apple", exchange.getProperty("fruit"));
        assertEquals("banana", exchange.getProperty("fruit1"));
        assertEquals("peach", exchange.getProperty("fruit2"));
        assertEquals("Africa", exchange.getProperty("zone"));

        exchange.removeProperties("fr*", "fruit1", "fruit2");
        assertTrue(exchange.hasProperties());
        assertEquals(3, exchange.getProperties().size());
        assertNull(exchange.getProperty("fruit", String.class));
        assertEquals("banana", exchange.getProperty("fruit1", String.class));
        assertEquals("peach", exchange.getProperty("fruit2", String.class));
        assertEquals("Africa", exchange.getProperty("zone", String.class));
    }

    @Test
    public void testRemovePropertiesPatternWithAllExcluded() {
        exchange.removeProperty("foobar");
        assertFalse(exchange.hasProperties());

        exchange.setProperty("fruit", "apple");
        exchange.setProperty("fruit1", "banana");
        exchange.setProperty("fruit2", "peach");
        exchange.setProperty("zone", "Africa");
        assertTrue(exchange.hasProperties());

        assertEquals("apple", exchange.getProperty("fruit"));
        assertEquals("banana", exchange.getProperty("fruit1"));
        assertEquals("peach", exchange.getProperty("fruit2"));
        assertEquals("Africa", exchange.getProperty("zone"));

        exchange.removeProperties("fr*", "fruit", "fruit1", "fruit2", "zone");
        assertTrue(exchange.hasProperties());
        assertEquals(4, exchange.getProperties().size());
        assertEquals("apple", exchange.getProperty("fruit", String.class));
        assertEquals("banana", exchange.getProperty("fruit1", String.class));
        assertEquals("peach", exchange.getProperty("fruit2", String.class));
        assertEquals("Africa", exchange.getProperty("zone", String.class));
    }

    @Test
    public void testRemoveInternalProperties() {
        exchange.setProperty(ExchangePropertyKey.CHARSET_NAME, "iso-8859-1");

        assertEquals("iso-8859-1", exchange.getProperty(ExchangePropertyKey.CHARSET_NAME));
        assertEquals("iso-8859-1", exchange.getProperty(Exchange.CHARSET_NAME));

        exchange.removeProperty(ExchangePropertyKey.CHARSET_NAME);
        assertNull(exchange.getProperty(ExchangePropertyKey.CHARSET_NAME));
        assertNull(exchange.getProperty(Exchange.CHARSET_NAME));

        exchange.setProperty(ExchangePropertyKey.CHARSET_NAME, "iso-8859-1");
        exchange.setProperty(ExchangePropertyKey.AGGREGATED_SIZE, "1");
        exchange.setProperty(ExchangePropertyKey.AGGREGATED_TIMEOUT, "2");

        exchange.removeProperties("CamelAggregated*");
        assertEquals("iso-8859-1", exchange.getProperty(ExchangePropertyKey.CHARSET_NAME));
        assertNull(exchange.getProperty(ExchangePropertyKey.AGGREGATED_SIZE));
        assertNull(exchange.getProperty(ExchangePropertyKey.AGGREGATED_TIMEOUT));

        exchange.removeProperties("*");
        assertNull(exchange.getProperty(ExchangePropertyKey.CHARSET_NAME));
    }

    @Test
    public void testAllProperties() {
        exchange.removeProperties("*");
        exchange.setProperty("foo", 123);
        exchange.setProperty(ExchangePropertyKey.TO_ENDPOINT, "seda:bar");
        exchange.setProperty(ExchangePropertyKey.CHARSET_NAME, "iso-8859-1");

        assertEquals(1, exchange.getProperties().size());
        assertEquals(2, exchange.getExchangeExtension().getInternalProperties().size());
        assertEquals(3, exchange.getAllProperties().size());
    }

    @Test
    public void testCopyExchangeWithVariables() {
        exchange.setVariable("beer", "Carlsberg");
        assertEquals(2, exchange.getVariables().size());

        Exchange copy = exchange.copy();
        assertTrue(copy.hasVariables());
        assertEquals(2, copy.getVariables().size());
        assertEquals("gauda", copy.getVariable("cheese"));
        assertEquals("Carlsberg", copy.getVariable("beer"));

        exchange.setVariable("beer", "Heineken");
        assertEquals("Carlsberg", copy.getVariable("beer"));

        exchange.removeVariable("beer");
        assertEquals(1, exchange.getVariables().size());
        assertEquals("Carlsberg", copy.getVariable("beer"));
        assertEquals(2, copy.getVariables().size());

        exchange.removeVariable("*");
        assertFalse(exchange.hasVariables());
        assertTrue(copy.hasVariables());
        assertEquals(2, copy.getVariables().size());
    }

    @Test
    public void testInType() {
        exchange.setIn(new MyMessage(context));

        MyMessage my = exchange.getIn(MyMessage.class);
        assertNotNull(my);
    }

    @Test
    public void testOutType() {
        exchange.setOut(new MyMessage(context));

        MyMessage my = exchange.getOut(MyMessage.class);
        assertNotNull(my);
    }

    @Test
    public void testCopy() {
        DefaultExchange sourceExchange = new DefaultExchange(context);
        MyMessage sourceIn = new MyMessage(context);
        sourceExchange.setIn(sourceIn);
        Exchange destExchange = sourceExchange.copy();
        Message destIn = destExchange.getIn();

        assertEquals(sourceIn.getClass(), destIn.getClass(), "Dest message should be of the same type as source message");
    }

    @Test
    public void testExchangeSafeCopy() {
        DefaultExchange exchange = new DefaultExchange(context);
        SafeProperty property = new SafeProperty();
        UnsafeProperty unsafeProperty = new UnsafeProperty();
        exchange.getExchangeExtension().setSafeCopyProperty(SAFE_PROPERTY, property);
        exchange.setProperty(UNSAFE_PROPERTY, unsafeProperty);

        Exchange copy = ExchangeHelper.createCorrelatedCopy(exchange, false);

        assertThat(copy.getProperty(SAFE_PROPERTY)).isNotSameAs(property);
        assertThat(copy.getProperty(UNSAFE_PROPERTY)).isSameAs(unsafeProperty);

    }

    private static final class SafeProperty implements SafeCopyProperty {

        private SafeProperty() {

        }

        @Override
        public SafeProperty safeCopy() {
            return new SafeProperty();
        }

    }

    private static class UnsafeProperty {

    }

    public static class MyMessage extends DefaultMessage {
        public MyMessage(CamelContext camelContext) {
            super(camelContext);
        }

        @Override
        public MyMessage newInstance() {
            return new MyMessage(getCamelContext());
        }
    }

}

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
package org.apache.camel.generator.openapi;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.apache.camel.CamelContext;
import org.apache.camel.model.rest.RestsDefinition;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.xml.LwModelToXMLDumper;

public class RestDslXmlGenerator extends RestDslGenerator<RestDslXmlGenerator> {

    RestDslXmlGenerator(final OpenAPI document) {
        super(document);
    }

    public String generate(final CamelContext context) throws Exception {
        final RestDefinitionEmitter emitter = new RestDefinitionEmitter();
        final String basePath = RestDslGenerator.determineBasePathFrom(this.basePath, document);
        final PathVisitor<RestsDefinition> restDslStatement = new PathVisitor<>(
                basePath, emitter, filter,
                destinationGenerator(),
                dtoPackageName);

        for (String name : document.getPaths().keySet()) {
            PathItem item = document.getPaths().get(name);
            restDslStatement.visit(name, item);
        }

        final RestsDefinition rests = emitter.result();
        final String xml = new LwModelToXMLDumper().dumpModelAsXml(context, rests);

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        builderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builderFactory.setNamespaceAware(true);

        final DocumentBuilder builder = builderFactory.newDocumentBuilder();

        final Document document = builder.parse(new InputSource(new StringReader(xml)));

        final Element root = document.getDocumentElement();

        // remove all customId attributes as we do not want them in the output
        final NodeList elements = document.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            final Element element = (Element) elements.item(i);
            element.removeAttribute("customId");
        }

        boolean restConfig = restComponent != null || restContextPath != null || clientRequestValidation;
        if (restConfig) {
            final Element configuration = document.createElement("restConfiguration");
            if (ObjectHelper.isNotEmpty(restComponent)) {
                configuration.setAttribute("component", restComponent);
            }
            if (ObjectHelper.isNotEmpty(restContextPath)) {
                configuration.setAttribute("contextPath", restContextPath);
            }
            if (ObjectHelper.isNotEmpty(apiContextPath)) {
                configuration.setAttribute("apiContextPath", apiContextPath);
            }
            if (clientRequestValidation) {
                configuration.setAttribute("clientRequestValidation", "true");
            }
            root.insertBefore(configuration, root.getFirstChild());
        }

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException e) {
            // ignore
        }
        try {
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException e) {
            // ignore
        }
        final Transformer transformer = transformerFactory.newTransformer();

        final StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));

        return writer.toString();
    }
}

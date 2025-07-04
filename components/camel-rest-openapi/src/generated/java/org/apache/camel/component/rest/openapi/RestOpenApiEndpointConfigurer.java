/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.rest.openapi;

import javax.annotation.processing.Generated;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.support.component.PropertyConfigurerSupport;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.EndpointSchemaGeneratorMojo")
@SuppressWarnings("unchecked")
public class RestOpenApiEndpointConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        RestOpenApiEndpoint target = (RestOpenApiEndpoint) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "apicontextpath":
        case "apiContextPath": target.setApiContextPath(property(camelContext, java.lang.String.class, value)); return true;
        case "basepath":
        case "basePath": target.setBasePath(property(camelContext, java.lang.String.class, value)); return true;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": target.setBridgeErrorHandler(property(camelContext, boolean.class, value)); return true;
        case "clientrequestvalidation":
        case "clientRequestValidation": target.setClientRequestValidation(property(camelContext, boolean.class, value)); return true;
        case "clientresponsevalidation":
        case "clientResponseValidation": target.setClientResponseValidation(property(camelContext, boolean.class, value)); return true;
        case "componentname":
        case "componentName": target.setComponentName(property(camelContext, java.lang.String.class, value)); return true;
        case "consumercomponentname":
        case "consumerComponentName": target.setConsumerComponentName(property(camelContext, java.lang.String.class, value)); return true;
        case "consumes": target.setConsumes(property(camelContext, java.lang.String.class, value)); return true;
        case "exceptionhandler":
        case "exceptionHandler": target.setExceptionHandler(property(camelContext, org.apache.camel.spi.ExceptionHandler.class, value)); return true;
        case "exchangepattern":
        case "exchangePattern": target.setExchangePattern(property(camelContext, org.apache.camel.ExchangePattern.class, value)); return true;
        case "host": target.setHost(property(camelContext, java.lang.String.class, value)); return true;
        case "lazystartproducer":
        case "lazyStartProducer": target.setLazyStartProducer(property(camelContext, boolean.class, value)); return true;
        case "missingoperation":
        case "missingOperation": target.setMissingOperation(property(camelContext, java.lang.String.class, value)); return true;
        case "mockincludepattern":
        case "mockIncludePattern": target.setMockIncludePattern(property(camelContext, java.lang.String.class, value)); return true;
        case "produces": target.setProduces(property(camelContext, java.lang.String.class, value)); return true;
        case "requestvalidationenabled":
        case "requestValidationEnabled": target.setRequestValidationEnabled(property(camelContext, boolean.class, value)); return true;
        case "restopenapiprocessorstrategy":
        case "restOpenapiProcessorStrategy": target.setRestOpenapiProcessorStrategy(property(camelContext, org.apache.camel.component.rest.openapi.RestOpenapiProcessorStrategy.class, value)); return true;
        default: return false;
        }
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "apicontextpath":
        case "apiContextPath": return java.lang.String.class;
        case "basepath":
        case "basePath": return java.lang.String.class;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return boolean.class;
        case "clientrequestvalidation":
        case "clientRequestValidation": return boolean.class;
        case "clientresponsevalidation":
        case "clientResponseValidation": return boolean.class;
        case "componentname":
        case "componentName": return java.lang.String.class;
        case "consumercomponentname":
        case "consumerComponentName": return java.lang.String.class;
        case "consumes": return java.lang.String.class;
        case "exceptionhandler":
        case "exceptionHandler": return org.apache.camel.spi.ExceptionHandler.class;
        case "exchangepattern":
        case "exchangePattern": return org.apache.camel.ExchangePattern.class;
        case "host": return java.lang.String.class;
        case "lazystartproducer":
        case "lazyStartProducer": return boolean.class;
        case "missingoperation":
        case "missingOperation": return java.lang.String.class;
        case "mockincludepattern":
        case "mockIncludePattern": return java.lang.String.class;
        case "produces": return java.lang.String.class;
        case "requestvalidationenabled":
        case "requestValidationEnabled": return boolean.class;
        case "restopenapiprocessorstrategy":
        case "restOpenapiProcessorStrategy": return org.apache.camel.component.rest.openapi.RestOpenapiProcessorStrategy.class;
        default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        RestOpenApiEndpoint target = (RestOpenApiEndpoint) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "apicontextpath":
        case "apiContextPath": return target.getApiContextPath();
        case "basepath":
        case "basePath": return target.getBasePath();
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return target.isBridgeErrorHandler();
        case "clientrequestvalidation":
        case "clientRequestValidation": return target.isClientRequestValidation();
        case "clientresponsevalidation":
        case "clientResponseValidation": return target.isClientResponseValidation();
        case "componentname":
        case "componentName": return target.getComponentName();
        case "consumercomponentname":
        case "consumerComponentName": return target.getConsumerComponentName();
        case "consumes": return target.getConsumes();
        case "exceptionhandler":
        case "exceptionHandler": return target.getExceptionHandler();
        case "exchangepattern":
        case "exchangePattern": return target.getExchangePattern();
        case "host": return target.getHost();
        case "lazystartproducer":
        case "lazyStartProducer": return target.isLazyStartProducer();
        case "missingoperation":
        case "missingOperation": return target.getMissingOperation();
        case "mockincludepattern":
        case "mockIncludePattern": return target.getMockIncludePattern();
        case "produces": return target.getProduces();
        case "requestvalidationenabled":
        case "requestValidationEnabled": return target.isRequestValidationEnabled();
        case "restopenapiprocessorstrategy":
        case "restOpenapiProcessorStrategy": return target.getRestOpenapiProcessorStrategy();
        default: return null;
        }
    }
}


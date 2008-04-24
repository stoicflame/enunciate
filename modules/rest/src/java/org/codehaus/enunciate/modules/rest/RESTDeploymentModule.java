/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.rest;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import freemarker.template.TemplateException;
import net.sf.jelly.apt.Context;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.rest.ContentTypeHandler;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.Enunciate;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.rest.json.JsonContentHandler;
import org.codehaus.enunciate.modules.rest.json.JsonSerializationMethod;
import org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * <h1>REST Module</h1>
 *
 * <p>All metadata for REST endpoints can be discovered at runtime, therefore, the REST deployment module
 * exists only as a set of tools used to deploy the REST API.  In other words, no compile-time
 * generation or validation is needed.</p>
 *
 * <p>The order of the REST deployment module is 0, as it doesn't depend on any artifacts exported
 * by any other module.</p>
 *
 * <ul>
 *   <li><a href="#model">REST Model</a></li>
 *   <li><a href="#constraints">Constraints</a></li>
 *   <li><a href="#java2rest">Mapping Java to a REST API</a></li>
 *   <li><a href="#json">JSON API</a></li>
 *   <li><a href="#steps">steps</a></li>
 *   <li><a href="#config">configuration</a></li>
 *   <li><a href="#artifacts">artifacts</a></li>
 * </ul>
 *
 * <h1><a name="model">REST Model</a></h1>
 *
 * <p>We start by defining a model for the REST API.  A REST API is comprised of a set of <i>resources</i>
 * on which a constrained set of <i>operations</i> can act.  Borrowing terms from english grammar, Enunciate
 * refers to the REST resources as <i>nouns</i> and the REST operations as <i>verbs</i>.  Because the REST
 * API is to be deployed using HTTP, Enunciate constrains the set of verbs to the set {<i>create</i>, <i>read</i>,
 * <i>update</i>, <i>delete</i>}, mapping to the HTTP verbs {<i>PUT</i>, <i>GET</i>, <i>POST</i>, <i>DELETE</i>},
 * respectively.</p>
 *
 * <p>While a REST endpoint <i>must</i> have a noun and a verb, it can optionally use other constructs to
 * more clearly define itself.</p>
 *
 * <h3>Adjectives</h3>
 *
 * <p>REST adjectives are used to qualify a REST noun or a REST verb.  For a REST invocation, an adjective
 * has a name and one or more values.  In terms of HTTP, adjectives are passed as HTTP parameters.</p>
 *
 * <p>For example, if we were to invoke the verb "read" on a noun "circle", but we wanted to describe the
 * color of the circle as "red", then "color" would be the adjective and "red" would be the adjective value.
 * And mapped to HTTP, the HTTP request would look something like this:</p>
 *
 * <code>
 * GET /rest/circle?color=red
 * </code>
 *
 * <h3>Proper Noun</h3>
 *
 * <p>A proper noun is used to identify a specific <i>noun</i>.  In practical terms, a proper noun usually takes
 * the form of an id, and the only difference between a proper noun and an adjective is that the proper noun is
 * supplied directly on the URL, as opposed to being supplied as a query parameter.</p>
 *
 * <p>For example, if we wanted to invoke the verb "read" on a noun "purchase-order", but identify the specific
 * purchase order by the id "12345", the "12345" could be a proper noun.  (Note that it could also be an adjective,
 * the only difference is that a proper noun doesn't need a name, only a value.)</p>
 *
 * <p>And an HTTP request might like like this:</p>
 *
 * <code>
 * GET /rest/purchase-order/12345
 * </code>
 *
 * <h3>Noun Value</h3>
 *
 * <p>In REST, a noun value often needs to be supplied, such as during a "create" or an "update".  For example,
 * If we were to invoke the verb "update" on the noun "shape" to be "red circle", "red circle" would be the
 * noun value.  In terms of HTTP, the noun value is the payload of the request, and the request would look
 * something like this:</p>
 *
 * <code>
 * POST /rest/shape
 *
 * &lt;circle color="red"/&gt;
 * </code>
 *
 * <p>It is also important to note that noun values (request payloads) don't necessarily have to be XML; they can
 * be of any custom content type.</p>
 *
 * <h3>Noun Context</h3>
 *
 * <p>A noun can be qualified by a noun context.  The noun context can be though of as a "grouping" of nouns.
 * Perhaps, as an admittedly contrived example, we were to have two separate resources for the noun "rectangle",
 * say "wide" and "tall". The "rectangle" those two contexts could be applied to qualifies the different "rectangle"
 * nouns.</p>
 *
 * <h3>Noun Context Parameters</h3>
 *
 * <p>A noun context parameter (or just "context parameter") is a parameter that is defined by the noun context. For example, if we wanted to identify
 * a specific user of a specific group, we could identify the "group id" as a context parameter, the user as the noun, and the user id as the proper
 * noun.</p>
 *
 * <h3>REST Payloads and Custom Content Types</h3>
 *
 * <p>It is often necessary to provide REST resources of custom content types along with the XML responses. We define these resources as REST payloads.
 * A REST payload consists of the resource, it's content type (MIME type), and an optional set of metadata (i.e. HTTP headers) that are associated with
 * the resource.</p>
 *
 * <h1><a name="constraints">Constraints</a></h1>
 *
 * <p>Enunciate uses J2EE and JAXB 2.0 to map a REST model onto HTTP.  In order to do that definitively, Enunciate
 * imposes the following constraints:</p>
 *
 * <ul>
 *   <li>All verbs that act on the same noun must be unique.  (E.g. there can't be two "read" methods for the same noun.)</li>
 *   <li>Proper nouns must not be of a complex XML type.  Only simple types are allowed (e.g. integer, string, enum, etc.).</li>
 *   <li>There can only be one proper noun for a REST operation</li>
 *   <li>Adjectives must be simple types, but there can be more than one value for a single adjective.</li>
 *   <li>The verbs "read" and "delete" cannot support a noun value.</li>
 *   <li>A noun value must be an xml root element (not just a complex type)</li>
 *   <li>A return type must be either a root element or a REST payload.</li>
 *   <li>Noun context parameters must be simple types</li>
 * </ul>
 *
 * <h1><a name="java2rest">Mapping Java to a REST API</a></h1>
 *
 * <h3>Java Types</h3>
 *
 * <p>The <i>org.codehaus.enunciate.rest.annotations.RESTEndpoint</i> annotation is used on a Java type (i.e. class or interface)
 * to indicate that it contains methods that will service REST endpoints.  This is used simply to indicate to
 * the engine that the methods on the annotated class or interface should be searched for their nouns and verbs.
 * Only if a method is annotated with <i>org.codehaus.enunciate.rest.annotations.Verb</i> will it service a REST endpoint
 * (see below).</p>
 *
 * <p>The @RESTEndpoint annotation on an interface means that the annotated interface defines the REST methods
 * for any methods on an annotated class that <i>directly implement</i> it.  In practical terms, classes annotated
 * with @RESTEndpoint will use the metadata on any @RESTEnpoint interface instead of the metadata in their own
 * methods.  Allowing interfaces to define the REST API allows developers to leverage the advantages of coding to
 * interfaces (e.g. introduction of aspects, multiple implementations, etc.).</p>
 *
 * <h3>Java Methods</h3>
 *
 * <p>Each Java method that is to serve as a REST endpoint must be assigned a verb and a noun.  A public method can be assigned a
 * verb with the <i>org.codehaus.enunciate.rest.annotations.Verb</i> annotation.  A method that is not assigned a verb will
 * not be considered to service a REST endpoint.</p>
 *
 * <p>A method that is assigned a verb must be assigned a noun as well.  The noun is specified with the
 * <i>org.codehaus.enunciate.rest.annotations.Noun</i> annotation, which can supply both the name and the context of the noun.  As a convenience,
 * The <i>org.codehaus.enunciate.rest.annotations.NounContext</i> annotation can be supplied along with the @RESTEndpoint
 * annotation at the level of the interface (or class) to specify the default context for all nouns that are defined by the methods
 * of the interface (or class).</p>
 *
 * <p>To identify a context parameter, specify the name of the context parameter in braces ("{" and "}") in the noun context.  When context
 * parameters are defined, Enunciate will look for a method parameter that is defined to be a context parameter with the same name.  If there is
 * no context parameter defined by that name, the context parameter will be silently ignored. See below for how to define a method parameter
 * as a context parameter.</p>
 *
 * <h3>Java Return Types</h3>
 *
 * <p>The return type of the Java method determines the content type (MIME type) of a REST resource.  By default, Enunciate will attempt serialize the return
 * value of a method using JAXB.  Thus the default requirement that return types must be XML root elements, since otherwise JAXB wouldn't know the name of the outer
 * root XML element. The default content type of a JAXB response is "text/xml" for XML requests and "application/json" for JSON requests.  You can use the
 * org.codehaus.enunciate.rest.annotations.ContentType annotation to specify a different content type for the XML requests (e.g. "application/atom+xml").</p>
 *
 * <p>However, Enunciate also supports resources of different content types. This can be done by defining the Java method to return
 * javax.activation.DataHandler, which defines its own payload and content type. For such methods, you must supply the
 * <i>@org.codehaus.enunciate.rest.annotations.DataFormat</i> annotation which is used to identify externally the id of the data format URL.</p>
 *
 * <h3>Java Method Parameters</h3>
 *
 * <p>A parameter to a method can be a proper noun, an adjective, a context parameter, or a noun value.  By default, a parameter is mapped
 * as an adjective.  The name of the adjective by default is the name of the parameter.  Parameters
 * can be customized with the <i>org.codehaus.enunciate.rest.annotations.Adjective</i>, <i>org.codehaus.enunciate.rest.annotations.NounValue</i>,
 * <i>org.codehaus.enunciate.rest.annotations.ContextParameter</i>, and <i>org.codehaus.enunciate.rest.annotations.ProperNoun</i> annotations.</p>
 *
 * <p><u>Complex Adjectives</u></p>
 *
 * <p>There may be cases where a REST request may accept a large number of adjectives (HTTP parameters).  If this is the case, the corresponding Java method
 * could start to get unwieldy because of the number of parameters on the method.  To address this inconvenience, Enunciate supports the concept of a
 * "complex adjective".  An adjective is marked as complex with by setting "complex=true" on the @Adjective annotation.  The type of a complex adjective is
 * expected to be a simple bean with an accessible no-arg constructor. In the case of a complex adjective the properties of the bean of a complex adjective
 * define the names and types of the adjectives.</p>
 *
 * <p><u>Noun Value Types</u></p>
 *
 * <p>A parameter that is identified as a noun value is usually required to have a type that is an XML root element (i.e. a class annotated with
 * javax.xml.bind.annotation.XmlRootElement).  There is an exception to this rule: a noun value parameter type can be javax.activation.DataHandler
 * or an array/collection of javax.activation.DataHandler. In the case of javax.activation.DataHandler, the request payload is considered to be of
 * a "custom type" and the DataHandler will become a handler to the InputStream of the request payload. The DataSource of the DataHandler will be
 * an instance of <a href="api/org/codehaus/enunciate/modules/rest/RESTRequestDataSource.html">org.codehaus.enunciate.modules.rest.RESTRequestDataSource</a>.</p>
 *
 * <p>In the case of an array/collection of javax.activation.DataHandler, the REST method will be considered able to handle a multipart file upload as defined
 * in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>. However, in order for a REST request to be able to be parsed as a multipart file upload,
 * an instance of <a href="api/org/codehaus/enunciate/modules/rest/MultipartRequestHandler.html">org.codehaus.enunciate.modules.rest.MultipartRequestHandler</a>
 * must be supplied. The default instance, <a href="api/org/codehaus/enunciate/modules/rest/DefaultMultipartRequestHandler.html">org.codehaus.enunciate.modules.rest.DefaultMultipartRequestHandler</a>,
 * will quietly fail (i.e. the request won't be parsed into a multipart request) unless the necessary <a href="http://commons.apache.org/fileupload/">Commons-FileUpload</a>
 * libraries are found on the classpath.</p>
 *
 * <p>In some cases, you may want to provide your own <a href="api/org/codehaus/enunciate/modules/rest/MultipartRequestHandler.html">org.codehaus.enunciate.modules.rest.MultipartRequestHandler</a>.
 * Enunciate provides another implementation, the <a href="api/org/codehaus/enunciate/modules/rest/StreamingMultipartRequestHandler.html">org.codehaus.enunciate.modules.rest.StreamingMultipartRequestHandler</a>,
 * that uses <a href="http://commons.apache.org/fileupload/">Commons-FileUpload</a> to provide a "streaming" approach to parsing a multipart form upload.  See
 * the docs for details.</p>
 *
 * <h3>Exceptions</h3>
 *
 * <p>By default, an exception that gets thrown during a REST invocation will return an HTTP 500 error.  This
 * can be customized with the <i>org.codehaus.enunciate.rest.annotations.RESTError</i> annotation on the exception
 * that gets thrown.</p>
 *
 * <h1><a name="json">JSON API</a></h1>
 *
 * <p>Each XML endpoint is also published as a JSON endpoint. JSON endpoints support three different serialization methods: "hierarchical",
 * "xmlMapped", and "badgerfish". These values can be passed as request parameters to specify which format is desired. The serialization method
 * to use when none is specified by a request parameter can be specified by using the "defaultJsonSerialization" attribute on the main enunciate
 * element of the Enunciate configuration file. Note the default value is ("xmlMapped").</p>
 *
 * <p>When the JSON serialization is done by converting the XML result to JSON, there are two mapping conventions that can be used. By default, the mapping is
 * done using the "mapped" convention (JSON serialization method "xmlMapped").  The Badgerfish convention is also available (method "badgerfish"). To learn
 * more about the difference between the two convensions, see the <a href="http://jettison.codehaus.org/User%27s+Guide">Jettison user's guide</a>.</p>
 *
 * <p>The "hierarchical" serialization method serializes the result of the operation using
 * <a href="http://xstream.codehaus.org">XStream</a>'s JSON serialization (which leverages the object hierarchy). For more information, see
 * <a href="http://xstream.codehaus.org/json-tutorial.html">XStream JSON Tutorial</a>.  You can also use
 * <a href="http://xstream.codehaus.org/annotations-tutorial.html">XStream's annotations</a> to customize the serialized JSON.</p>
 *
 * <h3>JSONP</h3>
 *
 * <p>You can tell Enunciate to enable a <a href="http://bob.pythonmac.org/archives/2005/12/05/remote-json-jsonp/">JSONP parameter</a> in a JSON request with
 * the use of the <i>org.codehaus.enunciate.rest.annotations.JSONP</i> annotation. When this annotation is applied at the method, class, or package level, any
 * JSON requests can supply a JSONP parameter.  The parameter name can be customized with the annotation.  The default value is "callback".</p>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <p>There are no significant steps in the REST module.  </p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The REST module supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "defaultContentTypeHandler" attribute is used to define the default content type handler when no others are found for a given content type.
 *       The default value is "org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler".</li>
 * </ul>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The REST deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @docFileName module_rest.html
 */
public class RESTDeploymentModule extends FreemarkerDeploymentModule {

  private String defaultContentTypeHandler = JaxbXmlContentHandler.class.getName();

  /**
   * @return "rest"
   */
  @Override
  public String getName() {
    return "rest";
  }

  /**
   * @return A new {@link org.codehaus.enunciate.modules.rest.RESTValidator}.
   */
  @Override
  public Validator getValidator() {
    return new RESTValidator(getEnunciate().getConfig().getContentTypeHandlers());
  }

  /**
   * @return The URL to "spring-servlet.fmt"
   */
  protected URL getProperyNamesTemplateURL() {
    return RESTDeploymentModule.class.getResource("enunciate-rest-parameter-names.properties.fmt");
  }

  @Override
  public void init(Enunciate enunciate) throws EnunciateException {
    super.init(enunciate);

    try {
      JsonSerializationMethod.valueOf(enunciate.getConfig().getDefaultJsonSerialization());
    }
    catch (IllegalArgumentException e) {
      throw new EnunciateException("Illegal JSON serialization method: " + enunciate.getConfig().getDefaultJsonSerialization());
    }
  }

  public void doFreemarkerGenerate() throws EnunciateException, IOException, TemplateException {
    EnunciateFreemarkerModel model = getModel();

    //set up the model with the content type handlers.
    Map<String, String> knownContentTypeHandlers = new TreeMap<String, String>();
    knownContentTypeHandlers.put("text/xml", JaxbXmlContentHandler.class.getName());
    knownContentTypeHandlers.put("application/xml", JaxbXmlContentHandler.class.getName());
    knownContentTypeHandlers.put("application/json", JsonContentHandler.class.getName());
    for (ContentTypeHandler handler : model.getContentTypeHandlers()) {
      for (String contentType : handler.getSupportedContentTypes()) {
        knownContentTypeHandlers.put(contentType, handler.getQualifiedName());
      }
    }
    model.put("configuredContentTypeHandlers", model.getEnunciateConfig().getContentTypeHandlers());
    model.put("knownContentTypeHandlers", knownContentTypeHandlers);
    model.put("defaultContentTypeHandler", getDefaultContentTypeHandler());

    //populate the content-type-to-handler map.
    File paramNameFile = new File(getGenerateDir(), "enunciate-rest-parameter-names.properties");
    if (!enunciate.isUpToDateWithSources(paramNameFile)) {
      processTemplate(getProperyNamesTemplateURL(), model);
    }
    else {
      info("Skipping generation of REST parameter names as everything appears up-to-date...");
    }
    
    enunciate.setProperty("rest.parameter.names", paramNameFile);
  }

  /**
   * The default conent type handler to use (when no others are configured).
   *
   * @return The default conent type handler to use (when no others are configured).
   */
  public String getDefaultContentTypeHandler() {
    return defaultContentTypeHandler;
  }

  /**
   * The default conent type handler to use (when no others are configured).
   *
   * @param defaultContentTypeHandler The default conent type handler to use (when no others are configured).
   */
  public void setDefaultContentTypeHandler(String defaultContentTypeHandler) {
    this.defaultContentTypeHandler = defaultContentTypeHandler;
  }
}

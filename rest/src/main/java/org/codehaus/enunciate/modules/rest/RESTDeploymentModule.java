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

import freemarker.template.TemplateException;
import org.apache.commons.digester.RuleSet;
import org.codehaus.enunciate.EnunciateException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.rest.ContentTypeHandler;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.contract.rest.RESTNoun;
import org.codehaus.enunciate.contract.validation.Validator;
import org.codehaus.enunciate.main.webapp.BaseWebAppFragment;
import org.codehaus.enunciate.main.webapp.WebAppComponent;
import org.codehaus.enunciate.modules.FreemarkerDeploymentModule;
import org.codehaus.enunciate.modules.ProjectExtensionModule;
import org.codehaus.enunciate.modules.rest.config.RESTRuleSet;
import org.codehaus.enunciate.modules.rest.json.JsonContentHandler;
import org.codehaus.enunciate.modules.rest.json.JsonSerializationMethod;
import org.codehaus.enunciate.modules.rest.json.XStreamReferenceAction;
import org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * <h1>REST Module</h1>
 *
 * <p>The REST module compiles and validates the REST API. The order of the REST deployment module is 0, as it doesn't depend on any artifacts exported
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
 * <p><i>It should be noted that this documentation applies to the Enunciate-specific REST model.  Enunciate also supports
 * the <a href="https://jsr311.dev.java.net/">JAX-RS</a> REST model, which was developed and released separately from the
 * Enunciate REST model. The two models are different, each with their own advantages and disadvantages, but now that the
 * JAX-RS spec has been finalized, most developers will want to use the "standard" JAX-RS model.  However, if you need
 * better support for multipart file upload and documentation of errors, you may still want to consider using Enunciate's
 * REST model.</i></p>
 *
 * <p>We start by defining a model for the REST API.  A REST API is comprised of a set of <i>resources</i>
 * on which a constrained set of <i>operations</i> can act.  Borrowing terms from english grammar, Enunciate
 * identifies each REST resource as a <i>noun</i> (with an associated <i>noun context</i>) and the REST operations
 * as <i>verbs</i>.  Because the REST API is to be deployed using HTTP, Enunciate constrains the set of verbs to
 * the set {<i>create</i>, <i>read</i>, <i>update</i>, <i>delete</i>}, mapping to the HTTP verbs 
 * {<i>PUT</i>, <i>GET</i>, <i>POST</i>, <i>DELETE</i>}, respectively.</p>
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
 * <h3>Noun Context</h3>
 *
 * <p>A noun can be qualified by a noun context.  The noun context can be thought of as a "grouping" of nouns.
 * Perhaps, as an admittedly contrived example, we were to have two separate resources for the noun "rectangle",
 * say "wide" and "tall". The "rectangle" to which those two contexts could be applied qualifies two different "rectangle"
 * nouns.</p>
 *
 * <h3>Noun Context Parameters</h3>
 *
 * <p>A noun context parameter (or just "context parameter" for short) is a parameter that is defined by the noun context. For example, if we wanted to identify
 * a specific user of a specific group, we could identify the "group id" as a context parameter, the user as the noun, and the user id as the proper
 * noun.</p>
 *
 * <a name="contentTypes">&nbsp;</a>
 * <h3>Content Types</h3>
 *
 * <p>Each REST noun is represented by a set of content types (i.e. MIME types).  By default, Enunciate represents each noun with both the "application/xml"
 * content type and "application/json" content type (corresponding to XML and JSON representations of the noun). You can also apply other content types to each
 * noun (and optionally disable the default content types) using the <i>org.codehaus.enunciate.rest.annotations.ContentType</i> annotation. This annotation
 * can be applied at the method level, type level, and package level (in that priority order).</p>
 *
 * <p>Associated with each content type is (1) a "content type id" and (2) a "content type handler".  By default, the content type id is the "subtype" of the
 * content type. For example, the default id of "application/xml" is "xml", the default id of "application/json" is "json", and the default id for
 * "application/atom+xml" is "atom+xml".  The content type id is used to identify the content type for a resource on the URL. For example, the content type
 * with content type id "xml" for a resource "circle" will be accessed at the relative URL "/xml/circle".</p>
 *
 * <p>Content type ids can be cofigured with the enunciate configuration file. Because content type ids relate to the location of endpoints, they are considered
 * application-level configuration (not module-level configuration).  The configuration for content type ids occurs in the "services" element of the main
 * Enunciate configuration file.  Here is an example:</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;services&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;rest&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;content-types&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;content-type type="..." id="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;content-type type="..." id="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/content-types&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/rest&gt;
 * &nbsp;&nbsp;&lt;/services&gt;
 * &lt;/enunciate&gt;
 * </code>
 *
 * <p>Content type handlers contain the logic for marshalling a Java object to/from a given content type. There must be one (and only one) content type handler
 * for each content type.  By default, Enunciate provides content type handlers for XML ("application/xml" and "text/xml") and JSON ("application/json").  You
 * can specify a content type handler by annotating the class declaration with <i>@org.codehaus.enunciate.rest.annotations.ContentTypeHandler</i>, which you can
 * use to identify the applicable content types.  You can also specify a content type handler using rest module configuration (see below).  If no content type handler for
 * a content type is specified via type declaration nor by configuration, a configurable (see below) default content type handler will be used.  Without
 * configuration, the default content handler will be <i>org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler</i> (the XML content handler).</p>
 *
 * <p>Content type handlers supported by the module must implement <i>org.codehaus.enunciate.modules.rest.RESTRequestContentTypeHandler</i> and have an accessible
 * no-arg constructor.</p>
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
 *   <li>A noun value must be either an xml root element (not just a complex type) or javax.activation.DataHandler.</li>
 *   <li>A return type must be either a root element or javax.activation.DataHandler.</li>
 *   <li>Noun context parameters must be simple types.</li>
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
 * <p>By default, the return type of the Java method defines the payload of the response.  In order support the default content type handlers (see
 * "Content Types" above), the return type of the Java method must be compatible with JAXB. This means that the return type should be a class (as opposed to an
 * interface) and unless the return type is annotated with @XmlRootElement, the element name of the XML will be the name of the noun. The exception to this
 * is that Enunciate also allows the return type of a Java method to be javax.activation.DataHandler, which defines its own payload and content type. In the
 * case where the return type is a DataHandler, consider using the @ContentType (see "Content Types" above) annotation to specify which content types the
 * DataHandler supports and which content types are not supported.</p>
 *
 * <p>Since it is possible to provide your own content type handlers that override the default JAXB content type handlers, you can disable the requirement
 * that the return type be JAXB-compatible in the <a href="#config">configuration</a> of this module.</p>
 *
 * <h3>Java Method Parameters</h3>
 *
 * <p>A parameter to a method can be a proper noun, an adjective, a context parameter, or a noun value.  By default, a parameter is mapped
 * as an adjective.  By default, the name of the adjective is the name of the parameter.  Parameters can be customized with the
 * <i>org.codehaus.enunciate.rest.annotations.Adjective</i>, <i>org.codehaus.enunciate.rest.annotations.NounValue</i>,
 * <i>org.codehaus.enunciate.rest.annotations.ContextParameter</i>, and <i>org.codehaus.enunciate.rest.annotations.ProperNoun</i> annotations.</p>
 *
 * <p><u>Complex Adjectives</u></p>
 *
 * <p>There may be cases where a REST request may accept a large number of adjectives (HTTP parameters).  If this is the case, the corresponding Java method
 * could start to get unwieldy because of the number of parameters on the method.  To address this inconvenience, Enunciate supports the concept of a
 * "complex adjective".  An adjective is marked as complex with by setting "complex=true" on the @Adjective annotation.  The type of a complex adjective is
 * expected to be a simple bean with an accessible no-arg constructor. In the case of a complex adjective, the properties of the bean
 * define the names and types of the adjectives.</p>
 *
 * <p><u>Noun Value Types</u></p>
 *
 * <p>By default, a method parameter that is identified as a noun value is deserialized from the request body from the content type handler (see "Content Types"
 * above).  This means that the noun value parameter must be compatible with JAXB (i.e. must not be an interface) to support the default content type handlers.
 * Like the return type, there is an exception to this rule: a noun value parameter can be of type javax.activation.DataHandler or an array/collection of
 * javax.activation.DataHandler. In the case of javax.activation.DataHandler, the request payload is considered to be of a "custom type" and the DataHandler
 * will become a handler to the InputStream of the request payload. The DataSource of the DataHandler will be an instance of
 * <i>org.codehaus.enunciate.modules.rest.RESTRequestDataSource</i>.</p>
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
 * the JavaDocs for details.</p>
 *
 * <h3>Exceptions</h3>
 *
 * <p>By default, an exception that gets thrown during a REST invocation will return an HTTP 500 error.  This
 * can be customized with the <i>org.codehaus.enunciate.rest.annotations.RESTError</i> annotation on the exception
 * that gets thrown. The body of the error response can be customized by annotating an accessor method on the exception with
 * <i>org.codehaus.enunciate.rest.annotations.RESTErrorBody</i>.  The object that is returns from this method will be written to the response in the same manner
 * that the return type of the method is serialized (see "Java Return Types" above).</p>
 *
 * <h1><a name="json">JSON API</a></h1>
 *
 * <p>By default, each resource is available as both the "application/xml" content type and "application/json" content type. The default JSON content type
 * handler supports three different serialization methods: "hierarchical", "xmlMapped", and "badgerfish". These values can be passed as request parameters
 * to specify which format is desired. The serialization method to use when none is specified by a request parameter can be specified by using the
 * "defaultJsonSerialization" attribute on the REST module configuration element. Note the default value is ("xmlMapped").</p>
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
 * <h3>generate</h3>
 *
 * <p>The generate step of the REST module sets up the known content type handlers and writes out a metadata file that contains
 * the parameter names of each REST method.</p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>The REST module supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "defaultContentTypeHandler" attribute is used to define the default content type handler when no others are found for a given content type.
 *       The default value is "org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler".</li>
 *   <li>The "requireJAXBCompatibility" attribute is used to disable the requirement that return types and noun values be JAXB-compatible. Since Enunciate
 *       uses JAXB by default for marshalling and umarshalling, this requirement should only be disabled if you have provided your own content type
 *       handlers.</li>
 *   <li>The "handlerExceptionResolver" attribute is used to specify the handler exception resolver to use to map REST exceptions to the error body.</li>
 *   <li>The "conversionSupport" attribute is used to specify the converter to use to convert request strings to the appropriate java types.</li>
 * </ul>
 *
 * <h3>The "json" element</h3>
 *
 * <p>The "json" child element of the rest module configuration element supports configuration options for JSON serialization. The following attributes
 * are supported on this element:</p>
 *
 * <ul>
 *   <li>The "defaultJsonSerialization" attribute is used to define the default default JSON serialization method. Default: "xmlMapped".</li>
 *   <li>The "xstreamReferenceAction" attribute is used to define the action that XStream takes with object references.  Possible values are "no_references",
 *       "id_references", "relative_references", "absolute_references". Default value is "relative_references". For more information, see
 *       <a href="http://xstream.codehaus.org/graphs.html">the XStream documentation.</a></li>
 * </ul>
 *
 * <h3>The "content-type-handlers" element</h3>
 *
 * <p>The "content-type-handlers" child element of the rest module configuration element supports configuration of the content type handler for a specific
 * content type. Each "handler" child element identifies the handler.  The "contentType" attribute identifies the content type.  The "class" element is the fully-qualified classname of the content handler.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The REST deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 * @author Jason Holmes
 * @docFileName module_rest.html
 */
public class RESTDeploymentModule extends FreemarkerDeploymentModule implements ProjectExtensionModule {

  private String defaultContentTypeHandler = JaxbXmlContentHandler.class.getName();
  private String xstreamReferenceAction = XStreamReferenceAction.relative_references.toString();
  private Map<String, String> contentTypeHandlers = new TreeMap<String, String>();
  private String defaultJsonSerialization = JsonSerializationMethod.xmlMapped.toString();
  private boolean requireJAXBCompatibility = true;
  private String handlerExceptionResolver = RESTResourceExceptionHandler.class.getName();
  private String conversionSupport = DefaultConverter.class.getName();
 
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
    return new RESTValidator(getContentTypeHandlers(), isRequireJAXBCompatibility());
  }

  /**
   * @return The URL to "spring-servlet.fmt"
   */
  protected URL getProperyNamesTemplateURL() {
    return RESTDeploymentModule.class.getResource("enunciate-rest-parameter-names.properties.fmt");
  }

  /**
   * @return The URL to "rest-servlet.xml.fmt"
   */
  protected URL getRestServletTemplateURL() {
    return RESTDeploymentModule.class.getResource("rest-servlet.xml.fmt");
  }

  // Inherited.
  @Override
  public void initModel(EnunciateFreemarkerModel model) {
    super.initModel(model);

    if (!isDisabled()) {
      Map<RESTNoun, Set<String>> nouns2contentTypes = model.getNounsToContentTypes();
      Map<String, String> contentTypes2Ids = model.getContentTypesToIds();
      contentTypes2Ids.put("application/json", "json"); //it's assumed we've got some json provider on the classpath...

      for (Map.Entry<RESTNoun, List<RESTMethod>> nounMethodEntry : model.getNounsToRESTMethods().entrySet()) {
        Map<String, Set<String>> subcontexts = new HashMap<String, Set<String>>();
        RESTNoun restNoun = nounMethodEntry.getKey();
        subcontexts.put(null, new TreeSet<String>(Arrays.asList(getRestSubcontext())));
        for (String contentType : nouns2contentTypes.get(restNoun)) {
          String contentTypeId = contentTypes2Ids.get(contentType);
          if (contentTypeId != null) {
            subcontexts.put(contentType, new TreeSet<String>(Arrays.asList("/" + contentTypeId)));
          }
        }

        for (RESTMethod restMethod : nounMethodEntry.getValue()) {
          restMethod.putMetaData("defaultSubcontext", getRestSubcontext());
          restMethod.putMetaData("subcontexts", subcontexts);
        }
      }
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
    model.put("configuredContentTypeHandlers", getContentTypeHandlers());
    model.put("knownContentTypeHandlers", knownContentTypeHandlers);
    model.put("defaultContentTypeHandler", getDefaultContentTypeHandler());
    model.put("defaultJsonSerialization", getDefaultJsonSerialization());
    model.put("xstreamReferenceAction", getXstreamReferenceAction());
    model.put("restSubcontext", getRestSubcontext());
    model.put("handlerExceptionResolver", getHandlerExceptionResolver());
    model.put("conversionSupport", getConversionSupport());
 
    //populate the content-type-to-handler map.
    File paramNameFile = new File(getGenerateDir(), "enunciate-rest-parameter-names.properties");
    if (!enunciate.isUpToDateWithSources(paramNameFile)) {
      processTemplate(getProperyNamesTemplateURL(), model);
    }
    else {
      info("Skipping generation of REST parameter names as everything appears up-to-date...");
    }

    File restServletXml = new File(getGenerateDir(), "rest-servlet.xml");
    if (!enunciate.isUpToDateWithSources(restServletXml)) {
      processTemplate(getRestServletTemplateURL(), model);
    }
    else {
      info("Skipping generation of the REST servlet configuration as everything appears up-to-date...");
    }
  }

  protected String getRestSubcontext() {
    String restSubcontext = getEnunciate().getConfig().getDefaultRestSubcontext();
    //todo: override default rest subcontext?
    return restSubcontext;
  }

  @Override
  protected void doBuild() throws EnunciateException, IOException {
    super.doBuild();

    File webappDir = getBuildDir();
    webappDir.mkdirs();
    File webinf = new File(webappDir, "WEB-INF");
    getEnunciate().copyFile(new File(getGenerateDir(), "rest-servlet.xml"), new File(webinf, "rest-servlet.xml"));
    File webinfClasses = new File(webinf, "classes");
    getEnunciate().copyFile(new File(getGenerateDir(), "enunciate-rest-parameter-names.properties"), new File(webinfClasses, "enunciate-rest-parameter-names.properties"));

    BaseWebAppFragment webappFragment = new BaseWebAppFragment(getName());
    webappFragment.setBaseDir(webappDir);
    WebAppComponent servletComponent = new WebAppComponent();
    servletComponent.setName("rest");
    servletComponent.setClassname(DispatcherServlet.class.getName());
    TreeSet<String> urlMappings = new TreeSet<String>();
    Map<RESTNoun, Set<String>> nouns2contentTypes = getModel().getNounsToContentTypes();
    Map<String, String> contentTypes2Ids = getModel().getContentTypesToIds();
    for (RESTNoun restNoun : nouns2contentTypes.keySet()) {
      for (String servletPattern : restNoun.getServletPatterns()) {
        urlMappings.add(getRestSubcontext() + servletPattern);
        for (String contentType : nouns2contentTypes.get(restNoun)) {
          String contentTypeId = contentTypes2Ids.get(contentType);
          if (contentTypeId != null) {
            urlMappings.add("/" + contentTypeId + servletPattern);
          }
          else {
            debug("No content id for type '%s'.  REST noun %s will not be mounted for that content type.", contentType, restNoun);
          }
        }
      }
    }
    servletComponent.setUrlMappings(urlMappings);
    webappFragment.setServlets(Arrays.asList(servletComponent));
    getEnunciate().addWebAppFragment(webappFragment);
  }

  /**
   * The content handlers (content type to content handler).
   *
   * @return The content handlers.
   */
  public Map<String, String> getContentTypeHandlers() {
    return contentTypeHandlers;
  }

  /**
   * Put a content type handler.
   *
   * @param contentType The content type.
   * @param contentHandler The content handler.
   */
  public void putContentTypeHandler(String contentType, String contentHandler) {
    if (contentType == null) {
      throw new IllegalArgumentException("A content type must be supplied.");
    }
    if (contentHandler == null) {
      throw new IllegalArgumentException("A content type handler must be supplied.");
    }

    this.contentTypeHandlers.put(contentType, contentHandler);
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

  /**
   * The xstream reference action.
   *
   * @return The xstream reference action.
   */
  public String getXstreamReferenceAction() {
    return xstreamReferenceAction;
  }

  /**
   * Set the reference action for XStream references.
   *
   * @param xstreamReferenceAction The xstream reference action.
   */
  public void setXstreamReferenceAction(String xstreamReferenceAction) {
    this.xstreamReferenceAction = XStreamReferenceAction.valueOf(xstreamReferenceAction).toString();
  }

  /**
   * The default JSON serialization method.
   *
   * @return The default JSON serialization method.
   */
  public String getDefaultJsonSerialization() {
    return defaultJsonSerialization;
  }

  /**
   * The default JSON serialization method.
   *
   * @param defaultJsonSerialization The default JSON serialization method.
   */
  public void setDefaultJsonSerialization(String defaultJsonSerialization) {
    this.defaultJsonSerialization = JsonSerializationMethod.valueOf(defaultJsonSerialization).toString();
  }

  /**
   * Whether to require JAXB compatibility with the return types and noun values.
   *
   * @return Whether to require JAXB compatibility with the return types and noun values.
   */
  public boolean isRequireJAXBCompatibility() {
    return requireJAXBCompatibility;
  }

  /**
   * Whether to require JAXB compatibility with the return types and noun values.
   *
   * @param requireJAXBCompatibility Whether to require JAXB compatibility with the return types and noun values.
   */
  public void setRequireJAXBCompatibility(boolean requireJAXBCompatibility) {
    this.requireJAXBCompatibility = requireJAXBCompatibility;
  }

	/**
	 * The exception handler to use for marshalling exceptions to the response
	 *
	 * @return The exception handler to use for marshalling exceptions to the response
	 */
	public String getHandlerExceptionResolver() {
		return handlerExceptionResolver;
	}

	/**
	 * The exception handler to use for marshalling exceptions to the response
	 *
	 * @param handlerExceptionResolver The exception handler to use for marshalling exceptions to the response
	 */
	public void setHandlerExceptionResolver(String handlerExceptionResolver) {
		this.handlerExceptionResolver = handlerExceptionResolver;
	}
	
  /**
   * The converter to use for converting from Strings to types
   * 
   * @return The converter to use for converting from Strings to types
   */
  public String getConversionSupport() {
		return conversionSupport;
	}

  /**
   * The converter to use for converting from Strings to types
   * 
   * @return The converter to use for converting from Strings to types
   */
  public void setConversionSupport(String conversionSupport) {
		this.conversionSupport = conversionSupport;
	}

@Override
  public RuleSet getConfigurationRules() {
    return new RESTRuleSet();
  }

  // Inherited.
  @Override
  public boolean isDisabled() {
    if (super.isDisabled()) {
      return true;
    }
    else if (getModelInternal() != null && getModelInternal().getRESTEndpoints().isEmpty()) {
      debug("REST module is disabled because there are no REST endpoints.");
      return true;
    }

    return false;

  }

  public List<File> getProjectSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectTestSources() {
    return Collections.emptyList();
  }

  public List<File> getProjectResourceDirectories() {
    return Arrays.asList(getGenerateDir());
  }

  public List<File> getProjectTestResourceDirectories() {
    return Collections.emptyList();
  }
}

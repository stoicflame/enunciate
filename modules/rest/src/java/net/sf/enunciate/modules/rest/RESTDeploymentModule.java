package net.sf.enunciate.modules.rest;

import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.EnunciateException;

import java.io.IOException;

/**
 * <h1>Introduction</h1>
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
 * has a name and one or more values.  In terms of HTTP, adjectives are passed as an HTTP parameters.</p>
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
 * </ul>
 *
 * <h1><a name="java2rest">Mapping Java to a REST API</a></h1>
 *
 * <h3>Java Types</h3>
 *
 * <p>The <i>net.sf.enunciate.rest.annotations.RESTEndpoint</i> annotation is used on a Java type (i.e. class or interface)
 * to indicate that it contains methods that will service REST endpoints.  This is used simply to indicate to
 * the engine that the methods on the annotated class or interface should be searched for their nouns and verbs.
 * Only if a method is annotated with <i>net.sf.enunciate.rest.annotations.Verb</i> will it service a REST endpoint
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
 * verb with the <i>net.sf.enunciate.rest.annotations.Verb</i> annotation.  A method that is not assigned a verb will
 * not be considered to service a REST endpoint.</p>
 *
 * <p>A method that is assigned a verb will be assigned a noun as well.  By default, the noun is the name of the method.  The
 * noun can also be customized with the <i>net.sf.enunciate.rest.annotations.Noun</i> annotation.</p>
 *
 * <h3>Java Method Parameters</h3>
 *
 * <p>A parameter to a method can be a proper noun, an adjective, or a noun value.  By default, a parameter is mapped
 * as an adjective.  The name of the adjective by default is arg<i>i</i>, where <i>i</i> is the parameter index.  Parameters
 * can be customized with the <i>net.sf.enunciate.rest.annotations.Adjective</i>, <i>net.sf.enunciate.rest.annotations.NounValue</i>,
 * and <i>net.sf.enunciate.rest.annotations.ProperNoun</i> annotations.</p>
 *
 * <h3>Exceptions</h3>
 *
 * <p>By default, an exception that gets thrown during a REST invocation will return an HTTP 500 error.  This
 * can be customized with the <i>net.sf.enunciate.rest.annotations.RESTError</i> annotation on the exception
 * that gets thrown.</p>
 *
 * <h1><a name="steps">Steps</a></h1>
 *
 * <p>There are no significant steps in the REST module.  </p>
 *
 * <h1><a name="config">Configuration</a></h1>
 *
 * <p>There are no configuration options for the REST deployment module.</p>
 *
 * <h1><a name="artifacts">Artifacts</a></h1>
 *
 * <p>The REST deployment module exports no artifacts.</p>
 *
 * @author Ryan Heaton
 */
public class RESTDeploymentModule extends BasicDeploymentModule {

  /**
   * @return "rest"
   */
  @Override
  public String getName() {
    return "rest";
  }

  /**
   * @return A new {@link net.sf.enunciate.modules.rest.RESTValidator}.
   */
  @Override
  public Validator getValidator() {
    return new RESTValidator();
  }

  @Override
  protected void doGenerate() throws EnunciateException, IOException {
    //todo: export the parameter names.
    //todo: export the namespace prefixes for Jettison export.
    super.doGenerate();
  }
}

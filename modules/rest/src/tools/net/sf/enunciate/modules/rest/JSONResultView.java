package net.sf.enunciate.modules.rest;

import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;
import java.util.Map;

/**
 * A JSON view of a REST result.
 *
 * @author Ryan Heaton
 */
public class JSONResultView extends RESTResultView {

  private final Map<String, String> ns2prefix;

  /**
   * Construct a view for the result of a REST operation.
   *
   * @param operation The operation.
   * @param result    The result.
   * @param ns2prefix The map of namespaces to prefixes.
   */
  public JSONResultView(RESTOperation operation, Object result, Map<String, String> ns2prefix) {
    super(operation, result);
    this.ns2prefix = ns2prefix;
  }

  /**
   * Marshals the result as a JSON response.
   *
   * @param marshaller The marshaller.
   * @param request The request.
   * @param response The response.
   */
  @Override
  protected void marshal(Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    XMLStreamWriter streamWriter = (request.getParameter("badgerfish") == null) ?
      new MappedXMLOutputFactory(this.ns2prefix).createXMLStreamWriter(response.getOutputStream()) :
      new BadgerFishXMLOutputFactory().createXMLStreamWriter(response.getOutputStream());
    response.setContentType("application/json");
    marshaller.marshal(getResult(), streamWriter);
  }
}

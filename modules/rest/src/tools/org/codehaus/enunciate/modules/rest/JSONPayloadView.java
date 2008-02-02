package org.codehaus.enunciate.modules.rest;

import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletOutputStream;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.Map;
import java.io.InputStream;
import java.io.IOException;

/**
 * JSON view for a REST payload. Assumes the payload is for XML data.
 *
 * @author Ryan Heaton
 */
public class JSONPayloadView extends RESTPayloadView {

  public JSONPayloadView(RESTOperation operation, Object payload, Map<String, String> ns2prefix) {
    super(operation, payload, ns2prefix);
  }

  @Override
  protected void marshalPayloadStream(InputStream payloadStream, HttpServletRequest request, HttpServletResponse response) throws IOException, XMLStreamException {
    ServletOutputStream out = response.getOutputStream();
    XMLStreamWriter streamWriter = (request.getParameter("badgerfish") == null) ?
      new MappedXMLOutputFactory(getNamespaces2Prefixes()).createXMLStreamWriter(out) :
      new BadgerFishXMLOutputFactory().createXMLStreamWriter(out);
    String callbackName = null;
    String jsonpParameter = getOperation().getJSONPParameter();
    if (jsonpParameter != null) {
      callbackName = request.getParameter(jsonpParameter);
      if ((callbackName != null) && (callbackName.trim().length() > 0)) {
        out.print(callbackName);
        out.print("(");
      }
      else {
        callbackName = null;
      }
    }

    JSONDataHandlerView.convertXMLStreamToJSON(XMLInputFactory.newInstance().createXMLEventReader(payloadStream), streamWriter);

    if (callbackName != null) {
      out.print(")");
    }

    streamWriter.flush();
    streamWriter.close();
  }

  @Override
  protected String getContentType() {
    return "application/json";
  }
}

package net.sf.enunciate.modules.rest;

import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Marshaller;
import java.util.Map;

/**
 * A view for the result of a REST operation.
 *
 * @author Ryan Heaton
 */
public class RESTResultView implements View {

  private final RESTOperation operation;
  private final Object result;

  /**
   * Construct a view for the result of a REST operation.
   *
   * @param operation The operation.
   * @param result The result.
   */
  public RESTResultView(RESTOperation operation, Object result) {
    this.operation = operation;
    this.result = result;
  }

  /**
   * Renders the XML view of the result.
   *
   * @param map The model.
   * @param request The request.
   * @param response The response.
   * @throws Exception If a problem occurred during serialization.
   */
  public void render(Map map, HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("text/xml");
    Marshaller marshaller = operation.getSerializationContext().createMarshaller();
    marshaller.setAttachmentMarshaller(RESTAttachmentMarshaller.INSTANCE);
    marshaller.marshal(result, response.getOutputStream());
  }
}

package org.codehaus.enunciate.modules.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * View of a REST operation (that returns a  result).
 * 
 * @author Ryan Heaton
 */
public class RESTOperationView<R> extends BasicRESTView {

  public static final String MODEL_RESULT = "result";

  protected final RESTOperation operation;

  public RESTOperationView(RESTOperation operation) {
    this.operation = operation;
  }

  @Override
  protected String getContentType(Map model) {
    String contentType = super.getContentType(model);

    if (contentType == null) {
      R result = (R) model.get(MODEL_RESULT);
      if (result != null) {
        contentType = String.format("%s;charset=%s", getContentType(result), this.operation.getCharset());
      }
    }

    return contentType;
  }

  /**
   * The operation used to render this view.
   *
   * @return The operation used to render this view.
   */
  public RESTOperation getOperation() {
    return operation;
  }

  /**
   * Get the content type for the specified result.
   *
   * @return The content type. @param result The result.
   */
  protected String getContentType(R result) {
    return this.operation.getContentType();
  }

  /**
   * Pulls the result from the model and renders it.
   *
   * @param model The model.
   * @param request The request.
   * @param response The response.
   */
  @Override
  public void renderBody(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    R result = (R) model.get(MODEL_RESULT);

    if (result != null) {
      renderResult(result, request, response);
    }

    response.flushBuffer();
  }

  /**
   * Renders the result of the REST operation.  Default implementation is a no-op.
   *
   * @param result The result.
   * @param request The request.
   * @param response The response.
   */
  protected void renderResult(R result, HttpServletRequest request, HttpServletResponse response) throws Exception {
  }

}

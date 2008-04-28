package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.rest.annotations.ContentTypeHandler;
import org.codehaus.enunciate.modules.rest.RESTRequestContentTypeHandler;
import org.codehaus.enunciate.samples.petclinic.schema.ClinicBrochure;
import org.codehaus.enunciate.samples.petclinic.schema.AnimalBrochure;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.util.Map;
import java.io.InputStream;

/**
 * @author Ryan Heaton
 */
@ContentTypeHandler (
  contentTypes = {"text/plain", "text/html", "application/pdf"}
)
public class BrochureContentTypeHandler implements RESTRequestContentTypeHandler {

  public Object read(HttpServletRequest request) throws Exception {
    throw new UnsupportedOperationException();
  }

  public void write(Object data, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (data instanceof ClinicBrochure) {
      ClinicBrochure brochure = (ClinicBrochure) data;
      if (brochure.getMetaData() != null) {
        for (Map.Entry<String, String> entry : brochure.getMetaData().entrySet()) {
          response.setHeader(entry.getKey(), entry.getValue());
        }

        if (brochure.getData() != null) {
          brochure.getData().writeTo(response.getOutputStream());
        }
      }
    }
    else if (data instanceof AnimalBrochure) {
      AnimalBrochure brochure = (AnimalBrochure) data;
      InputStream in = brochure.getContent();
      ServletOutputStream out = response.getOutputStream();
      byte[] bytes = new byte[1024];
      int len = in.read(bytes);
      while (len >= 0) {
        out.write(bytes, 0, len);
        len = in.read(bytes);
      }
      out.flush();
      in.close();
      out.close();
    }
  }
}

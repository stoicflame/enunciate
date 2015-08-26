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

package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.api.resources.StatusCode;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResponsesOfMethod implements TemplateMethodModelEx {

  private static Set<String> DEFAULT_201_METHODS = new TreeSet<String>(Arrays.asList("POST", "PUT", "DELETE"));

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The responsesOf method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap(from);
    if (unwrapped instanceof Method) {
      Method method = (Method) unwrapped;
      ArrayList<SwaggerResponse> responses = new ArrayList<SwaggerResponse>();
      List<? extends Parameter> headers = method.getResponseHeaders();

      DataTypeReference dataType = findBestDataType(method);

      boolean has20xResponse = false;
      Map<Integer, String> codes = new TreeMap<Integer, String>();
      if (method.getResponseCodes() != null) {
        for (StatusCode code : method.getResponseCodes()) {
          codes.put(code.getCode(), code.getCondition());
          has20xResponse |= (code.getCode() >= 200 && code.getCode() < 300);
        }
      }

      if (codes.isEmpty() || !has20xResponse) {
        int code = DEFAULT_201_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 201 : 200;
        codes.put(code, "Success");
      }

      for (Map.Entry<Integer, String> code : codes.entrySet()) {
        responses.add(new SwaggerResponse(code.getKey(), dataType, headers, code.getValue()));
      }


      return responses;
    }

    throw new TemplateModelException("No responses for: " + unwrapped);
  }

  private DataTypeReference findBestDataType(Method method) {
    if (method.getResponseEntity() != null) {
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getResponseEntity().getMediaTypes()) {
        if (mediaTypeDescriptor.getSyntax().toLowerCase().contains("json")) {
          return mediaTypeDescriptor.getDataType();
        }
      }

      //didn't find json; try again.
      for (MediaTypeDescriptor mediaTypeDescriptor : method.getResponseEntity().getMediaTypes()) {
        return mediaTypeDescriptor.getDataType();
      }
    }

    return null;
  }
}
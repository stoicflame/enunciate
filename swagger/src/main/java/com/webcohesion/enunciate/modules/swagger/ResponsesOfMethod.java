/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.api.resources.StatusCode;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResponsesOfMethod implements TemplateMethodModelEx {

  private static final Set<String> DEFAULT_201_METHODS = new TreeSet<>(Collections.singletonList("POST"));
  private static final Set<String> DEFAULT_204_METHODS = new TreeSet<>(Arrays.asList("PATCH", "PUT", "DELETE"));

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The responsesOf method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);
    if (unwrapped instanceof Method) {
      Method method = (Method) unwrapped;
      TreeSet<SwaggerResponse> responses = new TreeSet<>(Comparator.comparingInt(SwaggerResponse::getCode));

      List<? extends Parameter> successHeaders = method.getResponseHeaders();
      Entity responseEntity = method.getResponseEntity();
      boolean successResponseFound = false;
      if (method.getResponseCodes() != null) {
        for (StatusCode code : method.getResponseCodes()) {
          boolean successResponse = code.getCode() >= 200 && code.getCode() < 300;
          List<? extends Parameter> headers = successResponse ? successHeaders : Collections.emptyList();
          responses.add(new SwaggerResponse(code.getCode(), code.getMediaTypes(), headers, code.getCondition()));
          successResponseFound |= successResponse;
        }
      }

      if (!successResponseFound) {
        int code = DEFAULT_201_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 201 : DEFAULT_204_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 204 : 200;
        String description = responseEntity != null ? responseEntity.getDescription() : "Success";
        responses.add(new SwaggerResponse(code, responseEntity != null ? responseEntity.getMediaTypes() : Collections.emptyList(), successHeaders, description));
      }

      return responses;
    }

    throw new TemplateModelException("No responses for: " + unwrapped);
  }

}
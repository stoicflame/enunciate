/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
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

  private static Set<String> DEFAULT_201_METHODS = new TreeSet<String>(Collections.singletonList("POST"));
  private static Set<String> DEFAULT_204_METHODS = new TreeSet<String>(Arrays.asList("PATCH", "PUT", "DELETE"));

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The responsesOf method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap(from);
    if (unwrapped instanceof Method) {
      Method method = (Method) unwrapped;
      ArrayList<SwaggerResponse> responses = new ArrayList<SwaggerResponse>();

      List<? extends Parameter> successHeaders = method.getResponseHeaders();
      Entity responseEntity = method.getResponseEntity();
      DataTypeReference successDataType = FindBestDataTypeMethod.findBestDataType(responseEntity);
      boolean successResponseFound = false;
      if (method.getResponseCodes() != null) {
        for (StatusCode code : method.getResponseCodes()) {
          boolean successResponse = code.getCode() >= 200 && code.getCode() < 300;
          DataTypeReference dataType = FindBestDataTypeMethod.findBestDataType(code.getMediaTypes());
          dataType = dataType == null && successResponse ? successDataType : dataType;
          List<? extends Parameter> headers = successResponse ? successHeaders : Collections.<Parameter>emptyList();
          responses.add(new SwaggerResponse(code.getCode(), dataType, headers, code.getCondition()));
          successResponseFound |= successResponse;
        }
      }

      if (!successResponseFound) {
        int code = DEFAULT_201_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 201 : DEFAULT_204_METHODS.contains(method.getHttpMethod().toUpperCase()) ? 204 : 200;
        String description = responseEntity != null ? responseEntity.getDescription() : "Success";
        responses.add(new SwaggerResponse(code, successDataType, successHeaders, description));
      }

      return responses;
    }

    throw new TemplateModelException("No responses for: " + unwrapped);
  }

}
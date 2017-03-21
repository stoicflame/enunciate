package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.api.resources.StatusCode;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.ResponseCode;
import com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class StatusCodeImpl implements StatusCode {

  private final ResponseCode responseCode;
  private final ApiRegistrationContext registrationContext;

  public StatusCodeImpl(ResponseCode responseCode, ApiRegistrationContext registrationContext) {
    this.responseCode = responseCode;
    this.registrationContext = registrationContext;
  }

  @Override
  public int getCode() {
    return responseCode.getCode();
  }

  @Override
  public String getCondition() {
    return responseCode.getCondition();
  }

  public Map<String, String> getAdditionalHeaders() {
    return responseCode.getAdditionalHeaders();
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>();
    DecoratedTypeMirror type = this.responseCode.getType();
    if (type != null) {
      ResourceMethod resourceMethod = this.responseCode.getResourceMethod();
      Set<MediaType> produces = resourceMethod.getProducesMediaTypes();
      for (com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType mt : produces) {
        for (Syntax syntax : resourceMethod.getContext().getContext().getApiRegistry().getSyntaxes(registrationContext)) {
          MediaTypeDescriptor descriptor = syntax.findMediaTypeDescriptor(mt.getMediaType(), type);
          if (descriptor != null) {
            mts.add(new MediaTypeDescriptorImpl(descriptor, mt, descriptor.getExample()));
          }
        }
      }
    }
    return mts;
  }

  public String getCodeString() {
    String codeString = this.getCode() + " ";
    switch (getCode()) {
      case 100:
        codeString += "Continue";
        break;
      case 101:
        codeString += "Switching Protocols";
        break;
      case 102:
        codeString += "Processing";
        break;
      case 200:
        codeString += "OK";
        break;
      case 201:
        codeString += "Created";
        break;
      case 202:
        codeString += "Accepted";
        break;
      case 203:
        codeString += "Non-Authoritative Information";
        break;
      case 204:
        codeString += "No Content";
        break;
      case 205:
        codeString += "Reset Content";
        break;
      case 206:
        codeString += "Partial Content";
        break;
      case 207:
        codeString += "Multi-Status";
        break;
      case 208:
        codeString += "Already Reported";
        break;
      case 226:
        codeString += "IM Used";
        break;
      case 300:
        codeString += "Multiple Choices";
        break;
      case 301:
        codeString += "Moved Permanently";
        break;
      case 302:
        codeString += "Found";
        break;
      case 303:
        codeString += "See Other";
        break;
      case 304:
        codeString += "Not Modified";
        break;
      case 305:
        codeString += "Use Proxy";
        break;
      case 307:
        codeString += "Temporary Redirect";
        break;
      case 308:
        codeString += "Permanent Redirect";
        break;
      case 400:
        codeString += "Bad Request";
        break;
      case 401:
        codeString += "Unauthorized";
        break;
      case 402:
        codeString += "Payment Required";
        break;
      case 403:
        codeString += "Forbidden";
        break;
      case 404:
        codeString += "Not Found";
        break;
      case 405:
        codeString += "Method Not Allowed";
        break;
      case 406:
        codeString += "Not Acceptable";
        break;
      case 407:
        codeString += "Proxy Authentication Required";
        break;
      case 408:
        codeString += "Request Time-out";
        break;
      case 409:
        codeString += "Conflict";
        break;
      case 410:
        codeString += "Gone";
        break;
      case 411:
        codeString += "Length Required";
        break;
      case 412:
        codeString += "Precondition Failed";
        break;
      case 413:
        codeString += "Request Entity Too Large";
        break;
      case 414:
        codeString += "Request-URL Too Long";
        break;
      case 415:
        codeString += "Unsupported Media Type";
        break;
      case 416:
        codeString += "Requested range not satisfiable";
        break;
      case 417:
        codeString += "Expectation Failed";
        break;
      case 420:
        codeString += "Policy Not Fulfilled";
        break;
      case 421:
        codeString += "There are too many connections from your internet address";
        break;
      case 422:
        codeString += "Unprocessable Entity";
        break;
      case 423:
        codeString += "Locked";
        break;
      case 424:
        codeString += "Failed Dependency";
        break;
      case 425:
        codeString += "Unordered Collection";
        break;
      case 426:
        codeString += "Upgrade Required";
        break;
      case 429:
        codeString += "Too Many Requests";
        break;
      case 444:
        codeString += "No Response";
        break;
      case 449:
        codeString += "The request should be retried after doing the appropriate action";
        break;
      case 500:
        codeString += "Internal Server Error";
        break;
      case 501:
        codeString += "Not Implemented";
        break;
      case 502:
        codeString += "Bad Gateway";
        break;
      case 503:
        codeString += "Service Unavailable";
        break;
      case 504:
        codeString += "Gateway Time-out";
        break;
      case 505:
        codeString += "HTTP Version not supported";
        break;
      case 506:
        codeString += "Variant Also Negotiates";
        break;
      case 507:
        codeString += "Insufficient Storage";
        break;
      case 509:
        codeString += "Bandwidth Limit Exceeded";
        break;
      case 510:
        codeString += "Not Extended";
        break;
    }
    return codeString;
  }
}

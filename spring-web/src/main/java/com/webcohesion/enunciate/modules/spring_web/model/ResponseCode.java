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

package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.api.resources.StatusCode;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ryan Heaton
 */
public class ResponseCode implements StatusCode {

  private int code;
  private String condition;
  private Map<String, String> additionalHeaders = new TreeMap<String, String>();

  
  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getCondition() {
    return condition;
  }

  public void setCondition(String condition) {
    this.condition = condition;
  }

  public Map<String, String> getAdditionalHeaders() {
        return additionalHeaders;
  }

  public void setAdditionalHeader(String key, String value){
      this.additionalHeaders.put(key, value);
  }

  public String getCodeString (){
      String codeString = this.code + " ";
      switch(code){
          case 100: codeString += "Continue"; break;
          case 101: codeString += "Switching Protocols"; break;
          case 102: codeString += "Processing"; break;
          case 200: codeString += "OK"; break;
          case 201: codeString += "Created"; break;
          case 202: codeString += "Accepted"; break;
          case 203: codeString += "Non-Authoritative Information"; break;
          case 204: codeString += "No Content"; break;
          case 205: codeString += "Reset Content"; break;
          case 206: codeString += "Partial Content"; break;
          case 207: codeString += "Multi-Status"; break;
          case 208: codeString += "Already Reported"; break;
          case 226: codeString += "IM Used"; break;
          case 300: codeString += "Multiple Choices"; break;
          case 301: codeString += "Moved Permanently"; break;
          case 302: codeString += "Found"; break;
          case 303: codeString += "See Other"; break;
          case 304: codeString += "Not Modified"; break;
          case 305: codeString += "Use Proxy"; break;
          case 307: codeString += "Temporary Redirect"; break;
          case 308: codeString += "Permanent Redirect"; break;
          case 400: codeString += "Bad Request"; break;
          case 401: codeString += "Unauthorized"; break;
          case 402: codeString += "Payment Required"; break;
          case 403: codeString += "Forbidden"; break;
          case 404: codeString += "Not Found"; break;
          case 405: codeString += "Method Not Allowed"; break;
          case 406: codeString += "Not Acceptable"; break;
          case 407: codeString += "Proxy Authentication Required"; break;
          case 408: codeString += "Request Time-out"; break;
          case 409: codeString += "Conflict"; break;
          case 410: codeString += "Gone"; break;
          case 411: codeString += "Length Required"; break;
          case 412: codeString += "Precondition Failed"; break;
          case 413: codeString += "Request Entity Too Large"; break;
          case 414: codeString += "Request-URL Too Long"; break;
          case 415: codeString += "Unsupported Media Type"; break;
          case 416: codeString += "Requested range not satisfiable"; break;
          case 417: codeString += "Expectation Failed"; break;
          case 420: codeString += "Policy Not Fulfilled"; break;
          case 421: codeString += "There are too many connections from your internet address"; break;
          case 422: codeString += "Unprocessable Entity"; break;
          case 423: codeString += "Locked"; break;
          case 424: codeString += "Failed Dependency"; break;
          case 425: codeString += "Unordered Collection"; break;
          case 426: codeString += "Upgrade Required"; break;
          case 429: codeString += "Too Many Requests"; break;
          case 444: codeString += "No Response"; break;
          case 449: codeString += "The request should be retried after doing the appropriate action"; break;
          case 500: codeString += "Internal Server Error"; break;
          case 501: codeString += "Not Implemented"; break;
          case 502: codeString += "Bad Gateway"; break;
          case 503: codeString += "Service Unavailable"; break;
          case 504: codeString += "Gateway Time-out"; break;
          case 505: codeString += "HTTP Version not supported"; break;
          case 506: codeString += "Variant Also Negotiates"; break;
          case 507: codeString += "Insufficient Storage"; break;
          case 509: codeString += "Bandwidth Limit Exceeded"; break;
          case 510: codeString += "Not Extended"; break;
      }
      return codeString;
  }

}

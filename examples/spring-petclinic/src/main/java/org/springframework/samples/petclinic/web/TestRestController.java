package org.springframework.samples.petclinic.web;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestRestController {

  /**
   * @requestExample application/json { "name" : "TEST" }
   */
  @RequestMapping(value = "/doc", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  Map<String, String> testDoc(@RequestBody Map<String, String> testBody) {
    return testBody;
  }

  /**
   * Download some binary.
   *
   * @return the binary
   */
  @RequestMapping(value = "/binary", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  byte[] testBinary() {
    return null;
  }

  /**
   * Upload some binary.
   *
   * @param body the body
   */
  @RequestMapping(value = "/binary", method = RequestMethod.POST, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  void uploadBinary(@RequestBody byte[] body) {
  }
}
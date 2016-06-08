package org.springframework.samples.petclinic.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author Ryan Heaton
 */
@ControllerAdvice
public class HeaderAdvice {

  @ModelAttribute
  public SpecialHeaders getSpecialHeaders(@RequestHeader("X-Special-1") String special1, @RequestHeader("X-Special-2") String special2) {
    return new SpecialHeaders();
  }
}

package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services;

import java.util.List;
import javax.jws.WebService;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.Assertion;

@WebService(targetNamespace = "http://enunciate.webcohesion.com/samples/full")
public interface AssertionService {

    List<Assertion> readAssertions();
}

package com.webcohesion.enunciate.examples.genealogy.services;

import java.util.List;
import javax.jws.WebService;

import com.webcohesion.enunciate.examples.genealogy.data.Assertion;

@WebService(targetNamespace = "http://enunciate.codehaus.org/samples/full")
public interface AssertionService {

    List<Assertion> readAssertions();
}

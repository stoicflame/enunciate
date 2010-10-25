package org.codehaus.enunciate.samples.genealogy.services;

import java.util.List;
import javax.jws.WebService;
import org.codehaus.enunciate.samples.genealogy.data.Assertion;

@WebService(targetNamespace = "http://enunciate.codehaus.org/samples/full")
public interface AssertionService {

    List<Assertion> readAssertions();
}

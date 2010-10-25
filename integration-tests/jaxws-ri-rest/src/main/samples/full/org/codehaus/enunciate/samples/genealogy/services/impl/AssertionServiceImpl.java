package org.codehaus.enunciate.samples.genealogy.services.impl;

import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;
import org.codehaus.enunciate.samples.genealogy.data.Assertion;
import org.codehaus.enunciate.samples.genealogy.data.Gender;
import org.codehaus.enunciate.samples.genealogy.data.Name;
import org.codehaus.enunciate.samples.genealogy.services.AssertionService;

@WebService(endpointInterface = "org.codehaus.enunciate.samples.genealogy.services.AssertionService")
public class AssertionServiceImpl implements AssertionService {

    public List<Assertion> readAssertions() {
        ArrayList<Assertion> result = new ArrayList<Assertion>();
        Gender gender = new Gender();
        gender.setId("gender");
        result.add(gender);
        Name name = new Name();
        name.setId("name");
        result.add(name);
        return result;
    }

}

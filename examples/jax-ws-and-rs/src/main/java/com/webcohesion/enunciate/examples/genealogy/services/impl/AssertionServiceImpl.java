package com.webcohesion.enunciate.examples.genealogy.services.impl;

import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;

import com.webcohesion.enunciate.examples.genealogy.data.Assertion;
import com.webcohesion.enunciate.examples.genealogy.data.Gender;
import com.webcohesion.enunciate.examples.genealogy.data.Name;
import com.webcohesion.enunciate.examples.genealogy.services.AssertionService;

@WebService(endpointInterface = "com.webcohesion.enunciate.examples.genealogy.services.AssertionService" )
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

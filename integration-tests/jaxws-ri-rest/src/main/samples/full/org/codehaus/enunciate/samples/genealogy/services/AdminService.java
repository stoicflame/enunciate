package org.codehaus.enunciate.samples.genealogy.services;

import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.samples.genealogy.data.PersonAdmin;

import javax.jws.WebService;

@Facet(name = "http://enunciate.codehaus.org/samples/full#admin" )
@WebService(targetNamespace = "http://enunciate.codehaus.org/samples/full")
public interface AdminService {

    PersonAdmin readAdminPerson(String id);
}

package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.services;

import com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data.PersonAdmin;
import com.webcohesion.enunciate.metadata.Facet;

import javax.jws.WebService;

@Facet( "http://enunciate.webcohesion.com/samples/full#admin" )
@WebService(targetNamespace = "http://enunciate.webcohesion.com/samples/full")
public interface AdminService {

    PersonAdmin readAdminPerson(String id);
}

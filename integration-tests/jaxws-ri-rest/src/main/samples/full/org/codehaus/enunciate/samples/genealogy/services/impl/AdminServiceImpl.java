package org.codehaus.enunciate.samples.genealogy.services.impl;

import org.codehaus.enunciate.samples.genealogy.data.PersonAdmin;
import org.codehaus.enunciate.samples.genealogy.services.AdminService;

import javax.jws.WebService;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path ("/admin")
@WebService(endpointInterface = "org.codehaus.enunciate.samples.genealogy.services.AdminService")
public class AdminServiceImpl implements AdminService {

  @Path("/admin/person/{id}")
  public PersonAdmin readAdminPerson(@PathParam("id") String id) {
    return new PersonAdmin();
  }

}

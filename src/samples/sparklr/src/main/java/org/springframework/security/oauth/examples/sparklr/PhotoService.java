package org.springframework.security.oauth.examples.sparklr;

import org.codehaus.enunciate.rest.annotations.*;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.annotation.security.RolesAllowed;

/**
 * Service for retrieving photos.
 * 
 * @author Ryan Heaton
 */
@WebService
@RESTEndpoint
@JSONP
@RolesAllowed (
  "ROLE_USER"
)
public interface PhotoService {

  /**
   * Load the photos for the current user.
   *
   * @return The photos for the current user.
   */
  @Noun (
    "photos"
  )
  @Verb (
    VerbType.get
  )
  Photos getPhotosForCurrentUser();

  /**
   * Load a photo by id.
   * 
   * @param id The id of the photo.
   * @return The photo that was read.
   */
  @Noun (
    value = "photo",
    context = "jpg"
  )
  @Verb (
    VerbType.get
  )
  DataHandler loadPhoto(@ProperNoun String id);
}

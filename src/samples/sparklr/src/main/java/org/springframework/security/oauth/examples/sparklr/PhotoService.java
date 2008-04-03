package org.springframework.security.oauth.examples.sparklr;

import org.codehaus.enunciate.rest.annotations.RESTEndpoint;
import org.codehaus.enunciate.rest.annotations.Noun;
import org.codehaus.enunciate.rest.annotations.ProperNoun;

import javax.activation.DataHandler;
import javax.jws.WebService;

/**
 * Service for retrieving photos.
 * 
 * @author Ryan Heaton
 */
@WebService
@RESTEndpoint
public interface PhotoService {

  /**
   * Load the photos for the current user.
   *
   * @return The photos for the current user.
   */
  @Noun (
    "photos"
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
    disableTopContext = true
  )
  DataHandler loadPhoto(@ProperNoun String id);
}

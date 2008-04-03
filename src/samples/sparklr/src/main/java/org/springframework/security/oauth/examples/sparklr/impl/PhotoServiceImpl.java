package org.springframework.security.oauth.examples.sparklr.impl;

import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.springframework.security.oauth.examples.sparklr.PhotoInfo;
import org.springframework.security.oauth.examples.sparklr.PhotoService;
import org.springframework.security.oauth.examples.sparklr.Photos;
import org.codehaus.enunciate.rest.annotations.RESTEndpoint;

import javax.activation.DataHandler;
import javax.jws.WebService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation for the photo service.
 *
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.springframework.security.oauth.examples.sparklr.PhotoService"
)
@RESTEndpoint
public class PhotoServiceImpl implements PhotoService {

  private List<PhotoInfo> photos;

  public Photos getPhotosForCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getPrincipal() instanceof UserDetails) {
      UserDetails details = (UserDetails) authentication.getPrincipal();
      String username = details.getUsername();
      ArrayList<PhotoInfo> infos = new ArrayList<PhotoInfo>();
      for (PhotoInfo info : getPhotos()) {
        if (username.equals(info.getUserId())) {
          infos.add(info);
        }
      }
      Photos photos = new Photos();
      photos.setPhotos(infos);
      return photos;
    }
    else {
      throw new BadCredentialsException("Bad credentials: not a username/password authentication.");
    }
  }

  public DataHandler loadPhoto(String id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication.getPrincipal() instanceof UserDetails) {
      UserDetails details = (UserDetails) authentication.getPrincipal();
      String username = details.getUsername();
      for (PhotoInfo photoInfo : getPhotos()) {
        if (id.equals(photoInfo.getId()) && username.equals(photoInfo.getUserId())) {
          URL resourceURL = getClass().getResource(photoInfo.getResourceURL());
          if (resourceURL != null) {
            return new DataHandler(resourceURL);
          }
        }
      }
    }
    return null;
  }

  public List<PhotoInfo> getPhotos() {
    return photos;
  }

  public void setPhotos(List<PhotoInfo> photos) {
    this.photos = photos;
  }
}

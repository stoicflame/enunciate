package com.webcohesion.enunciate.examples.jaxrsjackson.genealogy.data.api;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Binary stuff.
 */
@Path ( "/binary" )
public class BinaryService {

  /**
   * Generate some binary.
   *
   * @param fooParam the foo param
   * @return the binary
   */
  @POST
  @Consumes
  @Produces ( MediaType.APPLICATION_OCTET_STREAM )
  @Path ( "/generateBinary" )
  public byte[] generateBinary(String fooParam) {
    return null;
  }

  /**
   * Download a stream.
   *
   * @return the stream
   */
  @GET
  @Produces ( MediaType.APPLICATION_OCTET_STREAM )
  @Path ( "/stream" )
  public InputStream downloadStream() {
    return null;
  }

  /**
   * Upload some binary.
   *
   * @param body the body
   */
  @POST
  @Consumes ( MediaType.APPLICATION_OCTET_STREAM )
  @Path ( "/upload" )
  public void upload(byte[] body) {
  }

  /**
   * A pdf.
   *
   * @return the pdf
   */
  @GET
  @Produces ( "application/pdf" )
  @Path ( "/pdf" )
  public byte[] pdf() {
    return null;
  }

  /**
   * A custom vendor thing that happens to be binary.
   *
   * @return the thing
   */
  @GET
  @Produces ( "application/vnd.acme.thing" )
  @Path ( "/vendor" )
  public byte[] vendorThing() {
    return null;
  }

  /**
   * Plain text.
   *
   * @return the text
   */
  @GET
  @Produces ( MediaType.TEXT_PLAIN )
  @Path ( "/text" )
  public String text() {
    return null;
  }
}

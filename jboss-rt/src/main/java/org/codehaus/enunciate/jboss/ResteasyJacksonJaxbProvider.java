package org.codehaus.enunciate.jboss;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.jboss.resteasy.annotations.providers.NoJackson;
import org.jboss.resteasy.util.FindAnnotation;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Only different from Jackson one is *+json in @Produces/@Consumes
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision$
 */
@Provider
@Consumes({"application/*+json", "text/json"})
@Produces({"application/*+json", "text/json"})
public class ResteasyJacksonJaxbProvider extends JacksonJaxbJsonProvider
{
   @Override
   public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      if (FindAnnotation.findAnnotation(aClass, annotations, NoJackson.class) != null) return false;
      return super.isReadable(aClass, type, annotations, mediaType);
   }

   @Override
   public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType)
   {
      if (FindAnnotation.findAnnotation(aClass, annotations, NoJackson.class) != null) return false;
      return super.isWriteable(aClass, type, annotations, mediaType);
   }
}

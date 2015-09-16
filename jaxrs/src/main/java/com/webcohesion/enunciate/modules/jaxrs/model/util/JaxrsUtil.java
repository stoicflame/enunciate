
package com.webcohesion.enunciate.modules.jaxrs.model.util;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public final class JaxrsUtil {

  /**
	 * Extracts the value for a {@link Produces} annotation, splitting any media
   * types that are combined using , (see the JAX-RS javadoc)
   */
  public static List<String> value(Produces produces) {
    return splitMediaTypes(produces.value());
  }

  /**
   * Extracts the value for a {@link Consumes} annotation, splitting any media
   * types that are combined using , (see the JAX-RS javadoc)
   */
  public static List<String> value(Consumes consumes) {
    return splitMediaTypes(consumes.value());
  }

  private static List<String> splitMediaTypes(String... mediaTypes) {
		ArrayList<String> values = new ArrayList<String>();
		for (String mediaType : mediaTypes) {
			for (StringTokenizer tokens = new StringTokenizer(mediaType, ","); tokens.hasMoreTokens(); ) {
				String item = tokens.nextToken();
				int paramSeparatorIndex = item.indexOf(';');
				if (paramSeparatorIndex >= 0) {
					item = item.substring(0, paramSeparatorIndex);
				}
				values.add(item.trim());
			}
		}
		return values;
  }

}

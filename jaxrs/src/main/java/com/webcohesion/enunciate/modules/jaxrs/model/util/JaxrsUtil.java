
package com.webcohesion.enunciate.modules.jaxrs.model.util;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;


public final class JaxrsUtil {

	/**
	 * Extracts the value for a {@link Produces} annotation, splitting any media
	 * types that are combined using , (see the JAX-RS javadoc)
	 */
	public static String[] value(Produces produces) {
		return splitMediaTypes(produces.value());
	}
	
	/**
	 * Extracts the value for a {@link Consumes} annotation, splitting any media
	 * types that are combined using , (see the JAX-RS javadoc)
	 */
	public static String[] value(Consumes consumes) {
		return splitMediaTypes(consumes.value());
	}
	
	private static String[] splitMediaTypes(String... mediaTypes) {
		int count = 0;
		for (String mediaType : mediaTypes) {
			count ++;
			int i = 0;
			while ((i = mediaType.indexOf(',', i + 1)) > 0)
				count ++;
		}
		
		if (count == mediaTypes.length)
			return mediaTypes;
		else {
			String[] result = new String[count];
			int i = 0;
			for (String mediaType : mediaTypes)
				for (String value : mediaType.split(","))
					result[i++] = value;
			
			return result;
		}
	}
	
}

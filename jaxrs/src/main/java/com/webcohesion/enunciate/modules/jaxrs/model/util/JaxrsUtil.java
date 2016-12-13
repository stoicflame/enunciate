/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.modules.jaxrs.model.util;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.*;


public final class JaxrsUtil {

  /**
	 * Extracts the value for a {@link Produces} annotation, splitting any media
   * types that are combined using , (see the JAX-RS javadoc)
   */
  public static List<MediaType> value(Produces produces) {
    return splitMediaTypes(produces.value());
  }

  /**
   * Extracts the value for a {@link Consumes} annotation, splitting any media
   * types that are combined using , (see the JAX-RS javadoc)
   */
  public static List<MediaType> value(Consumes consumes) {
    return splitMediaTypes(consumes.value());
  }

  private static List<MediaType> splitMediaTypes(String... mediaTypes) {
		ArrayList<MediaType> values = new ArrayList<MediaType>();
		for (String mediaType : mediaTypes) {
			for (StringTokenizer tokens = new StringTokenizer(mediaType, ","); tokens.hasMoreTokens(); ) {
				String token = tokens.nextToken();
				String value = token.trim();
				float qs = 1.0F;
				Map<String, String> ps = new HashMap<String, String>();
				int paramSeparatorIndex = token.indexOf(';');
				if (paramSeparatorIndex >= 0) {
					value = token.substring(0, paramSeparatorIndex).trim();
					if (paramSeparatorIndex + 1 < token.length()) {
						for (StringTokenizer params = new StringTokenizer(token.substring(paramSeparatorIndex + 1), ";"); params.hasMoreTokens(); ) {
              String paramToken = params.nextToken();
							int equalsIndex = paramToken.indexOf('=');
							if (equalsIndex > 0 && equalsIndex + 1 < paramToken.length()) {
								String param = paramToken.substring(0, equalsIndex).trim().toLowerCase();
								String paramValue = paramToken.substring(equalsIndex + 1).trim();
								if ("qs".equals(param)) {
									try {
										qs = Float.parseFloat(paramValue);
									}
									catch (NumberFormatException e) {
										//fall through...
									}
								}
								else {
									ps.put(param, paramValue);
								}
							}
						}
					}
				}
				values.add(new MediaType(value, qs, ps));
			}
		}
		return values;
  }

}

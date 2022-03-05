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

import java.util.*;


public final class JaxrsUtil {

  /**
   * Extracts the value for annotation, splitting any media types that are combined using , (see the JAX-RS javadoc)
     * @see javax.ws.rs.Consumes <br>
     * @see jakarta.ws.rs.Consumes <br>
     * @see javax.ws.rs.Produces <br>
     * @see jakarta.ws.rs.Produces <br>
     * 
   */
  public static List<MediaType> value(String[] consumes) {
    return splitMediaTypes(consumes);
  }

  private static List<MediaType> splitMediaTypes(String... mediaTypes) {
		ArrayList<MediaType> values = new ArrayList<MediaType>();
		for (String mediaType : mediaTypes) {
			for (StringTokenizer tokens = new StringTokenizer(mediaType, ","); tokens.hasMoreTokens(); ) {
				String token = tokens.nextToken();
				StringBuilder value = new StringBuilder(token.trim());
				float qs = 1.0F;
				int paramSeparatorIndex = token.indexOf(';');
				if (paramSeparatorIndex >= 0) {
					value = new StringBuilder(token.substring(0, paramSeparatorIndex).trim());
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
									value = value.append(';').append(param).append('=').append(paramValue);
								}
							}
						}
					}
				}
				values.add(new MediaType(value.toString(), qs));
			}
		}
		return values;
  }

}

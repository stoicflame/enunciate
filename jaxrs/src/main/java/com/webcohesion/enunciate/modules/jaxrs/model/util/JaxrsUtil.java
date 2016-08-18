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

/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package org.codehaus.enunciate.test.integration.util;

import java.util.Iterator;

import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This hides <code>org.json.JSONObject</code> hash table lookup method allowing
 * natural JXPath expression to be used in JSON JXPath evaluation.
 * 
 * @author reaster
 */
public class JSONObjectJXPathHandler implements DynamicPropertyHandler {

	public Object getProperty(Object jsonObject, String propertyName) {
		JSONObject json = (JSONObject)jsonObject;
		try {
			return json.get(propertyName);
		} catch (JSONException e) {
			throw new IllegalStateException(e);
		}
	}

	public String[] getPropertyNames(Object jsonObject) {
		JSONObject json = (JSONObject)jsonObject;
		Iterator<?> iter = json.keys();
		String[] keys = new String[json.length()];
		int index = 0;
		while(iter.hasNext()) {
			keys[index++] = iter.next().toString();
		}
		return keys;
	}

	public void setProperty(Object jsonObject, String propertyName, Object value) {
		JSONObject json = (JSONObject)jsonObject;
		try {
			json.put(propertyName, value);
		} catch (JSONException e) {
			throw new IllegalStateException(e);
		}
	}
	
}
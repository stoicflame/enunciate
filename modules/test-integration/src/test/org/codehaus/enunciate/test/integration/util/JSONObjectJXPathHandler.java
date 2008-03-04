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
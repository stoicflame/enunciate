/*
 * Copyright (c) 2007 Garmin International. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Garmin International.
 * You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement
 * you entered into with Garmin International.
 *
 * Garmin International MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. Garmin International SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package org.codehaus.enunciate.test.integration;

import org.apache.commons.jxpath.JXPathContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author holmes
 *
 */
public class JXPathUtil {
	/**
	 * Pull a single value out of a jsonSubContext result using JXPath.
	 * 
	 * @param jsonObj
	 *            a JSONObject containing parsed jsonSubContext object
	 * @param jxpath
	 *            JXPath query
	 * @return returns single result or null
	 */
	public Object jXPathValue(JSONObject jsonObj, String jxpath) {
		JXPathContext context = JXPathContext.newContext(jsonObj);
		Object value = context.getValue(jxpath);
		return value;
	}

	/**
	 * Pull a single value out of a jsonSubContext result using JXPath.
	 * 
	 * @param json
	 *            unparsed jsonSubContext string
	 * @param jxpath
	 *            JXPath query
	 * @return returns single result or null
	 * @throws JSONException
	 *             json text is not a valid jsonSubContext object
	 */
	public Object jXPathValue(String json, String jxpath) throws JSONException {
		return jXPathValue(new JSONObject(json), jxpath);
	}
}

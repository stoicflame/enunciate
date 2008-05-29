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

import java.net.URL;

/**
 * @author holmes
 * 
 */
public class ContextProperties {
	protected String protocol;

	protected String host;

	protected String soapSubContext;

	protected String restSubContext;

	protected String jsonSubContext;

	protected String relativeContext;

	protected Integer specifiedPort;

	private URL baseContext;

	public String getProtocol() {
		return this.protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getSoapSubContext() {
		return this.soapSubContext;
	}

	public void setSoapSubContext(String soapSubContext) {
		this.soapSubContext = soapSubContext;
	}

	public String getRestSubContext() {
		return this.restSubContext;
	}

	public void setRestSubContext(String restSubContext) {
		this.restSubContext = restSubContext;
	}

	public String getJsonSubContext() {
		return this.jsonSubContext;
	}

	public void setJsonSubContext(String jsonSubContext) {
		this.jsonSubContext = jsonSubContext;
	}

	public String getRelativeContext() {
		return this.relativeContext;
	}

	public void setRelativeContext(String relativeContext) {
		this.relativeContext = relativeContext;
	}

	public Integer getSpecifiedPort() {
		return this.specifiedPort;
	}

	public void setSpecifiedPort(Integer specifiedPort) {
		this.specifiedPort = specifiedPort;
	}

	public URL getBaseContext() {
		return baseContext;
	}

	public void setBaseContext(URL baseContext) {
		this.baseContext = baseContext;
	}

}

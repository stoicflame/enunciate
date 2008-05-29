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

import org.junit.Before;

import com.meterware.httpunit.WebConversation;

/**
 * @author holmes
 */
public abstract class EnunciateTestCase {

	protected ContextProperties contextProperties;

	protected WebConversation webConversation;

	@Before
	public void setupWebConversation() {
		webConversation = new WebConversation();
		webConversation.setExceptionsThrownOnErrorStatus(false);
		
		contextProperties = DeployedTestContainer.getInstance().getContextProperties();
	}

	/**
	 * Host of the service. Default is <code>localhost</code>.
	 */
	public String getHost() {
		return contextProperties.getHost();
	}

	/**
	 * Returns the port that was requested. The only time this will be different
	 * from the <code>actualPort</code> is when <code>0</code> (the default)
	 * is used, in which case the server assigns an arbitrary unused port.
	 */
	public int getSpecifiedPort() {
		return contextProperties.getSpecifiedPort();
	}

	/**
	 * @return the context of the webapp, excluding protocol, host and port.
	 */
	public String getRelativeContext() {
		return contextProperties.getRelativeContext();
	}

	/** Base URL of the webapp including: protocol, host, port, webapp context. */
	public URL getBaseContext() {
		return contextProperties.getBaseContext();
	}

	/**
	 * BaseContext as a string.
	 */
	public String getBaseContextAsString() {
		return getBaseContext().toExternalForm();
	}

	/**
	 * @return soapSubContext WS endpoint given the name of the web service.
	 */
	public String getSoapEndpoint(String webServiceName) {
		return getBaseContextAsString() + "/" + getSoapSubContext() + "/" + webServiceName;
	}

	/**
	 * @return restSubContext WS endpoint given a restSubContext noun.
	 */
	public String getRestEndpoint(String noun) {
		return getBaseContextAsString() + "/" + getRestSubContext() + "/" + noun + "/";
	}

	/**
	 * @return jsonSubContext WS endpoint given a restSubContext noun.
	 */
	public String getJsonEndpoint(String noun) {
		return getBaseContextAsString() + "/" + getJsonSubContext() + "/" + noun + "/";
	}

	/**
	 * @return the subcontext you should use to access SOAP services
	 */
	public String getSoapSubContext() {
		return contextProperties.getSoapSubContext();
	}

	/**
	 * @return the subcontext you should use to access rest/xml services
	 */
	public String getRestSubContext() {
		return contextProperties.getRestSubContext();
	}

	/**
	 * @return the subcontext you should use to access json services
	 */
	public String getJsonSubContext() {
		return contextProperties.getJsonSubContext();
	}
	
	public WebConversation getWebConversation() {
		return this.webConversation;
	}
	
	public void setWebConversation(WebConversation webConversation) {
		this.webConversation = webConversation;
	}

	public ContextProperties getContextProperties() {
		return contextProperties;
	}

	public void setContextProperties(ContextProperties properties) {
		contextProperties = properties;
	}
}

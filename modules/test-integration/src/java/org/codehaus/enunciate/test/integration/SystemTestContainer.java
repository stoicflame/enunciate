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

import org.junit.BeforeClass;

/**
 * Extend this when you want to run tests against a deployed server (i.e. jboss)
 * 
 * If you want to run against Jetty, extend from {@link BaseEnunciateWsTest}
 * instead
 * 
 * @author holmes
 */
public class SystemTestContainer extends DeployedTestContainer {

	public static final String CONTEXT_PROPERTY_NAME = "deployedContext";
	
	public static final String PORT_PROPERTY_NAME = "deployedPort";
	
	@BeforeClass
	public static void startUpServer() {
		setInstance(new SystemTestContainer());
	}
	
	@Override
	protected String getContextPropertyName() {
		return CONTEXT_PROPERTY_NAME;
	}

	@Override
	protected String getPortPropertyName() {
		return PORT_PROPERTY_NAME;
	}
}

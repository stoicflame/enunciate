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
import org.junit.runner.RunWith;
import org.junit.runners.Enclosed;

/**
 * Extend from this test case when you want to start up Jetty and continue to
 * use existing tests.
 * 
 * Ensure that your test class uses the {@link RunWith} annotation, with a value
 * of {@link Enclosed}. Then just include the other test case as an inner
 * class. Should run like normal, but the properties will point to a temporary
 * jetty deployment.
 * 
 * <pre>
 * &#064;RunWith(Enclosed.class)
 * </pre>
 * 
 * @author holmes
 */
@RunWith(Enclosed.class)
public class WsTestContainer extends EnunciateTestCase {

	@BeforeClass
	public static void startUpServer() {
		contextProperties = new WsContextProperties();
	}
}

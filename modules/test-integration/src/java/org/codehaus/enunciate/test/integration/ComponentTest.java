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

/**
 * The tests in your InnerTest class should create a new one of these and
 * implement the various methods as they see fit.
 * 
 * Ensure that you call {@link Runnable#run()} at the end of declaring the
 * method. If you don't, it'll look like your test passes w/o it actually have
 * done anything.
 * 
 * <pre>
 * &#064;Test
 * public void two() {
 * 	new ComponentTest() {
 * 		&#064;Override
 * 		protected void runAsserts() {
 * 			assertTrue(true);
 * 		}
 * 	}.run();
 * }
 * </pre>
 * 
 * @author holmes
 */
public abstract class ComponentTest implements Runnable {

	/**
	 * Allows you to {@link #setupMocks()}, then runs your actual test and
	 * asserts in {@link #runAsserts()}, and allows you to verify the mocks in
	 * {@link #runVerifies()}
	 */
	public final void run() {
		try {
			setupMocks();
			runAsserts();
			runVerifies();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A place for you to setup mocks for your objects. Don't have to do this if
	 * you're running system tests
	 */
	protected void setupMocks() throws Exception {
		// your choice to override
	}

	/**
	 * This is where your test will do the work, and the asserts should live
	 * @throws Exception 
	 */
	protected abstract void runAsserts() throws Exception;

	/**
	 * A place for you to verify that your mocks were run appropriately. Don't
	 * have to implement if you're not doing anything
	 */
	protected void runVerifies() throws Exception {
		// your choice to override
	}
}
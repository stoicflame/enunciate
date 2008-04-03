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

package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.transport.TransportManager;

/**
 * @author Ryan Heaton
 */
public class TestExplicitJAXWSAnnotationServiceFactory extends TestCase {

  /**
   * test the methods that set up a service.
   */
  public void testServiceSetup() throws Exception {
    XFire xFire = XFireFactory.newInstance().getXFire();
    TransportManager transportManager = xFire.getTransportManager();
    EnunciatedClientBindingProvider clientBP = new EnunciatedClientBindingProvider(new DefaultTypeMappingRegistry(true));
    ExplicitJAXWSAnnotationServiceFactory factory = new ExplicitJAXWSAnnotationServiceFactory(new ExplicitWebAnnotations(), transportManager, clientBP);
    assertTrue(factory.getSerializer(null) instanceof EnunciatedClientMessageBinding);

    //todo: find a good way to test the other stuff.
  }
}

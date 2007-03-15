/*
 * Copyright 2006 Web Cohesion
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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The referential integrity handler is used to preserve referential integrity across java objects using
 * the method layed out in the JAXB specification.
 *
 * @author Ryan Heaton
 * @see org.codehaus.enunciate.modules.xfire_client.ReferentialIntegrityHandler
 */
public class ReferentialIntegrityHandler {

  private final ArrayList references = new ArrayList();
  private final ArrayList resolutions = new ArrayList();

  /**
   * Register a reference to another object.
   *
   * @param refId The id of the object being referenced.
   * @param referenceType The type of the object being referenced.
   * @param callback The logic to invoke when a resolution is made.
   */
  public void registerReference(String refId, Class referenceType, ReferenceResolutionCallback callback) {
    for (int i = 0; i < resolutions.size(); i++) {
      Resolution resolution = (Resolution) resolutions.get(i);
      if ((resolution.getRefId().equals(refId)) && (referenceType.isAssignableFrom(resolution.getResolution().getClass()))) {
        callback.handleResolution(resolution.getResolution());
        return;
      }
    }

    references.add(new Reference(refId, referenceType, callback));
  }

  /**
   * Register a resolution to possible references.
   *
   * @param refId The id of the reference.
   * @param resolution The resolution.
   */
  public void registerResolution(String refId, Object resolution) {
    resolutions.add(new Resolution(refId, resolution));

    Iterator it = references.iterator();
    while (it.hasNext()) {
      Reference reference = (Reference) it.next();
      if ((reference.getRefId().equals(refId)) && (reference.getReferenceType().isAssignableFrom(resolution.getClass()))) {
        reference.getCallback().handleResolution(resolution);
        it.remove();
      }
    }
  }

  /**
   * A reference to an object, possibly not yet deserialized.
   */
  private static class Reference {

    private final String refId;
    private final Class referenceType;
    private final ReferenceResolutionCallback callback;

    public Reference(String refId, Class referenceType, ReferenceResolutionCallback callback) {
      this.refId = refId;
      this.referenceType = referenceType;
      this.callback = callback;
    }

    public String getRefId() {
      return refId;
    }

    public Class getReferenceType() {
      return referenceType;
    }

    public ReferenceResolutionCallback getCallback() {
      return callback;
    }

  }

  /**
   * A resolution to a reference.
   */
  private static class Resolution {

    private final String refId;
    private final Object resolution;

    public Resolution(String refId, Object resolution) {
      this.refId = refId;
      this.resolution = resolution;
    }

    public String getRefId() {
      return refId;
    }

    public Object getResolution() {
      return resolution;
    }
  }

}

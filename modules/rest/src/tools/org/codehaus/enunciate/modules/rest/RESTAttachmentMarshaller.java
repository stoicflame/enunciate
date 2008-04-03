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

package org.codehaus.enunciate.modules.rest;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

/**
 * The Enunciate REST mechanism doesn't support attachments yet.
 *
 * @author Ryan Heaton
 */
public class RESTAttachmentMarshaller extends AttachmentMarshaller {

  public static final RESTAttachmentMarshaller INSTANCE = new RESTAttachmentMarshaller();

  /**
   * @return false;
   */
  @Override
  public boolean isXOPPackage() {
    return false;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String addMtomAttachment(DataHandler dataHandler, String string, String string1) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String addMtomAttachment(byte[] bytes, int i, int i1, String string, String string1, String string2) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String addSwaRefAttachment(DataHandler dataHandler) {
    throw new UnsupportedOperationException();
  }

}

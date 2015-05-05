/*
 * Copyright 2007 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.codehaus.enunciate.samples.petclinic.app.client;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ImageBundle;
import com.google.gwt.user.client.ui.TreeImages;

/**
 * A 'component' is a single panel of the clinic component. They are meant to be lazily
 * instantiated so that the application doesn't pay for all of them on startup.
 */
public abstract class ClinicComponent extends Composite {

  /**
   * An image provider to make available images to Components.
   */
  public interface Images extends ImageBundle, TreeImages {
    AbstractImagePrototype gwtLogo();
  }

  /**
   * Encapsulated information about a component. Each component is expected to have a
   * static <code>init()</code> method that will be called by the clinic component
   * on startup.
   */
  public abstract static class ClinicComponentInfo {
    private ClinicComponent instance;
    private String name, description;

    public ClinicComponentInfo(String name, String desc) {
      this.name = name;
      description = desc;
    }

    public abstract ClinicComponent createInstance();

    public String getColor() {
      return "#2a8ebf";
    }

    public String getDescription() {
      return description;
    }

    public final ClinicComponent getInstance() {
      if (instance != null) {
        return instance;
      }
      return (instance = createInstance());
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Called just before this component is hidden.
   */
  public void onHide() {
  }

  /**
   * Called just after this component is shown.
   */
  public void onShow() {
  }
}

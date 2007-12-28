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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import org.codehaus.enunciate.samples.petclinic.app.client.ClinicComponent.ClinicComponentInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.HistoryListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Application that demonstrates all of the built-in widgets.
 */
public class PetClinicApp implements EntryPoint, HistoryListener {

  private static final ClinicComponent.Images images = (ClinicComponent.Images) GWT.create(ClinicComponent.Images.class);

  protected ComponentList list = new ComponentList(images);
  private ClinicComponentInfo curInfo;
  private ClinicComponent curClinicComponent;
  private HTML description = new HTML();
  private VerticalPanel panel = new VerticalPanel();

  public void onHistoryChanged(String token) {
    // Find the ComponentInfo associated with the history context. If one is
    // found, show it (It may not be found, for example, when the user mis-
    // types a URL, or on startup, when the first context will be "").
    ClinicComponentInfo info = list.find(token);
    if (info == null) {
      showInfo();
      return;
    }
    show(info, false);
  }

  public void onModuleLoad() {
    // Load all the components.
    loadComponents();

    panel.add(list);
    panel.add(description);
    panel.setWidth("100%");

    description.setStyleName("clinic-Info");

    History.addHistoryListener(this);
    RootPanel.get().add(panel);

    // Show the initial screen.
    String initToken = History.getToken();
    if (initToken.length() > 0) {
      onHistoryChanged(initToken);
    } else {
      showInfo();
    }
  }

  public void show(ClinicComponentInfo info, boolean affectHistory) {
    // Don't bother re-displaying the existing component. This can be an issue
    // in practice, because when the history context is set, our
    // onHistoryChanged() handler will attempt to show the currently-visible
    // component.
    if (info == curInfo) {
      return;
    }
    curInfo = info;

    // Remove the old component from the display area.
    if (curClinicComponent != null) {
      curClinicComponent.onHide();
      panel.remove(curClinicComponent);
    }

    // Get the new component instance, and display its description in the
    // component list.
    curClinicComponent = info.getInstance();
    list.setComponentSelection(info.getName());
    description.setHTML(info.getDescription());

    // If affectHistory is set, create a new item on the history stack. This
    // will ultimately result in onHistoryChanged() being called. It will call
    // show() again, but nothing will happen because it will request the exact
    // same component we're already showing.
    if (affectHistory) {
      History.newItem(info.getName());
    }

    // Change the description background color.
    DOM.setStyleAttribute(description.getElement(), "backgroundColor",
        info.getColor());

    // Display the new component.
    curClinicComponent.setVisible(false);
    panel.add(curClinicComponent);
    panel.setCellHorizontalAlignment(curClinicComponent, VerticalPanel.ALIGN_CENTER);
    curClinicComponent.setVisible(true);
    curClinicComponent.onShow();
  }

  /**
   * Adds all components to the list. Note that this does not create actual instances
   * of all components yet (they are created on-demand). This can make a significant
   * difference in startup time.
   */
  protected void loadComponents() {
    list.addComponent(Info.init());
    list.addComponent(Vets.init());
    list.addComponent(Owners.init());
    list.addComponent(FlashVets.init());
  }

  private void showInfo() {
    show(list.find("Intro"), false);
  }
}

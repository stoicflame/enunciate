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

import org.codehaus.enunciate.samples.petclinic.app.client.ClinicComponent.ClinicComponentInfo;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;

/**
 * The left panel that contains all of the components, along with a short description
 * of each.
 */
public class ComponentList extends Composite {

  private class MouseLink extends Hyperlink {

    private int index;

    public MouseLink(String name, int index) {
      super(name, name);
      this.index = index;
      sinkEvents(Event.MOUSEEVENTS);
    }

    public void onBrowserEvent(Event event) {
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEOVER:
          mouseOver(index);
          break;

        case Event.ONMOUSEOUT:
          mouseOut(index);
          break;
      }

      super.onBrowserEvent(event);
    }
  }

  private HorizontalPanel list = new HorizontalPanel();
  private ArrayList components = new ArrayList();

  private int selectedComponent = -1;

  public ComponentList(ClinicComponent.Images images) {
    initWidget(list);
    list.add(images.gwtLogo().createImage());
    setStyleName("clinic-List");
  }

  public void addComponent(final ClinicComponentInfo info) {
    String name = info.getName();
    int index = list.getWidgetCount() - 1;

    MouseLink link = new MouseLink(name, index);
    list.add(link);
    components.add(info);

    list.setCellVerticalAlignment(link, HorizontalPanel.ALIGN_BOTTOM);
    styleComponent(index, false);
  }

  public ClinicComponentInfo find(String componentName) {
    for (int i = 0; i < components.size(); ++i) {
      ClinicComponentInfo info = (ClinicComponentInfo) components.get(i);
      if (info.getName().equals(componentName)) {
        return info;
      }
    }

    return null;
  }

  public void setComponentSelection(String name) {
    if (selectedComponent != -1) {
      styleComponent(selectedComponent, false);
    }

    for (int i = 0; i < components.size(); ++i) {
      ClinicComponentInfo info = (ClinicComponentInfo) components.get(i);
      if (info.getName().equals(name)) {
        selectedComponent = i;
        styleComponent(selectedComponent, true);
        return;
      }
    }
  }

  private void colorComponent(int index, boolean on) {
    String color = "";
    if (on) {
      color = ((ClinicComponentInfo) components.get(index)).getColor();
    }

    Widget w = list.getWidget(index + 1);
    DOM.setStyleAttribute(w.getElement(), "backgroundColor", color);
  }

  private void mouseOut(int index) {
    if (index != selectedComponent) {
      colorComponent(index, false);
    }
  }

  private void mouseOver(int index) {
    if (index != selectedComponent) {
      colorComponent(index, true);
    }
  }

  private void styleComponent(int index, boolean selected) {
    String style = (index == 0) ? "clinic-FirstClinicItem" : "clinic-ClinicItem";
    if (selected) {
      style += "-selected";
    }

    Widget w = list.getWidget(index + 1);
    w.setStyleName(style);

    colorComponent(index, selected);
  }
}

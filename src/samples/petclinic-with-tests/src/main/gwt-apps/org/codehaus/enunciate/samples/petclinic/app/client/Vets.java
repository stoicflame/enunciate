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

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import org.codehaus.enunciate.samples.petclinic.client.services.Clinic;
import org.codehaus.enunciate.samples.petclinic.client.schema.Vet;
import org.codehaus.enunciate.samples.petclinic.client.schema.Specialty;

import java.util.Collection;
import java.util.Iterator;

/**
 * Demonstrates a list of vets.
 */
public class Vets extends ClinicComponent {

  public static ClinicComponentInfo init() {
    return new ClinicComponentInfo("Vets",
        "<h2>Vets</h2>" +
        "<p>This list of vets is retrieved through an Enunciate-supported GWT-RPC call.  Click on a vet name to see the vet's details.</p>") {

      public ClinicComponent createInstance() {
        return new Vets();
      }
    };
  }

  private Grid grid;

  public Vets() {
    grid = new Grid();
    Clinic clinic = new Clinic();
    clinic.getVets(new Clinic.GetVetsResponseCallback() {
      public void onResponse(Collection collection) {
        grid.resize(collection.size() + 1, 2);
        grid.setWidget(0, 0, new Label("name"));
        grid.setWidget(0, 1, new Label("action"));
        grid.getCellFormatter().setWidth(0, 1, "12em");
        grid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
        grid.getCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);
        grid.getRowFormatter().setStyleName(0, "clinic-tables-header");
        int row = 1;
        Iterator vetsIt = collection.iterator();
        while (vetsIt.hasNext()) {
          final Vet vet = (Vet) vetsIt.next();
          final Label details = new Label(vet.getFirstName() + " " + vet.getLastName());
          details.addClickListener(new ClickListener() {
            public void onClick(Widget widget) {
              final DialogBox detailsPanel = new DialogBox(true);
              String html = "Id: " + vet.getId()
                + "<br/>First Name: " + vet.getFirstName()
                + "<br/>Last Name: " + vet.getLastName()
                + "<br/>Phone: " + vet.getTelephone()
                + "<br/>Specialties: " ;
              Iterator specialtiesIt = vet.getSpecialties().iterator();
              while (specialtiesIt.hasNext()) {
                Specialty specialty = (Specialty) specialtiesIt.next();
                html = html + specialty.getName();
                if (specialtiesIt.hasNext()) {
                  html += ",";
                }
              }
              HTML htmlValue = new HTML(html);
              htmlValue.setHorizontalAlignment(HasAlignment.ALIGN_LEFT);
              detailsPanel.setWidget(htmlValue);
              detailsPanel.setTitle(vet.getFirstName() + " " + vet.getLastName());
              detailsPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {
                  detailsPanel.setPopupPosition(details.getAbsoluteLeft(), details.getAbsoluteTop());
                }
              });
            }
          });
          details.addStyleName("clinic-clickable");
          grid.setWidget(row, 0, details);
          grid.setWidget(row, 1, new Button("delete", new ClickListener() {
            public void onClick(Widget widget) {
              Window.alert("You don't have permission to delete a vet");
            }
          }));
          row++;
        }
      }

      public void onError(Throwable throwable) {
        grid.resize(1, 1);
        grid.setWidget(0, 0, new Label("ERROR: " + throwable.getMessage()));
      }
    });
    initWidget(grid);
  }

}

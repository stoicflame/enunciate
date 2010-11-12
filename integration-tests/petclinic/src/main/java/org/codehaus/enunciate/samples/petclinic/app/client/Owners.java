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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.codehaus.enunciate.samples.petclinic.client.services.ClinicAsync;
import org.codehaus.enunciate.samples.petclinic.client.schema.Owner;
import org.codehaus.enunciate.samples.petclinic.client.schema.Pet;

import java.util.Collection;
import java.util.Iterator;

/**
 * Demonstrates the various text widgets.
 */
public class Owners extends ClinicComponent {

  public static ClinicComponentInfo init() {
    return new ClinicComponentInfo(
      "Owners",
      "<h2>Owners</h2>" +
        "<p>You can search for owners by last name.  " +
        "You can click on an owner name to see a list of that owner's pets.  " +
        "Each action involves a GWT remote procedure call.</p>") {

      public ClinicComponent createInstance() {
        return new Owners();
      }

      public String getColor() {
        return "#2fba10";
      }
    };
  }

  public Owners() {
    final ClinicAsync clinic = ClinicAsync.Util.getInstance();
    FlowPanel searchPanel = new FlowPanel();
    final Grid grid = new Grid();
    final VerticalPanel layout = new VerticalPanel();
    final TextBox searchBox = new TextBox();
    searchPanel.add(searchBox);
    searchPanel.add(new Button("find", new ClickListener() {
      public void onClick(Widget widget) {
        if (searchBox.getText().length() > 0) {
          clinic.findOwners(searchBox.getText(), new AsyncCallback<Collection<Owner>>() {
            public void onSuccess(Collection<Owner> collection) {
              if (collection.size() == 0) {
                grid.resize(1, 1);
                grid.setWidget(0, 0, new Label("No owners of last name '" + searchBox.getText() + "' were found."));
              }
              else {
                grid.resize(collection.size() + 1, 4);
                grid.setWidget(0, 0, new Label("name"));
                grid.setWidget(0, 1, new Label("phone"));
                grid.setWidget(0, 2, new Label("address"));
                grid.setWidget(0, 3, new Label("city"));
                grid.getCellFormatter().setWidth(0, 1, "12em");
                grid.getCellFormatter().setHorizontalAlignment(0, 0, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setHorizontalAlignment(0, 2, HasAlignment.ALIGN_CENTER);
                grid.getCellFormatter().setHorizontalAlignment(0, 3, HasAlignment.ALIGN_CENTER);
                grid.getRowFormatter().setStyleName(0, "clinic-tables-header");
                int row = 1;
                Iterator<Owner> it = collection.iterator();
                while (it.hasNext()) {
                  final Owner owner = it.next();
                  final Label nameLabel = new Label(owner.getFirstName() + " " + owner.getLastName());
                  nameLabel.addStyleName("clinic-clickable");
                  nameLabel.addClickListener(new ClickListener() {
                    public void onClick(Widget widget) {
                      final DialogBox detailsPanel = new DialogBox(true);
                      final VerticalPanel petList = new VerticalPanel();
                      petList.add(new Label("Pets of " + nameLabel.getText() + ":"));
                      detailsPanel.setWidget(petList);
                      detailsPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
                        public void setPosition(int offsetWidth, int offsetHeight) {
                          detailsPanel.setPopupPosition(nameLabel.getAbsoluteLeft(), nameLabel.getAbsoluteTop());
                        }
                      });
                      Iterator petsIt = owner.getPetIds().iterator();
                      while (petsIt.hasNext()) {
                        final Integer petId = (Integer) petsIt.next();
                        clinic.loadPet(petId.intValue(), new AsyncCallback<Pet>() {
                          public void onSuccess(Pet response) {
                            petList.add(new Label("A " + response.getType().getName() + " named " + response.getName() + "."));
                          }

                          public void onFailure(Throwable throwable) {
                            petList.add(new Label("Error loading pet " + petId + ": " + throwable.getMessage()));
                          }
                        });
                      }
                    }
                  });
                  grid.setWidget(row, 0, nameLabel);
                  grid.setWidget(row, 1, new Label(owner.getTelephone()));
                  grid.setWidget(row, 2, new Label(owner.getAddress()));
                  grid.setWidget(row, 3, new Label(owner.getCity()));
                  row++;
                }
              }
            }

            public void onFailure(Throwable throwable) {
              grid.resize(1, 1);
              grid.setWidget(0, 0, new Label("ERROR: " + throwable.getMessage()));
            }
          });
        }
      }
    }));
    layout.add(searchPanel);
    layout.add(grid);
    initWidget(layout);
  }

}

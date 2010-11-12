package net.java.ws.addressbook.apps.addressbook.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.http.client.*;
import net.java.ws.addressbook.client.services.AddressBookAsync;
import net.java.ws.addressbook.client.domain.ContactList;
import net.java.ws.addressbook.client.domain.Contact;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Basic entry point for the GWT address book application.
 * 
 * @author Ryan Heaton
 */
public class AddressBookApp implements EntryPoint {
  public void onModuleLoad() {
    //the panel that will hold the widgets that will look up by name.
    VerticalPanel byNamePanel = new VerticalPanel();

    //the Enunciate-generated client-side GWT address book service
    final AddressBookAsync book = AddressBookAsync.Util.getInstance();

    //the grid we will use to display the contacts that are found.
    final Grid contactGrid = new Grid();

    //the oracle that will make a remote call to populate the suggest box.
    SuggestOracle oracle = new SuggestOracle() {
      public void requestSuggestions(final Request request, final Callback callback) {
        String query = request.getQuery();

        //call the method to find contacts by name.
        book.findContactsByName(query, new AsyncCallback<ContactList>() {
          public void onSuccess(ContactList response) {
            Iterator contactIt = response.getContacts().iterator();
            Collection suggestions = new ArrayList();
            while (contactIt.hasNext()) {
              final Contact contact = (Contact) contactIt.next();

              //add the suggestion.
              suggestions.add(new Suggestion() {
                public String getDisplayString() {
                  return contact.getName();
                }

                public String getReplacementString() {
                  return contact.getName();
                }
              });
            }

            contactGrid.clear(); //clear the grid.
            callback.onSuggestionsReady(request, new Response(suggestions));
          }

          public void onFailure(Throwable throwable) {
            //do nothing if an error occurs while asking for suggestions
          }
        });
      }
    };

    //the suggest box (instantiated with our oracle).
    final SuggestBox suggestBox = new SuggestBox(oracle);

    //The panel that will hold the suggest box and the "find" button.
    HorizontalPanel findForm = new HorizontalPanel();
    findForm.add(suggestBox);

    //the "find" button.
    Button findButton = new Button("find");
    findButton.addClickListener(new ClickListener() {
      public void onClick(Widget widget) {
        //when "find" is clicked, make the query and populate the grid.
        String text = suggestBox.getText();
        book.findContactsByName(text, new AsyncCallback<ContactList>() {
          public void onSuccess(ContactList response) {
            contactGrid.resize(6 * response.getContacts().size(), 2);
            Iterator contactIt = response.getContacts().iterator();
            int i = 0;
            while (contactIt.hasNext()) {
              Contact contact = (Contact) contactIt.next();
              contactGrid.setWidget(++i, 0, new Label("Name:"));
              contactGrid.setWidget(i, 1, new Label(contact.getName()));
              contactGrid.setWidget(++i, 0, new Label("Phone:"));
              contactGrid.setWidget(i, 1, new Label(contact.getPhone()));
              contactGrid.setWidget(++i, 0, new Label("Address:"));
              contactGrid.setWidget(i, 1, new Label(contact.getAddress1()));
              contactGrid.setWidget(++i, 0, new Label("City:"));
              contactGrid.setWidget(i, 1, new Label(contact.getCity()));
              contactGrid.setWidget(++i, 0, new Label("Type:"));
              contactGrid.setWidget(i, 1, new Label(String.valueOf(contact.getContactType())));
              contactGrid.setWidget(++i, 0, new HTML("<hr/>"));
              contactGrid.setWidget(i, 1, new HTML("<hr/>"));
            }
          }

          public void onFailure(Throwable throwable) {
            //if an error while doing a "find," display it in the grid.
            contactGrid.resize(1, 1);
            contactGrid.setWidget(0, 0, new Label("ERROR: " + throwable.getMessage()));
          }
        });
      }
    });

    //add the find button.
    findForm.add(findButton);

    byNamePanel.add(new Label("This demonstrates GWT-RPC"));

    //add the find form to the panel.
    byNamePanel.add(findForm);

    //add the display grid to the panel.
    byNamePanel.add(contactGrid);

    //create the tab panel.
    TabPanel panel = new TabPanel();

    //add the find by name panel to the tab panel.
    panel.add(byNamePanel, "&nbsp;<a href=\"#byname\">by name (RPC)</a>&nbsp;", true);

    final VerticalPanel byIdPanel = new VerticalPanel();

    byIdPanel.add(new Label("This demonstrates using GWT to access a JSON-REST endpoint and using GWT JSON overlays"));

    HorizontalPanel findByIdForm = new HorizontalPanel();
    findByIdForm.add(new Label("id:"));
    final TextBox idBox = new TextBox();
    idBox.setText("1");
    findByIdForm.add(idBox);
    Button findByIdButton = new Button("find");
    findByIdForm.add(findByIdButton);
    findByIdButton.addClickListener(new ClickListener() {
      public void onClick(Widget widget) {
        final RequestBuilder restRequestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + "../json/contact/" + idBox.getText());
        try {
          restRequestBuilder.sendRequest(null, new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
              if (200 == response.getStatusCode()) {
                net.java.ws.addressbook.client.json.domain.Contact contact = net.java.ws.addressbook.client.json.domain.Contact.fromJson(response.getText());
                byIdPanel.add(new Label("Found " + contact.getContactType().toString() + ": " + contact.getName()));
              }
              else {
                byIdPanel.add(new Label("ERROR: " + response.getStatusText() + ": REQUEST URL: " + restRequestBuilder.getUrl()));
              }
            }

            public void onError(Request request, Throwable throwable) {
              byIdPanel.add(new Label("ERROR: " + throwable.getMessage() + ": REQUEST URL: " + restRequestBuilder.getUrl()));
            }
          });
        }
        catch (RequestException e) {
          byIdPanel.add(new Label("ERROR: " + e.getMessage() + ": REQUEST URL: " + restRequestBuilder.getUrl()));
        }
      }
    });

    byIdPanel.add(findByIdForm);

    //add the find by id panel to the tab panel.
    panel.add(byIdPanel, "&nbsp;<a href=\"#byid\">by id (JSON-REST)</a>&nbsp;", true);

    //create some HTML that will embed our flash component.
    HTML flashHTML = new HTML("<object width=\"550\" height=\"400\">\n" +
              "<param name=\"movie\" value=\"../bytype.swf\">\n" +
              "<embed src=\"../bytype.swf\" width=\"550\" height=\"400\">\n" +
              "</embed>\n" +
              "</object>");

    //add the flash component to the other tab in the tab panel.
    panel.add(flashHTML, "&nbsp;<a href=\"#bytype\">by type</a>&nbsp;", true);

    //add the tab panel to the root HTML.
    RootPanel.get().add(panel);
  }

  public static native net.java.ws.addressbook.client.json.domain.Contact contactFromJson(String json) /*-{
    return eval('(' + json + ')');
  }-*/;

}

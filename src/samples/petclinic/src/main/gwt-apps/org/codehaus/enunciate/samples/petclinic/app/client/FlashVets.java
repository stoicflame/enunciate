package org.codehaus.enunciate.samples.petclinic.app.client;

import com.google.gwt.http.client.*;
import com.google.gwt.user.client.ui.HTML;

/**
 * Demonstrates a list of vets.
 */
public class FlashVets extends ClinicComponent {

  public static ClinicComponentInfo init() {
    return new ClinicComponentInfo("Flash Vets",
                                   "<h2>Flash Vets</h2>" +
                                   "<p>This list of vets is actually a flash movie. When you press the \"Get Data\" button, the flash app is making an Enunciate-supported RPC call to get the list of vets via AMF.</p>") {

      public ClinicComponent createInstance() {
        return new FlashVets();
      }
    };
  }

  private HTML html;

  public FlashVets() {
    final String swfURL = "vets.swf";
    html = new HTML("<p>Looking for " + swfURL + "...");
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, swfURL);

    try {
      builder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable exception) {
          html.setHTML("<h2>Woops!</h2><p>Looks like the AMF module is disabled (" + exception.getMessage() + "). You can enable it in the Enunciate configuration file when you build.</p>");
        }

        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == 200) {
            html.setHTML("<object width=\"550\" height=\"400\">\n" +
              "<param name=\"movie\" value=\"" + swfURL + "\">\n" +
              "<embed src=\"" + swfURL + "\" width=\"550\" height=\"400\">\n" +
              "</embed>\n" +
              "</object>");
          }
          else {
            html.setHTML("<h2>Woops!</h2><p>Looks like the AMF module is disabled (" + response.getStatusText() + "). You can enable it in the Enunciate configuration file when you build.</p>");
          }
        }
      });
    }
    catch (RequestException e) {
      html.setHTML("<h2>Woops!</h2><p>Looks like the AMF module is disabled (" + e.getMessage() + "). You can enable it in the Enunciate configuration file when you build.</p>");
    }

    initWidget(html);
  }

}

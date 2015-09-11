#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;

/**
 * @author Ryan Heaton
 */
public class App extends ResourceConfig {

    public App() {
        packages(App.class.getPackage().getName(), "com.webcohesion.enunciate.rt");
        register(JacksonJsonProvider.class);
        property(ServletProperties.FILTER_FORWARD_ON_404, true);
    }
}

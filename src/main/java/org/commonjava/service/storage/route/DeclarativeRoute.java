package org.commonjava.service.storage.route;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.io.IOUtils;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.nio.charset.Charset;

@ApplicationScoped
public class DeclarativeRoute {
    private static final String BASE_DIR = "META-INF/resources/";

    @Route(path = "/browse", methods = HttpMethod.GET, produces = "text/html")
    public String browse(RoutingContext rc) throws IOException {
        return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(BASE_DIR + "browse.html"),
                Charset.defaultCharset());
    }
}

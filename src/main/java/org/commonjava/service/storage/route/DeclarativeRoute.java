/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.storage.route;

import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.io.IOUtils;

import jakarta.enterprise.context.ApplicationScoped;
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

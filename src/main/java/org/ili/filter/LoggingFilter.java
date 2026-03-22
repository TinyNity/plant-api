package org.ili.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
/**
 * Request filter logging basic request metadata for observability.
 */

@Provider
public class LoggingFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(LoggingFilter.class);

    /**
     * Logs method, path and origin for each incoming HTTP request.
     *
     * @param requestContext inbound request context.
     * @throws IOException when request inspection fails.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        
        LOG.infof("Incoming request: %s %s | Origin: %s", method, path, origin != null ? origin : "null");
    }
}


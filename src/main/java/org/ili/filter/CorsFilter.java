package org.ili.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
/**
 * Dynamic CORS filter reflecting request origin for allowed browser clients.
 */

@Provider
public class CorsFilter implements ContainerResponseFilter {

    /**
     * Adds CORS headers on responses when an Origin header is present.
     *
     * @param requestContext inbound request context.
     * @param responseContext outbound response context.
     * @throws IOException when response header update fails.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // On récupère l'origine de la requête
        String origin = requestContext.getHeaderString("Origin");
        
        // Si l'origine est présente (c'est une requête CORS), on l'autorise explicitement
        if (origin != null && !origin.isEmpty()) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}


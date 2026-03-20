package org.ili.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    @Mock
    ContainerRequestContext requestContext;

    @Mock
    UriInfo uriInfo;

    @Test
    void filterLogsRequestWithoutThrowing() throws Exception {
        LoggingFilter filter = new LoggingFilter();

        when(requestContext.getHeaderString("Origin")).thenReturn("https://example.com");
        when(requestContext.getMethod()).thenReturn("GET");
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("api/v1/plants");

        filter.filter(requestContext);
    }
}

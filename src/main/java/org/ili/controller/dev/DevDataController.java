package org.ili.controller.dev;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.ili.dto.dev.DevSeedRequest;
import org.ili.service.dev.DevDataSeederService;
/**
 * Development-only endpoint used to seed local demo/test data.
 */

@Path("/api/v1/dev/seed")
@IfBuildProfile("dev")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DevDataController {

    @Inject
    DevDataSeederService devDataSeederService;

    /**
     * Seeds development data according to the requested plan.
     *
     * @param request seeding options and counters.
     * @return the seeding execution report.
     */
    @POST
    public Response seed(DevSeedRequest request) {
        return Response.ok(devDataSeederService.seed(request)).build();
    }
}


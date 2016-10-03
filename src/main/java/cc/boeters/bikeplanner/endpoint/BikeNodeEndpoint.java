package cc.boeters.bikeplanner.endpoint;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import cc.boeters.bikeplanner.service.BikeNodeService;

@Path("/api")
public class BikeNodeEndpoint {

	@Inject
	private BikeNodeService service;

	@GET
	@Path("/nodes/{left}/{bottom}/{right}/{top}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodes(@PathParam("left") Double left, @PathParam("bottom") Double bottom,
			@PathParam("right") Double right, @PathParam("top") Double top) {
		return Response.ok(service.getNodes(left, bottom, right, top)).build();
	}

	@GET
	@Path("/route/{from}/{to}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNodes(@PathParam("from") Long from, @PathParam("to") Long to) {
		return Response.ok(service.getRoute(from, to)).build();
	}

}

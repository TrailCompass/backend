package space.itoncek.trailcompass.exchange;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.ISystemExchange;
import space.itoncek.trailcompass.commons.requests.system.ServerTimeRequest;
import space.itoncek.trailcompass.commons.requests.system.ServerVersionRequest;
import space.itoncek.trailcompass.commons.responses.system.ServerTimeResponse;
import space.itoncek.trailcompass.commons.responses.system.ServerVersionResponse;
import space.itoncek.trailcompass.server.BuildMetadata;

public class SystemExchange implements ISystemExchange {
	private final TrailServer server;

	public SystemExchange(TrailServer server) {
		this.server = server;
	}

	@Override
	public ServerTimeResponse time(ServerTimeRequest request) {
		return new ServerTimeResponse(request.start(), System.currentTimeMillis());
	}

	@Override
	public ServerVersionResponse version(ServerVersionRequest request) {
		return new ServerVersionResponse(BuildMetadata.APP_VERSION, BuildMetadata.BUILD_TIME);
	}
}

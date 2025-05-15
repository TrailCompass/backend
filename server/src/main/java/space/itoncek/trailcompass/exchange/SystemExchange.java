package space.itoncek.trailcompass.exchange;

import space.itoncek.trailcompass.commons.exchange.ISystemExchange;
import space.itoncek.trailcompass.commons.requests.system.ServerTimeRequest;
import space.itoncek.trailcompass.commons.requests.system.ServerVersionRequest;
import space.itoncek.trailcompass.commons.responses.system.ServerTimeResponse;
import space.itoncek.trailcompass.commons.responses.system.ServerVersionResponse;

public class SystemExchange implements ISystemExchange {
	@Override
	public ServerTimeResponse time(ServerTimeRequest request) {
		return null;
	}

	@Override
	public ServerVersionResponse version(ServerVersionRequest request) {
		return null;
	}
}

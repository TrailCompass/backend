package space.itoncek.trailcompass.exchange;

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IExchange;

public class Exchange implements IExchange {
	private final TrailServer server;
	private final AuthExchange ax;
	private final SystemExchange sx;

	public Exchange(TrailServer server) {
		this.server = server;
		ax = new AuthExchange(server);
		sx = new SystemExchange(server);
	}

	@Override
	public AuthExchange auth() {
		return ax;
	}

	@Override
	public SystemExchange system() {
		return sx;
	}
}

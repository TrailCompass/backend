package space.itoncek.trailcompass.exchange;

import space.itoncek.trailcompass.commons.exchange.IExchange;

public class Exchange implements IExchange {
	@Override
	public AuthExchange auth() {
		return null;
	}

	@Override
	public SystemExchange system() {
		return null;
	}
}

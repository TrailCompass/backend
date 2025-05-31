package space.itoncek.trailcompass.exchange;

/*
 *
 * ████████╗██████╗  █████╗ ██╗██╗      ██████╗ ██████╗ ███╗   ███╗██████╗  █████╗ ███████╗███████╗
 * ╚══██╔══╝██╔══██╗██╔══██╗██║██║     ██╔════╝██╔═══██╗████╗ ████║██╔══██╗██╔══██╗██╔════╝██╔════╝
 *    ██║   ██████╔╝███████║██║██║     ██║     ██║   ██║██╔████╔██║██████╔╝███████║███████╗███████╗
 *    ██║   ██╔══██╗██╔══██║██║██║     ██║     ██║   ██║██║╚██╔╝██║██╔═══╝ ██╔══██║╚════██║╚════██║
 *    ██║   ██║  ██║██║  ██║██║███████╗╚██████╗╚██████╔╝██║ ╚═╝ ██║██║     ██║  ██║███████║███████║
 *    ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝ ╚═════╝ ╚═════╝ ╚═╝     ╚═╝╚═╝     ╚═╝  ╚═╝╚══════╝╚══════╝
 *
 *                                    Copyright (c) 2025.
 */

import space.itoncek.trailcompass.TrailServer;
import space.itoncek.trailcompass.commons.exchange.IExchange;

public class Exchange implements IExchange {
	private final TrailServer server;
	private final AuthExchange ax;
	private final SystemExchange sx;
	private final MapExchange mx;
	private final GameManagerExchange gmx;

	public Exchange(TrailServer server) {
		this.server = server;
		ax = new AuthExchange(server);
		sx = new SystemExchange(server);
		mx = new MapExchange(server);
		gmx = new GameManagerExchange(server);
	}

	@Override
	public AuthExchange auth() {
		return ax;
	}

	@Override
	public SystemExchange system() {
		return sx;
	}

	@Override
	public MapExchange map() {
		return mx;
	}

	@Override
	public GameManagerExchange gameMgr() {
		return gmx;
	}
}

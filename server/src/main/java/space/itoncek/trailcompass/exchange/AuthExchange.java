package space.itoncek.trailcompass.exchange;

import space.itoncek.trailcompass.commons.exchange.IAuthExchange;
import space.itoncek.trailcompass.commons.requests.auth.LoginRequest;
import space.itoncek.trailcompass.commons.requests.auth.ProfileOtherRequest;
import space.itoncek.trailcompass.commons.requests.auth.ProfileRequest;
import space.itoncek.trailcompass.commons.requests.auth.RegisterRequest;
import space.itoncek.trailcompass.commons.responses.auth.LoginResponse;
import space.itoncek.trailcompass.commons.responses.auth.ProfileResponse;
import space.itoncek.trailcompass.commons.responses.generic.OkResponse;

public class AuthExchange implements IAuthExchange {
	@Override
	public LoginResponse login(LoginRequest request) {
		return null;
	}

	@Override
	public OkResponse register(RegisterRequest request) {
		return null;
	}

	@Override
	public ProfileResponse getProfile(ProfileRequest request) {
		return null;
	}

	@Override
	public ProfileResponse getOtherProfile(ProfileOtherRequest request) {
		return null;
	}
}

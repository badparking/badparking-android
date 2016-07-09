package ua.in.badparking.services.api;

import org.greenrobot.eventbus.EventBus;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.TokenApi;
import ua.in.badparking.api.requests.TokenRequest;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.events.TokenRefreshedEvent;
import ua.in.badparking.events.TokenVerifiedEvent;

public class TokenService extends ApiService {

    private final TokenApi mTokenApi;

    protected TokenService(ApiGenerator apiGenerator) {
        super(apiGenerator);
        mTokenApi = apiGenerator.createApi(TokenApi.class, false);
    }

    public void refreshToken(TokenRequest tokenRequest) {
        mTokenApi.refreshToken(tokenRequest, new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenRefreshedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void verifyToken(TokenRequest tokenRequest) {
        mTokenApi.verifyToken(tokenRequest, new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenVerifiedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}

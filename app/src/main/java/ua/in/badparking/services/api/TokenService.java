package ua.in.badparking.services.api;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.TokenApi;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.events.TokenRefreshedEvent;
import ua.in.badparking.events.TokenVerifiedEvent;

public class TokenService extends ApiService {

    private final TokenApi mTokenApi;

    @Inject
    protected TokenService(ApiGenerator apiGenerator) {
        super(apiGenerator);
        mTokenApi = apiGenerator.createApi(TokenApi.class, false);
    }

    public void refreshToken(String tokenRequest) {
        mTokenApi.refreshToken(new TypedString(tokenRequest), new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenRefreshedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void verifyToken(String tokenRequest) {
        if(tokenRequest == null) {
            tokenRequest = "";
        }
        mTokenApi.verifyToken(new TypedString(tokenRequest), new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenVerifiedEvent(true));
            }

            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().post(new TokenVerifiedEvent(false));
            }
        });
    }

}

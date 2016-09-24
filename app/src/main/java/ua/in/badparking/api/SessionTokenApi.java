package ua.in.badparking.api;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import ua.in.badparking.api.responses.TokenResponse;

public interface SessionTokenApi {

    @GET("/profiles/login/dummy")
    void refreshToken(Callback<Response> responseCallback);

}

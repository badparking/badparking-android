package ua.in.badparking.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import ua.in.badparking.api.requests.TokenRequest;
import ua.in.badparking.api.responses.TokenResponse;

public interface TokenApi {
    @POST("/token/refresh")
    void refreshToken(@Body TokenRequest tokenRequest, Callback<TokenResponse> responseCallback);

    @POST("/token/verify")
    void verifyToken(@Body TokenRequest user, Callback<TokenResponse> responseCallback);

}

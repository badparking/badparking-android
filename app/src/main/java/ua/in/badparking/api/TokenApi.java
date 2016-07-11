package ua.in.badparking.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedString;
import ua.in.badparking.api.responses.TokenResponse;

public interface TokenApi {
    @POST("/token/refresh")
    void refreshToken(@Body TypedString tokenRequest, Callback<TokenResponse> responseCallback);

    @Multipart
    @POST("/token/verify")
    void verifyToken(@Part("token") TypedString token, Callback<TokenResponse> responseCallback);

}

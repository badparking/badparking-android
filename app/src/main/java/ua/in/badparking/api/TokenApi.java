package ua.in.badparking.api;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Query;
import retrofit.mime.TypedString;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.model.User;

public interface TokenApi {
    @POST("/token/refresh")
    void refreshToken(@Body TypedString tokenRequest, Callback<TokenResponse> responseCallback);

    @Multipart
    @POST("/token/verify")
    void verifyToken(@Part("token") TypedString token, Callback<TokenResponse> responseCallback);

    @Multipart
    @POST("/user/auth/facebook")
    void authorizeWithFacebook(@PartMap() Map token,
                               @Query("client_id") String clientId,
                               @Query("client_secret") String clientSecret,
                               @Query("timestamp") String timestamp,
                               Callback<User> responseCallback);
}

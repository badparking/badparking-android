package ua.in.badparking.api;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Query;
import retrofit.mime.TypedString;
import ua.in.badparking.api.requests.UserRequest;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.model.User;

public interface UserApi {

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

    @GET("/user/me")
    void getUser(Callback<User> responseCallback);

    @Multipart
    @PUT("/user/me/complete")
    void putUserComplete(@PartMap() Map userData, Callback<User> responseCallback);

    @PATCH("/user/me/complete")
    void patchUserComplete(@Body UserRequest userRequest, Callback<User> responseCallback);


}

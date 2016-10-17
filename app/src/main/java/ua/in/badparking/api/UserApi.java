package ua.in.badparking.api;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Query;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.model.User;

public interface UserApi {

    @FormUrlEncoded
    @POST("/token/refresh")
    void refreshToken(@FieldMap() Map tokenRequest, Callback<TokenResponse> responseCallback);

    @FormUrlEncoded
    @POST("/token/refresh")
    TokenResponse refreshToken(@FieldMap() Map tokenRequest);

//    @Multipart
//    @POST("/token/verify")
//    void verifyToken(@Part("token") TypedString token, Callback<TokenResponse> responseCallback);

    @FormUrlEncoded
    @POST("/user/auth/facebook")
    void authorizeWithFacebook(@Field("access_token") String token,
                               @Query("client_id") String clientId,
                               @Query("client_secret") String clientSecret,
                               @Query("timestamp") String timestamp,
                               Callback<User> responseCallback);

    @GET("/user/me")
    void getUser(Callback<User> responseCallback);

    @FormUrlEncoded
    @PUT("/user/me/complete")
    void putUserComplete(@FieldMap() Map userData, Callback<User> responseCallback);

//    @PATCH("/user/me/complete")
//    void patchUserComplete(@Body UserRequest userRequest, Callback<User> responseCallback);


}

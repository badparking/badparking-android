package ua.in.badparking.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import ua.in.badparking.api.requests.UserRequest;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.model.User;

public interface UserApi {
    @GET("/user/me")
    void getUser(Callback<User> responseCallback);

    @POST("/user/me/complete")
    void postUserComplete(@Body UserRequest userRequest, Callback<User> responseCallback);

    @PATCH("/user/me/complete")
    void patchUserComplete(@Body UserRequest userRequest, Callback<User> responseCallback);

}

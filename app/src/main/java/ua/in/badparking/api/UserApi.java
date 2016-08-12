package ua.in.badparking.api;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.PartMap;
import retrofit.http.Query;
import ua.in.badparking.api.requests.UserRequest;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.model.User;

public interface UserApi {
    @GET("/user/me")
    void getUser(Callback<User> responseCallback);

    @Multipart
    @PUT("/user/me/complete")
    void putUserComplete(@PartMap() Map userData, Callback<User> responseCallback);

    @PATCH("/user/me/complete")
    void patchUserComplete(@Body UserRequest userRequest, Callback<User> responseCallback);


}

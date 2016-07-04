package ua.in.badparking.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import ua.in.badparking.api.requests.ClaimRequest;
import ua.in.badparking.api.responses.BaseResponse;
import ua.in.badparking.api.responses.ClaimsResponse;
import ua.in.badparking.model.Claim;

public interface ClaimsApi {
    @GET("/claims")
    void getClaims(@Query("client_id") String clientId,
                   @Query("client_secret") String clientSecret,
                   @Query("timestamp") String timestamp,
                   Callback<List<Claim>> responseCallback);

    @GET("/claims/my")
    void getMyClaims(Callback<List<Claim>> responseCallback);

    @POST("/claims/my")
    void postMyClaims(@Body ClaimRequest claimRequest, Callback<ClaimsResponse> responseCallback);

    @PUT("/claims/my/{pk}")
    void putMyClaims(@Path("pk") String pk, @Body ClaimRequest claimRequest, Callback<ClaimsResponse> responseCallback);

    @PATCH("/claims/my/{pk}")
    void patchMyClaims(@Path("pk") String pk, @Body ClaimRequest claimRequest, Callback<ClaimsResponse> responseCallback);

    @GET("/claims/my/{pk}")
    void getClaim(@Path("pk") String pk, Callback<ClaimsResponse> responseCallback);

    @POST("/claims/my/{pk}/cancel")
    void cancelClaim(@Path("pk") String pk, Callback<BaseResponse> responseCallback);


}

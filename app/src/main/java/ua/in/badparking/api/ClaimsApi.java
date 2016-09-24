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
import retrofit.http.Part;
import retrofit.http.PartMap;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import ua.in.badparking.api.responses.BaseResponse;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.CrimeType;

public interface ClaimsApi {

    @GET("/types")
    void getTypes(Callback<List<CrimeType>> responseCallback);

    @GET("/types/{pk}")
    void getType(@Path("pk") String pk, Callback<CrimeType> responseCallback);

    @Multipart
    @GET("/claims")
    void getClaims(@Part("client_id") TypedString clientId,
                   @Part("client_secret") TypedString clientSecret,
                   @Part("timestamp") TypedString timestamp,
                   Callback<List<Claim>> responseCallback);

    @Multipart
    @POST("/claims")
    void postClaims(@PartMap() Map crimetypes,
                    @PartMap() Map claimData,
                    @PartMap() Map userData,
                    @PartMap() Map fileData,
                    @PartMap() Map claimStateData,
                    Callback<Claim> responseCallback);

    @Multipart
    @POST("/claims/{pk}/authorize")
    void authorizeClaim(@PartMap() Map crimetypes,
                        @PartMap() Map claimData,
                        @PartMap() Map userData,
                        @PartMap() Map fileData,
                        @PartMap() Map claimStateData,
                        Callback<Claim> responseCallback);

    @GET("/claims/my")
    void getMyClaims(Callback<List<Claim>> responseCallback);

    @Multipart
    @POST("/claims/my")
    void postMyClaims(@PartMap() Map crimetypes,
                      @PartMap() Map claimData,
                      Callback<Claim> responseCallback);

    @PUT("/claims/my/{pk}")
    void putMyClaims(@Path("pk") String pk, @Body Claim claimRequest, Callback<Claim> responseCallback);

    @PATCH("/claims/my/{pk}")
    void patchMyClaims(@Path("pk") String pk, @Body Claim claimRequest, Callback<Claim> responseCallback);

    @GET("/claims/my/{pk}")
    void getClaim(@Path("pk") String pk, Callback<Claim> responseCallback);

    @POST("/claims/my/{pk}/cancel")
    void cancelClaim(@Path("pk") String pk, Callback<BaseResponse> responseCallback);

    @Multipart
    @POST("/claims/my/{pk}/media")
    void postImage(@Path("pk") String pk, @Part("file") TypedFile file, Callback<BaseResponse> responseCallback);

}

package ua.in.badparking.api;

import java.util.Map;
import java.util.Set;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import ua.in.badparking.api.responses.BaseResponse;
import ua.in.badparking.model.Claim;

public interface ClaimsApi {

//    @GET("/types/{pk}")
//    void getType(@Path("pk") String pk, Callback<CrimeType> responseCallback);
//
//    @Multipart
//    @GET("/claims")
//    void getClaims(@Part("client_id") TypedString clientId,
//                   @Part("client_secret") TypedString clientSecret,
//                   @Part("timestamp") TypedString timestamp,
//                   Callback<List<Claim>> responseCallback);
//
//    @Multipart
//    @POST("/claims")
//    void postClaims(@PartMap() Map crimetypes,
//                    @PartMap() Map claimData,
//                    @PartMap() Map userData,
//                    @PartMap() Map fileData,
//                    @PartMap() Map claimStateData,
//                    Callback<Claim> responseCallback);
//
//    @Multipart
//    @POST("/claims/{pk}/authorize")
//    void authorizeClaim(@PartMap() Map crimetypes,
//                        @PartMap() Map claimData,
//                        @PartMap() Map userData,
//                        @PartMap() Map fileData,
//                        @PartMap() Map claimStateData,
//                        Callback<Claim> responseCallback);
//
//    @GET("/claims/my")
//    void getMyClaims(Callback<List<Claim>> responseCallback);

    @FormUrlEncoded
    @POST("/claims/my")
    void postMyClaims(@Field("crimetypes") Set<Integer> crimetypes,
                      @FieldMap() Map claimData,
                      Callback<Claim> responseCallback);

//    @PUT("/claims/my/{pk}")
//    void putMyClaims(@Path("pk") String pk, @Body Claim claimRequest, Callback<Claim> responseCallback);
//
//    @PATCH("/claims/my/{pk}")
//    void patchMyClaims(@Path("pk") String pk, @Body Claim claimRequest, Callback<Claim> responseCallback);
//
//    @GET("/claims/my/{pk}")
//    void getClaim(@Path("pk") String pk, Callback<Claim> responseCallback);
//
//    @POST("/claims/my/{pk}/cancel")
//    void cancelClaim(@Path("pk") String pk, Callback<BaseResponse> responseCallback);

    @Multipart
    @POST("/claims/my/{pk}/media")
    void postImage(@Path("pk") String pk, @Part("file") TypedFile file, Callback<BaseResponse> responseCallback);

}

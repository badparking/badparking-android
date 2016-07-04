package ua.in.badparking.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import ua.in.badparking.model.CrimeType;

public interface TypesApi {
    @GET("/types")
    void getTypes(Callback<List<CrimeType>> responseCallback);

    @GET("/types/{pk}")
    void getType(@Path("pk") String pk, Callback<CrimeType> responseCallback);

}

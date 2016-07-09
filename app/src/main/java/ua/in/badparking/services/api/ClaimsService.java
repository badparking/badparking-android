package ua.in.badparking.services.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.ClaimsApi;
import ua.in.badparking.api.TypesApi;
import ua.in.badparking.api.responses.BaseResponse;
import ua.in.badparking.events.ClaimCancelledEvent;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.events.ClaimPutEvent;
import ua.in.badparking.events.ClaimsLoadedEvent;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.CrimeType;

@Singleton
public class ClaimsService extends ApiService {

    private final TypesApi mTypesApi;
    private final ClaimsApi mClaimsApi;

    @Inject
    protected ClaimsService(ApiGenerator apiGenerator) {
        super(apiGenerator);
        mClaimsApi = apiGenerator.createApi(ClaimsApi.class, true);
        mTypesApi = apiGenerator.createApi(TypesApi.class, false);
    }

    public void getClaims(String clientId, String clientSecret, String timestamp) {
        mClaimsApi.getClaims(new TypedString(clientId), new TypedString(clientSecret),
                new TypedString(timestamp), new Callback<List<Claim>>() {
            @Override
            public void success(List<Claim> claims, Response response) {
                EventBus.getDefault().post(new ClaimsLoadedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getMyClaims() {
        mClaimsApi.getMyClaims(new Callback<List<Claim>>() {
            @Override
            public void success(List<Claim> claims, Response response) {
                EventBus.getDefault().post(new ClaimsLoadedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void postMyClaims(Claim claim) {
        String latitude = claim.getLatitude();
        String longitude = claim.getLongitude();
        List<Integer> crimetypes = claim.getCrimetypes();
        LinkedHashMap<String, Integer> crimeMap= new LinkedHashMap<>();
        LinkedHashMap<String, String> paramsMap= new LinkedHashMap<>();
        paramsMap.put("latitude", latitude);
        paramsMap.put("longitude", longitude);
        paramsMap.put("city", claim.getCity());
        paramsMap.put("address", claim.getAddress());
        paramsMap.put("license_plates", claim.getLicensePlates());
        for(Integer crimetype : crimetypes) {
            crimeMap.put("crimetypes", crimetype);
        }
        mClaimsApi.postMyClaims(crimeMap, paramsMap, new Callback<Claim>() {
            @Override
            public void success(Claim claimsResponse, Response response) {
                EventBus.getDefault().post(new ClaimPostedEvent(claimsResponse.toString()));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void putMyClaims(String pk, Claim claim) {
        mClaimsApi.putMyClaims(pk, claim, new Callback<Claim>() {
            @Override
            public void success(Claim claimsResponse, Response response) {
                EventBus.getDefault().post(new ClaimPutEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void patchMyClaims(String pk, Claim claim) {
        mClaimsApi.patchMyClaims(pk, claim, new Callback<Claim>() {
            @Override
            public void success(Claim claimsResponse, Response response) {
                EventBus.getDefault().post(new ClaimPutEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getClaim(String pk) {
        mClaimsApi.getClaim(pk, new Callback<Claim>() {
            @Override
            public void success(Claim claimsResponse, Response response) {
                EventBus.getDefault().post(new ClaimsLoadedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
    public void cancelClaim(String pk) {
        mClaimsApi.cancelClaim(pk, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse baseResponse, Response response) {
                EventBus.getDefault().post(new ClaimCancelledEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getTypes() {
        mTypesApi.getTypes(new Callback<List<CrimeType>>() {
            @Override
            public void success(List<CrimeType> crimeTypes, Response response) {
                EventBus.getDefault().post(new TypesLoadedEvent(crimeTypes));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getType(String pk) {
        mTypesApi.getType(pk, new Callback<CrimeType>() {
            @Override
            public void success(CrimeType crimeType, Response response) {
                List types = new ArrayList();
                types.add(crimeType);
                EventBus.getDefault().post(new TypesLoadedEvent(types));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }


}

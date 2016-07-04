package ua.in.badparking.services.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.otto.Bus;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.ClaimsApi;
import ua.in.badparking.api.requests.ClaimRequest;
import ua.in.badparking.api.responses.BaseResponse;
import ua.in.badparking.api.responses.ClaimsResponse;
import ua.in.badparking.events.ClaimCancelledEvent;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.events.ClaimPutEvent;
import ua.in.badparking.events.ClaimsLoadedEvent;
import ua.in.badparking.model.Claim;

@Singleton
public class ClaimsService extends ApiService {

    private final ClaimsApi mClaimsApi;

    @Inject
    protected ClaimsService(Bus bus, ApiGenerator apiGenerator) {
        super(bus, apiGenerator);
        mClaimsApi = apiGenerator.createApi(ClaimsApi.class);
    }

    public void getClaims(String clientId, String clientSecret, String timestamp) {
        mClaimsApi.getClaims(clientId, clientSecret, timestamp, new Callback<List<Claim>>() {
            @Override
            public void success(List<Claim> claims, Response response) {
                mBus.post(new ClaimsLoadedEvent());
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
                mBus.post(new ClaimsLoadedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void postMyClaims(ClaimRequest claimRequest) {
        mClaimsApi.postMyClaims(claimRequest, new Callback<ClaimsResponse>() {
            @Override
            public void success(ClaimsResponse claimsResponse, Response response) {
                mBus.post(new ClaimPostedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void putMyClaims(String pk, ClaimRequest claimRequest) {
        mClaimsApi.putMyClaims(pk, claimRequest, new Callback<ClaimsResponse>() {
            @Override
            public void success(ClaimsResponse claimsResponse, Response response) {
                mBus.post(new ClaimPutEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void patchMyClaims(String pk, ClaimRequest claimRequest) {
        mClaimsApi.patchMyClaims(pk, claimRequest, new Callback<ClaimsResponse>() {
            @Override
            public void success(ClaimsResponse claimsResponse, Response response) {
                mBus.post(new ClaimPutEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getClaim(String pk) {
        mClaimsApi.getClaim(pk, new Callback<ClaimsResponse>() {
            @Override
            public void success(ClaimsResponse claimsResponse, Response response) {
                mBus.post(new ClaimsLoadedEvent());
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
                mBus.post(new ClaimCancelledEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }
}

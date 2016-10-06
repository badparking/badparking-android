package ua.in.badparking.services;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import ua.in.badparking.Constants;
import ua.in.badparking.R;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.ClaimsApi;
import ua.in.badparking.api.TypesApi;
import ua.in.badparking.api.responses.BaseResponse;
import ua.in.badparking.events.ClaimPostedEvent;
import ua.in.badparking.events.ImageUploadedEvent;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.model.Claim;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.model.MediaFile;

public enum ClaimService {
    INST;

    private ApiGenerator mApiGenerator;
    private ClaimsApi mClaimsApi;
    private TypesApi mTypesApi;

    private Context context;
    private List<CrimeType> availableCrimeTypes;

    private String mLicensePlates;
    private Claim claim = new Claim();
    private String pk;
    private List<String> uploadedPictures = new ArrayList<>();


    public void init(Context context) {
        this.context = context;
        mTypesApi = ApiGenerator.INST.createApi(TypesApi.class, Constants.API_BASE_URL, null);
    }

//     TYPES

    public void updateTypes() {
        mTypesApi.getTypes(new Callback<List<CrimeType>>() {

            @Override
            public void success(List<CrimeType> crimeTypes, Response response) {
                availableCrimeTypes = crimeTypes;
                // TODO save to prefs
                EventBus.getDefault().post(new TypesLoadedEvent(crimeTypes));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public List<CrimeType> getAvailableCrimeTypes() {
        return availableCrimeTypes;
    }

//     CLAIMS

    public void postMyClaims(Claim claim) {
        String latitude = claim.getLatitude();
        String longitude = claim.getLongitude();
        LinkedHashMap<String, String> paramsMap = new LinkedHashMap<>();
        paramsMap.put("latitude", latitude);
        paramsMap.put("longitude", longitude);
        paramsMap.put("city", claim.getCity());
        paramsMap.put("address", claim.getAddress());
        paramsMap.put("license_plates", claim.getLicensePlates());

        mClaimsApi.postMyClaims(claim.getCrimetypes(), paramsMap, new Callback<Claim>() {
            @Override
            public void success(Claim claimsResponse, Response response) {
                EventBus.getDefault().post(new ClaimPostedEvent(claimsResponse.getPk(), context.getString(R.string.claim_sent), true));
            }

            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().post(new ClaimPostedEvent(null, context.getString(R.string.error_claim_sent), false));
            }
        });
    }

//    public void putMyClaims(String pk, Claim claim) {
//        mClaimsApi.putMyClaims(pk, claim, new Callback<Claim>() {
//            @Override
//            public void success(Claim claimsResponse, Response response) {
//                EventBus.getDefault().post(new ClaimPutEvent());
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
//    }
//
//    public void patchMyClaims(String pk, Claim claim) {
//        mClaimsApi.patchMyClaims(pk, claim, new Callback<Claim>() {
//            @Override
//            public void success(Claim claimsResponse, Response response) {
//                EventBus.getDefault().post(new ClaimPutEvent());
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
//    }
//
//    public void getClaim(String pk) {
//        mClaimsApi.getClaim(pk, new Callback<Claim>() {
//            @Override
//            public void success(Claim claimsResponse, Response response) {
//                EventBus.getDefault().post(new ClaimsLoadedEvent());
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
//    }
//
//    public void cancelClaim(String pk) {
//        mClaimsApi.cancelClaim(pk, new Callback<BaseResponse>() {
//            @Override
//            public void success(BaseResponse baseResponse, Response response) {
//                EventBus.getDefault().post(new ClaimCancelledEvent());
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
//    }

    public void postImage(final String pk, MediaFile image) {
        TypedFile typedImage = new TypedFile("multipart/form-data", image);
        mClaimsApi.postImage(pk, typedImage, new Callback<BaseResponse>() {
            @Override
            public void success(BaseResponse baseResponse, Response response) {
                uploadedPictures.add(pk);
                if(uploadedPictures.size() == getPictures().size()) {
                    EventBus.getDefault().post(new ImageUploadedEvent(true));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().post(new ImageUploadedEvent(false));
            }
        });
    }

    public void recreateClaimsApi(String tokenHeader) {
         mClaimsApi = ApiGenerator.INST.createApi(ClaimsApi.class, Constants.API_BASE_URL, tokenHeader);
    }

    public Claim getClaim() {
        return claim;
    }

    public List<CrimeType> getSelectedCrimeTypes() {
        List<CrimeType> selectedCrimeTypeList = new ArrayList<>();

        for(int id: claim.getCrimetypes()){
            for(CrimeType ct: availableCrimeTypes){
                if(ct.getId() == id) selectedCrimeTypeList.add(ct);
            }
        }

        return selectedCrimeTypeList;
    }

    public String getFullAddress() {
        if (claim.getCity() != null && claim.getAddress() != null) {
            return getClaim().getCity() + ", " + getClaim().getAddress();
        }
        return "";
    }

    public List<MediaFile> getPictures() {
        return claim.getPhotoFiles();
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

//    public void getClaims(String clientId, String clientSecret, String timestamp) {
//        mClaimsApi.getClaims(new TypedString(clientId), new TypedString(clientSecret),
//                new TypedString(timestamp), new Callback<List<Claim>>() {
//                    @Override
//                    public void success(List<Claim> claims, Response response) {
//                        EventBus.getDefault().post(new ClaimsLoadedEvent());
//                    }
//
//                    @Override
//                    public void failure(RetrofitError error) {
//
//                    }
//                });
//    }
//
//    public void getMyClaims() {
//        mClaimsApi.getMyClaims(new Callback<List<Claim>>() {
//            @Override
//            public void success(List<Claim> claims, Response response) {
//                EventBus.getDefault().post(new ClaimsLoadedEvent());
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//
//            }
//        });
//    }

}

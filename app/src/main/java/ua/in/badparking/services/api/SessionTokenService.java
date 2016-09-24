package ua.in.badparking.services.api;

import com.google.inject.Inject;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import ua.in.badparking.Constants;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.SessionTokenApi;

public class SessionTokenService {

    private final SessionTokenApi mBaseTokenApi;
    private ClaimsService mClaimsService;
    private UserService mUserService;

    @Inject
    protected SessionTokenService(ApiGenerator apiGenerator, ClaimsService claimsService, UserService userService) {
        mClaimsService = claimsService;
        mUserService = userService;
        mBaseTokenApi = apiGenerator.createApi(SessionTokenApi.class, Constants.TOKEN_BASE_URL, null);
    }

    public void refreshToken() {
        mBaseTokenApi.refreshToken(new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String tokenHeader = null;
                List<Header> headers = response.getHeaders();
                for (Header header : headers) {
                    if ("X-JWT".equals(header.getName())) {
                        tokenHeader = header.getValue();
                    }
                }
                if (tokenHeader != null) {
                    mClaimsService.onSessionTokenFetched(tokenHeader);
                    mUserService.onSessionTokenFetched(tokenHeader);
                } else {
                    // TODO handle error
                }
            }

            @Override
            public void failure(RetrofitError error) {
                // TODO handle error
            }
        });
    }

}

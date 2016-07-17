package ua.in.badparking.services.api;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.PartMap;
import retrofit.http.QueryMap;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.UserApi;
import ua.in.badparking.api.requests.UserRequest;
import ua.in.badparking.events.AuthorizedWithFacebookEvent;
import ua.in.badparking.events.UserLoadedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.User;

public class UserService extends ApiService {

    private final UserApi mUserApi;

    @Inject
    protected UserService( ApiGenerator apiGenerator) {
        super(apiGenerator);
        mUserApi = apiGenerator.createApi(UserApi.class, true);
    }

    public void getUser() {
        mUserApi.getUser(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                EventBus.getDefault().post(new UserLoadedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void postUserComplete(UserRequest userRequest) {
        mUserApi.postUserComplete(userRequest, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                EventBus.getDefault().post(new UserUpdatedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void patchUserComplete(UserRequest userRequest) {
        mUserApi.patchUserComplete(userRequest, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                EventBus.getDefault().post(new UserUpdatedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void authorizeWithFacebook(String fbToken) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", fbToken);
//        params.put("client_id", clientId); TODO: put this in config file, but don't add to git!!!!
//        params.put("client_secret", clientSecret);
//        params.put("timestamp", timestamp); // in sec
        mUserApi.authorizeWithFacebook(params, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                //TODO get token from our API here, save it in SecurePrefs!!!
                EventBus.getDefault().post(new AuthorizedWithFacebookEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                // TODO : show error dialog
            }
        });
    }

}

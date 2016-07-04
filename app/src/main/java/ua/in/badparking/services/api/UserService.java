package ua.in.badparking.services.api;

import com.squareup.otto.Bus;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.UserApi;
import ua.in.badparking.api.requests.UserRequest;
import ua.in.badparking.events.UserLoadedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.User;

public class UserService extends ApiService {

    private final UserApi mUserApi;

    protected UserService(Bus bus, ApiGenerator apiGenerator) {
        super(bus, apiGenerator);
        mUserApi = apiGenerator.createApi(UserApi.class);
    }

    public void getUser() {
        mUserApi.getUser(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                mBus.post(new UserLoadedEvent());
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
                mBus.post(new UserUpdatedEvent());
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
                mBus.post(new UserUpdatedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}

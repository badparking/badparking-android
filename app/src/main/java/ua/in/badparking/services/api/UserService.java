package ua.in.badparking.services.api;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.UserApi;
import ua.in.badparking.api.requests.UserRequest;
import ua.in.badparking.events.UserLoadedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.User;
import ua.in.badparking.services.UserState;

public class UserService extends ApiService {

    private final UserApi mUserApi;

    @Inject
    protected UserService(ApiGenerator apiGenerator) {
        super(apiGenerator);
        mUserApi = apiGenerator.createApi(UserApi.class, true);
    }

    public void getUser() {
        mUserApi.getUser(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                UserState.INST.setUser(user);
                EventBus.getDefault().post(new UserLoadedEvent(user));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void putUserComplete(String email, String phone) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("phone", phone);

        mUserApi.putUserComplete(params, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                EventBus.getDefault().post(new UserUpdatedEvent(user));
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
                EventBus.getDefault().post(new UserUpdatedEvent(user));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}

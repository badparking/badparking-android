package ua.in.badparking.services.api;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import retrofit.RequestInterceptor;
import retrofit.RetrofitError;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.events.AuthCompleteEvent;
import ua.in.badparking.events.ErrorEvent;
import ua.in.badparking.services.ClaimState;

public class ApiService {

    protected ApiService(ApiGenerator apiGenerator) {
        EventBus.getDefault().register(this);
    }

    protected void handleRetrofitError(String message, RetrofitError.Kind errorKind) {
        EventBus.getDefault().post(new ErrorEvent());
    }

    RequestInterceptor requestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addHeader("Authorization", "JWT " + ClaimState.INST.getToken());
        }
    };

    @Subscribe
    public void onEvent(ErrorEvent event) {

    }

    @Subscribe
    public void onAuthComplete(AuthCompleteEvent event) {
        ClaimState.INST.setToken(event.getToken());
    }

}

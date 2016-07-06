package ua.in.badparking.services.api;

import org.greenrobot.eventbus.EventBus;

import retrofit.RetrofitError;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.events.ErrorEvent;

public abstract class ApiService {

    protected ApiService(ApiGenerator apiGenerator) {
        EventBus.getDefault().register(this);
    }

    protected void handleRetrofitError(String message, RetrofitError.Kind errorKind) {
        EventBus.getDefault().post(new ErrorEvent());
    }

}

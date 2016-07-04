package ua.in.badparking.services.api;

import com.squareup.otto.Bus;

import retrofit.RetrofitError;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.events.ErrorEvent;

public abstract class ApiService {

    protected Bus mBus;

    protected ApiService(Bus bus, ApiGenerator apiGenerator) {
        mBus = bus;
        mBus.register(this);
    }

    protected void handleRetrofitError(String message, RetrofitError.Kind errorKind) {
        mBus.post(new ErrorEvent());
    }

}

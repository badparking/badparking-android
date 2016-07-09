package ua.in.badparking.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import ua.in.badparking.Constants;
import ua.in.badparking.services.ClaimState;

@Singleton
public class ApiGenerator {

    private final RestAdapter.Builder mBuilder;
    private final RestAdapter.Builder mBuilderwithInterceptor;

    @Inject
    public ApiGenerator(Context context) {
        OkHttpClient client = new OkHttpClient();

        client.setConnectTimeout(30, TimeUnit.SECONDS);
        client.setReadTimeout(300, TimeUnit.SECONDS);

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "JWT " + ClaimState.INST.getToken());
            }
        };


        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        mBuilder = new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint(Constants.API_BASE_URL)
                .setClient(new OkClient(client));

        mBuilderwithInterceptor = new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint(Constants.API_BASE_URL)
                .setClient(new OkClient(client))
                .setRequestInterceptor(requestInterceptor);
    }

    private <S> S buildApi(Class<S> apiClass, RestAdapter.Builder builder) {
        RestAdapter adapter = builder.build();
        adapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return adapter.create(apiClass);
    }

    public <S> S createApi(Class<S> apiClass, boolean requiresAuth) {
        RestAdapter.Builder builder = mBuilder;
        if (requiresAuth) {
            builder = mBuilderwithInterceptor;
        }
        return buildApi(apiClass, builder);
    }

}

package ua.in.badparking.services.api;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.TypesApi;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.model.CrimeType;

public class TypesService extends ApiService {

    private final TypesApi mTypesApi;

    @Inject
    protected TypesService(ApiGenerator apiGenerator) {
        super(apiGenerator);
        mTypesApi = apiGenerator.createApi(TypesApi.class);
    }

    public void getTypes() {
        mTypesApi.getTypes(new Callback<List<CrimeType>>() {
            @Override
            public void success(List<CrimeType> crimeTypes, Response response) {
                EventBus.getDefault().post(new TypesLoadedEvent(crimeTypes));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void getType(String pk) {
        mTypesApi.getType(pk, new Callback<CrimeType>() {
            @Override
            public void success(CrimeType crimeType, Response response) {
                List types = new ArrayList();
                types.add(crimeType);
                EventBus.getDefault().post(new TypesLoadedEvent(types));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

}

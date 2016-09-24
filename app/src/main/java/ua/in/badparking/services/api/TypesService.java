package ua.in.badparking.services.api;

import android.content.Context;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.TypesApi;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.model.CrimeType;

@Singleton
public class TypesService {

    private final TypesApi mTypesApi;
    private Context context;

    private List<CrimeType> _crimeTypes;

    @Inject
    protected TypesService(ApiGenerator apiGenerator) {
        mTypesApi = apiGenerator.createApi(TypesApi.class, false);
    }

    public void updateTypes() {
        mTypesApi.getTypes(new Callback<List<CrimeType>>() {

            @Override
            public void success(List<CrimeType> crimeTypes, Response response) {
                _crimeTypes = crimeTypes;
                // TODO save to prefs
                EventBus.getDefault().post(new TypesLoadedEvent(crimeTypes));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public List<CrimeType> getCrimeTypes() {
        return _crimeTypes;
    }
}

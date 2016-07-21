package ua.in.badparking.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.inject.Inject;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.Constants;
import ua.in.badparking.R;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.api.ClaimsService;

@ContentView(R.layout.activity_launch)
public class LaunchActivity extends RoboActivity {

    @Inject
    private ClaimsService mClaimsService;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mClaimsService.getTypes();
        String url = Constants.BASE_URL + "/profiles/login/dummy";
        get(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                    EventBus.getDefault().post(new ClaimPostedEvent(e.getMessage()));
            }

            @Override
            public void onResponse(Response response) throws IOException {
                ClaimState.INST.setToken(response.headers().get("X-JWT"));
            }
        });
    }

    @Subscribe
    public void onTypesLoaded(TypesLoadedEvent event) {
        ClaimState.INST.setCrimeTypes(event.getCrimeTypes());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    Call get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }
}

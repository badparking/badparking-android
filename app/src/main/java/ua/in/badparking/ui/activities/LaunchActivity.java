package ua.in.badparking.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import ua.in.badparking.R;
import ua.in.badparking.events.TypesLoadedEvent;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.api.ClaimsService;

@ContentView(R.layout.activity_launch)
public class LaunchActivity extends RoboActivity {

    @Inject
    private ClaimsService mClaimsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_launch);
        mClaimsService.getTypes();
    }

    @Subscribe
    public void onTypesLoaded(TypesLoadedEvent event) {
        ClaimState.INST.setCrimeTypes(event.getCrimeTypes());
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

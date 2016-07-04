package badparking.in.ua.badparking.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import badparking.in.ua.badparking.R;
import badparking.in.ua.badparking.fragments.BankIdFragment;
import roboguice.inject.ContentView;

public class CaptureActivity extends AppCompatActivity {

    protected FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        mFragmentManager = getSupportFragmentManager();
        BankIdFragment journeyPlannerFragment = new BankIdFragment();
        showFragment(journeyPlannerFragment, false);
    }

    private void showFragment(Fragment fragment, boolean backstack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.layout_main, fragment);
        if (backstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

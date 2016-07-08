package ua.in.badparking.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import roboguice.inject.InjectView;
import ua.in.badparking.Constants;
import ua.in.badparking.R;
import ua.in.badparking.services.AuthState;

public class AuthFragment extends BaseFragment {

    @InjectView(R.id.web_auth) private WebView mAuthWeb;

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_auth, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO: It is not possible to get response headers from WebView
//        mAuthWeb.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                super.onPageStarted(view, url, favicon);
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//            }
//        });
        String url = Constants.BASE_URL + "/profiles/login/dummy";

        get(url, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                String err = e.getMessage();
            }

            @Override
            public void onResponse(Response response) throws IOException {
               AuthState.INST.setToken(response.headers().get("X-JWT"));
            }
        });

       }

    Call get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    public static Fragment newInstance() {
        return new AuthFragment();
    }
}

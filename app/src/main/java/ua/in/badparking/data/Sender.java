package ua.in.badparking.data;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum Sender {
    INST;

    public static final String POST_URL = "http://badparking.in.ua/modules/json.php";

    public void send() {
        final Trespass trespass = TrespassController.INST.getTrespass();
        final String json = new Gson().toJson(trespass);
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("cmd", "save");
        params.put("data", json);
        client.post(POST_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {


            }
        });


    }
}

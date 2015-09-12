package ua.in.badparking.data;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum Sender {
    INST;

    public static final String POST_URL = "http://badparking.in.ua/modules/json.php";
    public static final int CODE_UPLOADING_PHOTO = 9001;
    public static final int CODE_FILE_NOT_FOUND = 9002;

    public void send(final SendCallback sendCallback) {
        final Trespass trespass = TrespassController.INST.getTrespass();
        final String json = new Gson().toJson(trespass);
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("cmd", "save");
        params.put("data", json);
        client.post(POST_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (trespass.getPhotoFiles().size() != 0) {
                    try {
                        uploadPhoto(1, trespass.getPhotoFiles().get(0));
                        sendCallback.onCallback(CODE_UPLOADING_PHOTO, "Завантажуеться фото N1...");
                    } catch (FileNotFoundException e) {
                        sendCallback.onCallback(CODE_FILE_NOT_FOUND, "Фото не знайдено.");
                        e.printStackTrace();
                    }
                } else {
                    sendCallback.onCallback(statusCode, "");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                sendCallback.onCallback(statusCode, "Помилка N" + statusCode + ". Спробуйте пiзнiше");
            }
        });

    }

    private void uploadPhoto(int sessionId, File image) throws FileNotFoundException {
        AsyncHttpClient client = new AsyncHttpClient();
        final RequestParams params = new RequestParams();
        params.put("cmd", "upload");
        params.put("data", image);
        client.post(POST_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    public interface SendCallback {
        public void onCallback(int code, String message);
    }
}

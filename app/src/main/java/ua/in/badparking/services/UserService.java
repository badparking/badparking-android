package ua.in.badparking.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;
import ua.in.badparking.Constants;
import ua.in.badparking.Utils;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.UserApi;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.events.AuthorizedWithFacebookEvent;
import ua.in.badparking.events.TokenRefreshedEvent;
import ua.in.badparking.events.UserLoadedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.User;

public class UserService {

    private static final String USER_DATA_PREFS = "userDataPrefs";
    private static final String USER_TOKEN_KEY = "userTokenKey";
    private static final String USER_KEY = "userKey";

    private SharedPreferences userDataPrefs;
    private Gson gson = new Gson();

    private User mUser = null;

    private UserApi mUserApi;
    private Context context;
    private ClaimsService mClaimsService;
    private ApiGenerator mApiGenerator;

    @Inject
    protected UserService(Context appContext, ApiGenerator apiGenerator, ClaimsService claimsService) {
        mApiGenerator = apiGenerator;
        context = appContext;
        mClaimsService = claimsService;
        userDataPrefs = appContext.getSharedPreferences(USER_DATA_PREFS, Context.MODE_PRIVATE);
    }

    public void fetchUser() {
        mUserApi.getUser(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                mUser = user;
                String userJson = gson.toJson(user);
                userDataPrefs.edit().putString(USER_KEY, userJson).commit();
                EventBus.getDefault().post(new UserLoadedEvent(user));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void restoreUser() {
        String userJson = userDataPrefs.getString(USER_KEY, null);
        if (userJson != null) {
            Type fooType = new TypeToken<User>() {
            }.getType();

            mUser = gson.fromJson(userJson, fooType);
        }
    }

    public User getUser() {
        return mUser;
    }

    public void putUserComplete(String email, String phone) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("phone", phone);

        mUserApi.putUserComplete(params, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                EventBus.getDefault().post(new UserUpdatedEvent(user));
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void refreshToken(String tokenRequest) {
        mUserApi.refreshToken(new TypedString(tokenRequest), new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenRefreshedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void authorizeWithFacebook(String fbToken) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", fbToken);
        String clientId = Utils.getConfigValue(context, "clientId");// TODO: put this in config file, but don't add to git!!!!
        String clientSecret = Utils.getConfigValue(context, "clientSecret");
        //String timestamp = String.valueOf(System.currentTimeMillis() / 1000); // in sec

        Long timestamp = System.currentTimeMillis() / 1000;
        String secretMessage = clientSecret + timestamp.toString();

        MessageDigest sha256 = null;
        byte[] secretHash = new byte[0];
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
            secretHash = sha256.digest(secretMessage.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mUserApi.authorizeWithFacebook(params, clientId, String.format("%064x", new BigInteger(1, secretHash)), String.valueOf(timestamp), new Callback<User>() {
            @Override
            public void success(User user, Response response) {

                mClaimsService.setToken(user.getToken());
                EventBus.getDefault().post(new AuthorizedWithFacebookEvent(user));
            }

            @Override
            public void failure(RetrofitError error) {
                // TODO : show error dialog
            }
        });
    }

    public String getUserToken() {
        return userDataPrefs.getString(USER_TOKEN_KEY, null);
    }

    public void setUserToken(String token) {
        userDataPrefs.edit().putString(USER_TOKEN_KEY, token).commit();
    }

    public void onSessionTokenFetched(String tokenHeader) {
        mUserApi = mApiGenerator.createApi(UserApi.class, Constants.API_BASE_URL, tokenHeader);

//        Jwts.parser().setSigningKey(key).parseClaimsJws(jwtString).getBody().getExpiration();
    }
}

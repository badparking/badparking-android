package ua.in.badparking.services;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ua.in.badparking.utils.Constants;
import ua.in.badparking.utils.Log;
import ua.in.badparking.utils.Utils;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.UserApi;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.events.TokenRefreshFailedEvent;
import ua.in.badparking.events.TokenRefreshedEvent;
import ua.in.badparking.events.UserLoadedEvent;
import ua.in.badparking.events.UserUpdatedEvent;
import ua.in.badparking.model.User;

public enum UserService {
    INST;

    private static final String TAG = UserService.class.getName();
    private static final String USER_DATA_PREFS = "userDataPrefs";
    private static final String USER_TOKEN_KEY = "userTokenKey";
    private static final String USER_KEY = "userKey";

    private SharedPreferences userDataPrefs;
    private Gson gson = new Gson();

    private User mUser;

    private UserApi mUserApi;
    private Context context;

    public void init(Context appContext) {
        context = appContext;
        userDataPrefs = appContext.getSharedPreferences(USER_DATA_PREFS, Context.MODE_PRIVATE);
        recreateUserApi(null);
    }

    public void fetchUser() {
        mUserApi.getUser(new Callback<User>() {
            @Override
            public void success(User user, Response response) {
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

            String token = getUserToken();
            ClaimService.INST.recreateClaimsApi(token);
            recreateUserApi(token);

        }
    }

    public User getUser() {
        if (mUser == null) {
            restoreUser();
        }
        return mUser;
    }

    public void putUserComplete(String email, String phone) {
        if (!mUser.isComplete()) {
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
    }

    public void refreshToken(final String tokenRequest) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("token", tokenRequest);
        mUserApi.refreshToken(params, new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                saveUserToken(tokenResponse.getToken());
                ClaimService.INST.recreateClaimsApi(tokenResponse.getToken());
                recreateUserApi(tokenResponse.getToken());
                saveUser(getUser());
                EventBus.getDefault().post(new TokenRefreshedEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().post(new TokenRefreshFailedEvent());
            }
        });
    }

    public void authorizeWithFacebook(String fbToken) {
        String clientId = Utils.getConfigValue(context, "clientId");
        String clientSecret = Utils.getConfigValue(context, "clientSecret");

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

        // Make sure we're not using an old JWT when asking for a new one, which might have been stored
        recreateUserApi(null);
        mUserApi.authorizeWithFacebook(fbToken, clientId, String.format("%064x", new BigInteger(1, secretHash)), String.valueOf(timestamp), new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                ClaimService.INST.recreateClaimsApi(user.getToken());
                recreateUserApi(user.getToken());
                saveUserToken(user.getToken());
                saveUser(user);
            }

            @Override
            public void failure(RetrofitError error) {
                // TODO : show error dialog
                Log.e(TAG, "authorizeWithFacebook call failed", error);
            }
        });
    }

    public void saveUser(User user) {
        mUser = user;
        String userJson = gson.toJson(user);
        userDataPrefs.edit().putString(USER_KEY, userJson).commit();
    }

    public void recreateUserApi(String tokenHeader) {
        mUserApi = ApiGenerator.INST.createApi(UserApi.class, Constants.API_BASE_URL, tokenHeader);
    }

    public String getUserToken() {
        return userDataPrefs.getString(USER_TOKEN_KEY, null);
    }

    public void saveUserToken(String token) {
        userDataPrefs.edit().putString(USER_TOKEN_KEY, token).commit();
    }

    public void onJwtTokenFetched(String tokenHeader) {
        String key = Utils.getConfigValue(context, "jwtKey");
        try {
            Jwts.parser()
                    .setSigningKey(key.getBytes("UTF-8"))
                    .parseClaimsJws(tokenHeader).getBody().getExpiration();
            EventBus.getDefault().post(new TokenRefreshedEvent());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "onJwtTokenFetched error", e);
        } catch (ExpiredJwtException e) {
            refreshToken(tokenHeader);
        }
    }
}

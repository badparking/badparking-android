package ua.in.badparking.services.api;

import com.google.inject.Inject;

import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedString;
import ua.in.badparking.App;
import ua.in.badparking.Utils;
import ua.in.badparking.api.ApiGenerator;
import ua.in.badparking.api.TokenApi;
import ua.in.badparking.api.responses.TokenResponse;
import ua.in.badparking.events.AuthorizedWithFacebookEvent;
import ua.in.badparking.events.TokenRefreshedEvent;
import ua.in.badparking.events.TokenVerifiedEvent;
import ua.in.badparking.model.User;
import ua.in.badparking.services.ClaimState;
import ua.in.badparking.services.UserState;

public class TokenService extends ApiService {

    private final TokenApi mTokenApi;

    @Inject
    protected TokenService(ApiGenerator apiGenerator) {
        super(apiGenerator);
        mTokenApi = apiGenerator.createApi(TokenApi.class, false);
    }

    public void refreshToken(String tokenRequest) {
        mTokenApi.refreshToken(new TypedString(tokenRequest), new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenRefreshedEvent());
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void verifyToken(String tokenRequest) {
        if(tokenRequest == null) {
            tokenRequest = "";
        }
        mTokenApi.verifyToken(new TypedString(tokenRequest), new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse tokenResponse, Response response) {
                EventBus.getDefault().post(new TokenVerifiedEvent(true));
            }

            @Override
            public void failure(RetrofitError error) {
                EventBus.getDefault().post(new TokenVerifiedEvent(false));
            }
        });
    }

    public void authorizeWithFacebook(String fbToken) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", fbToken);
        String clientId = Utils.getConfigValue(App.getAppContext(), "clientId");// TODO: put this in config file, but don't add to git!!!!
        String clientSecret = Utils.getConfigValue(App.getAppContext(), "clientSecret");
        //String timestamp = String.valueOf(System.currentTimeMillis() / 1000); // in sec

        Long timestamp = System.currentTimeMillis() / 1000;
        String secretMessage = clientSecret + timestamp.toString();

        MessageDigest sha256 = null;
        byte[] secretHash = new byte[0];
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
            secretHash = sha256.digest(secretMessage.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        mTokenApi.authorizeWithFacebook(params, clientId, String.format("%064x", new BigInteger(1, secretHash)), String.valueOf(timestamp), new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                //TODO get token from our API here, save it in SecurePrefs!!!
                ClaimState.INST.setToken(user.getToken());

                UserState.INST.setUser(user);
                EventBus.getDefault().post(new AuthorizedWithFacebookEvent());
            }

            @Override
            public void failure(RetrofitError error) {
                // TODO : show error dialog
            }
        });
    }



}

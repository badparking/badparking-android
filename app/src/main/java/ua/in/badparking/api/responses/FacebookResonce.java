package ua.in.badparking.api.responses;

import com.google.gson.annotations.SerializedName;

public class FacebookResonce {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("client_id")
    private String clientId;
    @SerializedName("client_secret")
    private String client_secret;
    @SerializedName("timestamp")
    private String timestamp;

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

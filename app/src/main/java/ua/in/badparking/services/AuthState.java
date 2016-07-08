package ua.in.badparking.services;

import ua.in.badparking.model.Claim;

public enum AuthState {
    INST;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

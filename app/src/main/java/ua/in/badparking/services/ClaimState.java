package ua.in.badparking.services;

import java.util.ArrayList;
import java.util.List;

import ua.in.badparking.model.Claim;
import ua.in.badparking.model.CrimeType;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ClaimState {
    INST;

    private Claim claim = new Claim();
    private List<CrimeType> crimeTypes = new ArrayList<>();
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public Claim getClaim() {
        return claim;
    }

    public List<CrimeType> getCrimeTypes() {
        return crimeTypes;
    }

    public void setCrimeTypes(List<CrimeType> crimeTypes) {
        this.crimeTypes = crimeTypes;
    }
}

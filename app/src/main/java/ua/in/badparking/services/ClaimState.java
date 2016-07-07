package ua.in.badparking.services;

import ua.in.badparking.model.Claim;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ClaimState {
    INST;

    private Claim claim = new Claim();

    public Claim getClaim() {
        return claim;
    }
}

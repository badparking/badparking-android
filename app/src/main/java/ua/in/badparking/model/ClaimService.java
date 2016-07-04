package ua.in.badparking.model;

import ua.in.badparking.data.Claim;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ClaimService {
    INST;

    private Claim claim = new Claim();

    public Claim getClaim() {
        return claim;
    }
}

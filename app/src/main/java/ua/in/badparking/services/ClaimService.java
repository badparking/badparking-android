package ua.in.badparking.services;

import ua.in.badparking.model.Claim;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ClaimService {
    INST;

    private Claim claim = new Claim();

//    public ClaimRequest getClaimRequest(List<File> images, Location location,) {
//        return null;
//    }

    public Claim getClaim() {
        return claim;
    }
}

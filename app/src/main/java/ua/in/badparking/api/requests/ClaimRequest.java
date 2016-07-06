package ua.in.badparking.api.requests;

import java.util.List;

import ua.in.badparking.model.Claim;

public class ClaimRequest {
    private List<Claim> claims;

    public ClaimRequest(List<Claim> claims) {
        this.claims = claims;
    }
}

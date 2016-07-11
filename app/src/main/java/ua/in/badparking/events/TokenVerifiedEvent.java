package ua.in.badparking.events;

public class TokenVerifiedEvent {
    Boolean verificationResult;

    public TokenVerifiedEvent(Boolean verificationResult) {
        this.verificationResult = verificationResult;
    }

    public Boolean getVerificationResult() {
        return verificationResult;
    }
}

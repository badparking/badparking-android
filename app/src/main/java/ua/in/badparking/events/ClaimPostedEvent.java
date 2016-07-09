package ua.in.badparking.events;

public class ClaimPostedEvent {
    private String message;

    public String getMessage() {
        return message;
    }

    public ClaimPostedEvent(String message) {
        this.message = message;
    }
}

package ua.in.badparking.events;

public class ClaimPostedEvent {
    private String message;
    private Boolean isPosted;

    public String getMessage() {
        return message;
    }

    public ClaimPostedEvent(String message, Boolean isPosted) {
        this.message = message;
        this.isPosted = isPosted;
    }

    public Boolean getPosted() {
        return isPosted;
    }
}

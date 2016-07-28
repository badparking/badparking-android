package ua.in.badparking.events;

public class ClaimPostedEvent {
    private String message;
    private Boolean isPosted;
    private String pk;

    public String getMessage() {
        return message;
    }

    public ClaimPostedEvent(String pk, String message, Boolean isPosted) {
        this.message = message;
        this.isPosted = isPosted;
        this.pk = pk;
    }

    public Boolean getPosted() {
        return isPosted;
    }

    public String getPk() {
        return pk;
    }
}

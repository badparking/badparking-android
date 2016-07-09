package ua.in.badparking.events;


public class AuthCompleteEvent {

    private String token;

    public AuthCompleteEvent(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

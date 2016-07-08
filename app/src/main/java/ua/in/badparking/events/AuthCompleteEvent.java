package ua.in.badparking.events;


public class AuthCompleteEvent {

    String token;

    public AuthCompleteEvent(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

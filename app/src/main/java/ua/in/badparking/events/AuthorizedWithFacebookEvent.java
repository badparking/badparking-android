package ua.in.badparking.events;

import ua.in.badparking.model.User;

public class AuthorizedWithFacebookEvent {
    private final User user;

    public AuthorizedWithFacebookEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

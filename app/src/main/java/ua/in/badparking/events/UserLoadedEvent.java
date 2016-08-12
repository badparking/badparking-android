package ua.in.badparking.events;

import ua.in.badparking.model.User;

public class UserLoadedEvent {
    private final User user;

    public UserLoadedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

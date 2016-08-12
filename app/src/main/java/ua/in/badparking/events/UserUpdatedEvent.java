package ua.in.badparking.events;

import ua.in.badparking.model.User;

public class UserUpdatedEvent {
    private final User user;

    public UserUpdatedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}

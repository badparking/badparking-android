package ua.in.badparking.events;

/**
 * Created by Dima Kovalenko on 9/21/16.
 */
public class ShowHeaderEvent {
    private boolean show;

    public ShowHeaderEvent(boolean show) {
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }
}

package ua.in.badparking.events;

public class ImageUploadedEvent {
    int imageCounter;

    public ImageUploadedEvent(int imageCounter) {
        this.imageCounter = imageCounter;
    }

    public int getImageCounter() {
        return imageCounter;
    }
}

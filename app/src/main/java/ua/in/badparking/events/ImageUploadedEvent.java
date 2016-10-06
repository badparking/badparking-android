package ua.in.badparking.events;

public class ImageUploadedEvent {
    boolean filesUploaded;

    public ImageUploadedEvent(boolean filesUploaded) {
        this.filesUploaded = filesUploaded;
    }

    public boolean getFilesUploaded() {
        return filesUploaded;
    }
}

package ua.in.badparking.model;

import java.io.File;

/**
 * Created by Dima Kovalenko on 7/9/16.
 */
public class MediaFile extends File {

    long timestamp;

    public MediaFile(String filePath, long currentTimeMillis) {
        super(filePath);

        this.timestamp = currentTimeMillis;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

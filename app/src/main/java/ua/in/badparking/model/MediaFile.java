package ua.in.badparking.model;

/**
 * Created by Dima Kovalenko on 7/9/16.
 */
public class MediaFile {

    String filePath;
    long timestamp;

    public MediaFile(String filePath, long currentTimeMillis) {

        this.filePath = filePath;
        this.timestamp = currentTimeMillis;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

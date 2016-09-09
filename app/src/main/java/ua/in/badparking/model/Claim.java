package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Claim implements Serializable {

    @SerializedName("pk")
    private String pk;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;
    @SerializedName("crimetypes")
    private Set<Integer> crimetypes = new HashSet<>();
    @SerializedName("city")
    private String city;
    @SerializedName("address")
    private String address;
    @SerializedName("license_plates")
    private String licensePlates;
    @SerializedName("images")
    private List<MediaFile> photoFiles = new ArrayList<MediaFile>();
    @SerializedName("user")
    private User user;
    @SerializedName("status")
    private String status;

    public String getStatus() {
        return status;
    }

    public String getLicensePlates() {
        return licensePlates;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public Set<Integer> getCrimetypes() {
        return crimetypes;
    }

    public void setLicensePlates(String licensePlates) {
        this.licensePlates = licensePlates;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCrimetypes(Set<Integer> crimetypes) {
        this.crimetypes = crimetypes;
    }

    public List<MediaFile> getPhotoFiles() {
        return photoFiles;
    }

    public void addPhoto(String filePath) {
        photoFiles.add(new MediaFile(filePath, System.currentTimeMillis()));
    }

    public void removePhoto(MediaFile mediaFile) {
        photoFiles.remove(mediaFile);
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setPhotoFiles(List<MediaFile> photoFiles) {
        this.photoFiles = photoFiles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Claim{" +
                "pk='" + pk + '\'' +
                ", license_plates='" + licensePlates + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", crimetypes=" + crimetypes +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", licensePlates='" + licensePlates + '\'' +
                ", photoFiles=" + photoFiles +
                ", user=" + user +
                '}';
    }

    public boolean isComplete() {
        return true;
    }
}

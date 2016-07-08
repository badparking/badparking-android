package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Claim implements Serializable{

    @SerializedName("pk")
    private String pk;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;
    @SerializedName("crimetypes")
    private List<CrimeType> crimetypes;
    @SerializedName("city")
    private String city;
    @SerializedName("address")
    private String address;
    @SerializedName("license_plates")
    private String licensePlates;
    @SerializedName("images")
    private List<MediaFileSerializer> photoFiles;
    @SerializedName("user")
    private User user;


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

    public List<CrimeType> getCrimetypes() {
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

    public void setCrimetypes(List<CrimeType> crimetypes) {
        this.crimetypes = crimetypes;
    }

    public List<MediaFileSerializer> getPhotoFiles() {
        return photoFiles;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public void setPhotoFiles(List<MediaFileSerializer> photoFiles) {
        this.photoFiles = photoFiles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public class MediaFileSerializer {

    }
}

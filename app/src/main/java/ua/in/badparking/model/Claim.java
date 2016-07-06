package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Claim {

    @SerializedName("pk")
    private String pk;
    @SerializedName("license_plates")
    private String licensePlates;
    @SerializedName("longitude")
    private String longitude;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("city")
    private String city;
    @SerializedName("address")
    private String address;
    @SerializedName("crimetypes")
    private String crimetypes;
    private List<File> photoFiles = new ArrayList<File>();


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

    public String getCrimetypes() {
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

    public void setCrimetypes(String crimetypes) {
        this.crimetypes = crimetypes;
    }

    public List<File> getPhotoFiles() {
        return photoFiles;
    }
}

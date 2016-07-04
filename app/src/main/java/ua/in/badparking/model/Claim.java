package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

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

}

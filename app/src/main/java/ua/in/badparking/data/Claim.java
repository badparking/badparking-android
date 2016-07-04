package ua.in.badparking.data;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public class Claim {

    @SerializedName("address")
    private String address;

    @SerializedName("lng")
    private float lng;

    @SerializedName("lat")
    private float lat;

    @SerializedName("caseDt")
    private long caseDt;

    @SerializedName("case_type_id")
    private String caseTypeId;

    @SerializedName("number_plates")
    private String numberPlates;

    private List<File> photoFiles = new ArrayList<File>();
    private String city;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setCaseDt(long caseDt) {
        this.caseDt = caseDt;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public void setNumberPlates(String numberPlates) {
        this.numberPlates = numberPlates;
    }

    public void clearPhotos() {
        photoFiles.clear();
    }

    public void addPhoto(File photo) {
        photoFiles.add(photo);
    }

    public List<File> getPhotoFiles() {
        return photoFiles;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
}

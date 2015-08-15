package ua.in.badparking.data;

import com.google.gson.annotations.SerializedName;

import java.io.File;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public class Trespass {

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

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

    private File image1;
    private File image2;

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

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

    public void setImage1(File image1) {
        this.image1 = image1;
    }

    public void setImage2(File image2) {
        this.image2 = image2;
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}

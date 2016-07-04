package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("middle_name")
    private String middleName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("full_name")
    private String fullName;
    private String email;
    private String dob;
    private String inn;
    private String phone;
    @SerializedName("is_complete")
    private String isComplete;

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getDob() {
        return dob;
    }

    public String getInn() {
        return inn;
    }

    public String getPhone() {
        return phone;
    }

    public String getIsComplete() {
        return isComplete;
    }
}

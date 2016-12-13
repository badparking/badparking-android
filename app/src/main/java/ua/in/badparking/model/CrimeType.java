package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

public class CrimeType {

    @SerializedName("id")
    private Integer id;

    @SerializedName("name")
    private String name;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrimeType crimeType = (CrimeType) o;

        if (id != null ? !id.equals(crimeType.id) : crimeType.id != null) return false;
        return name != null ? name.equals(crimeType.name) : crimeType.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}

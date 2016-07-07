package ua.in.badparking.model;

import com.google.gson.annotations.SerializedName;

public class CrimeType {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;
    private boolean selected;

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    //temp setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}

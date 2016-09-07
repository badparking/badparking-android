package ua.in.badparking.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.in.badparking.model.Claim;
import ua.in.badparking.model.CrimeType;
import ua.in.badparking.model.MediaFile;

/**
 * Created by Dima Kovalenko on 8/15/15.
 */
public enum ClaimState {
    INST;

    private String license_plates;
    private Claim claim = new Claim();
    private List<CrimeType> crimeTypes = new ArrayList<>();
    private String token;
    private String pk;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public Claim getClaim() {
        return claim;
    }

    public List<CrimeType> getCrimeTypes() {
        return crimeTypes;
    }

    public void setCrimeTypes(List<CrimeType> crimeTypes) {
        this.crimeTypes = crimeTypes;
    }

    public List<CrimeType> getSelectedCrimeTypes() {
        List<CrimeType> selectedCrimeTypeList = new ArrayList<>();

        for (CrimeType ct : crimeTypes) {
            if (ct.isSelected()) {
                selectedCrimeTypeList.add(ct);
            }
        }

        return selectedCrimeTypeList;
    }

    public String getSelectedCrimeTypesNames() {
        StringBuilder sb = new StringBuilder();
        Iterator<CrimeType> crimeTypeIterator = getSelectedCrimeTypes().iterator();

        while (crimeTypeIterator.hasNext()) {
            CrimeType ct = crimeTypeIterator.next();

            sb.append("- ").append(ct.getName());
            if (crimeTypeIterator.hasNext()) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String getFullAddress(){
        if(claim.getCity()!= null && claim.getAddress()!= null) {
            return getClaim().getCity() + ", " + getClaim().getAddress();
        }
        return "";
    }

    public List<MediaFile> getPictures() {
        return claim.getPhotoFiles();
    }

    public void setPk(String pk) {
        this.pk = pk;
    }
}
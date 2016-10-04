package ua.in.badparking.events;

import java.util.List;

import ua.in.badparking.model.CrimeType;

public class CrimeTypeEvent {
    private List<CrimeType> _crimeTypeList;

    public CrimeTypeEvent(List<CrimeType> crimeTypeList) {
        _crimeTypeList = crimeTypeList;
    }

    public List<CrimeType> getCrimeTypeList() {
        return _crimeTypeList;
    }
}

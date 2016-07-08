package ua.in.badparking.events;

import java.util.List;

import ua.in.badparking.model.CrimeType;

public class TypesLoadedEvent {
    List<CrimeType> crimeTypes;

    public TypesLoadedEvent(List<CrimeType> crimeTypes) {
        this.crimeTypes = crimeTypes;
    }

    public List<CrimeType> getCrimeTypes() {
        return crimeTypes;
    }
}

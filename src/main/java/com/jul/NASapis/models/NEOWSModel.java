package com.jul.NASapis.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NEOWSModel {
    int neoReferenceId;
    String name;
    String nasaJplUrl;
    boolean isPotentiallyHazardousAsteroid;

    public NEOWSModel(int neoRefId, String name, String nasaJplUrl, boolean isDangerous) {
        this.neoReferenceId = neoRefId;
        this.name = name;
        this.nasaJplUrl = nasaJplUrl;
        this.isPotentiallyHazardousAsteroid = isDangerous;
    }
}

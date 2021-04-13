package com.jul.NASapis.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EONETModel {
    String id;
    String title;
    String url;

    public EONETModel(String id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    public EONETModel(String id, String title) {
        this.id = id;
        this.title = title;
    }
}

package com.jul.NASapis.models;

import lombok.Getter;

@Getter
public class APODModel {
    String concept_tags;
    String date;
    String hd;
    Integer count;
    String start_date;
    String end_date;
    boolean thumbs;
    String explanation;
    String title;
    String url;

    public APODModel(String dateJson, String explanationJson, String hdurlJson, String titleJson, String urlJson) {
        this.date = dateJson;
        this.explanation = explanationJson;
        this.hd = hdurlJson;
        this.title = titleJson;
        this.url = urlJson;
    }
}

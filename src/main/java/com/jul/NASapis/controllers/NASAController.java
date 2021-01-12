package com.jul.NASapis.controllers;

import com.jul.NASapis.models.APODModel;
import com.jul.NASapis.models.NEOWSModel;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/nasa")
public class NASAController {
    @Value("${nasa.api.key}") String TOKEN;

    /**
     * @param concept_tags
     * A boolean indicating whether concept tags should be returned with the rest of the response.
     * The concept tags are not necessarily included in the explanation, but rather derived from common search tags that are associated with the description text.
     * (Better than just pure text search.) Defaults to False.
     *
     * @param date
     * A string in YYYY-MM-DD format indicating the date of the APOD image (example: 2014-11-03).
     *@param hd
     * A boolean parameter indicating whether or not high-resolution images should be returned.
     * This is present for legacy purposes, it is always ignored by the service and high-resolution urls are returned regardless.
     *@param count
     * A positive integer, no greater than 100.
     * If this is specified then count randomly chosen images will be returned in a JSON array.
     * CANNOT BE USED IN CONJUNCTION WITH DATE OR START_DATE AND END_DATE.
     *@param start_date
     * A string in YYYY-MM-DD format indicating the start of a date range.
     * All images in the range from start_date to end_date will be returned in a JSON array.
     * CANNOT BE USED WITH DATE.
     *@param end_date
     * A string in YYYY-MM-DD format indicating that end of a date range.
     * If start_date is specified without an end_date then end_date defaults to the current date.
     *@param thumbs
     * If set to true, the API returns URL of video thumbnail.
     * @return
     */
    @GetMapping("/apod")
    public Map<String, Object> apod(@RequestParam(required = false, defaultValue = "false") boolean concept_tags,
                         @RequestParam(required = false) String date,
                         @RequestParam(required = false, defaultValue = "false") boolean hd,
                         @RequestParam(required = false, defaultValue = "10") Integer count,
                         @RequestParam(required = false) String start_date,
                         @RequestParam(required = false) String end_date,
                         @RequestParam(required = false, defaultValue = "false") boolean thumbs){

        JsonNode response = Unirest.get("https://api.nasa.gov/planetary/apod")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("concept_tags", concept_tags)
                .queryString("date", date)
                .queryString("hd", hd)
                //.queryString("count", count)
                //.queryString("start_date", start_date)
                //.queryString("end_date", end_date)
                .queryString("thumbs", thumbs)
                .asJson()
                .getBody();

        JSONArray array = response.getArray();
        List<APODModel> list = new ArrayList<>();
        for(int i = 0 ; i < array.length(); i++){
            String dateJson = array.getJSONObject(i).getString("date");
            String explanationJson = array.getJSONObject(i).getString("explanation");
            String hdurlJson = array.getJSONObject(i).getString("hdurl");
            String titleJson = array.getJSONObject(i).getString("title");
            String urlJson = array.getJSONObject(i).getString("url");

            list.add(new APODModel(dateJson, explanationJson, hdurlJson, titleJson, urlJson));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("Info", list);
        return map;
    }

    @GetMapping("/neo/feed")
    public Map<String, Object> neo(@RequestParam String start_date,
                        @RequestParam(required = false) String end_date,
                        @RequestParam(required = false, defaultValue = "true") boolean detailed) {
        JsonNode response = Unirest.get("https://api.nasa.gov/neo/rest/v1/feed")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("start_date", start_date)
                .queryString("end_date", end_date)
                .queryString("detailed", detailed)
                .asJson()
                .getBody();

        JSONArray array = response.getArray();
        List<NEOWSModel> list = new ArrayList<>();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for(int i = 0 ; i <  response.getArray().length(); i++){
            JSONObject object = array.getJSONObject(i);
            JSONObject nearEarthObjects = object.getJSONObject("near_earth_objects");
            JSONArray date  = nearEarthObjects.getJSONArray(start_date);

            for(int j = 0 ; j < date.length(); j++) {
                    int neoRefId = date.getJSONObject(j).getInt("neo_reference_id");
                    String name = date.getJSONObject(j).getString("name");
                    String nasaJplUrl = date.getJSONObject(j).getString("nasa_jpl_url");
                    boolean isDangerous = date.getJSONObject(j).getBoolean("is_potentially_hazardous_asteroid");
                    list.add(new NEOWSModel(neoRefId, name, nasaJplUrl, isDangerous));
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("Near earth objects", list);
        return map;
    }
}

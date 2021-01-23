package com.jul.NASapis.controllers;

import com.jul.NASapis.models.APODModel;
import com.jul.NASapis.models.NEOWSModel;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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
     * Astronomy Picture of the Day
     *
     * @param concept_tags
     * A boolean indicating whether concept tags should be returned with the rest of the response.
     * The concept tags are not necessarily included in the explanation, but rather derived from common search tags that are associated with the description text.
     * (Better than just pure text search.) Defaults to False.
     *
     * @param date
     * A string in YYYY-MM-DD format indicating the date of the APOD image (example: 2014-11-03).
     *
     *@param hd
     * A boolean parameter indicating whether or not high-resolution images should be returned.
     * This is present for legacy purposes, it is always ignored by the service and high-resolution urls are returned regardless.
     *
     *@param count
     * A positive integer, no greater than 100.
     * If this is specified then count randomly chosen images will be returned in a JSON array.
     * CANNOT BE USED IN CONJUNCTION WITH DATE OR START_DATE AND END_DATE.
     *
     *@param start_date
     * A string in YYYY-MM-DD format indicating the start of a date range.
     * All images in the range from start_date to end_date will be returned in a JSON array.
     * CANNOT BE USED WITH DATE.
     *
     *@param end_date
     * A string in YYYY-MM-DD format indicating that end of a date range.
     * If start_date is specified without an end_date then end_date defaults to the current date.
     *
     *@param thumbs
     * If set to true, the API returns URL of video thumbnail.
     */
    @GetMapping("/planetary/apod")
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

    /*Near Earth Object Web Service*/
    @GetMapping("/neo/feed")
    public Map<String, Object> neo(@RequestParam(required = false) String start_date,
                        @RequestParam(required = false) String end_date,
                        @RequestParam(required = false, defaultValue = "true") boolean detailed) {

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

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

//                for(int j = 0 ; j < date.length(); j++){
//                    for (LocalDate start = (LocalDate) startTime; start.isBefore((ChronoLocalDate) endTime); start = start.plusDays(1)){
//                        int neoRefId = date.getJSONObject(j).getInt("neo_reference_id");
//                        String name = date.getJSONObject(j).getString("name");
//                        String nasaJplUrl = date.getJSONObject(j).getString("nasa_jpl_url");
//                        boolean isDangerous = date.getJSONObject(j).getBoolean("is_potentially_hazardous_asteroid");
//                        list.add(new NEOWSModel(neoRefId, name, nasaJplUrl, isDangerous));
//            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("Near earth objects", list);
        return map;
    }

    @GetMapping("/neo/browse")
    public String browseNeo(){
        JsonNode response = Unirest.get("https://api.nasa.gov/neo/rest/v1/neo/browse/")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .asJson()
                .getBody();
        return response.toPrettyString();
    }

    /*Coronal Mass Ejection*/
    @GetMapping("/DONKI/CME")
    public String donkiCME(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/CME")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /**
     *
     * @param mostAccurateOnly
     * default is set to true
     *
     * @param completeEntryOnly
     * default is set to true
     *
     * @param speed
     * default is set to 0
     *
     * @param halfAngle
     * default is set to 0
     *
     * @param catalog
     * default is set to ALL (choices: ALL, SWRC_CATALOG, JANG_ET_AL_CATALOG)
     *
     * @param keyword
     * default is set to NONE (example choices: swpc_annex)
     */
    @GetMapping("/DONKI/CMEAnalysis")
    public String donkiCMEAnalysis(@RequestParam(required = false) String start_date,
                                   @RequestParam(required = false) String end_date,
                                   @RequestParam(required = false, defaultValue = "true") boolean mostAccurateOnly,
                                   @RequestParam(required = false, defaultValue = "true") boolean completeEntryOnly,
                                   @RequestParam(required = false, defaultValue = "0") Integer speed,
                                   @RequestParam(required = false, defaultValue = "0") Integer halfAngle,
                                   @RequestParam(required = false, defaultValue = "ALL") String catalog,
                                   @RequestParam(required = false, defaultValue = "NONE") String keyword){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/CMEAnalysis")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .queryString("mostAccurateOnly", mostAccurateOnly)
                .queryString("completeEntryOnly", completeEntryOnly)
                .queryString("speed", speed)
                .queryString("halfAngle", halfAngle)
                .queryString("catalog", catalog)
                .queryString("keyword", keyword)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /*Geomagnetic Storm*/
    @GetMapping("/DONKI/GST")
    public String donkiGST(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/GST")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /**
     * Interplanetary Shock
     *
     * @param location
     * default to ALL (choices: Earth, MESSENGER, STEREO A, STEREO B)
     *
     * @param catalog
     * default to ALL (choices: SWRC_CATALOG, WINSLOW_MESSENGER_ICME_CATALOG)
     */
    @GetMapping("/DONKI/IPS")
    public String donkiIPS(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date,
                           @RequestParam(required = false, defaultValue = "ALL") String location,
                           @RequestParam(required = false, defaultValue = "ALL") String catalog){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/IPS")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .queryString("location", location)
                .queryString("catalog", catalog)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /*Solar Flare*/
    @GetMapping("/DONKI/FLR")
    public String donkiFLR(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/FLR")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /*Solar Energetic Particle*/
    @GetMapping("/DONKI/SEP")
    public String donkiSEP(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/SEP")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /*Magnetopause Crossing*/
    @GetMapping("/DONKI/MPC")
    public String donkiMPC(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/MPC")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /*Radiation Belt Enhancement*/
    @GetMapping("/DONKI/RBE")
    public String donkiRBE(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/RBE")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /*High Speed Stream*/
    @GetMapping("/DONKI/HSS")
    public String donkiHSS(@RequestParam(required = false) String start_date,
                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/HSS")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    @GetMapping("/DONKI/WSAEnlilSimulations")
    public String donkiWSAEnlilSimulations(@RequestParam(required = false) String start_date,
                                           @RequestParam(required = false) String end_date){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/WSAEnlilSimulations")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    /**
     *
     * @param type
     * could be: all, FLR, SEP, CME, IPS, MPC, GST, RBE, report
     */
    @GetMapping("/DONKI/notifications")
    public String donkiNotifications(@RequestParam(required = false) String start_date,
                                     @RequestParam(required = false) String end_date,
                                     @RequestParam(required = false, defaultValue = "all") String type){

        if(end_date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            end_date = dtf.format(now);
        }

        JsonNode response = Unirest.get("https://api.nasa.gov/DONKI/notifications")
                .header("content-type", "application/json")
                .queryString("api_key", TOKEN)
                .queryString("startDate", start_date)
                .queryString("endDate", end_date)
                .queryString("type", type)
                .asJson()
                .getBody();

        return response.toPrettyString();
    }

    @GetMapping(value = "/planetary/earth", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] earth(@RequestParam(required = false) Float latitude,
                        @RequestParam(required = false) Float longitude,
                        @RequestParam(required = false, defaultValue = "0.025") Float dim,
                        @RequestParam(required = false) String date){

        if(date == null){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime now = LocalDateTime.now();
            date = dtf.format(now);
        }

        byte[] response = Unirest.get("https://api.nasa.gov/planetary/earth/imagery")
                .header("content-type", MediaType.IMAGE_PNG_VALUE)
                .queryString("api_key", TOKEN)
                .queryString("lat", latitude)
                .queryString("lon", longitude)
                .queryString("dim", dim)
                .queryString("date", date)
                .asBytes()
                .getBody();

        return response;
    }
}

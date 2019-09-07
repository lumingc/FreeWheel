package com.cs160.freewheel;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
    Code credits to Google directions API examples
*/

public class DirectionsJSONParser {

    private String arrival = "";
    private String departure = "";
    private String agency_name = "";
    private String agency_phone = "";
    private String agency_url = "";
    private String route_name_long = "";
    private String route_name = "";
    private String transit_type = "";
    private String arrival_stop = "";
    private String departure_stop = "";



    public List<List<HashMap<String,String>>> parse(JSONObject jObject){

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        boolean hasTravelDetails = false;

        try {

            jRoutes = jObject.getJSONArray("routes");


            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                List path = new ArrayList<HashMap<String, String>>();


                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");


                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);

                        String mode = (String)(((JSONObject)jSteps.get(k)).get("travel_mode"));


                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );

                            hm.put("mode", mode);

                            path.add(hm);
                        }

                        if (!hasTravelDetails && mode.equals("TRANSIT")) {
                            hasTravelDetails = true;
                            System.out.println("getting details");
                            arrival = (String)((JSONObject)((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("arrival_time")).get("text");
                            System.out.println("getting details2");
                            departure = (String)((JSONObject)((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("departure_time")).get("text");


                            JSONObject line = ((JSONObject)((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details")).get("line"));
                            if ( ((JSONObject)((JSONArray)line.getJSONArray("agencies")).get(0)).has("name")) {
                                agency_name = (String)((JSONObject)((JSONArray)line.getJSONArray("agencies")).get(0)).get("name");
                            }
                            if ( ((JSONObject)((JSONArray)line.getJSONArray("agencies")).get(0)).has("phone")) {
                                agency_phone = (String)((JSONObject)((JSONArray)line.getJSONArray("agencies")).get(0)).get("phone");
                            }
                            if ( ((JSONObject)((JSONArray)line.getJSONArray("agencies")).get(0)).has("url")) {
                                agency_url = (String)((JSONObject)((JSONArray)line.getJSONArray("agencies")).get(0)).get("url");
                            }
                            if (line.has("name")) {
                                route_name_long = (String)line.get("name");
                            }
                            if (line.has("short_name")) {
                                route_name = (String)line.get("short_name");
                            }
                            if (((JSONObject)line.get("vehicle")).has("name")) {
                                transit_type = (String)((JSONObject)line.get("vehicle")).get("name");
                            }

                            JSONObject transit_details = ((JSONObject)((JSONObject)jSteps.get(k)).get("transit_details"));
                            if (((JSONObject)(transit_details.get("arrival_stop"))).has("name")) {
                                arrival_stop = (String)((JSONObject)(transit_details.get("arrival_stop"))).get("name");
                            }
                            if (((JSONObject)(transit_details.get("departure_stop"))).has("name")) {
                                departure_stop = (String)((JSONObject)(transit_details.get("departure_stop"))).get("name");
                            }

                            //System.out.println("got all details");
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public ArrayList<String> getRouteDetails() {
        ArrayList<String> details = new ArrayList<>();
        details.add(arrival);
        details.add(departure);
        details.add(agency_name);
        details.add(agency_phone);
        details.add(agency_url);
        details.add(route_name_long);
        details.add(route_name);
        details.add(transit_type);
        details.add(arrival_stop);
        details.add(departure_stop);

        System.out.println("array list " + details);
        return details;
    }
}
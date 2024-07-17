package com.expediodigital.ventas360.util;

/**
 * Created by Nelson on 28/03/16.
 */

import android.util.Log;

import com.expediodigital.ventas360.model.DirectionApiModel;
import com.expediodigital.ventas360.model.DirectionApiResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DirectionsJSONParser {
    final String TAG = DirectionsJSONParser.class.getName();

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public DirectionApiResponse parse(JSONObject jObject){
        DirectionApiResponse directionApiResponse = new DirectionApiResponse();

        List<DirectionApiModel> rutas = new ArrayList<>();
        List<Integer> listaOrden = new ArrayList<>();

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jOrder = null;
        JSONArray jSteps = null;
        JSONObject jDistance = null;
        JSONObject jDuration = null;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                jOrder = ( (JSONObject)jRoutes.get(i)).getJSONArray("waypoint_order");

                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    DirectionApiModel directionApiModel = new DirectionApiModel();

                    /*Obtener el orden del punto*/
                    //int orden = (int) jOrder.get(j);
                    //directionApiModel.setOrden(orden);

                    /** Getting distance from the json data */
                    jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                    directionApiModel.setDistance(jDistance.getInt("value"));
                    //HashMap<String, String> hmDistance = new HashMap<String, String>();
                    //hmDistance.put("distance", jDistance.getString("text"));

                    /** Getting duration from the json data */
                    jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                    directionApiModel.setDuration(jDuration.getInt("value"));
                    //HashMap<String, String> hmDuration = new HashMap<String, String>();
                    //hmDuration.put("duration", jDuration.getString("text"));

                    /** Adding distance object to the path */
                    //path.add(hmDistance);

                    /** Adding duration object to the path */
                    //path.add(hmDuration);

                    jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
                    List<LatLng> stepsList = new ArrayList<>();
                    /** Traversing all steps */
                    for(int k=0;k<jSteps.length();k++){
                        String polyline = "";
                        polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                        stepsList.addAll(decodePoly(polyline));

                        /** Traversing all points
                        for(int l=0;l<list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            //hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            //hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            hm.put("lat", String.valueOf(list.get(l).latitude));
                            hm.put("lng", String.valueOf(list.get(l).longitude));
                            Log.d(TAG,">> lat: "+String.valueOf(list.get(l).latitude));
                            Log.d(TAG,">> lng: "+String.valueOf(list.get(l).longitude));
                            path.add(hm);
                        }
                        */
                    }
                    directionApiModel.setSteps(stepsList);

                    JSONObject jStartLocation = ((JSONObject) jLegs.get(j)).getJSONObject("start_location");
                    directionApiModel.setStartLocation(new LatLng(jStartLocation.getDouble("lat"),jStartLocation.getDouble("lng")));
                    JSONObject jEndLocation = ((JSONObject) jLegs.get(j)).getJSONObject("end_location");
                    directionApiModel.setEndLocation(new LatLng(jEndLocation.getDouble("lat"),jEndLocation.getDouble("lng")));

                    rutas.add(directionApiModel);
                }

                for(int j=0;j<jOrder.length();j++) {
                    /*Obtener el orden del punto*/
                    int orden = (int) jOrder.get(j);
                    listaOrden.add(orden);
                }


                directionApiResponse.setListaPuntos(rutas);
                directionApiResponse.setListaOrden(listaOrden);
                return directionApiResponse;

                //routes.add(path);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return directionApiResponse;
    }

    /**
     * Method to decode polyline points
     * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        poly = PolyUtil.decode(encoded);
        /*
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
        */
        return poly;
    }
}
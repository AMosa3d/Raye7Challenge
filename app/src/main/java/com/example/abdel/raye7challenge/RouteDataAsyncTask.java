package com.example.abdel.raye7challenge;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abdel on 6/11/2017.
 */

public class RouteDataAsyncTask extends AsyncTask<String,List<List<List<LatLng>>>,List<List<List<LatLng>>>> {

    final String API_KEY;
    final AsyncRespond asyncRespond;

    public RouteDataAsyncTask(String API_KEY, Context context) {
        this.API_KEY = API_KEY;
        this.asyncRespond = (AsyncRespond) context;
    }

    @Override
    protected List<List<List<LatLng>>> doInBackground(String... params) {

        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;
        String routesJSON = "";

        final String ROUTES_URL1 = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        final String ROUTES_URL2 = "&destination=";
        final String ROUTES_URL3 = "&key=";
        final String ROUTES_URL4 = "&alternatives=true";     //multiple roots

        String UrlString = ROUTES_URL1 + params[0] + ROUTES_URL2 + params[1] + ROUTES_URL4 + ROUTES_URL3 + API_KEY;

        try {
            URL url = new URL(UrlString);

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream stream = httpURLConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();

            if (stream == null)//no data has returned
                return null;

            reader = new BufferedReader(new InputStreamReader(stream));

            String line = "";

            while ((line = reader.readLine()) != null)
                buffer.append(line + "\n");

            if (buffer == null)
                return null;

            routesJSON = buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
            routesJSON = null;
        } catch (IOException e) {
            e.printStackTrace();
            routesJSON = null;
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        try {
            return JSONParser(routesJSON);
        } catch (JSONException e) {
            Log.e("JSON", e.getMessage());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<List<List<LatLng>>> routes) {

        if (routes == null)
            return;

        //asyncRespond.processFinished(routes);


    }


    List<List<List<LatLng>>> JSONParser(String routesJSON) throws JSONException {

        final String ROUTES_ID = "routes";
        final String LEGS_ID = "legs";
        final String STEPS_ID = "steps";
        final String POLYLINE_ID = "polyline";
        final String POINTS_ID = "points";

        List<List<List<LatLng>>> routes = new ArrayList<>();//big list for every route - second list for every leg
        // last list for every step's polyline

        JSONArray jsonRoutes = null, jsonLegs = null, jsonSteps = null;

        JSONObject jsonData = new JSONObject(routesJSON);
        jsonRoutes = jsonData.getJSONArray(ROUTES_ID);

        for (int i = 0; i < jsonRoutes.length(); i++) {
            jsonLegs = ((JSONObject) jsonRoutes.get(i)).getJSONArray(LEGS_ID);
            List<List<LatLng>> path = new ArrayList<>();

            for (int j = 0; j < jsonLegs.length(); j++) {
                jsonSteps = ((JSONObject) jsonLegs.get(j)).getJSONArray(STEPS_ID);


                for (int k = 0; k < jsonSteps.length(); k++) {
                    JSONObject object = ((JSONObject) jsonSteps.get(k));
                    object = (JSONObject) object.get(POLYLINE_ID);

                    String polyline = "";
                    polyline = (String) object.get(POINTS_ID);

                    List<LatLng> decodingPloylineResult = decodePoly(polyline);

                    path.add(decodingPloylineResult);
                }
                routes.add(path);
            }
        }

        asyncRespond.processFinished(routes);
        return routes;
    }


    //NOTE : I have searched for this all the time on internet and it seems that all the resources that i found used
    //this algorithm to convert the polylines to latlng points


    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
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
}
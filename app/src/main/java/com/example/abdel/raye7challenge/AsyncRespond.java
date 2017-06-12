package com.example.abdel.raye7challenge;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by abdel on 6/11/2017.
 */

public interface AsyncRespond {
    void processFinished (List<List<List<LatLng>>> r);
}
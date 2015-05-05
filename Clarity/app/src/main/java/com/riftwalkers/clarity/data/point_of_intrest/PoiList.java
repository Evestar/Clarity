package com.riftwalkers.clarity.data.point_of_intrest;

import android.content.Context;

import com.riftwalkers.clarity.data.database.PointsOfInterestDAO;

import java.util.ArrayList;

public class PoiList extends ArrayList<PointOfInterest> {

    private final PointsOfInterestDAO pointsOfInterestDAO;

    public PoiList(Context context) {

        pointsOfInterestDAO = new PointsOfInterestDAO(context);
        this.addAll(pointsOfInterestDAO.getAllPointsOfInterest());

    }

}

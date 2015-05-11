package com.riftwalkers.clarity.data.point_of_intrest;

import android.location.Location;

import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.LLACoordinate;

public class PointOfInterest {
    private int Id;
    private String featureId;
    private PoiType Type;
    private String Description;
    private Location Coordinate;
    private IGeometry geometry;

    public void setId(int id) {
        Id = id;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public void setType(PoiType type) {
        Type = type;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setCoordinate(Location coordinate) {
        Coordinate = coordinate;
    }

    public void setGeometry(IGeometry geometry) {
        this.geometry = geometry;
    }

    public int getId() {

        return Id;
    }

    public String getFeatureId() {
        return featureId;
    }

    public PoiType getType() {
        return Type;
    }

    public String getDescription() {
        return Description;
    }

    public Location getCoordinate() {
        return Coordinate;
    }

    public IGeometry getGeometry() {
        return geometry;
    }

    public String GetImageName()
    {
        switch (this.Type){
            case Ligplaats: return "POIa.png";
            case Boei: return "POIb.png";
            case Meerpaal: return "POIc.png";
            default: return "ExamplePOI.png";
        }
    }

    @Override
    public String toString(){
        return "OBJECTID: " + String.valueOf(getId()) + " FEATUREID: " + getFeatureId();
    }
}

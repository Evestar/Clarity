package com.riftwalkers.clarity;

import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.LLACoordinate;

public class PointOfInterest {
    private int Id;
    private PoiType Type;
    private String Description;
    private LLACoordinate Coordinate;
    private IGeometry geometry;

    public void setId(int id) {
        Id = id;
    }

    public void setType(PoiType type) {
        Type = type;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setCoordinate(LLACoordinate coordinate) {
        Coordinate = coordinate;
    }

    public void setGeometry(IGeometry geometry) {
        this.geometry = geometry;
    }

    public int getId() {

        return Id;
    }

    public PoiType getType() {
        return Type;
    }

    public String getDescription() {
        return Description;
    }

    public LLACoordinate getCoordinate() {
        return Coordinate;
    }

    public IGeometry getGeometry() {
        return geometry;
    }

    public String GetImageName()
    {
        switch (this.Type){
            case Schip: return "POIa.png";
            case Boei: return "POIb.png";
            case Bolder: return "POIc.png";
            default: return "ExamplePOI.png";
        }
    }
}

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
    private String materiaal;
    private String methodeVerankering;
    private int trekkracht;
    private String trekkrachtEenheid;
    private String typePaal;
    private int nummer;
    private String eigenaar;
    private String havenNaam;
    private String ligplaatsAfmeerType;
    private String oeverNummer;

    public String getMateriaal() {
        return materiaal;
    }

    public void setMateriaal(String materiaal) {
        this.materiaal = materiaal;
    }

    public String getMethodeVerankering() {
        return methodeVerankering;
    }

    public void setMethodeVerankering(String methodeVerankering) {
        this.methodeVerankering = methodeVerankering;
    }

    public int getTrekkracht() {
        return trekkracht;
    }

    public void setTrekkracht(int trekkracht) {
        this.trekkracht = trekkracht;
    }

    public String getTrekkrachtEenheid() {
        return trekkrachtEenheid;
    }

    public void setTrekkrachtEenheid(String trekkrachtEenheid) {
        this.trekkrachtEenheid = trekkrachtEenheid;
    }

    public String getTypePaal() {
        return typePaal;
    }

    public void setTypePaal(String typePaal) {
        this.typePaal = typePaal;
    }

    public int getNummer() {
        return nummer;
    }

    public void setNummer(int nummer) {
        this.nummer = nummer;
    }

    public String getEigenaar() {
        return eigenaar;
    }

    public void setEigenaar(String eigenaar) {
        this.eigenaar = eigenaar;
    }

    public String getHavenNaam() {
        return havenNaam;
    }

    public void setHavenNaam(String havenNaam) {
        this.havenNaam = havenNaam;
    }

    public String getLigplaatsAfmeerType() {
        return ligplaatsAfmeerType;
    }

    public void setLigplaatsAfmeerType(String ligplaatsAfmeerType) {
        this.ligplaatsAfmeerType = ligplaatsAfmeerType;
    }

    public String getOeverNummer() {
        return oeverNummer;
    }

    public void setOeverNummer(String oeverNummer) {
        this.oeverNummer = oeverNummer;
    }

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
            case Bolder: return "backup3.png";
            default: return "ExamplePOI.png";
        }
    }

    @Override
    public String toString(){
        return getDescription();
    }
}

package com.riftwalkers.clarity.data.point_of_intrest;

import android.location.Location;

import com.metaio.sdk.jni.IGeometry;

import java.util.LinkedList;

public class PointOfInterest {
    private int Id;                             // Alles
    private PoiType poiType;                    // Alles
    private String featureId;                   // Willekeurig
    private String description;                 // Bolders en Meerpalen, maar beperkt
    private String materiaal;                   // Bolders en meerpalen, maar beperkt
    private String methodeVerankering;          // Alleen bolders, maar beperkt
    private int trekkracht;                     // ALTIJD IN KN! Bolder en meerpalen, maar beperkt
    private String typePaal;                    // Alleen meerpalen, maar beperkt
    private int nummer;                         // Alleen meerpalen, maar beperkt
    private String eigenaar;                    // Alleen ligplaats, BIJNA allemaal
    private String havenNaam;                   // Alleen ligplaats, BIJNA allemaal
    private String oeverFrontNummer;            // Alleen ligplaats, BIJNA allemaal
    private String xmeTXT;                      // Alleen ligplaats, BIJNA allemaal -> Bijv. VOPAK 623
    private String lxmeTXT;                     // Alleen ligplaats, BIJNA allemaal -> uitgebreidere naam van xmeTXT. Bijv. NIEUWE MAAS VOPAK 623 = havenNaam + xmeTXT

    private LinkedList<Location> coordinates;   // Alles
    private IGeometry geometry;                 // Alles

    public PointOfInterest() {
        coordinates = new LinkedList<>();
    }

    public int getId() {
        return Id;
    }

    public PoiType getPoiType() {
        return poiType;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getDescription() {
        return description;
    }

    public String getMateriaal() {
        return materiaal;
    }

    public String getMethodeVerankering() {
        return methodeVerankering;
    }

    public int getTrekkracht() {
        return trekkracht;
    }

    public String getTypePaal() {
        return typePaal;
    }

    public int getNummer() {
        return nummer;
    }

    public String getEigenaar() {
        return eigenaar;
    }

    public String getHavenNaam() {
        return havenNaam;
    }

    public String getOeverFrontNummer() {
        return oeverFrontNummer;
    }

    public String getXmeTXT() {
        return xmeTXT;
    }

    public String getLxmeTXT() {
        return lxmeTXT;
    }

    public LinkedList<Location> getCoordinates() {
        return coordinates;
    }

    public IGeometry getGeometry() {
        return geometry;
    }

    public String GetImageName()
    {
        switch (this.poiType){
            case Ligplaats: return "POIa.png";
            case Meerpaal: return "POIc.png";
            case Bolder: return "backup3.png";
            default: return "ExamplePOI.png";
        }
    }

    public void setId(int id) {
        Id = id;
    }

    public void setPoiType(PoiType poiType) {
        this.poiType = poiType;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMateriaal(String materiaal) {
        this.materiaal = materiaal;
    }

    public void setMethodeVerankering(String methodeVerankering) {
        this.methodeVerankering = methodeVerankering;
    }

    public void setTrekkracht(int trekkracht) {
        this.trekkracht = trekkracht;
    }

    public void setTypePaal(String typePaal) {
        this.typePaal = typePaal;
    }

    public void setNummer(int nummer) {
        this.nummer = nummer;
    }

    public void setEigenaar(String eigenaar) {
        this.eigenaar = eigenaar;
    }

    public void setHavenNaam(String havenNaam) {
        this.havenNaam = havenNaam;
    }

    public void setOeverFrontNummer(String oeverFrontNummer) {
        this.oeverFrontNummer = oeverFrontNummer;
    }

    public void setXmeTXT(String xmeTXT) {
        this.xmeTXT = xmeTXT;
    }

    public void setLxmeTXT(String lxmeTXT) {
        this.lxmeTXT = lxmeTXT;
    }

    public void setGeometry(IGeometry geometry) {
        this.geometry = geometry;
    }

    public String getPaalNummer(){
        if(getDescription()==null) return null;
        String[] temp = getDescription().split(" ");
        String a =  temp[temp.length-1];

        try{
            int last = Integer.parseInt(String.valueOf(a.charAt(a.length() - 1)));

            return a;
        } catch (Exception e){

            return a.substring(0,a.length()-1);
        }
    }

    public String getPaalHaven() {
        if(getDescription()==null) return null;
        String[] temp = getDescription().split(" ");

        String a = "";
        for (int i = 0; i < temp.length - 1; i++) {
            a += temp[i]+" ";
        }
        return a.trim();
    }
}

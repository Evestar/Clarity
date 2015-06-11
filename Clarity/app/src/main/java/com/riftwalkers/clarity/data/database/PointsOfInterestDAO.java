package com.riftwalkers.clarity.data.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

import java.util.ArrayList;

public class PointsOfInterestDAO {
    private SQLiteDatabase db;
    private ClarityDBHelper helper;

    public PointsOfInterestDAO(Context context){
        helper = new ClarityDBHelper(context);
    }

    public ArrayList<PointOfInterest> getAllPointsOfInterest(){
        db = helper.getReadableDatabase();

        ArrayList<PointOfInterest> pointOfInterests = new ArrayList<>();

        String[] MEERPALENcolumns = {helper.UID, helper.FEATUREID, helper.DESCRIPTION, helper.MATERIAL, helper.TREKKRACHT, helper.TYPE_PAAL, helper.NUMMER, helper.COORDINATES};

        Cursor cursor = db.query(helper.MEERPALEN_TABLE_NAME,MEERPALENcolumns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();

            poi.setId(cursor.getInt(cursor.getColumnIndex(helper.UID)));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            poi.setPoiType(PoiType.Meerpaal);
            poi.setDescription(cursor.getString(cursor.getColumnIndex(helper.DESCRIPTION)));
            poi.setMateriaal(cursor.getString(cursor.getColumnIndex(helper.MATERIAL)));
            poi.setTrekkracht(cursor.getInt(cursor.getColumnIndex(helper.TREKKRACHT)));
            poi.setTypePaal(cursor.getString(cursor.getColumnIndex(helper.TYPE_PAAL)));
            poi.setNummer(cursor.getInt(cursor.getColumnIndex(helper.NUMMER)));

            String[] coordinates = cursor.getString(cursor.getColumnIndex(helper.COORDINATES)).split(" ");

            for (int i = 0; i < coordinates.length; i++) {
                String[] coordinateData = coordinates[i].split(",");

                String latitude = coordinateData[1];
                String longitude = coordinateData[0];

                Location location = new Location("");
                location.setLongitude(Double.parseDouble(longitude));
                location.setLatitude(Double.parseDouble(latitude));

                poi.getCoordinates().add(location);
            }

            pointOfInterests.add(poi);
        }

        String[] LIGPLLATSENcolumns = {helper.UID, helper.FEATUREID, helper.EIGENAAR, helper.HAVEN_NAAM, helper.OEVER_FRONT_NUMMER, helper.XMETXT, helper.LXMETXT, helper.COORDINATES};

        cursor = db.query(helper.LIGPLAATSEN_TABLE_NAME,LIGPLLATSENcolumns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();

            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            poi.setPoiType(PoiType.Ligplaats);
            poi.setEigenaar(cursor.getString(cursor.getColumnIndex(helper.EIGENAAR)));
            poi.setHavenNaam(cursor.getString(cursor.getColumnIndex(helper.HAVEN_NAAM)));
            poi.setOeverFrontNummer(cursor.getString(cursor.getColumnIndex(helper.OEVER_FRONT_NUMMER)));
            poi.setXmeTXT(cursor.getString(cursor.getColumnIndex(helper.XMETXT)));
            poi.setLxmeTXT(cursor.getString(cursor.getColumnIndex(helper.LXMETXT)));

            String[] coordinates = cursor.getString(cursor.getColumnIndex(helper.COORDINATES)).split(" ");

            for (int i = 0; i < coordinates.length; i++) {
                String[] coordinateData = coordinates[i].split(",");

                String latitude = coordinateData[1];
                String longitude = coordinateData[0];

                Location location = new Location("");
                location.setLongitude(Double.parseDouble(longitude));
                location.setLatitude(Double.parseDouble(latitude));

                poi.getCoordinates().add(location);
            }

            pointOfInterests.add(poi);
        }

        String[] BOLDERScolumns = {helper.UID, helper.FEATUREID, helper.DESCRIPTION, helper.MATERIAL, helper.METHODE_VERANKERING, helper.TREKKRACHT, helper.COORDINATES};

        cursor = db.query(helper.BOLDERS_TABLE_NAME,BOLDERScolumns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();

            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            poi.setPoiType(PoiType.Bolder);
            poi.setDescription(cursor.getString(cursor.getColumnIndex(helper.DESCRIPTION)));
            poi.setMateriaal(cursor.getString(cursor.getColumnIndex(helper.MATERIAL)));
            poi.setMethodeVerankering(cursor.getString(cursor.getColumnIndex(helper.METHODE_VERANKERING)));
            poi.setTrekkracht(cursor.getInt(cursor.getColumnIndex(helper.TREKKRACHT)));

            String[] coordinates = cursor.getString(cursor.getColumnIndex(helper.COORDINATES)).split(" ");

            for (int i = 0; i < coordinates.length; i++) {
                String[] coordinateData = coordinates[i].split(",");

                String latitude = coordinateData[1];
                String longitude = coordinateData[0];

                Location location = new Location("");
                location.setLongitude(Double.parseDouble(longitude));
                location.setLatitude(Double.parseDouble(latitude));

                poi.getCoordinates().add(location);
            }

            pointOfInterests.add(poi);
        }

        return pointOfInterests;
    }

    class ClarityDBHelper extends SQLiteAssetHelper {

        private static final String MEERPALEN_TABLE_NAME = "MEERPALEN";
        private static final String LIGPLAATSEN_TABLE_NAME = "LIGPLAATSEN";
        private static final String BOLDERS_TABLE_NAME = "BOLDERS";

        private static final String UID = "_id";
        private static final String FEATUREID = "FEATUREID";
        private static final String DESCRIPTION = "O_DESCR";
        private static final String MATERIAL = "O_MAT_ALG";
        private static final String METHODE_VERANKERING = "O_METH_VER";
        private static final String TREKKRACHT = "O_TOEL_TRK";
        private static final String TYPE_PAAL = "O_TYP_PLN";
        private static final String NUMMER = "NUMMER";
        private static final String EIGENAAR = "ZZEIGE";
        private static final String HAVEN_NAAM = "ZZHVNAAM";
        private static final String OEVER_FRONT_NUMMER = "ZZOEVFRN";
        private static final String XMETXT = "XMETXT";
        private static final String LXMETXT = "LXMETXT";
        private static final String COORDINATES = "coordinates";

        private static final String DATABASE_NAME = "Clarity";
        private static final int DATABASE_VERSION = 1;

        public ClarityDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    }
}



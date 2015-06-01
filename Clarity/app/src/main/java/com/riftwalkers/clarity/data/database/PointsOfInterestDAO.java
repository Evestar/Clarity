package com.riftwalkers.clarity.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;

import com.metaio.sdk.jni.LLACoordinate;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.riftwalkers.clarity.data.point_of_intrest.PoiType;
import com.riftwalkers.clarity.data.point_of_intrest.PointOfInterest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PointsOfInterestDAO {
    private SQLiteDatabase db;
    private ClarityDBHelper helper;

    public PointsOfInterestDAO(Context context){
        helper = new ClarityDBHelper(context);
    }

    /*public void insertJsonArray(JSONArray jsonArray){
        db = helper.getWritableDatabase();

        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                JSONObject properties = jsonObject.getJSONObject("properties");
                String objectId = properties.getString("OBJECTID");
                String featureId = properties.getString("FEATUREID");

                JSONObject geometry = jsonObject.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                String lat;
                String lng;

                if(geometry.getString("type").equals("Point")) {
                    lat = coordinates.getString(1);
                    lng = coordinates.getString(0);
                } else {
                    float totalLat = 0;
                    float totalLng = 0;

                    JSONArray polygonCoordinatesArray = coordinates.getJSONArray(0);
                    for (int j = 0; j < polygonCoordinatesArray.length(); j++) {
                        totalLat += Float.parseFloat(polygonCoordinatesArray.getJSONArray(j).get(1).toString());
                        totalLng += Float.parseFloat(polygonCoordinatesArray.getJSONArray(j).get(0).toString());
                    }

                    lat = Float.toString(totalLat/polygonCoordinatesArray.length());
                    lng = Float.toString(totalLng/polygonCoordinatesArray.length());
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(helper.UID, objectId);
                contentValues.put(helper.FEATUREID, featureId);
                contentValues.put(helper.LATITUDE, lat);
                contentValues.put(helper.LONGITUDE, lng);
                if(properties.has("ASTNAME") && (properties.getString("ASTNAME").equals("Meerpaal"))) {
                    this.db.insert(helper.MEERPALEN_TABLE_NAME, null, contentValues);
                } else if(geometry.getString("type").equals("Polygon")) {
                    this.db.insert(helper.LIGPLAATSEN_TABLE_NAME, null, contentValues);
                } else {
                    this.db.insert(helper.BOEIEN_TABLE_NAME, null, contentValues);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }*/

    public ArrayList<PointOfInterest> getAllPointsOfInterest(){
        db = helper.getReadableDatabase();

        ArrayList<PointOfInterest> pointOfInterests = new ArrayList<>();

        String[] columns = {helper.UID, helper.FEATUREID, helper.LATITUDE, helper.LONGITUDE, helper.DESCRIPTION,
                helper.MATERIAL, helper.METHODE_VERANKERING, helper.TREKKRACHT, helper.TREKKRACHT_EENHEID, helper.TYPE_PAAL,
                helper.NUMMER, helper.EIGENAAR, helper.HAVEN_NAAM, helper.LIGPLAATS_AFMEER_TYPE, helper.OEVER_NUMMER};

        Cursor cursor = db.query(helper.MEERPALEN_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(cursor.getInt(cursor.getColumnIndex(helper.UID)));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))));
            location.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))));

            poi.setCoordinate(location);
                    /*new LLACoordinate(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE)),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));*/
            poi.setType(PoiType.Meerpaal);

            poi.setDescription(cursor.getString(cursor.getColumnIndex(helper.DESCRIPTION)));
            poi.setMateriaal(cursor.getString(cursor.getColumnIndex(helper.MATERIAL)));
            poi.setTrekkracht(cursor.getInt(cursor.getColumnIndex(helper.TREKKRACHT)));
            poi.setTrekkrachtEenheid(cursor.getString(cursor.getColumnIndex(helper.TREKKRACHT_EENHEID)));
            poi.setTypePaal(cursor.getString(cursor.getColumnIndex(helper.TYPE_PAAL)));
            poi.setNummer(cursor.getInt(cursor.getColumnIndex(helper.NUMMER)));

            pointOfInterests.add(poi);
        }

        cursor = db.query(helper.LIGPLAATSEN_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))));
            location.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))));

            poi.setCoordinate(location);
                    /*new LLACoordinate(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));*/
            poi.setType(PoiType.Ligplaats);

            poi.setDescription(cursor.getString(cursor.getColumnIndex(helper.DESCRIPTION)));
            poi.setEigenaar(cursor.getString(cursor.getColumnIndex(helper.EIGENAAR)));
            poi.setHavenNaam(cursor.getString(cursor.getColumnIndex(helper.HAVEN_NAAM)));
            poi.setLigplaatsAfmeerType(cursor.getString(cursor.getColumnIndex(helper.LIGPLAATS_AFMEER_TYPE)));
            poi.setOeverNummer(cursor.getString(cursor.getColumnIndex(helper.OEVER_NUMMER)));

            pointOfInterests.add(poi);
        }

        cursor = db.query(helper.BOEIEN_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))));
            location.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))));

            poi.setCoordinate(location);
                    /*new LLACoordinate(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));*/
            poi.setType(PoiType.Boei);

            pointOfInterests.add(poi);
        }

        cursor = db.query(helper.BOLDERS_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            Location location = new Location("");
            location.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))));
            location.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))));

            poi.setCoordinate(location);
                    /*new LLACoordinate(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));*/
            poi.setType(PoiType.Bolder);

            poi.setDescription(cursor.getString(cursor.getColumnIndex(helper.DESCRIPTION)));
            poi.setMateriaal(cursor.getString(cursor.getColumnIndex(helper.MATERIAL)));
            poi.setMethodeVerankering(cursor.getString(cursor.getColumnIndex(helper.METHODE_VERANKERING)));
            poi.setTrekkracht(cursor.getInt(cursor.getColumnIndex(helper.TREKKRACHT)));
            poi.setTrekkrachtEenheid(cursor.getString(cursor.getColumnIndex(helper.TREKKRACHT_EENHEID)));

            pointOfInterests.add(poi);
        }

        return pointOfInterests;
    }

    class ClarityDBHelper extends SQLiteAssetHelper {

        private static final String MEERPALEN_TABLE_NAME = "MEERPALEN";
        private static final String LIGPLAATSEN_TABLE_NAME = "LIGPLAATSEN";
        private static final String BOEIEN_TABLE_NAME = "BOEIEN";
        private static final String BOLDERS_TABLE_NAME = "BOLDERS";

        private static final String UID = "OBJECTID";
        private static final String FEATUREID = "FEATUREID";
        private static final String DESCRIPTION = "O_DESCR";
        private static final String LATITUDE = "latitude";
        private static final String LONGITUDE = "longitude";
        private static final String MATERIAL = "O_MAT_ALG";
        private static final String METHODE_VERANKERING = "O_METH_VER";
        private static final String TREKKRACHT = "O_TOEL_TRK";
        private static final String TREKKRACHT_EENHEID = "O_TOEL_T_1";
        private static final String TYPE_PAAL = "O_TYP_PLN";
        private static final String NUMMER = "NUMMER";
        private static final String EIGENAAR = "ZZEIGE";
        private static final String HAVEN_NAAM = "ZZHVNAAM";
        private static final String LIGPLAATS_AFMEER_TYPE = "ZZAFMVZ";
        private static final String OEVER_NUMMER = "ZZOEVFRN";

        private static final String DATABASE_NAME = "ClarityNew";
        private static final int DATABASE_VERSION = 1;

        public ClarityDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
    }

    /*class ClarityDBHelper extends SQLiteOpenHelper {

        private static final String MEERPALEN_TABLE_NAME = "MEERPALEN";
        private static final String LIGPLAATSEN_TABLE_NAME = "LIGPLAATSEN";
        private static final String BOEIEN_TABLE_NAME = "BOEIEN";

        private static final String UID = "_id";
        private static final String FEATUREID = "featureId";
        private static final String TYPE = "type";
        private static final String DESCRIPTION = "description";
        private static final String LATITUDE = "latitude";
        private static final String LONGITUDE = "longitude";

        private static final String DATABASE_NAME = "Clarity";
        private static final int DATABASE_VERSION = 1;

        public ClarityDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " + MEERPALEN_TABLE_NAME + " (_id INTEGER PRIMARY KEY, featureId TEXT, type TEXT, description TEXT, latitude TEXT, longitude TEXT);");
                db.execSQL("CREATE TABLE " + LIGPLAATSEN_TABLE_NAME + " (_id INTEGER PRIMARY KEY, featureId TEXT, type TEXT, description TEXT, latitude TEXT, longitude TEXT);");
                db.execSQL("CREATE TABLE " + BOEIEN_TABLE_NAME + " (_id INTEGER PRIMARY KEY, featureId TEXT, type TEXT, description TEXT, latitude TEXT, longitude TEXT);");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MEERPALEN_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + LIGPLAATSEN_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + BOEIEN_TABLE_NAME);
            onCreate(db);
        }
    }*/
}



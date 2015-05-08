package com.riftwalkers.clarity.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

        String[] columns = {helper.UID, helper.FEATUREID, helper.LATITUDE, helper.LONGITUDE};

        Cursor cursor = db.query(helper.MEERPALEN_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            poi.setCoordinate(
                    new LLACoordinate(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));
            poi.setType(PoiType.Meerpaal);

            pointOfInterests.add(poi);
        }

        cursor = db.query(helper.LIGPLAATSEN_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            poi.setCoordinate(
                    new LLACoordinate(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));
            poi.setType(PoiType.Ligplaats);

            pointOfInterests.add(poi);
        }

        cursor = db.query(helper.BOEIEN_TABLE_NAME,columns,null,null,null,null,null);
        while (cursor.moveToNext()){
            PointOfInterest poi = new PointOfInterest();
            poi.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(helper.UID))));
            poi.setFeatureId(cursor.getString(cursor.getColumnIndex(helper.FEATUREID)));
            poi.setCoordinate(
                    new LLACoordinate(
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LATITUDE))),
                            Double.parseDouble(cursor.getString(cursor.getColumnIndex(helper.LONGITUDE))),
                            0,
                            0));
            poi.setType(PoiType.Boei);

            pointOfInterests.add(poi);
        }

        return pointOfInterests;
    }

    class ClarityDBHelper extends SQLiteAssetHelper {

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



package com.riftwalkers.clarity.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.metaio.sdk.jni.LLACoordinate;
import com.riftwalkers.clarity.PoiType;
import com.riftwalkers.clarity.PointOfInterest;

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

    public void insertJsonArray(JSONArray jsonArray){
        db = helper.getWritableDatabase();

        for (int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                JSONObject properties = jsonObject.getJSONObject("properties");
                String objectId = properties.getString("OBJECTID");
                String featureId = properties.getString("FEATUREID");

                JSONObject geometry = jsonObject.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                String lat = coordinates.getString(1);
                String lng = coordinates.getString(0);

                ContentValues contentValues = new ContentValues();
                contentValues.put(helper.UID, objectId);
                contentValues.put(helper.FEATUREID, featureId);
                contentValues.put(helper.LATITUDE, lat);
                contentValues.put(helper.LONGITUDE, lng);
                this.db.insert(helper.TABLE_NAME,null,contentValues);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<PointOfInterest> getAllPointsOfInterest(){
        db = helper.getReadableDatabase();

        ArrayList<PointOfInterest> pointOfInterests = new ArrayList<>();

        String[] columns = {helper.UID, helper.FEATUREID, helper.LATITUDE, helper.LONGITUDE};
        Cursor cursor = db.query(helper.TABLE_NAME,columns,null,null,null,null,null);

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
            poi.setType(PoiType.Bolder);

            pointOfInterests.add(poi);
        }

        return pointOfInterests;
    }
}

    class ClarityDBHelper extends SQLiteOpenHelper {

        public static final String TABLE_NAME = "POINTSOFINTEREST";
        public static final String UID = "_id";
        public static final String FEATUREID = "featureId";
        public static final String TYPE = "type";
        public static final String DESCRIPTION = "description";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";

        private static final String DATABASE_NAME = "Clarity";
        private static final int DATABASE_VERSION = 1;

        public ClarityDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY, featureId TEXT, type TEXT, description TEXT, latitude TEXT, longitude TEXT);");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
}

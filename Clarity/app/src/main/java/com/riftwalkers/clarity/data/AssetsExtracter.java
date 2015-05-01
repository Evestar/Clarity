package com.riftwalkers.clarity.data;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;
import com.riftwalkers.clarity.R;
import com.riftwalkers.clarity.data.JSONHelperClass;
import com.riftwalkers.clarity.data.database.PointsOfInterestDAO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
{
    private Context context;
    private Activity activity;
    private MyAssetsExtracterInterface myAssetsExtracterInterface;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setActivity(Activity activity) {
    this.activity = activity;
    }

    public void setAssetExtracterInterface(MyAssetsExtracterInterface assetExtracterInterface){
        myAssetsExtracterInterface = assetExtracterInterface;
    }

    @Override
    protected Boolean doInBackground(Integer... params)
    {
        try
        {
            AssetsManager.extractAllAssets(context, true);

            try {
                String jsonFileContent = JSONHelperClass.ReadJSONFile(activity.getResources(), R.raw.json_meerpalen);

                //get JSON string
                JSONObject reader = new JSONObject(jsonFileContent);

                // Skip initial objects
                JSONArray featureArray = reader.getJSONArray("features");
                new PointsOfInterestDAO(activity.getApplicationContext()).insertJsonArray(featureArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e)
        {
            MetaioDebug.printStackTrace(Log.ERROR, e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result){
        if(myAssetsExtracterInterface != null)
            myAssetsExtracterInterface.finished();
    }

    public interface MyAssetsExtracterInterface {
        public void finished();
    }
}

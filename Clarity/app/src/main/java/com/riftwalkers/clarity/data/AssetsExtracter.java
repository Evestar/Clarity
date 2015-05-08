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
            String[] ignoreList = {"databases","images","sounds","webkit"};
            AssetsManager.extractAllAssets(context,"",ignoreList, true);

            /*try {
                // Meerpalen
                String jsonFileContent = JSONHelperClass.ReadJSONFile(activity.getResources(), R.raw.json_meerpalen);
                if(myAssetsExtracterInterface != null)
                    myAssetsExtracterInterface.OnStageChange("Meerpalen");
                loadJsonToDAO(jsonFileContent);

                // Ligplaatsen
                jsonFileContent = JSONHelperClass.ReadJSONFile(activity.getResources(), R.raw.json_ligplaatsen);
                if(myAssetsExtracterInterface != null)
                    myAssetsExtracterInterface.OnStageChange("Ligplaatsen");
                loadJsonToDAO(jsonFileContent);

                // Boeien
                jsonFileContent = JSONHelperClass.ReadJSONFile(activity.getResources(), R.raw.json_afmeerboeien);
                if(myAssetsExtracterInterface != null)
                    myAssetsExtracterInterface.OnStageChange("Boeien");
                loadJsonToDAO(jsonFileContent);

            } catch (JSONException e) {
                e.printStackTrace();
            }*/
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

    /*private void loadJsonToDAO(String jsonFileContent) throws JSONException{
        JSONObject reader = new JSONObject(jsonFileContent);
        JSONArray featureArray = reader.getJSONArray("features");
        new PointsOfInterestDAO(activity.getApplicationContext()).insertJsonArray(featureArray);
    }*/

    public interface MyAssetsExtracterInterface {
        public void finished();
        public void OnStageChange(String stage);
    }
}

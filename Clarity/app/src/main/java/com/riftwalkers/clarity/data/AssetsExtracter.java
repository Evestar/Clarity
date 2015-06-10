package com.riftwalkers.clarity.data;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

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
        public void OnStageChange(String stage);
    }
}

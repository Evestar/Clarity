package com.riftwalkers.clarity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

import java.io.IOException;

public class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean>
{
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Integer... params)
    {
        try
        {
            AssetsManager.extractAllAssets(context, true);
        }
        catch (IOException e)
        {
            MetaioDebug.printStackTrace(Log.ERROR, e);
            return false;
        }

        return true;
    }
}

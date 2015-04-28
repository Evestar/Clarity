package com.riftwalkers.clarity;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONHelperClass {
    public static String ReadJSONFile(Resources resources,int resourceId)
    {
        //
        try {
            InputStream inputStream = resources.openRawResource(resourceId);

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        }
        catch (Exception ex)
        {
            return null;
        }
    }
}

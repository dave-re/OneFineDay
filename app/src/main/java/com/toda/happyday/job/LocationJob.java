package com.toda.happyday.job;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.toda.happyday.async.AsyncPostExecute;
import com.toda.happyday.models.Picture;
import com.toda.happyday.utils.TextViewUtil;
import com.toda.happyday.views.PictureGroupAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

import br.com.condesales.models.Venue;

/**
 * Created by fpgeek on 2014. 2. 27..
 */
public class LocationJob extends Job {

    private static final String FOURSQUARE_URL = "https://api.foursquare.com/v2/venues/search";
    private static final String FOURSQUARE_CLIENT_ID = "CX1HRRFGA20JK5BDIQ34ZKRXLWTABKYNC1DYBUNRWPTY30EP";
    private static final String FOURSQUARE_CLIENT_SECRET = "VZLNNXQTLNXWPVQTEDTRT3QBPZLSJT111V531K3W5EWJRARP";
    private static final String FOURSQUARE_VERSION = "20140101";
    private static final String FOURSQUARE_LIMIT = "1";

    private WeakReference<Context> mWeakRefContext;
    private Picture mPicture;
    private Set<Long> mLocationJobPictureIdSet;
    private AsyncPostExecute<String> mAsyncPostExecute;

    public LocationJob(Context context, Picture picture, Set<Long> locationJobPictureIdSet, AsyncPostExecute<String> asyncPostExecute) {
        super(new Params(Priority.LOW));
        mPicture = picture;
        mWeakRefContext = new WeakReference<Context>(context);
        mLocationJobPictureIdSet = locationJobPictureIdSet;
        mAsyncPostExecute = asyncPostExecute;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        if (!mLocationJobPictureIdSet.contains(mPicture.getId()) && mPicture.getLocation() == null) {
            mLocationJobPictureIdSet.add(mPicture.getId());
            if (mPicture.hasValidLocationInfo()) {
                String location = getLocationFromFoursquare(mPicture.getLatitude(), mPicture.getLongitude());
                Log.i("FORSQUAR", "location : " + location);

                Context context = mWeakRefContext.get();
                if (context != null) {
                    Picture.updateLocation(context, mPicture.getId(), location);
                }
                mLocationJobPictureIdSet.remove(mPicture.getId());
                mPicture.setLocation(location);

                mAsyncPostExecute.onPostExecute(location);
            }
        }
    }

    @Override
    protected void onCancel() {
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    private static String getLocationFromFoursquare(double latitude, double longitude) {

        ArrayList<Venue> venues = new ArrayList<Venue>();
        try {
            JSONObject venuesJson = executeHttpGet(FOURSQUARE_URL
                    + "?"
                    + "ll=" + latitude + "," + longitude
                    + "&client_id=" + FOURSQUARE_CLIENT_ID
                    + "&client_secret=" + FOURSQUARE_CLIENT_SECRET
                    + "&v=" + FOURSQUARE_VERSION
                    + "&limit=" + FOURSQUARE_LIMIT);

            // Get return code
            int returnCode = Integer.parseInt(venuesJson.getJSONObject("meta").getString("code"));

            // 200 = OK
            if (returnCode == 200) {
                Gson gson = new Gson();
                JSONArray json = venuesJson.getJSONObject("response")
                        .getJSONArray("venues");
                for (int i = 0; i < json.length(); i++) {
                    Venue venue = gson.fromJson(json.getJSONObject(i)
                            .toString(), Venue.class);
                    venues.add(venue);
                }
            } else {
                return null;
            }

        } catch (Exception exp) {
            return null;
        }

        return venues.get(0).getName();
    }

    // Calls a URI and returns the answer as a JSON object
    private static JSONObject executeHttpGet(String uri) throws Exception {
        HttpGet req = new HttpGet(uri);
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
        HttpConnectionParams.setSoTimeout(httpParameters, 5000);

        DefaultHttpClient client = new DefaultHttpClient();
        client.setParams(httpParameters);
        HttpResponse resLogin = client.execute(req);
        BufferedReader r = new BufferedReader(new InputStreamReader(resLogin
                .getEntity().getContent()));
        StringBuilder sb = new StringBuilder();
        String s = null;
        while ((s = r.readLine()) != null) {
            sb.append(s);
        }

        return new JSONObject(sb.toString());
    }
}

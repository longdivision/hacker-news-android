package com.hexforhn.hex;

import android.app.Application;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;


public class HexApplication extends Application {

    private RequestQueue mRequestQueue;

    public final String apiBaseUrl = "https://hex-api.herokuapp.com";

    public void onCreate() {
        super.onCreate();
        setupAnalytics();
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            int MEGABYTE = 1024 * 1024;
            Cache cache = new DiskBasedCache(getCacheDir(), MEGABYTE);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache, network);
            mRequestQueue.start();
        }

        return mRequestQueue;
    }

    private void setupAnalytics() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        Tracker tracker = analytics.newTracker(R.xml.global_tracker);
        tracker.enableAutoActivityTracking(true);
        tracker.enableExceptionReporting(true);
    }
}

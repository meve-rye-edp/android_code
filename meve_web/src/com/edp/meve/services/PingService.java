/*
 * Android Project: MEVE
 * Ryerson EDP 2012-2013
 * Author: Ariel Fertman
 * Periodic ping service that pings the default web server to verify online status 
 * This service is an intent service that runs every 15 minutes (android clock)
 * 
 * */

package com.edp.meve.services;

import java.net.HttpURLConnection;
import java.net.URL;

import com.edp.meve.MeveApp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class PingService extends IntentService {

	public static String TAG = "PingService";
	public static final String BROADCAST_ACTION = "com.edp.meve.Ping.PingStatusEvent";
	public static boolean connected = false;
	public static final int PingRequestCode = 90210;
	Intent intent;
	private MeveApp meveApp;

	public PingService() {
		super("Ping Service");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		intent = new Intent(BROADCAST_ACTION);
		Log.d(TAG, "PingService Created");
		meveApp = (MeveApp)getApplication();
	}

	@Override
	protected void onHandleIntent(Intent arg) {
		Log.d(TAG, "PingService handling intent");
		boolean conn = false;
		try {
			URL url = new URL(meveApp.getDefaultServerUrl());
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("User-Agent",
					"Android Application - NextStopRecommender");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(1000 * 5); // 5 seconds
			urlc.connect();
			if (urlc.getResponseCode() == 200) {
				conn = true;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		Log.d(TAG, "Server [" + meveApp.getDefaultServerUrl() + "] Reachable: "
				+ conn);
		connected = conn;
		intent.putExtra("connected", connected);
		sendBroadcast(intent);
		Log.d(TAG, "Connection Broadcast sent");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}

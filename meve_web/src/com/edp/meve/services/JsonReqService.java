/*
 * Android Project: MEVE
 * Ryerson EDP 2012-2013
 * Author: Ariel Fertman
 * Periodic service that updates local storage with latest JSON information from the web server
 * The service is an intent service that runs every 15 minutes (android clock)
 * It calls the javascript function getJSONInfo() by sending a broadcast that the webview is registerd too 
 * 
 * */
package com.edp.meve.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class JsonReqService extends IntentService {

	public static String TAG = "JsonReqService";
	public static final String BROADCAST_ACTION = "com.edp.meve.Ping.UPDATEJSON";
	public static final int JsonReqServiceCode = 90210;
	Intent intent;

	public JsonReqService() {
		super("JSON Request Service");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		intent = new Intent(BROADCAST_ACTION);
		Log.d(TAG, "JSON Request Service Created");
	}

	@Override
	protected void onHandleIntent(Intent arg) {
		Log.d(TAG, "Sending update Broadcast");
		sendBroadcast(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}

package com.edp.meve;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 * Application class to handle basic preference requirements and storing
 * */
public class MeveApp extends Application {
	private static final String TAG = MeveApp.class.getSimpleName();
	public static String DEFAULT_SERVER_URL = "http://dev.arcx.com/ecar";
	private SharedPreferences prefs;
	
	public void onCreate(){
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Log.i(TAG, "Application started");
	}
	
	public void clearPrefs(){
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("username", "");
		editor.putString("password", "");
		editor.putString("current_car", "");
		editor.commit();
	}
	
	public String getDefaultServerUrl(){
		return DEFAULT_SERVER_URL;
	}

	public SharedPreferences getPreferences(){
		return this.prefs;
	}
	
	
}
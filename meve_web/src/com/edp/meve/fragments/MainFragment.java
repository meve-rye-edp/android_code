/*
 * Android Project: MEVE
 * Ryerson EDP 2012-2013
 * Author: Ariel Fertman
 * Main Fragment of the activity, this fragment holds the webview and all important
 * settings for the webview
 * 
 * */

package com.edp.meve.fragments;

import com.edp.meve.MeveApp;
import com.edp.meve.R;
import com.edp.meve.services.JsonReqService;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class MainFragment extends Fragment implements
		OnSharedPreferenceChangeListener {
	private static String TAG = "Webview Fragment";
	private static String TAG_CONSOLE = "JS Console";
	String packageName = "com.edp.meve";
	private View view;
	private WebView webview;
	private WebSettings webSettings;
	private MeveApp meveApp;
	private SharedPreferences prefs;
	private Boolean notifySOC;
	private Boolean notifyCharging;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// inflate webview into fragment space
		view = inflater.inflate(R.layout.frag_webview, container, false);
		meveApp = (MeveApp) getActivity().getApplication();
		webview = (WebView) view.findViewById(R.id.webView);
		webview.loadUrl(meveApp.getDefaultServerUrl());

		// Initialize javascript interface and console debugging
		final MyJavaScriptInterface myJavaScriptInterface = new MyJavaScriptInterface(
				getActivity());
		webview.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");
		webview.setWebChromeClient(new WebChromeClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d(TAG_CONSOLE,
						cm.message() + " -- From line " + cm.lineNumber()
								+ " of " + cm.sourceId());
				return true;
			}
		});

		/*
		 * Register SOC and charging flags on load from preference manager
		 */
		prefs = PreferenceManager
				.getDefaultSharedPreferences(view.getContext());
		notifySOC = prefs.getBoolean("flag_soc_low", false);
		notifyCharging = prefs.getBoolean("flag_charging", false);
		Log.d(TAG, "Notify Low SOC: " + notifySOC);
		Log.d(TAG, "Notify When Charging: " + notifyCharging);

		// override default redirect to prevent PHP header re-directs from
		// opening default browser on android
		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}

			// function listens to when page has finished loading
			@Override
			public void onPageFinished(WebView view, String url) {
				Log.d(TAG, "Page has finished loading.");
			}

		});

		/*
		 * Enable all required webview settings Javascript, local storage, local
		 * storage save path, caching
		 */
		webSettings = webview.getSettings();
		webSettings.setJavaScriptEnabled(true); // enable javascript
		webSettings.setDomStorageEnabled(true); // enable local storage
		webSettings.setDatabaseEnabled(true);
		webSettings.setDatabasePath("/data/data/" + packageName + "/databases");
		webSettings.setAppCacheEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

		// register receiver for when interval changes for update time
		getActivity().registerReceiver(jsonUpdater,
				new IntentFilter(JsonReqService.BROADCAST_ACTION));

		return view;
	}

	public WebView getWebView() {
		return webview;
	}

	public void updateJson() {
		Log.d(TAG, "Updating to latest json information");
		getWebView().loadUrl("javascript:getJSONInfo()");
	}

	// broadcast receiver for update time interval
	private BroadcastReceiver jsonUpdater = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Received JSON update broadcast");
			updateJson();
		}
	};

	// JavaScript interface to android phone
	// used for notification purposes
	public class MyJavaScriptInterface {
		Context mContext;
		
		//get the context 
		MyJavaScriptInterface(Context c) {
			mContext = c;
		}

		public void notifyCharging() {
				
			if (notifyCharging) {
				Log.d(TAG,"ECar is in charging mode");
				notificationBuilder("Car is Charging", "Your car is in its charging state.");
			}
		}

		// if charge is below 30%, notify user from javascript code 
		public void notifySOC(String charge) {

			if (notifySOC) {
				Log.d(TAG,"Current SOC from webpage: " + Double.parseDouble(charge));
				notificationBuilder("Charge Low!", "Your charge is: " + charge + "%");
			}

		}

		// clear webview cache content
		public void clearCache() {
			Log.d(TAG, "Clearing webview cache");
			getWebView().clearCache(true);

			AlertDialog.Builder myDialog = new AlertDialog.Builder(
					getActivity());
			myDialog.setTitle("Cache cleared");
			myDialog.setPositiveButton("Okay thanks", null);
			myDialog.show();

		}
		
		//receive car info when the page loads and register ecar id and ecar name 
		public void transmitECarInfo(String ecar_id, String ecar_name) {
			SharedPreferences.Editor editor = meveApp.getPreferences().edit();
			editor.putString("current_car", ecar_name.toString());
			editor.putString("ecar_id", ecar_id.toString());
			editor.commit();
		}
		
		
		//helper function to build notifications 
		@SuppressLint("NewApi")
		public void notificationBuilder(String content_title, String content_text){
			
			Intent intent = new Intent(getActivity()
					.getApplicationContext(), getActivity().getClass());
			PendingIntent pIntent = PendingIntent.getActivity(getActivity()
					.getApplicationContext(), 0, intent, 0);

			// Build notification
			Notification noti = new Notification.Builder(getActivity())
					.setContentTitle(content_title)
					.setContentText(content_text)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentIntent(pIntent).build();
			NotificationManager notificationManager = (NotificationManager) getActivity()
					.getSystemService(Context.NOTIFICATION_SERVICE);

			// Hide the notification after its selected
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			noti.defaults |= Notification.DEFAULT_SOUND;
			noti.defaults |= Notification.DEFAULT_VIBRATE;

			notificationManager.notify(0, noti);
			
		}

	}
	
	//listener for when any preference has changed
	//the soc and charging flags will automatically be updated.
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		notifySOC = prefs.getBoolean("flag_soc_low", false);
		notifyCharging = prefs.getBoolean("flag_charging", false);
		Log.d(TAG, "Pref change: Notify Low SOC: " + notifySOC);
		Log.d(TAG, "Pref change: Notify When Charging: " + notifyCharging);

	}

}

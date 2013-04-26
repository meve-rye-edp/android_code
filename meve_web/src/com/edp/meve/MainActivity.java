/*
 * Android Project: MEVE
 * Ryerson EDP 2012-2013
 * Author: Ariel Fertman
 * Main Activity that holds the fragment manager
 * This activity manages all fragments (webview, status bar and preferences)
 * 
 * */

package com.edp.meve;

import com.edp.meve.fragments.MainFragment;
import com.edp.meve.fragments.PrefsFragment;
import com.edp.meve.services.JsonReqService;
import com.edp.meve.services.PingService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {

	/* Static Declarations */
	public static final String TAG = "MainActivity";
	private static final int PREFS_VIEW = 1;

	/* local variables */
	private TextView connection_bool;
	private ImageButton btn_refresh;
	public MeveApp meveApp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/* View (UI) related component initializations */
		connection_bool = (TextView) findViewById(R.id.tv_connection);
		btn_refresh = (ImageButton) findViewById(R.id.btn_refresh);
		
		// register ping connector broadcasts
		registerReceiver(pingConncetionReceiver, new IntentFilter(
				PingService.BROADCAST_ACTION));
		/* System related initializations */
		meveApp = (MeveApp) getApplication();
		registerAlarmManager();
		registerJsonAlarm();
		startService(new Intent(this, PingService.class));
		//startService(new Intent(this, JsonReqService.class));
		
		// fragment init and load main webview fragment
		if (savedInstanceState == null) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			MainFragment fragment = new MainFragment();
			fragmentTransaction.add(R.id.myFragment, fragment);
			fragmentTransaction.commit();
		}
		
		btn_refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MainFragment reloadFragment = new MainFragment();
				LoadTask loadtask = new LoadTask();
				loadtask.execute(reloadFragment);
			}
		});
	}


	/*
	 * Add menu to the system menu Preferences Logout
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, PREFS_VIEW, 0, "Preferences");
		return true;
	}

	// Called when an options item is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Fragment newFragment;
		LoadTask loadtask;
		switch (item.getItemId()) {
		case PREFS_VIEW:
			newFragment = new PrefsFragment();
			loadtask = new LoadTask();
			loadtask.execute(newFragment);
			break;
		}
		return true;
	}

	/*
	 * Async task to handle progress bar loading while new fragments inflate
	 * Will serve a better purposes when fragments take more time to load their
	 * information
	 */
	private class LoadTask extends AsyncTask<Fragment, Void, Void> {
		ProgressDialog progressDialog;

		@Override
		protected Void doInBackground(Fragment... params) {
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.myFragment, params[0]);
			transaction.addToBackStack(null); // if back button is clicked, last
												// fragment will be loaded\
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			transaction.commit();
			return null;
		}

		protected void onPreExecute() {
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.show();
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
		}

	}

	/*
	 * Broadcast Receivers
	 */

	/*
	 * Receiver for ping service, will change the connection monitor result on
	 * the screen depending on response from the receiver
	 */
	private BroadcastReceiver pingConncetionReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			boolean connected = intent.getBooleanExtra("connected", false);
			if (connected) {
				connection_bool.setText(" Online");
				connection_bool.setTextColor(Color.GREEN);
			} else {
				connection_bool.setText(" Offline");
				connection_bool.setTextColor(Color.RED);
			}
		}
	};

	// alarm registration for ping service
	public void registerAlarmManager() {
		AlarmManager am = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent i2 = new Intent(this, PingService.class);
		PendingIntent pi = PendingIntent.getService(this,
				PingService.PingRequestCode, i2, 0);

		// check if already registered
		boolean alarmRegistered = (PendingIntent.getBroadcast(this,
				PingService.PingRequestCode, i2, PendingIntent.FLAG_NO_CREATE) != null);
		if (!alarmRegistered) {
			Log.d(TAG, "Registering PingService with AlarmManager");
			int type = AlarmManager.RTC;
			long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
			long triggerTime = System.currentTimeMillis() + interval;
			am.setRepeating(type, triggerTime, interval, pi);
		}
	}
	
	public void registerJsonAlarm(){
		//interval factor options are 1, 2, and 4 (15min, 30min, 1 hour)
		AlarmManager am = (AlarmManager) this
				.getSystemService(Context.ALARM_SERVICE);
		Intent i2 = new Intent(this, JsonReqService.class);
		PendingIntent pi = PendingIntent.getService(this,
				JsonReqService.JsonReqServiceCode, i2, 0);

		// check if already registered
		boolean alarmRegistered = (PendingIntent.getBroadcast(this,
				JsonReqService.JsonReqServiceCode, i2, PendingIntent.FLAG_NO_CREATE) != null);
		if (!alarmRegistered) {
			Log.d(TAG, "Registering JSON Updater with AlarmManager");
			int type = AlarmManager.RTC;
			long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
			long triggerTime = System.currentTimeMillis() + interval;
			am.setRepeating(type, triggerTime, interval, pi);
		}
	}
	
	
	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();

	}

}

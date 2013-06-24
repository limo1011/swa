package de.shop.ui.main;

import static de.shop.util.Constants.KUNDE_KEY;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import de.shop.data.AbstractKunde;
import de.shop.service.KundeService;
import de.shop.service.KundeService.KundeServiceBinder;
import de.shop.util.InternalShopError;

public class SucheIdActivity extends Activity {
	private static final String LOG_TAG = SucheIdActivity.class.getSimpleName();
	
	private String query;
	private KundeServiceBinder kundeServiceBinder;
	
	private ServiceConnection serviceConnection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.search);

		final Intent intent = getIntent();
		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
	
		query = intent.getStringExtra(SearchManager.QUERY);
		Log.d(LOG_TAG, query);
		
		createServiceConnection();
	}
	
	private void createServiceConnection() {
		Log.v(LOG_TAG, "createServiceConnection()");
		// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
		serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
				Log.v(LOG_TAG, "onServiceConnected()");
				kundeServiceBinder = (KundeServiceBinder) serviceBinder;
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				kundeServiceBinder = null;
			}
		};
	}
	
	@Override
	public void onStart() {		
		Log.v(LOG_TAG, "onStart()");
		final Intent intent = new Intent(this, KundeService.class);
		final boolean success = bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		if (!success) {
			throw new InternalShopError("bindService() von SucheIdActivitiy funktioniert nicht fuer KundeService");
		}
		// TODO Warum wird ServiceConnection.onServiceConnected() s.o. nicht aufgerufen, weshalb kundeServiceBinder null bleibt ???
		
		suchen();

		super.onStart();
	}

	@Override
	public void onStop() {
		unbindService(serviceConnection);
		super.onStop();
	}
	
	private void suchen() {
		Log.v(LOG_TAG, "suchen()");
		final Long kundeId = Long.valueOf(query);
		final AbstractKunde kunde = kundeServiceBinder.sucheKundeById(kundeId, this).resultObject;
		Log.d(LOG_TAG, kunde.toString());
		
		final Intent intent = new Intent(this, Main.class);
		if (kunde != null) {
			intent.putExtra(KUNDE_KEY, kunde);
		}
		startActivity(intent);
	}
}

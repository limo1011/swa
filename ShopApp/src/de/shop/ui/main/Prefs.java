package de.shop.ui.main;

import static de.shop.util.Constants.HOST_DEFAULT;
import static de.shop.util.Constants.MOCK_DEFAULT;
import static de.shop.util.Constants.PATH_DEFAULT;
import static de.shop.util.Constants.PORT_DEFAULT;
import static de.shop.util.Constants.PROTOCOL_DEFAULT;
import static de.shop.util.Constants.TIMEOUT_DEFAULT;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import de.shop.R;

public class Prefs extends PreferenceFragment implements OnPreferenceChangeListener {
	private static final String LOG_TAG = Prefs.class.getSimpleName();
	
	public static String protocol;
	public static String host;
	public static String port;
	public static String path;
	private static String timeoutStr;
	public static long timeout;
	public static String username;
	public static String password;
	public static boolean mock;
	
	private static boolean initialized = false;
	
	private static final String PROTOCOL_KEY = "protocol";
	private static final String HOST_KEY = "host";
	private static final String PORT_KEY = "port";
	private static final String PATH_KEY = "path";
	private static final String TIMEOUT_KEY = "timeout";
	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";
	private static final String MOCK_KEY = "mock";
	
	private ListPreference protocolPref;
	private EditTextPreference hostPref;
	private EditTextPreference portPref;
	private EditTextPreference pathPref;
	private EditTextPreference timeoutPref;
	private EditTextPreference usernamePref;
	private EditTextPreference passwordPref;
	private CheckBoxPreference mockPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		// Evtl. vorhandene Tabs der ACTIVITY loeschen
		getActivity().getActionBar().removeAllTabs();
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		init(getActivity());
		
		protocolPref = (ListPreference) findPreference(PROTOCOL_KEY);
		protocolPref.setOnPreferenceChangeListener(this);
		
		hostPref = (EditTextPreference) findPreference(HOST_KEY);
		hostPref.setOnPreferenceChangeListener(this);
		
		portPref = (EditTextPreference) findPreference(PORT_KEY);
		portPref.setOnPreferenceChangeListener(this);
		
		pathPref = (EditTextPreference) findPreference(PATH_KEY);
		pathPref.setOnPreferenceChangeListener(this);
		
		timeoutPref = (EditTextPreference) findPreference(TIMEOUT_KEY);
		timeoutPref.setOnPreferenceChangeListener(this);
		
		usernamePref = (EditTextPreference) findPreference(USERNAME_KEY);
		usernamePref.setOnPreferenceChangeListener(this);
		
		passwordPref = (EditTextPreference) findPreference(PASSWORD_KEY);
		passwordPref.setOnPreferenceChangeListener(this);
		
		mockPref = (CheckBoxPreference) findPreference(MOCK_KEY);
		mockPref.setOnPreferenceChangeListener(this);
	}

	public static void init(Context ctx) {
		if (initialized) {
			return;
		}
		
		// Objekt der Klasse SharedPreferences laden, das die Default-Datei
		// /data/data/de.shop/shared_prefs/de.shop_preferences.xml repraesentiert
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		protocol = prefs.getString(PROTOCOL_KEY, PROTOCOL_DEFAULT);
		host = prefs.getString(HOST_KEY, HOST_DEFAULT);
		port = prefs.getString(PORT_KEY, PORT_DEFAULT);
		path = prefs.getString(PATH_KEY, PATH_DEFAULT);
		
		timeoutStr = prefs.getString(TIMEOUT_KEY, TIMEOUT_DEFAULT);
		try {
			timeout = Long.parseLong(timeoutStr);
		}
		catch (NumberFormatException e) {
			timeout = Long.parseLong(TIMEOUT_DEFAULT);
		}
		
		username = prefs.getString(USERNAME_KEY, "");
		password = prefs.getString(PASSWORD_KEY, "");

		mock = prefs.getBoolean(MOCK_KEY, MOCK_DEFAULT);
		
		initialized = true;
		
		Log.i(LOG_TAG, "protocol=" + protocol + ", host=" + host  + ", port=" + port + ", path=" + path
				       + ", timeoutStr=" + timeoutStr + ", username=" + username + ", password=" + password + ", mock=" + mock);
	}
	
	@Override
	// OnPreferenceChangeListener
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		final Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
		if (preference.equals(protocolPref)) {
			protocol = (String) newValue;
			editor.putString(PROTOCOL_KEY, protocol);
		}
		else if (preference.equals(hostPref)) {
			host = (String) newValue;
			editor.putString(HOST_KEY, host);
		}
		else if (preference.equals(portPref)) {
			port = (String) newValue;
			editor.putString(PORT_KEY, port);
		}
		else if (preference.equals(pathPref)) {
			path = (String) newValue;
			editor.putString(PATH_KEY, path);
		}
		else if (preference.equals(timeoutPref)) {
			timeoutStr = (String) newValue;
			try {
				timeout = Long.parseLong(timeoutStr);
			}
			catch (NumberFormatException e) {
				timeout = Long.parseLong(TIMEOUT_DEFAULT);
			}
			editor.putString(TIMEOUT_KEY, timeoutStr);
		}
		else if (preference.equals(usernamePref)) {
			username = (String) newValue;
			editor.putString(USERNAME_KEY, username);
		}
		else if (preference.equals(passwordPref)) {
			password = (String) newValue;
			editor.putString(PASSWORD_KEY, password);
		}
		else if (preference.equals(mockPref)) {
			mock = (Boolean) newValue;
			editor.putBoolean(MOCK_KEY, mock);
		}
		editor.commit();
		
		// Fragment neu laden, damit die Aenderung sofort sichtbar ist
		getFragmentManager().beginTransaction()
                            .replace(R.id.details, new Prefs())
                            .addToBackStack(null)
                            .commit();
		
		return false;
	}
}

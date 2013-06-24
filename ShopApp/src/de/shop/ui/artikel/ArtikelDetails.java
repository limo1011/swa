package de.shop.ui.artikel;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;
import static de.shop.util.Constants.ARTIKEL_KEY;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.shop.R;
import de.shop.data.Artikel;
import de.shop.util.TabListener;

public class ArtikelDetails extends Fragment {
	private static final String LOG_TAG = ArtikelDetails.class.getSimpleName();
	
	private Artikel artikel;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        artikel = (Artikel) getArguments().get(ARTIKEL_KEY);
        Log.d(LOG_TAG, artikel.toString());
        
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.details_tabs, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		final Activity activity = getActivity();
		final ActionBar actionBar = activity.getActionBar();
		// (horizontale) Tabs; NAVIGATION_MODE_LIST fuer Dropdown Liste
		actionBar.setNavigationMode(NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);  // Titel der App ausblenden, um mehr Platz fuer die Tabs zu haben

	    final Bundle args = new Bundle(1);
    	args.putSerializable(ARTIKEL_KEY, artikel);
    	
	    Tab tab = actionBar.newTab()
	                       .setText(getString(R.string.a_stammdaten))
	                       .setTabListener(new TabListener<ArtikelStammdaten>(activity,
	                    		                                            ArtikelStammdaten.class,
	                    		                                            args));
	    actionBar.addTab(tab);
	}
}

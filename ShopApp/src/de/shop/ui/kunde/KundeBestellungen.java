package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDE_KEY;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.AbstractKunde;
import de.shop.data.Bestellung;
import de.shop.service.BestellungService.BestellungServiceBinder;
import de.shop.service.KundeService.KundeServiceBinder;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;
import de.shop.util.WischenListener;

public class KundeBestellungen extends Fragment implements OnItemClickListener, OnTouchListener {
	private static final String LOG_TAG = KundeBestellungen.class.getSimpleName();
	
	private AbstractKunde kunde;
	private List<Long> bestellungenIds;
	private List<Bestellung> bestellungen;
	private Bestellung bestellung;
	private int bestellungenListePos;
	
	private TextView txtBestellungId;
	private TextView txtBestellungDatum;
	
	private KundeServiceBinder kundeServiceBinder;
	private BestellungServiceBinder bestellungServiceBinder;
	
	private GestureDetector gestureDetector;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		kunde = (AbstractKunde) getArguments().get(KUNDE_KEY);
        
        setHasOptionsMenu(true);
        
        // attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunde_bestellungen, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// View-IDs fuer die Textfelder einer Bestellung des aktuellen Kunden
		final TextView kundeTxt = (TextView) view.findViewById(R.id.bestellungen_kunde_id);
		kundeTxt.setText(getString(R.string.k_bestellungen_kunde_id, kunde.id));
		txtBestellungId = (TextView) view.findViewById(R.id.bestellung_id);
		txtBestellungDatum = (TextView) view.findViewById(R.id.datum);
		
		final Activity activity = getActivity();
		if (Main.class.equals(activity.getClass())) {
			Main main = (Main) activity;
			kundeServiceBinder = main.getKundeServiceBinder();
			bestellungServiceBinder = main.getBestellungServiceBinder();
		}
		else if (KundenListe.class.equals(activity.getClass())) {
			KundenListe kundenListe = (KundenListe) activity;
			kundeServiceBinder = kundenListe.getKundeServiceBinder();
			bestellungServiceBinder = kundenListe.getBestellungServiceBinder();
		}
		else {
			Log.e(LOG_TAG, "Activity " + activity.getClass().getSimpleName() + " nicht beruecksichtigt.");
			return;
		}
		
		bestellungenIds = kundeServiceBinder.sucheBestellungenIdsByKundeId(kunde.id, view.getContext());
		if (bestellungenIds == null || bestellungenIds.isEmpty()) {
			kundeTxt.setText(getString(R.string.k_keine_bestellungen, kunde.id));
		}
		else {
			// ListView mit den IDs der Bestellungen aufbauen
			final ListView listView = (ListView) view.findViewById(R.id.bestellungen_liste);
	        int anzahl = bestellungenIds.size();
	        bestellungen = new ArrayList<Bestellung>(anzahl);
			final String[] values = new String[anzahl];
			for (int i = 0; i < anzahl; i++) {
	        	bestellungen.add(null);
	        	values[i] = getString(R.string.k_kunde_bestellung_id, bestellungenIds.get(anzahl - i - 1));
	        	Log.d(LOG_TAG, values[i]);
	        }
	        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
	        		                                                      android.R.layout.simple_list_item_1,
	        		                                                      android.R.id.text1,
	        		                                                      values);
	        // Items in der Liste duerfen angeklickt werden
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(this);
			
			// Die neueste Bestellung visualisieren
			activateBestellung(0, view);
		}
		
		final OnGestureListener onGestureListener = new WischenListener(activity);
	    gestureDetector = new GestureDetector(activity, onGestureListener);  // Context und OnGestureListener als Argumente
	    view.setOnTouchListener(this);
	}
	
	@Override
	// Implementierung zum Interface OnItemClickListener fuer die Liste der Bestellung-IDs
	public void onItemClick(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
		// view: TextView innerhalb von ListFragment
		// itemPosition: Textposition innerhalb der Liste mit Zaehlung ab 0
		// itemId = itemPosition bei String-Arrays bzw. = Primaerschluessel bei Listen aus einer DB
		
		// Bestellung ermitteln bzw. per Web Service nachladen
		activateBestellung(itemPosition, view);
	}
	
	@Override
	// OnTouchListener
	public boolean onTouch(View view, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.einstellungen:
				getFragmentManager().beginTransaction()
                                    .replace(R.id.details, new Prefs())
                                    .addToBackStack(null)
                                    .commit();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void activateBestellung(int itemPosition, View view) {
		// Bestellung-ID ermitteln
		bestellungenListePos = bestellungenIds.size() - itemPosition - 1;
		
		// Bestellung ermitteln bzw. per Web Service nachladen
		bestellung = bestellungen.get(bestellungenListePos);
		if (bestellung == null) {
			final Long bestellungId = bestellungenIds.get(bestellungenListePos);
			Log.v(LOG_TAG, "Bestellung nachladen: " + bestellungId);
			
			bestellung = bestellungServiceBinder.getBestellungById(bestellungId, view.getContext()).resultObject;
		}
		else {
			Log.v(LOG_TAG, "Bereits geladene Bestellung: " + bestellung);
			
			// Werte der geladenen Bestellung visualisieren
		}
		
		txtBestellungId.setText(String.valueOf(bestellung.id));
		final String datumStr = bestellung.datum == null ? "" : DateFormat.getDateFormat(getActivity()).format(bestellung.datum);
    	txtBestellungDatum.setText(datumStr);
	}
}

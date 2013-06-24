package de.shop.service;

import static android.app.ProgressDialog.STYLE_SPINNER;
import static de.shop.ui.main.Prefs.mock;
import static de.shop.ui.main.Prefs.timeout;
import static de.shop.util.Constants.ARTIKEL_ID_PREFIX_PATH;
import static de.shop.util.Constants.ARTIKEL_PATH;
import static de.shop.util.Constants.LOCALHOST;
import static de.shop.util.Constants.LOCALHOST_EMULATOR;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import de.shop.R;
import de.shop.data.Artikel;
import de.shop.util.InternalShopError;

public class ArtikelService extends Service {
	private static final String LOG_TAG = ArtikelService.class.getSimpleName();
	
	private ArtikelServiceBinder binder = new ArtikelServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class ArtikelServiceBinder extends Binder {
		
		public ArtikelService getService() {
			return ArtikelService.this;
		}
		
		private ProgressDialog progressDialog;
		private ProgressDialog showProgressDialog(Context ctx) {
			progressDialog = new ProgressDialog(ctx);
			progressDialog.setProgressStyle(STYLE_SPINNER);  // Kreis (oder horizontale Linie)
			progressDialog.setMessage(getString(R.string.s_bitte_warten));
			progressDialog.setCancelable(true);      // Abbruch durch Zuruecktaste
			progressDialog.setIndeterminate(true);   // Unbekannte Anzahl an Bytes werden vom Web Service geliefert
			progressDialog.show();
			return progressDialog;
		}
		
		/**
		 */
		public HttpResponse<Artikel> sucheArtikelById(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "AbstractKunde"
			final AsyncTask<Long, Void, HttpResponse<Artikel>> sucheArtikelByIdTask = new AsyncTask<Long, Void, HttpResponse<Artikel>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Artikel> doInBackground(Long... ids) {
					final Long id = ids[0];
		    		final String path = ARTIKEL_PATH + "/" + id;
		    		Log.v(LOG_TAG, "path = " + path);
		    		final HttpResponse<Artikel> result = mock
		    				                                   ? Mock.sucheArtikelById(id)
		    				                                   : WebServiceClient.getJsonSingle(path, Artikel.class);

					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Artikel> unused) {
					progressDialog.dismiss();
	    		}
			};

    		sucheArtikelByIdTask.execute(id);
    		HttpResponse<Artikel> result = null;
	    	try {
	    		result = sucheArtikelByIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	    	
    		if (result.responseCode != HTTP_OK) {
	    		return result;
		    }
		    return result;
		}
		
		/**
		 */
/*		public HttpResponse<Artikel> sucheArtikelByBezeichnung(String bezeichnung, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "String", Resultat vom Typ "List<AbstractKunde>"
			final AsyncTask<String, Void, HttpResponse<Artikel>> sucheArtikelByBezeichnungTask = new AsyncTask<String, Void, HttpResponse<Artikel>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Artikel> doInBackground(String... bezeichnungen) {
					final String bezeichnung = bezeichnungen[0];
					final String path = BEZEICHNUNG_PATH + bezeichnung;
					Log.v(LOG_TAG, "path = " + path);
		    		final HttpResponse<Artikel> result = mock
		    				                                   ? Mock.sucheKundenByNachname(bezeichnung)
		    				                                   : WebServiceClient.getJsonList(path, Artikel.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			sucheKundenByNameTask.execute(nachname);
			HttpResponse<AbstractKunde> result = null;
			try {
				result = sucheKundenByNameTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}

	    	if (result.responseCode != HTTP_OK) {
	    		return result;
	    	}
	    	
	    	final ArrayList<AbstractKunde> kunden = result.resultList;
	    	// URLs fuer Emulator anpassen
	    	for (AbstractKunde k : kunden) {
	    		setBestellungenUri(k);
	    	}
			return result;
	    }*/
	
		
		/**
		 * Annahme: wird ueber AutoCompleteTextView aufgerufen, wobei die dortige Methode
		 * performFiltering() schon einen neuen Worker-Thread startet, so dass AsyncTask hier
		 * ueberfluessig ist.
		 */
		public List<Long> sucheIds(String prefix) {
			final String path = ARTIKEL_ID_PREFIX_PATH + "/" + prefix;
		    Log.v(LOG_TAG, "sucheIds: path = " + path);

    		final List<Long> ids = mock
   				                   ? Mock.sucheArtikelIdsByPrefix(prefix)
   				                   : WebServiceClient.getJsonLongList(path);

			Log.d(LOG_TAG, "sucheIds: " + ids.toString());
			return ids;
		}
		
		/**
		 * Annahme: wird ueber AutoCompleteTextView aufgerufen, wobei die dortige Methode
		 * performFiltering() schon einen neuen Worker-Thread startet, so dass AsyncTask hier
		 * ueberfluessig ist.
		 */
/*		public List<String> sucheNachnamen(String prefix) {
			final String path = NACHNAME_PREFIX_PATH +  "/" + prefix;
		    Log.v(LOG_TAG, "sucheNachnamen: path = " + path);

    		final List<String> nachnamen = mock
    				                       ? Mock.sucheNachnamenByPrefix(prefix)
    				                       : WebServiceClient.getJsonStringList(path);
			Log.d(LOG_TAG, "sucheNachnamen: " + nachnamen);

			return nachnamen;
		}*/

		/**
		 */
		public HttpResponse<Artikel> createArtikel(Artikel artikel, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "AbstractKunde", Resultat vom Typ "void"
			final AsyncTask<Artikel, Void, HttpResponse<Artikel>> createArtikelTask = new AsyncTask<Artikel, Void, HttpResponse<Artikel>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Artikel> doInBackground(Artikel... artikels) {
					final Artikel artikel = artikels[0];
		    		final String path = ARTIKEL_PATH;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Artikel> result = mock
                                                               ? Mock.createArtikel(artikel)
                                                               : WebServiceClient.postJson(artikel, path);
		    		
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Artikel> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			createArtikelTask.execute(artikel);
			HttpResponse<Artikel> response = null; 
			try {
				response = createArtikelTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			artikel.id = Long.valueOf(response.content);
			final HttpResponse<Artikel> result = new HttpResponse<Artikel>(response.responseCode, response.content, artikel);
			return result;
	    }
		
		/**
		 */
/*		public HttpResponse<AbstractKunde> updateKunde(AbstractKunde kunde, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "AbstractKunde", Resultat vom Typ "void"
			final AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>> updateKundeTask = new AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<AbstractKunde> doInBackground(AbstractKunde... kunden) {
					final AbstractKunde kunde = kunden[0];
		    		final String path = KUNDEN_PATH;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<AbstractKunde> result = mock
		    				                          ? Mock.updateKunde(kunde)
		    		                                  : WebServiceClient.putJson(kunde, path);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			updateKundeTask.execute(kunde);
			final HttpResponse<AbstractKunde> result;
			try {
				result = updateKundeTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			if (result.responseCode == HTTP_NO_CONTENT || result.responseCode == HTTP_OK) {
				kunde.updateVersion();  // kein konkurrierendes Update auf Serverseite
				result.resultObject = kunde;
			}
			
			return result;
	    }*/
		
		/**
		 */
/*		public HttpResponse<Void> deleteKunde(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "AbstractKunde"
			final AsyncTask<Long, Void, HttpResponse<Void>> deleteKundeTask = new AsyncTask<Long, Void, HttpResponse<Void>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Void> doInBackground(Long... ids) {
					final Long kundeId = ids[0];
		    		final String path = KUNDEN_PATH + "/" + kundeId;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Void> result = mock ? Mock.deleteKunde(kundeId) : WebServiceClient.delete(path);
			    	return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Void> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			deleteKundeTask.execute(id);
			final HttpResponse<Void> result;
	    	try {
	    		result = deleteKundeTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			return result;
		}*/
	}
}

package de.shop;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonReaderFactory;

import android.app.Application;
import android.content.Context;

public class ShopApp extends Application {
	public static JsonReaderFactory jsonReaderFactory;
	public static JsonBuilderFactory jsonBuilderFactory;

	private static Context ctx;
	
	
	@Override
	public void onCreate() {
		System.getProperty("java.classpath");
		jsonReaderFactory = Json.createReaderFactory(null);
		jsonBuilderFactory = Json.createBuilderFactory(null);

		ctx = this;
	}
	
	public static InputStream open(int dateinameId) {
		// dateinameId = R.raw.dateiname
		// fuer die Datei res\raw\dateiname.json
		return ctx.getResources().openRawResource(dateinameId);
	}
	
	public static OutputStream open(String dateiname) throws FileNotFoundException {
		// /data/data/de.shop/files/<dateiname>
		return ctx.openFileOutput(dateiname, MODE_PRIVATE);
	}
}

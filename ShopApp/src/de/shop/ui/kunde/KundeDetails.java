package de.shop.ui.kunde;

import de.shop.R;
import de.shop.R.layout;
import de.shop.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class KundeDetails extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.kunde_details);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kunde_details, menu);
		return true;
	}

}

package dev.hacknc.pebblepulse;

import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {
	
	// Pebble application ID
	private static final UUID APP_UUID = UUID.fromString("0ca4b5be-331d-4d49-9850-e55245f72ded");
	
	// Pebble button keys -- MUST BE IDENTICAL TO THOSE IN WATCH APP
	private static final int DATA_KEY = 0;
	private static final int SELECT_BUTTON_KEY = 0;
	private static final int UP_BUTTON_KEY = 1;
	private static final int DOWN_BUTTON_KEY = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

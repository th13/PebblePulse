package dev.hacknc.pebblepulse;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MainActivity extends Activity {

	// Pebble application ID
	private static final UUID APP_UUID = UUID
			.fromString("0ca4b5be-331d-4d49-9850-e55245f72ded");

	// Pebble button keys -- MUST BE IDENTICAL TO THOSE IN WATCH APP
	private static final int DATA_KEY = 0;
	private static final int SELECT_BUTTON_KEY = 0;
	private static final int UP_BUTTON_KEY = 1;
	private static final int DOWN_BUTTON_KEY = 2;

	private PebbleKit.PebbleDataReceiver dataHandler;

	// Views
	private TextView morseText;
	private TextView decodedText;

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

	@Override
	public void onResume() {
		super.onResume();

		// Start the watch app
		PebbleKit.startAppOnPebble(getApplicationContext(), APP_UUID);

		// Create a Data Receiver
		dataHandler = new PebbleKit.PebbleDataReceiver(APP_UUID) {

			@Override
			public void receiveData(Context context, int transactionId,
					PebbleDictionary data) {
				// Ack to prevent timeouts
				PebbleKit.sendAckToPebble(context, transactionId);

				// Get which key was pressed
				int keyPressed = data.getUnsignedInteger(DATA_KEY).intValue();

				// Update UI
				switch (keyPressed) {
				case SELECT_BUTTON_KEY: {
					morseText.setText("Select button pressed");
					sendStringToPebble("Phone says 'select'");
					break;
				}
				case UP_BUTTON_KEY: {
					morseText.setText("Up button pressed");
					sendStringToPebble("Phone says 'up");
					break;
				}
				case DOWN_BUTTON_KEY: {
					morseText.setText("Down button pressed");
					sendStringToPebble("Phone says 'down");
					break;
				}
				default: {
					morseText.setText("Unkown button");
					break;
				}
				}

			}
		};

		// Register the Data Handler with Android to receive any message from
		// the watch
		PebbleKit.registerReceivedDataHandler(getApplicationContext(),
				dataHandler);
	}

	@Override
	public void onPause() {
		super.onPause();

		// Always deregister any Activity-scoped Broadcast Receivers when the
		// Activity is paused
		if (dataHandler != null) {
			unregisterReceiver(dataHandler);
			dataHandler = null;
		}
	}

}

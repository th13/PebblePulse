package dev.hacknc.pebblepulse;

import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

	private static final int BUFFER_LENGTH = 128;
	private PebbleKit.PebbleDataReceiver dataHandler;

	// Views
	private TextView morseText;
	private TextView decodeText;

	private String morseStr;
	private String decodeStr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
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
				morseStr = data.getString(DATA_KEY);

				morseText.setText(morseText.getText() + " " + morseStr);
				decodeStr = MorseDecoder.decode(morseStr);
				decodeText.setText(decodeText.getText() + decodeStr);
				morseStr = "";

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

	/**
	 * Send string to Pebble watch app
	 * 
	 * @param message
	 *            String to send
	 */
	private void sendStringToPebble(String message) {
		if (message.length() < BUFFER_LENGTH) {
			PebbleDictionary dictionary = new PebbleDictionary();
			dictionary.addString(DATA_KEY, message);
			PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID,
					dictionary);
		} else {
			Log.i("sendStringToPebble()", "String too long!");
		}
	}

	private void initialize() {
		morseText = (TextView) findViewById(R.id.tvMorse);
		decodeText = (TextView) findViewById(R.id.tvDecode);
		morseStr = "";
		decodeStr = "";
	}
}

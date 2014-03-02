package dev.hacknc.pebblepulse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends Activity {

	// Pebble application ID
	private static final UUID APP_UUID = UUID
			.fromString("0ca4b5be-331d-4d49-9850-e55245f72ded");

	private String userId;

	// Pebble button keys -- MUST BE IDENTICAL TO THOSE IN WATCH APP
	private static final int DATA_KEY = 0;
	private static final int SMS_KEY = 3;

	private static final int BUFFER_LENGTH = 128;
	private PebbleKit.PebbleDataReceiver dataHandler;

	// Views
	private TextView morseText;
	private TextView decodeText;

	private ParseObject messages;

	private String morseStr;
	private String decodeStr;
	private String to;

	private Button setLogin;
	private Button setSendTo;
	private TextView tvUser;
	private TextView tvSendTo;
	private EditText etUser;
	private EditText etSendTo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();

		setSendTo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				to = etSendTo.getText().toString();
				tvSendTo.setText(to);

			}

		});

		setLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
				query.whereEqualTo("user", userId);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> users, ParseException e) {
						if (e == null) {
							Log.d("score", "Retrieved " + users.size()
									+ " users");

							userId = users.get(0).getString("user");
							tvUser.setText(tvUser.getText() + " " + userId);

						} else {
							Log.d("score", "Error: " + e.getMessage());
						}

					}
				});

			}

		});

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

		// Check if connected to send any messages on the server

		HandlerThread hThread = new HandlerThread("HandlerThread");
		hThread.start();
		final Handler handler = new Handler(hThread.getLooper());
		Runnable r = new Runnable() {
			@Override
			public void run() {
				boolean connected = PebbleKit
						.isWatchConnected(getApplicationContext());

				if (connected) {
					// Pull data from server
					ParseQuery<ParseObject> query = ParseQuery
							.getQuery("Messages");
					query.whereEqualTo("to", userId);
					query.whereEqualTo("read", "no");
					query.findInBackground(new FindCallback<ParseObject>() {
						public void done(List<ParseObject> msgs,
								ParseException e) {
							if (e == null) {
								Log.d("score", "Retrieved " + msgs.size()
										+ " messages");

								final List<ParseObject> fmsgs = msgs;
								Handler uiHandler = new Handler();
								Runnable r = new Runnable() {
									public void run() {
										for (int i = 0; i < fmsgs.size(); i++) {
											Log.d("Data",
													"Message: "
															+ fmsgs.get(i)
																	.getString(
																			"morse"));
											sendAlertToPebble(fmsgs.get(i)
													.getString("morse")
													+ "\n"
													+ fmsgs.get(i).getString(
															"decode"));

											fmsgs.get(i).put("read", "yes");
											fmsgs.get(i).saveInBackground();
										}
									}
								};

								uiHandler.post(r);

							} else {
								Log.d("score", "Error: " + e.getMessage());
							}

						}
					});

					handler.postDelayed(this, 5000);
				}
			}
		};

		handler.postDelayed(r, 5000);

		// Create a Data Receiver
		dataHandler = new PebbleKit.PebbleDataReceiver(APP_UUID) {

			@Override
			public void receiveData(Context context, int transactionId,
					PebbleDictionary data) {
				// Ack to prevent timeouts
				PebbleKit.sendAckToPebble(context, transactionId);

				String decodeTmp;
				String morseTmp = data.getString(DATA_KEY);
				morseStr += morseTmp;

				morseText.setText(morseStr);
				decodeTmp = MorseDecoder.decode(morseTmp);
				decodeStr += decodeTmp;
				decodeText.setText(decodeStr);

				if (data.getInteger(SMS_KEY).intValue() == 1) {
					messages.put("to", to);
					messages.put("morse", morseStr);
					messages.put("decode", decodeStr);
					messages.put("read", "no");
					messages.saveInBackground();
					morseStr = "";
					decodeStr = "";
					morseText.setText("");
					decodeText.setText("");
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

	public void sendAlertToPebble(String msg) {
		final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

		final Map data = new HashMap();
		data.put("title", "PebblePulse");
		data.put("body", msg);
		final JSONObject jsonData = new JSONObject(data);
		final String notificationData = new JSONArray().put(jsonData)
				.toString();

		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "PebblePulse");
		i.putExtra("notificationData", notificationData);

		Log.d("Test", "About to send a modal alert to Pebble: "
				+ notificationData);
		sendBroadcast(i);
	}

	private void initialize() {
		morseText = (TextView) findViewById(R.id.tvMorse);
		decodeText = (TextView) findViewById(R.id.tvDecode);
		tvUser = (TextView) findViewById(R.id.textView3);
		tvSendTo = (TextView) findViewById(R.id.textView4);
		etUser = (EditText) findViewById(R.id.etUser);
		etSendTo = (EditText) findViewById(R.id.editText1);
		setLogin = (Button) findViewById(R.id.button1);
		setSendTo = (Button) findViewById(R.id.button2);
		morseStr = "";
		decodeStr = "";

		userId = "thelms501";

		Parse.initialize(this, "hzVOXqn5sGg6E4LQSuNnduF7Vgsh8r7RGYCfeaDc",
				"Ky9lB2gWiksTBQOUhHmxktZALgJF9Sw6LQ3mlG4o");
		messages = new ParseObject("Messages");
	}

}

#include <pebble.h>
#include <string.h>

// Enum pebble keys
enum {
	SELECT_KEY = 0x0,
	UP_KEY = 0x1,
	DOWN_KEY = 0x2,
	DATA_KEY = 0x0,
	SMS_KEY = 0x3
};

static Window *window;
static TextLayer *text_layer;
static AppTimer *timer;

char morse_text[20];


/**
* Sends a key press to phone
*/
static void send_msg(const char* morse_text) {
	Tuplet value = TupletCString(DATA_KEY, morse_text);
	Tuplet sms = TupletInteger(SMS_KEY, 0);

	// Construct the dictionary
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter);

	// Write tuplet to the dictionary
	dict_write_tuplet(iter, &value);
	dict_write_tuplet(iter, &sms);
	dict_write_end(iter);

	// Send dictionary and release buffer
	app_message_outbox_send();
}


/**
* Notifies phone to send sms
*/
static void notify_send_sms() {
	Tuplet sms = TupletInteger(SMS_KEY, 1);
	Tuplet value = TupletCString(DATA_KEY, "");

	DictionaryIterator *iter;
	app_message_outbox_begin(&iter);

	dict_write_tuplet(iter, &value);
	dict_write_tuplet(iter, &sms);
	dict_write_end(iter);

	app_message_outbox_send();
}

static void out_sent_handler(DictionaryIterator *sent, void *context) {
	text_layer_set_text(text_layer, "OK");
}

void out_failed_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
	text_layer_set_text(text_layer,"Oops! Something happened.");
}

void in_dropped_handler(AppMessageResult reason, void *context) {
	text_layer_set_text(text_layer,"Oops! Didn't get that.");
}

static void select_single_click_handler(ClickRecognizerRef recognizer, void *context) {
	send_msg(morse_text);
  	text_layer_set_text(text_layer, "OK");
  	strcpy(morse_text, "");
}

static void up_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  	strcat(morse_text, ".");
  	text_layer_set_text(text_layer, "Dot");
}

static void down_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  	strcat(morse_text, "-");
  	text_layer_set_text(text_layer, "Dash");
}

static void select_long_click_handler(ClickRecognizerRef recognizer, void *context) {
	notify_send_sms();
	text_layer_set_text(text_layer, "Sent the message!");
}


static void select_long_click_release_handler(ClickRecognizerRef recognizer, void *context) {

}


static void window_load(Window *window) {
	Layer *window_layer = window_get_root_layer(window);
	GRect bounds = layer_get_bounds(window_layer);
	text_layer = text_layer_create((GRect) { .origin = { 0, 72 }, .size = { bounds.size.w, 20 } });
	text_layer_set_text(text_layer, "Press a button");
	text_layer_set_text_alignment(text_layer, GTextAlignmentCenter);
	layer_add_child(window_layer, text_layer_get_layer(text_layer));
}

static void window_unload(Window *window) {
    text_layer_destroy(text_layer);
}


static void timer_callback(void *data) {
	AccelData accel = (AccelData) { .x = 0, .y = 0, .z = 0 };

	accel_service_peek(&accel);

	if (accel.y > 5) {
	    strcat(morse_text, ".");
		text_layer_set_text(text_layer, "Up");
		vibes_short_pulse();
	}
	  
	timer = app_timer_register(50, timer_callback, NULL);
}


void config_provider(Window *window) {
    // single click / repeat-on-hold config:
    window_single_click_subscribe(BUTTON_ID_DOWN, down_single_click_handler);
    window_single_click_subscribe(BUTTON_ID_UP, up_single_click_handler);
    window_single_click_subscribe(BUTTON_ID_SELECT, select_single_click_handler);
    window_long_click_subscribe(BUTTON_ID_SELECT, 700, select_long_click_handler, select_long_click_release_handler);
}

static void app_message_init(void) {
	app_message_open(128, 128);
	app_message_register_inbox_dropped(in_dropped_handler);
	app_message_register_outbox_sent(out_sent_handler);
	app_message_register_outbox_failed(out_failed_handler);
}

static void init(void) {
    window = window_create();
    window_set_window_handlers(window, (WindowHandlers) {
      .load = window_load,
      .unload = window_unload,
    });

    window_set_click_config_provider(window, (ClickConfigProvider) config_provider);
    window_stack_push(window, true);

    accel_data_service_subscribe(0, NULL);
    //timer = app_timer_register(50, timer_callback, NULL);

    app_message_init();
}

static void deinit(void) {
	accel_data_service_unsubscribe();
    window_destroy(window);
}

int main(void) {
	init();
  	app_event_loop();
  	deinit();
}

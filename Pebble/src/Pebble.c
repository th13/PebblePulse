#include <pebble.h>

// Enum pebble keys
enum {
	SELECT_KEY = 0x0,
	UP_KEY = 0x1,
	DOWN_KEY = 0x2,
	DATA_KEY = 0x0
};

static Window *window;
static TextLayer *text_layer;

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Select");
}

static void up_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Up");
}

static void down_click_handler(ClickRecognizerRef recognizer, void *context) {
  text_layer_set_text(text_layer, "Down");
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
  window_single_click_subscribe(BUTTON_ID_UP, up_click_handler);
  window_single_click_subscribe(BUTTON_ID_DOWN, down_click_handler);
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


/**
* Handler for AppMessage sent
*/
void out_sent_handler(DictionaryIterator* sent, void* context) {
	// Send successful
}


/**
* Handler for AppMessage send failed
*/
static void out_fail_handler(DictionaryIterator* failed, AppMessageResult reason, void* context) {
	// Send failed
}


/**
* Handler for received AppMessage
*/
static void in_received_handler(DictionaryIterator* iter, void* context) {
	// Data received here
}


/**
* Handler for received messages dropped
*/
void in_drop_handler(void* context, AppMessageResult reaason) {
	// Received failed -- mesage dropped
}


/**
* Main Pebble loop
*/
void pbl_main(void* params) {
	PebbleAppHandlers handlers = {
		.init_handler = &handle_init,

		.messaging_info = {
			// Set the sizess of the buffers
			.buffer_sizes = {
				.inbound = 64,
				.outbound = 16,
			},

			// Use default callback mode
			.default_callbcks.callbacks = {
				.out_sent = out_sent_handler,
				.out_failed = out_fail_handler,
				.in_received = in_received_handler,
				.in_dropped = in_drop_handler,
			},
		}
	};

	app_event_loop(params, &handlers);
}


static void init(void) {
  window = window_create();
  window_set_click_config_provider(window, click_config_provider);
  window_set_window_handlers(window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  const bool animated = true;
  window_stack_push(window, animated);
}

static void deinit(void) {
  window_destroy(window);
}

int main(void) {
  init();

  APP_LOG(APP_LOG_LEVEL_DEBUG, "Done initializing, pushed window: %p", window);

  app_event_loop();
  deinit();
}

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


/**
* Sends a key press to phone
*/
static void send_cmd(uint8_t cmd) {
	Tuplet value = TupletInteger(DATA_KEY, cmd);

	// Construct the dictionary
	DictionaryIterator *iter;
	app_message_outbox_begin(&iter);

	// Write tuplet to the dictionary
	dict_write_tuplet(iter, &value);
	dict_write_end(iter);

	// Send dictionary and release buffer
	app_message_outbox_send();
}

static void select_single_click_handler(ClickRecognizerRef recognizer, void *context) {
	send_cmd(SELECT_KEY);
  	text_layer_set_text(text_layer, "Select");
}

static void up_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  	send_cmd(UP_KEY);
  	text_layer_set_text(text_layer, "Up");
}

static void down_single_click_handler(ClickRecognizerRef recognizer, void *context) {
  	send_cmd(DOWN_KEY);
  	text_layer_set_text(text_layer, "Down");
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


void config_provider(Window *window) {
    // single click / repeat-on-hold config:
    window_single_click_subscribe(BUTTON_ID_DOWN, down_single_click_handler);
    window_single_click_subscribe(BUTTON_ID_UP, up_single_click_handler);
    window_single_click_subscribe(BUTTON_ID_SELECT, select_single_click_handler);
}

static void app_message_init(void) {
	app_message_open(64, 64);
}

static void init(void) {
    window = window_create();
    window_set_window_handlers(window, (WindowHandlers) {
      .load = window_load,
      .unload = window_unload,
    });

    window_set_click_config_provider(window, (ClickConfigProvider) config_provider);
    window_stack_push(window, true);
    app_message_init();
}

static void deinit(void) {
    window_destroy(window);
}

int main(void) {
	init();
  	app_event_loop();
  	deinit();
}

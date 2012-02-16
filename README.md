Android_Pusher
==============

This code provides a simple implementation allowing for an Android 1.6+ app to receive realtime events from http://pusher.com

This project makes use of a slightly modified Weberknect, available at http://v.google.com/p/weberknecht/

Special thanks to http://natronbaxter.com/ and http://mokasocial.com for giving me the opportunity to write and open source this!

This project is under the Apache License, Version 2.0! http://www.apache.org/licenses/LICENSE-2.0.html

To get started receiving events from your Pusher account:

 - Clone this repository locally
 - Sign up for an account at http://pusher.com
 - Download Eclipse and the Android SDK from http://developer.android.com/sdk/index.html
 - Open the project in Eclipse (Using File > New Project > From Existing Source)
 - Open PusherSampleActivity.java, and find the constants PUSHER_APP_KEY and PUSHER_CHANNEL_1
 - Change these constants to your assigned Pusher app key and your first channel.
 - Run the application on a device or emulator, and send messages from your Pusher console.
 - To add more channels, duplicate the existing code that handles inbound.
 - To handle multiple event types, modify the Activity's mHandler.handleMessage() function, checking the "type" string of the incoming event.

Quick Example of the new API:
 
``` java
Pusher pusher = new Pusher( "pusher_key", "pusher_secret" );
PusherChannel channel = pusher.subscribe( "chat-channel" );
channel.bind( "new_message" , new PusherCallback() {
  public void onEvent(String eventName, JSONObject eventData, String channelName) {
    Toast.makeText(getApplicationContext(),
    							 "Received\nEvent: " + eventName + "\nChannel: " + channelName + "\nData: " + eventData.toString(),
    							 Toast.LENGTH_LONG).show();
    							   
    Log.d(LOG_TAG, "Received " + eventData.toString() + " for event '" + eventName + "' on channel '" + channelName + "'.");
    /* this code runs in the UI thread by default, so you can change UI elements */
    myTextField.setText( eventData.getString("content") );
  }
  /* note that eventName and channelName are redundant here */
});
```

you can bind to any event of a channel as long as you are subscribed to it:

``` java
channel.bindAll(new PusherCallback() {
  public void onEvent(String eventName, JSONObject eventData, String channelName) {
    Log.d( "Pusher", "Received " + eventData.toString() + " for event '" + eventName +"' on channel " + channelName );
  }
});
```

bind() and bindAll() also work on the pusher instance, in order to retrieve events from all channels

``` java
pusher.bindAll(new PusherCallback() {
  public void onEvent(String eventName, JSONObject eventData, String channelName) {
    Log.d( "Pusher", "I will show up on any event on any subscribed channel" );
  }
});
```

by default, all callbacks run on the UI thread, you can change that behaviour by passing another Looper to PusherCallbacks's constructor

``` java
HandlerThread hd = new HandlerThread("i'm a separate thread");
hd.start();

pusher.bindAll(new PusherCallback(hd.getLooper()) { // if none given: Looper.getMainLooper()
  /* i will be executed in hd's thread */
  public void onEvent(String eventName, JSONObject eventData, String channelName) {
    /* i'm fine */
    Log.d( "Pusher", "I will show up on any event on any subscribed channel" );

    /* note that the following wont work, since we aren't in the ui thread. Toasts will work, though */
    someTextField.setText("i will kill your app");
  }
});
```

you can send client events to any subscribed private channel (note: client events must be supported by your pusher app, presence channels aren't support yet)

``` java
pusher.sendEvent("client-test", new JSONObject(), "private-channel");
```


For support, visit http://pusher.com/support.
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
Pusher pusher = new Pusher( "pusher_key" );
final Channel channel = pusher.subscribe( "chat-channel" );
channel.bind( "new_message" , new PusherCallback() {
  public void onEvent(JSONObject eventData) {
    Log.d( "Pusher", "Received " + eventData.toString() + " for event 'new_message' on channel " + channel.name );
    myTextField.setText( eventData.getString("content") );
  }
});
```

you can bind to any event of a channel as long as you are subscribed to it:

``` java
channel.bindAll(new PusherCallback() {
  public void onEvent(String eventName, JSONObject eventData) {
    Log.d( "Pusher", "Received " + eventData.toString() + " for event '" + eventName +"' on channel " + channel.name );
  }
});
```

bind() and bindAll() also work on the pusher instance, in order to retrieve events from all channels

``` java
pusher.bindAll(new PusherCallback() {
  public void onEvent(String eventName, JSONObject eventData) {
    Log.d( "Pusher", "I will show up on any event on any subscribed channel" );
  }
});
```

For support, visit http://pusher.com/support.
package com.emorym.android_pusher;

import org.json.JSONObject;

public interface PusherCallback
{
	public void onEvent(JSONObject eventData);
}

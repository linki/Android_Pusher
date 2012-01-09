package com.emorym.android_pusher;

import org.json.JSONObject;

public interface PusherCallback
{
	public void handle(JSONObject json);
}

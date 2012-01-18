package com.emorym.android_pusher;

/*  Copyright (C) 2011 Emory Myers
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DeprecatedPusherSampleActivity extends Activity
{
	private static final String PUSHER_APP_KEY = "f40fa92a7a408191e053";
	private static final String PUSHER_CHANNEL_1 = "test1";
	private static final String PUSHER_CHANNEL_2 = "test2";

	private Handler mHandler;
	private Pusher mPusher;
	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		mContext = this;
		setContentView( R.layout.main );

		// This Handler is going to deal with incoming messages
		mHandler = new Handler()
		{
			@Override
			public void handleMessage( Message msg )
			{
				super.handleMessage( msg );
				Bundle bundleData = msg.getData();
				if( bundleData.getString( "type" ).contentEquals( "pusher" ) )
				{
					try
					{
						JSONObject message = new JSONObject( bundleData.getString( "message" ) );

						Log.d( "Pusher Message", message.toString() );

						Toast.makeText( mContext, message.toString(), Toast.LENGTH_SHORT ).show();
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}
			}
		};

		mPusher = new Pusher( mHandler );
		mPusher.connect( PUSHER_APP_KEY );

		// Setup some toggles to subscribe/unsubscribe from our 2 test channels
		final ToggleButton test1 = (ToggleButton) findViewById( R.id.toggleButton1 );
		final ToggleButton test2 = (ToggleButton) findViewById( R.id.toggleButton2 );

		final Button send = (Button) findViewById( R.id.send_button );

		test1.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				if( test1.isChecked() )
					mPusher.subscribe( PUSHER_CHANNEL_1 );
				else
					mPusher.unsubscribe( PUSHER_CHANNEL_1 );
			}
		} );

		test2.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View v )
			{
				if( test2.isChecked() )
					mPusher.subscribe( PUSHER_CHANNEL_2 );
				else
					mPusher.unsubscribe( PUSHER_CHANNEL_2 );
			}
		} );

		send.setOnClickListener( new View.OnClickListener()
		{

			@Override
			public void onClick( View v )
			{
				EditText channelName = (EditText) findViewById( R.id.channel_name );
				EditText eventName = (EditText) findViewById( R.id.event_name );
				EditText eventData = (EditText) findViewById( R.id.event_data );

				try
				{
					JSONObject data = new JSONObject();
					data.put( "data", eventData.getText().toString() );
					mPusher.send( eventName.getText().toString(), data, channelName.getText().toString() );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
		} );
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mPusher.disconnect();
	}

}
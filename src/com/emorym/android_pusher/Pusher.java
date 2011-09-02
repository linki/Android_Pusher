package com.emorym.android_pusher;

/*	Copyright (C) 2011 Emory Myers 
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

import java.net.URI;
import java.util.HashMap;

import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.roderick.weberknecht.WebSocket;
import de.roderick.weberknecht.WebSocketConnection;
import de.roderick.weberknecht.WebSocketEventHandler;
import de.roderick.weberknecht.WebSocketMessage;

public class Pusher
{
	protected static final long WATCHDOG_SLEEP_TIME_MS = 5000;
	private final String VERSION = "1.8.3";
	private final String HOST = "ws.pusherapp.com";
	private final int WS_PORT = 80;
	private final String PREFIX = "ws://";

	private WebSocket mWebSocket;
	private final Handler mHandler;
	private Thread mWatchdog; // handles reconnecting
	private String mSocketId;

	public Pusher(Handler _mHandler)
	{
		// So we can get our messages back to whatever created this
		mHandler = _mHandler;
		channels = new HashMap<String, Channel>();
		mWebSocket = null;
	}

	private class Channel
	{
		public String name;

		public Channel(String _name)
		{
			name = _name;
		}
	}

	private final HashMap<String, Channel> channels;

	public void disconnect()
	{
		try
		{
			mWatchdog.interrupt();
			mWatchdog = null;
			mWebSocket.close();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void subscribe( String channelName )
	{
		Channel c = new Channel( channelName );

		if( mWebSocket != null && mWebSocket.isConnected() )
		{
			try
			{
				sendSubscribeMessage( c );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		channels.put( channelName, c );
	}

	public void unsubscribe( String channelName )
	{
		if( channels.containsKey( channelName ) )
		{
			if( mWebSocket != null && mWebSocket.isConnected() )
			{
				try
				{
					sendUnsubscribeMessage( channels.get( channelName ) );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}

			channels.remove( channelName );
		}
	}

	private void subscribeToAllChannels()
	{
		try
		{
			for( String channelName : channels.keySet() )
			{
				sendSubscribeMessage( channels.get( channelName ) );
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	private void sendSubscribeMessage( Channel c )
	{
		JSONObject data = new JSONObject();

		send( "pusher:subscribe", data, c.name );
	}

	private void sendUnsubscribeMessage( Channel c )
	{
		JSONObject data = new JSONObject();

		send( "pusher:unsubscribe", data, c.name );
	}

	public void send( String event_name, JSONObject data, String channel )
	{
		JSONObject message = new JSONObject();

		try
		{
			data.put( "channel", channel );
			message.put( "event", event_name );
			message.put( "data", data );
			Log.d( "Message", message.toString() );
			mWebSocket.send( message.toString() );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	public void connect( String application_key )
	{
		String path = "/app/" + application_key + "?client=js&version=" + VERSION;

		try
		{
			URI url = new URI( PREFIX + HOST + ":" + WS_PORT + path );
			Log.d( "Connecting", url.toString() );
			mWebSocket = new WebSocketConnection( url );
			mWebSocket.setEventHandler( new WebSocketEventHandler()
			{
				public void onOpen()
				{
					Log.d( "Open", "WebSocket Open" );
					subscribeToAllChannels();
				}

				public void onMessage( WebSocketMessage message )
				{
					try
					{
						Log.d( "Message", message.getText() );

						JSONObject jsonMessage = new JSONObject( message.getText() );

						String event = jsonMessage.optString( "event", null );

						if( event.equals( "pusher:connection_established" ) )
						{
							JSONObject data = new JSONObject( jsonMessage.getString( "data" ) );

							mSocketId = data.getString( "socket_id" );

							Log.d( "Connection Established", "Socket Id: " + mSocketId );
						}
						else
						{
							Bundle b = new Bundle();
							b.putString( "type", "pusher" );
							b.putString( "message", message.getText() );
							Message msg = new Message();
							msg.setData( b );
							mHandler.sendMessage( msg );
						}
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}
				}

				public void onClose()
				{
					Log.d( "Close", "WebSocket Closed" );
				}
			} );

			mWatchdog = new Thread( new Runnable()
			{
				public void run()
				{
					boolean interrupted = false;
					while (!interrupted)
					{
						try
						{
							Thread.sleep( WATCHDOG_SLEEP_TIME_MS );
							if( !mWebSocket.isConnected() )
								mWebSocket.connect();
						}
						catch( InterruptedException e )
						{
							interrupted = true;
						}
						catch( Exception e )
						{
							e.printStackTrace();
						}
					}
				}
			} );

			mWatchdog.start();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}

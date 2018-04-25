package com.zzy.smarttouch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class BaseActivity extends Activity
{
	public notifyService mService = null;
	private onServiceConnection callback;
	public interface onServiceConnection
	{
		public void callback();
	}
	
	public void setonServiceConnection(onServiceConnection callback)
	{
		this.callback = callback;
	}

	private ServiceConnection mConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			mService = ((notifyService.MsgBinder)service).getService();
			if(callback !=null)
			{
				callback.callback();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			mService = null;
		}

	};

	@Override
	protected void onStart()
	{
		Common.LogEx("base activity onstart");
		bindService(new Intent(this.getApplicationContext(),notifyService.class), mConnection, Context.BIND_AUTO_CREATE);
		super.onStart();
	}

	@Override
	protected void onStop()
	{
		Common.LogEx("base activity onStop");
		
		if (mService !=null)
		{
			unbindService(mConnection);
			mService = null;
		}
		super.onStop();
	}
}
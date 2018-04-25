package com.zzy.smarttouch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaButtonBroadcastReceiver extends BroadcastReceiver
{
	@Override
    public void onReceive(Context context, Intent intent)
    {
		Intent in = new Intent(context,notifyService.class);
		if (intent != null)
		{
			in.putExtra("system_internal_intent", intent);
		}
		context.startService(in);
    }

}

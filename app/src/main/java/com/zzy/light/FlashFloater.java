package com.zzy.light;

import com.zzy.light.FlashlightSurface.TakePictureListen;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

import com.zzy.record.RecordWin;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.notifyService;
import com.zzy.smarttouch.smartKeyApp;

import java.io.File;

public class FlashFloater
{
	private WindowManager windowManager;
	private LayoutParams windowManagerParams = new LayoutParams();
	private boolean isShow;
	private View viewPopu;
	FlashlightSurface sfCamera;
	private boolean bTakePic=false; 

	
	public FlashFloater(Context context,boolean bTakePic)
	{
		isShow = false;
		this.bTakePic = bTakePic;
		Init(context);
	}

	public void Init(Context c)
	{
		windowManager = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		windowManagerParams.type = LayoutParams.TYPE_PHONE|LayoutParams.TYPE_SYSTEM_ERROR;
		windowManagerParams.format = PixelFormat.RGBA_8888;
		windowManagerParams.flags =LayoutParams.FLAG_FULLSCREEN
									|LayoutParams.FLAG_NOT_FOCUSABLE
									|LayoutParams.FLAG_LAYOUT_NO_LIMITS
									|LayoutParams.FLAG_DISMISS_KEYGUARD
									|LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		
		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
		
		windowManagerParams.x =0;
		windowManagerParams.y =0;
		windowManagerParams.width =1;
		windowManagerParams.height =1;

		LayoutInflater mLayoutInflater = LayoutInflater.from(c);
		viewPopu = mLayoutInflater.inflate(R.layout.smartkey_flash, null);
		sfCamera = (FlashlightSurface)viewPopu.findViewById(R.id.sfCamera);
	}
	
	private void NotityCamera(Context context,String sPath)
	{
		NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		// ����֪ͨ��չ�ֵ�������Ϣ
		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = context.getApplicationContext().getText(R.string.STR_APP_CAMERA_QUICK);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		// ��������֪ͨ��ʱҪչ�ֵ�������Ϣ
		CharSequence contentTitle = tickerText;
		CharSequence contentText = sPath;
		
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_SEND);
		File file = new File(sPath);
		Uri uri = Uri.fromFile(file);
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

		Notification.Builder builder = new Notification.Builder(context);
		builder.setContentIntent(contentIntent);
		builder.setAutoCancel(false);
		builder.setWhen(System.currentTimeMillis());
		builder.setOngoing(true);
		builder.setContentTitle(contentTitle);
		builder.setContentText(contentText);
		builder.setSmallIcon(R.drawable.ic_launcher);

		notification = builder.build();
		notification.defaults &= ~Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.icon = R.drawable.ic_launcher;

		mNotificationManager.notify(1, notification);

		smartKeyApp.mInstance.showToast("picture path:"+sPath);

	}
	
	
	private TakePictureListen takePictureListen = new TakePictureListen()
	{

		@Override
        public void finish(Context context, String sName)
        {
			if(sName!=null)
			{
				NotityCamera(context,sName);
			}
	        close();
        }

	};
	
	public void ShowHide()
	{
		if(isShow)
		{
			Common.LogEx("ShowHide hide");
			sfCamera.setFlashlightSwitch(false);
			windowManager.removeView(viewPopu);
			isShow = false;
		}
		else
		{
			Common.LogEx("ShowHide show");
			if(bTakePic)
			{
				sfCamera.SetOnTakePictureListen(takePictureListen);
				sfCamera.setFlashlightSwitch(false);
			}
			else
			{
				sfCamera.setFlashlightSwitch(true); 
			}
			windowManager.addView(viewPopu, windowManagerParams);
			isShow = true;
		}
	}
	
	
	public void show()
	{
		if(isShow)
		{
			return;
		}
		windowManager.addView(viewPopu, windowManagerParams);
		isShow = true;
		if(bTakePic)
		{
			sfCamera.SetOnTakePictureListen(takePictureListen);
			sfCamera.setFlashlightSwitch(false);
		}
		else
		{
			sfCamera.setFlashlightSwitch(true); 
		}
	}
	
	public void close()
	{
		if (isShow)
		{
			sfCamera.setFlashlightSwitch(false); 
			windowManager.removeView(viewPopu);
			isShow = false;
		 
		}
	}
	
}

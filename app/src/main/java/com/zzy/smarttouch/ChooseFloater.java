package com.zzy.smarttouch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import com.zzy.smarttouch.R;


public class ChooseFloater implements OnClickListener
{
	private WindowManager windowManager;
	private LayoutParams windowManagerParams = new LayoutParams();
	private boolean isShow;
	private View viewPopu;
	
	private String sHeadsetName;
	private int iMicrophone;
	
	public ChooseFloater()
	{
		isShow = false;
	}

	public void show(Context c,String sHeadsetName,int iMicrophone)
	{
		this.sHeadsetName = sHeadsetName;
		this.iMicrophone = iMicrophone;
		
		Common.LogEx("show sHeadsetName="+sHeadsetName+" iMicrophone="+iMicrophone);
	    if(isShow)
	    {
	    	return;
	    }
		windowManager = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		windowManagerParams.type = LayoutParams.TYPE_PHONE|LayoutParams.TYPE_SYSTEM_ERROR;
		windowManagerParams.format = PixelFormat.RGBA_8888;
		windowManagerParams.flags =LayoutParams.FLAG_FULLSCREEN
									|LayoutParams.FLAG_NOT_FOCUSABLE
									|LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
		
		int iScreenW = c.getResources().getDisplayMetrics().widthPixels-(int)(96*c.getResources().getDisplayMetrics().density);
		int iScreenH = c.getResources().getDisplayMetrics().heightPixels;
		
		int x = (int)(48*c.getResources().getDisplayMetrics().density);
		int y = (iScreenH-iScreenH/2)/2;
		
		windowManagerParams.x =x;
		windowManagerParams.y =y;
		windowManagerParams.width =iScreenW;
		windowManagerParams.height =iScreenH/2;

		LayoutInflater mLayoutInflater = LayoutInflater.from(c);
		viewPopu = mLayoutInflater.inflate(R.layout.smartkey_choose, null);

		Button btnPopuRightKey = (Button)viewPopu.findViewById(R.id.btnPopuRightKey);
		btnPopuRightKey.setOnClickListener(this);
		
		Button btnPopuLeft = (Button)viewPopu.findViewById(R.id.btnPopuLeft);
		btnPopuLeft.setOnClickListener(this);
		
		windowManager.addView(viewPopu, windowManagerParams);
		
		isShow = true;
	}
	
	public void close()
	{
		if (isShow)
		{
			windowManager.removeView(viewPopu);
			isShow = false;
		}
	}
	
	private void SetViewLayout(View view, int x, int y, int w, int h)
	{
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)view.getLayoutParams();
		params.width = w;
		params.height = h;
		params.leftMargin = x;
		params.topMargin = y;
		view.setLayoutParams(params);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.btnPopuRightKey:
				close();
				SetHeadsetState(view.getContext(),sHeadsetName,0,iMicrophone);
				break;
			case R.id.btnPopuLeft:
				close();
				break;
			default:
				break;
		}
		
	}
	
	public static void SetHeadsetState(Context context, String sName,int iState, int iMicrophone)
	{
		try
		{
			AudioManager localAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
			if (Build.VERSION.SDK_INT >= 16)
			{
				Class localClass2 = localAudioManager.getClass();
				Class[] arrayOfClass2 = new Class[3];
				arrayOfClass2[0] = Integer.TYPE;
				arrayOfClass2[1] = Integer.TYPE;
				arrayOfClass2[2] = String.class;
				Method localMethod2 = localClass2.getMethod("setWiredDeviceConnectionState", arrayOfClass2);
				localMethod2.setAccessible(true);
				Object[] arrayOfObject2 = new Object[3];
				if (iMicrophone == 1)
				{
					arrayOfObject2[0] = Integer.valueOf(4);
					arrayOfObject2[1] = Integer.valueOf(0);
					arrayOfObject2[2] = sName;
					localMethod2.invoke(localAudioManager, arrayOfObject2);
				}
				else 
				{
					arrayOfObject2[0] = Integer.valueOf(8);
					arrayOfObject2[1] = Integer.valueOf(0);
					arrayOfObject2[2] = sName;
					localMethod2.invoke(localAudioManager, arrayOfObject2);
				}
			} 
			else
			{

				Intent localIntent = new Intent();
				localIntent.putExtra("name", sName);
				localIntent.putExtra("state", iState);
				localIntent.putExtra("microphone", iMicrophone);
				localIntent.putExtra("from_smartkey", 1);
				localIntent.addFlags(1073741824);
				localIntent.setAction("android.intent.action.HEADSET_PLUG");
				Class localClass1 = Class.forName("android.app.ActivityManagerNative");
				Class[] arrayOfClass1 = new Class[2];
				arrayOfClass1[0] = Intent.class;
				arrayOfClass1[1] = String.class;
				Method localMethod1 = localClass1.getMethod("broadcastStickyIntent", arrayOfClass1);
				localMethod1.setAccessible(true);
				Object[] arrayOfObject1 = new Object[2];
				arrayOfObject1[0] = localIntent;
				arrayOfObject1[1] = null;
				localMethod1.invoke(localClass1, arrayOfObject1);
			}
		} 
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

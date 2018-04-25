package com.zzy.smarttouch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.zzy.smarttouch.notifyService.OnActionCallBack;

import android.R.integer;
import android.R.interpolator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class MainFloater
{
	public static final int TAP_WIDTH = 32;
	public static final int TAP_HEIGHT = 32;

	private int CLICK_WIDTH;
	private int CLICK_HEIFHT;
	private WindowManager windowManager;
	private LayoutParams windowManagerParams = new LayoutParams();
	private boolean isShow;
	private View viewParent;
	private OnActionCallBack onActionCallBack;
	private Context context;

	private float fStartX = 0;
	private float fStartY = 0;
	private int iScreenW = 0;
	private int iScreenH = 0;
	private int iState = 0;

	private long iCurrClickTick = 0;
	private int iClickCount = 0;
	private int iClickViewID = 0;
	private float iDpi;

	public MainFloater(Context context, OnActionCallBack onActionCallBack, int iState)
	{
		iDpi = context.getResources().getDisplayMetrics().density;
		isShow = false;
		this.context = context;
		this.onActionCallBack = onActionCallBack;
		this.iState = iState;
		Init(context);
	}

	public static int getStatusBarHeight(Context context)
	{
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try
		{
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		return statusBarHeight;
	}

	public void Init(Context c)
	{
		windowManager = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		windowManagerParams.type =LayoutParams.TYPE_SYSTEM_ERROR;
		windowManagerParams.format = PixelFormat.RGBA_8888;
		windowManagerParams.flags = LayoutParams.FLAG_FULLSCREEN
		        | LayoutParams.FLAG_LAYOUT_IN_SCREEN
		        |
		        // | LayoutParams.FLAG_NOT_FOCUSABLE
		        LayoutParams.FLAG_LAYOUT_NO_LIMITS | LayoutParams.FLAG_TURN_SCREEN_ON
		        | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_SHOW_WHEN_LOCKED
		        | LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;

		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;

		DisplayMetrics display = c.getResources().getDisplayMetrics();
		iScreenW = display.widthPixels / 4;
		iScreenH = display.heightPixels;
		
		CLICK_WIDTH = (int) (TAP_WIDTH * iDpi);
		CLICK_HEIFHT = getStatusBarHeight(c);
		
		if(CLICK_HEIFHT<20)
		{
			CLICK_HEIFHT = 20;
		}
		
		switch (iState)
		{
		case smartKeyApp.CLICK_STATE_ONE:
			windowManagerParams.x = (iScreenW - CLICK_WIDTH) / 2;
			windowManagerParams.y = 0;
			break;
		case smartKeyApp.CLICK_STATE_TWO:
			windowManagerParams.x = iScreenW + (iScreenW - CLICK_WIDTH) / 2;
			windowManagerParams.y = 0;
			break;
		case smartKeyApp.CLICK_STATE_THRID:
			windowManagerParams.x = 2 * iScreenW + (iScreenW - CLICK_WIDTH) / 2;
			windowManagerParams.y = 0;
			break;
		case smartKeyApp.CLICK_STATE_FOUR:
			windowManagerParams.x = 3 * iScreenW + (iScreenW - CLICK_WIDTH) / 2;
			windowManagerParams.y = 0;
			break;
		default:
			break;
		}

		windowManagerParams.width = CLICK_WIDTH;
		windowManagerParams.height = CLICK_HEIFHT;

		LayoutInflater mLayoutInflater = LayoutInflater.from(c);
		viewParent = mLayoutInflater.inflate(R.layout.smartkey_main_floater, null);

	}

	public void show()
	{
		if (isShow)
		{
			return;
		}

		try
		{
			if (viewParent == null)
			{
				LayoutInflater mLayoutInflater = LayoutInflater.from(context);
				viewParent = mLayoutInflater.inflate(R.layout.smartkey_main_floater, null);
			}

			LinearLayout lyMainFloater = (LinearLayout) viewParent.findViewById(R.id.lyMainFloater);
			lyMainFloater.setOnTouchListener(onTouchListener);

			windowManager.addView(viewParent, windowManagerParams);
			isShow = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		try
		{
			if (isShow)
			{
				windowManager.removeView(viewParent);
			}
			Common.LogEx("floater close");
			isShow = false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	private OnTouchListener onTouchListener = new OnTouchListener()
	{

		@Override
		public boolean onTouch(View view, MotionEvent event)
		{
			if (onActionCallBack == null)
			{
				return false;
			}

			float x, y;
			x = event.getX();
			y = event.getY();
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				fStartX = x;
				fStartY = y;
				break;
			case MotionEvent.ACTION_UP:

				float xCha,
				yCha;
				xCha = Math.abs(fStartX - x);
				yCha = Math.abs(fStartY - y);
				Common.LogEx("onTouchListener xCha:" + xCha + " yCha:" + yCha);
				if (xCha < 10 && yCha < 10)
				{
					onActionCallBack.ActionClick(iState);
					//onTapClick(R.id.lyMainFloater);
				}
				else if (xCha < 10 && yCha > 10)
				{
					expandStatusBar(view.getContext());
				}
				break;

			case MotionEvent.ACTION_MOVE:
				break;
			default:
				// v.performClick();
				break;
			}

			return true;
		}
	};

	private void expandStatusBar(Context ctx)
	{
		Object sbService = ctx.getSystemService("statusbar");
		try
		{
			Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
			Method expandMethod;

			if (Build.VERSION.SDK_INT >= 17)
			{
				expandMethod = statusBarManager.getMethod("expandNotificationsPanel");
			}
			else
			{
				expandMethod = statusBarManager.getMethod("expand");
			}
			expandMethod.invoke(sbService);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

//	public void onTapClick(int id)
//	{
//		if (onActionCallBack != null)
//		{
//			long iNow = SystemClock.elapsedRealtime();
//			if (iClickViewID == id && iClickCount == 1 && iNow - iCurrClickTick < 500)
//			{
//				onActionCallBack.ActionClick(iState);
//				iClickCount = 0;
//				iClickViewID = 0;
//			}
//			else
//			{
//				iCurrClickTick = iNow;
//				iClickViewID = id;
//				iClickCount = 1;
//			}
//
//		}
//	}
}

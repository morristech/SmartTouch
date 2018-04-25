package com.zzy.popu;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class PopupWindows
{
	protected Context mContext;
	protected PopupWindow mWindow;
	protected View mRootView;
	protected Drawable mBackground = null;
	protected WindowManager mWindowManager;

	public PopupWindows(Context context)
	{
		this.mContext = context;
		this.mWindow = new PopupWindow(context);

		this.mWindow.setTouchInterceptor(new View.OnTouchListener()
		{
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE)
				{
					PopupWindows.this.mWindow.dismiss();

					return true;
				}

				return false;
			}
		});
		this.mWindowManager = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
	}

	protected void onDismiss()
	{
	}

	protected void onShow()
	{
	}

	protected void preShow()
	{
		if (this.mRootView == null)
		{
			throw new IllegalStateException("setContentView was not called with a view to display.");
		}
		onShow();

		if (this.mBackground == null)
		{
			this.mWindow.setBackgroundDrawable(new BitmapDrawable(mContext.getResources()));
		}
		else
		{
			this.mWindow.setBackgroundDrawable(this.mBackground);
		}
		this.mWindow.setWidth(-2);
		this.mWindow.setHeight(-2);
		this.mWindow.setTouchable(true);
		this.mWindow.setFocusable(true);
		this.mWindow.setOutsideTouchable(true);

		this.mWindow.setContentView(this.mRootView);
	}

	public void setBackgroundDrawable(Drawable background)
	{
		this.mBackground = background;
	}

	public void setContentView(View root)
	{
		this.mRootView = root;

		this.mWindow.setContentView(root);
	}

	public void setContentView(int layoutResID)
	{
		LayoutInflater inflator = (LayoutInflater) this.mContext.getSystemService("layout_inflater");

		setContentView(inflator.inflate(layoutResID, null));
	}

	public void setOnDismissListener(PopupWindow.OnDismissListener listener)
	{
		this.mWindow.setOnDismissListener(listener);
	}

	public void dismiss()
	{
		this.mWindow.dismiss();
	}
}

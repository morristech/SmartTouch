package com.zzy.privacy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;


public class CustomLineLayout extends LinearLayout
{

	private WinQuit winQuit;

	public CustomLineLayout(Context context)
    {
	    super(context);
    }
	
	public CustomLineLayout(Context context, AttributeSet attrs)
    {
	    super(context,attrs);
    }
	
	@SuppressLint("NewApi")
	public CustomLineLayout(Context context, AttributeSet attrs, int defStyle)
    {
	    super(context, attrs, defStyle);
    }
	
	
	public void setOnWinQuitListen(WinQuit winQuit)
	{
		this.winQuit = winQuit;
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		switch (event.getKeyCode())
		{
		case KeyEvent.KEYCODE_BACK:
			if(winQuit!=null)
			{
				winQuit.quit();
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	
	public interface WinQuit
	{
		public void quit();
	}
}

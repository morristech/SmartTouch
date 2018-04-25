package com.zzy.unlock;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.Common;

public class unlockActivity extends Activity
{
	public static Activity mActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_unlock);
	
		int iFlag = LayoutParams.FLAG_DISMISS_KEYGUARD
				   |LayoutParams.FLAG_SHOW_WHEN_LOCKED
				   |LayoutParams.FLAG_TURN_SCREEN_ON
		           |LayoutParams.FLAG_KEEP_SCREEN_ON;
		getWindow().addFlags(iFlag);
		getWindow().getAttributes().screenBrightness = 0.0f;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mActivity = this;
		Common.LogEx("unlockActivity onResume");
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		mActivity = null;
		Common.LogEx("unlockActivity close");
	}
	
}

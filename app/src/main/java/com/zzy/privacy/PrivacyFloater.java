package com.zzy.privacy;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zzy.privacy.CustomLineLayout.WinQuit;
import com.zzy.privacy.LockPatternView.Cell;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.smartKeyApp;
import com.zzy.smarttouch.notifyService.OnActionCallBack;

public class PrivacyFloater
{
	private WindowManager windowManager;
	private LayoutParams windowManagerParams = new LayoutParams();
	private boolean isShow;
	private CustomLineLayout viewParent;
	private Context context;
	
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private Animation mShakeAnim;
	private CountDownTimer mCountdownTimer = null;
	private String sPackeName;
	private ActivityManager mActivityManager;
	
	public PrivacyFloater(Context context)
	{
		isShow = false;
		this.context = context;
		mActivityManager = ((ActivityManager)context.getSystemService("activity"));
		Init(context);
	}

	public void Init(Context c)
	{
		windowManager = (WindowManager) c.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		windowManagerParams.type = LayoutParams.TYPE_PHONE | LayoutParams.TYPE_SYSTEM_ERROR;
		windowManagerParams.format = PixelFormat.RGBA_8888;
		windowManagerParams.flags = LayoutParams.FLAG_FULLSCREEN
									| LayoutParams.FLAG_LAYOUT_IN_SCREEN
									| LayoutParams.FLAG_LAYOUT_NO_LIMITS
									| LayoutParams.FLAG_TURN_SCREEN_ON 
									| LayoutParams.FLAG_DISMISS_KEYGUARD
									| LayoutParams.FLAG_SHOW_WHEN_LOCKED;
									//| LayoutParams.FLAG_NOT_TOUCH_MODAL
									//| LayoutParams.FLAG_NOT_FOCUSABLE;
		
		

		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
		windowManagerParams.width = LayoutParams.MATCH_PARENT;
		windowManagerParams.height = LayoutParams.MATCH_PARENT;

		LayoutInflater mLayoutInflater = LayoutInflater.from(c);
		viewParent = (CustomLineLayout)mLayoutInflater.inflate(R.layout.smartkey_gesturepassword_unlock, null);
	}
	
	private WinQuit winQuit=new WinQuit()
	{
		
		@Override
		public void quit()
		{
			// TODO Auto-generated method stub
			//close();
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			context.startActivity(intent);
		}
	};
	
	public void show(String sPackName)
	{
		if(isShow)
		{
			return;
		}
		
		this.sPackeName = sPackName;
		
		isShow = true;
		if(viewParent ==null)
		{
			LayoutInflater mLayoutInflater = LayoutInflater.from(context);
			viewParent = (CustomLineLayout)mLayoutInflater.inflate(R.layout.smartkey_gesturepassword_unlock, null);
		}
		windowManager.addView(viewParent, windowManagerParams);
		
		viewParent.setOnWinQuitListen(winQuit);
		
//		viewParent.setOnKeyListener(new OnKeyListener()
//		{
//			
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event)
//			{
//				if(event.getKeyCode()==KeyEvent.KEYCODE_BACK || event.getKeyCode()==KeyEvent.KEYCODE_HOME)
//				{
//					close();
//				}				
//				return false;
//			}
//		});
		
		mLockPatternView = (LockPatternView) viewParent.findViewById(R.id.gesturepwd_unlock_lockview);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) viewParent.findViewById(R.id.gesturepwd_unlock_text);
		mShakeAnim = AnimationUtils.loadAnimation(context, R.anim.shake_x);
	}
	
	public void close()
	{
		if (isShow && viewParent!=null)
		{
			mHandler.removeCallbacks(attemptLockout);
			if(mCountdownTimer !=null)
			{
				mCountdownTimer.cancel();
			}
			if(sPackeName !=null && sPackeName.length()>1)
			{
				mActivityManager.killBackgroundProcesses(sPackeName);
			}
			sPackeName = null;
			windowManager.removeView(viewParent);
		}
		isShow = false;
	}
	
	private Runnable mClearPatternRunnable = new Runnable()
	{
		public void run()
		{
			mLockPatternView.clearPattern();
		}
	};

	protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener()
	{

		public void onPatternStart()
		{
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			patternInProgress();
		}

		public void onPatternCleared()
		{
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}

		public void onPatternDetected(List<LockPatternView.Cell> pattern)
		{
			if (pattern == null)
			{
				return;
			}
			
			if (smartKeyApp.mInstance.getLockPatternUtils().checkPattern(pattern))
			{
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
				close();
			} 
			else
			{
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
				if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL)
				{
					mFailedPatternAttemptsSinceLastTimeout++;
					int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT- mFailedPatternAttemptsSinceLastTimeout;
					if (retry >= 0)
					{
						if (retry == 0)
						{
							smartKeyApp.mInstance.showToast(R.string.STR_PATTERN_BEYOND_TIMES);
						}
						
						String sTime = context.getText(R.string.STR_REMAIN_TIMES).toString();
						sTime = sTime.replace("%s",String.valueOf(retry));
						mHeadTextView.setText(sTime);
						mHeadTextView.setTextColor(Color.RED);
						mHeadTextView.startAnimation(mShakeAnim);
					}

				} 
				else
				{
					smartKeyApp.mInstance.showToast(R.string.STR_PATTERN_TOO_SHORT);
				}

				if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)
				{
					if(mHandler !=null)
					{
						mHandler.postDelayed(attemptLockout, 2000);
					}
				} else
				{
					if(mLockPatternView!=null)
					{
						mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
					}
				}
			}
		}

		public void onPatternCellAdded(List<Cell> pattern)
		{

		}

		private void patternInProgress()
		{
			mHeadTextView.setText("");
		}
	};
	
	private Runnable attemptLockout = new Runnable()
	{

		@Override
		public void run()
		{
			mLockPatternView.clearPattern();
			mLockPatternView.setEnabled(false);
			mCountdownTimer = new CountDownTimer(LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000)
			{

				@Override
				public void onTick(long millisUntilFinished)
				{
					int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
					if (secondsRemaining > 0)
					{
						String sTime = context.getText(R.string.STR_PATTERN_TRY_TIME).toString();
						sTime = sTime.replace("%s",String.valueOf(secondsRemaining));
						mHeadTextView.setText(sTime);
					} 
					else
					{
						mHeadTextView.setText("");
						mHeadTextView.setTextColor(Color.BLACK);
					}
				}

				@Override
				public void onFinish()
				{
					mLockPatternView.setEnabled(true);
					mFailedPatternAttemptsSinceLastTimeout = 0;
				}
			}.start();
		}
	};
}

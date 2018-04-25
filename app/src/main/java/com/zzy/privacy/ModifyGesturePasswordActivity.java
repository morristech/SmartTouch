package com.zzy.privacy;

import java.util.List;

import com.zzy.privacy.LockPatternView.Cell;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.notifyService;
import com.zzy.smarttouch.smartKeyApp;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class ModifyGesturePasswordActivity extends Activity
{
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private Animation mShakeAnim;
	private ImageView imgvAppIcon;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_gesturepassword_unlock);

		imgvAppIcon = (ImageView) this.findViewById(R.id.imgvAppIcon);
		mLockPatternView = (LockPatternView) this.findViewById(R.id.gesturepwd_unlock_lockview);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
		mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		PackageManager pManager = getPackageManager();
		try
        {
			if(notifyService.sShowPackName==null || notifyService.sShowPackName.length()<1)
			{
				imgvAppIcon.setImageResource(R.drawable.ic_launcher);
			}
			else 
			{
				Drawable dr = pManager.getApplicationIcon(notifyService.sShowPackName);
				imgvAppIcon.setImageDrawable(dr);
			}
	       
        }
        catch (NameNotFoundException e)
        {
	        // TODO Auto-generated catch block
        	imgvAppIcon.setImageResource(R.drawable.ic_launcher);
	        e.printStackTrace();
        }
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

		public void onPatternDetected(List<Cell> pattern)
		{
			if (pattern == null)
			{
				return;
			}
			
			if (smartKeyApp.mInstance.getLockPatternUtils().checkPattern(pattern))
			{
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
				setResult(Activity.RESULT_OK);
				finish();
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
						
						String sTime = getText(R.string.STR_REMAIN_TIMES).toString();
						sTime = sTime.replace("%s",String.valueOf(retry));
						mHeadTextView.setText(sTime);
						mHeadTextView.setTextColor(Color.RED);
						mHeadTextView.startAnimation(mShakeAnim);
					}

				} 
				else
				{
					mHeadTextView.setTextColor(Color.RED);
					mHeadTextView.setText(R.string.STR_PATTERN_TOO_SHORT);
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
			mHeadTextView.setTextColor(Color.BLACK);
			mHeadTextView.setText(R.string.STR_DRAW_GESTURE_PASSWORD);
		}
	};
	
	
	Runnable attemptLockout = new Runnable()
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
						String sTime = getText(R.string.STR_PATTERN_TRY_TIME).toString();
						sTime = sTime.replace("%s",String.valueOf(secondsRemaining));
						mHeadTextView.setText(sTime);
					} 
					else
					{
						mHeadTextView.setText(R.string.STR_DRAW_GESTURE_PASSWORD);
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

package com.zzy.privacy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.zzy.privacy.LockPatternView.Cell;
import com.zzy.privacy.LockPatternView.DisplayMode;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.smartKeyApp;

import android.R.raw;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CreateGesturePassword
{
	private static final int ID_EMPTY_MESSAGE = -1;
	private static final String KEY_UI_STAGE = "uiStage";
	private static final String KEY_PATTERN_CHOICE = "chosenPattern";
	private LockPatternView mLockPatternView;
	private Button mFooterRightButton;
	private Button mFooterLeftButton;
	protected TextView mHeaderText;
	protected List<Cell> mChosenPattern = null;
	private Toast mToast;
	private Stage mUiStage = Stage.Introduction;
	private View mPreviewViews[][] = new View[3][3];
	private Activity activity;
	private LockInterface lockInterface;
	/**
	 * The patten used during the help screen to show how to draw a pattern.
	 */
	private final List<Cell> mAnimatePattern = new ArrayList<Cell>();

	/**
	 * The states of the left footer button.
	 */
	enum LeftButtonMode
	{
		Cancel(R.string.STR_CANCEL, true), 
		CancelDisabled(R.string.STR_CANCEL, false), 
		Retry(R.string.STR_RETRY, true), 
		RetryDisabled(R.string.STR_RETRY, false), 
		Gone(0, false);
		/**
		 * @param text
		 *            The displayed text for this mode.
		 * @param enabled
		 *            Whether the button should be enabled.
		 */
		LeftButtonMode(int text, boolean enabled)
		{
			this.text = text;
			this.enabled = enabled;
		}

		final int text;
		final boolean enabled;
	}

	/**
	 * The states of the right button.
	 */
	enum RightButtonMode
	{
		Continue(R.string.STR_CONTINUE, true), 
		ContinueDisabled(R.string.STR_CONTINUE, false), 
		Confirm(R.string.STR_CONFIRM, true), 
		ConfirmDisabled(R.string.STR_CONFIRM, false), 
		Ok(R.string.STR_OK, true);

		/**
		 * @param text
		 *            The displayed text for this mode.
		 * @param enabled
		 *            Whether the button should be enabled.
		 */
		RightButtonMode(int text, boolean enabled)
		{
			this.text = text;
			this.enabled = enabled;
		}

		final int text;
		final boolean enabled;
	}

	/**
	 * Keep track internally of where the user is in choosing a pattern.
	 */
	protected enum Stage
	{

		Introduction(R.string.STR_DRAW_UNLOCK_PATTERN,
					 LeftButtonMode.Cancel, 
					 RightButtonMode.ContinueDisabled,
					 ID_EMPTY_MESSAGE, 
					 true), 
		HelpScreen(R.string.STR_PATTERN_HOW_TO_DRAW,
				   LeftButtonMode.Gone, 
				   RightButtonMode.Ok, 
				   ID_EMPTY_MESSAGE,
				   false), 
		ChoiceTooShort(R.string.STR_PATTERN_TOO_SHORT,
						LeftButtonMode.Retry, 
						RightButtonMode.ContinueDisabled,
						ID_EMPTY_MESSAGE,
						true),
		FirstChoiceValid(R.string.STR_PATTERN_RECORED,
						LeftButtonMode.Retry, 
						RightButtonMode.Continue,
						ID_EMPTY_MESSAGE, false), 
		NeedToConfirm(R.string.STR_PATTERN_DRAW_AGAIN, 
						LeftButtonMode.Cancel,
						RightButtonMode.ConfirmDisabled, 
						ID_EMPTY_MESSAGE, true), 
		ConfirmWrong(R.string.STR_PATTERN_MISMATCH,
					LeftButtonMode.Cancel, 
					RightButtonMode.ConfirmDisabled,
					ID_EMPTY_MESSAGE, 
					true),
		ChoiceConfirmed(R.string.STR_PATTERN_SAVE,
						LeftButtonMode.Cancel, 
						RightButtonMode.Confirm,
						ID_EMPTY_MESSAGE,
						false);

		/**
		 * @param headerMessage
		 *            The message displayed at the top.
		 * @param leftMode
		 *            The mode of the left button.
		 * @param rightMode
		 *            The mode of the right button.
		 * @param footerMessage
		 *            The footer message.
		 * @param patternEnabled
		 *            Whether the pattern widget is enabled.
		 */
		Stage(int headerMessage, LeftButtonMode leftMode,
				RightButtonMode rightMode, int footerMessage,
				boolean patternEnabled)
		{
			this.headerMessage = headerMessage;
			this.leftMode = leftMode;
			this.rightMode = rightMode;
			this.footerMessage = footerMessage;
			this.patternEnabled = patternEnabled;
		}

		final int headerMessage;
		final LeftButtonMode leftMode;
		final RightButtonMode rightMode;
		final int footerMessage;
		final boolean patternEnabled;
	}
	
	public CreateGesturePassword(Activity activity,LockInterface lockInterface)
	{
		this.activity = activity;
		this.lockInterface = lockInterface;
		
		mAnimatePattern.add(Cell.of(0, 0));
		mAnimatePattern.add(Cell.of(0, 1));
		mAnimatePattern.add(Cell.of(1, 1));
		mAnimatePattern.add(Cell.of(2, 1));
		mAnimatePattern.add(Cell.of(2, 2));
		
		mLockPatternView = (LockPatternView)this.activity.findViewById(R.id.gesturepwd_create_lockview);
		mHeaderText = (TextView) this.activity.findViewById(R.id.gesturepwd_create_text);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);

		mFooterRightButton = (Button) this.activity.findViewById(R.id.right_btn);
		mFooterLeftButton = (Button) this.activity.findViewById(R.id.reset_btn);
		initPreviewViews();
	
		mFooterLeftButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (mUiStage.leftMode == LeftButtonMode.Retry)
				{
					mChosenPattern = null;
					mLockPatternView.clearPattern();
					updateStage(Stage.Introduction);
				} 
				else if (mUiStage.leftMode == LeftButtonMode.Cancel)
				{
					// They are canceling the entire wizard
					CreateGesturePassword.this.lockInterface.cancel(null);
				} 
				else
				{
					throw new IllegalStateException("left footer button pressed, but stage of " + mUiStage+ " doesn't make sense");
				}

			}
		});
		
		mFooterRightButton.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				if (mUiStage.rightMode == RightButtonMode.Continue)
				{
					if (mUiStage != Stage.FirstChoiceValid)
					{
						throw new IllegalStateException("expected ui stage "
								+ Stage.FirstChoiceValid + " when button is "
								+ RightButtonMode.Continue);
					}
					updateStage(Stage.NeedToConfirm);
				} 
				else if (mUiStage.rightMode == RightButtonMode.Confirm)
				{
					if (mUiStage != Stage.ChoiceConfirmed)
					{
						throw new IllegalStateException("expected ui stage "
								+ Stage.ChoiceConfirmed + " when button is "
								+ RightButtonMode.Confirm);
					}
					saveChosenPatternAndFinish();
				} 
				else if (mUiStage.rightMode == RightButtonMode.Ok)
				{
					if (mUiStage != Stage.HelpScreen)
					{
						throw new IllegalStateException("Help screen is only mode with ok button, but "+ "stage is " + mUiStage);
					}
					mLockPatternView.clearPattern();
					mLockPatternView.setDisplayMode(DisplayMode.Correct);
					updateStage(Stage.Introduction);
				}
			}
		});
		
	}
	
	private void initPreviewViews()
	{
		mPreviewViews = new View[3][3];
		mPreviewViews[0][0] = this.activity.findViewById(R.id.gesturepwd_setting_preview_0);
		mPreviewViews[0][1] = this.activity.findViewById(R.id.gesturepwd_setting_preview_1);
		mPreviewViews[0][2] = this.activity.findViewById(R.id.gesturepwd_setting_preview_2);
		mPreviewViews[1][0] = this.activity.findViewById(R.id.gesturepwd_setting_preview_3);
		mPreviewViews[1][1] = this.activity.findViewById(R.id.gesturepwd_setting_preview_4);
		mPreviewViews[1][2] = this.activity.findViewById(R.id.gesturepwd_setting_preview_5);
		mPreviewViews[2][0] = this.activity.findViewById(R.id.gesturepwd_setting_preview_6);
		mPreviewViews[2][1] = this.activity.findViewById(R.id.gesturepwd_setting_preview_7);
		mPreviewViews[2][2] = this.activity.findViewById(R.id.gesturepwd_setting_preview_8);
	}
	
	private void updatePreviewViews()
	{
		if(mChosenPattern == null)
		{
			return;
		}
		
		for (Cell cell : mChosenPattern)
		{
			mPreviewViews[cell.getRow()][cell.getColumn()].setBackgroundResource(R.drawable.gesture_create_grid_selected);
		}
	}

	public void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(KEY_UI_STAGE, mUiStage.ordinal());
		if (mChosenPattern != null)
		{
			outState.putString(KEY_PATTERN_CHOICE,LockPatternUtils.patternToString(mChosenPattern));
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0)
		{
			if (mUiStage == Stage.HelpScreen)
			{
				updateStage(Stage.Introduction);
				return true;
			}
		}
		if (keyCode == KeyEvent.KEYCODE_MENU && mUiStage == Stage.Introduction)
		{
			updateStage(Stage.HelpScreen);
			return true;
		}
		return false;
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
			// Common.NetiPayLog("result = " + pattern.toString());
			if (mUiStage == Stage.NeedToConfirm || mUiStage == Stage.ConfirmWrong)
			{
				if (mChosenPattern == null)
				{
					throw new IllegalStateException("null chosen pattern in stage 'need to confirm");
				}
				if (mChosenPattern.equals(pattern))
				{
					updateStage(Stage.ChoiceConfirmed);
				} 
				else
				{
					updateStage(Stage.ConfirmWrong);
				}
			} 
			else if (mUiStage == Stage.Introduction || mUiStage == Stage.ChoiceTooShort)
			{
				if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE)
				{
					updateStage(Stage.ChoiceTooShort);
				} 
				else
				{
					mChosenPattern = new ArrayList<Cell>(pattern);
					updateStage(Stage.FirstChoiceValid);
				}
			} 
			else
			{
				throw new IllegalStateException("Unexpected stage " + mUiStage+ " when " + "entering the pattern.");
			}
		}

		public void onPatternCellAdded(List<Cell> pattern)
		{

		}

		private void patternInProgress()
		{
			int id = R.string.STR_PATTERN_RELEASE_FINGER;
			mHeaderText.setText(id);
			mFooterLeftButton.setEnabled(false);
			mFooterRightButton.setEnabled(false);
		}
	};

	private void updateStage(Stage stage)
	{
		mUiStage = stage;
		if (stage == Stage.ChoiceTooShort)
		{
			mHeaderText.setText(this.activity.getResources().getString(stage.headerMessage,LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
		} 
		else
		{
			mHeaderText.setText(stage.headerMessage);
		}

		if (stage.leftMode == LeftButtonMode.Gone)
		{
			mFooterLeftButton.setVisibility(View.GONE);
		} 
		else
		{
			mFooterLeftButton.setVisibility(View.VISIBLE);
			mFooterLeftButton.setText(stage.leftMode.text);
			mFooterLeftButton.setEnabled(stage.leftMode.enabled);
		}
		
		mFooterRightButton.setText(stage.rightMode.text);
		mFooterRightButton.setEnabled(stage.rightMode.enabled);

		// same for whether the patten is enabled
		if (stage.patternEnabled)
		{
			mLockPatternView.enableInput();
		} else
		{
			mLockPatternView.disableInput();
		}

		mLockPatternView.setDisplayMode(DisplayMode.Correct);

		switch (mUiStage)
		{
		case Introduction:
			mLockPatternView.clearPattern();
			break;
		case HelpScreen:
			mLockPatternView.setPattern(DisplayMode.Animate, mAnimatePattern);
			break;
		case ChoiceTooShort:
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			postClearPatternRunnable();
			break;
		case FirstChoiceValid:
			break;
		case NeedToConfirm:
			mLockPatternView.clearPattern();
			updatePreviewViews();
			break;
		case ConfirmWrong:
			mLockPatternView.setDisplayMode(DisplayMode.Wrong);
			postClearPatternRunnable();
			break;
		case ChoiceConfirmed:
			break;
		}

	}

	// clear the wrong pattern unless they have started a new one
	// already
	private void postClearPatternRunnable()
	{
		mLockPatternView.removeCallbacks(mClearPatternRunnable);
		mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
	}

	private void saveChosenPatternAndFinish()
	{
		smartKeyApp.mInstance.getLockPatternUtils().saveLockPatternDefault(mChosenPattern);
		this.lockInterface.finish(null);
	}
	
	public void Entry()
	{
		mChosenPattern = null;
		mLockPatternView.clearPattern();
		updateStage(Stage.HelpScreen);
	}
	
	public void ClearAll()
	{
		mChosenPattern = null;
		mLockPatternView.clearPattern();
		activity = null;
	}
}

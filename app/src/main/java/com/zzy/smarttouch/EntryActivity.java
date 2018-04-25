package com.zzy.smarttouch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.pocket.network.HttpDeal;
import com.pocket.network.HttpTask;
import com.pocket.network.HttpTask.DownLoadInterface;
import com.zzy.game.GameAdpter;
import com.zzy.privacy.CreateGestureActivity;
import com.zzy.privacy.LockPatternUtils;
import com.zzy.privacy.LockPatternView;
import com.zzy.privacy.LockPatternView.Cell;
import com.zzy.privacy.LockPinyinComparator;
import com.zzy.privacy.PrivacyAdpter;
import com.zzy.privacy.PrivacyAdpter.LockAppSt;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zzy.smarttouch.BaseActivity.onServiceConnection;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.sortlist.CharacterParser;
import com.zzy.sortlist.SideBar;
import com.zzy.sortlist.SideBar.OnTouchingLetterChangedListener;
import com.zzy.top.DetectService;

public class EntryActivity extends BaseActivity implements OnClickListener, onServiceConnection
{
	private final int RESULT_CODE_PATTERN = 1;
	private final int TAB_1 = 0;
	private final int TAB_2 = 1;

	private ViewPager vpTab;
	private ImageView imgvMainMenu;
	private Button btnCreate;

	private LinearLayout lyPrivacyList;
	private LinearLayout lyGuide;
	private LinearLayout lyUnLock;

	private TextView tvMainTab1;
	private TextView tvMainTab2;
	private View vMainTab1;
	private View vMainTab2;
	private int iCurrTab = TAB_1;
	private boolean bFinishUpdate;
	private boolean bEntryPrivateList;

	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private Animation mShakeAnim;

	private ListView lvPrivacy;
	private PrivacyAdpter privacyAdpter;
	private boolean bUnlock;
	
	private ListView lvMain;
	private MainClickAdpter adpterClick;

	private Handler handler = new Handler(new Handler.Callback()
	{
		@Override
		public boolean handleMessage(Message msg)
		{
			if (msg.what == 1)
			{
				Common.StopProgressDialog();
				bFinishUpdate = true;
				if (bEntryPrivateList)
				{
					EntryIntoPrivacy();
				}
			}
			return false;
		}
	});

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_main);

		imgvMainMenu = (ImageView) findViewById(R.id.imgvMainMenu);
		imgvMainMenu.setOnClickListener(this);

		tvMainTab1 = (TextView) findViewById(R.id.tvMainTab1);
		tvMainTab1.setOnClickListener(this);
		tvMainTab2 = (TextView) findViewById(R.id.tvMainTab2);
		tvMainTab2.setOnClickListener(this);
		vMainTab1 = (View) findViewById(R.id.vMainTab1);
		vMainTab1.setOnClickListener(this);
		vMainTab2 = (View) findViewById(R.id.vMainTab2);
		vMainTab2.setOnClickListener(this);
		setTab(TAB_1);

		InitViewPage();
		bUnlock = false;
	}

	private void InitViewPage()
	{
		vpTab = (ViewPager) findViewById(R.id.vpTab);
		ArrayList<View> listViews = new ArrayList<View>();
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
		View vPageOne = mLayoutInflater.inflate(R.layout.smartkey_action_setting, vpTab, false);
		View vPageTwo = mLayoutInflater.inflate(R.layout.smartkey_privacy_main, vpTab, false);
		listViews.add(vPageOne);
		listViews.add(vPageTwo);
		
		lvMain = (ListView)vPageOne.findViewById(R.id.lvMain);
		
		TextView tvFind = (TextView)vPageOne.findViewById(R.id.tvFind);
		tvFind.setOnClickListener(this);
		TextView tvCheck = (TextView)vPageOne.findViewById(R.id.tvCheck);
		tvCheck.setOnClickListener(this);
		
		adpterClick = new MainClickAdpter(smartKeyApp.mInstance.getEventList(), this);
		lvMain.setAdapter(adpterClick);
		lvMain.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
				GotoSettingPage(position);
            }
		});
		
		lvMain.setOnItemLongClickListener(new OnItemLongClickListener()
		{

			@Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
				ClearSetting(position);
	            return false;
            }
		});

		vpTab.setAdapter(new ViewPageAdapter(listViews));
		vpTab.setCurrentItem(0);
		vpTab.setOnPageChangeListener(new MyOnPageChangeListener());

		btnCreate = (Button) vPageTwo.findViewById(R.id.btnCreate);
		btnCreate.setOnClickListener(this);

		lyPrivacyList = (LinearLayout) vPageTwo.findViewById(R.id.lyPrivacyList);
		lyGuide = (LinearLayout) vPageTwo.findViewById(R.id.lyGuide);
		lyUnLock = (LinearLayout) vPageTwo.findViewById(R.id.lyUnLock);

		String sTmp = smartKeyApp.mInstance.getLockPatternUtils().GetPattern();
		if (sTmp == null || sTmp.length() < 1)
		{
			lyGuide.setVisibility(View.VISIBLE);
			lyPrivacyList.setVisibility(View.GONE);
			lyUnLock.setVisibility(View.GONE);
		}
		else
		{
			lyGuide.setVisibility(View.GONE);
			lyPrivacyList.setVisibility(View.GONE);
			lyUnLock.setVisibility(View.VISIBLE);
		}

		bFinishUpdate = false;
		bEntryPrivateList = false;
		new onUpdateThread(handler, getApplicationContext()).start();

		mLockPatternView = (LockPatternView) vPageTwo.findViewById(R.id.gesturepwd_unlock_lockview);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) vPageTwo.findViewById(R.id.gesturepwd_unlock_text);
		mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);
		ImageView imgvAppIcon = (ImageView) vPageTwo.findViewById(R.id.imgvAppIcon);
		imgvAppIcon.setVisibility(View.GONE);

		lvPrivacy = (ListView) vPageTwo.findViewById(R.id.lvPrivacy);
		SideBar sbPrivacy = (SideBar) vPageTwo.findViewById(R.id.sbPrivacy);
		TextView tvPrivacy = (TextView) vPageTwo.findViewById(R.id.tvPrivacy);
		sbPrivacy.setTextView(tvPrivacy);

		sbPrivacy.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener()
		{
			@Override
			public void onTouchingLetterChanged(String s)
			{
				if (privacyAdpter == null)
				{
					return;
				}

				int position = privacyAdpter.getFirstPosition(s.charAt(0));
				if (position != -1)
				{
					lvPrivacy.setSelection(position);
				}
			}
		});
	}

	public void openAccessibilityService(Context context) {
		if (!DetectService.isAccessibilitySettingsOn(context)){
			Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			smartKeyApp.mInstance.showToast(R.string.STR_ACCESSBILITY);
		}
	}

	public class MyOnPageChangeListener implements OnPageChangeListener
	{

		@Override
		public void onPageSelected(int arg0)
		{
			if (arg0 == 0)
			{
				setTab(TAB_1);
			}
			else
			{
                openAccessibilityService(EntryActivity.this);
				setTab(TAB_2);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2)
		{
		}

		@Override
		public void onPageScrollStateChanged(int arg0)
		{
		}
	}
	
	public void GetGamePicRequest(String sUrl, String sDir, String sName)
	{
		HttpTask jobGameBox = new HttpTask(HttpTask.DOWNLOAD_ID_GAMEPIC,sName, GetGamePicReponse);
		jobGameBox.setDownInfo(sDir, sName);
		jobGameBox.JobGet(sUrl);
		mService.mitJob(jobGameBox);
	}
	
	private DownLoadInterface GetGamePicReponse = new DownLoadInterface()
	{
		@Override
		public void download_finish(int iReason, Object oInputParam, Object oOutParam)
		{
			if (iReason == HttpTask.DOWNLOAD_STATE_SUCC)
			{
				String sName = String.valueOf(oInputParam);
				ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
				EventSt stEvent;
				for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
				{
					stEvent = lsEvent.get(i);
					if(stEvent==null || stEvent.stPrograme==null)
					{
						continue;
					}
					
					if(stEvent.stPrograme.iId == smartKeyApp.APP_GAME_ID && stEvent.stPrograme.sApkPicName!=null && stEvent.stPrograme.sApkPicName.equals(sName))
					{
						Bitmap bmp;
						try
						{
							File desFile = new File(notifyService.getSetPath(), stEvent.stPrograme.sApkPicName);
							bmp = GameAdpter.LoadBitmapSample(desFile, 128, 128);
							if (bmp == null)
							{
								stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
							}
							else
							{
								stEvent.stPrograme.drIcon = new BitmapDrawable(getResources(), bmp);
							}
							
						}
						catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
							stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
						}
					}
				}
			}
		}
	};

	

	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(adpterClick !=null)
		{
			adpterClick.notifyDataSetChanged();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == RESULT_CODE_PATTERN)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				EntryIntoPrivacy();
			}
		}
	}

	private void GotoSettingPage(int index)
	{
		ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
		EventSt stEvent = lsEvent.get(index);

		Intent intent = new Intent(EntryActivity.this, AppList.class);
		intent.putExtra("ClickMode", stEvent.iClickState);
		startActivity(intent);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
		case R.id.imgvMainMenu:
			Intent intentMainMenu = new Intent(EntryActivity.this,SettingPage.class);
			startActivity(intentMainMenu);
			break;
		case R.id.tvMainTab1:
		case R.id.vMainTab1:
			setTab(TAB_1);
			vpTab.setCurrentItem(TAB_1);
			break;
		case R.id.tvMainTab2:
		case R.id.vMainTab2:
			setTab(TAB_2);
			vpTab.setCurrentItem(TAB_2);
			break;
		case R.id.btnCreate:
			Intent intentGesture = new Intent(EntryActivity.this, CreateGestureActivity.class);
			startActivityForResult(intentGesture, RESULT_CODE_PATTERN);
			break;
		case R.id.tvFind:
			Intent iFind = new Intent(EntryActivity.this, FindActivity.class);
			startActivity(iFind);
			break;
		case R.id.tvCheck:
			if (!HttpDeal.bNetOk)
			{
				Toast.makeText(this, R.string.STR_REG_NO_NETWORK, Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this, R.string.STR_CHECK_UPDATE_TIPS, Toast.LENGTH_LONG).show();
			mService.CheckApkUpdateRequest();
			break;
		default:
			break;
		}
	}

	@Override
	public void callback()
	{
		// TODO Auto-generated method stub

	}

	private void setTab(int index)
	{
		if (index < 0 || index > 1)
		{
			return;
		}

		int iSelColor = getResources().getColor(R.color.TAB_SELECT_COLOR);
		int iColor = getResources().getColor(color.black);
		switch (index)
		{
		case TAB_1:
			tvMainTab1.setTextColor(iSelColor);
			tvMainTab2.setTextColor(iColor);
			vMainTab1.setVisibility(View.VISIBLE);
			vMainTab2.setVisibility(View.INVISIBLE);
			break;
		case TAB_2:
			if (!bFinishUpdate)
			{
				Common.StartProgressDialog(this);
			}
			tvMainTab2.setTextColor(iSelColor);
			tvMainTab1.setTextColor(iColor);
			vMainTab2.setVisibility(View.VISIBLE);
			vMainTab1.setVisibility(View.INVISIBLE);
			break;
		}
		iCurrTab = index;
	}

	public static class onUpdateThread extends Thread
	{
		Handler handler;
		Context context;

		public onUpdateThread(Handler handler, Context context)
		{
			this.handler = handler;
			this.context = context;
		}

		@Override
		public void run()
		{
			LockPinyinComparator lockComparator;
			lockComparator = new LockPinyinComparator();

			ArrayList<LockAppSt> lsApp = smartKeyApp.mInstance.getLockAppList();
			lsApp.clear();

			CharacterParser characterParser = new CharacterParser();
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			List<ResolveInfo> packages = context.getPackageManager().queryIntentActivities(mainIntent, 0);

			LockAppSt tmpInfo;
			for (int i = 0; i < packages.size(); i++)
			{
				ResolveInfo stPackageInfo = packages.get(i);

				tmpInfo = new LockAppSt();
				tmpInfo.sAppName = stPackageInfo.activityInfo.loadLabel(context.getPackageManager()).toString();
				tmpInfo.sPackName = stPackageInfo.activityInfo.packageName;
				tmpInfo.drIcon = stPackageInfo.activityInfo.loadIcon(context.getPackageManager());
				tmpInfo.bLock = false;

				String pinyin = characterParser.getSelling(tmpInfo.sAppName);
				String sortString = pinyin.substring(0, 1).toUpperCase(Locale.US);
				if (sortString.matches("[A-Z]"))
				{
					tmpInfo.sortLetters = sortString.toUpperCase(Locale.US);
				}
				else
				{
					tmpInfo.sortLetters = "#";
				}
				lsApp.add(tmpInfo);
			}

			Collections.sort(lsApp, lockComparator);

			ArrayList<String> lsLock = smartKeyApp.mInstance.getLockList();
			String spackName;
			boolean bFound;
			int iCount = lsLock.size();
			for (int i = iCount - 1; i >= 0; i--)
			{
				spackName = lsLock.get(i);
				bFound = false;
				for (int j = 0; j < lsApp.size(); j++)
				{
					tmpInfo = lsApp.get(j);
					if (spackName.equals(tmpInfo.sPackName))
					{
						tmpInfo.bLock = true;
						bFound = true;
						break;
					}
				}

				if (!bFound)
				{
					lsLock.remove(i);
					smartKeyApp.mInstance.DeleteLockByPaceName(spackName);
				}
			}

			handler.sendEmptyMessage(1);
		}
	}

	private void EntryIntoPrivacy()
	{
		bUnlock = true;
		lyGuide.setVisibility(View.GONE);
		lyPrivacyList.setVisibility(View.VISIBLE);
		lyUnLock.setVisibility(View.GONE);

		bEntryPrivateList = false;
		if (bFinishUpdate)
		{
			if (privacyAdpter == null)
			{
				privacyAdpter = new PrivacyAdpter(this, smartKeyApp.mInstance.getLockAppList());
				lvPrivacy.setAdapter(privacyAdpter);
			}
			else
			{
				privacyAdpter.notifyDataSetChanged();
			}
		}
		else
		{
			bEntryPrivateList = true;
			Common.StartProgressDialog(this);
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
				EntryIntoPrivacy();
			}
			else
			{
				mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
				if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL)
				{
					mFailedPatternAttemptsSinceLastTimeout++;
					int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT
					        - mFailedPatternAttemptsSinceLastTimeout;
					if (retry >= 0)
					{
						if (retry == 0)
						{
							smartKeyApp.mInstance.showToast(R.string.STR_PATTERN_BEYOND_TIMES);
						}

						String sTime = getText(R.string.STR_REMAIN_TIMES).toString();
						sTime = sTime.replace("%s", String.valueOf(retry));
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
					if (mHandler != null)
					{
						mHandler.postDelayed(attemptLockout, 2000);
					}
				}
				else
				{
					if (mLockPatternView != null)
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
						sTime = sTime.replace("%s", String.valueOf(secondsRemaining));
						mHeadTextView.setText(sTime);
					}
					else
					{
						mHeadTextView.setTextColor(Color.BLACK);
						mHeadTextView.setText(R.string.STR_DRAW_GESTURE_PASSWORD);
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

	public void ClearSetting(int index)
	{
		if (index < 0 || index >= smartKeyApp.CLICK_STATE_MAX)
		{
			return;
		}
		ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
		EventSt stEvent = lsEvent.get(index);
		stEvent.stPrograme = null;
		stEvent.object = null;
		mService.CloseFloatByIndex(index);
		smartKeyApp.mInstance.SaveEvent();
	}
}

package com.zzy.smarttouch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.pocket.network.HttpDeal;
import com.pocket.network.HttpTask;
import com.pocket.network.HttpTask.DownLoadInterface;
import com.zzy.appswitch.appSitchAdpter;
import com.zzy.appswitch.switchActivity;
import com.zzy.game.GameAdpter;
import com.zzy.game.GameAdpter.GameBoxSt;
import com.zzy.light.FlashFloater;
import com.zzy.lock.AdminManager;
import com.zzy.top.DetectService;
import com.zzy.unlock.unlockActivity;

import android.Manifest;
import android.R.array;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Notification.Builder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.JsonToken;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

import com.zzy.privacy.UnlockGesturePasswordActivity;
import com.zzy.record.CostomRecord;
import com.zzy.record.RecordWin;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;
import com.zzy.smarttouch.R.string;
import com.zzy.smarttouch.RegeditActivity.OnRegeditState;
import com.zzy.sql.Config;

public class notifyService extends Service
{
	private final MsgBinder msgBinder = new MsgBinder();

	public static final String DIR_SMART_TOUCH = "SmartTouch";
	public static final String DIR_SMART_RECORD = "Record";
	private final String sApkName = "SmartTouch.APK";
	public static final String DIR_SMART_DATA = "Data";
	public static final String DIR_SMART_SET = "Set";
	public static final String DIR_SMART_APK = "apk";
	public static final String DIR_SMART_PIC = "pic";

	// private final String ACTION_PHONE = "android.intent.action.PHONE_STATE";
	private final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
	private final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
	private final String ACTION_MEDIA_BUTTON = "android.intent.action.MEDIA_BUTTON";
	private ActionReceiver brBrocast = null;
	private MediaButtonBroadcastReceiver brMediaButton = null;

	// private ChooseFloater chooseFloater;

	private long iClickTime = 0;
	private int iClickCount = 0;
	private long iLongTime = 0;
	private FlashFloater flashFloater;
	private FlashFloater cameraFloater;
	// ����APK
	public HttpDeal stPayNet;
	private MainFloater[] mainFloaters;
	private CostomRecord costomRecord;
	public OnRegeditState onRegeditState;

	public static String sShowPackName;

	public ArrayList<GameBoxSt> lsGameBox;
	public boolean bCanUpdate;
	private OnGameRequestListen onGameRequestListen;
	
	private final int MAX_GAME_DOWNLOAD = 14;
	private HttpTask jobGameBox;
	private ArrayList<String> lsDownLoad;
	private DownloadManager downloadManager;
	private boolean bCanUpdateGame;
	private SoundPool soundPool;
	private int iSoundId;
	
	private NotificationManager nm; 
	private Notification notification;
	private Builder builder;
	private final int nId=100;
	
	private Handler handler = new Handler(new Callback()
	{

		@Override
		public boolean handleMessage(Message msg)
		{
			if (msg.what == 1)
			{
				if (UnlockGesturePasswordActivity.mActivity == null)
				{
					Intent intent = new Intent(notifyService.this, UnlockGesturePasswordActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					sShowPackName = String.valueOf(msg.obj);
				}
				return true;
			}
			else if(msg.what==2)
			{
				if(Common.hasPermission(notifyService.this,Manifest.permission.READ_PHONE_STATE)) {
					if (HttpDeal.bNetOk) {
						bCanUpdateGame = true;
						DateTime dtTime = new DateTime();
						smartKeyApp.mInstance.SaveUpadteGameTime(dtTime.getNow());
						GetGameBoxRequest(0);
					}
				}
			}
			return false;
		}
	});

	public static class notifyService_BR extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			Intent in = new Intent(context, notifyService.class);
			if (intent != null)
			{
				in.putExtra("system_internal_intent", intent);
			}
			context.startService(in);
		}
	}

	public class MsgBinder extends Binder
	{
		public notifyService getService()
		{
			return notifyService.this;
		}
	}

	@Override
	public void onCreate()
	{
		try
        {
			BrocastStartListen();
			// MediaButtonStartListen();
			// chooseFloater = new ChooseFloater();

			flashFloater = new FlashFloater(this, false);
			cameraFloater = new FlashFloater(this, true);
			
			mainFloaters = new MainFloater[smartKeyApp.CLICK_STATE_MAX];
			for (int i = 0; i < smartKeyApp.CLICK_STATE_MAX; i++)
			{
				mainFloaters[i] = new MainFloater(this, onActionCallBack, i);
			}
			
			if(smartKeyApp.mInstance.getEnable())
			{
				StartTouch();
			}

			costomRecord = new CostomRecord(this);

			onRegeditState = null;
			stPayNet = new HttpDeal(this);

			lsGameBox = new ArrayList<GameBoxSt>();
			bCanUpdate = true;
			bCanUpdateGame = false;
			
			lsDownLoad = new ArrayList<String>();
			downloadManager = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
			
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addDataScheme("package");
			intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
			intentFilter.setPriority(2147483647);
			registerReceiver(rbDownlaod,intentFilter);

			TopThread topThread = new TopThread(handler, getApplicationContext());
			topThread.start();
			
			soundPool =new SoundPool(2,AudioManager.STREAM_SYSTEM,0);
			iSoundId = soundPool.load(this,R.raw.click, 1);
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        }
	}
	
	public void StartTouch()
	{
		try
        {
			ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
			EventSt stEvent;
			for (int i = 0; i < smartKeyApp.CLICK_STATE_MAX; i++)
			{
				stEvent = lsEvent.get(i);
				if(stEvent!=null && stEvent.stPrograme!=null)
				{
					mainFloaters[i].show();
				}
			}
			
			CreateNotity();
        }
        catch (Exception e)
        {
	        e.printStackTrace();
        }

	}
	
	public void StopTouch()
	{
		for (int i = 0; i < smartKeyApp.CLICK_STATE_MAX; i++)
		{
			mainFloaters[i].close();
		}
		
		StopNotify();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Common.LogEx("service onStartCommand");
		if (intent != null && intent.hasExtra("system_internal_intent"))
		{
			intent = intent.getParcelableExtra("system_internal_intent");
			if (intent.getAction().equalsIgnoreCase(CostomRecord.RECORD_STATE_ACTION))
			{
				Common.LogEx("service stop service");
				if (costomRecord != null)
				{
					costomRecord.StopRecord();
				}
			}
			// onRecevied(intent);
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		Common.LogEx("service onBind");
		return msgBinder;
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		Common.LogEx("service onUnbind");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy()
	{
		Common.LogEx("service onDestroy");
		super.onDestroy();
		BrocastStopListen();
		unregisterReceiver(rbDownlaod);

		if (mainFloaters != null)
		{
			for (int i = 0; i < mainFloaters.length; i++)
			{
				if (mainFloaters[i] != null)
				{
					mainFloaters[i].close();
				}
				mainFloaters[i] = null;
			}
		}
		mainFloaters = null;
		
		
		Intent intent  = new Intent(this,notifyService.class);
		if(android.os.Build.VERSION.SDK_INT>=12)
		{
			intent.setFlags(32);
		}
		startService(intent);

	}
	
	public void ShowFloatByIndex(int index)
	{
		if(smartKeyApp.mInstance.getEnable())
		{
			mainFloaters[index].show();
		}
	}
	
	public void CloseFloatByIndex(int index)
	{
		mainFloaters[index].close();
	}

	private void onRecevied(Intent intent)
	{
		if (intent == null)
		{
			return;
		}

		String sAction = intent.getAction();
		if (sAction == null)
		{
			return;
		}

		if (sAction.equalsIgnoreCase(Intent.ACTION_MEDIA_BUTTON))
		{
			KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (event == null)
			{
				return;
			}
			int iKeyCode = event.getKeyCode();
			int iKeyAction = event.getAction();
			long iDownTime = event.getEventTime();

			Common.LogEx("iKeyCode:" + iKeyCode + " iKeyAction:" + iKeyAction + " iDownTime:" + iDownTime);

			if (iKeyCode == KeyEvent.KEYCODE_HEADSETHOOK)
			{
				if (iKeyAction == KeyEvent.ACTION_UP)
				{
					if (Math.abs(iDownTime - iLongTime) > 800)
					{
						StartClick(4);
						iClickCount = 0;
						iClickTime = 0;
						return;
					}

					if (iClickTime != 0)
					{
						long iCha = iDownTime - iClickTime;
						Common.LogEx("iCha:" + iCha);
						if (iCha > 500)
						{
							StartClick(iClickCount);
							iClickCount = 0;
							iClickTime = 0;
							handler.removeCallbacks(rbDelay);
							return;
						}
						iClickCount++;
						iClickTime = iDownTime;
						handler.removeCallbacks(rbDelay);
						handler.postDelayed(rbDelay, 1000);
					}
					else
					{
						iClickCount++;
						iClickTime = iDownTime;
						handler.postDelayed(rbDelay, 1000);
					}
				}
				else if (iKeyAction == KeyEvent.ACTION_DOWN)
				{
					iLongTime = iDownTime;
				}

			}
		}
	}

	private Runnable rbDelay = new Runnable()
	{

		@Override
		public void run()
		{

			StartClick(iClickCount);
			iClickCount = 0;
			iClickTime = 0;

		}
	};

	private void BrocastStartListen()
	{
		if (brBrocast == null)
		{
			IntentFilter intentFilter = new IntentFilter();
			// intentFilter.addAction(ACTION_HEADSET_PLUG);
			intentFilter.addAction(ACTION_SCREEN_ON);
			// intentFilter.addAction(ACTION_PHONE);
			intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
			intentFilter.setPriority(2147483647);
			brBrocast = new ActionReceiver();
			registerReceiver(brBrocast, intentFilter);
		}
	}

	private void BrocastStopListen()
	{
		if (brBrocast != null)
		{
			unregisterReceiver(brBrocast);
		}
		brBrocast = null;
	}

	public class ActionReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String sAction = intent.getAction();
			if (sAction.equalsIgnoreCase(ACTION_HEADSET_PLUG))
			{
				if (intent.hasExtra("state"))
				{
					int state = intent.getIntExtra("state", 0);
					Common.LogEx("HeadsetPlugReceiver state=" + String.valueOf(state));
					if (state == 0)
					{
						// chooseFloater.close();
					}
					else if (state == 1)
					{
						// chooseFloater.show(notifyService.this,intent.getStringExtra("name"),intent.getIntExtra("microphone",0));
					}
				}
			}
			else if (sAction.equalsIgnoreCase(ACTION_SCREEN_ON))
			{
				Common.LogEx("ACTION_SCREEN_ON in");
				if (unlockActivity.mActivity != null)
				{
					unlockActivity.mActivity.finish();
				}
			}
			else if (sAction.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
			{
				boolean bFound=false;
				long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				for(int i=0;i<lsDownLoad.size();i++)
				{
					if(lsDownLoad.get(i).equals(String.valueOf(downloadId)))
					{
						bFound = true;
						break;
					}
				}
				
				if(!bFound)
				{
					return;
				}
				
				Query query = new Query();
				query.setFilterById(downloadId);
				Cursor c = downloadManager.query(query);
				if (c.moveToFirst())
				{
					int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex))
					{
						String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						Intent intentApk = new Intent(Intent.ACTION_VIEW);
						intentApk.setDataAndType(Uri.parse(uriString), "application/vnd.android.package-archive");
						intentApk.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intentApk);
					}
				}
			}
		}

	}

	private void MediaButtonStartListen()
	{

		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName media = new ComponentName(getPackageName(), MediaButtonBroadcastReceiver.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(media);

		if (brMediaButton == null)
		{
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ACTION_MEDIA_BUTTON);
			intentFilter.setPriority(2147483647);

			brMediaButton = new MediaButtonBroadcastReceiver();
			registerReceiver(brMediaButton, intentFilter);
		}
	}

	private void MediaButtonStopListen()
	{
		if (brMediaButton != null)
		{
			unregisterReceiver(brMediaButton);
		}
		brMediaButton = null;

		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		ComponentName media = new ComponentName(getPackageName(), MediaButtonBroadcastReceiver.class.getName());
		// ȡ��ע��
		mAudioManager.unregisterMediaButtonEventReceiver(media);
	}

	private void StartClick(int iType)
	{
		if (iType == 1)
		{
			DealClick(smartKeyApp.CLICK_STATE_ONE, notifyService.this);
		}
		else if (iType == 2)
		{
			DealClick(smartKeyApp.CLICK_STATE_TWO, notifyService.this);
		}
		else if (iType == 3)
		{
			DealClick(smartKeyApp.CLICK_STATE_THRID, notifyService.this);
		}
		else if (iType == 4)
		{
			DealClick(smartKeyApp.CLICK_STATE_FOUR, notifyService.this);
		}
	}

	private void DealClick(int iClickState, Context context)
	{

		EventSt stEvent = smartKeyApp.mInstance.getEvent(iClickState);
		if (stEvent == null)
		{
			return;
		}

		if (stEvent.stPrograme == null)
		{
			return;
		}

		Intent intent = null;
		switch (stEvent.stPrograme.iId)
		{
		case smartKeyApp.APP_CASE_ID:
			// if(timeFloater ==null)
			// {
			// timeFloater = new TimeFloater(this);
			// }
			// timeFloater.ShowHide(context);
			break;
		case smartKeyApp.APP_CAMERA_QUICK_ID:
			cameraFloater.ShowHide();
			break;
		case smartKeyApp.APP_FLASHLIGHT_ID:
			flashFloater.ShowHide();
			break;
		case smartKeyApp.APP_PHONE_ID:
			intent = new Intent(Intent.ACTION_CALL_BUTTON);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			break;
		case smartKeyApp.APP_CAMERA_ID:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			break;
		case smartKeyApp.APP_UNLOCK_ID:

			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			if (!pm.isScreenOn())
			{
				intent = new Intent(this, unlockActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
			break;
		case smartKeyApp.APP_LOCK_ID:
			AdminManager adminManager = smartKeyApp.mInstance.getAdminManage();
			if (adminManager.getEnabled())
			{
				adminManager.LockScreen();
			}
			break;
		case smartKeyApp.APP_SWITCH_ID:
			intent = new Intent(context, switchActivity.class);
			intent.putExtra("StartMode", switchActivity.START_MODE_ENTER);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			break;
		case smartKeyApp.APP_MORE_ID:
			PackageManager packageManager = context.getPackageManager();
			intent = packageManager.getLaunchIntentForPackage(stEvent.stPrograme.sPackName);
			context.startActivity(intent);
			break;
		default:
			break;
		}
	}
	
	public void ClickSound()
	{
		if(smartKeyApp.mInstance.getClicksound())
		{
			soundPool.play(iSoundId, 1, 1, 0, 0, 1);
		}
		
		if(smartKeyApp.mInstance.getClickVibrate())
		{
			Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(1000);
		}
	}

	private OnActionCallBack onActionCallBack = new OnActionCallBack()
	{
		@Override
		public void ActionClick(int iClickState)
		{
			try
			{

				EventSt stEvent = smartKeyApp.mInstance.getEvent(iClickState);
				if (stEvent == null)
				{
					return;
				}

				if (stEvent.stPrograme == null)
				{
					return;
				}
				
				ClickSound();

				Intent intent = null;
				switch (stEvent.stPrograme.iId)
				{
				case smartKeyApp.APP_CASE_ID:
					// if(timeFloater ==null)
					// {
					// timeFloater = new TimeFloater(notifyService.this);
					// }
					// timeFloater.ShowHide(notifyService.this);
					if(Common.hasPermission(notifyService.this, Manifest.permission.RECORD_AUDIO)) {
						costomRecord.StartOrStopRecord();
					}else{
						smartKeyApp.mInstance.showToast(R.string.STR_CAMEAR_NOT_PERMISSION);
					}
					break;
				case smartKeyApp.APP_CAMERA_QUICK_ID:
					if(Common.hasPermission(notifyService.this, Manifest.permission.CAMERA)) {
						flashFloater.close();
						cameraFloater.ShowHide();
					}else{
						smartKeyApp.mInstance.showToast(R.string.STR_CAMEAR_NOT_PERMISSION);
					}
					break;
				case smartKeyApp.APP_FLASHLIGHT_ID:
					if(Common.hasPermission(notifyService.this, Manifest.permission.CAMERA)) {
						cameraFloater.close();
						flashFloater.ShowHide();
					}else{
						smartKeyApp.mInstance.showToast(R.string.STR_CAMEAR_NOT_PERMISSION);
					}

					break;
				case smartKeyApp.APP_PHONE_ID:
					intent = new Intent(Intent.ACTION_CALL_BUTTON);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					notifyService.this.startActivity(intent);
					break;
				case smartKeyApp.APP_CAMERA_ID:

					if(Common.hasPermission(notifyService.this, Manifest.permission.CAMERA))
					{
						flashFloater.close();
						cameraFloater.close();
						intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						notifyService.this.startActivity(intent);

//						intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						//notifyService.this.startActivity(intent);
//
//						String sName = new DateTime().getNow() + ".jpg";
//						File fileCamera = new File(getCameraDir(), sName);
//						Uri uriPic = null;
//						int currentapiVersion = android.os.Build.VERSION.SDK_INT;
//						if (currentapiVersion < 24) {
//							uriPic = Uri.fromFile(fileCamera);
//						} else {
//							ContentValues contentValues = new ContentValues(1);
//							contentValues.put(MediaStore.Images.Media.DATA, fileCamera.toString());
//							uriPic = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//						}
//
//						intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPic);// Uri.fromFile(fileCamera));
//						notifyService.this.startActivity(intent);
//
//						smartKeyApp.mInstance.showToast("picture path:"+fileCamera.toString());
					}
					else
					{
						smartKeyApp.mInstance.showToast(R.string.STR_CAMEAR_NOT_PERMISSION);
					}
					break;
				case smartKeyApp.APP_UNLOCK_ID:
					// PowerManager pm = (PowerManager)
					// getSystemService(Context.POWER_SERVICE);
					// if (!pm.isScreenOn())
					// {
					// intent = new Intent(notifyService.this,
					// unlockActivity.class);
					// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					// notifyService.this.startActivity(intent);
					// }
					intent = new Intent();
					intent.setAction(Intent.ACTION_MAIN);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.addCategory(Intent.CATEGORY_HOME);
					startActivity(intent);
					break;
				case smartKeyApp.APP_LOCK_ID:
					AdminManager adminManager = smartKeyApp.mInstance.getAdminManage();
					if (adminManager.getEnabled())
					{
						adminManager.LockScreen();
					}

					break;
				case smartKeyApp.APP_SWITCH_ID:
					intent = new Intent(notifyService.this, switchActivity.class);
					intent.putExtra("StartMode", switchActivity.START_MODE_ENTER);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
					break;
				case smartKeyApp.APP_MORE_ID:
					PackageManager packageManager = notifyService.this.getPackageManager();
					intent = packageManager.getLaunchIntentForPackage(stEvent.stPrograme.sPackName);
					notifyService.this.startActivity(intent);
					break;
				case smartKeyApp.APP_GAME_ID:
					DownLoadApk(stEvent.stPrograme.sApkUrl, stEvent.stPrograme.sAppName);
					break;
				default:
					break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	};

	private String getCameraDir()
	{
		String sDir=Environment.getExternalStorageDirectory().toString();
		File file  = new File(sDir,DIR_SMART_TOUCH);
		if(!file.exists())
		{
			file.mkdirs();
		}

		file = new File(file,"camera");
		if(!file.exists())
		{
			file.mkdirs();
		}

		return file.toString();
	}


	public interface OnActionCallBack
	{
		public void ActionClick(int iClickState);
	}

	public static void copyFile(File oldfile, File newFile) throws IOException
	{
		InputStream inStream = null;
		FileOutputStream outputStream = null;
		try
		{
			int byteread = 0;

			inStream = new FileInputStream(oldfile);
			outputStream = new FileOutputStream(newFile);

			byte[] buffer = new byte[1024];
			while ((byteread = inStream.read(buffer)) != -1)
			{
				outputStream.write(buffer, 0, byteread);
			}
		}
		finally
		{
			if (inStream != null)
			{
				inStream.close();
			}

			if (outputStream != null)
			{
				outputStream.close();
			}

		}

	}

	private DownLoadInterface DownLoadApkReponse = new DownLoadInterface()
	{
		@Override
		public void download_finish(int iReason, Object oInputParam, Object oOutParam)
		{
			if (oOutParam == null)
			{
				return;
			}

			if (iReason == HttpTask.DOWNLOAD_STATE_SUCC)
			{
				File fileOld = new File(HttpDeal.sTempFolder, String.valueOf(oOutParam));
				if (!fileOld.exists() || fileOld.length() < 1024)
				{
					return;
				}

				String sDir = Environment.getExternalStorageDirectory().toString();
				File file = new File(sDir, DIR_SMART_TOUCH);
				if (!file.exists())
				{
					file.mkdirs();
				}

				file = new File(file.toString(), sApkName);
				if (file.exists())
				{
					file.delete();
				}

				try
				{
					copyFile(fileOld, file);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}
	};

	public void DownLoadApkRequest(String sUrl)
	{
		sUrl = Common.GetFullUrl(this, sUrl + "?");
		HttpTask job = new HttpTask(HttpTask.DOWNLOAD_ID_DOWNLOADAPK, null, DownLoadApkReponse);
		job.JobGet(sUrl);
		stPayNet.mitJob(job);
	}

	public void CheckApkUpdateRequest()
	{
		String sUrl = "http://zzy.51gnss.cn/Interface/upgrade.ashx?";
		sUrl = Common.GetFullUrl(this, sUrl);
		HttpTask job = new HttpTask(HttpTask.DOWNLOAD_ID_CHECKAPK, null, CheckApkUpdateReponse);
		job.JobGet(sUrl);
		stPayNet.mitJob(job);
	}

	private float StrToFloat(String sName)
	{
		try
		{
			return Float.parseFloat(sName);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
			return 0.0f;
		}
	}

	private DownLoadInterface CheckApkUpdateReponse = new DownLoadInterface()
	{
		@Override
		public void download_finish(int iReason, Object oInputParam, Object oOutParam)
		{
			if (iReason == HttpTask.DOWNLOAD_STATE_SUCC)
			{

				try
				{
					@SuppressWarnings("unchecked")
					HashMap<String, Object> mp = (HashMap<String, Object>) oOutParam;
					PackageManager pManager = getPackageManager();
					String sPackName = getPackageName();
					String sSrcVer = pManager.getPackageInfo(sPackName, 0).versionName;

					String sUrl = (String) mp.get("url");
					String sVer = (String) mp.get("ver");
					if (sUrl == null || sVer == null || sUrl.length() < 1 || sVer.length() < 1)
					{
						return;
					}
					float fSrc = StrToFloat(sSrcVer);
					float fDes = StrToFloat(sVer);

					String sDir = Environment.getExternalStorageDirectory().toString();
					File file = new File(sDir, DIR_SMART_TOUCH);
					if (!file.exists())
					{
						file.mkdirs();
					}
					file = new File(file.toString(), sApkName);
					if (file.exists())
					{
						PackageInfo info = pManager.getPackageArchiveInfo(file.toString(),
						        PackageManager.GET_ACTIVITIES);
						float fLocal = StrToFloat(info.versionName);
						if (fLocal >= fDes)
						{
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							return;
						}
					}

					if (fDes > fSrc)
					{
						DownLoadApkRequest(sUrl);
					}
				}
				catch (NameNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (NumberFormatException e)
				{
					// TODO: handle exception
					e.printStackTrace();
				}
				catch (Exception e)
				{
					// TODO: handle exception
					e.printStackTrace();
				}

			}
		}
	};

	public void RegeditRequest(String sCode, OnRegeditState onRegeditState)
	{
		this.onRegeditState = onRegeditState;
		String sUrl = "http://zzy.51gnss.cn/interface/reg.ashx?code=" + sCode + "&";
		sUrl = Common.GetFullUrl(this, sUrl);
		HttpTask job = new HttpTask(HttpTask.DOWNLOAD_ID_REGEDIT, sCode, RegeditReponse);
		job.JobGet(sUrl);
		stPayNet.mitJob(job);
	}

	private String ReadFile(File file) throws IOException
	{

		FileInputStream in = null;
		try
		{
			in = new FileInputStream(file);
			int iLen = in.available();
			byte buffer[] = new byte[iLen];
			in.read(buffer);
			String sValue = new String(buffer, "UTF-8");
			return sValue;
		}
		finally
		{
			if (in != null)
			{
				in.close();
			}
		}
	}

	private DownLoadInterface RegeditReponse = new DownLoadInterface()
	{
		@Override
		public void download_finish(int iReason, Object oInputParam, Object oOutParam)
		{
			if (iReason == HttpTask.DOWNLOAD_STATE_SUCC)
			{
				if (oOutParam == null)
				{
					iReason = HttpTask.DOWNLOAD_STATE_DATA_ERROR;
					return;
				}

				File file = new File(HttpDeal.sTempFolder, String.valueOf(oOutParam));
				if (!file.exists())
				{
					iReason = HttpTask.DOWNLOAD_STATE_DATA_ERROR;
					return;
				}

				String sValue = null;
				try
				{
					sValue = ReadFile(file);
				}
				catch (IOException e)
				{
					iReason = HttpTask.DOWNLOAD_STATE_DATA_ERROR;
					e.printStackTrace();
					return;
				}

				if (sValue != null && sValue.equalsIgnoreCase("ok"))
				{
					if (oInputParam == null)
					{
						iReason = HttpTask.DOWNLOAD_STATE_DATA_ERROR;
						return;
					}
					if (!genCmp(String.valueOf(oInputParam)))
					{
						iReason = HttpTask.DOWNLOAD_STATE_DATA_ERROR;
					}
				}
				else
				{
					iReason = HttpTask.DOWNLOAD_STATE_DATA_ERROR;
				}
			}

			if (onRegeditState != null)
			{
				onRegeditState.getState(iReason);
			}

			onRegeditState = null;
		}
	};

	private boolean genCmp(String sCode)
	{
		// String sAndroidId =
		// Settings.System.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
		String sAndroidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		if (sAndroidId == null)
		{
			sAndroidId = "123456";
		}

		sCode = sCode.replace("-", "");
		if (sCode.length() != 16)
		{
			return false;
		}

		String sValue = CostomAes.encrypt(sCode, sAndroidId);
		if (sValue == null)
		{
			return false;
		}
		smartKeyApp.mInstance.SetConfig(Config.REGEDIT_CODE, sCode);
		smartKeyApp.mInstance.SetConfig(Config.REGEDIT_DATA, sValue);
		smartKeyApp.mInstance.setShowRegedit(false);
		return true;
	}

	public String getGameUpdateName(int iCount)
	{
		return "GameBox_" + String.valueOf(iCount) + ".dat";
	}

	public void ParseGameBox(String sDir, String sName)
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream(new File(sDir, sName));
			int length = in.available();
			byte[] buffer = new byte[length];
			in.read(buffer);

			String json = new String(buffer, "UTF-8");
			JSONObject obj = new JSONObject(json);
			JSONArray arrayGame = obj.getJSONArray("rows");
			if (arrayGame == null)
			{
				bCanUpdate = false;
				return;
			}

			GameBoxSt stGame;
			JSONObject jsonChild;

			int iCount = arrayGame.length();
			if (iCount < MAX_GAME_DOWNLOAD)
			{
				bCanUpdate = false;
			}

			for (int i = 0; i < arrayGame.length(); i++)
			{
				jsonChild = (JSONObject) arrayGame.opt(i);
				stGame = new GameBoxSt();
				stGame.iId = jsonChild.getInt("AppRID");
				stGame.sChName = jsonChild.getString("AppName");
				stGame.sEngName = jsonChild.getString("AppEnName");
				stGame.sVerison = jsonChild.getString("AppVerison");
				stGame.sPackageName = jsonChild.getString("AppPackage");
				stGame.sApkUrl = jsonChild.getString("ApkURL");
				stGame.sImgUrl = jsonChild.getString("AppImgURL");
				stGame.sCID = jsonChild.getString("CID");
				stGame.sType = jsonChild.getString("Type");
				stGame.iFree = jsonChild.getInt("Free");
				lsGameBox.add(stGame);
			}

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void mitJob(HttpTask job)
	{
		stPayNet.mitJob(job);
	}
	
	public interface OnGameRequestListen
	{
		public void GameReponse(int iCount, int iReason);
		public void GamePicReponse();
	}
	
	public void setOnGameRequestListen(OnGameRequestListen onGameRequestListen)
	{
		this.onGameRequestListen = onGameRequestListen;
	}

	private DownLoadInterface GetGameBoxReponse = new DownLoadInterface()
	{
		@Override
		public void download_finish(int iReason, Object oInputParam, Object oOutParam)
		{
			if (iReason == HttpTask.DOWNLOAD_STATE_SUCC)
			{
				ParseGameBox(jobGameBox.sDir, jobGameBox.sName);
			}
			jobGameBox = null;
			
			int iCount = 0;
			try
            {
				iCount = Integer.parseInt(String.valueOf(oInputParam));
            }
            catch (NumberFormatException  e)
            {
	           e.printStackTrace();
            }
			
			if(!bCanUpdateGame && onGameRequestListen !=null)
			{
				onGameRequestListen.GameReponse(iCount,iReason);
			}
			
			bCanUpdateGame = false;
		}
	};
	
	public void GetGameBoxRequestEx(int iCount,String sDir,String sName)
	{
		StringBuilder sbTmp = smartKeyApp.mInstance.getStringbuild();
		sbTmp.append("http://zzy.51gnss.cn/Interface/zzyapk.ashx?pageindex=").append(iCount).append("&pagesize=")
		        .append(MAX_GAME_DOWNLOAD).append("&");

		String sUrl = Common.GetFullUrl(this, sbTmp.toString());
		jobGameBox = new HttpTask(HttpTask.DOWNLOAD_ID_GAMEBOX, iCount, GetGameBoxReponse);
		jobGameBox.setDownInfo(sDir, sName);
		jobGameBox.JobGet(sUrl);
		stPayNet.mitJob(jobGameBox);
	}

	public boolean GetGameBoxRequest(int iCount)
	{
		if (iCount == 0)
		{
			bCanUpdate = true;
		}

		File fPath = new File(Environment.getExternalStorageDirectory(), notifyService.DIR_SMART_TOUCH);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}

		fPath = new File(fPath, notifyService.DIR_SMART_DATA);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}

		String sDir = fPath.toString();
		String sName = getGameUpdateName(iCount);
		fPath = new File(fPath, sName);
		if (fPath.exists())
		{
			ParseGameBox(sDir, sName);
			if(onGameRequestListen !=null)
			{
				onGameRequestListen.GameReponse(iCount,HttpTask.DOWNLOAD_STATE_SUCC);
			}
			return false;
		}
		else
		{
			GetGameBoxRequestEx(iCount,sDir,sName);
			return true;
		}
	}
	
	public String getGamePicPath()
	{
		File fPath = new File(Environment.getExternalStorageDirectory(), notifyService.DIR_SMART_TOUCH);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}

		fPath = new File(fPath, notifyService.DIR_SMART_PIC);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}
		return fPath.toString();
	}
	
	public static String getSetPath()
	{
		File fPath = new File(Environment.getExternalStorageDirectory(), notifyService.DIR_SMART_TOUCH);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}

		fPath = new File(fPath, notifyService.DIR_SMART_SET);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}
		return fPath.toString();
	}

	private DownLoadInterface GetGamePicReponse = new DownLoadInterface()
	{
		@Override
		public void download_finish(int iReason, Object oInputParam, Object oOutParam)
		{
			if (iReason == HttpTask.DOWNLOAD_STATE_SUCC)
			{
				GameBoxSt stBox = (GameBoxSt)oInputParam;;
				ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
				EventSt stEvent;
				for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
				{
					stEvent = lsEvent.get(i);
					if(stEvent==null || stEvent.stPrograme==null)
					{
						continue;
					}
					
					if(stEvent.stPrograme.iId == smartKeyApp.APP_GAME_ID && 
							stEvent.stPrograme.sApkPicUrl !=null && 
							stEvent.stPrograme.sApkPicUrl.equals(stBox.sImgUrl) )
					{
						File srcfile = new File(getGamePicPath(),stEvent.stPrograme.sApkPicName);
						File desFile = new File(getSetPath(),stEvent.stPrograme.sApkPicName);
						if(desFile.exists())
						{
							return;
						}
						
						try
                        {
	                        notifyService.copyFile(srcfile, desFile);
	                        Bitmap bmp =GameAdpter.LoadBitmapSample(desFile,128, 128);
	                        if(bmp==null)
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
				
				if(onGameRequestListen !=null)
				{
					onGameRequestListen.GamePicReponse();
				}
			}
		}
	};

	public void GetGamePicRequest(GameBoxSt stBox, String sDir, String sName)
	{
		String sUrl = stBox.sImgUrl;
		
		Common.LogEx("surl:"+sUrl+" sName:"+sName);
		HttpTask jobGameBox = new HttpTask(HttpTask.DOWNLOAD_ID_GAMEPIC, stBox, GetGamePicReponse);
		jobGameBox.setDownInfo(sDir, sName);
		jobGameBox.JobGet(sUrl);
		stPayNet.mitJob(jobGameBox);
	}

	public void DownLoadApk(String sUrl, String sName)
	{

		File fPath = new File(Environment.getExternalStorageDirectory(), notifyService.DIR_SMART_TOUCH);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}

		fPath = new File(fPath, notifyService.DIR_SMART_APK);
		if (!fPath.exists())
		{
			fPath.mkdirs();
		}
		
		fPath = new File(fPath,sName+".apk");
		if(fPath.exists())
		{
			Intent intentApk = new Intent(Intent.ACTION_VIEW);
			intentApk.setDataAndType(Uri.fromFile(fPath), "application/vnd.android.package-archive");
			intentApk.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intentApk);
			return;
		}
		
		Request request = new Request(Uri.parse(sUrl));

		request.addRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.addRequestHeader("Accept-Language", "en-us,en;q=0.5");
		request.addRequestHeader("Accept-Encoding", "gzip, deflate");
		request.addRequestHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
		request.addRequestHeader("Cache-Control", "max-age=0");

		request.setDestinationUri(Uri.fromFile(fPath));
		request.setTitle(sName); 
		request.setVisibleInDownloadsUi(true);
		
		lsDownLoad.add(String.valueOf(downloadManager.enqueue(request)));
	}
	
	BroadcastReceiver rbDownlaod = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if(action.equals(Intent.ACTION_PACKAGE_ADDED))
			{
				Common.LogEx("action pacjage add");
				String packageName = intent.getDataString();
				if(packageName==null || packageName.length()<7)
				{
					return;
				}
				
				packageName= packageName.substring(8);   //indexOf("package");
				ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
				EventSt stEvent;
				boolean bFound = false;
				for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
				{
					stEvent = lsEvent.get(i);
					if(stEvent==null || stEvent.stPrograme==null)
					{
						continue;
					}
					
					if(stEvent.stPrograme.iId == smartKeyApp.APP_GAME_ID && stEvent.stPrograme.sPackName!=null && stEvent.stPrograme.sPackName.equals(packageName))
					{
						bFound = true;
						stEvent.stPrograme.iId = smartKeyApp.APP_MORE_ID;
						PackageManager pm = smartKeyApp.mInstance.getApplicationContext().getPackageManager();
						try
                        {
							Drawable drawable = pm.getApplicationIcon(stEvent.stPrograme.sPackName);
							if (drawable != null)
							{
								stEvent.stPrograme.drIcon =drawable; 
							}
							else
							{
								stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
							}
                        }
                        catch (Exception e)
                        {
                        	stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
                        }
					}
				}
				
				if(bFound)
				{
					smartKeyApp.mInstance.SaveEvent();
				}
			}
			else if(action.equals(Intent.ACTION_PACKAGE_REMOVED))
			{
				Common.LogEx("action pacjage remove");
				String packageName = intent.getDataString();
				if(packageName==null || packageName.length()<7)
				{
					return;
				}
				
				packageName= packageName.substring(8);   //indexOf("package");
				
//				ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
//				EventSt stEvent;
//				boolean bFound = false;
//				for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
//				{
//					stEvent = lsEvent.get(i);
//					if(stEvent==null || stEvent.stPrograme==null)
//					{ 
//						continue;
//					}
//					
//					if(stEvent.stPrograme.sPackName.equals(packageName))
//					{
//						bFound = true;
//						stEvent.stPrograme = null;
//					}
//				}
//				
//				if(bFound)
//				{
//					smartKeyApp.mInstance.SaveEvent();
//				}
			}
			else if(action.equals(Intent.ACTION_PACKAGE_REPLACED))
			{
				Common.LogEx("action pacjage replaced");
			}
		}
	};


	public static class TopThread extends Thread
	{
		private ActivityManager mActivityManager;
		private Handler handler;
		private String sCurr;
		private String sSelfPageName;
		private DateTime dTime;
		private File fPath;
		private Context context;
		private long iStartTime;
		
		public TopThread(Handler handler, Context context)
		{
			mActivityManager = ((ActivityManager) context.getSystemService("activity"));
			this.handler = handler;
			sCurr = null;
			sSelfPageName = context.getPackageName();
			dTime = new DateTime();
			
			fPath = new File(Environment.getExternalStorageDirectory(), notifyService.DIR_SMART_TOUCH);
			if (!fPath.exists())
			{
				fPath.mkdirs();
			}

			fPath = new File(fPath, notifyService.DIR_SMART_DATA);
			if (!fPath.exists())
			{
				fPath.mkdirs();
			}
			
			this.context =context;
			
			
		}

		public static String getCurrentPkgName(Context context) {
			ActivityManager.RunningAppProcessInfo currentInfo = null;
			Field field = null;
			int START_TASK_TO_FRONT = 2;
			String pkgName = null;
			try {
				field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
			} catch (Exception e) {
				e.printStackTrace();
			}
			ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List appList = am.getRunningAppProcesses();
			List<ActivityManager.RunningAppProcessInfo> processes = ((ActivityManager) context.getSystemService(
					Context.ACTIVITY_SERVICE)).getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo app : processes) {
				if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					Integer state = null;
					try {
						state = field.getInt(app);
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (state != null && state == START_TASK_TO_FRONT) {
						currentInfo = app;
						break;
					}
				}
			}
			if (currentInfo != null) {
				pkgName = currentInfo.processName;
			}
			return pkgName;
		}

		@Override
		public void run()
		{
			while (true)
			{

				String packageName = null;
				ArrayList<String> lsLock = smartKeyApp.mInstance.getLockList();

				if (Build.VERSION.SDK_INT < 21) {
					ComponentName topActivity = ((ActivityManager.RunningTaskInfo) mActivityManager.getRunningTasks(1).get(0)).topActivity;
					packageName = topActivity.getPackageName();
					//String sClass = topActivity.getClassName();
				}
				else
				{
					//packageName = getCurrentPkgName(context);
					DetectService detectService = DetectService.getInstance();
					packageName = detectService.getForegroundPackage();
				}

				Common.LogEx("packageName:"+packageName);

				if(packageName !=null) {
					if (packageName.equals(sSelfPageName)) {
						Common.LogEx("TopThread UnlockGesturePasswordActivity");
					} else {
						if (sCurr == null || !sCurr.equals(packageName)) {
							String sTmp;
							for (int i = 0; i < lsLock.size(); i++) {
								sTmp = lsLock.get(i);
								if (sTmp.equals(packageName)) {
									sCurr = packageName;
									Message msg = new Message();
									msg.what = 1;
									msg.obj = packageName;
									handler.sendMessageDelayed(msg,500);
									break;
								}
							}
						}
						sCurr = packageName;
					}
				}

				long iTime  = dTime.getNow();
				if(Math.abs(smartKeyApp.mInstance.iUpdateGame-iTime)>24*60*60*1000 && !AppList.bGameIn)
				{
					String[] lsFile = fPath.list();
					if(lsFile!=null)
					{
						File file;
						for(int i=0;i<lsFile.length;i++)
						{
							file =  new File(fPath,lsFile[i]);
							if(file.isFile())
							{
								file.delete();
							}
						}
					}

					Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				}
				
//				long iEndTime = SystemClock.elapsedRealtime();
//				if(iEndTime-iStartTime>1*60*1000)
//				{
//					Intent intent = new Intent(context,StartService.class);
//					context.startService(intent);
//					iStartTime= iEndTime;
//				}
//
				try
				{
					sleep(400);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}
	
	private void StopNotify()
	{
		stopForeground(true);
	}
	
	
	@SuppressLint("NewApi")
    private void CreateNotity()
	{
		if(nm==null)
		{
			nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE); 
		}
		String sTitle = getText(R.string.app_name).toString();
		String sText = getText(R.string.STR_SERVICE_TEXT).toString();
		
		Intent intent = new Intent(this,MainActivity.class); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent piContent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT); 

		if(builder==null)
		{
			builder = new Builder(this);
			builder.setContentIntent(piContent);
			builder.setAutoCancel(false);
			builder.setWhen(System.currentTimeMillis());
			builder.setOngoing(true);
			builder.setContentTitle(sTitle);
			builder.setContentText(sText);
			builder.setSmallIcon(R.drawable.ic_launcher);
		}
		notification = builder.build();
		notification.defaults &= ~Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.icon = R.drawable.ic_launcher;
			
	        //nm.notify(nId,notification);

		
		startForeground(nId, notification);
	}

}

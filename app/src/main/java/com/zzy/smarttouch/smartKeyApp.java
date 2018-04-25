package com.zzy.smarttouch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import com.zzy.lock.AdminManager;
import com.zzy.privacy.LockPatternUtils;
import com.zzy.privacy.PrivacyAdpter.LockAppSt;
import com.zzy.sql.Config;


import android.R.integer;
import android.R.string;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import com.zzy.smarttouch.R;
import com.zzy.smarttouch.AppListAdpter.AppListInfo;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;

public class smartKeyApp extends Application
{
	public static final int CLICK_STATE_ONE=0;
	public static final int CLICK_STATE_TWO=1;
	public static final int CLICK_STATE_THRID=2;
	public static final int CLICK_STATE_FOUR=3;
	public static final int CLICK_STATE_MAX=4;
	
	public static final int APP_CASE_ID=0;
	public static final int APP_CAMERA_QUICK_ID=1;
	public static final int APP_FLASHLIGHT_ID=2;
	public static final int APP_PHONE_ID=3;
	public static final int APP_CAMERA_ID=4;
	public static final int APP_UNLOCK_ID=5;
	public static final int APP_LOCK_ID=6;
	public static final int APP_SWITCH_ID=7;
	public static final int APP_MORE_ID=8;
	public static final int APP_GAME_ID=9;
	
	public static final int FONT_TYPE_DIGIT=0;
	public static final int FONT_TYPE_ENGLISH=1;
	public static final int FONT_TYPE_CHINESE=2;
	private Typeface tfFont[];
	
	public static smartKeyApp mInstance;
	private Config dataConfig;
	private ArrayList<EventSt> lsEvent; 
	
	private String sSmartKeyDir=null;
	private String sPhotoDir=null;
	
	private ArrayList<AppListInfo> lsAppListInfos;
	private ArrayList<ProgrameSt> lsSwitch;
	
	private AdminManager adminManager;
	private Toast mToast;
	
	private RecordInfo recordInfo;
	private OnRecordStopListen onRecordStopListen;
	
	private boolean bShowRegedit;
	
	private LockPatternUtils mLockPatternUtils;
	private ArrayList<String> lsLock;
	private ArrayList<LockAppSt> lsLockApp;
	private StringBuilder sbTmp;
	public Drawable drApkDefault;
	public long iUpdateGame;
	
	private boolean bClickSound;
	private boolean bClickVibrate;
	private boolean bServiceEnable;
	
	private boolean bFirstUse;

	@Override
	public void onCreate()
	{
		super.onCreate();
		try
        {
			mInstance = this;
			drApkDefault = getResources().getDrawable(R.drawable.smartkey_app_more);
			OpenConfig(getApplicationContext().getFilesDir().toString());

			mLockPatternUtils = new LockPatternUtils(this);
			sbTmp = new StringBuilder();
        }
        catch (Exception e)
        {
	        // TODO: handle exception
        	e.printStackTrace();
        }
		

	}
	
	public StringBuilder getStringbuild()
	{
		sbTmp.setLength(0);
		return sbTmp;
	}
	
	public void OpenConfig(String sPath)
	{
		if (dataConfig != null && dataConfig.isOpen())
		{
			return;
		}
		
		dataConfig = new Config(sPath, Config.DBNAME, true);
		if (!dataConfig.IsExistTable(Config.CONFIG_TABLE))
		{
			HashMap<Object, Object> FieldsAndTypes = new HashMap<Object, Object>();
			FieldsAndTypes.put("name", "CHAR");
			FieldsAndTypes.put("value", "CHAR");
			dataConfig.CreateTable(Config.CONFIG_TABLE, FieldsAndTypes, null,false);
		}
		
		if (!dataConfig.IsExistTable(Config.EVENT_TABLE))
		{
			HashMap<Object, Object> FieldsAndTypes = new HashMap<Object, Object>();
			FieldsAndTypes.put("id", "INTEGER");
			FieldsAndTypes.put("state", "INTEGER");
			FieldsAndTypes.put("packName", "CHAR");
			FieldsAndTypes.put("apkname","CHAR");
			FieldsAndTypes.put("apkurl","CHAR");
			FieldsAndTypes.put("apkpicName","CHAR");
			FieldsAndTypes.put("apkpicUrl","CHAR");
			dataConfig.CreateTable(Config.EVENT_TABLE, FieldsAndTypes, null,false);
		}
		
		if(!dataConfig.IsExistTable(Config.SWITCH_TABLE))
		{
			HashMap<Object, Object> FieldsAndTypes = new HashMap<Object, Object>();
			FieldsAndTypes.put("id", "INTEGER");
			FieldsAndTypes.put("packName", "CHAR");
			dataConfig.CreateTable(Config.SWITCH_TABLE, FieldsAndTypes, null,false);
		}
		
		if(!dataConfig.IsExistTable(Config.LOCK_TABLE))
		{
			HashMap<Object, Object> FieldsAndTypes = new HashMap<Object, Object>();
			FieldsAndTypes.put("name", "CHAR");
			dataConfig.CreateTable(Config.LOCK_TABLE, FieldsAndTypes, null,false);
		}
		
		LoadEvent();
		
		setDir();
		
		FillAppListInfo();
		
		FillSwitchList();
		
		bShowRegedit =false;// RegeditCmp();
		
		if(lsLock ==null)
		{
			lsLock = new ArrayList<String>();
		}
		dataConfig.GetLockList(lsLock);
		
		
		
		String sValue = GetConfig(Config.UPDATE_GAME_TIME);
		if(sValue==null)
		{
			iUpdateGame = 0;
			
		}
		else
		{
			try
            {
				iUpdateGame = Long.parseLong(sValue);
            }
            catch (Exception e)
            {
            	DateTime dTime = new DateTime();
            	SaveUpadteGameTime(dTime.getNow());
            }
			
		}
		
		sValue = GetConfig(Config.DOUBLE_CLICK_SOUND);
		if(sValue==null)
		{
			bClickSound = true;
			SaveClickSound(true);
		}
		else
		{
			if(sValue.equals("0"))
			{
				bClickSound =false;
			}
			else
			{
				bClickSound =true;
			}
		}
		
		sValue = GetConfig(Config.DOUBLE_CLICK_VIBRATE);
		if(sValue==null)
		{
			bClickVibrate = true;
			SaveClickVibrate(true);
		}
		else
		{
			if(sValue.equals("0"))
			{
				bClickVibrate =false;
			}
			else
			{
				bClickVibrate =true;
			}
		}
		
		sValue = GetConfig(Config.SERVICE_ENABLE);
		if(sValue==null)
		{
			bServiceEnable = true;
			SaveClickVibrate(true);
		}
		else
		{
			if(sValue.equals("0"))
			{
				bServiceEnable =false;
			}
			else
			{
				bServiceEnable =true;
			}
		}
		
		sValue = GetConfig(Config.FIRST_USE);
		if(sValue==null)
		{
			bFirstUse = true;
		}
		else
		{
			bFirstUse = false;
		}
	}
	
	public void SaveNavState()
	{
		bFirstUse = false;
		SetConfig(Config.FIRST_USE, String.valueOf(1));
	}
	
	public boolean IsShowNav()
	{
		return bFirstUse;
	}
	
	public void SaveEnable(boolean bEnable)
	{
		int iValue = 0;
		if(bEnable)
		{
			iValue =1;
		}
		bServiceEnable = bEnable;
		SetConfig(Config.SERVICE_ENABLE, String.valueOf(iValue));
	}
	
	public boolean getEnable()
	{
		return bServiceEnable;
	}
	
	public void SaveClickSound(boolean bSound)
	{
		int iValue = 0;
		if(bSound)
		{
			iValue =1;
		}
		bClickSound = bSound;
		SetConfig(Config.DOUBLE_CLICK_SOUND, String.valueOf(iValue));
	}
	
	public boolean getClicksound()
	{
		return bClickSound;
	}
	
	public void SaveClickVibrate(boolean bVibrate)
	{
		int iValue = 0;
		if(bVibrate)
		{
			iValue =1;
		}
		bClickVibrate = bVibrate;
		SetConfig(Config.DOUBLE_CLICK_VIBRATE, String.valueOf(iValue));
	}
	
	public boolean getClickVibrate()
	{
		return bClickVibrate;
	}
	
	public void SaveUpadteGameTime(long iUpdateGame)
	{
		this.iUpdateGame = iUpdateGame;
		SetConfig(Config.UPDATE_GAME_TIME, String.valueOf(iUpdateGame));
	}
	
	public boolean RegeditCmp()
	{
		String sCode = GetConfig(Config.REGEDIT_CODE);
		if(sCode==null || sCode.length()<1)
		{
			return true;
		}
		String sData = GetConfig(Config.REGEDIT_DATA);
		if(sData==null || sData==null)
		{
			return true;
		}
		
		String sAndroidId = Settings.System.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
		if (sAndroidId == null)
		{
			sAndroidId = "123456";
		}
		String sValue = CostomAes.decrypt(sCode, sData);
		if(sAndroidId.equalsIgnoreCase(sValue))
		{
			return false;
		}
		return true;
	}
	
	public void LoadEvent()
	{
		
		if(lsEvent==null)
		{
			lsEvent = new ArrayList<EventSt>();
		}
		else
		{
			lsEvent.clear();
		}
		
		for(int i=0;i<CLICK_STATE_MAX;i++)
		{
			EventSt stEvent = new EventSt();
			stEvent.iClickState = i;
			stEvent.stPrograme = null;
			lsEvent.add(stEvent);
		}
		
		dataConfig.FillEventList(lsEvent);
	}
	
	public void SaveEvent()
	{
		if(lsEvent!=null)
		{
			dataConfig.saveEventList(lsEvent);
		}
	}
	
	public ArrayList<EventSt> getEventList()
	{
		return lsEvent;
	}
	
	public EventSt getEvent(int index)
	{
		if(index<0 || index>lsEvent.size()-1)
		{
			return null;
		}
		
		return lsEvent.get(index);
	}
	
	private void setDir()
	{
		String sDir=Environment.getExternalStorageDirectory().toString();
		File file  = new File(sDir,"SmartTouch");
		if(!file.exists())
		{
			file.mkdirs();
		}
		if(sSmartKeyDir==null)
		{
			sSmartKeyDir =file.toString();
		}
		
		file = new File(sSmartKeyDir,"photo");
		if(!file.exists())
		{
			file.mkdirs();
		}
		if(sPhotoDir==null)
		{
			sPhotoDir =file.toString();
		}
	}
	
	public String getDir()
	{
		return sSmartKeyDir;
	}
	
	public String getPhotoDir()
	{
		return sPhotoDir;
	}
	
	private void FillAppListInfo()
	{
		if(lsAppListInfos==null)
		{
			lsAppListInfos = new ArrayList<AppListInfo>();
		}
		
		AppListInfo stInfo = new AppListInfo();
		stInfo.id =APP_CASE_ID;
	    stInfo.iTitle = R.string.STR_APP_CASE;
	    stInfo.iIcon = R.drawable.smartkey_app_case;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_CAMERA_QUICK_ID;
	    stInfo.iTitle = R.string.STR_APP_CAMERA_QUICK;
	    stInfo.iIcon = R.drawable.smartkey_camera_quick;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_FLASHLIGHT_ID;
	    stInfo.iTitle = R.string.STR_APP_FLASHLIGHT;
	    stInfo.iIcon = R.drawable.smartkey_app_light;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_PHONE_ID;
	    stInfo.iTitle = R.string.STR_APP_PHONE;
	    stInfo.iIcon = R.drawable.smartkey_app_phone;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_CAMERA_ID;
	    stInfo.iTitle = R.string.STR_APP_CAMERA;
	    stInfo.iIcon = R.drawable.smartkey_camera;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_UNLOCK_ID;
	    stInfo.iTitle = R.string.STR_APP_UNLOCK;
	    stInfo.iIcon = R.drawable.smartkey_app_unlock;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_LOCK_ID;
	    stInfo.iTitle = R.string.STR_APP_LOCK;
	    stInfo.iIcon = R.drawable.smartkey_lock;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_SWITCH_ID;
	    stInfo.iTitle = R.string.STR_APP_SWITCH;
	    stInfo.iIcon = R.drawable.smartkey_app_quick;
	    lsAppListInfos.add(stInfo);
	    
	    stInfo = new AppListInfo();
		stInfo.id =APP_MORE_ID;
	    stInfo.iTitle = R.string.STR_APP_MORE;
	    stInfo.iIcon = R.drawable.smartkey_app_more;
	    lsAppListInfos.add(stInfo);
	}
	
	public ArrayList<AppListInfo> getAppList()
	{
		return lsAppListInfos;
	}
	
	public void SaveSwitchList()
	{
		if(lsSwitch!=null)
		{
			dataConfig.saveSwitchList(lsSwitch);
		}
	}
	
	public void FillSwitchList()
	{
		if(lsSwitch==null)
		{
			lsSwitch = new ArrayList<ProgrameSt>();
		}
		dataConfig.GetSwitchList(lsSwitch);
	}
	
	public ArrayList<ProgrameSt> getSwitchList()
	{
		return lsSwitch;
	}
	
	public InputStream GetInputStream(String sDir,String sName)
	{
		InputStream inStream = null;
		try
		{
			if (sDir.equals("AssetsDir"))
			{
				inStream = getApplicationContext().getApplicationContext().getAssets().open(sName.toLowerCase(Locale.US));

			} 
			else if (sDir.equals("ContentDir"))
			{
				inStream = getApplicationContext().getApplicationContext().getContentResolver().openInputStream(Uri.parse(sName));
			} 
			else
			{
				FileInputStream inputStream;
				inputStream = new FileInputStream(new File(sDir, sName));
				inStream = new BufferedInputStream(inputStream, 4096);
			}
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return inStream;
	}
	
	public void SetConfig(String sName,String sValue)
	{
		dataConfig.SetConfig(sName, sValue);
	}
	
	public String GetConfig(String sName)
	{
		return dataConfig.GetConfig(sName);
	}
	
	public AdminManager getAdminManage()
	{
		if(adminManager==null)
		{
			adminManager = new AdminManager(this);
		}
		return adminManager;
	}
	
	public void showToast(int id)
	{
		if (null == mToast)
		{
			mToast = Toast.makeText(getApplicationContext(),id, Toast.LENGTH_SHORT);
		} 
		else
		{
			mToast.setText(id);
		}

		mToast.show();
	}

	public void showToast(String str)
	{
		if (null == mToast)
		{
			mToast = Toast.makeText(getApplicationContext(),str, Toast.LENGTH_SHORT);
		}
		else
		{
			mToast.setText(str);
		}

		mToast.show();
	}
	
	public void setRecord(String sName,String sTime,boolean bRecord)
	{
		if(recordInfo==null)
		{
			recordInfo = new RecordInfo();
		}
		recordInfo.sName = sName;
		recordInfo.sTime = sTime;
		recordInfo.bRecord = bRecord;
	}
	
	public RecordInfo getRecord()
	{
		return recordInfo;
	}
	
	public void SetOnRecordStopListen(OnRecordStopListen onRecordStopListen)
	{
		this.onRecordStopListen = onRecordStopListen;
	}
	
	public void StopRecord()
	{
		if(onRecordStopListen !=null)
		{
			onRecordStopListen.onStop();
		}
	}
	
	public interface OnRecordStopListen
	{
		public void onStop();
	}
	
	public class RecordInfo
	{
		public String sName;
		public boolean bRecord;
		public String sTime;
	}
	
	public boolean isShowRegedit()
	{
		return bShowRegedit;
	}
	
	public void setShowRegedit(boolean bShow)
	{
		bShowRegedit = bShow;
	}
	
	public LockPatternUtils getLockPatternUtils()
	{
		return mLockPatternUtils;
	}
	
	public void SaveLock(LockAppSt lockAppSt)
	{
		dataConfig.saveLockList(lockAppSt);
	}
	
	public void DeleteLockByPaceName(String sName)
	{
		dataConfig.DelectLockByPackName(sName);
	}
	
	public ArrayList<String> getLockList()
	{
		return lsLock;
	}
	
	public ArrayList<LockAppSt> getLockAppList()
	{
		if(lsLockApp==null)
		{
			lsLockApp = new ArrayList<LockAppSt>();
		}
		return lsLockApp;
	}
}

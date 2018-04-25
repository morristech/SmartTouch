package com.zzy.smarttouch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import com.pocket.network.HttpDeal;
import com.pocket.network.HttpTask;
import com.zzy.appswitch.switchActivity;
import com.zzy.game.GameAdpter;
import com.zzy.game.GameAdpter.GameBoxSt;
import com.zzy.game.GameAdpter.OnDownLoadBmp;
import com.zzy.game.PullDownListView;
import com.zzy.game.PullDownListView.OnPullDownListener;
import com.zzy.lock.AdminManager;
import com.zzy.lock.AdminManager.AdminReceiver;
import com.zzy.sortlist.OpenAppMore;

import android.Manifest;
import android.R.color;
import android.R.integer;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AnalogClock;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zzy.smarttouch.R;
import com.zzy.smarttouch.AppListAdpter.AppListInfo;
import com.zzy.smarttouch.EntryActivity.MyOnPageChangeListener;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;
import com.zzy.smarttouch.notifyService.OnGameRequestListen;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class AppList extends BaseActivity implements OnClickListener,BaseActivity.onServiceConnection
{
	private final int TAB_1 = 0;
	private final int TAB_2 = 1;
	
	private ArrayList<AppListInfo> lsApp;
	private TextView tvAppTitle;
	private ImageView imgvAppBack;
	private int iClickMode;
	
	private ViewPager vpSetting;
	private GridView gvApp;
	
	
	private TextView tvTab1;
	private TextView tvTab2;
	private View vTab1;
	private View vTab2;
	private int iCurrTab= TAB_1;
	
	private LinearLayout lyNoNetWork;
	private LinearLayout lyBuild;
	private PullDownListView lvGame;
	private GameAdpter gameAdpter;
	private boolean bFrist;
	private boolean bLoginFirst;
	public static boolean bGameIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_app_choose);
		
	    Intent intent = getIntent();
	    if(intent==null)
	    {
	    	this.finish();
	    	return;
	    }
	    
		tvAppTitle = (TextView)findViewById(R.id.tvAppTitle);
		tvAppTitle.setOnClickListener(this);
		
		tvTab1 =(TextView)findViewById(R.id.tvTab1);
		tvTab1.setOnClickListener(this);
		tvTab2 =(TextView)findViewById(R.id.tvTab2);
		tvTab2.setOnClickListener(this);
		vTab1 =(View)findViewById(R.id.vTab1);
		vTab1.setOnClickListener(this);
		vTab2 =(View)findViewById(R.id.vTab2);
		vTab2.setOnClickListener(this);
		
	    int iMode = intent.getIntExtra("ClickMode", smartKeyApp.CLICK_STATE_ONE);
	    iClickMode= iMode;
	    switch (iMode) 
		{
			case smartKeyApp.CLICK_STATE_ONE:
				tvAppTitle.setText(R.string.STR_CLICK_ONE);
				break;
			case smartKeyApp.CLICK_STATE_TWO:
				tvAppTitle.setText(R.string.STR_CLICK_TWO);
				break;
			case smartKeyApp.CLICK_STATE_THRID:
				tvAppTitle.setText(R.string.STR_CLICK_THRID);
				break;
			case smartKeyApp.CLICK_STATE_FOUR:
				tvAppTitle.setText(R.string.STR_CLICK_FOUR);
				break;
			default:
				break;
		}
		
		imgvAppBack =(ImageView)findViewById(R.id.imgvAppBack);
		imgvAppBack.setOnClickListener(this);
		
		vpSetting= (ViewPager)findViewById(R.id.vpSetting);
		
		InitPager();
		setTab(TAB_1);
		
		setonServiceConnection(this);
		bLoginFirst = true;
	}
	
	private void InitPager()
	{
		ArrayList<View> listViews = new ArrayList<View>();
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
		View vPageOne=mLayoutInflater.inflate(R.layout.smartkey_setting_fun, vpSetting,false);
		View vPageTwo=mLayoutInflater.inflate(R.layout.smartkey_setting_game, vpSetting,false);
		listViews.add(vPageOne);
		listViews.add(vPageTwo);
		
		vpSetting.setAdapter(new ViewPageAdapter(listViews));
		vpSetting.setCurrentItem(0);
		vpSetting.setOnPageChangeListener(new MyOnPageChangeListener());
		
		gvApp = (GridView)vPageOne.findViewById(R.id.gvApp);
		AppListAdpter adpApp = new AppListAdpter(smartKeyApp.mInstance.getAppList(), this);
		gvApp.setAdapter(adpApp);
		
		gvApp.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
				if(mService==null)
				{
					return;
				}
				ArrayList<AppListInfo> lsAppList= smartKeyApp.mInstance.getAppList();
				AppListInfo stAppListInfo = lsAppList.get(position);
				
				Intent intent;
				if(stAppListInfo.id  ==smartKeyApp.APP_SWITCH_ID)
				{
					intent = new Intent(AppList.this,switchActivity.class);
					intent.putExtra("StartMode", switchActivity.START_MODE_ADD);
					startActivityForResult(intent,1);
				}
				else if(stAppListInfo.id == smartKeyApp.APP_LOCK_ID)
				{
	        		AdminManager adminManager = smartKeyApp.mInstance.getAdminManage();
	        		if(!adminManager.getEnabled())
	        		{
	        			String sTipString = AppList.this.getText(R.string.STR_APP_LOCK_TIP).toString();
	        			intent = adminManager.Enable(AppList.this,sTipString);
	        			startActivityForResult(intent, 2);
	        		}
	        		else 
	        		{
	        			ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
						EventSt stEvent = lsEvent.get(iClickMode);
						if (stEvent != null)
						{
							if(stEvent.stPrograme==null)
							{
								stEvent.stPrograme = new ProgrameSt();
							}
							stEvent.stPrograme.iId = stAppListInfo.id;
						}
						smartKeyApp.mInstance.SaveEvent();
						mService.ShowFloatByIndex(iClickMode);
						AppList.this.finish();
					}
				}
				else if(stAppListInfo.id == smartKeyApp.APP_MORE_ID)
				{
					intent = new Intent(AppList.this,OpenAppMore.class);
					intent.putExtra("PageMode",OpenAppMore.PAGE_APPLIST);
					intent.putExtra("ClickMode",iClickMode);
					startActivity(intent);
					AppList.this.finish();
				}
				else
				{

					switch (stAppListInfo.id)
					{
						case smartKeyApp.APP_CAMERA_ID:
						case smartKeyApp.APP_CAMERA_QUICK_ID:
						case smartKeyApp.APP_FLASHLIGHT_ID:
							AppListPermissionsDispatcher.showCameraWithCheck(AppList.this);
							break;
						case smartKeyApp.APP_CASE_ID:
							AppListPermissionsDispatcher.showRecordWithCheck(AppList.this);
							break;
					}

					ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
					EventSt stEvent = lsEvent.get(iClickMode);
					if (stEvent != null)
					{
						if(stEvent.stPrograme==null)
						{
							stEvent.stPrograme = new ProgrameSt();
						}
						stEvent.stPrograme.iId = stAppListInfo.id;
					}
					smartKeyApp.mInstance.SaveEvent();
					mService.ShowFloatByIndex(iClickMode);
					AppList.this.finish();
				}
            }
		});
		
		lyNoNetWork = (LinearLayout)vPageTwo.findViewById(R.id.lyNoNetWork);
		lyBuild = (LinearLayout)vPageTwo.findViewById(R.id.lyBuild);
		lvGame = (PullDownListView)vPageTwo.findViewById(R.id.lvGames);
		lvGame.show(null);
		lvGame.setOnPullDownListener(onPullDownListener);
		lvGame.setOnScrollListener(onScrollListener);
		
		lyNoNetWork.setVisibility(View.GONE);
		lyBuild.setVisibility(View.GONE);
		lvGame.setVisibility(View.GONE);
		lvGame.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
				GameBoxSt stGameBox = mService.lsGameBox.get(position);
				ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
				EventSt stEvent = lsEvent.get(iClickMode);
				if (stEvent != null)
				{
					if(stEvent.stPrograme==null)
					{
						stEvent.stPrograme = new ProgrameSt();
					}
					PackageManager pm = smartKeyApp.mInstance.getApplicationContext().getPackageManager();
					boolean bMore=false;
					try
                    {
						Drawable drawable = pm.getApplicationIcon(stGameBox.sPackageName);
						if (drawable != null)
						{
							stEvent.stPrograme.drIcon =drawable; 
							stEvent.stPrograme.iId = smartKeyApp.APP_MORE_ID;
							bMore =true;
						}
                    }
                    catch (Exception e)
                    {
                    }
					
					String sName = GameAdpter.getPicName(stGameBox);
					if(!bMore)
					{
						File srcfile = new File(mService.getGamePicPath(),sName);
						File desFile = new File(notifyService.getSetPath(),sName);

						try
                        {
							if(!desFile.exists())
							{
								notifyService.copyFile(srcfile, desFile);
							}
							
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
						
						stEvent.stPrograme.iId = smartKeyApp.APP_GAME_ID;
					}
					
					stEvent.stPrograme.sApkPicName = sName;
					stEvent.stPrograme.sApkUrl = stGameBox.sApkUrl;
					stEvent.stPrograme.sAppName = stGameBox.sChName;
					stEvent.stPrograme.sPackName = stGameBox.sPackageName;
					stEvent.stPrograme.sApkPicUrl = stGameBox.sImgUrl;
				}
				smartKeyApp.mInstance.SaveEvent();
				mService.ShowFloatByIndex(iClickMode);
				finish();
            }
		});
		
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener 
    {

        @Override
        public void onPageSelected(int arg0) 
        {
        	if(arg0==0)
        	{
        		setTab(TAB_1);
        	}
        	else 
        	{
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
	
	
	@Override
    public void onClick(View v)
    {
		switch(v.getId())
        {
			case R.id.tvAppTitle:
				finish();
				break;
			case R.id.imgvAppBack:
				finish();
				break;
			case R.id.tvTab1:
			case R.id.vTab1:
				setTab(TAB_1);
				vpSetting.setCurrentItem(TAB_1);
				break;
			case R.id.tvTab2:
			case R.id.vTab2:
				setTab(TAB_2);
				vpSetting.setCurrentItem(TAB_2);
				break;
			default:
				break;
		}
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(resultCode==Activity.RESULT_OK)
		{
			handler.sendEmptyMessageDelayed(requestCode,100);
		}
	}

	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {

			boolean bRet = true;
			switch(message.what)
			{
				case 1:
					if(mService!=null)
					{
						ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
						EventSt stEvent = lsEvent.get(iClickMode);
						if (stEvent != null)
						{
							if(stEvent.stPrograme==null)
							{
								stEvent.stPrograme = new ProgrameSt();
							}
							stEvent.stPrograme.iId = smartKeyApp.APP_SWITCH_ID;
						}
						smartKeyApp.mInstance.SaveEvent();
						mService.ShowFloatByIndex(iClickMode);
						AppList.this.finish();
					}
					else
					{
						handler.sendEmptyMessageDelayed(message.what,100);
					}
					break;
				case 2:
					if(mService!=null)
					{
						ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
						EventSt stEvent = lsEvent.get(iClickMode);
						if (stEvent != null)
						{
							if(stEvent.stPrograme==null)
							{
								stEvent.stPrograme = new ProgrameSt();
							}
							stEvent.stPrograme.iId = smartKeyApp.APP_LOCK_ID;
						}
						smartKeyApp.mInstance.SaveEvent();
						mService.ShowFloatByIndex(iClickMode);
						AppList.this.finish();

					}
					else
					{
						handler.sendEmptyMessageDelayed(message.what,100);
					}
					break;
				default:
					bRet = false;
			}
			return bRet;
		}
	});
	
	private void setTab(int index)
	{
		if(index<0 || index>1)
		{
			return;
		}
		
		int iSelColor = getResources().getColor(R.color.TAB_SELECT_COLOR);
		int iColor = getResources().getColor(color.black);
		switch(index)
		{
			case TAB_1:
				tvTab1.setTextColor(iSelColor);
				tvTab2.setTextColor(iColor);
				vTab1.setVisibility(View.VISIBLE);
				vTab2.setVisibility(View.INVISIBLE);
				break;
			case TAB_2:
				tvTab2.setTextColor(iSelColor);
				tvTab1.setTextColor(iColor);
				vTab2.setVisibility(View.VISIBLE);
				vTab1.setVisibility(View.INVISIBLE);
				EntryGameBox();
				break;
		}
		iCurrTab =index;
		
	}
	
	private void EntryGameBox()
	{
		lvGame.setVisibility(View.VISIBLE);
		if(gameAdpter ==null)
		{
			gameAdpter = new GameAdpter(this,mService.lsGameBox);
			gameAdpter.OnDownLoadBmp = onDownLoadBmp;
			gameAdpter.sbmpDir = mService.getGamePicPath();
			lvGame.setAdapter(gameAdpter);
			mService.setOnGameRequestListen(onGameRequestListen);
			bFrist = mService.GetGameBoxRequest(1);
			if(bFrist)
			{
				lvGame.setState(PullDownListView.STATE_LOADING_MORE);
			}
		}
		else 
		{
			gameAdpter.notifyDataSetChanged();
		}
	}
	
	private OnGameRequestListen onGameRequestListen = new OnGameRequestListen()
	{
		
		@Override
		public void GameReponse(int iCount,int iReason)
		{
			bFrist = false;
			PullDownFinish(iCount,iReason);
		}

		@Override
        public void GamePicReponse()
        {
			if(gameAdpter!=null && mService !=null && mService.lsGameBox.size()>0)
			{
				gameAdpter.notifyDataSetChanged();
			}
        }
	};
	
	private OnPullDownListener onPullDownListener = new OnPullDownListener()
	{
		
		@Override
		public void PullDown(Object object)
		{
			if(!mService.bCanUpdate)
			{
				return;
			}
			
			mService.setOnGameRequestListen(onGameRequestListen);
			mService.GetGameBoxRequest(mService.lsGameBox.size()+1);
		}
	};
	
	public void PullDownFinish(int iCount,int iReason)
	{
		if(iCount==1)
		{
			if(iReason== HttpTask.DOWNLOAD_STATE_SUCC)
			{
				if(mService.lsGameBox.size()<1)
				{
					lvGame.setVisibility(View.GONE);
					lyBuild.setVisibility(View.VISIBLE);
					lyNoNetWork.setVisibility(View.GONE);
					return;
				}
			}
			else 
			{
				lvGame.setVisibility(View.GONE);
				lyBuild.setVisibility(View.GONE);
				lyNoNetWork.setVisibility(View.VISIBLE);
				return;
			}
		}
		
		if(!mService.bCanUpdate)
		{
			lvGame.setState(PullDownListView.STATE_ALL_LOADED);
		}
		else 
		{
			lvGame.setState(PullDownListView.STATE_MORE);
		}
		gameAdpter.notifyDataSetChanged();

	}
	
	private OnScrollListener onScrollListener=  new OnScrollListener()
	{
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) 
			{
				if(view.getLastVisiblePosition() == view.getCount()-1)
				{
					onPullDownListener.PullDown(null);
				}
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
		{
			if(AppList.this.bFrist)
			{
				lvGame.setFooterViewVisible(View.VISIBLE);
				return;
			}
			
			if(totalItemCount>visibleItemCount)
			{
				lvGame.setFooterViewVisible(View.VISIBLE);
			}
			else
			{
				lvGame.setFooterViewVisible(View.GONE);
			}
		}
	};
	
	private OnDownLoadBmp onDownLoadBmp = new OnDownLoadBmp()
	{
		@Override
        public void Load(GameBoxSt stData, String sDir, String sName)
        {
	        // TODO Auto-generated method stub
	        mService.GetGamePicRequest(stData,sDir,sName);
        }

		@Override
        public void remove(Bitmap bmp)
        {
	        // TODO Auto-generated method stub
			if(bmp !=null && !bmp.isRecycled() )
			{
				bmp.recycle();
			}
	        
        }

		@Override
        public void removeAll(ArrayList<Bitmap> lsBmp)
        {
			for(int i=0;i<lsBmp.size();i++)
			{
				remove(lsBmp.get(i));
			}
        }
	};

	@Override
    public void callback()
    {
		if(bLoginFirst)
		{
			if(mService.lsGameBox!=null)
			{
				mService.lsGameBox.clear();
			}
			bLoginFirst = false;
		}
		
    }
	
	protected void onStop()
	{
		super.onStop();
		if(gameAdpter!=null)
		{
			lvGame.clearbitmap(gameAdpter.bmpDefault);
			gameAdpter.removeAllBmp();
		}
		bGameIn =false;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if(gameAdpter!=null)
		{
			gameAdpter.notifyDataSetChanged();
		}
	    bGameIn =true;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		// NOTE: delegate the permission handling to generated method

		AppListPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
	}

	@NeedsPermission(Manifest.permission.CAMERA)
	void showCamera() {
	}

	@OnPermissionDenied(Manifest.permission.CAMERA)
	void showDeniedForCamera() {
		smartKeyApp.mInstance.showToast(R.string.STR_CAMEAR_DENIED);
	}

	@OnNeverAskAgain(Manifest.permission.CAMERA)
	void showNeverAskForCamera() {
		smartKeyApp.mInstance.showToast(R.string.STR_CAMEAR_DENIED);
	}


	@NeedsPermission(Manifest.permission.RECORD_AUDIO)
	void showRecord() {
	}

	@OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
	void showDeniedForRecord() {
		smartKeyApp.mInstance.showToast(R.string.STR_RECORD_DENIED);
	}

	@OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
	void showNeverAskForRecord() {
		smartKeyApp.mInstance.showToast(R.string.STR_RECORD_DENIED);
	}

}

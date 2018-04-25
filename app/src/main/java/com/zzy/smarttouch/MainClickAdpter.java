package com.zzy.smarttouch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzy.game.GameAdpter;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.AppListAdpter.AppListInfo;

public class MainClickAdpter extends BaseAdapter
{
	ArrayList<EventSt> lsEvent = new ArrayList<EventSt>();
	LayoutInflater la;

	public MainClickAdpter(ArrayList<EventSt> lsEvent ,Context context)
	{
		this.lsEvent = lsEvent;
		la = LayoutInflater.from(context);
	}
    
    public void remove(int pos)
    {
    	this.lsEvent.remove(pos);
    }

	@Override
	public int getCount()
	{
		return lsEvent.size();
	}
	
	@Override
	public Object getItem(int position) 
	{
		return lsEvent.get(position);
	}
	
	@Override
	public long getItemId(int position) 
	{
		return position;
	}
	
	@Override
	public View getView(int position, View pConvertView, ViewGroup parent) 
	{
		if(pConvertView == null)
		{
			pConvertView=la.inflate(R.layout.smartkey_event_item, parent,false);
		}
		
		EventSt stEvent = lsEvent.get(position);
		if(stEvent==null)
		{
			return pConvertView;
		}

		ImageView imgvEventApp =(ImageView)pConvertView.findViewById(R.id.imgvMainLog);
		TextView tvEventAppName = (TextView)pConvertView.findViewById(R.id.tvMainTitle);
		
		TextView tvEventName = (TextView)pConvertView.findViewById(R.id.tvEventName);
		
		updateText(pConvertView.getContext(),tvEventAppName,imgvEventApp,stEvent);
		
		switch (stEvent.iClickState) 
		{
			case smartKeyApp.CLICK_STATE_ONE:
				tvEventName.setText(R.string.STR_ONE_KEY);
				break;
			case smartKeyApp.CLICK_STATE_TWO:
				tvEventName.setText(R.string.STR_TWO_KEY);
				break;
			case smartKeyApp.CLICK_STATE_THRID:
				tvEventName.setText(R.string.STR_THREE_KEY);
				break;
			case smartKeyApp.CLICK_STATE_FOUR:
				tvEventName.setText(R.string.STR_FOUR_KEY);
				break;
			default:
				break;
		}
		
		return pConvertView;
	}
	
	private void updateText(Context context,TextView tv, ImageView imgvEventApp, EventSt stEvent)
	{
		if (stEvent == null)
		{
			imgvEventApp.setImageResource(R.drawable.smartkey_app_default_icon);
			tv.setText("N/A");
			return;
		}

		ArrayList<AppListInfo> lsApplist = smartKeyApp.mInstance.getAppList();
		if (stEvent.stPrograme != null)
		{
			if (stEvent.stPrograme.iId == smartKeyApp.APP_MORE_ID)
			{
				if (stEvent.stPrograme.drIcon == null)
				{
					imgvEventApp.setImageResource(R.drawable.smartkey_app_default_icon);
					stEvent.stPrograme = null;
					tv.setText("N/A");
				}
				else
				{
					imgvEventApp.setImageDrawable(stEvent.stPrograme.drIcon);
					tv.setText(stEvent.stPrograme.sAppName);
				}
			}
			else if (stEvent.stPrograme.iId == smartKeyApp.APP_GAME_ID)
			{
				if(stEvent.stPrograme.drIcon == null || stEvent.stPrograme.drIcon == smartKeyApp.mInstance.drApkDefault) 
				{
					  if(stEvent.stPrograme.sApkPicName==null || stEvent.stPrograme.sApkPicName.length()<1)
					  {
						  stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
					  }
					  else 
					  {
						File desFile = new File(notifyService.getSetPath(), stEvent.stPrograme.sApkPicName);
						if(!desFile.exists())
						{
							stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
						}
						else
						{
							Bitmap bmp;
							try
							{
								bmp = GameAdpter.LoadBitmapSample(desFile, 128, 128);
								if (bmp == null)
								{
									stEvent.stPrograme.drIcon = smartKeyApp.mInstance.drApkDefault;
								}
								else
								{
									stEvent.stPrograme.drIcon = new BitmapDrawable(context.getResources(), bmp);
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
				    imgvEventApp.setImageDrawable(stEvent.stPrograme.drIcon);
					tv.setText(stEvent.stPrograme.sAppName);
				}
				else
				{
					imgvEventApp.setImageDrawable(stEvent.stPrograme.drIcon);
					tv.setText(stEvent.stPrograme.sAppName);
				}
			}
			else
			{

				AppListInfo stInfo = null;
				for (int i = 0; i < lsApplist.size(); i++)
				{
					stInfo = lsApplist.get(i);
					if (stInfo.id == stEvent.stPrograme.iId)
					{
						break;
					}
				}

				if (stInfo == null)
				{
					imgvEventApp.setImageResource(R.drawable.smartkey_app_default_icon);
					stEvent.stPrograme = null;
					tv.setText("N/A");
				}
				else
				{
					imgvEventApp.setImageResource(stInfo.iIcon);
					tv.setText(stInfo.iTitle);
				}
			}
		}
		else
		{
			imgvEventApp.setImageResource(R.drawable.smartkey_app_default_icon);
			tv.setText("N/A");
		}
	}

	public static class ProgrameSt
	{
	   public int iId;
	   public Drawable drIcon;
	   public String sAppName;
	   public String sPackName;
	   public String sApkUrl;
	   public String sApkPicName;
	   public String sApkPicUrl;
	   public String sortLetters; // 显示数据拼音的首字母
	}
	
	public static class EventSt
	{
		public int iClickState;
		public ProgrameSt stPrograme;
		public Object object;
	}
}

package com.zzy.smarttouch;

import java.util.ArrayList;


import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;

public class AppListAdpter extends BaseAdapter
{
	ArrayList<AppListInfo> lsApp = new ArrayList<AppListInfo>();
	LayoutInflater la;

	public AppListAdpter(ArrayList<AppListInfo> lsApp ,Context context)
	{
		this.lsApp = lsApp;
		la = LayoutInflater.from(context);
	}
    
    public void remove(int pos) 
    {
    	this.lsApp.remove(pos);
    }

	@Override
	public int getCount()
	{
		return lsApp.size();
	}
	
	@Override
	public Object getItem(int position) 
	{
		return lsApp.get(position);
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
			pConvertView=la.inflate(R.layout.smartkey_app_item, null);
		}
		
		AppListInfo stInfo = lsApp.get(position);
		if(stInfo==null)
		{
			return pConvertView;
		}
		
		ImageView imgvAppItem =(ImageView)pConvertView.findViewById(R.id.imgvAppItem);
		imgvAppItem.setImageResource(stInfo.iIcon);
		
		TextView tvAppItem = (TextView)pConvertView.findViewById(R.id.tvAppItem);
		tvAppItem.setText( pConvertView.getContext().getText(stInfo.iTitle));
		
		return pConvertView;
	}


	public static class AppListInfo
	{
		public int id;
		public int iTitle;
		public int iIcon;
	}
}

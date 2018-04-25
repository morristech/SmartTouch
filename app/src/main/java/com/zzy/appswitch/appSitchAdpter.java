package com.zzy.appswitch;

import java.util.ArrayList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;


public class appSitchAdpter extends BaseAdapter
{
	ArrayList<ProgrameSt> lsApp = new ArrayList<ProgrameSt>();
	LayoutInflater la;

	public appSitchAdpter(ArrayList<ProgrameSt> lsApp ,Context context)
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
			pConvertView=la.inflate(R.layout.smartkey_switch_item, parent,false);
		}
		
		ProgrameSt stInfo = lsApp.get(position);
		if(stInfo==null)
		{
			return pConvertView;
		}
		
		
		ImageView imgvAppItem =(ImageView)pConvertView.findViewById(R.id.imgvSwitch);
		
		if(stInfo.drIcon!=null)
		{
			imgvAppItem.setImageDrawable(stInfo.drIcon);
		}
		else 
		{
			imgvAppItem.setImageResource(R.drawable.smartkey_app_default_icon);
		}	
		return pConvertView;
	}
}
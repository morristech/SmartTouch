package com.zzy.record;

import java.io.File;
import java.util.ArrayList;


import android.R.integer;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.AppListAdpter.AppListInfo;

public class RecordListAdpter extends BaseAdapter implements OnClickListener
{
	private ArrayList<String> lsRecord = new ArrayList<String>();
	private LayoutInflater la;
	private String sParentDir;

	public RecordListAdpter(ArrayList<String> lsRecord ,Context context,String sParentDir)
	{
		this.lsRecord = lsRecord;
		this.la = LayoutInflater.from(context);
		this.sParentDir = sParentDir;
	}
	
	@Override
	public int getCount()
	{
		return lsRecord.size();
	}
	
	@Override
	public String getItem(int position) 
	{
		return lsRecord.get(position);
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
			pConvertView=la.inflate(R.layout.smartkey_record_list, parent,false);
		}
		
		String stRecord = lsRecord.get(position);
		if(stRecord==null)
		{
			return pConvertView;
		}
		
		TextView textView = (TextView)pConvertView.findViewById(R.id.tvRecordItem);
		textView.setText(stRecord);
		
		ImageView imgvDelete = (ImageView)pConvertView.findViewById(R.id.imgvDelete);
		imgvDelete.setOnClickListener(this);
		imgvDelete.setTag(position);
		
		
		return pConvertView;
	}

	@Override
    public void onClick(View view)
    {
	   if(view.getId()==R.id.imgvDelete)
	   {
		  	try
	        {
		  		Object object = view.getTag();
		  		if(object==null)
		  		{
		  			return;
		  		}
		        int iPos = Integer.parseInt(String.valueOf(object));
		        
		        if(iPos<0 || iPos>lsRecord.size()-1)
		        {
		        	return;
		        }
		     
				String stRecord = lsRecord.get(iPos);
				if(stRecord!=null)
				{
					File file  =  new File(sParentDir,stRecord);
					file.delete();
				}
				
				lsRecord.remove(iPos);
				notifyDataSetChanged();
	        }
	        catch (Exception e)
	        {
		        e.printStackTrace();
	        }
	   }
	    
    }
	
	
}

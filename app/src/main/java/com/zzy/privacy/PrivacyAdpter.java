package com.zzy.privacy;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.smartKeyApp;

public class PrivacyAdpter extends BaseAdapter
{
	private List<LockAppSt> list = null;
	private LayoutInflater la;

	public PrivacyAdpter(Context mContext, List<LockAppSt> list)
	{
		this.list = list;
		la = LayoutInflater.from(mContext);
		
		
	}

	public int getCount()
	{
		return this.list.size();
	}

	public LockAppSt getItem(int position)
	{
		return list.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View pConvertView, ViewGroup parent)
	{
		if (pConvertView == null)
		{
			pConvertView =la.inflate(R.layout.smartkey_privacty_setting_item, null);
		}
		
		LockAppSt stPrograme = list.get(position);
		if(stPrograme==null)
		{
			return pConvertView;
		}
		
		TextView tvPrivacyItem= (TextView) pConvertView.findViewById(R.id.tvPrivacyItem);
		TextView tvPrivacyLetter = (TextView) pConvertView.findViewById(R.id.tvPrivacyLetter);
		ImageView imgvPrivacyItem = (ImageView) pConvertView.findViewById(R.id.imgvPrivacyItem);
		
		ImageView imgvPrivacyLock = (ImageView) pConvertView.findViewById(R.id.imgvPrivacyLock);
		if(stPrograme.bLock)
		{
			imgvPrivacyLock.setImageResource(R.drawable.smartkey_btn_on);
		}
		else 
		{
			imgvPrivacyLock.setImageResource(R.drawable.smartkey_btn_off);
		}
		imgvPrivacyLock.setTag(position);
		imgvPrivacyLock.setOnClickListener(onClickListener);
		
		char iSection = stPrograme.sortLetters.charAt(0);
		boolean bVisible=false;
		if(position==0)
		{
			bVisible = true;
		}
		else 
		{
			char iPre = list.get(position-1).sortLetters.charAt(0);
			if(iPre !=iSection)
			{
				bVisible = true;
			}
		}

		if (bVisible)
		{
			tvPrivacyLetter.setVisibility(View.VISIBLE);
			tvPrivacyLetter.setText(stPrograme.sortLetters);
		}
		else
		{
			tvPrivacyLetter.setVisibility(View.GONE);
		}

		tvPrivacyItem.setText(stPrograme.sAppName);
		imgvPrivacyItem.setImageDrawable(stPrograme.drIcon);

		return pConvertView;

	}
	
	private OnClickListener onClickListener = new OnClickListener()
	{
		
		@Override
		public void onClick(View v)
		{
			try
            {
				int iPos = Integer.parseInt(String.valueOf(v.getTag()));
				if(list==null)
				{
					return;
				}
				
				if(!(v instanceof ImageView))
				{
					return;
				}
				
				ImageView imgvTmp = (ImageView)v;
				
				
				ArrayList<LockAppSt> lsLockApp = smartKeyApp.mInstance.getLockAppList();
				LockAppSt lockAppSt = lsLockApp.get(iPos);
				
				ArrayList<String> lsLock = smartKeyApp.mInstance.getLockList();
				
				if(lockAppSt.bLock)
				{
					lockAppSt.bLock = false;
					imgvTmp.setImageResource(R.drawable.smartkey_btn_off);
					smartKeyApp.mInstance.DeleteLockByPaceName(lockAppSt.sPackName);
					for(int i=0;i<lsLock.size();i++)
					{
						if(lockAppSt.sPackName.equals(lsLock.get(i)))
						{
							lsLock.remove(i);
							break;
						}
					}
				}
				else 
				{
					lockAppSt.bLock = true;
					imgvTmp.setImageResource(R.drawable.smartkey_btn_on);
					smartKeyApp.mInstance.SaveLock(lockAppSt);
					lsLock.add(lockAppSt.sPackName);
				}
				
				
            }
			catch (NumberFormatException e) 
			{
				e.printStackTrace();
			}
			
		}
	};
	
	public int getFirstPosition(int section)
	{
		int iCount = getCount();
		for (int i = 0; i < iCount; i++)
		{
			String sortStr = getItem(i).sortLetters;
			char firstChar = sortStr.charAt(0);
			if (firstChar == section)
			{
				return i;
			}
		}

		return -1;
	}
	
	public static class LockAppSt
	{
	   public Drawable drIcon;
	   public String sAppName;
	   public String sPackName;
	   public String sortLetters;
	   public boolean bLock;
	}
}

package com.zzy.sortlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.zzy.sortlist.SideBar.OnTouchingLetterChangedListener;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zzy.smarttouch.BaseActivity;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.AppList;
import com.zzy.smarttouch.EntryActivity;
import com.zzy.smarttouch.smartKeyApp;
import com.zzy.smarttouch.MainClickAdpter.EventSt;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;

public class OpenAppMore extends BaseActivity
{
	public static final int PAGE_SWITCH = 0;
	public static final int PAGE_APPLIST = 1;
	
	private ArrayList<ProgrameSt> lsApp;
	
	private CharacterParser characterParser;
	private PinyinComparator pinyinComparator;
	private SortAdapter adpSort;
	private ListView lvAppMore;
	private int iClickMode;
	private int iPage=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_app_more);
		
	    Intent intent = getIntent();
	    if(intent==null)
	    {
	    	this.finish();
	    	return;
	    }
	    
	    ImageView imgvMoreBack =(ImageView)findViewById(R.id.imgvMoreBack);
	    imgvMoreBack.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				OpenAppMore.this.finish();
			}
		});
	    
	    TextView tvMoreTitle = (TextView)findViewById(R.id.tvMoreTitle);
	    tvMoreTitle.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				OpenAppMore.this.finish();
			}
		});
	    
	    iPage = intent.getIntExtra("PageMode", PAGE_APPLIST);
		iClickMode= intent.getIntExtra("ClickMode",0);

    	characterParser = CharacterParser.getInstance();
    	pinyinComparator = new PinyinComparator();
    	GetPackagesList(this);
		
		ClearEditText etAppMoreSearch=(ClearEditText)findViewById(R.id.etAppMoreSearch);
		lvAppMore =(ListView)findViewById(R.id.lvAppMore);
		TextView tvAppMoreHead =(TextView)findViewById(R.id.tvAppMoreHead);
		SideBar sbAppMore = (SideBar)findViewById(R.id.sbAppMore);
		sbAppMore.setTextView(tvAppMoreHead);
		
		sbAppMore.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener()
		{
			
			@Override
			public void onTouchingLetterChanged(String s)
			{
				int position = adpSort.getPositionForSection(s.charAt(0));
				if (position != -1)
				{
					lvAppMore.setSelection(position);
				}
			}
		});
		
		lvAppMore.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				if(adpSort!=null)
				{
					if(iPage ==PAGE_SWITCH)
					{
						ProgrameSt stPrograme = adpSort.getItem(position);
						ArrayList<ProgrameSt> lsPrograme = smartKeyApp.mInstance.getSwitchList();
						ProgrameSt stEvent = lsPrograme.get(iClickMode);
						if (stEvent != null)
						{
							stEvent.iId = smartKeyApp.APP_MORE_ID;
							stEvent.drIcon = stPrograme.drIcon;
							stEvent.sAppName = stPrograme.sAppName;
							stEvent.sPackName = stPrograme.sPackName;
							stEvent.sortLetters = stPrograme.sortLetters;
						}
						OpenAppMore.this.finish();
					}
					else
					{
						ProgrameSt stPrograme = adpSort.getItem(position);
						ArrayList<EventSt> lsEvent = smartKeyApp.mInstance.getEventList();
						EventSt stEvent = lsEvent.get(iClickMode);
						if (stEvent != null)
						{
							if(stEvent.stPrograme==null)
							{
								stEvent.stPrograme = new ProgrameSt();
							}
							stEvent.stPrograme.iId = smartKeyApp.APP_MORE_ID;
							stEvent.stPrograme.drIcon = stPrograme.drIcon;
							stEvent.stPrograme.sAppName = stPrograme.sAppName;
							stEvent.stPrograme.sPackName = stPrograme.sPackName;
							stEvent.stPrograme.sortLetters = stPrograme.sortLetters;
						}
						smartKeyApp.mInstance.SaveEvent();
						mService.ShowFloatByIndex(iClickMode);
						OpenAppMore.this.finish();
					}
				}
			}
		});
		
		etAppMoreSearch.addTextChangedListener(new TextWatcher()
		{

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void afterTextChanged(Editable s)
			{
			}
		});
		
		Collections.sort(lsApp, pinyinComparator);
		adpSort = new SortAdapter(this, lsApp);
		lvAppMore.setAdapter(adpSort);
	}
	
    public void GetPackagesList(Context context)
    {
    	lsApp = new ArrayList<ProgrameSt>();
    	//List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
    	
    	 Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);  
         mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);  
         List<ResolveInfo> packages = getPackageManager().queryIntentActivities(mainIntent, 0);

    	for(int i=0;i<packages.size();i++)
    	{ 
    		ResolveInfo stPackageInfo = packages.get(i); 
	    	//if((stPackageInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0)
	    	//{
    		
		    	ProgrameSt tmpInfo =new ProgrameSt(); 
		    	tmpInfo.iId = smartKeyApp.APP_MORE_ID;
		    	tmpInfo.sAppName = stPackageInfo.activityInfo.loadLabel(getPackageManager()).toString(); 
		    	tmpInfo.sPackName = stPackageInfo.activityInfo.packageName; 
		    	tmpInfo.drIcon = stPackageInfo.activityInfo.loadIcon(getPackageManager());
		    	
		    	String pinyin = characterParser.getSelling(tmpInfo.sAppName);
				String sortString = pinyin.substring(0, 1).toUpperCase(Locale.US);
				if(sortString.matches("[A-Z]"))
				{
					tmpInfo.sortLetters = sortString.toUpperCase(Locale.US);
				}
				else
				{
					tmpInfo.sortLetters = "#";
				}
				lsApp.add(tmpInfo);
	    	//}
    	}
    }
    
	private void filterData(String filterStr)
	{
		List<ProgrameSt> filterDateList = new ArrayList<ProgrameSt>();

		if (TextUtils.isEmpty(filterStr))
		{
			filterDateList = lsApp;
		}
		else
		{
			filterDateList.clear();
			for (ProgrameSt stPrograme : lsApp)
			{
				String name = stPrograme.sAppName;
				if (name.indexOf(filterStr.toString()) != -1|| characterParser.getSelling(name).startsWith(filterStr.toString()))
				{
					filterDateList.add(stPrograme);
				}
			}
		}

		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adpSort.updateListView(filterDateList);
	}
    
}

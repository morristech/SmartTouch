package com.zzy.record;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import com.zzy.smarttouch.R;
import com.zzy.smarttouch.notifyService;
import com.zzy.smarttouch.smartKeyApp;
import com.zzy.smarttouch.smartKeyApp.RecordInfo;

import android.app.Activity;
import android.content.Intent;
import android.drm.DrmStore.RightsStatus;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class RecordWin extends Activity implements OnClickListener
{
	private TextView tvRecordWinTime;
	private ImageView imgvRwcordWinCircle;
	private TextView btnRecordWinAction;
	private LinearLayout lyRecord;
	private LinearLayout lyRecordList;
	private ListView lvRecord;
	private Handler handler;
	private File fDirPath;
	private ArrayList<String> lsRecord;
	private RecordListAdpter recordListAdpter;
	private ImageView imgvMainMenu;
	private ImageView imgvMainLog;
	private ImageView imgvRecordBack;
	
	public static String sRecordList[];
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_record_win);
		
		RecordInfo recordInfo = smartKeyApp.mInstance.getRecord();
		if(recordInfo==null)
		{
			this.finish();
			return;
		}
		
		imgvMainMenu=(ImageView)findViewById(R.id.imgvMainMenu);
		imgvMainMenu.setOnClickListener(this);
		
		imgvRecordBack=(ImageView)findViewById(R.id.imgvRecordBack);
		imgvRecordBack.setOnClickListener(this);
		
		imgvMainLog=(ImageView)findViewById(R.id.imgvMainLog);
		imgvMainLog.setOnClickListener(this);
		
		tvRecordWinTime =(TextView)findViewById(R.id.tvRecordWinTime);
		tvRecordWinTime.setText(recordInfo.sTime);
		
		imgvRwcordWinCircle =(ImageView)findViewById(R.id.imgvRwcordWinCircle);
		TextView tvRecordWinName =(TextView)findViewById(R.id.tvRecordWinName);
		tvRecordWinName.setText(recordInfo.sName);
		
		btnRecordWinAction =(Button)findViewById(R.id.btnRecordWinAction);
		btnRecordWinAction.setOnClickListener(this);
		if(recordInfo.bRecord)
		{
			btnRecordWinAction.setText(R.string.STR_STOP);
		}
		else 
		{
			btnRecordWinAction.setText(R.string.STR_SHARE);
			smartKeyApp.mInstance.StopRecord();
		}
		
		lyRecord = (LinearLayout)findViewById(R.id.lyRecord);
		lyRecordList = (LinearLayout)findViewById(R.id.lyRecordList);
		
		setRecordVisible(true);
		lvRecord = (ListView)findViewById(R.id.lvRecord);
		lvRecord.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				String sName = recordListAdpter.getItem(position);
				File file = new File(fDirPath,sName);
				intent.setDataAndType(Uri.fromFile(file), "audio/*");
				startActivity(intent);
            }
		});
		
		fDirPath = new File(Environment.getExternalStorageDirectory(),notifyService.DIR_SMART_TOUCH);
		fDirPath = new File(fDirPath,notifyService.DIR_SMART_RECORD);
		
		handler = new Handler(new Handler.Callback()
		{
			@Override
			public boolean handleMessage(Message msg)
			{
				if(msg.what==1)
				{
					lsRecord.clear();
					if(sRecordList!=null)
					{
						for(int i=0;i<sRecordList.length;i++)
						{
							lsRecord.add(sRecordList[i]);
						}
					}
					
					if(recordListAdpter==null)
					{
						recordListAdpter = new RecordListAdpter(lsRecord, RecordWin.this,RecordWin.this.fDirPath.toString());
						lvRecord.setAdapter(recordListAdpter);
					}
					else 
					{
						
						recordListAdpter.notifyDataSetChanged();
					}
				
					return true;
				}
				return false;
			}
		});
		lsRecord = new ArrayList<String>();
	}
	
	private Runnable runnable = new Runnable()
	{
		
		@Override
		public void run()
		{
			RecordInfo recordInfo = smartKeyApp.mInstance.getRecord();
			if(recordInfo==null)
			{
				RecordWin.this.finish();
				return;
			}
			
			tvRecordWinTime.setText(recordInfo.sTime);
			
			if(recordInfo.bRecord)
			{
				btnRecordWinAction.setText(R.string.STR_STOP);
				handler.postDelayed(this, 500);
			}
			else 
			{
				btnRecordWinAction.setText(R.string.STR_SHARE);
			}
		}
	};
	
	@Override 
	protected void onResume()
	{
		super.onResume();
		handler.postDelayed(runnable, 500);
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		handler.removeCallbacks(runnable);
	}
	
	private String getFileExtensionFromUrl(String Url)
	{
		MimeTypeMap mime = MimeTypeMap.getSingleton();
		String ext = MimeTypeMap.getFileExtensionFromUrl(Url);
		String type = mime.getMimeTypeFromExtension(ext);
		return type;
	}
	
	private void setRecordVisible(boolean bVisible)
	{
		if(bVisible)
		{
			lyRecord.setVisibility(View.VISIBLE);
			lyRecordList.setVisibility(View.GONE);
			imgvMainMenu.setVisibility(View.VISIBLE);
			imgvRecordBack.setVisibility(View.GONE);
		}
		else
		{
			lyRecord.setVisibility(View.GONE);
			lyRecordList.setVisibility(View.VISIBLE);
			imgvMainMenu.setVisibility(View.GONE);
			imgvRecordBack.setVisibility(View.VISIBLE);
		}
	}

	@Override
    public void onClick(View v)
    {
		switch (v.getId())
		{
		case R.id.btnRecordWinAction:

			RecordInfo recordInfo = smartKeyApp.mInstance.getRecord();
			if(recordInfo==null)
			{
				return;
			}
			
			if(recordInfo.bRecord)
			{
				smartKeyApp.mInstance.StopRecord();
				handler.removeCallbacks(runnable);
				btnRecordWinAction.setText(R.string.STR_SHARE);
			}
			else 
			{
				File fPath = new File(fDirPath,recordInfo.sName);
				Intent intent=new Intent(Intent.ACTION_SEND);   
		        intent.setType(getFileExtensionFromUrl(fPath.toString()));   
		        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fPath));
		        intent.putExtra(Intent.EXTRA_TEXT,getText(R.string.STR_SHARE));
		        startActivity(Intent.createChooser(intent,getText(R.string.STR_SHARE)));
			}
			break;
		case R.id.imgvMainMenu:
			setRecordVisible(false);
			new ScanThread(fDirPath, handler).start();
			break;
		case R.id.imgvRecordBack:
			setRecordVisible(true);
			break;
		case R.id.imgvMainLog:
			if(lyRecordList.getVisibility()==View.VISIBLE)
			{
				setRecordVisible(true);
			}
			break;
		default:
			break;
		}
    }
	
	public static class ScanThread extends Thread
	{
		File fileDir;
		Handler handler;
		public ScanThread(File fileDir,Handler handler)
		{
			this.fileDir = fileDir;
			this.handler = handler;
		}
		
		@Override
		public void run()
		{
			sRecordList = fileDir.list();
			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);
		}
	}
}

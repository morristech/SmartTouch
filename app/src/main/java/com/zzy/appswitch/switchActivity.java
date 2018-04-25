package com.zzy.appswitch;

import java.util.ArrayList;

import org.w3c.dom.ls.LSException;

import com.zzy.sortlist.OpenAppMore;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.smartKeyApp;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;

public class switchActivity extends Activity
{
	public static final int START_MODE_ADD=0;
	public static final int START_MODE_ENTER=1;
	private int iStartMode=START_MODE_ADD;
	
	private GridView gvSwitch;
	private appSitchAdpter adpApp;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_switch);
		
		Intent intent = getIntent();
		if(intent!=null && intent.hasExtra("StartMode"))
		{
			iStartMode =intent.getIntExtra("StartMode",START_MODE_ADD);
		}
		
		LinearLayout lySwitchButton=(LinearLayout)findViewById(R.id.lySwitchButton);
		Button btnSwitchOK =(Button)findViewById(R.id.btnSwitchOK);
		btnSwitchOK.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				setResult(Activity.RESULT_OK);
				switchActivity.this.finish();
			}
		});
		
		Button btnSwitchCancle=(Button)findViewById(R.id.btnSwitchCancle);
		btnSwitchCancle.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				setResult(Activity.RESULT_CANCELED);
				switchActivity.this.finish();
			}
		});
		
		gvSwitch = (GridView)findViewById(R.id.gvSwitch);
		adpApp = new appSitchAdpter(smartKeyApp.mInstance.getSwitchList(), this);
		gvSwitch.setAdapter(adpApp);
		
		gvSwitch.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
	            // TODO Auto-generated method stub
				Intent intent;
				if(iStartMode == START_MODE_ADD)
				{
					intent = new Intent(switchActivity.this,OpenAppMore.class);
					intent.putExtra("PageMode",OpenAppMore.PAGE_SWITCH);
					intent.putExtra("ClickMode",position);
					startActivity(intent);
				}
				else
				{
					ArrayList<ProgrameSt> lsPrograme = smartKeyApp.mInstance.getSwitchList();
					ProgrameSt stProgrameSt = lsPrograme.get(position);
					if(stProgrameSt.sPackName!=null && stProgrameSt.sPackName.trim().length()>0 )
					{
		        		PackageManager packageManager = getPackageManager(); 
		    			intent =packageManager.getLaunchIntentForPackage(stProgrameSt.sPackName);
		    			if(intent==null)
		    			{
		    				return;
		    			}
		    			startActivity(intent);
		    			switchActivity.this.finish();
					}
				}
            }

		});
		
		if(iStartMode == START_MODE_ADD)
		{
			lySwitchButton.setVisibility(View.VISIBLE);
			gvSwitch.setOnItemLongClickListener(new OnItemLongClickListener()
			{
	
				@Override
	            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	            {
					ArrayList<ProgrameSt> lsPrograme = smartKeyApp.mInstance.getSwitchList();
					ProgrameSt stProgrameSt = lsPrograme.get(position);
					stProgrameSt.drIcon=null;
					stProgrameSt.sPackName="";
					adpApp.notifyDataSetChanged();
		            return true;
	            }
			});
		}
		else 
		{
			lySwitchButton.setVisibility(View.GONE);
		}
		
		int iFlag = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING 
				|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON 
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		
		getWindow().addFlags(iFlag);

	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		adpApp.notifyDataSetChanged();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		if(isFinishing())
		{
			smartKeyApp.mInstance.SaveSwitchList();
		}
	}
}

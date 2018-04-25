package com.zzy.smarttouch;

import com.pocket.network.HttpDeal;
import com.zzy.lock.AdminManager;
import com.zzy.privacy.CreateGestureActivity;
import com.zzy.privacy.ModifyGesturePasswordActivity;
import com.zzy.privacy.UnlockGesturePasswordActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingPage extends BaseActivity implements OnClickListener
{
	private final int RESULT_CODE_UNLOCK = 1;
	private final int RESULT_CODE_CREATE = 2;
	
	private ImageView imgvSound;
	private ImageView imgvVibrate;
	private ImageView imgvEnable;
	private AboutDialog aboutDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_setting_page);
		
		ImageView imgvSettingBack =(ImageView)findViewById(R.id.imgvSettingBack);
		imgvSettingBack.setOnClickListener(this);
		TextView lbSettingTitle =(TextView)findViewById(R.id.lbSettingTitle);
		lbSettingTitle.setOnClickListener(this);
		
		
		imgvEnable = (ImageView)findViewById(R.id.imgvEnable);
		if(smartKeyApp.mInstance.getEnable())
		{
			imgvEnable.setImageResource(R.drawable.smartkey_btn_on);
		}
		else 
		{
			imgvEnable.setImageResource(R.drawable.smartkey_btn_off);
		}
		
		imgvSound = (ImageView)findViewById(R.id.imgvSound);
		if(smartKeyApp.mInstance.getClicksound())
		{
			imgvSound.setImageResource(R.drawable.smartkey_btn_on);
		}
		else 
		{
			imgvSound.setImageResource(R.drawable.smartkey_btn_off);
		}
		
		imgvVibrate =(ImageView)findViewById(R.id.imgvVibrate);
		if(smartKeyApp.mInstance.getClickVibrate())
		{
			imgvVibrate.setImageResource(R.drawable.smartkey_btn_on);
		}
		else 
		{
			imgvVibrate.setImageResource(R.drawable.smartkey_btn_off);
		}
		
		LinearLayout lySound =(LinearLayout)findViewById(R.id.lySound);
		lySound.setOnClickListener(this);
		LinearLayout lyVibrate =(LinearLayout)findViewById(R.id.lyVibrate);
		lyVibrate.setOnClickListener(this);
		LinearLayout lyModifyPattern =(LinearLayout)findViewById(R.id.lyModifyPattern);
		lyModifyPattern.setOnClickListener(this);
		LinearLayout lyShare =(LinearLayout)findViewById(R.id.lyShare);
		lyShare.setOnClickListener(this);
		LinearLayout lyAbout =(LinearLayout)findViewById(R.id.lyAbout);
		lyAbout.setOnClickListener(this);
		LinearLayout lyCheckUpdate =(LinearLayout)findViewById(R.id.lyCheckUpdate);
		lyCheckUpdate.setOnClickListener(this);
		LinearLayout lyUninstall =(LinearLayout)findViewById(R.id.lyUninstall);
		lyUninstall.setOnClickListener(this);
		
		LinearLayout lyClose =(LinearLayout)findViewById(R.id.lyClose);
		lyClose.setOnClickListener(this);
		
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
		if (requestCode == RESULT_CODE_UNLOCK)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Intent intent = new Intent(SettingPage.this,CreateGestureActivity.class);
				startActivityForResult(intent,RESULT_CODE_CREATE);
			}
		}
		else
		{
			if (resultCode == Activity.RESULT_OK)
			{
				smartKeyApp.mInstance.showToast(R.string.STR_MODIFY_SUCCESS);
			}
		}
	}

	@Override
    public void onClick(View view)
    {
	    switch (view.getId())
        {
	    case R.id.lyClose:
	    	boolean bEnable =false;
			if(smartKeyApp.mInstance.getEnable())
			{
				bEnable = false;
				imgvEnable.setImageResource(R.drawable.smartkey_btn_off);
				if(mService !=null)
				{
					mService.StopTouch();
				}
			}
			else 
			{
				imgvEnable.setImageResource(R.drawable.smartkey_btn_on);
				bEnable = true;
				if(mService !=null)
				{
					mService.StartTouch();
				}
			}
			smartKeyApp.mInstance.SaveEnable(bEnable);
	    	break;
	    case R.id.imgvSettingBack:
	    case R.id.lbSettingTitle:
	    	this.finish();
	    	break;
		case R.id.lySound:
			boolean bSound =false;
			if(smartKeyApp.mInstance.getClicksound())
			{
				bSound = false;
				imgvSound.setImageResource(R.drawable.smartkey_btn_off);
			}
			else 
			{
				imgvSound.setImageResource(R.drawable.smartkey_btn_on);
				bSound = true;
			}
			smartKeyApp.mInstance.SaveClickSound(bSound);

			break;
		case R.id.lyVibrate:
			boolean bVibrate =false;
			if(smartKeyApp.mInstance.getClickVibrate())
			{
				bVibrate = false;
				imgvVibrate.setImageResource(R.drawable.smartkey_btn_off);
			}
			else 
			{
				imgvVibrate.setImageResource(R.drawable.smartkey_btn_on);
				bVibrate = true;
			}
			smartKeyApp.mInstance.SaveClickVibrate(bVibrate);
			break;
		case R.id.lyModifyPattern:
			Intent intentPattern = new Intent(this,ModifyGesturePasswordActivity.class);
			startActivityForResult(intentPattern,RESULT_CODE_UNLOCK);
			break;
		case R.id.lyShare:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.STR_SHARE).toString());
			String sText = getText(R.string.STR_SHARE_TEXT).toString();
			intent.putExtra(Intent.EXTRA_TEXT, sText + "http://zzy.51gnss.cn/i0000.htm");
			startActivity(Intent.createChooser(intent, getTitle()));
			break;
		case R.id.lyAbout:
			if(aboutDialog==null)
			{
				aboutDialog = new AboutDialog(this);
			}
			aboutDialog.show();
			break;
		case R.id.lyCheckUpdate:
			if (!HttpDeal.bNetOk)
			{
				Toast.makeText(this, R.string.STR_REG_NO_NETWORK, Toast.LENGTH_SHORT).show();
				return;
			}
			Toast.makeText(this, R.string.STR_CHECK_UPDATE_TIPS, Toast.LENGTH_LONG).show();
			mService.CheckApkUpdateRequest();
			break;
		case R.id.lyUninstall:
			AdminManager adminManager = smartKeyApp.mInstance.getAdminManage();
			if (adminManager.getEnabled())
			{
				adminManager.Disable();
			}

			Uri uri = Uri.parse("package:" + this.getPackageName());
			Intent intentUninstall = new Intent(Intent.ACTION_DELETE, uri);
			startActivity(intentUninstall);
			break;

		default:
			break;
		}
	    
    }
	
	public class AboutDialog extends AlertDialog
	{
		public AboutDialog(Context context)
		{
			super(context);
			View view = getLayoutInflater().inflate(R.layout.smartkey_about, null);
			setButton(BUTTON_POSITIVE, context.getText(R.string.STR_OK), (OnClickListener) null);
			setIcon(R.drawable.ic_launcher);
			String sTitle = context.getText(R.string.app_name).toString();
			setTitle(sTitle);
			setView(view);
		}
	}
}

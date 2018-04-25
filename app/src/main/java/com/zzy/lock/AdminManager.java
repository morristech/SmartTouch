package com.zzy.lock;

import com.zzy.smarttouch.Common;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class AdminManager
{
	public static final int PASSWORD_QUALITY_ALPHABETIC = 262144;
	public static final int PASSWORD_QUALITY_ALPHANUMERIC = 327680;
	public static final int PASSWORD_QUALITY_NUMERIC = 131072;
	public static final int PASSWORD_QUALITY_UNSPECIFIED = 0;
	private ComponentName rec;
	private DevicePolicyManager dm;
	
	public AdminManager(Context context)
	{
		rec = new ComponentName(context.getApplicationContext(), AdminReceiver.class);
		dm = (DevicePolicyManager)context.getApplicationContext().getSystemService("device_policy");
	}

	public Intent Enable(Context context,String Explanation)
	{
		 ComponentName componentName = new ComponentName(context, AdminReceiver.class);
		 Intent intent = new Intent();
		 intent.setAction(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		 intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
		 intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, Explanation);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 return intent;
		// context.startActivity(intent);
	}

	public boolean getEnabled()
	{
		return this.dm.isAdminActive(this.rec);
	}

	public void LockScreen()
	{
		this.dm.lockNow();
	}

	public void Disable()
	{
		if (getEnabled())
			this.dm.removeActiveAdmin(this.rec);
	}

	public boolean getPasswordSufficient()
	{
		return this.dm.isActivePasswordSufficient();
	}

	public boolean ResetPassword(String NewPassword)
	{
		return this.dm.resetPassword(NewPassword, 0);
	}

	public void SetPasswordQuality(int QualityFlag, int MinimumLength)
	{
		this.dm.setPasswordMinimumLength(this.rec, MinimumLength);
		this.dm.setPasswordQuality(this.rec, QualityFlag);
	}

	public void RequestNewPassword(Context context) throws ClassNotFoundException
	{
		Intent i = new Intent("android.app.action.SET_NEW_PASSWORD");
		context.startActivity(i);
	}

	public void setMaximumTimeToLock(long value)
	{
		this.dm.setMaximumTimeToLock(this.rec, value);
	}

	public static class AdminReceiver extends DeviceAdminReceiver
	{
		private boolean checkForService = true;
		private Intent serviceIntent;

		public void onReceive(Context context, Intent intent)
		{
			if (this.checkForService)
			{
				this.checkForService = false;
				try
				{
					Class<?> ser = Class.forName(context.getPackageName() + ".managerservice");
					this.serviceIntent = new Intent(context, ser);
				}
				catch (ClassNotFoundException e)
				{
					this.serviceIntent = null;
				}
			}

			super.onReceive(context, intent);
			if (this.serviceIntent != null)
			{
				this.serviceIntent.putExtra("admin_intent", intent);
				context.startService(this.serviceIntent);
			}
		}

		public void onEnabled(Context context, Intent intent)
		{
			if (this.serviceIntent != null)
			{
				this.serviceIntent.putExtra("admin", "Enabled");
			}
		}

		public void onDisabled(Context context, Intent intent)
		{
			if (this.serviceIntent != null)
			{
				this.serviceIntent.putExtra("admin", "Disabled");
			}
		}

		public void onPasswordChanged(Context context, Intent intent)
		{
			if (this.serviceIntent != null)
			{
				this.serviceIntent.putExtra("admin", "PasswordChanged");
			}
		}
	}
}

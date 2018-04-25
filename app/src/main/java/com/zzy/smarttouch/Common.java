package com.zzy.smarttouch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

import com.pocket.network.HttpDeal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class Common
{
	private static ProgressDialog progressDialog;
	public static void LogEx(String sLog)
	{
		Log.v("B4A",sLog);
	}
	
	public static int Rnd(int Min, int Max)
	{
		Random random =null;
		random = new Random();
		return Min + random.nextInt(Max - Min);
	}
	
	public static boolean SensorAvailable(Context context,int iType)
	{
		SensorManager sm = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> lsSensor = sm.getSensorList(iType);
		if(lsSensor==null)
		{
			return false;
		}
		return true;
	}
	
	public static String EncodeUrl(String Url)
	{
		try
		{
			return URLEncoder.encode(Url,"UTF8");
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	
	public static String GetFullUrl(Context context,String sUrl)
	{
		String sManufacturer=EncodeUrl(Build.MANUFACTURER);
		String sVersion;
	
		sVersion = EncodeUrl(Build.VERSION.RELEASE);

		String sModel = EncodeUrl(Build.MODEL);
		String sProduct =EncodeUrl(Build.PRODUCT);
		String sBrand = EncodeUrl(Build.BRAND);
		
		Context ctx = context.getApplicationContext();
		
		TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		String sImsi = tm.getSubscriberId();
		if(sImsi==null)
		{
			sImsi="460014569878546";
		}
		
		String sImei = tm.getDeviceId();
		if(sImei==null)
		{
			sImei="";
		}
		
		//String sAndroidId = Settings.System.getString(ctx.getContentResolver(),Settings.Secure.ANDROID_ID);
		String sAndroidId = Settings.Secure.getString(ctx.getContentResolver(),Settings.Secure.ANDROID_ID);
		if (sAndroidId == null)
		{
			sAndroidId = "";
		}

		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();

		PackageManager pm = ctx.getPackageManager();
		String sCode = "";
		try
		{
			sCode = pm.getPackageInfo(ctx.getPackageName(), 0).versionName;
		} 
		catch (NameNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WifiManager wifi = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String sMac = info.getMacAddress();
		if(sMac!=null)
		{
			sMac =EncodeUrl(sMac);
		}
		else 
		{
			sMac ="";
		}  
		
		String sClientId="0000";
		String sPhoneType ="01";
		String sFree="0";
		
		StringBuilder sbUrl = new StringBuilder();
		 sbUrl.append(sUrl).append("cid=")
		 	.append(sClientId)
			.append("&imsi=")
			.append(sImsi)
			.append("&imei=")
			.append(sImei)
			.append("&androidid=")
			.append(sAndroidId)
			.append("&manufacturer=")
			.append(sManufacturer)
			.append("&version=")
			.append(sVersion)
			.append("&model=")
			.append(sModel)
			.append("&product=")
			.append(sProduct)
			.append("&brand=")
			.append(sBrand)
			.append("&NetworkType=")
			.append(HttpDeal.sNetType)
			.append("&w=")
			.append(dm.widthPixels)
			.append("&h=")
			.append(dm.heightPixels)
			.append("&type=")
			.append(sPhoneType)
			.append("&free=")
			.append(sFree)
			.append("&mac="+sMac)
			.append("&swver="+sCode);
		 return sbUrl.toString();
	}
	
	public static boolean Exists(String Dir, String FileName)
	{
		try
		{
			return new java.io.File(Dir, FileName).exists();
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static void StartProgressDialog(Context context)
	{
		if (progressDialog != null)
		{
			StopProgressDialog();
		}
		progressDialog = null;
		progressDialog = new ProgressDialog(context);
		progressDialog.setMessage(context.getText(R.string.STR_WAIT));
		progressDialog.show();
	}

	public static void StopProgressDialog()
	{
		if (progressDialog != null)
		{
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	public static boolean hasPermission(@Nullable Context context, @NonNull String permission) {
		return context != null && ((ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED));
	}

}

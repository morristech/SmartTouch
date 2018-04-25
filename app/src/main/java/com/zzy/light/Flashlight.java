package com.zzy.light;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import com.zzy.camera.QuickCamera;
import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.smartKeyApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

public class Flashlight
{
	public volatile Camera camera;
	private Handler handler = new Handler();
	
	public Flashlight()
	{
		this.camera = null;
	}
	
	private Runnable rbTack = new Runnable()
	{

		@Override
		public void run()
		{
			try
            {
				Flashlight.this.camera.startPreview();
				Common.LogEx("startPreview succ");
            }
            catch (Exception e)
            {
	            Common.LogEx("startPreview error");
	          	if(Flashlight.this.camera !=null)
            	{
	          		Flashlight.this.camera.release();
            	}
	          	Flashlight.this.camera = null;
	            e.printStackTrace();
            }
		}
	};
	
	public void Hide()
	{
		if(camera !=null)
    	{
			camera.stopPreview();
    		camera.release();
    	}
    	camera = null;
	}
	
	public void Show()
	{
		if(camera==null)
		{
			try
            {
				 camera  = Camera.open();
				 Camera.Parameters parameters = camera.getParameters();
			     parameters.setPictureFormat(ImageFormat.JPEG);
			     parameters.set("jpeg-quality", 100);
			     parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			     camera.setParameters(parameters);
			     handler.postDelayed(rbTack, 500);
			     Common.LogEx("OpenCamera succ");
            }
            catch (Exception e)
            {
            	if(camera !=null)
            	{
            		camera.release();
            	}
            	camera = null;
            	Common.LogEx("OpenCamera fail");
            	e.printStackTrace();
            }
			
		}
	}
	
}

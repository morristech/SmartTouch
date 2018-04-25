package com.zzy.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.smartKeyApp;

public class QuickCamera
{

	public volatile Camera camera;
	private Handler handler = new Handler();
	private Context context;
	
	public QuickCamera(Context context)
	{
		this.context = context;
		this.camera = null;
	}
	
	private Runnable rbTack = new Runnable()
	{

		@Override
		public void run()
		{
			try
            {
				QuickCamera.this.camera.startPreview();
				AutoFocus();
				Common.LogEx("startPreview succ");
            }
            catch (Exception e)
            {
	            Common.LogEx("startPreview error");
	          	if(QuickCamera.this.camera !=null)
            	{
	          		QuickCamera.this.camera.release();
            	}
	          	QuickCamera.this.camera = null;
	            e.printStackTrace();
            }
		}
	};
	
	public void TakePicture()
	{
		try
        {
			camera.takePicture(null, null, new Camera.PictureCallback()
			{
				public void onPictureTaken(byte[] data, Camera camera)
				{
					Common.LogEx("onPictureTaken succ");
					camera.stopPreview();
					StringBuilder sbJpgName =smartKeyApp.mInstance.getStringbuild();
					String sName = sbJpgName.append(System.currentTimeMillis()).append(".jpg").toString();

					File fileTmp = new File(smartKeyApp.mInstance.getPhotoDir(), sName);
					BufferedOutputStream output = null;
					try
					{
						output = new BufferedOutputStream(new FileOutputStream(fileTmp, false));
						output.write(data);
						output.close();
						output = null;
					}
					catch (FileNotFoundException e)
					{
						e.printStackTrace();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
					intent.setData(Uri.fromFile(fileTmp));
					context.sendBroadcast(intent);
					
					QuickCamera.this.camera.stopPreview();
					QuickCamera.this.camera.release();
					QuickCamera.this.camera = null;
				}
			});
        }
        catch (Exception e)
        {
            Common.LogEx("takePicture error");
          	if(camera !=null)
        	{
        		camera.release();
        	}
          	camera = null;
            e.printStackTrace();
        }
        
	}
	
	public void AutoFocus()
	{
		try
		{
			this.camera.autoFocus(new Camera.AutoFocusCallback()
			{
				@Override
	            public void onAutoFocus(boolean success, Camera camera)
	            {
					TakePicture();
	            }
			});
		//	TakePicture();
			Common.LogEx("AutoFocus Succ");
		}
        catch (Exception e)
        {
        	if(camera !=null)
        	{
        		camera.release();
        	}
        	camera = null;
        	e.printStackTrace();
        	Common.LogEx("AutoFocus fail");
        }
	}
	
	public void CloseCamera()
	{
		if(camera !=null)
    	{
			QuickCamera.this.camera.stopPreview();
    		camera.release();
    	}
    	camera = null;
	}
	
	public void OpenCamera()
	{
		if(camera==null)
		{
			try
            {
				 camera  = Camera.open();
				 Camera.Parameters parameters = camera.getParameters();
			     parameters.setPictureFormat(ImageFormat.JPEG);
			     parameters.set("jpeg-quality", 100);
			     parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
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
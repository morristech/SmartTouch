package com.zzy.light;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.zzy.camera.QuickCamera;
import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.smartKeyApp;

import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FlashlightSurface extends SurfaceView implements SurfaceHolder.Callback
{

	private SurfaceHolder mHolder;
	private Camera mCameraDevices;
	private Camera.Parameters mParameters;
	private boolean bOn;
	private TakePictureListen takePictureListen;

	public FlashlightSurface(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		mHolder = this.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		bOn =false;
		takePictureListen= null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		mParameters = mCameraDevices.getParameters();
		if (mParameters != null)
		{
			mParameters.setPictureFormat(ImageFormat.JPEG);
			mParameters.set("jpeg-quality", 100);
			mParameters.setJpegQuality(90);
		}
		if(bOn)
		{
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		}
		else 
		{
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		}
		mCameraDevices.setParameters(mParameters);
		mCameraDevices.startPreview();
		if(takePictureListen!=null)
		{
			mCameraDevices.autoFocus(autoFocusCallback);
		}
	}
	
	private AutoFocusCallback  autoFocusCallback= new AutoFocusCallback()
	{
		
		@Override
		public void onAutoFocus(boolean success, Camera camera)
		{
			TakePicture(camera);
		}
	};
	
	public void TakePicture(Camera camera)
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
					getContext().sendBroadcast(intent);
					
					//camera.stopPreview();
					//camera.release();
					if(takePictureListen!=null)
					{
						takePictureListen.finish(getContext(),fileTmp.toString());
					}
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
			if(takePictureListen!=null)
			{
				takePictureListen.finish(getContext(),null);
			}
            e.printStackTrace();
        }
        
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			mCameraDevices = Camera.open();
			mCameraDevices.setPreviewDisplay(mHolder);
		}
		catch (Exception e)
		{
			if (mCameraDevices != null)
			{
				mCameraDevices.release();
			}
			mCameraDevices = null;
			if(takePictureListen!=null)
			{
				takePictureListen.finish(getContext(),null);
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		if (mCameraDevices == null)
		{
			return;
		}
		mCameraDevices.stopPreview();
		mCameraDevices.release();
		mCameraDevices = null;
	}

	public void setFlashlightSwitch(boolean on)
	{
		bOn = on;
		if (mCameraDevices == null)
		{
			return;
		}
		
		if (mParameters == null)
		{
			mParameters = mCameraDevices.getParameters();
		}
		if (on)
		{
			Common.LogEx("setFlashlightSwitch true");
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		}
		else
		{
			Common.LogEx("setFlashlightSwitch false");
			mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		}
		mCameraDevices.setParameters(mParameters);
	}
	
	public void SetOnTakePictureListen(TakePictureListen takePictureListen)
	{
		this.takePictureListen = takePictureListen; 
	}
	
	public interface TakePictureListen
	{
		public void finish(Context context, String sName);
	}

}

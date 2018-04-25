package com.zzy.record;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.DateTime;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.notifyService;
import com.zzy.smarttouch.notifyService.notifyService_BR;
import com.zzy.smarttouch.smartKeyApp;
import com.zzy.smarttouch.R.string;
import com.zzy.smarttouch.smartKeyApp.OnRecordStopListen;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class CostomRecord
{
	public static final String RECORD_STATE_ACTION="smartTouch_record_state_action";
	private MediaRecorder mediaRecorder;
	
	private NotificationManager nm; 
	private Notification notification;
	private Builder builder;
	private final int nId=200;
	private Handler handler = new Handler();
	private Context context;
	private String sRecordName;
	private long iStartTick;
	private NumberFormat numberFormat;
	private boolean bRecord;
	
	private RemoteViews rViews;
	private PendingIntent piClick;
	private PendingIntent piContent;
	private boolean bShowNotify;
	
	public CostomRecord(Context context)
	{
		handler = new Handler();
		mediaRecorder = null;
		this.context = context;
		this.iStartTick = 0;
		this.bRecord = false;
		
		smartKeyApp.mInstance.SetOnRecordStopListen(new OnRecordStopListen()
		{
			
			@Override
			public void onStop()
			{
				if(bRecord)
				{
					StopRecord();
				}
				else 
				{
					if(bShowNotify)
					{
						nm.cancel(nId);
					}
					bShowNotify = false;
				}
			}
		});
	}
	
	private boolean getExternalWritable()
	{
		String state = Environment.getExternalStorageState();
		if ("mounted".equals(state))
		{
			return true;
		}
		return false;
	}
	 
	public void StartRecord()
	{
		Common.LogEx("start record");
		
		if(!getExternalWritable())
		{
			smartKeyApp.mInstance.showToast(R.string.STR_CHECK_SDCARD);
			return;
		}

		File fPath = new File(Environment.getExternalStorageDirectory(),notifyService.DIR_SMART_TOUCH);
		fPath.mkdirs();
		
		fPath = new File(fPath,notifyService.DIR_SMART_RECORD);
		fPath.mkdirs();

		try
		{
			DateTime dTime = new DateTime();
			sRecordName = String.valueOf("Record_")+String.valueOf(dTime.getNow())+".amr";
			fPath = new File(fPath,sRecordName);

			mediaRecorder = new MediaRecorder(); 

			// ��1����������Ƶ��Դ��MIC��ʾ��˷磩  
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
			//��2����������Ƶ�����ʽ��Ĭ�ϵ������ʽ��  
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);  
			//��3����������Ƶ���뷽ʽ��Ĭ�ϵı��뷽ʽ��  
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); 
			// ����ļ�·��
			mediaRecorder.setOutputFile(fPath.toString());
			mediaRecorder.setOnErrorListener(new OnErrorListener()
			{
				
				@Override
				public void onError(MediaRecorder mr, int what, int extra)
				{
					StopRecord();
				}
			});
			mediaRecorder.prepare();
			mediaRecorder.start();

			iStartTick =SystemClock.elapsedRealtime();
			handler.postDelayed(runnable, 500);
			bRecord = true;
			
			smartKeyApp.mInstance.setRecord(sRecordName, "00:00", true);
			
    		Intent intent = new Intent(context,RecordWin.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		context.startActivity(intent);
			
		}
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void StopRecord()
	{
		Common.LogEx("stop record");
		handler.removeCallbacks(runnable);
		bRecord = false;
		
		if (mediaRecorder != null)
		{
			String sTime = setRecordContent();
			CreateNotity(sTime,false);
			smartKeyApp.mInstance.setRecord(sRecordName, sTime, false);
			
			try
            {
				mediaRecorder.stop();
				mediaRecorder.release();
				mediaRecorder = null;
            }
            catch (IllegalStateException e)
            {
	            e.printStackTrace();
            }
		}
	}
	
	public void StartOrStopRecord()
	{
		if(bRecord)
		{
			StopRecord();
		}
		else 
		{
			StartRecord();
		}
	}
	
	private String millisecondsToTime(long iTick)
	{
		int iMinutes, iSeconds;
		iMinutes = (int) (iTick / (1000 * 60));
		iTick = iTick % (1000 * 60);
		iSeconds = (int) (iTick / 1000);

		StringBuilder sbTime = smartKeyApp.mInstance.getStringbuild();
		sbTime.append(TimeFormat(iMinutes, 2, 0)).append(":").append(TimeFormat(iSeconds, 2, 0));

		return sbTime.toString();
	}
 
	private String TimeFormat(int Number, int MinimumIntegers, int MaximumFractions)
	{
		if(numberFormat==null)
		{
			numberFormat = NumberFormat.getInstance(Locale.US);
		}
		numberFormat.setMaximumFractionDigits(MaximumFractions);
		numberFormat.setMinimumIntegerDigits(MinimumIntegers);
		return numberFormat.format(Number);
	}
	
	public String setRecordContent()
	{
		long iNow = SystemClock.elapsedRealtime();
		iNow = iNow-iStartTick;
		if(iNow>59*60*1000)
		{
			StopRecord();
		}
		
		String sTime =millisecondsToTime(iNow);
		return sTime;
	}
	
	private Runnable runnable = new Runnable()
	{
		@Override
		public void run()
		{
			String sTime = setRecordContent();
			CreateNotity(sTime,true);
			smartKeyApp.mInstance.setRecord(sRecordName, sTime, true);
			handler.postDelayed(this, 500);
		}
	};
	
	@SuppressWarnings("deprecation")
    @SuppressLint("NewApi") 
	private void CreateNotity(String sText,boolean bRecording)
	{
		bShowNotify = true;
		if(nm==null)
		{
			nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); 
		}

		boolean bFirst=false;
		if(rViews==null)
		{
			bFirst=true;
			rViews = new RemoteViews(context.getPackageName(),R.layout.smartkey_record_notification);
		}

		if(bRecording)
		{
			rViews.setViewVisibility(R.id.btnRecordView, View.GONE);
			rViews.setViewVisibility(R.id.btnRecordNotify, View.VISIBLE);
		}
		else
		{
			rViews.setViewVisibility(R.id.btnRecordView, View.VISIBLE);
			rViews.setViewVisibility(R.id.btnRecordNotify, View.GONE);
		}

		rViews.setTextViewText(R.id.tvRecordName, sRecordName);
		rViews.setTextViewText(R.id.tvRecordTime, sText);
		if(!bFirst)
		{
			nm.notify(nId,notification);
			return;
		}

		if(piClick==null)
		{
			Intent inAction = new Intent(context,notifyService_BR.class);
			inAction.setAction(RECORD_STATE_ACTION);
			piClick = PendingIntent.getBroadcast(context, 0, inAction,PendingIntent.FLAG_UPDATE_CURRENT);
		}
		rViews.setOnClickPendingIntent(R.id.btnRecordNotify, piClick);

		if(piContent==null)
		{
			Intent intent = new Intent(context,RecordWin.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			piContent = PendingIntent.getActivity(context, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
		}

		rViews.setOnClickPendingIntent(R.id.btnRecordView, piContent);

		if(builder==null)
		{
			builder = new Builder(context);
		}
		builder.setContentIntent(piContent);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setOngoing(true);

		notification = builder.build();
		notification.defaults &= ~Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = rViews;
		notification.icon = R.drawable.smartkey_app_case;

		nm.notify(nId,notification);


	}
}

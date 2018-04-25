package com.pocket.network;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.http.client.ClientProtocolException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;

import com.pocket.network.CostomHttpClient.HttpResponeWrapper;
import com.zzy.smarttouch.Common;

public class HttpDeal
{
	public static boolean bNetOk;
	public static String sNetType;
	private NetWorkReceiver brNetWork;
	
	private CostomHttpClient hc;
	private HashMap<String,HttpTask> TaskIdToJob;
	public static String sTempFolder;
	private int taskCounter;
	private OnNetStateList onNetStateList;

	public HttpDeal(Context context)
	{
		bNetOk = false;
		taskCounter = 0;
		brNetWork = new NetWorkReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.getApplicationContext().registerReceiver(brNetWork,filter);
		
		sTempFolder = context.getApplicationContext().getCacheDir().toString();
	    if (sTempFolder == null)
	    {
	    	sTempFolder = context.getApplicationContext().getFilesDir().toString();
	    }
	    
		TaskIdToJob = new HashMap<String, HttpTask>();
		try
		{
			hc = new CostomHttpClient();
			hc.Initialize(this.netInterface);
		} 
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean getNetWorkState()
	{
		return bNetOk;
	}
	
	private HttpInterface netInterface = new HttpInterface()
	{
		
		@Override
		public void net_streamfinish(boolean bRet, int iTask)
		{
			if(bRet)
			{
				CompleteJob(iTask,bRet, "");
			}
			else
			{
				CompleteJob(iTask,bRet,"");
			}
		}
		
		public String Combine(String Dir, String FileName)
		{
			return new File(Dir, FileName).toString();
		}
		
		@Override
		public void net_responsesuccess(HttpResponeWrapper res, int iTaskId)
		{
			FileOutputStream outputStream = null;
			try
			{
				outputStream = new FileOutputStream(Combine(sTempFolder,String.valueOf(iTaskId)),false);
				OutputStream Output = new BufferedOutputStream(outputStream);
				res.GetAsynchronously(Output,true,iTaskId);
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				if(outputStream!=null)
				{
					try
                    {
	                    outputStream.close();
                    }
                    catch (IOException e1)
                    {
	                    // TODO Auto-generated catch block
	                    e1.printStackTrace();
                    }
				}
				CompleteJob(iTaskId,false,"");
				e.printStackTrace();
			}
		}
		
		@Override
		public void net_responseerror(HttpResponeWrapper res, String sReason,int iStatusCode, int iTaskId)
		{
			if(res !=null)
			{
				try
				{
					res.Release();
				} 
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			Common.LogEx("net_responseerror error:"+sReason+" iTaskId:"+iTaskId+" iStatusCode:"+iStatusCode);
			CompleteJob(iTaskId,false, sReason);
		}
	};
	
	private void CompleteJob(int iTaskId,boolean bRet, String sError)
	{
		HttpTask job;
		job = TaskIdToJob.get(String.valueOf(iTaskId));
		Common.LogEx("iTaskId:"+iTaskId);
		TaskIdToJob.remove(String.valueOf(iTaskId));
		if(bRet)
		{
			job.iReson = HttpTask.DOWNLOAD_STATE_SUCC;
		}
		else
		{
			job.iReson = HttpTask.DOWNLOAD_STATE_FAIL;
		}
		job.sErrorMessage = sError;
		job.Complete(iTaskId);
	}
	
	public int mitJob(HttpTask job)
	{
		try
		{
			if(!bNetOk)
			{
				return -1;
			}
			
			Set<Entry<String,HttpTask>> set = TaskIdToJob.entrySet();
			if(set==null)
			{
				return -1;
			}
			Iterator<Entry<String,HttpTask>> it = set.iterator();
			if(it==null)
			{
				return -1;
			}
			while (it.hasNext())
			{
				Entry<String,HttpTask> me = (Entry<String,HttpTask>) it.next();
				HttpTask jobTmp =me.getValue();
				if(jobTmp.sLink.equalsIgnoreCase(job.sLink))
				{
					Common.LogEx("mitjob job is exist");
					return -1;
				}
			}
			
			if(taskCounter>=0x7FFFFFFE)
			{
				taskCounter=0;
			}
			
			taskCounter = taskCounter + 1;
			Common.LogEx("taskCounter:"+taskCounter);
			TaskIdToJob.put(String.valueOf(taskCounter),job);
			if(job.sUsername!="" && job.sPassword !="")
			{
				hc.ExecuteCredentials(job.GetRequest(), taskCounter, job.sUsername, job.sPassword);
			}
			else 
			{
				hc.Execute(job.GetRequest(), taskCounter);
			}
			Common.LogEx("mitjob url:"+job.sLink+" task="+taskCounter);
		} 
		catch(ClientProtocolException e)
		{
			CompleteJob(taskCounter,false,"");
			e.printStackTrace();
		} catch (IOException e)
		{
			CompleteJob(taskCounter,false,"");
			e.printStackTrace();
		}
		return taskCounter;
	}

	
	private class NetWorkReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE"))
			{
				NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra("networkInfo");
				sNetType = ni.getTypeName();
				String sState = ni.getState().toString();
				if (sState.equalsIgnoreCase("CONNECTED"))
				{
					bNetOk = true;
				} 
				else
				{
					bNetOk = false;
				}
				
				if(onNetStateList!=null)
				{
					onNetStateList.getNetStateList(bNetOk);
				}
			}
		}
	}
	
	public void setOnNetStateList(OnNetStateList onNetStateList)
	{
		this.onNetStateList = onNetStateList;
	}
	
	public interface OnNetStateList
	{
		public void getNetStateList(boolean bOk);
	}
}

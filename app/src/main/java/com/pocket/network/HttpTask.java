package com.pocket.network;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.util.EncodingUtils;
import org.xml.sax.Attributes;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.MimeTypeMap;
import com.pocket.network.CostomHttpClient.HttpUriRequestWrapper;
import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.notifyService;
import com.zzy.smarttouch.smartKeyApp;

public class HttpTask
{
	public static final int MSG_MIT_JOB = 0;
	public static final int MSG_SEND_MSG = 1;

	public static final int POST_TYPE_NORMAL = 0;
	public static final int POST_TYPE_MULTIPART = 1;

	public static final int DOWNLOAD_STATE_SUCC = 0;
	public static final int DOWNLOAD_STATE_FAIL = 1;
	public static final int DOWNLOAD_STATE_NO_NETWORK = 2;
	public static final int DOWNLOAD_STATE_DATA_ERROR = 3;

	public static final int DOWNLOAD_ID_CHECKAPK = 0;
	public static final int DOWNLOAD_ID_DOWNLOADAPK = 1;
	public static final int DOWNLOAD_ID_REGEDIT = 2;
	public static final int DOWNLOAD_ID_GAMEBOX = 3;
	public static final int DOWNLOAD_ID_GAMEPIC = 4;

	public int iReson;
	public String sUsername, sPassword;
	public String sErrorMessage;
	public String sLink;
	public int iTaskId;
	private HttpUriRequestWrapper req;
	private jobHandle mHandle;
	public String sDir;
	public String sName;

	public HttpTask(int id, Object oParam, DownLoadInterface inf)
	{
		this.req = new HttpUriRequestWrapper();
		mHandle = new jobHandle(id, oParam, inf);
		sDir = null;
		sName = null;
	}
	
	public void setDownInfo(String sDir,String sName)
	{
		this.sDir = sDir;
		this.sName = sName;
		mHandle.setDownInfo(sDir, sName);
	}

	public HttpUriRequestWrapper GetRequest()
	{
		return req;
	}

	private void JobError()
	{
		iReson = DOWNLOAD_STATE_NO_NETWORK;
		Complete(iTaskId);
	}

	public void JobGet(String sLink)
	{
		if (!HttpDeal.bNetOk)
		{
			JobError();
			return;
		}
		this.sLink = sLink;
		req.InitializeGet(this.sLink);
		req.SetHeader("User-Agent", "SmartWin");
	}

	public void JobPost(Context context, String sLink, String sDir, String sName, int iType, HashMap<String, String> paraHashMap)
	{
		if (!HttpDeal.bNetOk)
		{
			JobError();
			return;
		}

		InputStream inStream = null;
		String sUrl;
		try
		{

			int iFileSize = 0;
			if (!Common.Exists(sDir, sName))
			{
				iFileSize = 0;
			}
			else
			{
				if (sDir.equals("AssetsDir"))
				{
					inStream = context.getApplicationContext().getAssets().open(sName.toLowerCase(Locale.US));
				}
				else if (sDir.equals("ContentDir"))
				{
					inStream = context.getApplicationContext().getContentResolver().openInputStream(Uri.parse(sName));
				}
				else
				{
					FileInputStream inputStream = new FileInputStream(new File(sDir, sName));
					inStream = new BufferedInputStream(inputStream, 4096);
					iFileSize = inStream.available();
				}
			}

			int idx = 0;
			byte[] cParmBuf;
			byte[] cPostbuf;
			String sHeader;
			if (iType == POST_TYPE_NORMAL)
			{
				String sParm;
				idx = sLink.indexOf("?");
				if (idx > -1)
				{
					sParm = sLink.substring(idx + 1);
					sUrl = sLink.substring(0, idx);
				}
				else
				{
					sParm = "";
					sUrl = sLink;
				}

				cParmBuf = sParm.getBytes("utf8");
				cPostbuf = new byte[iFileSize + cParmBuf.length];
				for (int i = 0; i < cParmBuf.length - 1; i++)
				{
					cPostbuf[i] = cParmBuf[i];
				}

				if (inStream != null)
				{
					inStream.read(cPostbuf, cParmBuf.length, iFileSize);
				}
				sHeader = "application/x-www-form-urlencoded";
			}
			else
			{
				sUrl = sLink;
				MimeTypeMap mime = MimeTypeMap.getSingleton();
				String ext = MimeTypeMap.getFileExtensionFromUrl(sName);
				String mineType = mime.getMimeTypeFromExtension(ext);
				if (mineType == null || mineType.trim() == "")
				{
					mineType = "application/octet-stream";
				}

				StringBuilder sbTmp = new StringBuilder();

				Set<Entry<String, String>> set = paraHashMap.entrySet();
				Iterator<Entry<String, String>> it = set.iterator();
				while (it.hasNext())
				{

					Entry<String, String> me = (Entry<String, String>) it.next();
					sbTmp.append("------------------------------4794b066d046\r\nContent-Disposition: form-data; name=\"");
					sbTmp.append(me.getKey());
					sbTmp.append("\"\r\n\r\n");
					sbTmp.append(me.getValue());
					sbTmp.append("\r\n");
				}
				sbTmp.append("------------------------------4794b066d046\r\nContent-Disposition: form-data; name=\"upload\"; filename=\"");
				sbTmp.append(sName);
				sbTmp.append("\"\r\nContent-Type: ");
				sbTmp.append(mineType);
				sbTmp.append("\r\n\r\n");

				cParmBuf = sbTmp.toString().getBytes("utf8");

				String sEndFilter = "\r\n" + "------------------------------4794b066d046--";
				byte[] cEndBuf = sEndFilter.getBytes("utf8");

				cPostbuf = new byte[iFileSize + cParmBuf.length + cEndBuf.length];
				for (int i = 0; i < cParmBuf.length - 1; i++)
				{
					cPostbuf[i] = cParmBuf[i];
				}

				if (inStream != null)
				{
					inStream.read(cPostbuf, cParmBuf.length, iFileSize);
				}

				for (int i = 0; i < cEndBuf.length - 1; i++)
				{
					cPostbuf[iFileSize + cParmBuf.length + i] = cEndBuf[i];
				}
				sHeader = "multipart/form-data; boundary=----------------------------4794b066d046";
			}

			if (inStream != null)
			{
				inStream.close();
			}
			req.InitializePost2(sUrl, cPostbuf);
			req.SetContentType(sHeader);
			req.SetHeader("User-Agent", "SmartView");
		}
		catch (Exception e)
		{
			if (inStream != null)
			{
				try
				{
					inStream.close();
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}

	public void JobPost(String sLink, String sContent, int iType, HashMap<String, String> paraHashMap, String sMimeType)
	{
		if (!HttpDeal.bNetOk)
		{
			JobError();
			return;
		}

		String sUrl;
		try
		{
			byte aContent[] = sContent.getBytes("utf8");
			int iFileSize = aContent.length;

			int idx = 0;
			byte[] cParmBuf;
			byte[] cPostbuf;
			String sHeader;
			if (iType == POST_TYPE_NORMAL)
			{
				String sParm;
				idx = sLink.indexOf("?");
				if (idx > -1)
				{
					sParm = sLink.substring(idx + 1);
					sUrl = sLink.substring(0, idx);
				}
				else
				{
					sParm = "";
					sUrl = sLink;
				}

				cParmBuf = sParm.getBytes("utf8");
				cPostbuf = new byte[iFileSize + cParmBuf.length];

				System.arraycopy(cParmBuf, 0, cPostbuf, 0, cParmBuf.length);
				if (sContent != null)
				{
					System.arraycopy(aContent, 0, cPostbuf, cParmBuf.length, iFileSize);
				}

				sHeader = "application/x-www-form-urlencoded";
			}
			else
			{
				sUrl = sLink;
				if (sMimeType == null || sMimeType.trim() == "")
				{
					sMimeType = "application/octet-stream";
				}

				StringBuilder sbTmp = new StringBuilder();

				Set<Entry<String, String>> set = paraHashMap.entrySet();
				Iterator<Entry<String, String>> it = set.iterator();
				while (it.hasNext())
				{

					Entry<String, String> me = (Entry<String, String>) it.next();
					sbTmp.append("------------------------------4794b066d046\r\nContent-Disposition: form-data; name=\"");
					sbTmp.append(me.getKey());
					sbTmp.append("\"\r\n\r\n");
					sbTmp.append(me.getValue());
					sbTmp.append("\r\n");
				}
				sbTmp.append("------------------------------4794b066d046\r\nContent-Disposition: form-data; name=\"upload\"; filename=\"");
				sbTmp.append("tmp");
				sbTmp.append("\"\r\nContent-Type: ");
				sbTmp.append(sMimeType);
				sbTmp.append("\r\n\r\n");

				cParmBuf = sbTmp.toString().getBytes("utf8");

				String sEndFilter = "\r\n" + "------------------------------4794b066d046--";
				byte[] cEndBuf = sEndFilter.getBytes("utf8");

				cPostbuf = new byte[iFileSize + cParmBuf.length + cEndBuf.length];

				System.arraycopy(cParmBuf, 0, cPostbuf, 0, cParmBuf.length);
				if (sContent != null)
				{
					System.arraycopy(aContent, 0, cPostbuf, cParmBuf.length, iFileSize);
				}

				System.arraycopy(cEndBuf, 0, cPostbuf, iFileSize + cParmBuf.length, cEndBuf.length);
				sHeader = "multipart/form-data; boundary=----------------------------4794b066d046";
			}

			req.InitializePost2(sUrl, cPostbuf);
			req.SetContentType(sHeader);
			req.SetHeader("User-Agent", "SmartView");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void Complete(int iTask)
	{
		this.iTaskId = iTask;

		Message msg = new Message();
		msg.what = 0;
		msg.obj = null;

		Bundle bundle = new Bundle();
		bundle.putInt("task", iTaskId);
		bundle.putInt("reson", iReson);
		bundle.putString("message", sErrorMessage);
		msg.setData(bundle);

		this.mHandle.sendMessage(msg);
	}

	public boolean Release()
	{
		return new File(HttpDeal.sTempFolder, String.valueOf(this.iTaskId)).delete();
	}

	public interface DownLoadInterface
	{
		public void download_finish(int iReason, Object oInputParam, Object oOutParam);
	}

	public static class jobHandle extends Handler
	{
		private DownLoadInterface inf = null;
		private int id;
		public Object oParam;
		private HashMap<String, Object> hashTmp;
		private String sDir;
		private String sName;

		public jobHandle(int id, Object oParam, DownLoadInterface inf)
		{
			this.inf = inf;
			this.id = id;
			this.oParam = oParam;
			sDir = null;
			sName = null;
		}
		
		public void setDownInfo(String sDir,String sName)
		{
			this.sDir = sDir;
			this.sName = sName;
		}

		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == 0)
			{

				Bundle bundle = msg.getData();
				int iTaskId = bundle.getInt("task");
				int iReson = bundle.getInt("reson");
				// String sErrorMessage = bundle.getString("message");

				Object outParam = null;
				
				if(iReson == DOWNLOAD_STATE_SUCC && sDir!=null && sName !=null && sDir.length()>0 && sName.length()>0)
				{
					File fileSrc = new File(HttpDeal.sTempFolder, String.valueOf(iTaskId));
					File fileDes = new File(sDir, sName);
					try
                    {
	                    notifyService.copyFile(fileSrc, fileDes);
                    }
                    catch (IOException e)
                    {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
                    }
				}

				switch (id)
				{
				case DOWNLOAD_ID_CHECKAPK:
					if (iReson == DOWNLOAD_STATE_SUCC)
					{
						iReson = ParseCheckApp(iTaskId);
						outParam = hashTmp;
					}
					break;
				case DOWNLOAD_ID_DOWNLOADAPK:
					outParam = iTaskId;
					break;
				case DOWNLOAD_ID_REGEDIT:
					outParam = iTaskId;
					break;
				default:
					break;
				}

				if (inf != null)
				{
					inf.download_finish(iReson, oParam, outParam);
				}

				new File(HttpDeal.sTempFolder, String.valueOf(iTaskId)).delete();
			}
		}

		private int ParseCheckApp(int iTaskId)
		{
			if (hashTmp == null)
			{
				hashTmp = new HashMap<String, Object>();
			}

			hashTmp.clear();

			CostomSaxParser.SaxParserExInterface callback = new CostomSaxParser.SaxParserExInterface()
			{
				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes)
				{

				}

				@Override
				public void endElement(String uri, String localName, String qName)
				{
					if (localName.equalsIgnoreCase("url"))
					{
						hashTmp.put("url", qName);
					}
					else if (localName.equalsIgnoreCase("ver"))
					{
						hashTmp.put("ver", qName);
					}
				}

				@Override
				public void characters(char[] ch, int start, int length)
				{
					// TODO Auto-generated method stub

				}
			};

			InputStream in = null;
			try
			{
				in = smartKeyApp.mInstance.GetInputStream(HttpDeal.sTempFolder, String.valueOf(iTaskId));
				if (in == null)
				{
					return DOWNLOAD_STATE_FAIL;
				}
				CostomSaxParser sax = new CostomSaxParser(callback);
				sax.Parse(in);
				in.close();
				in = null;
				return DOWNLOAD_STATE_SUCC;
			}
			catch (Exception e)
			{
				if (in != null)
				{
					try
					{
						in.close();
					}
					catch (IOException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				e.printStackTrace();
			}
			return DOWNLOAD_STATE_FAIL;
		}
	}
}

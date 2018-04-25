package com.zzy.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Debug;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.smartKeyApp;

public class GameAdpter extends BaseAdapter
{
	private List<GameBoxSt> list = null;
	private LayoutInflater la;
	private LruCacheEx<String, Bitmap> mMemoryCache;
	public Bitmap bmpDefault;
	private boolean bRemoveAll;
	private ArrayList<Bitmap> lsRemove;
	public OnDownLoadBmp OnDownLoadBmp;
	//private StringBuilder sbTmp;
	public String sbmpDir;
	private float fDpi;

	public GameAdpter(Context mContext, List<GameBoxSt> list)
	{
		this.list = list;
		la = LayoutInflater.from(mContext);
		
		bmpDefault=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.smartkey_app_more);
		bRemoveAll = false;
		lsRemove = null;
		
		fDpi = mContext.getResources().getDisplayMetrics().density;

		int iFree = (int) (getFreeMemory() / (1024 * 1024));
		mMemoryCache = new LruCacheEx<String, Bitmap>(iFree * 1024 * 1024 / 8)
		{
			@Override
			protected int sizeOf(String key, Bitmap bitmap)
			{
				return GetBitmapSize(bitmap);
			}

			@Override
			protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue)
			{
				if (evicted)
				{
					Common.LogEx("entryRemoved key:" + key);
					if (bRemoveAll)
					{
						if (OnDownLoadBmp != null && lsRemove != null)
						{
							lsRemove.add(oldValue);
						}
					}
					else
					{
						if (OnDownLoadBmp != null)
						{
							OnDownLoadBmp.remove(oldValue);
						}
					}
				}
			}
		};
	}

	private long getFreeMemory()
	{
		Runtime localRuntime;
		long l1 = (localRuntime = Runtime.getRuntime()).maxMemory();
		long l2 = localRuntime.totalMemory();
		long l3 = localRuntime.freeMemory();
		if (Build.VERSION.SDK_INT < 11)
		{
			long l4 = Debug.getNativeHeapAllocatedSize();
			return Math.max(0L, l1 - l2 + l3 - l4);
		}

		return Math.max(0L, l1 - l2 + l3);
	}

	@SuppressLint("NewApi")
    private int GetBitmapSize(Bitmap bmp)
	{
		int size = 0;
		int iver = Build.VERSION.SDK_INT;
		if (iver <= 10)
		{
			size = bmp.getRowBytes() * bmp.getHeight();
		}
		else
		{
			size = bmp.getByteCount();
		}
		return size;
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap)
	{
		if (getBitmapFromMemCache(key) == null)
		{
			mMemoryCache.put(key,bitmap);
		}
	}

	public Bitmap getBitmapFromMemCache(String key)
	{
		return mMemoryCache.get(key);
	}

	public int getCount()
	{
		return this.list.size();
	}

	public GameBoxSt getItem(int position)
	{
		return list.get(position);
	}

	public long getItemId(int position)
	{
		return position;
	}

	public View getView(int position, View pConvertView, ViewGroup parent)
	{
		if (pConvertView == null)
		{
			pConvertView = la.inflate(R.layout.smartkey_game_item, null);
		}

		GameBoxSt stGameBox = list.get(position);
		if (stGameBox == null)
		{
			return pConvertView;
		}

		TextView tvName = (TextView) pConvertView.findViewById(R.id.tvName);
		tvName.setText(stGameBox.sChName);
		
		TextView tvInfo = (TextView) pConvertView.findViewById(R.id.tvInfo);
		tvInfo.setText(stGameBox.sEngName);
		
		ImageView imgvLog = (ImageView) pConvertView.findViewById(R.id.imgvLog);
		Bitmap bitmap = RefreshBmp(stGameBox);
		if(bitmap==null)
		{
			imgvLog.setImageBitmap(bmpDefault);
		}
		else 
		{
			imgvLog.setImageBitmap(bitmap);
		}
		pConvertView.setTag(imgvLog);
		
//		Button btnDownload = (Button) pConvertView.findViewById(R.id.btnDownload);
//		btnDownload.setOnClickListener(onClickListener);

		return pConvertView;

	}
	
	public static Bitmap LoadBitmapSample(File file,int MaxWidth,int MaxHeight) throws IOException
	{
		FileInputStream inputStream;
		inputStream = new FileInputStream(file);
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(inputStream, null, o);
		inputStream.close();
		inputStream = null;
		
		if(MaxWidth==0 || MaxHeight==0)
		{
			return null;
		}

		float r1 = o.outWidth / MaxWidth;
		r1 = Math.max(r1, o.outHeight / MaxHeight);
		BitmapFactory.Options o2 = null;
		if (r1 > 1.0F)
		{
			o2 = new BitmapFactory.Options();
			o2.inSampleSize = (int) r1;
		}
		
		Bitmap bmp = null;
		boolean oomFlag = false;
		
		int retries = 0;
		do
		{
			try
			{
				inputStream =new FileInputStream(file);
				bmp = BitmapFactory.decodeStream(inputStream,null, o2);
				inputStream.close();
				inputStream = null;
				break;
			} 
			catch (OutOfMemoryError oom)
			{
				if (inputStream != null)
				{
					inputStream.close();
				}
				inputStream = null;

				System.gc();
				if (o2 == null)
				{
					o2 = new BitmapFactory.Options();
					o2.inSampleSize = 1;
				}
				o2.inSampleSize *= 2;
				Common.LogEx("Downsampling image due to lack of memory: "+ o2.inSampleSize);
				oomFlag = true;
				retries++;
			}

		} while (retries < 5);
		
		if (bmp == null)
		{
			if (oomFlag)
			{
				Common.LogEx("Error loading bitmap (OutOfMemoryError)");
			}
			Common.LogEx("Error loading bitmap.");
			return null;
		}
		return bmp;
	}
	
	public static String getPicName(GameBoxSt stData)
	{
		StringBuilder sbTmp = smartKeyApp.mInstance.getStringbuild();
		sbTmp.append(stData.iId)
		.append(".dat");
		return sbTmp.toString();
	}
	
	public Bitmap RefreshBmp(GameBoxSt stData)
	{
		
		if(stData.sImgUrl=="")
		{
			Common.LogEx("RefreshBmp icon empty");
			return bmpDefault;
		}
		
		String sNew =getPicName(stData);
		Common.LogEx("RefreshBmp icon sNew:"+sNew);
		
		Bitmap bmp = getBitmapFromMemCache(sNew);
		if(bmp!=null)
		{
			return bmp;
		}
		else 
		{
			try
			{
				File file = new File(sbmpDir,sNew);
				if (file.exists())
				{
					bmp = LoadBitmapSample(file,(int)(72*fDpi), (int)(72*fDpi));
					if (bmp == null)
					{
						stData.sImgUrl = "";
						return bmpDefault;
					}
					else
					{
						addBitmapToMemoryCache(sNew,bmp);
						return bmp;
					}
				}
				else
				{
					if (OnDownLoadBmp != null)
					{
						OnDownLoadBmp.Load(stData,sbmpDir,sNew);
					}
					return bmpDefault;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			return bmpDefault;
		}
	}

	private OnClickListener onClickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{

		}
	};
	
	public void removeAllBmp()
	{
		if(mMemoryCache==null)
		{
			Common.LogEx("removeAllBmp mMemoryCache is null");
			return;
		}
		
		if(mMemoryCache.size()<1)
		{
			Common.LogEx("removeAllBmp mMemoryCache null");
			return;
		}

		bRemoveAll = true;
		if(lsRemove==null)
		{
			lsRemove = new ArrayList<Bitmap>();
		}
		lsRemove.clear();

		mMemoryCache.evictAll();
		if(OnDownLoadBmp !=null && lsRemove !=null)
		{
			OnDownLoadBmp.removeAll(lsRemove);
			lsRemove.clear();
		}
		bRemoveAll = false;
	}

	public interface OnDownLoadBmp
	{
		public void Load(GameBoxSt stData, String sDir, String sName);

		public void remove(Bitmap bmp);

		public void removeAll(ArrayList<Bitmap> lsBmp);
	}

	public static class GameBoxSt
	{
		public int iId;
		public String sChName;
		public String sEngName;
		public String sVerison;
		public String sPackageName;
		public String sApkUrl;
		public String sImgUrl;
		public String sCID;
		public String sType;
		public int iFree;
	}
}

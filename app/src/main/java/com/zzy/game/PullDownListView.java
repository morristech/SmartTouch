package com.zzy.game;

import com.zzy.smarttouch.Common;
import com.zzy.smarttouch.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PullDownListView extends ListView
{
	public static final int STATE_MORE = 0;			
	public static final int STATE_LOADING_MORE =1;
	public static final int STATE_ALL_LOADED =2;
	private int mState = STATE_MORE;
	
	private OnPullDownListener onPullDownListener;
	private TextView tvPullDownFooter;
	private ProgressBar pbPullDownFooter;
	private Object oParam;
	private View mFooterView;
	private boolean bVisibleFooter;
	
	public PullDownListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public PullDownListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public PullDownListView(Context context)
	{
		super(context);
	}
	
	public void show(Object object)
	{
		this.oParam = object;
		addFooter();
		this.setFooterDividersEnabled(false);
		
		Drawable drawable = getContext().getResources().getDrawable(R.color.COLOR_LIST_LINE);
		setDivider(drawable);
		setDividerHeight(2);
		setCacheColorHint(0);
		setFadingEdgeLength(0);
	}
	
	private void addFooter()
	{
		Context context = getContext();
		mFooterView = LayoutInflater.from(context).inflate(R.layout.smartkey_pulldown_footer, null);
		
		mFooterView.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if (getCount() - getHeaderViewsCount() - getFooterViewsCount() > 0) 
				{
					if(onPullDownListener!=null)
					{
						onPullDownListener.PullDown(PullDownListView.this.oParam);
					}
				}
			}
		});
		
		tvPullDownFooter = (TextView) mFooterView.findViewById(R.id.tvPullDownFooter);
		pbPullDownFooter = (ProgressBar)mFooterView.findViewById(R.id.pbPullDownFooter);

		setFooterDividersEnabled(true);
		addFooterView(mFooterView);
		mFooterView.setVisibility(View.GONE);
		bVisibleFooter = false;
	}
	
	public void setOnPullDownListener(OnPullDownListener onPullDownListener)
	{
		this.onPullDownListener = onPullDownListener;
	}
	
	public void setFooterViewVisible(int iVisible)
	{
		if(mFooterView !=null)
		{
			bVisibleFooter = true;
			mFooterView.setVisibility(iVisible);
		}
	}
	
	public int getState()
	{
		return mState;
	}
	
	public void setState(int iState)
	{
		mState = iState;
		if(mState==STATE_MORE)
		{
			tvPullDownFooter.setText(R.string.STR_MORE);
			pbPullDownFooter.setVisibility(View.GONE);
			if(mFooterView!=null)
			{
				if(bVisibleFooter)
				{
					mFooterView.setVisibility(View.VISIBLE);
				}
				else 
				{
					mFooterView.setVisibility(View.GONE);
				}
			}
		}
		else if(mState==STATE_LOADING_MORE)
		{
			tvPullDownFooter.setText(R.string.STR_LOADING);
			pbPullDownFooter.setVisibility(View.VISIBLE);
			if(mFooterView!=null)
			{
				mFooterView.setVisibility(View.VISIBLE);
			}
		}
		else if(mState==STATE_ALL_LOADED)
		{
			tvPullDownFooter.setText(R.string.STR_ALL);
			pbPullDownFooter.setVisibility(View.GONE);
			if(mFooterView!=null)
			{
				if(bVisibleFooter)
				{
					mFooterView.setVisibility(View.VISIBLE);
				}
				else 
				{
					mFooterView.setVisibility(View.GONE);
				}
			}
		}
	}
	
	public interface OnPullDownListener 
	{
		public void PullDown(Object object);
	}
	
	public void clearbitmap(Bitmap bmp)
	{
		for(int i=this.getFirstVisiblePosition();i<=this.getLastVisiblePosition();i++)
		{
			View view=this.getChildAt(i);
			if(view==null)
			{
				Common.LogEx("clearbitmap view null i:"+i);
				continue;
			}
			
			Object object = view.getTag();
			if(object ==null)
			{
				Common.LogEx("clearbitmap object null i:"+i);
				continue;
			}
			
			if(object instanceof ImageView)
			{
				ImageView imgTmp = (ImageView)object;
				imgTmp.setImageBitmap(bmp);
				Common.LogEx("clearbitmap is imageview:"+i);
			}
		}
	}
}

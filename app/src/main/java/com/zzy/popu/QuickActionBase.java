package com.zzy.popu;

import android.content.Context;
import android.widget.PopupWindow.OnDismissListener;

public class QuickActionBase extends PopupWindows implements OnDismissListener
{
	protected OnActionItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;
	protected boolean mDidAction;
	
	public QuickActionBase(Context context)
	{
		super(context);
	}
	
	public void setOnActionItemClickListener(OnActionItemClickListener listener)
	{
		this.mItemClickListener = listener;
	}

	
	public void setOnDismissListener(OnDismissListener listener)
	{
		setOnDismissListener(this);

		this.mDismissListener = listener;
	}

	
	public void onDismiss()
	{
		if ((!this.mDidAction) && (this.mDismissListener != null))
		{
			this.mDismissListener.onDismiss();
		}
	}

	
	public static abstract interface OnActionItemClickListener
	{
		public abstract void onItemClick(QuickActionBase paramQuickActionBase, int paramInt1, int paramInt2);
	}

	
	public static abstract interface OnDismissListener
	{
		public abstract void onDismiss();
	}
}
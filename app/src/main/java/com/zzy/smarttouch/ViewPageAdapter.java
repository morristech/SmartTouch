package com.zzy.smarttouch;

import java.util.ArrayList;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class ViewPageAdapter extends PagerAdapter
{
	public ArrayList<View> mListViews;

	public ViewPageAdapter(ArrayList<View> mListViews)
	{
		this.mListViews = mListViews;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object view)
	{
		container.removeView(mListViews.get(position));
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		container.addView(mListViews.get(position), 0);
		return mListViews.get(position);
	}

	@Override
	public void finishUpdate(View arg0)
	{
	}

	@Override
	public int getCount()
	{
		return mListViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1)
	{
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1)
	{
	}

	@Override
	public Parcelable saveState()
	{
		return null;
	}

	@Override
	public void startUpdate(View arg0)
	{
	}
}
package com.zzy.sortlist;

import java.util.List;
import java.util.Locale;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import com.zzy.smarttouch.R;
import com.zzy.smarttouch.MainClickAdpter.ProgrameSt;

public class SortAdapter extends BaseAdapter implements SectionIndexer
{
	private List<ProgrameSt> list = null;
	private LayoutInflater la;

	public SortAdapter(Context mContext, List<ProgrameSt> list)
	{
		this.list = list;
		la = LayoutInflater.from(mContext);
	}

	public void updateListView(List<ProgrameSt> list)
	{
		this.list = list;
		notifyDataSetChanged();
	}

	public int getCount()
	{
		return this.list.size();
	}

	public ProgrameSt getItem(int position)
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
			pConvertView =la.inflate(R.layout.smartkey_app_more_item, null);
		}
		
		ProgrameSt stPrograme = list.get(position);
		if(stPrograme==null)
		{
			return pConvertView;
		}
		
		TextView tvAppMoreItem= (TextView) pConvertView.findViewById(R.id.tvRecordItem);
		TextView tvAppMoreLetter = (TextView) pConvertView.findViewById(R.id.tvAppMoreLetter);
		ImageView imgvAppMoreItem = (ImageView) pConvertView.findViewById(R.id.imgvRecordItem);

		int section = getSectionForPosition(position);
		if (position == getPositionForSection(section))
		{
			tvAppMoreLetter.setVisibility(View.VISIBLE);
			tvAppMoreLetter.setText(stPrograme.sortLetters);
		}
		else
		{
			tvAppMoreLetter.setVisibility(View.GONE);
		}

		tvAppMoreItem.setText(stPrograme.sAppName);
		imgvAppMoreItem.setImageDrawable(stPrograme.drIcon);

		return pConvertView;

	}
	/**
	 * 根据ListView的当前位置获取分类的首字母的char ascii值
	 */
	public int getSectionForPosition(int position)
	{
		return list.get(position).sortLetters.charAt(0);
	}

	/**
	 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
	 */
	public int getPositionForSection(int section)
	{
		for (int i = 0; i < getCount(); i++)
		{
			String sortStr = list.get(i).sortLetters;
			char firstChar = sortStr.toUpperCase(Locale.US).charAt(0);
			if (firstChar == section)
			{
				return i;
			}
		}

		return -1;
	}

	@Override
	public Object[] getSections()
	{
		return null;
	}
}

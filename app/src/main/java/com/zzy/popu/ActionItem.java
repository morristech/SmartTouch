package com.zzy.popu;

import android.graphics.drawable.Drawable;

public class ActionItem
{
	private Drawable icon;
	private String title;
	private int actionId = -1;
	private boolean selected;
	private boolean sticky;
	private Object mTag;

	public ActionItem(int ActionId, String Title, Drawable Icon)
	{
		this.title = Title;
		this.icon = Icon;
		this.actionId = ActionId;
	}

	public void setTag(Object Tag)
	{
		this.mTag = Tag;
	}

	public Object getTag()
	{
		return this.mTag;
	}

	public void setTitle(String Title)
	{
		this.title = Title;
	}

	public String getTitle()
	{
		return this.title;
	}

	public void setIcon(Drawable Icon)
	{
		this.icon = Icon;
	}

	public Drawable getIcon()
	{
		return this.icon;
	}

	public void setActionId(int ActionId)
	{
		this.actionId = ActionId;
	}

	public int getActionId()
	{
		return this.actionId;
	}

	public void setSticky(boolean Sticky)
	{
		this.sticky = Sticky;
	}

	public boolean getSticky()
	{
		return this.sticky;
	}

	public void setSelected(boolean Selected)
	{
		this.selected = Selected;
	}

	public boolean getSelected()
	{
		return this.selected;
	}
}
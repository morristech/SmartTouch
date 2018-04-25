package com.zzy.popu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.zzy.smarttouch.R;

import java.util.ArrayList;
import java.util.List;

public class QuickAction3D extends QuickActionBase implements OnDismissListener
{
	private View mRootView;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private ViewGroup mTrack;
	private ScrollView mScroller;
	private List<ActionItem> actionItems = new ArrayList<ActionItem>();
	private int mChildPos;
	private int mInsertPos;
	private int mAnimStyle;
	private int mOrientation;
	private int rootWidth = 0;
	private float fDensity;
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	public static final int ANIM_REFLECT = 4;
	
	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_GROW_FROM_REFLET = 4;
	public static final int ANIM_AUTO = 5;

	public QuickAction3D(Context context, int Orientation)
	{
		super(context);
		this.mOrientation = Orientation;
		this.mInflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		fDensity = context.getResources().getDisplayMetrics().density;
		
		if (this.mOrientation == 0)
		{
			setRootViewId(R.layout.smartkey_popup_horizontal);
		}
		else
		{
			setRootViewId(R.layout.smartkey_popup_vertical);
		}
		
		this.mAnimStyle = ANIM_AUTO;
		this.mChildPos = 0;
	}

	public int getItemCount()
	{
		return this.actionItems.size();
	}

	public ActionItem GetActionItem(int Index)
	{
		return (ActionItem) this.actionItems.get(Index);
	}

	public void setRootViewId(int id)
	{
		this.mRootView = ((ViewGroup) this.mInflater.inflate(id, null));
		this.mTrack = (ViewGroup) this.mRootView.findViewById(R.id.tracks);

		this.mArrowDown = (ImageView) this.mRootView.findViewById(R.id.arrow_down);
		this.mArrowUp = (ImageView) this.mRootView.findViewById(R.id.arrow_up);

		this.mScroller = (ScrollView) this.mRootView.findViewById(R.id.scroller);

		this.mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		setContentView(this.mRootView);
	}

	public void setAnimStyle(int AnimStyle)
	{
		this.mAnimStyle = AnimStyle;
	}

	public void AddActionItem(ActionItem Action)
	{
		this.actionItems.add(Action);

		String title = Action.getTitle();
		Drawable icon = Action.getIcon();
		View container;
		if (this.mOrientation == 0)
		{
			container = this.mInflater.inflate(R.layout.smartkey_action_item_horizontal, null);
		}
		else
		{
			container = this.mInflater.inflate(R.layout.smartkey_action_item_vertical, null);
		}

		ImageView img = (ImageView) container.findViewById(R.id.iv_icon);
		TextView text = (TextView) container.findViewById(R.id.tv_title);

		if (icon != null)
		{
			img.setImageDrawable(icon);
		}
		else
		{
			img.setVisibility(View.GONE);
		}

		if (title != null)
		{
			text.setText(title);
		}
		else
		{
			text.setVisibility(View.GONE);
		}

		container.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				ActionItem actionItem =(ActionItem)v.getTag();

				if (QuickAction3D.this.mItemClickListener != null)
				{
					QuickAction3D.this.mItemClickListener.onItemClick(QuickAction3D.this, QuickAction3D.this.mChildPos,actionItem.getActionId());
				}

				if (!actionItem.getSticky())
				{
					QuickAction3D.this.mDidAction = true;
					QuickAction3D.this.dismiss();
				}
			}
		});

		container.setFocusable(true);
		container.setClickable(true);
		container.setTag(Action);

		if ((this.mOrientation == HORIZONTAL) && (this.mChildPos != 0))
		{
			View separator = this.mInflater.inflate(R.layout.smartkey_horiz_separator, null);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);

			separator.setLayoutParams(params);
			separator.setPadding((int) (5.0F * fDensity), 0, (int) (5.0F * fDensity), 0);

			this.mTrack.addView(separator, this.mInsertPos);

			this.mInsertPos += 1;
		}

		this.mTrack.addView(container, this.mInsertPos);

		this.mChildPos += 1;
		this.mInsertPos += 1;
	}

	public void Show(View AnchorView)
	{
		preShow();

		this.mDidAction = false;

		int[] location = new int[2];

		AnchorView.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0] + AnchorView.getWidth(), location[1]+ AnchorView.getHeight());

		this.mRootView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));

		int rootHeight = this.mRootView.getMeasuredHeight();

		if (this.rootWidth == 0)
		{
			this.rootWidth = this.mRootView.getMeasuredWidth();
		}

		int screenWidth = this.mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight = this.mWindowManager.getDefaultDisplay().getHeight();
		int arrowPos;
		int xPos;
		if (anchorRect.left + this.rootWidth > screenWidth)
		{
			xPos = anchorRect.left - (this.rootWidth - AnchorView.getWidth());
			xPos = xPos < 0 ? 0 : xPos;
			arrowPos = anchorRect.centerX() - xPos;
		}
		else
		{
			if (AnchorView.getWidth() > this.rootWidth)
			{
				xPos = anchorRect.centerX() - this.rootWidth / 2;
			}
			else
			{
				xPos = anchorRect.left;
			}

			arrowPos = anchorRect.centerX() - xPos;
		}

		Activity parent = (Activity) AnchorView.getContext();
		Rect rectgle = new Rect();
		Window window = parent.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
		int StatusBarHeight = rectgle.top;

		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;

		boolean onTop = dyTop > dyBottom;
		int yPos;
		;
		if (onTop)
		{
			if (rootHeight > dyTop)
			{
				yPos = StatusBarHeight;
				LayoutParams l = this.mScroller.getLayoutParams();
				l.height = (int) (dyTop - 32.0F * fDensity - StatusBarHeight);
			}
			else
			{
				yPos = anchorRect.top - rootHeight;
			}
		}
		else
		{
			yPos = anchorRect.bottom;

			if (rootHeight > dyBottom)
			{
				LayoutParams l = this.mScroller.getLayoutParams();
				l.height = (int) (dyBottom - 32.0F * fDensity);
			}
		}

		showArrow(onTop ? R.id.arrow_down : R.id.arrow_up, arrowPos);

		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

		this.mWindow.showAtLocation(AnchorView, 0, xPos, yPos);
	}

	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop)
	{
		int arrowPos = requestedX - this.mArrowUp.getMeasuredWidth() / 2;

		switch (this.mAnimStyle)
		{
		case ANIM_GROW_FROM_LEFT:
			this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Left
			        : R.style.Animations_PopDownMenu_Left);
			break;
		case ANIM_GROW_FROM_RIGHT:
			this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Right
			        : R.style.Animations_PopDownMenu_Right);
			break;
		case ANIM_GROW_FROM_CENTER:
			this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Center
			        : R.style.Animations_PopDownMenu_Center);
			break;
		case ANIM_GROW_FROM_REFLET:
			this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Reflect
			        : R.style.Animations_PopDownMenu_Reflect);
			break;
		case ANIM_AUTO:
			if (arrowPos <= screenWidth / 4)
			{
				this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Left
				        : R.style.Animations_PopDownMenu_Left);
			}
			else if ((arrowPos > screenWidth / 4) && (arrowPos < 3 * (screenWidth / 4)))
			{
				this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Center
				        : R.style.Animations_PopDownMenu_Center);
			}
			else
			{
				this.mWindow.setAnimationStyle(onTop ? R.style.Animations_PopUpMenu_Right
				        : R.style.Animations_PopDownMenu_Right);
			}
		}
	}

	private void showArrow(int whichArrow, int requestedX)
	{
		View showArrow = whichArrow == R.id.arrow_up ? this.mArrowUp : this.mArrowDown;
		View hideArrow = whichArrow == R.id.arrow_up ? this.mArrowDown : this.mArrowUp;

		int arrowWidth = this.mArrowUp.getMeasuredWidth();

		showArrow.setVisibility(0);

		MarginLayoutParams param = (MarginLayoutParams) showArrow.getLayoutParams();

		param.leftMargin = (requestedX - arrowWidth / 2);

		hideArrow.setVisibility(4);
	}
}
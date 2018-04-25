package com.zzy.smarttouch;

import java.util.ArrayList;

import com.pocket.network.HttpDeal;
import com.pocket.network.HttpTask;
import com.zxing.CaptureActivity;

import android.R.interpolator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader.ForceLoadContentObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class RegeditActivity extends BaseActivity implements OnClickListener
{
	private final int RESULT_CODE_SCAN = 1;
	
	private LinearLayout lyRegedit;
	private EditText etSerial;
	
	private LinearLayout lyNav;
	private ViewPager vpNav;
	private float fDpi;
	
	private float fStartX=0;
	private float fStartY=0;
	private int iScreenW;
	
	private long iCurrClickTick=0;
	private int iClickCount = 0;
	private int iClickViewID =0;
	
	private TextView tvFirst;
	private TextView tvSecond;
	private TextView tvThree;
	private TextView tvFour;
	
	private int[] arrayColor;
	private int[] arrayIndex;
	private int iCororIndex=0;
	
	private ImageView imgvIndi1;
	private ImageView imgvIndi2;
	private ImageView imgvIndi3;
	private ImageView imgvIndi4;
	private ImageView imgvIndi5;
	private LinearLayout lyIndi;
	
	private ImageView imgvPage1;
	private ImageView imgvPage2;
	private ImageView imgvPage3;
	private ImageView imgvPage4;
	
	private ImageView imgvPageDesc1;
	private ImageView imgvPageDesc2;
	private ImageView imgvPageDesc3;
	private ImageView imgvPageDesc4;
	
	
	private Animation animationTop;
	private Animation animation;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_regedit); 
		
		arrayColor = new int[6];
		arrayColor[0]= Color.rgb(0xff, 0x00, 0x00);
		arrayColor[1]= Color.rgb(0x00, 0xff, 0x00);
		arrayColor[2]= Color.rgb(0x00, 0x00, 0xff);
		arrayColor[3]= Color.rgb(0xff, 0xff, 0x00);
		arrayColor[4]= Color.rgb(0xff, 0x00, 0xff);
		arrayColor[5]= Color.rgb(0x00, 0xff, 0xff);
		
		imgvIndi1 = (ImageView)findViewById(R.id.imgvIndi1);
		imgvIndi2 = (ImageView)findViewById(R.id.imgvIndi2);
		imgvIndi3 = (ImageView)findViewById(R.id.imgvIndi3);
		imgvIndi4 = (ImageView)findViewById(R.id.imgvIndi4);
		imgvIndi5 = (ImageView)findViewById(R.id.imgvIndi5);
		lyIndi = (LinearLayout)findViewById(R.id.lyIndi);
		

		arrayIndex = new int[smartKeyApp.CLICK_STATE_MAX];
		for(int i=0;i<arrayIndex.length;i++)
		{
			arrayIndex[i] =0;
		}
		iCororIndex = 0;
		
		fDpi = getResources().getDisplayMetrics().density;
		iScreenW = getResources().getDisplayMetrics().widthPixels/4;
		
		lyRegedit = (LinearLayout) findViewById(R.id.lyRegedit);
		etSerial = (EditText) findViewById(R.id.etSerial);

		Button btnScan = (Button) findViewById(R.id.btnScan);
		btnScan.setOnClickListener(this);

		Button btnRegedit = (Button) findViewById(R.id.btnRegister);
		btnRegedit.setOnClickListener(this);
		
		lyNav = (LinearLayout)findViewById(R.id.lyNav);
		InitNavViewPage();
		
		if(smartKeyApp.mInstance.isShowRegedit())
		{
			lyRegedit.setVisibility(View.VISIBLE);
			lyNav.setVisibility(View.GONE);
		}
		else
		{
			lyRegedit.setVisibility(View.GONE);
			lyNav.setVisibility(View.VISIBLE);
		}
		
		animationTop = AnimationUtils.loadAnimation(this, R.anim.mf_window_enter);
		animation = AnimationUtils.loadAnimation(this, R.anim.activity_in_from_bottom);
		
		setPage(0);
	}
	
	private void setPage(int index)
	{
		switch (index)
        {
		case 0:
			lyIndi.setBackgroundColor(0xffffffff);
			imgvIndi1.setImageResource(R.drawable.smartkey_page_indicator_1);
			imgvIndi2.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi3.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi4.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi5.setImageResource(R.drawable.smartkey_page_indicator_0);
			
			imgvPage1.startAnimation(animationTop);
			imgvPageDesc1.startAnimation(animation);
			break;
		case 1:
			lyIndi.setBackgroundColor(0xffffffff);
			imgvIndi1.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi2.setImageResource(R.drawable.smartkey_page_indicator_2);
			imgvIndi3.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi4.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi5.setImageResource(R.drawable.smartkey_page_indicator_0);
			
			imgvPage2.startAnimation(animationTop);
			imgvPageDesc2.startAnimation(animation);
			break;
		case 2:
			lyIndi.setBackgroundColor(0xffffffff);
			imgvIndi1.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi2.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi3.setImageResource(R.drawable.smartkey_page_indicator_3);
			imgvIndi4.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi5.setImageResource(R.drawable.smartkey_page_indicator_0);
			
			imgvPage3.startAnimation(animationTop);
			imgvPageDesc3.startAnimation(animation);
			break;
		case 3:
			lyIndi.setBackgroundColor(0xffffffff);
			imgvIndi1.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi2.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi3.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi4.setImageResource(R.drawable.smartkey_page_indicator_4);
			imgvIndi5.setImageResource(R.drawable.smartkey_page_indicator_0);
			
			imgvPage4.startAnimation(animationTop);
			imgvPageDesc4.startAnimation(animation);
			break;
		case 4:
			lyIndi.setBackgroundColor(0xaa000000);
			imgvIndi1.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi2.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi3.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi4.setImageResource(R.drawable.smartkey_page_indicator_0);
			imgvIndi5.setImageResource(R.drawable.smartkey_page_indicator_5);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if(smartKeyApp.mInstance.isShowRegedit())
		{
			if (mService != null && mService.onRegeditState != null)
			{
				Common.StartProgressDialog(this);
			}
		}
		else
		{
			lyRegedit.setVisibility(View.GONE);
			lyNav.setVisibility(View.VISIBLE);
		}
	}
	
	private void InitNavViewPage()
	{
		vpNav = (ViewPager)findViewById(R.id.vpNav);
		ArrayList<View> listViews = new ArrayList<View>();
		LayoutInflater mLayoutInflater = LayoutInflater.from(this);
		View vPageOne = mLayoutInflater.inflate(R.layout.smartkey_page1, vpNav, false);
		View vPageTwo = mLayoutInflater.inflate(R.layout.smartkey_page2, vpNav, false);
		View vPageThree = mLayoutInflater.inflate(R.layout.smartkey_page3, vpNav, false);
		View vPageFour = mLayoutInflater.inflate(R.layout.smartkey_page4, vpNav, false);
		View vPageFive = mLayoutInflater.inflate(R.layout.smartkey_page5, vpNav, false);
		listViews.add(vPageOne);
		listViews.add(vPageTwo);
		listViews.add(vPageThree);
		listViews.add(vPageFour);
		listViews.add(vPageFive);
		
		imgvPage1 = (ImageView)vPageOne.findViewById(R.id.imgvPage1);
		imgvPage2 = (ImageView)vPageTwo.findViewById(R.id.imgvPage2);
		imgvPage3 = (ImageView)vPageThree.findViewById(R.id.imgvPage3);
		imgvPage4 = (ImageView)vPageFour.findViewById(R.id.imgvPage4);
		
		imgvPageDesc1 = (ImageView)vPageOne.findViewById(R.id.imgvPageDesc1);
		imgvPageDesc2 = (ImageView)vPageTwo.findViewById(R.id.imgvPageDesc2);
		imgvPageDesc3= (ImageView)vPageThree.findViewById(R.id.imgvPageDesc3);
		imgvPageDesc4 = (ImageView)vPageFour.findViewById(R.id.imgvPageDesc4);
		
		Button btnFind= (Button)vPageFive.findViewById(R.id.BtnFind);
		btnFind.setOnClickListener(this);
		
		tvFirst =(TextView)vPageFive.findViewById(R.id.tvFirst);
		tvSecond=(TextView)vPageFive.findViewById(R.id.tvSecond);
		tvThree=(TextView)vPageFive.findViewById(R.id.tvThree);
		tvFour=(TextView)vPageFive.findViewById(R.id.tvFour);
		
		LinearLayout lyTapPos= (LinearLayout)vPageFive.findViewById(R.id.lyTapPos);
		lyTapPos.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View view, MotionEvent event)
			{	
				float x,y;
				x = event.getX();
				y = event.getY();
				switch (event.getAction())
		        {
					case MotionEvent.ACTION_DOWN:
						fStartX = x;
						fStartY = y;
						break;
					case MotionEvent.ACTION_UP:
						
						float xCha,yCha;
						xCha = Math.abs(fStartX-x);
						yCha = Math.abs(fStartY-y);
						if(!(xCha<20 && yCha<20))
						{
							return true;
						}
						
						float iPosX = (iScreenW-MainFloater.TAP_WIDTH*fDpi)/2;
						
						int index = (int)(x/iScreenW);
						float iOffset =index*iScreenW+iPosX;
						if(x>iOffset && x<iOffset+MainFloater.TAP_WIDTH*fDpi)
						{
							//onTapClick(index);
							OnClick(index);
						}
						
						break;
						
					case MotionEvent.ACTION_MOVE:
						break;
					default:
						//v.performClick();
						break;
				}
				
			    return true;
			}
		});
		
		

		vpNav.setAdapter(new ViewPageAdapter(listViews));
		vpNav.setCurrentItem(0);
		vpNav.setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override
			public void onPageSelected(int index)
			{
				// TODO Auto-generated method stub
				setPage(index);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int index)
			{
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == RESULT_CODE_SCAN)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				String atype = data.getStringExtra("atype");
				String Value = data.getStringExtra("value");
				Common.LogEx("EntryActivity onActivityResult value:" + Value + " type:" + atype);

				int iLen = Value.length();
				if (iLen > 19)
				{
					etSerial.setText(Value.substring(iLen - 19));
				}
				else
				{
					Toast.makeText(RegeditActivity.this, R.string.STR_INVAILD_SERIAL_NUMBER, Toast.LENGTH_SHORT).show();
				}

			}
		}
	}
	
	@Override
    public void onClick(View view)
    {
		switch (view.getId())
		{
		    case R.id.btnRegister:
				String sValue = etSerial.getText().toString().trim();
				int iLen = sValue.length();
				if (iLen < 1)
				{
					Toast.makeText(view.getContext(), R.string.STR_SERIAL_NUMBER, Toast.LENGTH_SHORT).show();
					return;
				}
				else if (iLen != 19)
				{
					Toast.makeText(view.getContext(), R.string.STR_INVAILD_SERIAL_NUMBER, Toast.LENGTH_SHORT).show();
					return;
				}
				else if (!HttpDeal.bNetOk)
				{
					Toast.makeText(RegeditActivity.this, R.string.STR_REG_NO_NETWORK, Toast.LENGTH_SHORT).show();
					return;
				}
				Common.StartProgressDialog(RegeditActivity.this);
				mService.RegeditRequest(sValue, onRegeditState);
				break;
			case R.id.btnScan:
				Intent intent = new Intent(RegeditActivity.this, CaptureActivity.class);
				startActivityForResult(intent, RESULT_CODE_SCAN);
				break;
			case R.id.BtnFind:
				smartKeyApp.mInstance.SaveNavState();
				Intent intentMain = new Intent(RegeditActivity.this, EntryActivity.class);
				startActivityForResult(intentMain, RESULT_CODE_SCAN);
				this.finish();
				break;
		}
    }
	
	public OnRegeditState onRegeditState = new OnRegeditState()
	{

		@Override
		public void getState(int iState)
		{
			Common.StopProgressDialog();
			if (iState == HttpTask.DOWNLOAD_STATE_SUCC)
			{
				Toast.makeText(RegeditActivity.this, R.string.STR_REG_SUCC, Toast.LENGTH_SHORT).show();
				lyRegedit.setVisibility(View.GONE);
				lyNav.setVisibility(View.VISIBLE);
			}
			else if (iState == HttpTask.DOWNLOAD_STATE_DATA_ERROR)
			{
				Toast.makeText(RegeditActivity.this, R.string.STR_REG_DATA_ERROR, Toast.LENGTH_SHORT).show();
			}
			else if (iState == HttpTask.DOWNLOAD_STATE_NO_NETWORK)
			{
				Toast.makeText(RegeditActivity.this, R.string.STR_REG_NO_NETWORK, Toast.LENGTH_SHORT).show();
			}
			else
			{
				Toast.makeText(RegeditActivity.this, R.string.STR_REG_NETWORK_ERROR, Toast.LENGTH_SHORT).show();
			}
		}
	};

	public interface OnRegeditState
	{
		public void getState(int iState);
	}
	
	private void OnClick(int id)
	{
		int iCount = 0;
		for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
		{
			if(arrayIndex[i]==1)
			{
				iCount++;
			}
		}
		
		if(iCount==smartKeyApp.CLICK_STATE_MAX)
		{
			for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
			{
				arrayIndex[i]=0;
				iCount= 0;
				iCororIndex ++;
				if(iCororIndex>arrayColor.length-1)
				{
					iCororIndex = 0;
				}
			}
		}
		
		arrayIndex[id]=1;
		switch (id)
        {
		case smartKeyApp.CLICK_STATE_ONE:
			tvFirst.setTextColor(arrayColor[iCororIndex]);
			break;
		case smartKeyApp.CLICK_STATE_TWO:
			tvSecond.setTextColor(arrayColor[iCororIndex]);			
			break;
		case smartKeyApp.CLICK_STATE_THRID:
			tvThree.setTextColor(arrayColor[iCororIndex]);
			break;
		case smartKeyApp.CLICK_STATE_FOUR:
			tvFour.setTextColor(arrayColor[iCororIndex]);
			break;
		default:
			break;
		}
		
		if(mService !=null)
		{
			mService.ClickSound();
		}
	}
	
//	public void onTapClick(int id)
//    {
//		long iNow = SystemClock.elapsedRealtime();
//		if(iClickViewID ==id && iClickCount==1 && iNow-iCurrClickTick<500)
//		{
//			int iCount = 0;
//			for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
//			{
//				if(arrayIndex[i]==1)
//				{
//					iCount++;
//				}
//			}
//			
//			if(iCount==smartKeyApp.CLICK_STATE_MAX)
//			{
//				for(int i=0;i<smartKeyApp.CLICK_STATE_MAX;i++)
//				{
//					arrayIndex[i]=0;
//					iCount= 0;
//					iCororIndex ++;
//					if(iCororIndex>arrayColor.length-1)
//					{
//						iCororIndex = 0;
//					}
//				}
//			}
//			
//			arrayIndex[id]=1;
//			switch (id)
//            {
//			case smartKeyApp.CLICK_STATE_ONE:
//				tvFirst.setTextColor(arrayColor[iCororIndex]);
//				break;
//			case smartKeyApp.CLICK_STATE_TWO:
//				tvSecond.setTextColor(arrayColor[iCororIndex]);			
//				break;
//			case smartKeyApp.CLICK_STATE_THRID:
//				tvThree.setTextColor(arrayColor[iCororIndex]);
//				break;
//			case smartKeyApp.CLICK_STATE_FOUR:
//				tvFour.setTextColor(arrayColor[iCororIndex]);
//				break;
//			default:
//				break;
//			}
//			iClickCount = 0;
//			iClickViewID = 0;
//		}
//		else 
//		{
//			iCurrClickTick = iNow;
//			iClickViewID = id;
//			iClickCount=1;
//		}
//    }

}

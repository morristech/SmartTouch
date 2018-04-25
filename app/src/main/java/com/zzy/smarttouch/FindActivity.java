package com.zzy.smarttouch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FindActivity extends BaseActivity
{
	private float fDpi;
	
	private float fStartX=0;
	private float fStartY=0;
	private int iScreenW;
	
	private long iCurrClickTick=0;
	private int iClickCount = 0;
	private int iClickViewID =0;
	
	private int[] arrayColor;
	private int[] arrayIndex;
	private int iCororIndex=0;
	
	private TextView tvFirst;
	private TextView tvSecond;
	private TextView tvThree;
	private TextView tvFour;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_page5); 
		
		arrayColor = new int[6];
		arrayColor[0]= Color.rgb(0xff, 0x00, 0x00);
		arrayColor[1]= Color.rgb(0x00, 0xff, 0x00);
		arrayColor[2]= Color.rgb(0x00, 0x00, 0xff);
		arrayColor[3]= Color.rgb(0xff, 0xff, 0x00);
		arrayColor[4]= Color.rgb(0xff, 0x00, 0xff);
		arrayColor[5]= Color.rgb(0x00, 0xff, 0xff);
		
		arrayIndex = new int[smartKeyApp.CLICK_STATE_MAX];
		for(int i=0;i<arrayIndex.length;i++)
		{
			arrayIndex[i] =0;
		}
		iCororIndex = 0;
		
		fDpi = getResources().getDisplayMetrics().density;
		iScreenW = getResources().getDisplayMetrics().widthPixels/4;
		
		tvFirst =(TextView)findViewById(R.id.tvFirst);
		tvSecond=(TextView)findViewById(R.id.tvSecond);
		tvThree=(TextView)findViewById(R.id.tvThree);
		tvFour=(TextView)findViewById(R.id.tvFour);
		
		Button btnFind= (Button)findViewById(R.id.BtnFind);
		btnFind.setOnClickListener(new OnClickListener()
		{	
			@Override
			public void onClick(View view)
			{
				Intent intentMain = new Intent(FindActivity.this, EntryActivity.class);
				startActivity(intentMain);
				FindActivity.this.finish();
			}
		});
		
		LinearLayout lyTapPos= (LinearLayout)findViewById(R.id.lyTapPos);
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

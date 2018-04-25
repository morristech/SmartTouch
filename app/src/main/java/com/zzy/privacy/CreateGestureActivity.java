package com.zzy.privacy;

import com.zzy.smarttouch.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class CreateGestureActivity extends Activity
{
	private CreateGesturePassword stGesture;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_gesturepassword_create);
		stGesture = new CreateGesturePassword(this,lockInterface);
		stGesture.Entry(); 
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if(stGesture!=null && stGesture.onKeyDown(keyCode, event))
		{
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(stGesture!=null)
		{
			stGesture.onSaveInstanceState(outState);
			return;
		}
	}
	
    @Override  
    protected void onDestroy() 
    {  
        super.onDestroy();  
        if(stGesture!=null)
        {
        	stGesture.ClearAll();
        	stGesture = null;
        }
    }
    
    private LockInterface lockInterface = new  LockInterface()
	{
		
		@Override
		public void finish(Object oParam)
		{
			setResult(Activity.RESULT_OK);
			CreateGestureActivity.this.finish();
			stGesture.ClearAll();
			stGesture= null;
		}
		
		@Override
		public void cancel(Object oParam)
		{
			CreateGestureActivity.this.finish();
			stGesture.ClearAll();
			stGesture= null;
		}
	};
	
}

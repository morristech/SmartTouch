package com.zzy.smarttouch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.zzy.floater.FloatWindowManager;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends Activity
{
	Handler handler = new Handler();
    int iCount = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.smartkey_entry);

        FloatWindowManager.getInstance().setOnCancelResult(new FloatWindowManager.OnCancelResult() {
            @Override
            public void cancelResult() {
                smartKeyApp.mInstance.showToast(R.string.STR_PERMISSION_FAILED);
                MainActivity.this.finish();
            }
        });
	}

    public void applyOrShowFloatWindow(Context context) {
        if (FloatWindowManager.getInstance().checkPermission(context)) {
            MainActivityPermissionsDispatcher.showFileWithCheck(this);
        } else {
            FloatWindowManager.getInstance().applyPermission(context);
        }
        iCount++;
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if(iCount !=0) {
            if (FloatWindowManager.getInstance().checkPermission(this)) {
                MainActivityPermissionsDispatcher.showFileWithCheck(this);
            } else {
                this.finish();
            }
            iCount = 0;
        }
        else {
            applyOrShowFloatWindow(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method

        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.READ_PHONE_STATE})
    void showFile() {
        Intent intent = new Intent(this, notifyService.class);
        startService(intent);

        handler.postDelayed(rbEntry, 1*1000);
    }

//    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    void showRationaleForFile(final PermissionRequest request) {
//        new AlertDialog.Builder(this)
//                .setMessage(R.string.STR_POPU_TITLE)
//                .setPositiveButton(R.string.STR_OK, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        request.proceed();
//                    }
//                })
//                .setNegativeButton(R.string.STR_CANCEL, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        request.cancel();
//                    }
//                })
//                .show();
//    }

    @OnPermissionDenied({Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.READ_PHONE_STATE})
    void showDeniedForFile() {
        smartKeyApp.mInstance.showToast(R.string.STR_PERMISSION_FAILED);
    }

    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE
            ,Manifest.permission.READ_PHONE_STATE})
    void showNeverAskForFile() {
        smartKeyApp.mInstance.showToast(R.string.STR_PERMISSION_FAILED);
    }
	
	
	private Runnable rbEntry = new Runnable()
	{
		
		@Override
		public void run()
		{
			if(smartKeyApp.mInstance.isShowRegedit())
			{
				Intent intent = new Intent(MainActivity.this,RegeditActivity.class);
				startActivity(intent);
			}
			else if(smartKeyApp.mInstance.IsShowNav())
			{
				Intent intent = new Intent(MainActivity.this,RegeditActivity.class);
				startActivity(intent);
			}
			else 
			{
				Intent intent = new Intent(MainActivity.this,EntryActivity.class);
				startActivity(intent);
			}
			
			MainActivity.this.finish();
			
		}
	};
}

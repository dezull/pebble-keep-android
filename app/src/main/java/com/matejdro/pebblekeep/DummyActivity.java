package com.matejdro.pebblekeep;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class DummyActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dummy);
	}

	@Override
	protected void onResume() {		
		super.onResume();		

		System.out.println("start check");
		boolean rooted = RootUtil.isRooted();
		System.out.println("root = " + rooted);

		if (!rooted)
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setTitle("You need ROOT access. Otherwise this application won't work!");
			builder.setPositiveButton("Exit", new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
				
			});
			
			builder.show();
		}		
	}
}

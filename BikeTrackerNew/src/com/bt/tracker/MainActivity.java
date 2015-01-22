package com.bt.tracker;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
	
	private CheckBox cbDetectWifiChanges;
	private TextView serviceStatusText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		serviceStatusText = (TextView) findViewById(R.id.serviceStatusText);
		cbDetectWifiChanges = (CheckBox) findViewById(R.id.cbDetectWifiChanges);
		
		cbDetectWifiChanges.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ComponentName wifiBCreceiver = new ComponentName(getApplicationContext(), WifiStateChangedReceiver.class);
				PackageManager pkgMgr = getApplicationContext().getPackageManager();
				
				if(((CheckBox) v).isChecked()) {
					pkgMgr.setComponentEnabledSetting(wifiBCreceiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
					
					serviceStatusText.setText("Tracking Service is running");
					serviceStatusText.setTextColor(getApplicationContext().getResources().getColor(R.color.color_started));
				} else {
					pkgMgr.setComponentEnabledSetting(wifiBCreceiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
					
					serviceStatusText.setText("Tracking Service is stopped");
					serviceStatusText.setTextColor(getApplicationContext().getResources().getColor(R.color.color_stopped));
				}
			}
		});
	}
}
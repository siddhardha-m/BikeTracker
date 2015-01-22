package com.bt.tracker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.widget.Toast;

public class WifiStateChangedReceiver extends BroadcastReceiver {

	private static String phoneNo;
	private static String apiKey;

	@Override
	public void onReceive(Context context, Intent intent) {
		wifiStateChanged(context, intent);

		phoneNo = context.getResources().getString(R.string.phone_number);
		apiKey = context.getResources().getString(R.string.api_key);
	}

	public void wifiStateChanged(Context context, Intent intent) {

		//String action = intent.getAction();

		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

		if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			Toast.makeText(context, "Wifi Connected", Toast.LENGTH_LONG).show();

			String geolocationApiUrl = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + apiKey;
			new RetrieveLocationTask(context).execute(geolocationApiUrl);

		} else if (netInfo != null && netInfo.isConnectedOrConnecting() && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {

		} else {
			Toast.makeText(context, "Wifi Disconnected", Toast.LENGTH_LONG).show();

			String connectionText = "Disconnected";

			try {
				SmsManager smsMgr = SmsManager.getDefault();
				smsMgr.sendTextMessage(phoneNo, null, connectionText, null, null);
				Toast.makeText(context, "Disconnected SMS sent", Toast.LENGTH_LONG).show();
			}
			catch (Exception e) {
				Toast.makeText(context, "Disconnected SMS sending failed", Toast.LENGTH_LONG).show();
			}
		}
	}

	public String retrieveLocationFromMACaddrs(ArrayList<JSONObject> wifiList, String... url) throws Exception {

		JSONObject jsonMain = new JSONObject();
		jsonMain.put("wifiAccessPoints", wifiList);

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(url[0]);

		post.setEntity(new StringEntity(jsonMain.toString()));
		post.setHeader("Content-type", "application/json");

		HttpResponse response = client.execute(post);

		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		StringBuilder builder = new StringBuilder();

		for (String line = null; (line = reader.readLine()) != null; ) {
			builder.append(line).append("\n");
		}

		JSONTokener tokener = new JSONTokener(builder.toString());
		JSONObject finalResult = new JSONObject(tokener);

		return ((JSONObject) finalResult.get("location")).getString("lat") + "," + ((JSONObject) finalResult.get("location")).getString("lng");
	}


	private class RetrieveLocationTask extends AsyncTask<String, Void, String> {

		private Context context;

		public RetrieveLocationTask(Context context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... url) {
			String result;

			WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			List<ScanResult> scanResults = wifiMgr.getScanResults();

			ArrayList<JSONObject> wifiList = new ArrayList<JSONObject>();

			try {

				for(ScanResult sr : scanResults) {
					JSONObject wifiObject = new JSONObject();
					wifiObject.put("macAddress", sr.BSSID);
					wifiObject.put("signalStrength", sr.level);
					wifiObject.put("channel", sr.frequency);
					wifiList.add(wifiObject);
				}

				result = retrieveLocationFromMACaddrs(wifiList, url);

			} catch(Exception e) {
				result = "";
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(context, result, Toast.LENGTH_LONG).show();

			String connectionText = "Connected: ";

			String locationText = result.equals("") ? "Unable to retrieve the location" : "https://www.google.com/maps?q=" + result;

			try {
				SmsManager smsMgr = SmsManager.getDefault();
				smsMgr.sendTextMessage(phoneNo, null, connectionText + locationText, null, null);
				Toast.makeText(context, "Connected SMS sent", Toast.LENGTH_LONG).show();
			}
			catch (Exception e) {
				Toast.makeText(context, "Connected SMS sending failed", Toast.LENGTH_LONG).show();
			}
		}
	}
}

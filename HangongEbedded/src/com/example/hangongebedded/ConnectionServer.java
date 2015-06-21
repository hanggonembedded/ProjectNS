package com.example.hangongebedded;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;

public class ConnectionServer extends Activity {
	
	private Context mContext;
	private Activity mActivity;
	
	public ConnectionServer(Activity activity)
	{
		mActivity = activity;
		mContext = activity.getBaseContext();
	}
	
    double latitude = 122.12;
    double longitude = 122.12;
	double accuracy = 100;
	LocationManager mLocMgr;

	Location loc;
    @SuppressLint("HandlerLeak")
	public final Handler mHandler = new Handler();
	public Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
        	
        	//latitude = loc.getLatitude();
            //longitude = loc.getLongitude();
            //accuracy = loc.getAccuracy();
			
		//	mTextLat.setText("Lat: " + loc.getLatitude());
		//	mTextLng.setText("Lng: " + loc.getLongitude());
		//	mTextAlt.setText("Alt: " + loc.getAltitude());
		//	mTextAcc.setText("Acc: " + loc.getAccuracy());
        	
			
			new ReadWeatherJSONFeedTask()
					.execute("http://nslab.kau.ac.kr:8080/follow/api/addlatlon.php"
							+ "?id=" + 10
							+ "&lat=" + String.valueOf(latitude)			//멀티콥터 ID와 보낼 주소를 바꿔야 함.
							+ "&lon=" + String.valueOf(longitude));		//멀티콥터의 위치를 서버에 전송.
			
			new ReadJSONFeed().execute("http://nslab.kau.ac.kr:8080/follow/api/target.php");
			
			// 아두이노 보드에 타겟의 위치를 전송하는 코드가 필요함.
			
        	//mHandler.postDelayed(mRunnable, 1000);	//무한반복을 위해 핸들러를 다시 호출.
        }
    };
	
    
	public String readJSONFeed(String URL) {
        StringBuilder stringBuilder = new StringBuilder();
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(URL);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
               // Log.d("JSON", "Success" + );
            } else {
                Log.d("JSON", "Failed to download file");
			}
		} catch (Exception e) {
			Log.d("readJSONFeed", e.getLocalizedMessage());
		}
		return stringBuilder.toString();
	}
 
    private class ReadWeatherJSONFeedTask extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }
 
		protected void onPostExecute(String result) {
			try {
				JSONObject jsonObject = new JSONObject(result);
				JSONObject weatherObservationItems = new JSONObject(
						jsonObject.getString("weatherObservation"));
				
				/*Toast.makeText( getBaseContext(), weatherObservationItems.getString("clouds")
								+ " - " + weatherObservationItems .getString("stationName"), 
								Toast.LENGTH_SHORT).show();*/
				
			} catch (Exception e) {
				Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
			}
		}
    }
    

	private class ReadJSONFeed extends AsyncTask<String, String, String> {
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();
			StringBuilder builder = new StringBuilder();
			HttpPost httppost = new HttpPost(urls[0]);
			try {
				HttpResponse response = httpclient.execute(httppost);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content));
					String line;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return builder.toString();
		}

		protected void onPostExecute(String result) {
			
			String targetInfo = "";
			try {
				JSONArray targetArray = new JSONArray(result);
				for (int i = 0; i < targetArray.length(); i++) {
					JSONObject jObject = targetArray.getJSONObject(i);
					//targetInfo += "ID: " + jObject.getString("target_id") + "\n";
					//targetInfo += "Lat: " + jObject.getString("lat") + "\n";
					//targetInfo += "Lon: " + jObject.getString("lon") + "\n";

					targetInfo += "ID: " + "testr" + "\n";
					targetInfo += "Lat: " + "122.22" + "\n";
					targetInfo += "Lon: " + "222.222" + "\n";

					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//if (targetInfo.trim().length() > 0)
			//	tTextID.setText(targetInfo);
			//else
			//	tTextID.setText("Sorry no match found");
		}
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
		//mTextMsg = (TextView) findViewById(R.id.textMsg);
		//mTextLat = (TextView) findViewById(R.id.textLat);
		//mTextLng = (TextView) findViewById(R.id.textLng);
		//mTextAlt = (TextView) findViewById(R.id.textAlt);
		//mTextAcc = (TextView) findViewById(R.id.textAcc);
		mLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//btnStartFlight = (Button) findViewById(R.id.btnStartFlight);
		//btnStopFlight = (Button) findViewById(R.id.btnStopFlight);
		//mTextSta = (TextView) findViewById(R.id.textFlightStatus);
		//tTextID = (TextView) findViewById(R.id.textTargetID);	

		/*
		btnStartFlight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if (accuracy < 10) {
					mHandler.postDelayed(mRunnable, 1000);
					mTextSta.setText("Start Flight");
				} else
					Toast.makeText(getApplicationContext(),
							"10미터 미만의 오차가 필요합니다.", Toast.LENGTH_LONG).show();
			}
		});
		*/
		/*
		btnStopFlight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	mHandler.removeCallbacks(mRunnable);
            	mTextSta.setText("Stop Flight");
            }
        });*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	LocationListener mLocListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			loc = location;
			
			latitude = location.getLatitude();
            longitude = location.getLongitude();
            accuracy = location.getAccuracy();
			
		//	mTextLat.setText("Lat: " + location.getLatitude());
		//	mTextLng.setText("Lng: " + location.getLongitude());
		//	mTextAlt.setText("Alt: " + location.getAltitude());
		//	mTextAcc.setText("Acc: " + location.getAccuracy());
		}

		public void onProviderDisabled(String provider) {
			//mTextMsg.setText("Provider Disabled");
		}

		public void onProviderEnabled(String provider) {
			//mTextMsg.setText("Provider Enabled");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

			switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				//mTextMsg.setText("Provider Out of Service");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				//mTextMsg.setText("Provider Temporarily Unavailable");
				break;
			case LocationProvider.AVAILABLE:
				//mTextMsg.setText("Provider Available");
				break;
			}
		}
	};
	
	public static Criteria getCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(true);
		criteria.setSpeedRequired(true);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);
		return criteria;
	}

	public void onResume() {
		super.onResume();
		String locProv = mLocMgr.getBestProvider(getCriteria(), true);
		mLocMgr.requestLocationUpdates(locProv, 3000, 3, mLocListener);
		//mTextMsg.setText("Location Service Start");
	}

	public void onPause() {
		super.onPause();
		mLocMgr.removeUpdates(mLocListener);
		//mTextMsg.setText("Location Service Stop");
	}

}

package eom.embedded.mc_android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	TextView mTextMsg;
	TextView mTextLat;
	TextView mTextLng;
	TextView mTextAlt;
	TextView mTextAcc;
	LocationManager mLocMgr;
	Button btnStartFlight;
	Button btnStopFlight;
	TextView mTextSta;
	
	TextView tTextID;
	
	double latitude;
    double longitude;
	double accuracy = 100;

	Location loc;
	

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler();
	private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
        	
        	latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            accuracy = loc.getAccuracy();
			
			mTextLat.setText("Lat: " + loc.getLatitude());
			mTextLng.setText("Lng: " + loc.getLongitude());
			mTextAlt.setText("Alt: " + loc.getAltitude());
			mTextAcc.setText("Acc: " + loc.getAccuracy());
        	
			
			new ReadWeatherJSONFeedTask()
					.execute("http://nslab.kau.ac.kr:8080/follow/api/adddrone.php"
							+ "?altitude=" + 0
							+ "&speed=" + 0
							+ "&battery=" + 0
							+ "&lat=" + String.valueOf(latitude)			//��Ƽ���� ID�� ���� �ּҸ� �ٲ�� ��.
							+ "&lon=" + String.valueOf(longitude));		//��Ƽ������ ��ġ�� ������ ����.
			
			new ReadJSONFeed().execute("http://nslab.kau.ac.kr:8080/follow/api/target.php");
			
			// �Ƶ��̳� ���忡 Ÿ���� ��ġ�� �����ϴ� �ڵ尡 �ʿ���.
			
        	mHandler.postDelayed(mRunnable, 1000);	//���ѹݺ��� ���� �ڵ鷯�� �ٽ� ȣ��.
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
				Toast.makeText( getBaseContext(), weatherObservationItems.getString("clouds")
								+ " - " + weatherObservationItems .getString("stationName"), 
								Toast.LENGTH_SHORT).show();
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
					targetInfo += "ID: " + jObject.getString("target_id") + "\n";
					targetInfo += "Lat: " + jObject.getString("lat") + "\n";
					targetInfo += "Lon: " + jObject.getString("lon") + "\n";
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (targetInfo.trim().length() > 0)
				tTextID.setText(targetInfo);
			else
				tTextID.setText("Sorry no match found");
		}
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextMsg = (TextView) findViewById(R.id.textMsg);
		mTextLat = (TextView) findViewById(R.id.textLat);
		mTextLng = (TextView) findViewById(R.id.textLng);
		mTextAlt = (TextView) findViewById(R.id.textAlt);
		mTextAcc = (TextView) findViewById(R.id.textAcc);
		mLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		btnStartFlight = (Button) findViewById(R.id.btnStartFlight);
		btnStopFlight = (Button) findViewById(R.id.btnStopFlight);
		mTextSta = (TextView) findViewById(R.id.textFlightStatus);
		tTextID = (TextView) findViewById(R.id.textTargetID);	

		btnStartFlight.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if (accuracy < 10) {
					mHandler.postDelayed(mRunnable, 1000);
					mTextSta.setText("Start Flight");
				} else
					Toast.makeText(getApplicationContext(),
							"10���� �̸��� ������ �ʿ��մϴ�.", Toast.LENGTH_LONG).show();
			}
		});
		
		btnStopFlight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
            	mHandler.removeCallbacks(mRunnable);
            	mTextSta.setText("Stop Flight");
            }
        });
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
			
			mTextLat.setText("Lat: " + location.getLatitude());
			mTextLng.setText("Lng: " + location.getLongitude());
			mTextAlt.setText("Alt: " + location.getAltitude());
			mTextAcc.setText("Acc: " + location.getAccuracy());
		}

		public void onProviderDisabled(String provider) {
			mTextMsg.setText("Provider Disabled");
		}

		public void onProviderEnabled(String provider) {
			mTextMsg.setText("Provider Enabled");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

			switch (status) {
			case LocationProvider.OUT_OF_SERVICE:
				mTextMsg.setText("Provider Out of Service");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				mTextMsg.setText("Provider Temporarily Unavailable");
				break;
			case LocationProvider.AVAILABLE:
				mTextMsg.setText("Provider Available");
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
		mTextMsg.setText("Location Service Start");
	}

	public void onPause() {
		super.onPause();
		mLocMgr.removeUpdates(mLocListener);
		mTextMsg.setText("Location Service Stop");
	}
}
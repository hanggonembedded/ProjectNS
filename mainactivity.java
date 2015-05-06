package com.example.gps_send;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
 
/** GPS 샘플 */
public class MainActivity extends Activity {
 
    private Button btnShowLocation;
    private EditText ed1;
    private TextView txtLat;
    private TextView txtLon;
     
    // GPSTracker class
    private GpsInfo gps;
    
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
 
    private class ReadWeatherJSONFeedTask extends AsyncTask
    <String, Void, String> {
        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }
 
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject weatherObservationItems = 
                    new JSONObject(jsonObject.getString("weatherObservation"));
 
                Toast.makeText(getBaseContext(), 
                    weatherObservationItems.getString("clouds") + 
                 " - " + weatherObservationItems.getString("stationName"), 
                 Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("ReadWeatherJSONFeedTask", e.getLocalizedMessage());
            }          
        }
    }

 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        btnShowLocation = (Button) findViewById(R.id.button1);
        ed1 = (EditText) findViewById(R.id.editText1);
        txtLat = (TextView) findViewById(R.id.textView1);
        txtLon = (TextView) findViewById(R.id.textView2);
         
        // GPS 정보를 보여주기 위한 이벤트 클래스 등록
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View arg0) {
            	
            	String myID = ed1.getText().toString();
            	
                gps = new GpsInfo(MainActivity.this);
                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
 
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                     
                    txtLat.setText(String.valueOf(latitude));
                    txtLon.setText(String.valueOf(longitude));
                    
                    new ReadWeatherJSONFeedTask().execute(
                    		"http://nslab.kau.ac.kr:8080/follow/api/addlatlon.php?id=" + myID + "&lat=" + String.valueOf(latitude) + "&lon=" + String.valueOf(longitude));

                    //Toast.makeText( getApplicationContext(), "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude, Toast.LENGTH_LONG).show();
                    Toast.makeText( getApplicationContext(), "위치를 전송하였습니다.", Toast.LENGTH_LONG).show();
                    
                } else {
                    // GPS 를 사용할수 없으므로
                    gps.showSettingsAlert();
                }
            }
        });
    }
}


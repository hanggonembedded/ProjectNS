package com.example.groundmonitor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	//Button groundButton;
	
	//String IP = "220.67.133.58";
	String IP = "220.67.133.58";
	int PORT = 8080;

	Socket socket;
	DataInputStream Input;
	DataOutputStream Output;
	SetSocket setSocket;
	TextView SysView;

	MessageReciver messageReceiver;
	
	//다음 맵 변수
	MapView mapView;
	ViewGroup mapViewContainer;
	double lastLat = 0, lastLon = 0;
	
	//PolyLine으로 선 지도위에 선 그리기
	MapPolyline polyline;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SysView = (TextView)findViewById(R.id.SysView);
		mapViewContainer = (ViewGroup)findViewById(R.id.DaumView);
		
		//groundButton = (Button)findViewById(R.id.groundButton);
		
		if (setSocket == null) {
			setSocket = new SetSocket(IP, PORT);
			setSocket.start();
		} else
			Toast.makeText(getApplicationContext(), "이미 연결 중입니다.",
					Toast.LENGTH_SHORT).show();
		
		messageReceiver = new MessageReciver();
		
		/*
		groundButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					Output.writeBytes("GroundMonitor is already connect....\n");
					Output.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
				
			}
			
		});
		*/
		
		//다음 맵
		
		mapView = new MapView(this);
		mapView.setDaumMapApiKey("0b31a6f448734d8c3592c649dced5bc639bc6c46");
		
		mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(37.53737528, 127.00557633), true);//중심점 변경
		mapViewContainer.addView(mapView);
		
		//PolyLine 지도위에 선 그리기
		polyline = new MapPolyline();
		polyline.setTag(1000);
		polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.	
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public class SetSocket extends Thread {

		int PORT;
		String IP;

		public SetSocket(String IP, int PORT) {
			this.IP = IP;
			this.PORT = PORT;
		}

		public void run() {
			try {
				socket = new Socket(IP, PORT);
				Input = new DataInputStream(socket.getInputStream());
				Output = new DataOutputStream(socket.getOutputStream());
					
				messageReceiver.start();
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	public String chatMessage;

	public class MessageReciver extends Thread {
		@SuppressWarnings("deprecation")
		public void run() {
			try {
				String received;
				
				while ((received = Input.readLine()) != null) {						
					
					chatMessage = received;
								
					Message message = handler.obtainMessage(1, received);
					handler.sendMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void print(Object message) {
		
		String[] receiveResult;
		char checkMsg;
		String Str = null;
		String Latitude = null,Longitude = null;
		double _Latitude = 0, _Longitude = 0; //지도위에 그려지는 좌표
		
		receiveResult = message.toString().split("'");
		
		checkMsg =receiveResult[0].charAt(0);
		Str = receiveResult[1];
		
		if(checkMsg == 'm')
		{
			receiveResult = Str.split(",");
			Latitude = receiveResult[0];
			Longitude = receiveResult[1];
			
			_Latitude = Double.parseDouble(Latitude);
			_Longitude = Double.parseDouble(Longitude);
			
			if(lastLat == 0 && lastLon == 0)
			{
				lastLat = _Latitude;
				lastLon = _Longitude;
			}
			
			
			polyline.addPoint(MapPoint.mapPointWithGeoCoord(lastLat,lastLon));//좌표지정
			polyline.addPoint(MapPoint.mapPointWithGeoCoord(_Latitude, _Longitude));
			
			lastLat = _Latitude;
			lastLon = _Longitude;
			
			// Polyline 지도에 올리기.
			mapView.addPolyline(polyline);
			
			// 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
			MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
			int padding = 100; // px
			
			mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));

			//mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(_Latitude,_L ongitude), true);//중심점 변경
		}
		else
		{
			SysView.append(Str + "\n");
		}
		
		
	}

	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			print(chatMessage);
		}
	};

}

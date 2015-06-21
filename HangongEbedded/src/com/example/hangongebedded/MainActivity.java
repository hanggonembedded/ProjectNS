package com.example.hangongebedded;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import kr.co.driver.serial.FTDriver;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class MainActivity extends Activity {

	private FTDriver mSerial;
	private UsbReceiver mUsbReceiver;

	private static final String ACTION_USB_PERMISSION = "kr.co.andante.mobiledgs.USB_PERMISSION";

	private Boolean SHOW_DEBUG = false;
	private String TAG = "HDJ";

	private int mBaudrate;

	private TextView Tv;
	//private EditText	etLog;

	String IP = "220.67.133.59";
	int PORT = 8080;
	boolean status = false;

	Socket socket;
	DataOutputStream Output;
	DataInputStream Input;
	SetSocket setSocket;
	MessageReciver messageReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Tv = (TextView)findViewById(R.id.TestView);

		//etLog = (EditText)findViewById(R.id.strLog);
		//etLog.setFocusable(false);
		//etLog.setClickable(false);

		mSerial = new FTDriver((UsbManager) getSystemService(Context.USB_SERVICE));

		// listen for new devices
		mUsbReceiver = new UsbReceiver(this, mSerial);

		IntentFilter filter = new IntentFilter();
		filter.addAction (UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction (UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver (mUsbReceiver, filter);

		// load default baud rate
		mBaudrate = mUsbReceiver.loadDefaultBaudrate();

		// for requesting permission
		// setPermissionIntent() before begin()
		PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		mSerial.setPermissionIntent(permissionIntent);

		if (SHOW_DEBUG) {
			Log.d(TAG, "FTDriver beginning");
		}

		//etLog.setTextSize(mUsbReceiver.GetTextFontSize());

		if (mSerial.begin(mBaudrate)) {
			if (SHOW_DEBUG) {
				Log.d(TAG, "FTDriver began");
			}
			mUsbReceiver.loadDefaultSettingValues();
			mUsbReceiver.mainloop();
		} else {
			if (SHOW_DEBUG) {
				Log.d(TAG, "FTDriver no connection");
			}
			Toast.makeText(this, "no connection", Toast.LENGTH_SHORT).show();
		}

		/*
		btWrite = (Button)findViewById(R.id.btnWrite);
		btWrite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mUsbReceiver.writeDataToSerial(etWrite.getText().toString());
			}
		});
		 */

		if (setSocket == null) {
			setSocket = new SetSocket(IP, PORT);
			Tv.append("\nIP: " + IP + "Connected...\n");
			setSocket.start();
			messageReceiver = new MessageReciver();
		} else
			Toast.makeText(getApplicationContext(), "이미 연결 중입니다.",
					Toast.LENGTH_SHORT).show();

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
				mUsbReceiver.SetOutputStream(Output);
				messageReceiver.start();

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onDestroy() {
		mUsbReceiver.closeUsbSerial();
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	public void onSetText(String buf)
	{
		Message message = handler.obtainMessage(1, buf);
		handler.sendMessage(message);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.action_open:
			mUsbReceiver.openUsbSerial();
			break;
		case R.id.action_close:
			mUsbReceiver.closeUsbSerial();
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	 */

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

		Tv.append(message.toString()+"\n");
		/*
		String[] receiveResult;
		String result;
		String Classification;

		receiveResult = message.toString().split("'");
		result = receiveResult[1];
		Classification = receiveResult[0];		

		if(Classification.equals("m"))
		{
			result = result.replaceAll(" ","");

			result = "11" + result;


			byte[] bytes = new java.math.BigInteger(result, 16).toByteArray();

			byte[] resultBytes = new byte[bytes.length-1];


			System.arraycopy(bytes, 1, resultBytes, 0, bytes.length-1);

			mSerial.write(resultBytes);

		}
		else
		{
			mUsbReceiver.writeDataToSerial(result);
		}*/



		int scrollamout = Tv.getLayout().getLineTop(Tv.getLineCount()) - Tv.getHeight();

		if (scrollamout > Tv.getHeight())
		{
			Tv.scrollTo(0, scrollamout);
		}

	}

	Handler handler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			//print(chatMessage);
			Tv.append(message.toString()+"\n");
		}
	};
}

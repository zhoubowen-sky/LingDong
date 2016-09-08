/**
 * 剪贴板Activity
 */
package service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lingdong20.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import connectpc.Connect_PC;


public class ClipBoardActivity extends Activity implements OnClickListener {


	private Socket socket;
	private TextView mResultTextView;
	private Button mStart;
	private Button mStop;
	private Button mBind;
	private Button mUnBind;
	private Context mContext;
	private ClipBoardReceiver mBoardReceiver;

	private ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e(this.getClass().getSimpleName(), "onServiceDisconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e(this.getClass().getSimpleName(), "onServiceConnected");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = this;

		mBoardReceiver = new ClipBoardReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.cybertron.dict.ClipBoardReceiver");
		registerReceiver(mBoardReceiver, filter);
		
		mResultTextView = (TextView) findViewById(R.id.clip_text);
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);
		mBind = (Button) findViewById(R.id.bind);
		mUnBind = (Button) findViewById(R.id.unbind);

		mStart.setOnClickListener(this);
		mStop.setOnClickListener(this);
		mBind.setOnClickListener(this);
		mUnBind.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent mIntent = new Intent();
		switch (v.getId()) {
		case R.id.start:
			mIntent.setClass(ClipBoardActivity.this, ClipBoardService.class);
			mContext.startService(mIntent);
			break;
		case R.id.stop:
			mIntent.setClass(ClipBoardActivity.this, ClipBoardService.class);
			mContext.stopService(mIntent);
			break;

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(mBoardReceiver);
	}
	public static String value=null;
	class ClipBoardReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				  value = (String) bundle.get("clipboardvalue");


				Log.i("tag", "onReceive: " + value);

				Client(Connect_PC.DuiFangde_IP2,1234);

				//Log.i("tag", "onReceive: " + value);

				/*new Thread(new Runnable()
				{
					public void run()
					{
						//Client("192.168.191.4",1234);
						try {

							Thread.sleep(1000);
							Log.i("tag", "onReceive: " + value);

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();*/

//				Intent show = new Intent();
//				show.putExtra(FloatingWindowService.OPERATION,FloatingWindowService.OPERATION_SHOW);
//				show.putExtra("copyValue", value);

			}
		}		
	}


	public void Client(final String ipAddress, final int port)
	{
		socket = null;
		new Thread(new Runnable() {
			public void run() {
				DataOutputStream dos = null;
				try
				{

					socket = new Socket();
					socket.connect(new InetSocketAddress(ipAddress, port));
					dos = new DataOutputStream(socket.getOutputStream());
					dos.writeUTF(10+value);// 传送指令
					dos.flush();

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (dos != null)
						try {
							dos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					if (socket != null)
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}

		}).start();

	}

	
}

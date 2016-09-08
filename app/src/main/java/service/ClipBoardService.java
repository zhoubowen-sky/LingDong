/**
 * 剪贴板服务
 */
package service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * 周博文
 */
public class ClipBoardService extends Service{

	private MyBinder binder = new MyBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		final ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		cm.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {
			@Override
			public void onPrimaryClipChanged() {
				ClipData data = cm.getPrimaryClip();
				Item item = data.getItemAt(0);
				Intent mIntent = new Intent();
				mIntent.setAction("com.cybertron.dict.ClipBoardReceiver");
				mIntent.putExtra("clipboardvalue", item.getText().toString());
				sendBroadcast(mIntent);

				Log.e(this.getClass().getSimpleName(), "========复制文字:"+item.getText());
			}
		});
	}

	@Override
	public void onStart(Intent intent, int startId) {
	}
	
	@Override
	public void onDestroy() {
	}
	
	
	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	public class MyBinder extends Binder{
		ClipBoardService getService(){
			return ClipBoardService.this;
		}
	}
	
}

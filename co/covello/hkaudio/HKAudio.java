package co.covello.hkaudio;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import android.util.Log;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.harman.hkwirelessapi.HKErrorCode;
import com.harman.hkwirelessapi.HKPlayerState;
import com.harman.hkwirelessapi.HKWirelessListener;
import com.harman.hkwirelesscore.HKWirelessUtil;
import com.harman.hkwirelesscore.PcmCodecUtil;
import com.harman.hkwirelesscore.Util;


public class HKAudio extends CordovaPlugin {

	private String TAG="co.covello.HKAudio";
	private static HKWirelessUtil hkwireless = HKWirelessUtil.getInstance();
	private static PcmCodecUtil pcmCodec = PcmCodecUtil.getInstance();
	private final String ERROR_CODE = "error_code";
	private final String ERROR_MSG = "error_msg";
	private int lastErrorCode = -1;
	//private Context context = null;

	
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String errorMsg = bundle.getString(ERROR_MSG);
			int errorCode = bundle.getInt(ERROR_CODE, -1);
			if (lastErrorCode == errorCode && errorCode == HKErrorCode.ERROR_DISC_TIMEOUT.ordinal())
				return;
			//Toast.makeText(context, errorMsg, 1000).show();
			lastErrorCode = errorCode;
		}
	};
	
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
		
		Log.v(TAG,"Init HKAudioPlugins");
	}
	
	public void initializeHKWController(){
		hkwireless.registerHKWirelessControllerListener(new HKWirelessListener(){

			@Override
			public void onPlayEnded() {
				// TODO Auto-generated method stub
				Util.getInstance().setMusicTimeElapse(0);
				Log.i("HKWirelessListener","onPlayEnded");
				
			}

			@Override
			public void onPlaybackStateChanged(int arg0) {
				// TODO Auto-generated method stub
				if (arg0 == HKPlayerState.EPlayerState_Stop.ordinal())
					Util.getInstance().setMusicTimeElapse(0);
			}

			@Override
			public void onPlaybackTimeChanged(int arg0) {
				// TODO Auto-generated method stub
				Util.getInstance().setMusicTimeElapse(arg0);
			}

			@Override
			public void onVolumeLevelChanged(long deviceId, int deviceVolume,
					int avgVolume) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onDeviceStateUpdated(long deviceId, int reason) {
				// TODO Auto-generated method stub
				Util.getInstance().updateDeviceInfor(deviceId);
				
			}

			@Override
			public void onErrorOccurred(int errorCode, String errorMesg) {
				// TODO Auto-generated method stub
				Log.i("HKWirelessListener","hkwErrorOccurred,errorCode="+errorCode+",errorMesg="+errorMesg);

				Message errMsg = new Message();
				Bundle bundle = new Bundle();
				bundle.putInt(ERROR_CODE, errorCode);
				bundle.putString(ERROR_MSG, errorMesg);
				errMsg.setData(bundle);
				handler.sendMessage(errMsg);
				
				
			}
		});

		if (!hkwireless.isInitialized()) {
			hkwireless.initializeHKWirelessController();
			if (hkwireless.isInitialized()) {
				//Toast.makeText(this, "Wireless controller init success", 1000).show();
			} else {
				//Toast.makeText(this, "Wireless controller init fail", 1000).show();
			}
		}

	}
	public String getSoundRecorderDir()
	{
		StringBuilder path = new StringBuilder();
		if (Environment.getExternalStorageState()
				.equals(Environment.MEDIA_MOUNTED)) {
			path.append(Environment.getExternalStorageDirectory().toString());
			path.append("/wirelessomni/soundRecorder");
			return path.toString();
		}
		return null;
	}
	public boolean execute(final String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		
		if(action.equals("start")){
			initializeHKWController();
		}
		else if(action.equals("startRefreshDeviceInfo")){
			hkwireless.startRefreshDeviceInfo();
		}else if(action.equals("stopRefreshDeviceInfo")){
			hkwireless.stopRefreshDeviceInfo();
		}else if(action.equals("refreshDeviceInfoOnce")){
			
		}
		return true;
	}
	
}

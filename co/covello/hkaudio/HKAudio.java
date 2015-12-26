package co.covello.hkaudio;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

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

import com.harman.hkwirelessapi.DeviceObj;
import com.harman.hkwirelessapi.GroupObj;
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
	private int PRE_MUTE_VOLUME=0;
	private boolean IS_MUTED = false;
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
			hkwireless.refreshDeviceInfoOnce();
		}
		else if(action.equals("playCAF")){
			pcmCodec.play(args.getString(0),0);
		}
		else if(action.equals("isPlaying")){
			 // callbackContext.success(pcmCodec.isPlaying());
			JSONObject json = new JSONObject();
			json.put("result", pcmCodec.isPlaying());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("pause")){
			pcmCodec.pause();
		}
		else if(action.equals("stop")){
			pcmCodec.stop();
		}
		else if(action.equals("playCAFFromCertainTime")){
			pcmCodec.play(args.getString(0),args.getInt(1));
		}
		
		else if(action.equals("getPlayerState")){
			HKPlayerState hps = pcmCodec.getPlayerState();
			JSONObject json = new JSONObject();
			json.put("result", hps);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("setVolume")){
			pcmCodec.setVolumeAll(args.getInt(0));
		}
		
		else if(action.equals("setVolumeDevice")){
			pcmCodec.setVolumeDevice(args.getInt(0),args.getInt(1));
		}
		
		else if(action.equals("getVolume")){
			int Volume = pcmCodec.getVolume();
			JSONObject json = new JSONObject();
			json.put("result", Volume);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		else if(action.equals("getDeviceVolume")){
			int Volume = pcmCodec.getDeviceVolume(args.getInt(0));
			JSONObject json = new JSONObject();
			json.put("result", Volume);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getMaximumVolumeLevel")){
			int Volume = pcmCodec.getMaximumVolumeLevel();
			JSONObject json = new JSONObject();
			json.put("result", Volume);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("mute")){
			PRE_MUTE_VOLUME = pcmCodec.getVolume();
			IS_MUTED = true;
			pcmCodec.setVolumeAll(0);
		}
		
		else if(action.equals("unmute")){
			pcmCodec.setVolumeAll(PRE_MUTE_VOLUME);
			IS_MUTED = false;
		}
		else if(action.equals("isMuted")){
			JSONObject json = new JSONObject();
			json.put("result", PRE_MUTE_VOLUME);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("addDeviceToSession")){
				hkwireless.addDeviceToSession(args.getInt(0));
		}
		else if(action.equals("removeDeviceFromSession")){
			hkwireless.removeDeviceFromSession(args.getInt(0));
		}
		
		else if(action.equals("getActiveDeviceCount")){
			
			JSONObject json = new JSONObject();
			json.put("result", hkwireless.getActiveDeviceCount());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getGroupCount")){
			JSONObject json = new JSONObject();
			json.put("result", hkwireless.getGroupCount());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		else if(action.equals("getDeviceCount")){
			
			JSONObject json = new JSONObject();
			json.put("result", hkwireless.getDeviceCount());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceCountInGroupIndex")){
			int groupCount = hkwireless.getGroupCount();
			int groupIndex = groupCount - 1;
			long deviceCount = hkwireless.getDeviceCountInGroupIndex(groupIndex);
			JSONObject json = new JSONObject();
			json.put("result", deviceCount);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceInfoByGroupIndexAndDeviceIndex")){
			int deviceCount = hkwireless.getDeviceCount();
			int groupCount = hkwireless.getGroupCount();
			int groupIndex = groupCount - 1;
			int deviceIndex = deviceCount-1;
			DeviceObj deviceInfo = hkwireless.getDeviceInfoFromTable(groupIndex, deviceIndex);
			
			JSONObject json = new JSONObject();
			json.put("result", deviceInfo.toString());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceInfoByIndex")){
			int deviceCount = hkwireless.getDeviceCount();
			

			int deviceIndex = deviceCount-1;
			DeviceObj deviceInfo = hkwireless.getDeviceInfoByIndex( deviceIndex);
			
			JSONObject json = new JSONObject();
			json.put("result", deviceInfo.toString());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceGroupByDeviceId")){
			
			int deviceId = args.getInt(0);
			GroupObj groupInfo = hkwireless.getDeviceGroupById(deviceId);
			
			JSONObject json = new JSONObject();
			json.put("result", groupInfo.toString());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceInfoById")){
			
			int deviceId = args.getInt(0);
			DeviceObj deviceInfo = hkwireless.getDeviceInfoByIndex(deviceId);
			
			JSONObject json = new JSONObject();
			json.put("result", deviceInfo.toString());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		else if(action.equals("isDeviceAvailable")){
			long deviceId = args.getLong(0);
			JSONObject json = new JSONObject();
			json.put("result", hkwireless.isDeviceActive(deviceId));
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		else if(action.equals("isDeviceActive")){
			long deviceId = args.getLong(0);
			JSONObject json = new JSONObject();
			json.put("result", hkwireless.isDeviceActive(deviceId));
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		else if(action.equals("removeDeviceFromGroup")){
			int groupCount = hkwireless.getGroupCount();
			int groupId = groupCount - 1;
			hkwireless.removeDeviceFromGroup(groupId, args.getLong(0));
		}
		else if(action.equals("getDeviceGroupByIndex")){

			GroupObj groupInfo = hkwireless.getDeviceGroupByIndex( args.getInt(0));
			JSONObject json = new JSONObject();
			json.put("result", groupInfo.toString());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceGroupByGroupId")){

			GroupObj groupInfo = hkwireless.getDeviceGroupById( args.getInt(0));
			JSONObject json = new JSONObject();
			json.put("result", groupInfo.toString());
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceGroupNameByIndex")){

			String groupInfo = hkwireless.getDeviceGroupNameByIndex( args.getInt(0));
			JSONObject json = new JSONObject();
			json.put("result", groupInfo);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("getDeviceGroupIdByIndex")){

			long groupId = hkwireless.getDeviceGroupIdByIndex( args.getInt(0));
			JSONObject json = new JSONObject();
			json.put("result", groupId);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		else if(action.equals("setDeviceName")){

			 hkwireless.setDeviceName( args.getLong(0), args.getString(1));
			
		}
		
		else if(action.equals("setDeviceGroupName")){
			//Mis-spelling on the HK side
			 hkwireless.setDeiceGroupName( args.getInt(0), args.getString(1));
			
		}
		
		else if(action.equals("setDeviceRole")){
			//Mis-spelling on the HK side
			 hkwireless.setDeviceRole( args.getLong(0), args.getInt(1));
			
		}
		
		else if(action.equals("getActiveGroupCount")){

			int activeGroupCount = hkwireless.getActiveGroupCount();
			JSONObject json = new JSONObject();
			json.put("result", activeGroupCount);
			//callbackContext.success(json);
			callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, json));
		}
		
		else if(action.equals("refreshDeviceWiFiSignal")){
			 hkwireless.refreshDeviceWiFiSignal( args.getLong(0));
			
		}
		
		else if(action.equals("getWifiSignalStrengthType")){
			//not here????
			
		}
		
		return true;
	}
	

	
	
}



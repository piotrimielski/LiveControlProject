package com.givevision.livecontrolproject.util;

import android.os.Environment;

public final class Constants {
	public static final String MyPREFERENCES = "GIVEVISION_Pref";
	public static final String CONFIGURATION_PATH = "GiveVision/givevision.properties";
	public static final String CONFIGURATION1_PATH = "GiveVision/givevision1.properties";
	public static final String PREF_L_KEY = "locked";

	public static final String ACTION_SWITCH_MODE = "switch mode";
	public static final CharSequence MSG_MODE = "mode";
	public static final CharSequence MSG_STATE = "status";
	public static final CharSequence MSG_BATTERY = "battery";
	public static final CharSequence MSG_TEMPERATURE ="temperature" ;
	public static final String ACTION_TYPE_PING = "ping";
	public static final String ACTION_TYPE_SOCKET = "socket";
	public static final String ACTION_OK = "ping OK";
	public static final String ACTION_CHECK = "check kit";
	public static final String ACTION_RESET = "reset player in the kit";
	public static final String ACTION_ON_APP = "from app event";
	public static final String ACTION_CAMERA="camera";
	public static final String ACTION_VIDEO="video";
	public static final CharSequence MSG_SIGNAL_RSSI = "rssi";
	public static final String MSG_CHECK_KIT = "check state of kit";
	public static final String MSG_WIFI_DISABLED = "wifi disabled";
	public static final String MSG_WIFI_ENABLED = "wifi enabled";
	public static final String MSG_WIFI_CONNECTED="wifi connected";
	public static final String MSG_NETWORK_UNAVAILABLE="network unavailable";
	public static final String MSG_SIGNAL_QUALITY_OK="signal ok";
	public static final String MSG_MOBILE_CONNECTED="mobile connected";
	public static final String MSG_LOST_CONNECTION="lost connection";
	public static final String MSG_SIGNAL_NOT_STRONG="signal not strong";
	public static final String MSG_ERROR_CODE_SENT="error code sent";
	public static final String MSG_CONNECTING_TO_NETWORK="connecting to network";
	public static final String MSG_NETWORK_ADD_ERROR = "add network error";
	public static final String MSG_NETWORK_EXCEPTION_ERROR = "network exception error";
	public static final String MSG_NETWORK_CONNECTED="network connected";
	public static final String MSG_PLAYER_OK = "player OK";
	public static final String MSG_PLAYER_ENCODER_ERROR = "encoder not found";
	public static final String MSG_PLAYER_ERROR = "player error";
	public static final String MSG_PLAYER_MEDIA_PREPARED = "media prepared";
	public static final String MSG_PLAYER_PLAYBACK_COMPLETE ="playback complete" ;
	public static final String MSG_PLAYER_BUFFERING_UPDATE = "buffering update";
	public static final String MSG_PLAYER_MEDIA_SEEK_COMPLETE = "media seek complete" ;
	public static final String MSG_PLAYER_SET_VIDEO_SIZE = "video sizes set";
	public static final String MSG_PLAYER_VIDEO_RENDERING_START = "video rendering start";
	public static final String MSG_PLAYER_TIMED_TEXT = "timed text";
	public static final String MSG_PLAYER_TIMED_TEXT_ERROR = "timed text null";
	public static final String MSG_PLAYER_SET_VIDEO_SAR = "video sar set";
	public static final String MSG_PLAYER_UNKNOWN_MSG = "player unknown msg ";
	public static final String MSG_ROUTER_ADDR_OK ="router addr OK" ;
	public static final String MSG_ROUTER_ADDR_ERROR = "router addr ERROR";
	public static final String MSG_ENCODER_ADDR_OK = "encoder addr OK";
	public static final String MSG_ENCODER_ADDR_ERROR = "encoder addr ERROR";
	public static final String MSG_CONTROL_ADDR_OK = "control addr OK";
	public static final String MSG_CONTROL_ADDR_ERROR = "control addr ERROR";
	public static final String MSG_CONTROL_SVR_INIT ="ready to send logs" ;

	public static final int KEY_UP = 19;
	public static final int KEY_DOWN = 20;
	public static final int KEY_LEFT = 21;
	public static final int KEY_RIGHT = 22;
	public static final int KEY_TRIGGER = 96;
	public static final int KEY_BACK = 97;
	public static final int KEY_POWER1 = 100;
	public static final int KEY_POWER2 = 62;

	public static final int BYTES_PER_FLOAT = 4;
	public static final int KERNEL_SIZE = 9;



	public static float[] MATRIX_SHARPEN =
    	{  0, -1,  0, 
    	-1,  5, -1,  
    	 0, -1,  0, };
	public static float[] MATRIX__EDGE = 
    	  { -1, -1, -1,
    	-1, 8, -1,
    	-1, -1, -1};

	// Defines a custom Intent action
	public static final String BROADCAST_TTS_ACTION = "TTS service";
	public static final String TTS_STARTED = "started talk";
    
    public static  float MAX_CAM_ZOOM = 8; //6 or 4 for asus
    public static final float MAX_GL_ZOOM = 0.08f;
    public static final boolean TTS = true;

	public static final String ACTION_ZOOM = "zoom";
	public static final String ACTION_MODE = "mode";
	public static final String ACTION_BRIGHTNESS = "brightness";
	public static final String ACTION_REMAPPING = "remapping";
	public static final String ACTION_FLASH = "flash";
	public static final String ACTION_START = "restart";
	public static final String ACTION_LIVE_DEMO = "liveDemo";
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_PAUSE = "pause";
	public static final String ACTION_STOP = "stop";
	public static final String ACTION_ERROR = "error";
	public static final String ACTION_BATTERY = "battery";
	public static final String ACTION_BLUETOOTH = "bluetooth";
	public static final String ACTION_PHOTO_ZOOM = "photoZoom";

	public static final String TYPE_USER = "user";
	public static final String CONF_LOG_FILENAME = "logSightSport.txt";
	public static final String CONF_PATH = Environment.getExternalStorageDirectory()+ "/GiveVision/";;
	public static String file_path;
	public static String pref_key_name = "SIGHTSPORT_Pref";
	public static String pref_key_file_path = "SIGHTSPORT_Pref_file_path";
	public static String pref_key_app_status = "SIGHTSPORT_Pref_app_status";
	public static String folder_name = "SIGHTSPORT";
	public static final String PREF_CLN_KEY = "crashlognbr";

	public static final String[] formats=new String[] {
		   "yyyyMMddHHmmss",
		   "yyyy-MM-dd",
		   "yyyy-MM-dd HH:mm",
		   "yyyy-MM-dd HH:mm:ss",
		   "yyyy-MM-dd HH:mm:ss.SSSZ",
		   "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		 };


}

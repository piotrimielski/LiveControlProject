package com.givevision.livecontrolproject.methodes.wifi;

import java.util.ArrayList;

public class WifiEvent {
//	private final String wifi_msg;
	private ArrayList<String> wifi_msg;
	public WifiEvent(ArrayList<String> wifi_msg) {
		this.wifi_msg = wifi_msg;
	}

	public ArrayList getWifi_msg() {
		return this.wifi_msg;
	}
}

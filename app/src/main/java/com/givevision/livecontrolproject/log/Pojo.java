package com.givevision.livecontrolproject.log;

import com.givevision.livecontrolproject.LogManagement;
import com.givevision.livecontrolproject.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Pojo {

	private String action;
	private String message;
	private String type;
	private String created;
	private String ipAddress;

	public void setPojo(String ipAddress, String action, String message, String type, Date currentTime) {
		this.action = action;
		this.message = message;
		this.type = type;
		this.ipAddress= ipAddress;
		SimpleDateFormat sdf= new SimpleDateFormat(Constants.formats[3], Locale.UK);
		created = sdf.format(currentTime);
	}

	public boolean setPojo(String json){
		try {
			JSONObject obj = new JSONObject(json);
			this.action= (String) obj.get("action");
			this.created= (String) obj.get("created");
			this.message= (String) obj.get("message");
			this.type= (String) obj.get("type");
			this.ipAddress= (String) obj.get("ipAddress");
			LogManagement.Log_d("POJO", "pojo json string : "+obj.toString());
		} catch (Throwable t) {
			LogManagement.Log_e("POJO", "Could not parse malformed JSON: \"" + json + "\""+ t);
			return false;
		}
		return true;
	}

	public String getAction() {
		return this.action;
	}

	public String getMessage() {
		return this.message;
	}

	public String getType() {
		return this.type;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		SimpleDateFormat sdf= new SimpleDateFormat(Constants.formats[3], Locale.UK);
		this.created = sdf.format(created);
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String toJSON() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("ipAddress", getIpAddress());
			jsonObject.put("action", getAction());
			jsonObject.put("type", getType());
			jsonObject.put("created", getCreated());
			jsonObject.put("message", getMessage());
			return jsonObject.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
